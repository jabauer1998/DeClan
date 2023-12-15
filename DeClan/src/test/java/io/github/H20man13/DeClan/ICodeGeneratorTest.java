package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.pat.P.ELSE;
import io.github.H20man13.DeClan.common.pat.P.GOTO;
import io.github.H20man13.DeClan.common.pat.P.IF;
import io.github.H20man13.DeClan.common.pat.P.INEG;
import io.github.H20man13.DeClan.common.pat.P.LABEL;
import io.github.H20man13.DeClan.common.pat.P.THEN;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class ICodeGeneratorTest {
    
    public static void testReaderSource(List<ICode> program, String programInput){
        StringBuilder sb = new StringBuilder();

        for(ICode iCode : program){
            sb.append(iCode.toString());
            sb.append("\r\n");
        }

        assertTrue("Expected \n...\n\n" + programInput + "\n\n but found \n\n" + sb.toString(), programInput.equals(sb.toString()));
    }

    private void testDeclanFileOnICode(String programName, String expectedOutput){
        try{
            Source mySource = new ReaderSource(new FileReader(programName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            MyStandardLibrary stdLib = new MyStandardLibrary(errLog);
            Program prog = parser.parseProgram();
            
            IrRegisterGenerator gen = new IrRegisterGenerator();
            MyICodeGenerator igen = new MyICodeGenerator(errLog, gen);
            
            Prog program = igen.generateProgramIr(prog);

            List<ICode> flatCode = program.genFlatCode();

            testReaderSource(flatCode, expectedOutput);
        } catch(FileNotFoundException exp) {
            assertTrue("Error File not found...", false);
        }
    }

    @Test
    public void testBinaryOp(){
        String program = "x := 456\r\n"
                       + "z := 48393\r\n"
                       + "v := x IADD z\r\n"
                       + "y := v ISUB v\r\n"
                       + "g := v IMOD y\r\n"
                       + "e := y IMUL g\r\n"
                       + "v := x RADD z\r\n"
                       + "y := v RSUB v\r\n"
                       + "e := y RMUL g\r\n"
                       + "y := z LOR x\r\n"
                       + "Z := b IOR x\r\n"
                       + "z := v IDIV y\r\n"
                       + "z := v RDIVIDE y\r\n"
                       + "g := v LAND z\r\n"
                       + "d := v IAND z\r\n"
                       + "e := v ILSHIFT x\r\n"
                       + "d := b IRSHIFT f\r\n"
                       + "e := v LT x\r\n"
                       + "e := i GT g\r\n"
                       + "f := u LE j\r\n"
                       + "h := y GE o\r\n"
                       + "j := h NE u\r\n"
                       + "y := y EQ u\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> prog = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }


    @Test
    public void testUnaryOp(){
        String program = "x := 38393\r\n"
                       + "y := INEG x\r\n"
                       + "z := BNOT y\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> prog = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(prog, program);
    }

    @Test
    public void testBooleanAssignment(){
        String program = "v := FALSE\r\n"
                       + "z := TRUE\r\n";

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
        String program = "x := 89309\r\n"
                       + "z := 438.343\r\n";

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
                       + "t := 899\r\n"
                       + "g := 89\r\n"
                       + "f := 98\r\n"
                       + "CALL func ( t -> x , g -> y , f -> z )\r\n"
                       + "x <- z\r\n"
                       + "END\r\n"
                       + "PROC SECTION\r\n"
                       + "PROC LABEL func\r\n"
                       + "x := 78\r\n"
                       + "y := 79\r\n"
                       + "z := 48\r\n"
                       + "RETURN\r\n";

        String flatProgram = "t := 899\r\n"
                           + "g := 89\r\n"
                           + "f := 98\r\n"
                           + "CALL func ( t -> x , g -> y , f -> z )\r\n"
                           + "x <- z\r\n"
                           + "END\r\n"
                           + "PROC LABEL func\r\n"
                           + "x := 78\r\n"
                           + "y := 79\r\n"
                           + "z := 48\r\n"
                           + "RETURN\r\n";


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
        String program = "t := \"Text Here\"\r\n";

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
        String program = "LABEL x\r\n"
                       + "v <- y\r\n"
                       + "z <- t\r\n"
                       + "g := z IADD v\r\n";

        Source mySource = new ReaderSource(new StringReader(program));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(mySource, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);

        List<ICode> programICode = parser.parseInstructions();

        assertTrue(!parser.containsErrors());

        testReaderSource(programICode, program);
    }

    
    @Test
    public void testDeclanConversions(){
        String programName = "test_source/conversions.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 0\r\n" + //
                               "c := 0.0\r\n" + //
                               "d := 0.0\r\n" + //
                               "h := 1\r\n" + //
                               "b := h\r\n" + //
                               "i := 1.0\r\n" + //
                               "c := i\r\n" + //
                               "j := 2\r\n" + //
                               "a := j\r\n" + //
                               "k := 2.0\r\n" + //
                               "d := k\r\n" + //
                               "EXTERNAL CALL WriteInt(b)\r\n" + //
                               "EXTERNAL CALL WriteReal(c)\r\n" + //
                               "EXTERNAL CALL WriteReal(d)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "l := b RDIVIDE a\r\n" + //
                               "EXTERNAL CALL WriteReal(l)\r\n" + //
                               "m := 5\r\n" + //
                               "n := b IADD m\r\n" + //
                               "o := 6\r\n" + //
                               "p := b IADD o\r\n" + //
                               "q := n IMUL p\r\n" + //
                               "EXTERNAL CALL WriteInt(q)\r\n" + //
                               "r := 4\r\n" + //
                               "s := a IADD r\r\n" + //
                               "t := 5.0\r\n" + //
                               "u := a RADD t\r\n" + //
                               "v := s RMUL u\r\n" + //
                               "EXTERNAL CALL WriteReal(v)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "w := 3.1415\r\n" + //
                               "CALL p ( b -> e , w -> f )\r\n" + //
                               "x <| g\r\n" + //
                               "a := x\r\n" + //
                               "EXTERNAL CALL WriteReal(d)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "END\r\n" + //
                               "PROC LABEL p\r\n" + //
                               "z <- f\r\n" + //
                               "A <- e\r\n" + //
                               "y := 0\r\n" + //
                               "B := z IADD A\r\n" + //
                               "C := EXTERNAL CALL Round(B)\r\n" + //
                               "y := C\r\n" + //
                               "g |< y\r\n" + //
                               "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
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
        
        testDeclanFileOnICode(programName, expectedICodee);
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

        testDeclanFileOnICode(programName, expectedICode);
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

        testDeclanFileOnICode(programName, expectedICode);
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
       testDeclanFileOnICode(programName, expectedICode);
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
        testDeclanFileOnICode(programName, expectedICode);
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
                                "END\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }
}
