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
                       + "y := INEG x\n"
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
                               "GOTO begin_1\n"+
                               "LABEL WriteLn\n"+
                               "RETURN\n"+
                               "LABEL WriteInt\n"+
                               "RETURN\n"+
                               "LABEL WriteReal\n"+
                               "RETURN\n"+
                               "LABEL begin_1\n"+
                               "GOTO begin_2\n"+
                               "LABEL Round\n"+
                               "d := 1\n"+
                               "e := INEG d\n"+
                               "RETURN\n"+
                               "LABEL Floor\n"+
                               "g := 1\n"+
                               "h := INEG g\n"+
                               "RETURN\n"+
                               "LABEL Ceil\n"+
                               "j := 1\n"+
                               "k := INEG j\n"+
                               "RETURN\n"+
                               "LABEL begin_2\n"+
                               "l := 0.0\n"+
                               "m := 0\n"+
                               "GOTO begin_3\n"+
                               "LABEL p\n"+
                               "p := 0\n" +
                               "q := n RADD o\n"+
                               "PROC Round ( q -> c )\n"+
                               "r <- e\n"+
                               "p := r\n"+
                               "RETURN\n"+
                               "LABEL begin_3\n"+
                               "s := 1\n"+
                               "m := s\n"+
                               "t := 2\n"+
                               "l := t\n"+
                               "PROC WriteInt ( m -> a )\n"+
                               "PROC WriteReal ( m -> b )\n"+
                               "PROC WriteReal ( l -> b )\n"+
                               "PROC WriteLn (  )\n"+
                               "u := m RDIVIDE l\n"+
                               "PROC WriteReal ( u -> b )\n"+
                               "v := 5\n"+
                               "w := m IADD v\n"+
                               "x := 6\n"+
                               "y := m IADD x\n"+
                               "z := w IMUL y\n"+
                               "PROC WriteInt ( z -> a )\n"+
                               "A := 4\n"+
                               "B := l RADD A\n"+
                               "C := 5.0\n"+
                               "D := l RADD C\n"+
                               "E := B RMUL D\n"+
                               "PROC WriteReal ( E -> b )\n"+
                               "PROC WriteLn (  )\n"+
                               "F := 3.1415\n"+
                               "PROC p ( m -> n , F -> o )\n"+
                               "G <- p\n"+
                               "l := G\n"+
                               "PROC WriteReal ( l -> b )\n"+
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
