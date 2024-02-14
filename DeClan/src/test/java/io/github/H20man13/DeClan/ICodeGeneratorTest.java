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
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrParser;

public class ICodeGeneratorTest {
    public static void testReaderSource(List<ICode> program, String programInput){
        StringBuilder sb = new StringBuilder();

        for(ICode iCode : program){
            sb.append(iCode.toString());
            sb.append("\r\n");
        }

        assertTrue("Expected \n...\n\n" + programInput + "\n\n but found \n\n" + sb.toString(), programInput.equals(sb.toString()));
    }

    private static void testDeclanFileOnICode(String programName, String expectedOutput){
        try{
            Source mySource = new ReaderSource(new FileReader(programName));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            
            IrRegisterGenerator gen = new IrRegisterGenerator();
            MyICodeGenerator igen = new MyICodeGenerator(errLog, gen);
            
            Prog program = igen.generateProgramIr(prog);

            List<ICode> flatCode = program.genFlatCode();

            testReaderSource(flatCode, expectedOutput);

            parser.close();
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
                       + "e := y IMUL g\r\n"
                       + "v := x RADD z\r\n"
                       + "y := v RSUB v\r\n"
                       + "e := y RMUL g\r\n"
                       + "y := z LOR x\r\n"
                       + "Z := b IOR x\r\n"
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
                               "l := EXTERNAL CALL Divide(b, a)\r\n" + //
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
                               "u := EXTERNAL CALL IntToReal(a)\r\n" + //
                               "v := EXTERNAL CALL RAdd(u, t)\r\n" + //
                               "w := EXTERNAL CALL IntToReal(s)\r\n" + //
                               "x := EXTERNAL CALL RMul(w, v)\r\n" + //
                               "EXTERNAL CALL WriteReal(x)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "y := 3.1415\r\n" + //
                               "CALL p ( b -> e , y -> f )\r\n" + //
                               "z <| g\r\n" + //
                               "a := z\r\n" + //
                               "EXTERNAL CALL WriteReal(d)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "END\r\n" + //
                               "PROC LABEL p\r\n" + //
                               "B <- f\r\n" + //
                               "C <- e\r\n" + //
                               "A := 0\r\n" + //
                               "D := EXTERNAL CALL IntToReal(C)\r\n" + //
                               "E := EXTERNAL CALL RAdd(B, D)\r\n" + //
                               "F := EXTERNAL CALL Round(E)\r\n" + //
                               "A := F\r\n" + //
                               "g |< A\r\n" + //
                               "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testExpressions(){
        String programName = "test_source/expressions.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := a\r\n" + //
                                "c := 1.2\r\n" + //
                                "d := c\r\n" + //
                                "e := 3.14\r\n" + //
                                "f := EXTERNAL CALL RNeg(e)\r\n" + //
                                "g := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "h := EXTERNAL CALL RAdd(f, g)\r\n" + //
                                "i := h\r\n" + //
                                "j := 6\r\n" + //
                                "k := 6\r\n" + //
                                "l := 1\r\n" + //
                                "m := k IADD l\r\n" + //
                                "n := j IMUL m\r\n" + //
                                "o := n\r\n" + //
                                "p := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "q := EXTERNAL CALL RMul(p, d)\r\n" + //
                                "r := EXTERNAL CALL IntToReal(o)\r\n" + //
                                "s := EXTERNAL CALL RDivide(i, r)\r\n" + //
                                "t := EXTERNAL CALL RNotEqualTo(q, s)\r\n" + //
                                "u := t\r\n" + //
                                "v := 0.0\r\n" + //
                                "w := 0.0\r\n" + //
                                "x := 0.0\r\n" + //
                                "y := 0\r\n" + //
                                "z := 0\r\n" + //
                                "A := 0\r\n" + //
                                "B := FALSE\r\n" + //
                                "C := FALSE\r\n" + //
                                "D := FALSE\r\n" + //
                                "E := EXTERNAL CALL RDivide(d, d)\r\n" + //
                                "v := E\r\n" + //
                                "F := 10\r\n" + //
                                "G := EXTERNAL CALL Mod(o, F)\r\n" + //
                                "y := G\r\n" + //
                                "H := EXTERNAL CALL RNeg(i)\r\n" + //
                                "I := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "J := EXTERNAL CALL RMul(d, I)\r\n" + //
                                "K := EXTERNAL CALL RSub(H, J)\r\n" + //
                                "w := K\r\n" + //
                                "EXTERNAL CALL WriteInt(b)\r\n" + //
                                "EXTERNAL CALL WriteReal(v)\r\n" + //
                                "EXTERNAL CALL WriteReal(v)\r\n" + //
                                "EXTERNAL CALL WriteReal(w)\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "L := EXTERNAL CALL Div(o, y)\r\n" + //
                                "z := L\r\n" + //
                                "M := EXTERNAL CALL Divide(o, y)\r\n" + //
                                "x := M\r\n" + //
                                "EXTERNAL CALL WriteInt(z)\r\n" + //
                                "EXTERNAL CALL WriteReal(x)\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "N := EXTERNAL CALL Round(i)\r\n" + //
                                "x := N\r\n" + //
                                "EXTERNAL CALL WriteReal(x)\r\n" + //
                                "IF u EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "O := 2\r\n" + //
                                "P := 2\r\n" + //
                                "Q := O IMUL P\r\n" + //
                                "EXTERNAL CALL WriteInt(Q)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "R := 10\r\n" + //
                                "S := EXTERNAL CALL Divide(o, R)\r\n" + //
                                "EXTERNAL CALL WriteReal(S)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "T := BNOT u\r\n" + //
                                "U := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "V := EXTERNAL CALL RGreaterThan(v, U)\r\n" + //
                                "W := T LAND V\r\n" + //
                                "B := W\r\n" + //
                                "X := BNOT u\r\n" + //
                                "Y := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "Z := EXTERNAL CALL RGreaterThanOrEqualTo(v, Y)\r\n" + //
                                "a0 := X LOR Z\r\n" + //
                                "C := a0\r\n" + //
                                "a1 := B EQ C\r\n" + //
                                "D := a1\r\n" + //
                                "IF B EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "a2 := 4\r\n" + //
                                "EXTERNAL CALL WriteInt(a2)\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "IF C EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "a3 := 5\r\n" + //
                                "EXTERNAL CALL WriteInt(a3)\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_1_LEVEL_0\r\n" + //
                                "IF D EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "a4 := 5\r\n" + //
                                "EXTERNAL CALL WriteInt(a4)\r\n" + //
                                "GOTO IFEND_2_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "a5 := 6\r\n" + //
                                "EXTERNAL CALL WriteInt(a5)\r\n" + //
                                "GOTO IFEND_2_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_2_LEVEL_0\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "END\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testForLoopAdvanced(){
        String programName = "test_source/ForLoopBasic.dcl";
        String expectedResult= "a := 0\r\n" + //
                                "b := 1\r\n" + //
                                "a := b\r\n" + //
                                "c := 10\r\n" + //
                                "LABEL FORBEG_0_LEVEL_0\r\n" + //
                                "IF a NE c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                "EXTERNAL CALL WriteInt(a)\r\n" + //
                                "d := 1\r\n" + //
                                "e := a IADD d\r\n" + //
                                "a := e\r\n" + //
                                "GOTO FORBEG_0_LEVEL_0\r\n" + //
                                "LABEL FOREND_0_LEVEL_0\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "END\r\n";
        testDeclanFileOnICode(programName, expectedResult);
    }

    @Test
    public void testForLoopBasic(){
        String programName = "test_source/ForLoopBasic.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 1\r\n" + //
                               "a := b\r\n" + //
                               "c := 10\r\n" + //
                               "LABEL FORBEG_0_LEVEL_0\r\n" + //
                               "IF a NE c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "d := 1\r\n" + //
                               "e := a IADD d\r\n" + //
                               "a := e\r\n" + //
                               "GOTO FORBEG_0_LEVEL_0\r\n" + //
                               "LABEL FOREND_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "END\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testForLoopBasic2(){
        String programName = "test_source/ForLoopBasic2.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 1\r\n" + //
                               "a := b\r\n" + //
                               "c := 10\r\n" + //
                               "LABEL FORBEG_0_LEVEL_0\r\n" + //
                               "IF a LT c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "d := 1\r\n" + //
                               "e := a IADD d\r\n" + //
                               "a := e\r\n" + //
                               "GOTO FORBEG_0_LEVEL_0\r\n" + //
                               "LABEL FOREND_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testForLoopBasic3(){
        String programName = "test_source/ForLoopBasic3.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 10\r\n" + //
                               "a := b\r\n" + //
                               "c := 1\r\n" + //
                               "LABEL FORBEG_0_LEVEL_0\r\n" + //
                               "IF a GT c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "d := 1\r\n" + //
                               "e := EXTERNAL CALL INeg(d)\r\n" + //
                               "f := a IADD e\r\n" + //
                               "a := f\r\n" + //
                               "GOTO FORBEG_0_LEVEL_0\r\n" + //
                               "LABEL FOREND_0_LEVEL_0\r\n" + //
                               "END\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testIfStatementAdvanced(){
        String programName = "test_source/IfStatementAdvanced.dcl";
        String expectedICodee = "a := TRUE\r\n" + //
                                "b := a\r\n" + //
                                "c := FALSE\r\n" + //
                                "d := c\r\n" + //
                                "IF b EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF d EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "e := 5\r\n" + //
                                "EXTERNAL CALL WriteInt(e)\r\n" + //
                                "GOTO IFEND_1_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "f := 6\r\n" + //
                                "EXTERNAL CALL WriteInt(f)\r\n" + //
                                "GOTO IFEND_1_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "LABEL IFEND_1_LEVEL_1\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF d EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_1 ELSE IFNEXT_3_SEQ_0_LEVEL_1\r\n" + //
                                "LABEL IFSTAT_3_SEQ_0_LEVEL_1\r\n" + //
                                "g := 7\r\n" + //
                                "EXTERNAL CALL WriteInt(g)\r\n" + //
                                "GOTO IFEND_3_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_3_SEQ_0_LEVEL_1\r\n" + //
                                "h := 8\r\n" + //
                                "EXTERNAL CALL WriteInt(h)\r\n" + //
                                "GOTO IFEND_3_LEVEL_1\r\n" + //
                                "LABEL IFNEXT_3_SEQ_1_LEVEL_1\r\n" + //
                                "LABEL IFEND_3_LEVEL_1\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "END\r\n";
        
        testDeclanFileOnICode(programName, expectedICodee);
    }

    @Test
    public void testIfStatementBasic(){
        String programName = "test_source/IfStatementBasic.dcl";
        String expectedICode = "a := TRUE\r\n" + //
                                "b := a\r\n" + //
                                "c := FALSE\r\n" + //
                                "d := c\r\n" + //
                                "IF b EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "e := 4\r\n" + //
                                "EXTERNAL CALL WriteInt(e)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "f := 5\r\n" + //
                                "EXTERNAL CALL WriteInt(f)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "IF d EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "g := 2\r\n" + //
                                "EXTERNAL CALL WriteInt(g)\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "IF b EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "h := 5\r\n" + //
                                "EXTERNAL CALL WriteInt(h)\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "i := 6\r\n" + //
                                "EXTERNAL CALL WriteInt(i)\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "LABEL IFEND_1_LEVEL_0\r\n" + //
                                "END\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testLoops(){
        String programName = "test_source/loops.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 1\r\n" + //
                               "a := b\r\n" + //
                               "c := 10\r\n" + //
                               "LABEL FORBEG_0_LEVEL_0\r\n" + //
                               "IF a LT c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "d := 1\r\n" + //
                               "e := a IADD d\r\n" + //
                               "a := e\r\n" + //
                               "GOTO FORBEG_0_LEVEL_0\r\n" + //
                               "LABEL FOREND_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "f := 1\r\n" + //
                               "a := f\r\n" + //
                               "g := 10\r\n" + //
                               "LABEL FORBEG_1_LEVEL_0\r\n" + //
                               "IF a LT g THEN FORLOOP_1_LEVEL_0 ELSE FOREND_1_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_1_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "h := 2\r\n" + //
                               "i := a IADD h\r\n" + //
                               "a := i\r\n" + //
                               "GOTO FORBEG_1_LEVEL_0\r\n" + //
                               "LABEL FOREND_1_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "j := 10\r\n" + //
                               "a := j\r\n" + //
                               "k := 1\r\n" + //
                               "LABEL FORBEG_2_LEVEL_0\r\n" + //
                               "IF a GT k THEN FORLOOP_2_LEVEL_0 ELSE FOREND_2_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_2_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "l := 2\r\n" + //
                               "m := EXTERNAL CALL INeg(l)\r\n" + //
                               "n := a IADD m\r\n" + //
                               "a := n\r\n" + //
                               "GOTO FORBEG_2_LEVEL_0\r\n" + //
                               "LABEL FOREND_2_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "o := 10\r\n" + //
                               "a := o\r\n" + //
                               "p := 1\r\n" + //
                               "LABEL FORBEG_3_LEVEL_0\r\n" + //
                               "IF a GT p THEN FORLOOP_3_LEVEL_0 ELSE FOREND_3_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_3_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "q := 1\r\n" + //
                               "r := EXTERNAL CALL INeg(q)\r\n" + //
                               "s := a IADD r\r\n" + //
                               "a := s\r\n" + //
                               "GOTO FORBEG_3_LEVEL_0\r\n" + //
                               "LABEL FOREND_3_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "t := 1\r\n" + //
                               "a := t\r\n" + //
                               "u := 10\r\n" + //
                               "v := a LE u\r\n" + //
                               "IF v EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                               "IF v EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                               "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "w := 1\r\n" + //
                               "x := a IADD w\r\n" + //
                               "a := x\r\n" + //
                               "y := 10\r\n" + //
                               "z := a LE y\r\n" + //
                               "v := z\r\n" + //
                               "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILEEND_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "A := 1\r\n" + //
                               "a := A\r\n" + //
                               "B := 10\r\n" + //
                               "C := a LE B\r\n" + //
                               "D := 2\r\n" + //
                               "E := EXTERNAL CALL Mod(a, D)\r\n" + //
                               "F := 1\r\n" + //
                               "G := E EQ F\r\n" + //
                               "H := C LAND G\r\n" + //
                               "IF H EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILECOND_2_SEQ_0_LEVEL_0\r\n" + //
                               "IF H EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0 ELSE WHILEEND_2_LEVEL_0\r\n" + //
                               "LABEL WHILESTAT_2_SEQ_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "I := 1\r\n" + //
                               "J := a IADD I\r\n" + //
                               "a := J\r\n" + //
                               "K := 10\r\n" + //
                               "L := a LE K\r\n" + //
                               "M := 2\r\n" + //
                               "N := EXTERNAL CALL Mod(a, M)\r\n" + //
                               "O := 1\r\n" + //
                               "P := N EQ O\r\n" + //
                               "Q := L LAND P\r\n" + //
                               "H := Q\r\n" + //
                               "GOTO WHILECOND_2_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILENEXT_2_SEQ_0_LEVEL_0\r\n" + //
                               "R := 10\r\n" + //
                               "S := a LE R\r\n" + //
                               "IF S EQ TRUE THEN WHILESTAT_2_SEQ_1_LEVEL_0 ELSE WHILENEXT_2_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL WHILECOND_2_SEQ_1_LEVEL_0\r\n" + //
                               "IF S EQ TRUE THEN WHILESTAT_2_SEQ_1_LEVEL_0 ELSE WHILEEND_2_LEVEL_0\r\n" + //
                               "LABEL WHILESTAT_2_SEQ_1_LEVEL_0\r\n" + //
                               "T := 1\r\n" + //
                               "U := a IADD T\r\n" + //
                               "a := U\r\n" + //
                               "V := 10\r\n" + //
                               "W := a LE V\r\n" + //
                               "S := W\r\n" + //
                               "GOTO WHILECOND_2_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL WHILENEXT_2_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL WHILEEND_2_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "X := 10\r\n" + //
                               "a := X\r\n" + //
                               "Y := 1\r\n" + //
                               "Z := a LT Y\r\n" + //
                               "LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                               "IF Z EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                               "LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "a0 := 2\r\n" + //
                               "a1 := a ISUB a0\r\n" + //
                               "a := a1\r\n" + //
                               "a2 := 1\r\n" + //
                               "a3 := a LT a2\r\n" + //
                               "Z := a3\r\n" + //
                               "GOTO REPEATBEG_0_LEVEL_0\r\n" + //
                               "LABEL REPEATEND_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "a4 := 10\r\n" + //
                               "a := a4\r\n" + //
                               "a5 := 1\r\n" + //
                               "a6 := a GE a5\r\n" + //
                               "LABEL REPEATBEG_1_LEVEL_0\r\n" + //
                               "IF a6 EQ TRUE THEN REPEATEND_1_LEVEL_0 ELSE REPEATLOOP_1_LEVEL_0\r\n" + //
                               "LABEL REPEATLOOP_1_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "a7 := 1\r\n" + //
                               "a8 := a IADD a7\r\n" + //
                               "a := a8\r\n" + //
                               "a9 := 1\r\n" + //
                               "b0 := a GE a9\r\n" + //
                               "a6 := b0\r\n" + //
                               "GOTO REPEATBEG_1_LEVEL_0\r\n" + //
                               "LABEL REPEATEND_1_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "END\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRepeatLoopBasic(){
        String programName = "test_source/RepeatLoopBasic.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 1\r\n" + //
                               "a := b\r\n" + //
                               "c := 10\r\n" + //
                               "d := a GE c\r\n" + //
                               "LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                               "IF d EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                               "LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(a)\r\n" + //
                               "e := 1\r\n" + //
                               "f := a IADD e\r\n" + //
                               "a := f\r\n" + //
                               "g := 10\r\n" + //
                               "h := a GE g\r\n" + //
                               "d := h\r\n" + //
                               "GOTO REPEATBEG_0_LEVEL_0\r\n" + //
                               "LABEL REPEATEND_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "END\r\n";
       testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testSample(){
        String programName = "test_source/sample.dcl";
        String expectedICode =  "a := 6\r\n" + //
                                "b := a\r\n" + //
                                "c := 7\r\n" + //
                                "d := c\r\n" + //
                                "e := 0\r\n" + //
                                "CALL gcd ( b -> f , d -> g )\r\n" + //
                                "i <| h\r\n" + //
                                "e := i\r\n" + //
                                "j := b IMUL d\r\n" + //
                                "k := j IMUL e\r\n" + //
                                "e := k\r\n" + //
                                "l := 1.0\r\n" + //
                                "m := EXTERNAL CALL IntToReal(e)\r\n" + //
                                "n := EXTERNAL CALL RMul(m, l)\r\n" + //
                                "EXTERNAL CALL WriteReal(n)\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "END\r\n" + //
                                "PROC LABEL gcd\r\n" + //
                                "o <- f\r\n" + //
                                "p <- g\r\n" + //
                                "q := o NE p\r\n" + //
                                "IF q EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF q EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "r := o GT p\r\n" + //
                                "IF r EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "s := o ISUB p\r\n" + //
                                "o := s\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "t := p ISUB o\r\n" + //
                                "p := t\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "u := o NE p\r\n" + //
                                "q := u\r\n" + //
                                "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "h |< o\r\n" + //
                                "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testTest(){
        String programName = "test_source/test.dcl";
        String expectedICode = "a := 42\r\n" + //
                               "b := EXTERNAL CALL INeg(a)\r\n" + //
                                "c := b\r\n" + //
                                "d := 0\r\n" + //
                                "d := c\r\n" + //
                                "g := 0\r\n" + //
                                "h := d LT g\r\n" + //
                                "IF h EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "i := EXTERNAL CALL INeg(d)\r\n" + //
                                "d := i\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "j := 0\r\n" + //
                                "k := d GT j\r\n" + //
                                "IF k EQ TRUE THEN IFSTAT_0_SEQ_1_LEVEL_0 ELSE IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "CALL Display ( d -> e )\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC LABEL Display\r\n" + //
                                "m <- e\r\n" + //
                                "l := FALSE\r\n" + //
                                "n := 2\r\n" + //
                                "o := EXTERNAL CALL Mod(m, n)\r\n" + //
                                "p := 0\r\n" + //
                                "q := o NE p\r\n" + //
                                "l := q\r\n" + //
                                "IF l EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "r := 1\r\n" + //
                                "EXTERNAL CALL WriteInt(r)\r\n" + //
                                "s := 1\r\n" + //
                                "t := m ISUB s\r\n" + //
                                "u := 2\r\n" + //
                                "v := EXTERNAL CALL Div(t, u)\r\n" + //
                                "m := v\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "w := 0\r\n" + //
                                "x := m EQ w\r\n" + //
                                "IF x EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "y := 1\r\n" + //
                                "z := EXTERNAL CALL INeg(y)\r\n" + //
                                "EXTERNAL CALL WriteInt(z)\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "A := 0\r\n" + //
                                "EXTERNAL CALL WriteInt(A)\r\n" + //
                                "B := 2\r\n" + //
                                "C := EXTERNAL CALL Div(m, B)\r\n" + //
                                "m := C\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "LABEL IFEND_1_LEVEL_0\r\n" + //
                                "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testTest2(){
        String programName = "test_source/test2.dcl";
        String expectedICode = "a := 42\r\n" + //
                                "b := EXTERNAL CALL INeg(a)\r\n" + //
                                "c := b\r\n" + //
                                "d := 0\r\n" + //
                                "d := c\r\n" + //
                                "g := 0\r\n" + //
                                "h := d LT g\r\n" + //
                                "IF h EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "IF h EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "i := EXTERNAL CALL INeg(d)\r\n" + //
                                "d := i\r\n" + //
                                "j := 0\r\n" + //
                                "k := d LT j\r\n" + //
                                "h := k\r\n" + //
                                "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "l := 0\r\n" + //
                                "m := d GT l\r\n" + //
                                "IF m EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                "IF m EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "LABEL WHILESTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "CALL Display ( d -> e )\r\n" + //
                                "n := 0\r\n" + //
                                "o := d GT n\r\n" + //
                                "m := o\r\n" + //
                                "GOTO WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "p := 10\r\n" + //
                                "d := p\r\n" + //
                                "q := 2\r\n" + //
                                "LABEL FORBEG_0_LEVEL_0\r\n" + //
                                "IF d GT q THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                "EXTERNAL CALL WriteInt(d)\r\n" + //
                                "r := 1\r\n" + //
                                "s := EXTERNAL CALL INeg(r)\r\n" + //
                                "t := d IADD s\r\n" + //
                                "d := t\r\n" + //
                                "GOTO FORBEG_0_LEVEL_0\r\n" + //
                                "LABEL FOREND_0_LEVEL_0\r\n" + //
                                "u := TRUE\r\n" + //
                                "LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                                "IF u EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                                "LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                                "CALL Display ( d -> e )\r\n" + //
                                "v := TRUE\r\n" + //
                                "u := v\r\n" + //
                                "GOTO REPEATBEG_0_LEVEL_0\r\n" + //
                                "LABEL REPEATEND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC LABEL Display\r\n" + //
                                "x <- e\r\n" + //
                                "w := FALSE\r\n" + //
                                "y := 2\r\n" + //
                                "z := EXTERNAL CALL Mod(x, y)\r\n" + //
                                "A := 0\r\n" + //
                                "B := z NE A\r\n" + //
                                "w := B\r\n" + //
                                "IF w EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "C := 1\r\n" + //
                                "EXTERNAL CALL WriteInt(C)\r\n" + //
                                "D := 1\r\n" + //
                                "E := x ISUB D\r\n" + //
                                "F := 2\r\n" + //
                                "G := EXTERNAL CALL Div(E, F)\r\n" + //
                                "x := G\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "H := 0\r\n" + //
                                "I := x EQ H\r\n" + //
                                "IF I EQ TRUE THEN IFSTAT_0_SEQ_1_LEVEL_0 ELSE IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "J := 1\r\n" + //
                                "K := EXTERNAL CALL INeg(J)\r\n" + //
                                "EXTERNAL CALL WriteInt(K)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "L := 0\r\n" + //
                                "EXTERNAL CALL WriteInt(L)\r\n" + //
                                "M := 2\r\n" + //
                                "N := EXTERNAL CALL Div(x, M)\r\n" + //
                                "x := N\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_2_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testTest3(){
        String programName = "test_source/test3.dcl";
        String expectedICode = "a := 42\r\n" + //
                               "b := a\r\n" + //
                               "c := 42.0\r\n" + //
                               "d := c\r\n" + //
                               "e := 0\r\n" + //
                                "f := 0\r\n" + //
                                "g := 0.0\r\n" + //
                                "h := 0.0\r\n" + //
                                "i := FALSE\r\n" + //
                                "o := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "p := EXTERNAL CALL RMul(o, h)\r\n" + //
                                "CALL Foo ( e -> j , g -> k , p -> l )\r\n" + //
                                "q <| m\r\n" + //
                                "i := q\r\n" + //
                                "IF i EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "EXTERNAL CALL WriteInt(e)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "r := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "s := EXTERNAL CALL RSub(d, r)\r\n" + //
                                "EXTERNAL CALL WriteReal(s)\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC LABEL Foo\r\n" + //
                                "u <- j\r\n" + //
                                "t := 0\r\n" + //
                                "t := b\r\n" + //
                                "LABEL FORBEG_0_LEVEL_0\r\n" + //
                                "IF t GT u THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                "CALL Bar (  )\r\n" + //
                                "v := 20\r\n" + //
                                "w := EXTERNAL CALL INeg(v)\r\n" + //
                                "x := t IADD w\r\n" + //
                                "t := x\r\n" + //
                                "GOTO FORBEG_0_LEVEL_0\r\n" + //
                                "LABEL FOREND_0_LEVEL_0\r\n" + //
                                "A := 3.1415927\r\n" + //
                                "B := EXTERNAL CALL RGreaterThan(z, A)\r\n" + //
                                "C := y LAND B\r\n" + //
                                "m |< C\r\n" + //
                                "RETURN\r\n" + //
                                "PROC LABEL Bar\r\n" + //
                                "D := FALSE\r\n" + //
                                "E := 1\r\n" + //
                                "F := e IADD E\r\n" + //
                                "e := F\r\n" + //
                                "G := BNOT i\r\n" + //
                                "IF G EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "H := TRUE\r\n" + //
                                "i := H\r\n" + //
                                "I := 0\r\n" + //
                                "J := 0\r\n" + //
                                "CALL Foo ( f -> j , I -> k , J -> l )\r\n" + //
                                "K <| m\r\n" + //
                                "D := K\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFEND_1_LEVEL_0\r\n" + //
                                "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testTest4(){
        String programName = "test_source/test4.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := 0\r\n" + //
                                "c := 0\r\n" + //
                                "j := EXTERNAL CALL ReadInt()\r\n" + //
                                "a := j\r\n" + //
                                "k := 0\r\n" + //
                                "b := k\r\n" + //
                                "LABEL FORBEG_0_LEVEL_0\r\n" + //
                                "IF b LT a THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                "CALL F ( b -> f )\r\n" + //
                                "l <| g\r\n" + //
                                "c := l\r\n" + //
                                "EXTERNAL CALL WriteInt(c)\r\n" + //
                                "CALL Fact ( b -> h )\r\n" + //
                                "m <| i\r\n" + //
                                "c := m\r\n" + //
                                "EXTERNAL CALL WriteInt(c)\r\n" + //
                                "EXTERNAL CALL WriteLn()\r\n" + //
                                "n := 1\r\n" + //
                                "o := b IADD n\r\n" + //
                                "b := o\r\n" + //
                                "GOTO FORBEG_0_LEVEL_0\r\n" + //
                                "LABEL FOREND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC LABEL M\r\n" + //
                                "r <- d\r\n" + //
                                "p := 0\r\n" + //
                                "q := 0\r\n" + //
                                "s := 0\r\n" + //
                                "t := r EQ s\r\n" + //
                                "IF t EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "u := 0\r\n" + //
                                "q := u\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "v := 1\r\n" + //
                                "w := r ISUB v\r\n" + //
                                "CALL M ( w -> d )\r\n" + //
                                "x <| e\r\n" + //
                                "p := x\r\n" + //
                                "CALL F ( p -> f )\r\n" + //
                                "y <| g\r\n" + //
                                "q := y\r\n" + //
                                "z := r ISUB q\r\n" + //
                                "q := z\r\n" + //
                                "GOTO IFEND_0_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_0_LEVEL_0\r\n" + //
                                "e |< q\r\n" + //
                                "RETURN\r\n" + //
                                "PROC LABEL F\r\n" + //
                                "C <- f\r\n" + //
                                "A := 0\r\n" + //
                                "B := 0\r\n" + //
                                "D := 0\r\n" + //
                                "E := C EQ D\r\n" + //
                                "IF E EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "F := 1\r\n" + //
                                "B := F\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "G := 1\r\n" + //
                                "H := C ISUB G\r\n" + //
                                "CALL F ( H -> f )\r\n" + //
                                "I <| g\r\n" + //
                                "A := I\r\n" + //
                                "CALL M ( A -> d )\r\n" + //
                                "J <| e\r\n" + //
                                "B := J\r\n" + //
                                "K := C ISUB B\r\n" + //
                                "B := K\r\n" + //
                                "GOTO IFEND_1_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_1_LEVEL_0\r\n" + //
                                "g |< B\r\n" + //
                                "RETURN\r\n" + //
                                "PROC LABEL Fact\r\n" + //
                                "N <- h\r\n" + //
                                "L := 0\r\n" + //
                                "M := 0\r\n" + //
                                "O := 0\r\n" + //
                                "P := N EQ O\r\n" + //
                                "IF P EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "LABEL IFSTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "Q := 1\r\n" + //
                                "M := Q\r\n" + //
                                "GOTO IFEND_2_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "R := 1\r\n" + //
                                "S := N ISUB R\r\n" + //
                                "CALL Fact ( S -> h )\r\n" + //
                                "T <| i\r\n" + //
                                "L := T\r\n" + //
                                "U := N IMUL L\r\n" + //
                                "M := U\r\n" + //
                                "GOTO IFEND_2_LEVEL_0\r\n" + //
                                "LABEL IFNEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                "LABEL IFEND_2_LEVEL_0\r\n" + //
                                "i |< M\r\n" + //
                                "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String programName = "test_source/WhileLoopAdvanced.dcl";
        String expectedICode = "a := 10\r\n" + //
                               "b := a\r\n" + //
                               "c := 0\r\n" + //
                               "d := c\r\n" + //
                               "e := 0\r\n" + //
                               "f := 0\r\n" + //
                               "e := d\r\n" + //
                               "g := e GT b\r\n" + //
                               "IF g EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                               "IF g EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                               "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(e)\r\n" + //
                               "h := 1\r\n" + //
                               "i := e IADD h\r\n" + //
                               "e := i\r\n" + //
                               "j := e GT b\r\n" + //
                               "g := j\r\n" + //
                               "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "k := e LT b\r\n" + //
                               "IF k EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                               "IF k EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                               "LABEL WHILESTAT_0_SEQ_1_LEVEL_0\r\n" + //
                               "f := d\r\n" + //
                               "l := f LT b\r\n" + //
                               "IF l EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_1 ELSE WHILENEXT_2_SEQ_0_LEVEL_1\r\n" + //
                               "LABEL WHILECOND_2_SEQ_0_LEVEL_1\r\n" + //
                               "IF l EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_1 ELSE WHILEEND_2_LEVEL_1\r\n" + //
                               "LABEL WHILESTAT_2_SEQ_0_LEVEL_1\r\n" + //
                               "EXTERNAL CALL WriteInt(f)\r\n" + //
                               "m := 1\r\n" + //
                               "n := f IADD m\r\n" + //
                               "f := n\r\n" + //
                               "o := f LT b\r\n" + //
                               "l := o\r\n" + //
                               "GOTO WHILECOND_2_SEQ_0_LEVEL_1\r\n" + //
                               "LABEL WHILENEXT_2_SEQ_0_LEVEL_1\r\n" + //
                               "LABEL WHILEEND_2_LEVEL_1\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "EXTERNAL CALL WriteInt(e)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "p := 1\r\n" + //
                               "q := e IADD p\r\n" + //
                               "e := q\r\n" + //
                               "r := e LT b\r\n" + //
                               "k := r\r\n" + //
                               "GOTO WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL WHILEEND_0_LEVEL_0\r\n" + //
                               "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }


    @Test
    public void testWhileLoopBasic(){
        String programName = "test_source/WhileLoopBasic.dcl";
        String expectedICode = "a := 10\r\n" + //
                               "b := a\r\n" + //
                               "c := 0\r\n" + //
                               "d := 0\r\n" + //
                               "c := d\r\n" + //
                               "e := c LT b\r\n" + //
                               "IF e EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                               "IF e EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                               "LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(c)\r\n" + //
                               "f := 1\r\n" + //
                               "g := c IADD f\r\n" + //
                               "c := g\r\n" + //
                               "h := c LT b\r\n" + //
                               "e := h\r\n" + //
                               "GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL WHILEEND_0_LEVEL_0\r\n" + //
                               "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testSingleConversion(){
        String programName = "test_source/SingleConversion.dcl";
        String expectedICode = "a := 5\r\n" + //
                                "b := a\r\n" + //
                                "c := 0.0\r\n" + //
                                "d := 0.0\r\n" + //
                                "e := EXTERNAL CALL IntToReal(b)\r\n" + //
                                "c := e\r\n" + //
                                "EXTERNAL CALL WriteReal(c)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testSingleConversion2(){
        String programName = "test_source/SingleConversion2.dcl";
        String expectedICode = "a := 6.5\r\n" + //
                                "b := a\r\n" + //
                                "c := 0\r\n" + //
                                "d := EXTERNAL CALL RealToInt(b)\r\n" + //
                                "c := d\r\n" + //
                                "EXTERNAL CALL WriteInt(c)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealAddition(){
        String programName = "test_source/RealAddition.dcl";
        String expectedICode = "a := 4.5\r\n" + //
                                "b := a\r\n" + //
                                "c := 8.5\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RAdd(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealAddition2(){
        String programName = "test_source/RealAddition2.dcl";
        String expectedICode = "a := 7.5\r\n" + //
                                "b := a\r\n" + //
                                "c := 49.5\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RSub(d, b)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealAddition3(){
        String programName = "test_source/RealAddition3.dcl";
        String expectedICode = "a := 50.0\r\n" + //
                                "b := a\r\n" + //
                                "c := 92.0\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RSub(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealMultiplication(){
        String programName = "test_source/RealMultiplication.dcl";
        String expectedICode = "a := 21.0\r\n" + //
                                "b := a\r\n" + //
                                "c := 2.0\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RMul(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealMultiplication2(){
        String programName = "test_source/RealMultiplication2.dcl";
        String expectedICode = "a := 30.32\r\n" + //
                                "b := a\r\n" + //
                                "c := 2.0\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RMul(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testIntegerDiv(){
        String programName = "test_source/IntegerDiv.dcl";
        String expectedICode = "a := 20\r\n" + //
                                "b := a\r\n" + //
                                "c := 5\r\n" + //
                                "d := c\r\n" + //
                                "e := 0\r\n" + //
                                "f := EXTERNAL CALL Div(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteInt(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testIntegerDiv2(){
        String programName = "test_source/IntegerDiv2.dcl";
        String expectedICode = "a := 30\r\n" + //
                                "b := a\r\n" + //
                                "c := 8\r\n" + //
                                "d := c\r\n" + //
                                "e := 0\r\n" + //
                                "f := EXTERNAL CALL Div(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteInt(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealDivision(){
        String programName = "test_source/RealDivision.dcl";
        String expectedICode = "a := 30.0\r\n" + //
                                "b := a\r\n" + //
                                "c := 2.0\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RDivide(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }

    @Test
    public void testRealDivision2(){
        String programName = "test_source/RealDivision.dcl";
        String expectedICode = "a := 30.0\r\n" + //
                                "b := a\r\n" + //
                                "c := 2.0\r\n" + //
                                "d := c\r\n" + //
                                "e := 0.0\r\n" + //
                                "f := EXTERNAL CALL RDivide(b, d)\r\n" + //
                                "e := f\r\n" + //
                                "EXTERNAL CALL WriteReal(e)\r\n" + //
                                "END\r\n";

        testDeclanFileOnICode(programName, expectedICode);
    }
}
