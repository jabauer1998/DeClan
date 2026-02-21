package declan;

import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import declan.utils.ErrorLog;
import declan.utils.ErrorLog.LogItem;
import declan.utils.source.ReaderSource;
import declan.utils.source.Source;
import declan.frontend.MyIrLexer;
import declan.frontend.MyIrParser;

public class IrParserTest {

    @Test
    public void testBinaryOp(){
        String program = "SYMBOL SECTION\n"
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "x := 456 <INT>\n"
                       + "z := 48393 <INT>\n"
                       + "v := x IADD z <INT>\n"
                       + "y := v ISUB v <INT>\n"
                       + "y := z LOR x <BOOL>\n"
                       + "g := v LAND z <BOOL>\n"
                       + "g := v IAND x <INT>\n"
                       + "f := t IOR e <INT>\n"
                       + "z := g ILSHIFT f <INT>\n"
                       + "c := r IRSHIFT y <INT>\n"
                       + "e := v LT x <BOOL>\n"
                       + "e := i GT g <BOOL>\n"
                       + "f := u LE j <BOOL>\n"
                       + "h := y GE o <BOOL>\n"
                       + "j := h INE u <BOOL>\n"
                       + "y := y IEQ u <BOOL>\n"
                       + "F := y BNE g <BOOL>\n"
                       + "H := y BEQ u <BOOL>\n"
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
    public void testUnaryOp(){
        String program = "SYMBOL SECTION\n"
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "x := 38393 <INT>\n"
                       + "y := INOT x <INT>\n"
                       + "z := BNOT y <BOOL>\n"
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
    public void testBooleanAssignment(){
        String program = "SYMBOL SECTION\n"
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "v := FALSE <BOOL>\n"
                       + "z := TRUE <BOOL>\n"
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
    public void testNumAssignment(){
        String program = "SYMBOL SECTION\n"
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "x := 89309 <INT>\n"
                       + "z := 438.343 <INT>\n"
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
    public void testProcedureCall(){
        String program = "SYMBOL SECTION\n" 
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "DEF GLOBAL t := 899 <INT>\n"
                       + "DEF GLOBAL g := 89 <INT>\n"
                       + "DEF GLOBAL f := 98 <INT>\n"
                       + "CODE SECTION\n"
                       + "CALL func ([t -> x]<INT>, [g -> y]<INT>, [f -> z]<INT>)\n"
                       + "END\n"
                       + "PROC SECTION\n"
                       + "PROC LABEL func\n"
                       + "x := 78 <INT>\n"
                       + "y := 79 <INT>\n"
                       + "z := 48 <INT>\n"
                       + "RETURN FROM func\n";

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
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "DEF GLOBAL t := \"Text Here\" <STRING>\n"
                       + "CODE SECTION\n"
                       + "t := \"Text Here Too\" <STRING>\n"
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
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "DEF GLOBAL trueVal := TRUE <BOOL>\n"
                       + "CODE SECTION\n"
                       + "LABEL y\n"
                       + "IF x BEQ trueVal THEN z ELSE y\n"
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
                       + "BSS SECTION\n"
                       + "DATA SECTION\n"
                       + "CODE SECTION\n"
                       + "IPARAM x <INT> %dr\n"
                       + "IPARAM z <INT> %ua\n"
                       + "IASM \"LDR %dr, %ua\"\n"
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
