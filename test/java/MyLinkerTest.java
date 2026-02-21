package declan;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import declan.utils.ErrorLog;
import declan.frontend.ast.Program;
import declan.middleware.icode.Lib;
import declan.middleware.icode.Prog;
import declan.utils.source.ReaderSource;
import declan.utils.source.Source;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;
import declan.frontend.MyICodeGenerator;
import declan.frontend.MyIrLexer;
import declan.middleware.MyIrLinker;
import declan.frontend.MyIrParser;
import declan.utils.MyStandardLibrary;

public class MyLinkerTest {
    private static void linkTestProgram(String prgSrc){
        ErrorLog log = new ErrorLog();
        Scanner actualScanner = null;
        Scanner expectedScanner = null;
        try{
            FileReader reader = new FileReader(prgSrc);
            Source source = new ReaderSource(reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, null, log);
            MyDeClanParser parser = new MyDeClanParser(lexer, log);
            Program prog = parser.parseProgram();
            parser.close();
            MyICodeGenerator icodeGen = new MyICodeGenerator(null, log);
            Prog program = icodeGen.generateProgramIr(prog);
            MyStandardLibrary lib = new MyStandardLibrary(log);
            MyIrLinker linker = new MyIrLinker(null, log);
            Prog irCode = linker.performLinkage(program, lib.irIoLibrary(), lib.irMathLibrary(), lib.irConversionsLibrary(), lib.irIntLibrary(), lib.irRealLibrary(), lib.irUtilsLibrary());
            String outputFile = prgSrc.replace("src/declan/test", "src/ir/linked").replace(".dcl", ".ir");
            FileReader fileReader = new FileReader(outputFile);
            expectedScanner = new Scanner(fileReader);
            StringReader sReader = new StringReader(irCode.toString());
            actualScanner = new Scanner(sReader);
            int lineNumber = 0;
            while(expectedScanner.hasNextLine() && actualScanner.hasNextLine()){
                String expectedLine = expectedScanner.nextLine();
                String actualLine = actualScanner.nextLine();
                assertTrue("Expected: " + expectedLine + "\n\nbut found: " + actualLine + "\n at line " + lineNumber, expectedLine.equals(actualLine));
                lineNumber++;
            }
            expectedScanner.close();
            actualScanner.close();
        } catch(Exception exp){
            if(expectedScanner != null){
                expectedScanner.close();
            }
            if(actualScanner != null){
                actualScanner.close();
            }
            assertTrue(exp.toString(), false);
        }
    }
    
    private static void compareProgramStrings(String resultProgram, String expectedProgram){
        String[] resultLines = resultProgram.split("(\n|(\r\n))");
        String[] expLines = resultProgram.split("\r\n");

        for(int i = 0; i < resultLines.length; i++){
            String resultLine = resultLines[i];
            String expLine = expLines[i];
            assertTrue("Expected \n" + expLine + "\n\n but found \n\n" + resultLine + "\n\n at line " + i, expLine.equals(resultLine));
        }
    }

    private static void regenerateProgram(Prog  prog, String expected){
        String program = prog.toString();
        compareProgramStrings(program, expected);
    }

    private static Prog parseProgram(String prog, ErrorLog errLog){
        StringReader reader = new StringReader(prog);
        ReaderSource source = new ReaderSource(reader);
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        return parser.parseProgram();
    }

    private static Lib parseLibrary(String library, ErrorLog errorLog){
        StringReader reader = new StringReader(library);
        ReaderSource source = new ReaderSource(reader);
        MyIrLexer lexer = new MyIrLexer(source, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);
        return parser.parseLibrary();
    }

    private static void linkProgramStrings(String expectedString, String prog, String... libs){
        ErrorLog errLog = new ErrorLog();
        Prog program = parseProgram(prog, errLog);

        Lib[] newLibs = new Lib[libs.length];
        for(int i = 0; i < libs.length; i++){
            newLibs[i] = parseLibrary(libs[i], errLog);
        }

        linkPrograms(errLog, expectedString, program, newLibs);
    }

    private static void linkPrograms(ErrorLog errLog, String expectedString, Prog prog, Lib... libs){
        MyIrLinker linker = new MyIrLinker(null, errLog);
        Prog program = linker.performLinkage(prog, libs);
        regenerateProgram(program, expectedString);
    }

    @Test
    public void linkProgramWithNothingInCommon(){
       String prog1 = "SYMBOL SECTION\n"
                    + "BSS SECTION\n"
                    + " DEF GLOBAL a := 20 <INT>\n"
                    + " DEF GLOBAL v := 30 <INT>\n"
                    + "DATA SECTION\n"
                    + "CODE SECTION\n"
                    + " DEF d := (GLOBAL a) IADD (GLOBAL v) <INT>\n"
                    + "END\n"
                    + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + " DEF GLOBAL a := 3 <INT>\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + " PROC LABEL func\n"
                    + "  DEF a := 3 <INT>\n"
                    + "  DEF RETURN b := a <INT>\n"
                    + " RETURN FROM func\n";

        String res = "SYMBOL SECTION\r\n" + //
                     "DATA SECTION\r\n" + //
                     " DEF GLOBAL a := 20 <INT>\r\n" + //
                     " DEF GLOBAL v := 30 <INT>\r\n" + //
                     "CODE SECTION\r\n" + //
                     " DEF d := a IADD v <INT>\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";

        linkProgramStrings(res, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalVariable(){
        String prog1 = "SYMBOL SECTION\n"
                     + "ENTRY a EXTERNAL lib1VariableName <INT>\n"
                     + "BSS SECTION\n"
                     + " DEF GLOBAL v := 30 <INT>\n"
                     + "DATA SECTION\n"
                     + "CODE SECTION\n"
                     + " d := a IADD v <INT>\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + " ENTRY a INTERNAL lib1VariableName <INT>\n"
                    + "DATA SECTION\n"
                    + " DEF GLOBAL a := 3 <INT>\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + " DEF a := 3 <INT>\n"
                    + " DEF RETURN b := a <INT>\n"
                    + "RETURN FROM func\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " ENTRY c INTERNAL lib1VariableName <INT>\r\n" + //
                     "DATA SECTION\r\n" + //
                     " DEF GLOBAL v := 30 <INT>\r\n" + //
                     " DEF GLOBAL c := 3 <INT>\r\n" + //
                     "BSS SECTION\r\n" +
                     "CODE SECTION\r\n" + //
                     " d := c IADD v <INT>\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void checkVariableRename(){
        String prog1 = "SYMBOL SECTION\n"
                     + "ENTRY b EXTERNAL lib1VariableName <INT>\n"
                     + "BSS SECTION\n"
                     + " DEF GLOBAL v := 30 <INT>\n"
                     + " DEF GLOBAL a := 20 <INT>\n"
                     + "DATA SECTION\n"
                     + "CODE SECTION\n"
                     + " d := b IADD v <INT>\n"
                     + " g := d IADD a <INT>\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "ENTRY a INTERNAL lib1VariableName <INT>\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + " DEF GLOBAL a := 3 <INT>\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + " DEF a := 3 <INT>\n"
                    + " DEF RETURN b := a <INT>\n"
                    + "RETURN FROM func\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " ENTRY c INTERNAL lib1VariableName <INT>\r\n" + //
                     "DATA SECTION\r\n" + //
                     " DEF GLOBAL v := 30 <INT>\r\n" + //
                     " DEF GLOBAL a := 20 <INT>\r\n" + //
                     " DEF GLOBAL c := 3 <INT>\r\n" + //
                     "BSS SECTION\r\n" +
                     "CODE SECTION\r\n" + //
                     " d := c IADD v <INT>\r\n" + //
                     " g := d IADD a <INT>\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";//

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalCall1(){
        String prog1 = "SYMBOL SECTION\n"
                     + " ENTRY v EXTERNAL lib1VariableName <INT>\n"
                     + " ENTRY s EXTERNAL RETURN func <INT>\n"
                     + "BSS SECTION\n"
                     + " DEF GLOBAL a := 20 <INT>\n"
                     + " DEF GLOBAL b := 500 <INT>\n"
                     + "DATA SECTION\n"
                     + "CODE SECTION\n"
                     + " CALL func ( )\n"
                     + " DEF d := (RETURN s) <INT>"
                     + " DEF g := d IADD (GLOBAL v) <INT>\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + " ENTRY a INTERNAL lib1VariableName <INT>\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + " DEF GLOBAL a := 3 <INT>\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + " DEF a := 3 <INT>\n"
                    + " DEF RETURN b := a <INT>\n"
                    + "RETURN FROM func\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " ENTRY f INTERNAL lib1VariableName <INT>\r\n" + //
                     "DATA SECTION\r\n" + //
                     " DEF GLOBAL a := 20 <INT>\r\n" + //
                     " DEF GLOBAL b := 500 <INT>\r\n" + //
                     " DEF GLOBAL f := 3 <INT>\r\n" + //
                     "BSS SECTION\r\n" + //
                     "CODE SECTION\r\n" + //
                     " CALL func()\r\n" + //
                     " DEF d := (RETURN e) <INT>\r\n" + //
                     " DEF g := d IADD (GLOBAL f) <INT>\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func\r\n" + //
                     "  DEF c := 3 <INT>\r\n" + //
                     "  DEF RETURN e := c <INT>\r\n" + //
                     " RETURN FROM func\r\n";

                     

       linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalCall2(){
        String prog1 = "SYMBOL SECTION\n"
                     + " ENTRY v EXTERNAL lib1VariableName <INT>\n"
                     + " ENTRY s EXTERNAL RETURN func2 <INT>\n"
                     + "BSS SECTION\n"
                     + " DEF GLOBAL a := 20 <INT>\n"
                     + " DEF GLOBAL b := 500 <INT>\n"
                     + "DATA SECTION\n"
                     + "CODE SECTION\n"
                     + " CALL func2 ()\n"
                     + " DEF d := (RETURN s) <INT>\n"
                     + " g := d IADD (GLOBAL v) <INT>\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + " ENTRY a INTERNAL lib1VariableName <INT>\n" //The internal Declaration will start out as an A
                    + " ENTRY g EXTERNAL RETURN func2 <INT>\n"
                    + "DATA SECTION\n"
                    + " DEF GLOBAL a := 3 <INT>\n"
                    + "PROC SECTION\n"
                    + " PROC LABEL func2\n"
                    + "  CALL func1()\n"
                    + "  DEF b := (RETURN g) <INT>\n"
                    + "  DEF c := b ISUB (GLOBAL a) <INT>\n"
                    + "  DEF RETURN d := c <INT>\n"
                    + " RETURN FROM func2\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func1\n"
                    + " DEF a := 3 <INT>\n"
                    + " DEF RETURN b := a <INT>\n"
                    + "RETURN FROM func1\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " ENTRY j INTERNAL lib1VariableName <INT>\r\n" + //
                     "DATA SECTION\r\n" + //
                     " DEF GLOBAL a := 20 <INT>\r\n" + //
                     " DEF GLOBAL b := 500 <INT>\r\n" + //
                     " DEF GLOBAL j := 3 <INT>\r\n" + //
                     "BSS SECTION\r\n" +
                     "CODE SECTION\r\n" + //
                     " CALL func2 ( ) <INT>\r\n" + //
                     " DEF e := (RETURN d) <INT>\r\n" + //
                     " DEF g := e IADD (GLOBAL j) <INT>\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func2\r\n" + //
                     "  CALL func1() \r\n" + //
                     "  DEF f := (RETURN i) <INT>\r\n" + //
                     "  DEF c := f ISUB (GLOBAL j) <INT>\r\n" + //
                     "  DEF RETURN d := c <INT>\r\n" + //
                     " RETURN FROM func2\r\n" + //
                     " PROC LABEL func1\r\n" + //
                     "  DEF h := 3 <INT>\r\n" + //
                     "  DEF RETURN i := h <INT>\r\n" + //
                     " RETURN FROM func1\r\n";

         linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkDuplicateLabels(){
        String prog1 = "SYMBOL SECTION\n"
                     + " ENTRY v EXTERNAL lib1VariableName <INT>\n"
                     + " ENTRY s EXTERNAL RETURN func2 <INT>\n"
                     + "BSS SECTION\n"
                     + " DEF GLOBAL a := 20 <INT>\n"
                     + " DEF GLOBAL b := 500 <INT>\n"
                     + "DATA SECTION\n"
                     + "CODE SECTION\n"
                     + " CALL func2 ( )\n"
                     + " DEF d := (RETURN s) <INT>\n"
                     + " DEF g := d IADD (GLOBAL v) <INT>\n"
                     + "LABEL begin2\n"
                     + "IF g IEQ v THEN begin ELSE end\n"
                     + "LABEL begin\n"
                     + "DEF g := d IADD (GLOBAL v) <INT>\n"
                     + "GOTO begin2\n"
                     + "LABEL end\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + " ENTRY a INTERNAL lib1VariableName <INT>\n"
                    + " ENTRY h INTERNAL RETURN func2 <INT>\n"
                    + "DATA SECTION\n"
                    + " DEF GLOBAL a := 3 <INT>\n"
                    + "PROC SECTION\n"
                    + " PROC LABEL func2\n"
                    + "  CALL func1 ()\n"
                    + "  DEF b := (RETURN h) <INT>\n"
                    + "  DEF c := b ISUB (GLOBAL a) <INT>\n"
                    + "  LABEL begin2\n"
                    + "  IF c IEQ b THEN begin ELSE end\n"
                    + "  LABEL begin\n"
                    + "  DEF e := c IADD b <INT>\n"
                    + "  GOTO begin2\n"
                    + "  LABEL end\n"
                    + "  DEF RETURN d := e <INT>\n"
                    + " RETURN FROM func2\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func1\n"
                    + "  DEF a := 3 <INT>\n"
                    + "  LABEL begin2\n"
                    + "  IF a IEQ a THEN begin ELSE end\n"
                    + "  LABEL begin\n"
                    + "  DEF e := a IADD a <INT>\n"
                    + "  GOTO begin2\n"
                    + "  LABEL end\n"
                    + "  DEF RETURN b := e <INT>\n"
                    + "RETURN FROM func1\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " ENTRY l INTERNAL lib1VariableName <INT>\r\n" + //
                     "DATA SECTION\r\n" + //
                     " DEF GLOBAL a := 20 <INT>\r\n" + //
                     " DEF GLOBAL b := 500 <INT>\r\n" + //
                     " DEF GLOBAL l := 3 <INT>\r\n" + //
                     "BSS SECTION\r\n" + //
                     "CODE SECTION\r\n" + //
                     " CALL func2 ()\r\n" + //
                     " DEF f := (RETURN d) <INT>\r\n" + //
                     " DEF g := f IADD (GLOBAL l) <INT>\r\n" + //
                     " LABEL begin2\r\n" + //
                     " IF g EQ (GLOBAL l) THEN begin ELSE end\r\n" + //
                     " LABEL begin\r\n" + //
                     " DEF g := f IADD (GLOBAL l) <INT>\r\n" + //
                     " GOTO begin2\r\n" + //
                     " LABEL end\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func2\r\n" + //
                     "  CALL func1 (  )\r\n" + //
                     "  DEF i := (RETURN j) <INT>\r\n" + //
                     "  DEF c := i ISUB (GLOBAL l) <INT>\r\n" + //
                     "  LABEL begin2_1\r\n" + //
                     "  IF c IEQ i THEN begin_1 ELSE end_1\r\n" + //
                     "  LABEL begin_1\r\n" + //
                     "  DEF h := c IADD i <INT>\r\n" + //
                     "  GOTO begin2_1\r\n" + //
                     "  LABEL end_1\r\n" + //
                     "  DEF RETURN d := h <INT>\r\n" + //
                     " RETURN FROM func2\r\n" + //
                     " PROC LABEL func1\r\n" + //
                     "  DEF k := 3 <INT>\r\n" + //
                     "  LABEL begin2_0\r\n" + //
                     "  IF k EQ k THEN begin_0 ELSE end_0\r\n" + //
                     "  LABEL begin_0\r\n" + //
                     "  DEF e := k IADD k <INT>\r\n" + //
                     "  GOTO begin2_0\r\n" + //
                     "  LABEL end_0\r\n" + //
                     "  DEF RETURN j := e <INT>\r\n" + //
                     " RETURN FROM func1\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void testConversions(){
        String progSrc = "src/declan/test/conversions.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testExpressions(){
        String progSrc = "src/declan/test/expressions.dcl";;
        linkTestProgram(progSrc);
    }

    @Test
    public void testForLoopAdvanced(){
        String progSrc = "src/declan/test/ForLoopAdvanced.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testForLoopBasic(){
        String progSrc = "src/declan/test/ForLoopBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testFoorLoopBasic2(){
        String progSrc = "src/declan/test/ForLoopBasic2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testForLoopBasic3(){
        String progSrc = "src/declan/test/ForLoopBasic3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIfStatementAdvanced(){
        String progSrc = "src/declan/test/IfStatementAdvanced.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIfStatementBasic(){
        String progSrc = "src/declan/test/IfStatementBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testLoops(){
        String progSrc = "src/declan/test/loops.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRepeatLoop(){
        String progSrc = "src/declan/test/RepeatLoopBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testSample(){
        String progSrc = "src/declan/test/Sample.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest(){
        String progSrc = "src/declan/test/test.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest2(){
        String progSrc = "src/declan/test/test2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest3(){
        String progSrc = "src/declan/test/test3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest4(){
        String progSrc = "src/declan/test/test4.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String progSrc = "src/declan/test/WhileLoopAdvanced.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testWhileLoopBasic(){
        String progSrc = "src/declan/test/WhileLoopBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testSingleConversion(){
        String progSrc = "src/declan/test/SingleConversion.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testSingleConversion2(){
        String progSrc = "src/declan/test/SingleConversion2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealAddition(){
        String progSrc = "src/declan/test/RealAddition.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealAddition2(){
        String progSrc = "src/declan/test/RealAddition2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealAddition3(){
        String progSrc = "src/declan/test/RealAddition3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealMultiplication(){
        String progSrc = "src/declan/test/RealMultiplication.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealMultiplication2(){
        String progSrc = "src/declan/test/RealMultiplication2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealMultiplication3(){
        String progSrc = "src/declan/test/RealMultiplication3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIntegerDiv(){
        String progSrc = "src/declan/test/IntegerDiv.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIntegerDiv2(){
        String progSrc = "src/declan/test/IntegerDiv2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealDivision(){
        String progSrc = "src/declan/test/RealDivision.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealDivision4(){
        String progSrc = "src/declan/test/RealDivision4.dcl";
        linkTestProgram(progSrc);
    }
}
