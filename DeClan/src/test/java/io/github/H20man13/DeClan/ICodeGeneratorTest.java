package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

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
                       + "PROC func ( t , g , f )\n"
                       + "x := CALL func ( g , t , g )\n"
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
}
