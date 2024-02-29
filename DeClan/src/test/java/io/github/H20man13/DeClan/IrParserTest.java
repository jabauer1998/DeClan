package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

public class IrParserTest {

    @Test
    public void testBinaryOp(){
        String program = "x := 456 [INT]\n"
                       + "z := 48393 [INT]\n"
                       + "v := x IADD z [INT]\n"
                       + "y := v ISUB v [INT]\n"
                       + "g := v IMOD y [INT]\n"
                       + "e := y IMUL g [INT]\n"
                       + "y := v RADD g [REAL]\n"
                       + "z := v RSUB g [REAL]\n"
                       + "t := x RMUL x [REAL]\n"
                       + "y := z LOR x [BOOL]\n"
                       + "z := r IDIV r [INT]\n"
                       + "z := v RDIV y [REAL]\n"
                       + "g := v LAND z [BOOL]\n"
                       + "g := v IAND x [INT]\n"
                       + "f := t IOR e [INT]\n"
                       + "z := g ILSHIFT f [INT]\n"
                       + "c := r IRSHIFT y [INT]\n"
                       + "e := v LT x [BOOL]\n"
                       + "e := i GT g [BOOL]\n"
                       + "f := u LE j [BOOL]\n"
                       + "h := y GE o [BOOL]\n"
                       + "j := h NE u [BOOL]\n"
                       + "y := y EQ u [BOOL]\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseInstructions();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }


    @Test
    public void testUnaryOp(){
        String program = "x := 38393 [INT]\n"
                       + "y := INEG x [INT]\n"
                       + "y := RNEG x [REAL]\n"
                       + "z := BNOT y [BOOL]\n";
        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseInstructions();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testBooleanAssignment(){
        String program = "v := FALSE [BOOL]\n"
                       + "z := TRUE [BOOL]\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseInstructions();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testNumAssignment(){
        String program = "x := 89309 [INT]\n"
                       + "z := 438.343 [INT]\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseInstructions();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testProcedureCall(){
        String program = "SYMBOL SECTION\n" 
                       + "DATA SECTION\n"
                       + "t := 899 [INT]\n"
                       + "g := 89 [INT]\n"
                       + "f := 98 [INT]\n"
                       + "CODE SECTION\n"
                       + "CALL func ((t -> x)[INT], (g -> y)[INT], (f -> z)[INT])\n"
                       + "END\n"
                       + "PROC SECTION\n"
                       + "PROC LABEL func\n"
                       + "x := 78 [INT]\n"
                       + "y := 79 [INT]\n"
                       + "z := 48 [INT]\n"
                       + "RETURN\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testStringDecl(){
        String program = "SYMBOL SECTION\n"
                       + "DATA SECTION\n"
                       + "t := \"Text Here\" [STRING]\n"
                       + "CODE SECTION\n"
                       + "t := \"Text Here Too\" [STRING]\n"
                       + "END\n"
                       + "PROC SECTION\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testIfStatement(){
        String program = "SYMBOL SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "LABEL y\n"
                       + "IF x EQ TRUE THEN z ELSE y\n"
                       + "LABEL z\n"
                       + "END\n"
                       + "PROC SECTION\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }

    @Test
    public void testInlineAssembly(){
        String program = "SYMBOL SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "IPARAM x\n"
                       + "IPARAM z\n"
                       + "IASM \"LDR %r, %a\"\n"
                       + "END\n"
                       + "PROC SECTION\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        parser.parseProgram();

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        assertTrue(!parser.containsErrors());
    }
}
