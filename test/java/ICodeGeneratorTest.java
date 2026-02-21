package declan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import declan.utils.ErrorLog;
import declan.frontend.ast.Library;
import declan.frontend.ast.Program;
import declan.middleware.icode.Lib;
import declan.middleware.icode.Prog;
import declan.utils.source.ReaderSource;
import declan.utils.source.Source;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;
import declan.frontend.MyICodeGenerator;
import declan.frontend.MyIrLexer;
import declan.frontend.MyIrParser;

public class ICodeGeneratorTest {
    public static void testReaderFile(Lib program, String programInput){
        try{
            FileReader expectedReader = new FileReader(programInput);
            Scanner expectedScanner = new Scanner(expectedReader);

            StringReader actualReader = new StringReader(program.toString());
            Scanner actualScanner = new Scanner(actualReader);

            int lineNumber = 0;
            while(actualScanner.hasNext() && expectedScanner.hasNext()){
                String actualLine = actualScanner.nextLine();
                String expectedLine = expectedScanner.nextLine();
                assertTrue("At line " + lineNumber + "expected \n...\n\n" + expectedLine + "\n\n but found \n\n" + actualLine, expectedLine.equals(actualLine));
                lineNumber++;
            }

            expectedScanner.close();
            actualScanner.close();
        } catch(FileNotFoundException exp){
            assertTrue(exp.toString(), false);
        }
    }

    public static void testReaderSource(Prog program, String programInput){
        StringReader expectedReader = new StringReader(programInput);
        Scanner expectedScanner = new Scanner(expectedReader);

        StringReader actualReader = new StringReader(program.toString());
        Scanner actualScanner = new Scanner(actualReader);

        int lineNumber = 0;
        while(actualScanner.hasNext() && expectedScanner.hasNext()){
            String actualLine = actualScanner.nextLine();
            String expectedLine = expectedScanner.nextLine();
            assertTrue("At line " + lineNumber + " expected \n...\n\n" + expectedLine + "\n\n but found \n\n" + actualLine, expectedLine.equals(actualLine));
            lineNumber++;
        }

        expectedScanner.close();
        actualScanner.close();
    }
    
    private static void testStandardLibraryOnICode(String programName){
        String expectedOutput = programName.replace(".declib", ".ilib").replace("src/declan/std/lib", "src/ir/std/lib/linkable");
        try{
            Source mySource = new ReaderSource(new FileReader(programName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, null, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Library prog = parser.parseLibrary();
            
            MyICodeGenerator igen = new MyICodeGenerator(null, errLog);
            
            Lib program = igen.generateLibraryIr(prog);
            testReaderFile(program, expectedOutput);

            parser.close();
        } catch(FileNotFoundException exp) {
            assertTrue(exp.toString(), false);
        }
    }

    private static void testDeclanFileOnICode(String programName){
        String expectedOutput = programName.replace(".dcl", ".ir").replace("src/declan/test", "src/ir/linkable");
        try{
            Source mySource = new ReaderSource(new FileReader(programName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, null, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            
            MyICodeGenerator igen = new MyICodeGenerator(null, errLog);
            
            Prog program = igen.generateProgramIr(prog);
            testReaderFile(program, expectedOutput);

            parser.close();
        } catch(FileNotFoundException exp) {
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testBinaryOp(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + " x := 456 <INT>\r\n"
                       + " z := 48393 <INT>\r\n"
                       + " v := x IADD z <INT>\r\n"
                       + " y := v ISUB v <INT>\r\n"
                       + " y := z LOR x <BOOL>\r\n"
                       + " Z := b IOR x <INT>\r\n"
                       + " g := v LAND z <BOOL>\r\n"
                       + " d := v IAND z <INT>\r\n"
                       + " e := v ILSHIFT x <INT>\r\n"
                       + " d := b IRSHIFT f <INT>\r\n"
                       + " e := v LT x <BOOL>\r\n"
                       + " e := i GT g <BOOL>\r\n"
                       + " f := u LE j <BOOL>\r\n"
                       + " h := y GE o <BOOL>\r\n"
                       + " j := h INE u <BOOL>\r\n"
                       + " y := y IEQ u <BOOL>\r\n"
                       + " j := h BNE u <BOOL>\r\n"
                       + " y := y BEQ u <BOOL>\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog prog = parser.parseProgram();
        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }


    @Test
    public void testUnaryOp(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + " DEF GLOBAL x := 38393 <INT>\r\n"
                       + " DEF GLOBAL z := BNOT y <BOOL>\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog prog = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testBooleanAssignment(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + " DEF GLOBAL v := FALSE <BOOL>\r\n"
                       + " DEF GLOBAL z := TRUE <BOOL>\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog prog = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testNumAssignment(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + " DEF GLOBAL x := 89309 <INT>\r\n"
                       + " DEF GLOBAL z := 438.343 <INT>\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog prog = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testProcedureCall(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + " DEF t := 899 <INT>\r\n"
                       + " DEF g := 89 <INT>\r\n"
                       + " DEF f := 98 <INT>\r\n"
                       + " CALL func([t -> x]<INT>, [g -> y]<INT>, [f -> z]<INT>)\r\n"
                       + " DEF x := (RETURN z) <INT>\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n"
                       + " PROC LABEL func\r\n"
                       + "  DEF x := 78 <INT>\r\n"
                       + "  DEF y := 79 <INT>\r\n"
                       + "  DEF RETURN z := 48 <INT>\r\n"
                       + " RETURN FROM func\r\n";


        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testStringDecl(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + " DEF GLOBAL t := \"Text Here\" <STRING>\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testIfStatement(){
        String program = "SYMBOL SECTION\r\n"
                       + "BSS SECTION\r\n"
                       + " DEF GLOBAL trueVal := TRUE <BOOL>\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\n"
                       + " LABEL y\r\n"
                       + " IF x BEQ trueVal\r\n"
                       + " THEN z\r\n"
                       + " ELSE y\r\n"
                       + " LABEL z\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    
    @Test
    public void testDeclanConversions(){
        String programName = "src/declan/test/conversions.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testExpressions(){
        String programName = "src/declan/test/expressions.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopAdvanced(){
        String programName = "src/declan/test/ForLoopBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopBasic(){
        String programName = "src/declan/test/ForLoopBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopBasic2(){
        String programName = "src/declan/test/ForLoopBasic2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopBasic3(){
        String programName = "src/declan/test/ForLoopBasic3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIfStatementAdvanced(){
        String programName = "src/declan/test/IfStatementAdvanced.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIfStatementBasic(){
        String programName = "src/declan/test/IfStatementBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testLoops(){
        String programName = "src/declan/test/loops.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRepeatLoopBasic(){
        String programName = "src/declan/test/RepeatLoopBasic.dcl";
       testDeclanFileOnICode(programName);
    }

    @Test
    public void testSample(){
        String programName = "src/declan/test/sample.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest(){
        String programName = "src/declan/test/test.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest2(){
        String programName = "src/declan/test/test2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest3(){
        String programName = "src/declan/test/test3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest4(){
        String programName = "src/declan/test/test4.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String programName = "src/declan/test/WhileLoopAdvanced.dcl";
        testDeclanFileOnICode(programName);
    }


    @Test
    public void testWhileLoopBasic(){
        String programName = "src/declan/test/WhileLoopBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testSingleConversion(){
        String programName = "src/declan/test/SingleConversion.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testSingleConversion2(){
        String programName = "src/declan/test/SingleConversion2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealAddition(){
        String programName = "src/declan/test/RealAddition.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealAddition2(){
        String programName = "src/declan/test/RealAddition2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealAddition3(){
        String programName = "src/declan/test/RealAddition3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealMultiplication(){
        String programName = "src/declan/test/RealMultiplication.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealMultiplication2(){
        String programName = "src/declan/test/RealMultiplication2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealMultipliation3(){
        String programName = "src/declan/test/RealMultiplication3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIntegerDiv(){
        String programName = "src/declan/test/IntegerDiv.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIntegerDiv2(){
        String programName = "src/declan/test/IntegerDiv2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision(){
        String programName = "src/declan/test/RealDivision.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision2(){
        String programName = "src/declan/test/RealDivision2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision3(){
        String programName = "src/declan/test/RealDivision3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision4(){
        String programName = "src/declan/test/RealDivision4.dcl";
        testDeclanFileOnICode(programName);
    }
    
    //Now add a bunch of tests to test the standard library
    
    @Test
    public void testConversions(){
        String programName = "src/declan/std/lib/Conversions.declib";
        testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testIntOperations() {
        String programName = "src/declan/std/lib/IntOperations.declib";
        testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testIo() {
        String programName =  "src/declan/std/lib/Io.declib";
        testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testMath() {
        String programName = "src/declan/std/lib/Math.declib";
        testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testRealOperations() {
        String programName = "src/declan/std/lib/RealOperations.declib";
        testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testUtils() {
        String programName = "src/declan/std/lib/Utils.declib";
        testStandardLibraryOnICode(programName);
    }
}
