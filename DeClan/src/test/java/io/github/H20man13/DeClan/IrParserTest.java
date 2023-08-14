package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

public class IrParserTest {

    @Test
    public void testBinaryOp(){
        String program = "x := 456\n"
                       + "z := 48393\n"
                       + "v := x IADD z\n"
                       + "y := v ISUB v\n"
                       + "g := v IMOD y\n"
                       + "e := y IMUL g\n"
                       + "y := v RADD g\n"
                       + "z := v RSUB g\n"
                       + "t := x RMUL x\n"
                       + "y := z LOR x\n"
                       + "z := r IDIV r\n"
                       + "z := v RDIV y\n"
                       + "g := v LAND z\n"
                       + "g := v IAND x\n"
                       + "f := t IOR e\n"
                       + "z := g ILSHIFT f\n"
                       + "c := r IRSHIFT y\n"
                       + "e := v LT x\n"
                       + "e := i GT g\n"
                       + "f := u LE j\n"
                       + "h := y GE o\n"
                       + "j := h NE u\n"
                       + "y := y EQ u\n"
                       + "END";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }


    @Test
    public void testUnaryOp(){
        String program = "x := 38393\n"
                       + "y := INEG x\n"
                       + "y := RNEG x\n"
                       + "z := BNOT y\n"
                       + "END";
        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testBooleanAssignment(){
        String program = "v := FALSE\n"
                       + "z := TRUE\n"
                       + "END";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testNumAssignment(){
        String program = "x := 89309\n"
                       + "z := 438.343\n"
                       + "END";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
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
                       + "PROC func (t -> x, g -> y, f -> z)\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
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

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
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

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testInlineAssembly(){
        String program = "IPARAM x\n"
                       + "IPARAM z\n"
                       + "IASM \"LDR %r, %a\"\n"
                       + "END\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }
}
