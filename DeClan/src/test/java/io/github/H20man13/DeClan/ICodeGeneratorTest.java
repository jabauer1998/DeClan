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
import io.github.H20man13.DeClan.common.pat.P.ELSE;
import io.github.H20man13.DeClan.common.pat.P.GOTO;
import io.github.H20man13.DeClan.common.pat.P.IF;
import io.github.H20man13.DeClan.common.pat.P.INEG;
import io.github.H20man13.DeClan.common.pat.P.LABEL;
import io.github.H20man13.DeClan.common.pat.P.THEN;
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

    @Test
    public void testIfStatementAdvanced(){
        String programName = "test_source/IfStatementAdvanced.dcl";
        String expectedICodee = "LABEL begin_0\r\n" + //
                                "GOTO begin_1\r\n" + //
                                "LABEL WriteLn\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteInt\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteReal\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL ReadInt\r\n" + //
                                "g := 1\r\n" + //
                                "h := INEG g\r\n" + //
                                "f := h\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_1\r\n" + //
                                "GOTO begin_2\r\n" + //
                                "LABEL Round\r\n" + //
                                "o := 1\r\n" + //
                                "p := INEG o\r\n" + //
                                "j := p\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Floor\r\n" + //
                                "q := 1\r\n" + //
                                "r := INEG q\r\n" + //
                                "l := r\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Ceil\r\n" + //
                                "s := 1\r\n" + //
                                "t := INEG s\r\n" + //
                                "n := t\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_2\r\n" + //
                                "u := TRUE\r\n" + //
                                "v := u\r\n" + //
                                "w := FALSE\r\n" + //
                                "x := w\r\n" + //
                                "GOTO begin_3\r\n" + //
                                "LABEL begin_3\r\n" + //
                                "IF v EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF x EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_1 ELSE IFNEXT_0_SEQ_0_LEVEL_1\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_1\r\n" + //
                                "y := 5\r\n" + //
                                "PROC WriteInt ( y -> b )\r\n" + //
                                "GOTO IFEND_0_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_1\r\n" + //
                                "z := 6\r\n" + //
                                "PROC WriteInt ( z -> b )\r\n" + //
                                "GOTO IFEND_0_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_1\r\n" + //
                                "LABEL IFEND_0_LEVEL_1\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF x EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_1 ELSE IFNEXT_0_SEQ_0_LEVEL_1\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_1\r\n" + //
                                "A := 7\r\n" + //
                                "PROC WriteInt ( A -> b )\r\n" + //
                                "GOTO IFEND_0_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_1\r\n" + //
                                "B := 8\r\n" + //
                                "PROC WriteInt ( B -> b )\r\n" + //
                                "GOTO IFEND_0_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_1\r\n" + //
                                "LABEL IFEND_0_LEVEL_1\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
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

            testReaderSource(icode, expectedICodee);
        } catch(FileNotFoundException exp) {
            assertTrue("Error File not found...", false);
        }
    }


    @Test
    public void testWhileLoopBasic(){
        String programName = "test_source/WhileLoopBasic.dcl";
        String expectedICode = "LABEL begin_0\r\n" + //
                                "GOTO begin_1\r\n" + //
                                "LABEL WriteLn\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteInt\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteReal\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL ReadInt\r\n" + //
                                "g := 1\r\n" + //
                                "h := INEG g\r\n" + //
                                "f := h\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_1\r\n" + //
                                "GOTO begin_2\r\n" + //
                                "LABEL Round\r\n" + //
                                "o := 1\r\n" + //
                                "p := INEG o\r\n" + //
                                "j := p\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Floor\r\n" + //
                                "q := 1\r\n" + //
                                "r := INEG q\r\n" + //
                                "l := r\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Ceil\r\n" + //
                                "s := 1\r\n" + //
                                "t := INEG s\r\n" + //
                                "n := t\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_2\r\n" + //
                                "u := 10\r\n" + //
                                "v := u\r\n" + //
                                "w := 0\r\n" + //
                                "GOTO begin_3\r\n" + //
                                "LABEL begin_3\r\n" + //
                                "x := 0\r\n" + //
                                "w := x\r\n" + //
                                "y := w LT v\r\n" + //
                                "IF y EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF y EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "PROC WriteInt ( w -> b )\r\n" + //
                                "z := 1\r\n" + //
                                "A := w IADD z\r\n" + //
                                "w := A\r\n" + //
                                "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILEEND_0_LEVEL_0\r\n" + //
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

    @Test
    public void testWhileLoopAdvanced(){
        String programName = "test_source/WhileLoopAdvanced.dcl";
        String expectedICode = "LABEL begin_0\r\n" + //
                               "GOTO begin_1\r\n" + //
                               "LABEL WriteLn\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteInt\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteReal\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL ReadInt\r\n" + //
                                "g := 1\r\n" + //
                                "h := INEG g\r\n" + //
                                "f := h\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_1\r\n" + //
                                "GOTO begin_2\r\n" + //
                                "LABEL Round\r\n" + //
                                "o := 1\r\n" + //
                                "p := INEG o\r\n" + //
                                "j := p\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Floor\r\n" + //
                                "q := 1\r\n" + //
                                "r := INEG q\r\n" + //
                                "l := r\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Ceil\r\n" + //
                                "s := 1\r\n" + //
                                "t := INEG s\r\n" + //
                                "n := t\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_2\r\n" + //
                                "u := 10\r\n" + //
                                "v := u\r\n" + //
                                "w := 0\r\n" + //
                                "x := w\r\n" + //
                                "y := 0\r\n" + //
                                "z := 0\r\n" + //
                                "GOTO begin_3\r\n" + //
                                "LABEL begin_3\r\n" + //
                                "y := x\r\n" + //
                                "A := y GT v\r\n" + //
                                "IF A EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF A EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "PROC WriteInt ( y -] b )\r\n" + //
                                "B := 1\r\n" + //
                                "C := y IADD B\r\n" + //
                                "y := C\r\n" + //
                                "D := y GT v\r\n" + //
                                "A := D\r\n" + //
                                "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "E := y LT v\r\n" + //
                                "IF E EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                "IF E EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "z := x\r\n" + //
                                "F := z LT v\r\n" + //
                                "IF F EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                "IF F EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "PROC WriteInt ( z -] b )\r\n" + //
                                "G := 1\r\n" + //
                                "H := z IADD G\r\n" + //
                                "z := H\r\n" + //
                                "I := z LT v\r\n" + //
                                "F := I\r\n" + //
                                "GOTO WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "PROC WriteLn (  )\r\n" + //
                                "PROC WriteInt ( y -] b )\r\n" + //
                                "PROC WriteLn (  )\r\n" + //
                                "J := 1\r\n" + //
                                "K := y IADD J\r\n" + //
                                "y := K\r\n" + //
                                "L := y LT v\r\n" + //
                                "E := L\r\n" + //
                                "GOTO WHILECOND_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILEEND_1_LEVEL_0\r\n" + //
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

    @Test
    public void testRepeatLoopBasic(){
        String programName = "test_source/RepeatLoopBasic.dcl";
        String expectedICode = "LABEL begin_0\r\n" + //
                                "GOTO begin_1\r\n" + //
                                "LABEL WriteLn\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteInt\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL WriteReal\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL ReadInt\r\n" + //
                                "g := 1\r\n" + //
                                "h := INEG g\r\n" + //
                                "f := h\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_1\r\n" + //
                                "GOTO begin_2\r\n" + //
                                "LABEL Round\r\n" + //
                                "o := 1\r\n" + //
                                "p := INEG o\r\n" + //
                                "j := p\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Floor\r\n" + //
                                "q := 1\r\n" + //
                                "r := INEG q\r\n" + //
                                "l := r\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL Ceil\r\n" + //
                                "s := 1\r\n" + //
                                "t := INEG s\r\n" + //
                                "n := t\r\n" + //
                                "RETURN\r\n" + //
                                "LABEL begin_2\r\n" + //
                                "u := 0\r\n" + //
                                "GOTO begin_3\r\n" + //
                                "LABEL begin_3\r\n" + //
                                "v := 1\r\n" + //
                                "u := v\r\n" + //
                                "w := 10\r\n" + //
                                "x := u GE w\r\n" + //
                                "LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                                "IF x EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                                "LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                                "PROC WriteInt ( u -] b )\r\n" + //
                                "y := 1\r\n" + //
                                "z := u IADD y\r\n" + //
                                "u := z\r\n" + //
                                "A := 10\r\n" + //
                                "B := u GE A\r\n" + //
                                "x := B\r\n" + //
                                "LABEL REPEATEND_0_LEVEL_0\r\n" + //
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

    @Test
    public void testTest3(){
        String programName = "test_source/test3.dcl";
        String expectedICode = "LABEL begin_0\r\n" + //
                "GOTO begin_1\r\n" + //
                "LABEL WriteLn\r\n" + //
                "RETURN\r\n" + //
                "LABEL WriteInt\r\n" + //
                "RETURN\r\n" + //
                "LABEL WriteReal\r\n" + //
                "RETURN\r\n" + //
                "LABEL ReadInt\r\n" + //
                "g := 1\r\n" + //
                "h := INEG g\r\n" + //
                "f := h\r\n" + //
                "RETURN\r\n" + //
                "LABEL begin_1\r\n" + //
                "GOTO begin_2\r\n" + //
                "LABEL Round\r\n" + //
                "o := 1\r\n" + //
                "p := INEG o\r\n" + //
                "j := p\r\n" + //
                "RETURN\r\n" + //
                "LABEL Floor\r\n" + //
                "q := 1\r\n" + //
                "r := INEG q\r\n" + //
                "l := r\r\n" + //
                "RETURN\r\n" + //
                "LABEL Ceil\r\n" + //
                "s := 1\r\n" + //
                "t := INEG s\r\n" + //
                "n := t\r\n" + //
                "RETURN\r\n" + //
                "LABEL begin_2\r\n" + //
                "u := 42\r\n" + //
                "v := u\r\n" + //
                "w := 42.0\r\n" + //
                "x := w\r\n" + //
                "y := 0\r\n" + //
                "z := 0\r\n" + //
                "A := 0.0\r\n" + //
                "B := 0.0\r\n" + //
                "C := 0.0\r\n" + //
                "GOTO begin_3\r\n" + //
                "LABEL Foo\r\n" + //
                "J := v EQ x\r\n" + //
                "K := J\r\n" + //
                "L := 355\r\n" + //
                "M := 113\r\n" + //
                "N := L IDIVIDE M\r\n" + //
                "O := N\r\n" + //
                "P := 0\r\n" + //
                "P := v\r\n" + //
                "LABEL FORBEG_0_LEVEL_0\r\n" + //
                "IF P NE D THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                "PROC Bar (  )\r\n" + //
                "Q := 20\r\n" + //
                "R := INEG Q\r\n" + //
                "S := P IADD R\r\n" + //
                "P := S\r\n" + //
                "GOTO FORBEG_0_LEVEL_0\r\n" + //
                "LABEL FOREND_0_LEVEL_0\r\n" + //
                "T := 3.14159265\r\n" + //
                "U := O GT T\r\n" + //
                "V := K BAND U\r\n" + //
                "G := V\r\n" + //
                "RETURN\r\n" + //
                "LABEL Bar\r\n" + //
                "W := 0.0\r\n" + //
                "X := 1\r\n" + //
                "Y := y IADD X\r\n" + //
                "y := Y\r\n" + //
                "Z := BNOT C\r\n" + //
                "IF Z EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                "a0 := TRUE\r\n" + //
                "C := a0\r\n" + //
                "a1 := 0\r\n" + //
                "a2 := 0\r\n" + //
                "PROC Foo ( z -] D , a1 -] E , a2 -] F , W -] G )\r\n" + //
                "GOTO IFEND_0_LEVEL_0\r\n" + //
                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                "LABEL IFEND_0_LEVEL_0\r\n" + //
                "RETURN\r\n" + //
                "LABEL begin_3\r\n" + //
                "a3 := v RMUL B\r\n" + //
                "PROC Foo ( y -] D , A -] E , a3 -] F , C -] G )\r\n" + //
                "IF C EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                "PROC WriteInt ( y -] b )\r\n" + //
                "GOTO IFEND_1_LEVEL_0\r\n" + //
                "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                "a4 := x RSUB v\r\n" + //
                "PROC WriteReal ( a4 -] d )\r\n" + //
                "GOTO IFEND_1_LEVEL_0\r\n" + //
                "LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                "LABEL IFEND_1_LEVEL_0\r\n" + //
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

    @Test
    public void testForLoopBasic(){
        String programName = "test_source/ForLoopBasic.dcl";
        String expectedICode = "LABEL begin_0\r\n" + //
                "GOTO begin_1\r\n" + //
                "LABEL WriteLn\r\n" + //
                "RETURN\r\n" + //
                "LABEL WriteInt\r\n" + //
                "RETURN\r\n" + //
                "LABEL WriteReal\r\n" + //
                "RETURN\r\n" + //
                "LABEL ReadInt\r\n" + //
                "g := 1\r\n" + //
                "h := INEG g\r\n" + //
                "f := h\r\n" + //
                "RETURN\r\n" + //
                "LABEL begin_1\r\n" + //
                "GOTO begin_2\r\n" + //
                "LABEL Round\r\n" + //
                "o := 1\r\n" + //
                "p := INEG o\r\n" + //
                "j := p\r\n" + //
                "RETURN\r\n" + //
                "LABEL Floor\r\n" + //
                "q := 1\r\n" + //
                "r := INEG q\r\n" + //
                "l := r\r\n" + //
                "RETURN\r\n" + //
                "LABEL Ceil\r\n" + //
                "s := 1\r\n" + //
                "t := INEG s\r\n" + //
                "n := t\r\n" + //
                "RETURN\r\n" + //
                "LABEL begin_2\r\n" + //
                "u := 0\r\n" + //
                "GOTO begin_3\r\n" + //
                "LABEL begin_3\r\n" + //
                "v := 1\r\n" + //
                "u := v\r\n" + //
                "w := 10\r\n" + //
                "LABEL FORBEG_0_LEVEL_0\r\n" + //
                "IF u NE w THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                "PROC WriteInt ( u -] b )\r\n" + //
                "x := 1\r\n" + //
                "y := u IADD x\r\n" + //
                "u := y\r\n" + //
                "GOTO FORBEG_0_LEVEL_0\r\n" + //
                "LABEL FOREND_0_LEVEL_0\r\n" + //
                "PROC WriteLn (  )\r\n" + //
                "END\r\n" + //
                "\r\n" + //
                " at io.github.H20man13.DeClan.ICodeGeneratorTest.testReaderSource(ICodeGeneratorTest.java:41)\r\n" + //
                " at io.github.H20man13.DeClan.ICodeGeneratorTest.testForLoopBasic(ICodeGeneratorTest.java:792)\r\n" + //
                "";

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
