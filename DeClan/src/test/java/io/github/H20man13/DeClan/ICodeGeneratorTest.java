package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

public class ICodeGeneratorTest {
    public static void testReaderSource(Prog program, String programInput){
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

    private static void testDeclanFileOnICode(String programName){
        String expectedOutput = programName.replace(".dcl", ".ir").replace("test/declan", "test/ir/linkable");
        try{
            Source mySource = new ReaderSource(new FileReader(programName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            
            MyICodeGenerator igen = new MyICodeGenerator(errLog);
            
            Prog program = igen.generateProgramIr(prog);
            testReaderSource(program, expectedOutput);

            parser.close();
        } catch(FileNotFoundException exp) {
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testBinaryOp(){
        String program = "SYMBOL SECTION\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + " x := 456 [INT]\r\n"
                       + " z := 48393 [INT]\r\n"
                       + " v := x IADD z [INT]\r\n"
                       + " y := v ISUB v [INT]\r\n"
                       + " e := y IMUL g [INT]\r\n"
                       + " v := x RADD z [REAL]\r\n"
                       + " y := v RSUB v [REAL]\r\n"
                       + " e := y RMUL g [REAL]\r\n"
                       + " y := z LOR x [BOOL]\r\n"
                       + " Z := b IOR x [INT]\r\n"
                       + " g := v LAND z [BOOL]\r\n"
                       + " d := v IAND z [INT]\r\n"
                       + " e := v ILSHIFT x [INT]\r\n"
                       + " d := b IRSHIFT f [INT]\r\n"
                       + " e := v LT x [BOOL]\r\n"
                       + " e := i GT g [BOOL]\r\n"
                       + " f := u LE j [BOOL]\r\n"
                       + " h := y GE o [BOOL]\r\n"
                       + " j := h NE u [BOOL]\r\n"
                       + " y := y EQ u [BOOL]\r\n"
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
        String program = "x := 38393 [INT]\r\n"
                       + "z := BNOT y [BOOL]\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Program prog = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testBooleanAssignment(){
        String program = "v := FALSE [BOOL]\r\n"
                       + "z := TRUE [BOOL]\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> prog = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testNumAssignment(){
        String program = "x := 89309 [INT]\r\n"
                       + "z := 438.343 [INT]\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> prog = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testProcedureCall(){
        String program = "SYMBOL SECTION\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + "t := 899 [INT]\r\n"
                       + "g := 89 [INT]\r\n"
                       + "f := 98 [INT]\r\n"
                       + "CALL func ((t -> x)[INT], (g -> y)[INT], (f -> z)[INT])\r\n"
                       + "EXTERNAL RETURN x := z [INT]\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n"
                       + "PROC LABEL func\r\n"
                       + "x := 78 [INT]\r\n"
                       + "y := 79 [INT]\r\n"
                       + "INTERNAL RETURN z := 48 [INT]\r\n"
                       + "RETURN\r\n";

        String flatProgram = "t := 899 [INT]\r\n" + //
                                "g := 89 [INT]\r\n" + //
                                "f := 98 [INT]\r\n" + //
                                "CALL func((t -> x)[INT], (g -> y)[INT], (f -> z)[INT])\r\n" + //
                                "EXTERNAL RETURN x := z [INT]\r\n" + //
                                "END\r\n" + //
                                "PROC LABEL func\r\n" + //
                                "x := 78 [INT]\r\n" + //
                                "y := 79 [INT]\r\n" + //
                                "INTERNAL RETURN z := 48 [INT]\r\n" + //
                                "RETURN\r\n";


        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog programICode = parser.parseProgram();
        List<ICode> flatProg = programICode.genFlatCode();

        assertTrue(!parser.containsErrors());

        testReaderSource(flatProg, flatProgram);
    }

    @Test
    public void testStringDecl(){
        String program = "t := \"Text Here\" [STRING]\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testIfStatement(){
        String program = "LABEL y\r\n"
                       + "IF x EQ TRUE THEN z ELSE y\r\n"
                       + "LABEL z\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testParamaterPlacement(){
        String program = "SYMBOL SECTION\r\n"
                       + "DATA SECTION\r\n"
                       + "CODE SECTION\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n"
                       + "PROC LABEL x\r\n"
                       + "PARAM v := y [INT]\r\n"
                       + "PARAM z := t [INT]\r\n"
                       + "g := z IADD v [INT]\r\n"
                       + "RETURN\r\n";

        String expectedResult = "END\r\n" + //
                                "PROC LABEL x\r\n" + //
                                "PARAM v := y [INT]\r\n" + //
                                "PARAM z := t [INT]\r\n" + //
                                "g := z IADD v [INT]\r\n" + //
                                "RETURN\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        Prog programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, expectedResult);
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
        String programName = "test/declan/RealDivision.dcl";
        testDeclanFileOnICode(programName);
    }
}
