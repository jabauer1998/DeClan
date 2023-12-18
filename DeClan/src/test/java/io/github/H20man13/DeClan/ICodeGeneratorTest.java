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
    public void testExpressions(){
        String programName = "test_source/expressions.dcl";
        String expectedICode = "a := 0\r\n" + //
                               "b := a\r\n" + //
                               "c := 1.2\r\n" + //
                               "d := c\r\n" + //
                               "e := 3.14\r\n" + //
                               "f := EXTERNAL CALL RNeg(e)\r\n" + //
                               "g := f RADD b\r\n" + //
                               "h := g\r\n" + //
                               "i := 6\r\n" + //
                               "j := 6\r\n" + //
                               "k := 1\r\n" + //
                               "l := j IADD k\r\n" + //
                               "m := i IMUL l\r\n" + //
                               "n := m\r\n" + //
                               "o := b RMUL d\r\n" + //
                               "p := EXTERNAL CALL Divide(h, n)\r\n" + //
                               "q := o NE p\r\n" + //
                               "r := q\r\n" + //
                               "s := 0.0\r\n" + //
                               "t := 0.0\r\n" + //
                               "u := 0.0\r\n" + //
                               "v := 0\r\n" + //
                               "w := 0\r\n" + //
                               "x := 0\r\n" + //
                               "y := 0.0\r\n" + //
                               "z := 0.0\r\n" + //
                               "A := 0.0\r\n" + //
                               "B := EXTERNAL CALL Divide(d, d)\r\n" + //
                               "s := B\r\n" + //
                               "C := 10\r\n" + //
                               "D := EXTERNAL CALL Mod(n, C)\r\n" + //
                               "v := D\r\n" + //
                               "E := EXTERNAL CALL RNeg(h)\r\n" + //
                               "F := d RMUL b\r\n" + //
                               "G := E RSUB F\r\n" + //
                               "t := G\r\n" + //
                               "EXTERNAL CALL WriteInt(b)\r\n" + //
                               "EXTERNAL CALL WriteReal(s)\r\n" + //
                               "EXTERNAL CALL WriteReal(s)\r\n" + //
                               "EXTERNAL CALL WriteReal(t)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "H := EXTERNAL CALL Div(n, v)\r\n" + //
                               "w := H\r\n" + //
                               "I := EXTERNAL CALL Divide(n, v)\r\n" + //
                               "u := I\r\n" + //
                               "EXTERNAL CALL WriteInt(w)\r\n" + //
                               "EXTERNAL CALL WriteReal(u)\r\n" + //
                               "EXTERNAL CALL WriteLn()\r\n" + //
                               "J := EXTERNAL CALL Round(h)\r\n" + //
                               "u := J\r\n" + //
                               "EXTERNAL CALL WriteReal(u)\r\n" + //
                               "IF r EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                               "K := 2\r\n" + //
                               "L := 2\r\n" + //
                               "M := K IMUL L\r\n" + //
                               "EXTERNAL CALL WriteInt(M)\r\n" + //
                               "GOTO IFEND_0_LEVEL_0\r\n" + //
                               "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                            "N := 10\r\n" + //
                            "O := EXTERNAL CALL Divide(n, N)\r\n" + //
                            "EXTERNAL CALL WriteReal(O)\r\n" + //
                            "GOTO IFEND_0_LEVEL_0\r\n" + //
                            "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                            "LABEL IFEND_0_LEVEL_0\r\n" + //
                            "EXTERNAL CALL WriteLn()\r\n" + //
                            "P := BNOT r\r\n" + //
                            "Q := s GT b\r\n" + //
                            "R := P LAND Q\r\n" + //
                            "y := R\r\n" + //
                            "S := BNOT r\r\n" + //
                            "T := s GE b\r\n" + //
                            "U := S LOR T\r\n" + //
                            "z := U\r\n" + //
                            "V := y EQ z\r\n" + //
                            "A := V\r\n" + //
                            "IF y EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                            "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                            "W := 4\r\n" + //
                            "EXTERNAL CALL WriteInt(W)\r\n" + //
                            "GOTO IFEND_1_LEVEL_0\r\n" + //
                            "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                            "IF z EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                            "LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                            "X := 5\r\n" + //
                            "EXTERNAL CALL WriteInt(X)\r\n" + //
                            "GOTO IFEND_1_LEVEL_0\r\n" + //
                            "LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                            "LABEL IFEND_1_LEVEL_0\r\n" + //
                            "IF A EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                            "LABEL IFSTAT_2_SEQ_0_LEVEL_0\r\n" + //
                            "Y := 5\r\n" + //
                            "EXTERNAL CALL WriteInt(Y)\r\n" + //
                            "GOTO IFEND_2_LEVEL_0\r\n" + //
                            "LABEL IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                            "Z := 6\r\n" + //
                            "EXTERNAL CALL WriteInt(Z)\r\n" + //
                            "GOTO IFEND_2_LEVEL_0\r\n" + //
                            "LABEL IFNEXT_2_SEQ_1_LEVEL_0\r\n" + //
                            "LABEL IFEND_2_LEVEL_0\r\n" + //
                            "EXTERNAL CALL WriteLn()\r\n" + //
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
                               "i := 0.0\r\n" + //
                               "o := b RMUL h\r\n" + //
                               "CALL Foo ( e -> j , g -> k , o -> l )\r\n" + //
                               "p <| m\r\n" + //
                               "i := p\r\n" + //
                               "IF i EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                               "EXTERNAL CALL WriteInt(e)\r\n" + //
                               "GOTO IFEND_0_LEVEL_0\r\n" + //
                               "LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                               "q := d RSUB b\r\n" + //
                               "EXTERNAL CALL WriteReal(q)\r\n" + //
                               "GOTO IFEND_0_LEVEL_0\r\n" + //
                               "LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                               "LABEL IFEND_0_LEVEL_0\r\n" + //
                               "END\r\n" + //
                               "PROC LABEL Foo\r\n" + //
                               "s <- j\r\n" + //
                               "r := 0\r\n" + //
                               "r := b\r\n" + //
                               "LABEL FORBEG_0_LEVEL_0\r\n" + //
                               "IF r GT s THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                               "LABEL FORLOOP_0_LEVEL_0\r\n" + //
                               "CALL Bar (  )\r\n" + //
                               "t := 20\r\n" + //
                               "u := EXTERNAL CALL INeg(t)\r\n" + //
                               "v := r IADD u\r\n" + //
                               "r := v\r\n" + //
                               "GOTO FORBEG_0_LEVEL_0\r\n" + //
                               "LABEL FOREND_0_LEVEL_0\r\n" + //
                               "y := 3.14159265\r\n" + //
                               "m |< \r\n" + //
                               "RETURN\r\n" + //
                               "PROC LABEL Bar\r\n" + //
                               "z := 0.0\r\n" + //
                               "A := 1\r\n" + //
                               "B := e IADD A\r\n" + //
                               "e := B\r\n" + //
                               "C := BNOT i\r\n" + //
                               "IF C EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                               "D := TRUE\r\n" + //
                               "i := D\r\n" + //
                               "E := 0\r\n" + //
                               "F := 0\r\n" + //
                               "CALL Foo ( f -> j , E -> k , F -> l )\r\n" + //
                               "G <| m\r\n" + //
                               "z := G\r\n" + //
                               "GOTO IFEND_1_LEVEL_0\r\n" + //
                               "LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                               "LABEL IFEND_1_LEVEL_0\r\n" + //
                               "RETURN\r\n";
        testDeclanFileOnICode(programName, expectedICode);
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
}
