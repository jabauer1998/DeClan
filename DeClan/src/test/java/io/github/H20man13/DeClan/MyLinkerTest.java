package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrLinker;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class MyLinkerTest {
    private static void linkTestProgram(String prgSrc){
        ErrorLog log = new ErrorLog();
        Scanner actualScanner = null;
        Scanner expectedScanner = null;
        try{
            FileReader reader = new FileReader(prgSrc);
            Source source = new ReaderSource(reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, log);
            MyDeClanParser parser = new MyDeClanParser(lexer, log);
            Program prog = parser.parseProgram();
            parser.close();
            MyStandardLibrary lib = new MyStandardLibrary(log);
            MyIrLinker linker = new MyIrLinker(log);
            Prog irCode = linker.performLinkage(prog, lib.ioLibrary(), lib.mathLibrary(), lib.conversionsLibrary(), lib.intLibrary(), lib.realLibrary(), lib.utilsLibrary());
            String outputFile = prgSrc.replace("test/declan", "test/ir/linked").replace(".dcl", ".ir");
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
        MyIrLinker linker = new MyIrLinker(errLog);
        Prog program = linker.performLinkage(prog, libs);
        regenerateProgram(program, expectedString);
    }

    @Test
    public void linkProgramWithNothingInCommon(){
       String prog1 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + " GLOBAL a := 20 [INT]\n"
                    + " GLOBAL v := 30 [INT]\n"
                    + "CODE SECTION\n"
                    + " d := a IADD v [INT]\n"
                    + "END\n"
                    + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + " GLOBAL a := 3 [INT]\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + " PROC LABEL func\n"
                    + "  a := 3 [INT]\n"
                    + "  INTERNAL RETURN b := a [INT]\n"
                    + " RETURN\n";

        String res = "SYMBOL SECTION\r\n" + //
                     "DATA SECTION\r\n" + //
                     " GLOBAL a := 20 [INT]\r\n" + //
                     " GLOBAL v := 30 [INT]\r\n" + //
                     "CODE SECTION\r\n" + //
                     " d := a IADD v [INT]\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";

        linkProgramStrings(res, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalVariable(){
        String prog1 = "SYMBOL SECTION\n"
                     + "a EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " GLOBAL v := 30 [INT]\n"
                     + "CODE SECTION\n"
                     + " d := a IADD v [INT]\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + " a INTERNAL lib1VariableName\n"
                    + "DATA SECTION\n"
                    + " GLOBAL a := 3 [INT]\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + " a := 3 [INT]\n"
                    + " INTERNAL RETURN b := a [INT]\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " c INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " GLOBAL v := 30 [INT]\r\n" + //
                     " GLOBAL c := 3 [INT]\r\n" + //
                     "CODE SECTION\r\n" + //
                     " d := c IADD v [INT]\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void checkVariableRename(){
        String prog1 = "SYMBOL SECTION\n"
                     + "b EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " GLOBAL v := 30 [INT]\n"
                     + " GLOBAL a := 20 [INT]\n"
                     + "CODE SECTION\n"
                     + " d := b IADD v [INT]\n"
                     + " g := d IADD a [INT]\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + " GLOBAL a := 3 [INT]\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + " a := 3 [INT]\n"
                    + " INTERNAL RETURN b := a [INT]\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " c INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " GLOBAL v := 30 [INT]\r\n" + //
                     " GLOBAL a := 20 [INT]\r\n" + //
                     " GLOBAL c := 3 [INT]\r\n" + //
                    "CODE SECTION\r\n" + //
                    " d := c IADD v [INT]\r\n" + //
                    " g := d IADD a [INT]\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n";//

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalCall1(){
        String prog1 = "SYMBOL SECTION\n"
                     + "v EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " GLOBAL a := 20 [INT]\n"
                     + " GLOBAL b := 500 [INT]\n"
                     + "CODE SECTION\n"
                     + " d := EXTERNAL CALL func ( ) [INT]\n"
                     + " g := d IADD v [INT]\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + "GLOBAL a := 3 [INT]\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + " a := 3 [INT]\n"
                    + " INTERNAL RETURN b := a [INT]\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " f INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " GLOBAL a := 20 [INT]\r\n" + //
                     " GLOBAL b := 500 [INT]\r\n" + //
                     " GLOBAL f := 3 [INT]\r\n" + //
                     "CODE SECTION\r\n" + //
                     " CALL func()\r\n" + //
                     " EXTERNAL RETURN d := e [INT]\r\n" + //
                     " g := d IADD f [INT]\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func\r\n" + //
                     "  c := 3 [INT]\r\n" + //
                     "  INTERNAL RETURN e := c [INT]\r\n" + //
                     " RETURN\r\n";

                     

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalCall2(){
        String prog1 = "SYMBOL SECTION\n"
                     + "v EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " GLOBAL a := 20 [INT]\n"
                     + " GLOBAL b := 500 [INT]\n"
                     + "CODE SECTION\n"
                     + " d := EXTERNAL CALL func2 () [INT]\n"
                     + " g := d IADD v [INT]\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + " GLOBAL a := 3 [INT]\n"
                    + "PROC SECTION\n"
                    + " PROC LABEL func2\n"
                    + "  b := EXTERNAL CALL func1() [INT]\n"
                    + "  c := b ISUB a [INT]\n"
                    + "  INTERNAL RETURN d := c [INT]\n"
                    + " RETURN\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func1\n"
                    + " a := 3 [INT]\n"
                    + " INTERNAL RETURN b := a [INT]\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                    " j INTERNAL lib1VariableName\r\n" + //
                    "DATA SECTION\r\n" + //
                    " GLOBAL a := 20 [INT]\r\n" + //
                    " GLOBAL b := 500 [INT]\r\n" + //
                    " GLOBAL j := 3 [INT]\r\n" + //
                    "CODE SECTION\r\n" + //
                    " CALL func2 ( ) [INT]\r\n" + //
                    " EXTERNAL RETURN e := d [INT]\r\n" + //
                    " g := e IADD j [INT]\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n" + //
                    " PROC LABEL func2\r\n" + //
                    "  CALL func1() [INT]\r\n" + //
                    "  EXTERNAL RETURN f := i [INT]\r\n" + //
                    "  c := f ISUB j [INT]\r\n" + //
                    "  INTERNAL RETURN d := c [INT]\r\n" + //
                    " RETURN\r\n" + //
                    " PROC LABEL func1\r\n" + //
                    "  h := 3 [INT]\r\n" + //
                    "  INTERNAL RETURN i := h [INT]\r\n" + //
                    " RETURN\r\n";

         linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkDuplicateLabels(){
        String prog1 = "SYMBOL SECTION\n"
                      + "v EXTERNAL lib1VariableName\n"
                      + "DATA SECTION\n"
                      + " GLOBAL a := 20 [INT]\n"
                      + " GLOBAL b := 500 [INT]\n"
                      + "CODE SECTION\n"
                      + " d := EXTERNAL CALL func2 ( ) [INT]\n"
                      + " g := d IADD v [INT]\n"
                      + "LABEL begin2\n"
                      + "IF g EQ v THEN begin ELSE end\n"
                      + "LABEL begin\n"
                      + "g := d IADD v [INT]\n"
                      + "GOTO begin2\n"
                      + "LABEL end\n"
                      + "END\n"
                      + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                      + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                      + "DATA SECTION\n"
                      + "GLOBAL a := 3 [INT]\n"
                      + "PROC SECTION\n"
                      + " PROC LABEL func2\n"
                      + "  b := EXTERNAL CALL func1 () [INT]\n"
                      + "  c := b ISUB a [INT]\n"
                      + "  LABEL begin2\n"
                      + "  IF c EQ b THEN begin ELSE end\n"
                      + "  LABEL begin\n"
                      + "  e := c IADD b [INT]\n"
                      + "  GOTO begin2\n"
                      + "  LABEL end\n"
                      + "  INTERNAL RETURN d := e [INT]\n"
                      + " RETURN\n";

        String lib2 = "SYMBOL SECTION\n"
                      + "DATA SECTION\n"
                      + "PROC SECTION\n"
                      + "PROC LABEL func1\n"
                      + "  a := 3 [INT]\n"
                      + "  LABEL begin2\n"
                      + "  IF a EQ a THEN begin ELSE end\n"
                      + "  LABEL begin\n"
                      + "  e := a IADD a [INT]\n"
                      + "  GOTO begin2\n"
                      + "  LABEL end\n"
                      + " INTERNAL RETURN b := e [INT]\n"
                      + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                    " l INTERNAL lib1VariableName\r\n" + //
                    "DATA SECTION\r\n" + //
                    " GLOBAL a := 20 [INT]\r\n" + //
                    " GLOBAL b := 500 [INT]\r\n" + //
                    " GLOBAL l := 3 [INT]\r\n" + //
                    "CODE SECTION\r\n" + //
                    " CALL func2 () [INT]\r\n" + //
                    " EXTERNAL RETURN f := d [INT]\r\n" + //
                    " g := f IADD l [INT]\r\n" + //
                    " LABEL begin2\r\n" + //
                    " IF g EQ l THEN begin ELSE end\r\n" + //
                    " LABEL begin\r\n" + //
                    " g := f IADD l [INT]\r\n" + //
                    " GOTO begin2\r\n" + //
                    " LABEL end\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n" + //
                    " PROC LABEL func2\r\n" + //
                    "  CALL func1 (  )\r\n" + //
                    "  EXTERNAL RETURN i := j [INT]\r\n" + //
                    "  c := i ISUB l [INT]\r\n" + //
                    "  LABEL begin2_1\r\n" + //
                    "  IF c EQ i THEN begin_1 ELSE end_1\r\n" + //
                    "  LABEL begin_1\r\n" + //
                    "  h := c IADD i [INT]\r\n" + //
                    "  GOTO begin2_1\r\n" + //
                    "  LABEL end_1\r\n" + //
                    "  INTERNAL RETURN d := h [INT]\r\n" + //
                    " RETURN\r\n" + //
                    " PROC LABEL func1\r\n" + //
                    "  k := 3 [INT]\r\n" + //
                    "  LABEL begin2_0\r\n" + //
                    "  IF k EQ k THEN begin_0 ELSE end_0\r\n" + //
                    "  LABEL begin_0\r\n" + //
                    "  e := k IADD k [INT]\r\n" + //
                    "  GOTO begin2_0\r\n" + //
                    "  LABEL end_0\r\n" + //
                    "  INTERNAL RETURN j := e [INT]\r\n" + //
                    " RETURN\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void testConversions(){
        String progSrc = "test/declan/conversions.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testExpressions(){
        String progSrc = "test/declan/expressions.dcl";;
        linkTestProgram(progSrc);
    }

    @Test
    public void testForLoopAdvanced(){
        String progSrc = "test/declan/ForLoopAdvanced.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testForLoopBasic(){
        String progSrc = "test/declan/ForLoopBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testFoorLoopBasic2(){
        String progSrc = "test/declan/ForLoopBasic2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testForLoopBasic3(){
        String progSrc = "test/declan/ForLoopBasic3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIfStatementAdvanced(){
        String progSrc = "test/declan/IfStatementAdvanced.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIfStatementBasic(){
        String progSrc = "test/declan/IfStatementBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testLoops(){
        String progSrc = "test/declan/loops.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRepeatLoop(){
        String progSrc = "test/declan/RepeatLoopBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testSample(){
        String progSrc = "test/declan/Sample.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest(){
        String progSrc = "test/declan/test.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest2(){
        String progSrc = "test/declan/test2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest3(){
        String progSrc = "test/declan/test3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testTest4(){
        String progSrc = "test/declan/test4.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String progSrc = "test/declan/WhileLoopAdvanced.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testWhileLoopBasic(){
        String progSrc = "test/declan/WhileLoopBasic.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testSingleConversion(){
        String progSrc = "test/declan/SingleConversion.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testSingleConversion2(){
        String progSrc = "test/declan/SingleConversion2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealAddition(){
        String progSrc = "test/declan/RealAddition.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealAddition2(){
        String progSrc = "test/declan/RealAddition2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealAddition3(){
        String progSrc = "test/declan/RealAddition3.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealMultiplication(){
        String progSrc = "test/declan/RealMultiplication.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealMultiplication2(){
        String progSrc = "test/declan/RealMultiplication2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIntegerDiv(){
        String progSrc = "test/declan/IntegerDiv.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testIntegerDiv2(){
        String progSrc = "test/declan/IntegerDiv2.dcl";
        linkTestProgram(progSrc);
    }

    @Test
    public void testRealDivision(){
        String progSrc = "test/declan/RealDivision.dcl";
        linkTestProgram(progSrc);
    }
}
