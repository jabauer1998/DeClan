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
        
        String expectedICode = "a := 1\n" +
                               "b := NEG a\n" +
                               "c := b\n" +
                               "LABEL WriteLn\n" +
                               "RETURN\n" +
                               "LABEL WriteInt\n" +
                               "RETURN\n" +
                               "LABEL WriteReal\n" +
                               "RETURN\n" +
                               "f := 1\n" +
                               "g := NEG f\n" +
                               "h := g\n" +
                               "LABEL Round\n" +
                               "k := 5.0\n" +
                               "l := i ADD k\n" +
                               "RETURN\n" +
                               "LABEL Floor\n" +
                               "o := 5.0\n" +
                               "p := m SUB o\n" +
                               "RETURN\n" +
                               "q := 0.0\n" +
                               "r := 0\n" +
                               "GOTO begin\n"+
                               "LABEL p\n"+
                               "u := 0\n"+
                               "v := q ADD r\n" +
                               "PROC Round ( v -> i )\n" +
                               "w <- l\n" +
                               "u := w\n" +
                               "RETURN\n" +
                               "LABEL begin\n" +
                               "x := 1\n" +
                               "r := x\n" +
                               "y := 2\n" +
                               "q := y\n" +
                               "PROC WriteInt ( r -> d )\n" +
                               "PROC WriteReal ( r -> e )\n" +
                               "PROC WriteReal ( q -> e )\n" +
                               "PROC WriteLn (  )\n" +
                               "z := r DIV q\n" +
                               "PROC WriteReal ( z -> e )\n" +
                               "A := 5\n" +
                               "B := r ADD A\n" +
                               "C := 6\n" +
                               "D := r ADD C\n" +
                               "E := B MUL D\n" +
                               "PROC WriteInt ( E -> d )\n" + 
                               "F := 4\n" +
                               "G := q ADD F\n" +
                               "H := 5.0\n" +
                               "I := q ADD H\n" +
                               "J := G MUL I\n" +
                               "PROC WriteReal ( J -> e )\n" +
                               "PROC WriteLn (  )\n" +
                               "K := 3.1415\n" + 
                               "PROC p ( r -> s , K -> t )\n" + 
                               "L <- u\n" + 
                               "q := L\n" + 
                               "PROC WriteReal ( q -> e )\n" + 
                               "PROC WriteLn (  )\n" + 
                               "END\n";


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
