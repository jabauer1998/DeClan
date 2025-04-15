package io.github.H20man13.DeClan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ast.Library;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.source.ReaderSource;
import io.github.H20man13.DeClan.common.source.Source;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

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
        String expectedOutput = programName.replace(".declib", ".ilib").replace("standard_library/declan", "standard_library/ir/linkable");
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
        String expectedOutput = programName.replace(".dcl", ".ir").replace("test/declan", "test/ir/linkable");
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
                       + "DATA SECTION\r\n"
                       + "BSS SECTION\r\n"
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
                       + "DATA SECTION\r\n"
                       + " DEF GLOBAL x := 38393 <INT>\r\n"
                       + " DEF GLOBAL z := BNOT y <BOOL>\r\n"
                       + "BSS SECTION\r\n"
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
                       + "DATA SECTION\r\n"
                       + " DEF GLOBAL v := FALSE <BOOL>\r\n"
                       + " DEF GLOBAL z := TRUE <BOOL>\r\n"
                       + "BSS SECTION\r\n"
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
                       + "DATA SECTION\r\n"
                       + " DEF GLOBAL x := 89309 <INT>\r\n"
                       + " DEF GLOBAL z := 438.343 <INT>\r\n"
                       + "BSS SECTION\r\n"
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
                       + "DATA SECTION\r\n"
                       + "BSS SECTION\r\n"
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
                       + " RETURN\r\n";


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
                       + "DATA SECTION\r\n"
                       + " DEF GLOBAL t := \"Text Here\" <STRING>\r\n"
                       + "BSS SECTION\r\n"
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
                       + "DATA SECTION\r\n"
                       + " DEF GLOBAL trueVal := TRUE <BOOL>\r\n"
                       + "BSS SECTION\r\n"
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
        String programName = "test/declan/conversions.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testExpressions(){
        String programName = "test/declan/expressions.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopAdvanced(){
        String programName = "test/declan/ForLoopBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopBasic(){
        String programName = "test/declan/ForLoopBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopBasic2(){
        String programName = "test/declan/ForLoopBasic2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testForLoopBasic3(){
        String programName = "test/declan/ForLoopBasic3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIfStatementAdvanced(){
        String programName = "test/declan/IfStatementAdvanced.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIfStatementBasic(){
        String programName = "test/declan/IfStatementBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testLoops(){
        String programName = "test/declan/loops.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRepeatLoopBasic(){
        String programName = "test/declan/RepeatLoopBasic.dcl";
       testDeclanFileOnICode(programName);
    }

    @Test
    public void testSample(){
        String programName = "test/declan/sample.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest(){
        String programName = "test/declan/test.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest2(){
        String programName = "test/declan/test2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest3(){
        String programName = "test/declan/test3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testTest4(){
        String programName = "test/declan/test4.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String programName = "test/declan/WhileLoopAdvanced.dcl";
        testDeclanFileOnICode(programName);
    }


    @Test
    public void testWhileLoopBasic(){
        String programName = "test/declan/WhileLoopBasic.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testSingleConversion(){
        String programName = "test/declan/SingleConversion.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testSingleConversion2(){
        String programName = "test/declan/SingleConversion2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealAddition(){
        String programName = "test/declan/RealAddition.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealAddition2(){
        String programName = "test/declan/RealAddition2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealAddition3(){
        String programName = "test/declan/RealAddition3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealMultiplication(){
        String programName = "test/declan/RealMultiplication.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealMultiplication2(){
        String programName = "test/declan/RealMultiplication2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealMultipliation3(){
        String programName = "test/declan/RealMultiplication3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIntegerDiv(){
        String programName = "test/declan/IntegerDiv.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testIntegerDiv2(){
        String programName = "test/declan/IntegerDiv2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision(){
        String programName = "test/declan/RealDivision.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision2(){
        String programName = "test/declan/RealDivision2.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision3(){
        String programName = "test/declan/RealDivision3.dcl";
        testDeclanFileOnICode(programName);
    }

    @Test
    public void testRealDivision4(){
        String programName = "test/declan/RealDivision4.dcl";
        testDeclanFileOnICode(programName);
    }
    
    //Now add a bunch of tests to test the standard library
    
    @Test
    public void testConversions(){
        String programName = "standard_library/declan/Conversions.declib";
        testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testIntOperations() {
    	String programName = "standard_library/declan/IntOperations.declib";
    	testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testIo() {
    	String programName =  "standard_library/declan/Io.declib";
    	testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testMath() {
    	String programName = "standard_library/declan/Math.declib";
    	testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testRealOperations() {
    	String programName = "standard_library/declan/RealOperations.declib";
    	testStandardLibraryOnICode(programName);
    }
    
    @Test
    public void testUtils() {
    	String programName = "standard_library/declan/Utils.declib";
    	testStandardLibraryOnICode(programName);
    }
}
