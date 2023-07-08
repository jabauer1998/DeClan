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
import io.github.H20man13.DeClan.common.pat.P.LABEL;
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
                       + "v := x IADD z\n"
                       + "y := v ISUB v\n"
                       + "g := v IMOD y\n"
                       + "e := y IMUL g\n"
                       + "v := x RADD z\n"
                       + "y := v RSUB v\n"
                       + "e := y RMUL g\n"
                       + "y := z BOR x\n"
                       + "z := v IDIV y\n"
                       + "z := v RDIV y\n"
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
        
        String expectedICode = "LABEL begin_0\n"+
                               "a := 1\n"+
                               "b := INEG a\n"+
                               "c := b\n"+
                               "GOTO begin_1\n"+
                               "LABEL WriteLn\n"+
                               "RETURN\n"+
                               "LABEL WriteInt\n"+
                               "RETURN\n"+
                               "LABEL WriteReal\n"+
                               "RETURN\n"+
                               "LABEL begin_1\n"+
                               "f := 1\n"+
                               "g := INEG f\n"+
                               "h := g\n"+
                               "GOTO begin_2\n"+
                               "LABEL Round\n"+
                               "j := 5.0\n"+
                               "k := i RADD j\n"+
                               "RETURN\n"+
                               "LABEL Floor\n"+
                               "m := 5.0\n"+
                               "n := l RSUB m\n"+
                               "RETURN\n"+
                               "LABEL begin_2\n"+
                               "o := 0.0\n"+
                               "p := 0\n"+
                               "GOTO begin_3\n"+
                               "LABEL p\n"+
                               "s := 0\n"+
                               "t := o RADD p\n"+
                               "PROC Round ( t -> i )\n"+
                               "u <- k\n"+
                               "s := u\n"+
                               "RETURN\n"+
                               "LABEL begin_3\n"+
                               "v := 1\n"+
                               "p := v\n"+
                               "w := 2\n"+
                               "o := w\n"+
                               "PROC WriteInt ( p -> d )\n"+
                               "PROC WriteReal ( p -> e )\n"+
                               "PROC WriteReal ( o -> e )\n"+
                               "PROC WriteLn (  )\n"+
                               "x := p RDIV o\n"+
                               "PROC WriteReal ( x -> e )\n"+
                               "y := 5\n"+
                               "z := p IADD y\n"+
                               "A := 6\n"+
                               "B := p IADD A\n" +
                               "C := z IMUL B\n" +
                               "PROC WriteInt ( C -> d )\n" +
                               "D := 4\n" + 
                               "E := o RADD D\n" + 
                               "F := 5.0\n" + 
                               "G := o RADD F\n" +
                               "H := E RMUL G\n" + 
                               "PROC WriteReal ( H -> e )\n"+
                               "PROC WriteLn (  )\n"+
                               "I := 3.1415\n"+
                               "PROC p ( p -> q , I -> r )\n"+
                               "J <- s\n"+
                               "o := J\n"+
                               "PROC WriteReal ( o -> e )\n"+
                               "PROC WriteLn (  )\n"+
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
