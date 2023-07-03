package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class ICodeGeneratorTest {
    
    public void testReaderSource(List<ICode> iCodes, String programInput){
        StringBuilder sb = new StringBuilder();
        
        for(ICode iCode : iCodes){
            sb.append(iCode.toString());
            sb.append("\n");
        }

        assertTrue("Expected \n...\n\n" + programInput + "\n\n but found \n\n" + sb.toString(), programInput.equals(sb.toString()));
    }

    @Test
    public void testBinaryOp(){
        String program = "x := 456\n"
                       + "z := 48393\n"
                       + "v := x ADD z\n"
                       + "y := v SUB v\n"
                       + "g := v MOD y\n"
                       + "e := y MUL g\n"
                       + "y := z BOR x\n"
                       + "z := v DIV y\n"
                       + "g := v BAND z\n"
                       + "e := v LT x\n"
                       + "e := i GT g\n"
                       + "f := u LE j\n"
                       + "h := y GE o\n"
                       + "j := h NE u\n"
                       + "y := y EQ u\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }


    @Test
    public void testUnaryOp(){
        String program = "x := 38393\n"
                       + "y := NEG x\n"
                       + "z := BNOT y\n"
                       + "END\n";
        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testBooleanAssignment(){
        String program = "v := FALSE\n"
                       + "z := TRUE\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testNumAssignment(){
        String program = "x := 89309\n"
                       + "z := 438.343\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testProcedureCall(){
        String program = "t := 899\n"
                       + "g := 89\n"
                       + "f := 98\n"
                       + "LABEL func\n"
                       + "x := 78\n"
                       + "y := 79\n"
                       + "z := 48\n"
                       + "RETURN\n"
                       + "PROC func ( t -> x , g -> y , f -> z )\n"
                       + "x <- z\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testStringDecl(){
        String program = "t := \"Text Here\"\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testIfStatement(){
        String program = "LABEL y\n"
                       + "IF x EQ TRUE THEN z ELSE y\n"
                       + "LABEL z\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    @Test
    public void testDeclanConversions(){
        String programName = "test_source/conversions.dcl";
        
        String expectedICode = "a := 0\r\n" + //
                               "b := 0\r\n" + //
                               "LABEL p\r\n" + //
                               "c := 0\r\n" + //
                               "d := 0\r\n" + //
                               "e := 0\r\n" + //
                               "f := 0\r\n" + //
                               "g := a ADD b\r\n" + //
                               "PROC Round ( g , f )\r\n" + //
                               "h := e ADD f\r\n" + //
                               "i := \"e\"\r\n" + //
                               "j := 1\r\n" + //
                               "k := \"b\"\r\n" + //
                               "l := 2\r\n" + //
                               "m := \"a\"\r\n" + //
                               "PROC WriteInt ( b )\r\n" + //
                               "PROC WriteReal ( b )\r\n" + //
                               "PROC WriteReal ( a )\r\n" + //
                               "PROC WriteLn (  )\r\n" + //
                               "n := b DIV a\r\n" + //
                               "PROC WriteReal ( n )\r\n" + //
                               "o := 5\r\n" + //
                               "p := b ADD o\r\n" + //
                               "q := 6\r\n" + //
                               "r := b ADD q\r\n" + //
                               "s := p MUL r\r\n" + //
                               "PROC WriteInt ( s )\r\n" + //
                               "t := 4\r\n" + //
                               "u := a ADD t\r\n" + //
                               "5. := 5.0\r\n" + //
                               "w := a ADD v\r\n" + //
                               "x := u MUL w\r\n" + //
                               "PROC WriteReal ( x )\r\n" + //
                               "PROC WriteLn (  )\r\n" + //
                               "3.1415 := 3.1415\r\n" + //
                               "PROC p ( b , y , a )\r\n" + //
                               "PROC WriteReal ( a )\r\n" + //
                               "PROC WriteLn (  )\r\n" + //
                               "END\r\n";

        try{
            Source mySource = new ReaderSource(new FileReader(programName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
            Program prog = parser.parseProgram();
            
            IrRegisterGenerator gen = new IrRegisterGenerator();
            MyICodeGenerator igen = new MyICodeGenerator(errLog, gen);
            stdLib.ioLibrary().accept(igen);
            stdLib.mathLibrary().accept(igen);
            prog.accept(igen);
            List<ICode> icode = igen.getICode();

            testReaderSource(icode, expectedICode);
        } catch(FileNotFoundException exp) {
            assertTrue("Error File not found...", false);
        }
    }
}
