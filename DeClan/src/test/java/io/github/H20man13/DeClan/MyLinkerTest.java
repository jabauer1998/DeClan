package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrLinker;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class MyLinkerTest {
    private static void compareProgramStrings(String resultProgram, String expectedProgram){
        
        assertTrue("Error expected -\n\n" + expectedProgram + "\n\n but found -\n\n" + resultProgram, resultProgram.equals(expectedProgram));
        /* 
        int line = 0;
        int position = 0;

        int shorterLength = (resultProgram.length() < expectedProgram.length()) ? resultProgram.length() : expectedProgram.length();
        for(int i = 0; i < shorterLength; i++){
            char resultChar = resultProgram.charAt(i);
            char expectedChar = expectedProgram.charAt(i);
            assertTrue("Error got " + resultChar + " but expected " + expectedChar + " at line " + line + " and position " + position, resultChar == expectedChar);
            if(resultChar == '\r'){
                line++;
                position = 0;
            } else {
                position++;
            }
        }

        assertTrue("Result program length is equal to " + resultProgram.length() + "and expected program length is " + expectedProgram.length(), resultProgram.length() == expectedProgram.length());

        assertTrue("Result program --\n\n" + resultProgram + "\n\n is not equal to expected program \n\n" + expectedProgram, resultProgram.equals(expectedProgram));
        */
    }

    private static void regenerateProgram(Prog  prog, String expected){
        String program = prog.toString();
        compareProgramStrings(program, expected);
    }

    private static Prog parseProgram(String prog, ErrorLog errLog){
        StringReader reader = new StringReader(prog);
        ReaderSource source = new ReaderSource(reader);
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        return parser.parseProgram();
    }

    private static Lib parseLibrary(String library, ErrorLog errorLog){
        StringReader reader = new StringReader(library);
        ReaderSource source = new ReaderSource(reader);
        MyIrLexer lexer = new MyIrLexer(source, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);
        return parser.parseLibrary();
    }

    private static void linkProgramStrings(String expectedString, String prog, String... libs){
        ErrorLog errLog = new ErrorLog();
        Prog program = parseProgram(prog, errLog);

        Lib[] newLibs = new Lib[libs.length];
        for(int i = 0; i < libs.length; i++){
            newLibs[i] = parseLibrary(libs[i], errLog);
        }

        linkPrograms(errLog, expectedString, program, newLibs);
    }

    private static void linkPrograms(ErrorLog errLog, String expectedString, Prog prog, Lib... libs){
        MyIrLinker linker = new MyIrLinker(errLog);
        Prog program = linker.performLinkage(prog, libs);
        regenerateProgram(program, expectedString);
    }

    @Test
    public void linkProgramWithNothingInCommon(){
       String prog1 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + " a := 20\n"
                    + " v := 30\n"
                    + "CODE SECTION\n"
                    + " d := a IADD v\n"
                    + "END\n"
                    + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "a := 3\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + "a := 3\n"
                    + "b |< a\n"
                    + "RETURN\n";

        String res = "SYMBOL SECTION\r\n" + //
                     "DATA SECTION\r\n" + //
                     " a := 20\r\n" + //
                     " v := 30\r\n" + //
                     "CODE SECTION\r\n" + //
                     " d := a IADD v\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";

        linkProgramStrings(res, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalVariable(){
        String prog1 = "SYMBOL SECTION\n"
                     + "a EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " v := 30\n"
                     + "CODE SECTION\n"
                     + " d := a IADD v\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n"
                    + "DATA SECTION\n"
                    + "a := 3\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + "a := 3\n"
                    + "b |< a\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " c INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " v := 30\r\n" + //
                     " c := 3\r\n" + //
                     "CODE SECTION\r\n" + //
                     " d := c IADD v\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void checkVariableRename(){
        String prog1 = "SYMBOL SECTION\n"
                     + "b EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " v := 30\n"
                     + " a := 20\n"
                     + "CODE SECTION\n"
                     + " d := b IADD v\n"
                     + " g := d IADD a\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + "a := 3\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + "a := 3\n"
                    + "b |< a\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " c INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " v := 30\r\n" + //
                     " a := 20\r\n" + //
                     " c := 3\r\n" + //
                    "CODE SECTION\r\n" + //
                    " d := c IADD v\r\n" + //
                    " g := d IADD a\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n";//

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalCall1(){
        String prog1 = "SYMBOL SECTION\n"
                     + "v EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " a := 20\n"
                     + " b := 500\n"
                     + "CODE SECTION\n"
                     + " d := EXTERNAL CALL func ( )\n"
                     + " g := d IADD v\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + "a := 3\n"
                    + "PROC SECTION\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func\n"
                    + "a := 3\n"
                    + "b |< a\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " f INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " a := 20\r\n" + //
                     " b := 500\r\n" + //
                     " f := 3\r\n" + //
                     "CODE SECTION\r\n" + //
                     " CALL func (  )\r\n" + //
                     " c <| e\r\n" + //
                     " g := c IADD f\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func\r\n" + //
                     "  d := 3\r\n" + //
                     "  e |< d\r\n" + //
                     " RETURN\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkExternalCall2(){
        String prog1 = "SYMBOL SECTION\n"
                     + "v EXTERNAL lib1VariableName\n"
                     + "DATA SECTION\n"
                     + " a := 20\n"
                     + " b := 500\n"
                     + "CODE SECTION\n"
                     + " d := EXTERNAL CALL func2 ( )\n"
                     + " g := d IADD v\n"
                     + "END\n"
                     + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                    + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                    + "DATA SECTION\n"
                    + "a := 3\n"
                    + "PROC SECTION\n"
                    + " PROC LABEL func2\n"
                    + "  b := EXTERNAL CALL func1 ( )\n"
                    + "  c := b ISUB a\n"
                    + "  d |< c\n"
                    + " RETURN\n";

        String lib2 = "SYMBOL SECTION\n"
                    + "DATA SECTION\n"
                    + "PROC SECTION\n"
                    + "PROC LABEL func1\n"
                    + "a := 3\n"
                    + "b |< a\n"
                    + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " n INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " a := 20\r\n" + //
                     " b := 500\r\n" + //
                     " n := 3\r\n" + //
                     "CODE SECTION\r\n" + //
                     " CALL func2 (  )\r\n" + //
                     " e <| h\r\n" + //
                     " g := e IADD n\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func2\r\n" + //
                     "  CALL func1 (  )\r\n" + //
                     "  i <| h\r\n" + //
                     "  l := i ISUB n\r\n" + //
                     "  h |< l\r\n" + //
                     " RETURN\r\n" + //
                     " PROC LABEL func1\r\n" + //
                     "  j := 3\r\n" + //
                     "  k |< j\r\n" + //
                     " RETURN\r\n";

         linkProgramStrings(exp, prog1, lib1, lib2);
    }

    @Test
    public void linkDuplicateLabels(){
        String prog1 = "SYMBOL SECTION\n"
                      + "v EXTERNAL lib1VariableName\n"
                      + "DATA SECTION\n"
                      + " a := 20\n"
                      + " b := 500\n"
                      + "CODE SECTION\n"
                      + " d := EXTERNAL CALL func2 ( )\n"
                      + " g := d IADD v\n"
                      + "LABEL begin2\n"
                      + "IF g EQ v THEN begin ELSE end\n"
                      + "LABEL begin\n"
                      + " g := d IADD v\n"
                      + "GOTO begin2\n"
                      + "LABEL end\n"
                      + "END\n"
                      + "PROC SECTION\n";

        String lib1 = "SYMBOL SECTION\n"
                      + "a INTERNAL lib1VariableName\n" //The internal Declaration will start out as an A
                      + "DATA SECTION\n"
                      + "a := 3\n"
                      + "PROC SECTION\n"
                      + " PROC LABEL func2\n"
                      + "  b := EXTERNAL CALL func1 ( )\n"
                      + "  c := b ISUB a\n"
                      + "  LABEL begin2\n"
                      + "  IF c EQ b THEN begin ELSE end\n"
                      + "  LABEL begin\n"
                      + "  e := c IADD b\n"
                      + "  GOTO begin2\n"
                      + "  LABEL end\n"
                      + "  d |< e\n"
                      + " RETURN\n";

        String lib2 = "SYMBOL SECTION\n"
                      + "DATA SECTION\n"
                      + "PROC SECTION\n"
                      + "PROC LABEL func1\n"
                      + "  a := 3\n"
                      + "  LABEL begin2\n"
                      + "  IF a EQ a THEN begin ELSE end\n"
                      + "  LABEL begin\n"
                      + "  e := a IADD a\n"
                      + "  GOTO begin2\n"
                      + "  LABEL end\n"
                      + "  b |< e\n"
                      + "RETURN\n";

        String exp = "SYMBOL SECTION\r\n" + //
                     " n INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " a := 20\r\n" + //
                     " b := 500\r\n" + //
                     " n := 3\r\n" + //
                     "CODE SECTION\r\n" + //
                     " CALL func2 (  )\r\n" + //
                     " f <| d\r\n" + //
                     " g := f IADD n\r\n" + //
                     " LABEL begin2\r\n" + //
                     " IF g EQ n THEN begin ELSE end\r\n" + //
                     " LABEL begin\r\n" + //
                     " g := f IADD n\r\n" + //
                     " GOTO begin2\r\n" + //
                     " LABEL end\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func2\r\n" + //
                     "  CALL func1 (  )\r\n" + //
                     "  i <| d\r\n" + //
                     "  c := i ISUB n\r\n" + //
                     "  LABEL begin2_1\r\n" + //
                     "  IF c EQ i THEN begin_1 ELSE end_1\r\n" + //
                     "  LABEL begin_1\r\n" + //
                     "  h := c IADD i\r\n" + //
                     "  GOTO begin2_1\r\n" + //
                     "  LABEL end_1\r\n" + //
                     "  d |< h\r\n" + //
                     " RETURN\r\n" + //
                     " PROC LABEL func1\r\n" + //
                     "  k := 3\r\n" + //
                     "  LABEL begin2_0\r\n" + //
                     "  IF k EQ k THEN begin_0 ELSE end_0\r\n" + //
                     "  LABEL begin_0\r\n" + //
                     "  e := k IADD k\r\n" + //
                     "  GOTO begin2_0\r\n" + //
                     "  LABEL end_0\r\n" + //
                     "  j |< e\r\n" + //
                     " RETURN\r\n";

        linkProgramStrings(exp, prog1, lib1, lib2);
    }

    private static void linkTestProgram(String expectedResult, String prgSrc){
        ErrorLog log = new ErrorLog();
        try{
            FileReader reader = new FileReader(prgSrc);
            Source source = new ReaderSource(reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, log);
            MyDeClanParser parser = new MyDeClanParser(lexer, log);
            Program prog = parser.parseProgram();
            MyStandardLibrary lib = new MyStandardLibrary(log);
            MyIrLinker linker = new MyIrLinker(log);
            Prog irCode = linker.performLinkage(prog, lib.ioLibrary(), lib.mathLibrary(), lib.conversionsLibrary(), lib.intLibrary(), lib.realLibrary(), lib.utilsLibrary());
            assertTrue("Expected -\n\n" + expectedResult + "\n\n but found \n\n" + irCode.toString(), irCode.toString().equals(expectedResult));
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        String progSrc = "test_source/conversions.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                    " a := 0\r\n" + //
                                    " b := 0\r\n" + //
                                    " c := 0.0\r\n" + //
                                    " d := 0.0\r\n" + //
                                    "CODE SECTION\r\n" + //
                                    " h := 1\r\n" + //
                                    " b := h\r\n" + //
                                    " i := 1.0\r\n" + //
                                    " c := i\r\n" + //
                                    " j := 2\r\n" + //
                                    " a := j\r\n" + //
                                    " k := 2.0\r\n" + //
                                    " d := k\r\n" + //
                                    " CALL WriteInt ( b -] W0 )\r\n" + //
                                    " CALL WriteReal ( c -] W2 )\r\n" + //
                                    " CALL WriteReal ( d -] W2 )\r\n" + //
                                    " CALL WriteLn (  )\r\n" + //
                                    " CALL Divide ( b -] W5 , a -] W7 )\r\n" + //
                                    " W4 [| X0\r\n" + //
                                    " CALL WriteReal ( W4 -] W2 )\r\n" + //
                                    " m := 5\r\n" + //
                                    " n := b IADD m\r\n" + //
                                    " o := 6\r\n" + //
                                    " p := b IADD o\r\n" + //
                                    " q := n IMUL p\r\n" + //
                                    " CALL WriteInt ( q -] W0 )\r\n" + //
                                    " r := 4\r\n" + //
                                    " s := a IADD r\r\n" + //
                                    " t := 5.0\r\n" + //
                                    " CALL IntToReal ( a -] X4 )\r\n" + //
                                    " b20 [| X7\r\n" + //
                                    " CALL RAdd ( b20 -] b22 , t -] b24 )\r\n" + //
                                    " b21 [| b27\r\n" + //
                                    " CALL IntToReal ( s -] X4 )\r\n" + //
                                    " e44 [| X7\r\n" + //
                                    " CALL RMul ( e44 -] e46 , b21 -] e48 )\r\n" + //
                                    " e45 [| e51\r\n" + //
                                    " CALL WriteReal ( e45 -] W2 )\r\n" + //
                                    " CALL WriteLn (  )\r\n" + //
                                    " y := 3.1415\r\n" + //
                                    " CALL p ( b -> e , y -> f )\r\n" + //
                                    " z <| g\r\n" + //
                                    " a := z\r\n" + //
                                    " CALL WriteReal ( d -] W2 )\r\n" + //
                                    " CALL WriteLn (  )\r\n" + //
                                    "END\r\n" + //
                                    "PROC SECTION\r\n" + //
                                    " PROC LABEL WriteInt\r\n" + //
                                    "  W1 <- W0\r\n" + //
                                    "  IPARAM W1\r\n" + //
                                    "IASM \"LDR R0, %a\"\r\n" + //
                                    "  IASM \"SWI 1\"\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL WriteReal\r\n" + //
                                    "  W3 <- W2\r\n" + //
                                    "  IPARAM W3\r\n" + //
                                    "IASM \"LDR R0, %a\"\r\n" + //
                                    "  IASM \"SWI 2\"\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL WriteLn\r\n" + //
                                    "  IASM \"SWI 4\"\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL Divide\r\n" + //
                                    "  W6 <- W5\r\n" + //
                                    "  W8 <- W7\r\n" + //
                                    "  X1 := 0\r\n" + //
                                    "  W9 := 0\r\n" + //
                                    "  CALL Div ( W6 -> c , W8 -> d )\r\n" + //
                                    "  X2 [| e\r\n" + //
                                    "  X1 := X2\r\n" + //
                                    "  CALL IntToReal ( X1 -> X4 )\r\n" + //
                                    "  X3 [| X0\r\n" + //
                                    "  W9 := X3\r\n" + //
                                    "  X0 |[ W9\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL IntToReal\r\n" + //
                                    "  X5 [- X4\r\n" + //
                                    "  b19 := 0.0\r\n" + //
                                    "  a21 := FALSE\r\n" + //
                                    "  b17 := 0\r\n" + //
                                    "  a40 := 0\r\n" + //
                                    "  a43 := 0\r\n" + //
                                    "  a46 := 0\r\n" + //
                                    "  a37 := 0\r\n" + //
                                    "  a79 := 0\r\n" + //
                                    "  a90 := 0\r\n" + //
                                    "  a97 := 0\r\n" + //
                                    "  a95 := 0\r\n" + //
                                    "  Y9 := 0\r\n" + //
                                    "  a46 := X5\r\n" + //
                                    "  Z1 := 0\r\n" + //
                                    "  a43 := Z1\r\n" + //
                                    "  CALL IntIsNegative ( X5 -] Z4 )\r\n" + //
                                    "  Z3 [| X7\r\n" + //
                                    "  a21 := Z3\r\n" + //
                                    "  CALL IntIsZero ( Z0 -] a23 )\r\n" + //
                                    "  a22 [| X7\r\n" + //
                                    "  a49 := BNOT a22\r\n" + //
                                    "  IF a49 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  IF a49 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0_0_0 ELSE WHILEEND_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0_0_0\r\n" + //
                                    "  a35 := 1\r\n" + //
                                    "  a36 := a46 IAND a35\r\n" + //
                                    "  a37 := a36\r\n" + //
                                    "  a38 := 1\r\n" + //
                                    "  a39 := a37 EQ a38\r\n" + //
                                    "  IF a39 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  a40 := a43\r\n" + //
                                    "  GOTO IFEND_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFEND_0_LEVEL_0_0_0\r\n" + //
                                    "  a41 := 1\r\n" + //
                                    "  a42 := a43 IADD a41\r\n" + //
                                    "  a43 := a42\r\n" + //
                                    "  a44 := 1\r\n" + //
                                    "  a45 := a46 IRSHIFT a44\r\n" + //
                                    "  a46 := a45\r\n" + //
                                    "  CALL IntIsZero ( a46 -] a23 )\r\n" + //
                                    "  a47 [| X7\r\n" + //
                                    "  a48 := BNOT a47\r\n" + //
                                    "  a49 := a48\r\n" + //
                                    "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL WHILEEND_0_LEVEL_0_0_0\r\n" + //
                                    "  a50 := 23\r\n" + //
                                    "  a51 := a40 LT a50\r\n" + //
                                    "  IF a51 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  a52 := 23\r\n" + //
                                    "  a53 := a52 ISUB a40\r\n" + //
                                    "  a79 := a53\r\n" + //
                                    "  a55 := INOT a79\r\n" + //
                                    "  a56 := 1\r\n" + //
                                    "  a57 := a55 IADD a56\r\n" + //
                                    "  a90 := a57\r\n" + //
                                    "  a59 := -2147483648\r\n" + //
                                    "  a60 := INOT a59\r\n" + //
                                    "  a61 := a90 IAND a60\r\n" + //
                                    "  a90 := a61\r\n" + //
                                    "  a63 := 64\r\n" + //
                                    "  a64 := a90 IOR a63\r\n" + //
                                    "  a90 := a64\r\n" + //
                                    "  a66 := 1\r\n" + //
                                    "  a67 := a66 ILSHIFT a40\r\n" + //
                                    "  a68 := 1\r\n" + //
                                    "  a69 := a67 ISUB a68\r\n" + //
                                    "  a95 := a69\r\n" + //
                                    "  a71 := X5 IAND a95\r\n" + //
                                    "  a97 := a71\r\n" + //
                                    "  a73 := a97 ILSHIFT a79\r\n" + //
                                    "  a97 := a73\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  a75 := 23\r\n" + //
                                    "  a76 := a40 GT a75\r\n" + //
                                    "  IF a76 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_1_0\r\n" + //
                                    "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_0_0\r\n" + //
                                    "  a77 := 23\r\n" + //
                                    "  a78 := a40 ISUB a77\r\n" + //
                                    "  a79 := a78\r\n" + //
                                    "  a90 := a79\r\n" + //
                                    "  a81 := 1\r\n" + //
                                    "  a82 := a81 ILSHIFT a40\r\n" + //
                                    "  a83 := 1\r\n" + //
                                    "  a84 := a82 ISUB a83\r\n" + //
                                    "  a95 := a84\r\n" + //
                                    "  a86 := X5 IAND a95\r\n" + //
                                    "  a97 := a86\r\n" + //
                                    "  a88 := a97 IRSHIFT a79\r\n" + //
                                    "  a97 := a88\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_1_0\r\n" + //
                                    "  a90 := a40\r\n" + //
                                    "  a91 := 1\r\n" + //
                                    "  a92 := a91 ILSHIFT a40\r\n" + //
                                    "  a93 := 1\r\n" + //
                                    "  a94 := a92 ISUB a93\r\n" + //
                                    "  a95 := a94\r\n" + //
                                    "  a96 := X5 IAND a95\r\n" + //
                                    "  a97 := a96\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_2_LEVEL_0_0\r\n" + //
                                    "  LABEL IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                    "  b17 := a97\r\n" + //
                                    "  a99 := 23\r\n" + //
                                    "  b10 := a90 ILSHIFT a99\r\n" + //
                                    "  b11 := b17 IOR b10\r\n" + //
                                    "  b17 := b11\r\n" + //
                                    "  IF a21 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  b13 := 1\r\n" + //
                                    "  b14 := 31\r\n" + //
                                    "  b15 := b13 ILSHIFT b14\r\n" + //
                                    "  b16 := b17 IOR b15\r\n" + //
                                    "  b17 := b16\r\n" + //
                                    "  GOTO IFEND_2_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  LABEL IFEND_2_LEVEL_0_0_0\r\n" + //
                                    "  CALL IntBinaryAsReal ( b17 -] e37 )\r\n" + //
                                    "  b18 [| e40\r\n" + //
                                    "  b19 := b18\r\n" + //
                                    "  X7 |[ b19\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL IntIsNegative\r\n" + //
                                    "  Z5 [- Z4\r\n" + //
                                    "  a20 := FALSE\r\n" + //
                                    "  a14 := 0\r\n" + //
                                    "  a10 := 31\r\n" + //
                                    "  a11 := Z5 IRSHIFT a10\r\n" + //
                                    "  a12 := 1\r\n" + //
                                    "  a13 := a11 IAND a12\r\n" + //
                                    "  a14 := a13\r\n" + //
                                    "  a15 := 0\r\n" + //
                                    "  a16 := a14 EQ a15\r\n" + //
                                    "  IF a16 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  a17 := FALSE\r\n" + //
                                    "  a20 := a17\r\n" + //
                                    "  GOTO IFEND_2_LEVEL_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  a19 := TRUE\r\n" + //
                                    "  a20 := a19\r\n" + //
                                    "  GOTO IFEND_2_LEVEL_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                    "  LABEL IFEND_2_LEVEL_0_0_0_0\r\n" + //
                                    "  Z7 |[ a20\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL IntIsZero\r\n" + //
                                    "  a24 [- a23\r\n" + //
                                    "  a33 := FALSE\r\n" + //
                                    "  a28 := 0\r\n" + //
                                    "  a29 := a24 EQ a28\r\n" + //
                                    "  IF a29 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  a30 := TRUE\r\n" + //
                                    "  a33 := a30\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  a32 := FALSE\r\n" + //
                                    "  a33 := a32\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                    "  LABEL IFEND_1_LEVEL_0_0_0_0\r\n" + //
                                    "  a26 |[ a33\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL RAdd\r\n" + //
                                    "  b23 [- b22\r\n" + //
                                    "  b25 [- b24\r\n" + //
                                    "  e43 := 0.0\r\n" + //
                                    "  e35 := 0\r\n" + //
                                    "  b61 := 0\r\n" + //
                                    "  b63 := 0\r\n" + //
                                    "  d90 := 0\r\n" + //
                                    "  d91 := 0\r\n" + //
                                    "  d54 := 0\r\n" + //
                                    "  d94 := 0\r\n" + //
                                    "  d57 := 0\r\n" + //
                                    "  e23 := 0\r\n" + //
                                    "  e18 := 0\r\n" + //
                                    "  e26 := 0\r\n" + //
                                    "  d24 := 0\r\n" + //
                                    "  CALL RealSign ( b23 -] b42 )\r\n" + //
                                    "  b41 [| b27\r\n" + //
                                    "  b61 := b41\r\n" + //
                                    "  CALL RealSign ( b25 -] b42 )\r\n" + //
                                    "  b62 [| b27\r\n" + //
                                    "  b63 := b62\r\n" + //
                                    "  CALL RealExponent ( b23 -] b65 )\r\n" + //
                                    "  b64 [| b27\r\n" + //
                                    "  d91 := b64\r\n" + //
                                    "  CALL RealExponent ( b25 -] b65 )\r\n" + //
                                    "  b79 [| b27\r\n" + //
                                    "  d54 := b79\r\n" + //
                                    "  CALL RealMantissa ( b23 -] b82 )\r\n" + //
                                    "  b81 [| b27\r\n" + //
                                    "  d94 := b81\r\n" + //
                                    "  CALL RealMantissa ( b25 -] b82 )\r\n" + //
                                    "  b94 [| b27\r\n" + //
                                    "  d57 := b94\r\n" + //
                                    "  b96 := b61 EQ b63\r\n" + //
                                    "  IF b96 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  d24 := b61\r\n" + //
                                    "  b98 := d91 EQ d54\r\n" + //
                                    "  IF b98 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_1_0_0\r\n" + //
                                    "  LABEL IFSTAT_1_SEQ_0_LEVEL_1_0_0\r\n" + //
                                    "  b99 := d94 IADD d57\r\n" + //
                                    "  e26 := b99\r\n" + //
                                    "  c11 := 25\r\n" + //
                                    "  c12 := e26 IRSHIFT c11\r\n" + //
                                    "  c13 := 1\r\n" + //
                                    "  c14 := c12 IAND c13\r\n" + //
                                    "  e18 := c14\r\n" + //
                                    "  e23 := d91\r\n" + //
                                    "  c17 := 1\r\n" + //
                                    "  c18 := e18 EQ c17\r\n" + //
                                    "  IF c18 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_2_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  c19 := 1\r\n" + //
                                    "  c20 := e23 IADD c19\r\n" + //
                                    "  e23 := c20\r\n" + //
                                    "  c22 := 1\r\n" + //
                                    "  c23 := e26 IRSHIFT c22\r\n" + //
                                    "  e26 := c23\r\n" + //
                                    "  GOTO IFEND_2_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFNEXT_2_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFEND_2_LEVEL_2_0_0\r\n" + //
                                    "  c25 := 23\r\n" + //
                                    "  c26 := e23 ILSHIFT c25\r\n" + //
                                    "  e35 := c26\r\n" + //
                                    "  c28 := e35 IOR e26\r\n" + //
                                    "  e35 := c28\r\n" + //
                                    "  c30 := 31\r\n" + //
                                    "  c31 := d24 ILSHIFT c30\r\n" + //
                                    "  c32 := e35 IOR c31\r\n" + //
                                    "  e35 := c32\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_0_LEVEL_1_0_0\r\n" + //
                                    "  c34 := d91 GT d54\r\n" + //
                                    "  IF c34 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_1_0_0\r\n" + //
                                    "  LABEL IFSTAT_1_SEQ_1_LEVEL_1_0_0\r\n" + //
                                    "  c35 := d91 ISUB d54\r\n" + //
                                    "  d90 := c35\r\n" + //
                                    "  d54 := d91\r\n" + //
                                    "  e23 := d91\r\n" + //
                                    "  c39 := d57 IRSHIFT d90\r\n" + //
                                    "  d57 := c39\r\n" + //
                                    "  c41 := d94 IADD d57\r\n" + //
                                    "  e26 := c41\r\n" + //
                                    "  c43 := 25\r\n" + //
                                    "  c44 := e26 IRSHIFT c43\r\n" + //
                                    "  c45 := 1\r\n" + //
                                    "  c46 := c44 IAND c45\r\n" + //
                                    "  e18 := c46\r\n" + //
                                    "  c48 := 1\r\n" + //
                                    "  c49 := e18 EQ c48\r\n" + //
                                    "  IF c49 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_4_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_4_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  c50 := 1\r\n" + //
                                    "  c51 := e23 IADD c50\r\n" + //
                                    "  e23 := c51\r\n" + //
                                    "  c53 := 1\r\n" + //
                                    "  c54 := e26 IRSHIFT c53\r\n" + //
                                    "  e26 := c54\r\n" + //
                                    "  GOTO IFEND_4_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFNEXT_4_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFEND_4_LEVEL_2_0_0\r\n" + //
                                    "  c56 := 23\r\n" + //
                                    "  c57 := e23 ILSHIFT c56\r\n" + //
                                    "  e35 := c57\r\n" + //
                                    "  c59 := 31\r\n" + //
                                    "  c60 := d24 ILSHIFT c59\r\n" + //
                                    "  c61 := e35 IOR c60\r\n" + //
                                    "  e35 := c61\r\n" + //
                                    "  c63 := e35 IOR e26\r\n" + //
                                    "  e35 := c63\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_1_LEVEL_1_0_0\r\n" + //
                                    "  c65 := d54 ISUB d91\r\n" + //
                                    "  d90 := c65\r\n" + //
                                    "  d91 := d54\r\n" + //
                                    "  e23 := d54\r\n" + //
                                    "  c69 := d94 IRSHIFT d90\r\n" + //
                                    "  d94 := c69\r\n" + //
                                    "  c71 := d94 IADD d57\r\n" + //
                                    "  e26 := c71\r\n" + //
                                    "  c73 := 25\r\n" + //
                                    "  c74 := e26 IRSHIFT c73\r\n" + //
                                    "  c75 := 1\r\n" + //
                                    "  c76 := c74 IAND c75\r\n" + //
                                    "  e18 := c76\r\n" + //
                                    "  c78 := 1\r\n" + //
                                    "  c79 := e18 EQ c78\r\n" + //
                                    "\r\n" + //
                                    "  IF c79 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_6_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_6_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  c80 := 1\r\n" + //
                                    "  c81 := e23 IADD c80\r\n" + //
                                    "  e23 := c81\r\n" + //
                                    "  c83 := 1\r\n" + //
                                    "  c84 := e26 IRSHIFT c83\r\n" + //
                                    "  e26 := c84\r\n" + //
                                    "  GOTO IFEND_6_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFNEXT_6_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFEND_6_LEVEL_2_0_0\r\n" + //
                                    "  c86 := 23\r\n" + //
                                    "  c87 := e23 ILSHIFT c86\r\n" + //
                                    "  e35 := c87\r\n" + //
                                    "  c89 := 31\r\n" + //
                                    "  c90 := d24 ILSHIFT c89\r\n" + //
                                    "  c91 := e35 IOR c90\r\n" + //
                                    "  e35 := c91\r\n" + //
                                    "  c93 := e35 IOR e26\r\n" + //
                                    "  e35 := c93\r\n" + //
                                    "  GOTO IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_1_SEQ_2_LEVEL_1_0\r\n" + //
                                    "  LABEL IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                    "  GOTO IFEND_0_LEVEL_0_0_1_0\r\n" + //
                                    "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0_1\r\n" + //
                                    "  c95 := 0\r\n" + //
                                    "  c96 := b61 EQ c95\r\n" + //
                                    "  c97 := 1\r\n" + //
                                    "  c98 := b63 EQ c97\r\n" + //
                                    "  c99 := c96 LAND c98\r\n" + //
                                    "  IF c99 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1_0_0 ELSE IFNEXT_9_SEQ_0_LEVEL_1_0_0\r\n" + //
                                    "  LABEL IFSTAT_9_SEQ_0_LEVEL_1_0_0\r\n" + //
                                    "  d10 := d57 GT d94\r\n" + //
                                    "  IF d10 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_10_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_10_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  d11 := 1\r\n" + //
                                    "  d24 := d11\r\n" + //
                                    "  GOTO IFEND_10_LEVEL_2_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_10_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  d13 := 0\r\n" + //
                                    "  d24 := d13\r\n" + //
                                    "  GOTO IFEND_10_LEVEL_2_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_10_SEQ_1_LEVEL_2_0\r\n" + //
                                    "  LABEL IFEND_10_LEVEL_2_0_0_0\r\n" + //
                                    "  GOTO IFEND_9_LEVEL_1_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_9_SEQ_0_LEVEL_1_0_0\r\n" + //
                                    "  d15 := 1\r\n" + //
                                    "  d16 := b61 EQ d15\r\n" + //
                                    "  d17 := 0\r\n" + //
                                    "  d18 := b63 EQ d17\r\n" + //
                                    "  d19 := d16 LAND d18\r\n" + //
                                    "  IF d19 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_12_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_12_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  d20 := d57 GE d94\r\n" + //
                                    "  IF d20 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_13_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFSTAT_13_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  d21 := 0\r\n" + //
                                    "  d24 := d21\r\n" + //
                                    "  GOTO IFEND_13_LEVEL_3_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_13_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  d23 := 1\r\n" + //
                                    "  d24 := d23\r\n" + //
                                    "  GOTO IFEND_13_LEVEL_3_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_13_SEQ_1_LEVEL_3_0\r\n" + //
                                    "  LABEL IFEND_13_LEVEL_3_0_0_0\r\n" + //
                                    "  GOTO IFEND_12_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFNEXT_12_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFEND_12_LEVEL_2_0_0\r\n" + //
                                    "  d25 := d91 EQ d54\r\n" + //
                                    "  IF d25 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_15_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_15_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  d26 := 0\r\n" + //
                                    "  e26 := d26\r\n" + //
                                    "  d28 := 25\r\n" + //
                                    "  d29 := e26 IRSHIFT d28\r\n" + //
                                    "  d30 := 1\r\n" + //
                                    "  d31 := d29 IAND d30\r\n" + //
                                    "  e18 := d31\r\n" + //
                                    "  e23 := d91\r\n" + //
                                    "  d34 := 1\r\n" + //
                                    "  d35 := e18 EQ d34\r\n" + //
                                    "  IF d35 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_16_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFSTAT_16_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  d36 := 1\r\n" + //
                                    "  d37 := e23 IADD d36\r\n" + //
                                    "  e23 := d37\r\n" + //
                                    "  d39 := 1\r\n" + //
                                    "  d40 := e26 IRSHIFT d39\r\n" + //
                                    "  e26 := d40\r\n" + //
                                    "  GOTO IFEND_16_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFNEXT_16_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFEND_16_LEVEL_3_0_0\r\n" + //
                                    "  d42 := 23\r\n" + //
                                    "  d43 := e23 ILSHIFT d42\r\n" + //
                                    "  e35 := d43\r\n" + //
                                    "  d45 := e35 IOR e26\r\n" + //
                                    "  e35 := d45\r\n" + //
                                    "  d47 := 31\r\n" + //
                                    "  d48 := d24 ILSHIFT d47\r\n" + //
                                    "  d49 := e35 IOR d48\r\n" + //
                                    "  e35 := d49\r\n" + //
                                    "  GOTO IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_15_SEQ_0_LEVEL_2_0_0\r\n" + //
                                    "  d51 := d91 GT d54\r\n" + //
                                    "  IF d51 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2_0_0 ELSE IFNEXT_15_SEQ_1_LEVEL_2_0_0\r\n" + //
                                    "  LABEL IFSTAT_15_SEQ_1_LEVEL_2_0_0\r\n" + //
                                    "  d52 := d91 ISUB d54\r\n" + //
                                    "  d90 := d52\r\n" + //
                                    "  d54 := d91\r\n" + //
                                    "  e23 := d91\r\n" + //
                                    "  d56 := d57 IRSHIFT d90\r\n" + //
                                    "  d57 := d56\r\n" + //
                                    "  d58 := 1\r\n" + //
                                    "  d59 := b61 EQ d58\r\n" + //
                                    "  d60 := 0\r\n" + //
                                    "  d61 := b63 EQ d60\r\n" + //
                                    "  d62 := d59 LAND d61\r\n" + //
                                    "  IF d62 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_18_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFSTAT_18_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  d63 := d57 ISUB d94\r\n" + //
                                    "  e26 := d63\r\n" + //
                                    "  GOTO IFEND_18_LEVEL_3_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_18_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  d65 := d94 ISUB d57\r\n" + //
                                    "  e26 := d65\r\n" + //
                                    "  GOTO IFEND_18_LEVEL_3_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_18_SEQ_1_LEVEL_3_0\r\n" + //
                                    "  LABEL IFEND_18_LEVEL_3_0_0_0\r\n" + //
                                    "  d67 := 25\r\n" + //
                                    "  d68 := e26 IRSHIFT d67\r\n" + //
                                    "  d69 := 1\r\n" + //
                                    "  d70 := d68 IAND d69\r\n" + //
                                    "  e18 := d70\r\n" + //
                                    "  d72 := 1\r\n" + //
                                    "  d73 := e18 EQ d72\r\n" + //
                                    "  IF d73 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_19_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFSTAT_19_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  d74 := 1\r\n" + //
                                    "  d75 := e23 IADD d74\r\n" + //
                                    "  e23 := d75\r\n" + //
                                    "  d77 := 1\r\n" + //
                                    "  d78 := e26 IRSHIFT d77\r\n" + //
                                    "  e26 := d78\r\n" + //
                                    "  GOTO IFEND_19_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFNEXT_19_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFEND_19_LEVEL_3_0_0\r\n" + //
                                    "  d80 := 23\r\n" + //
                                    "  d81 := e23 ILSHIFT d80\r\n" + //
                                    "  e35 := d81\r\n" + //
                                    "  d83 := 31\r\n" + //
                                    "  d84 := d24 ILSHIFT d83\r\n" + //
                                    "  d85 := e35 IOR d84\r\n" + //
                                    "  e35 := d85\r\n" + //
                                    "  d87 := e35 IOR e26\r\n" + //
                                    "  e35 := d87\r\n" + //
                                    "  GOTO IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_15_SEQ_1_LEVEL_2_0_0\r\n" + //
                                    "  d89 := d54 ISUB d91\r\n" + //
                                    "  d90 := d89\r\n" + //
                                    "  d91 := d54\r\n" + //
                                    "  e23 := d54\r\n" + //
                                    "  d93 := d94 IRSHIFT d90\r\n" + //
                                    "  d94 := d93\r\n" + //
                                    "  d95 := 1\r\n" + //
                                    "  d96 := b61 EQ d95\r\n" + //
                                    "  d97 := 0\r\n" + //
                                    "  d98 := b63 EQ d97\r\n" + //
                                    "  d99 := d96 LAND d98\r\n" + //
                                    "  IF d99 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_21_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFSTAT_21_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  e10 := d57 ISUB d94\r\n" + //
                                    "  e26 := e10\r\n" + //
                                    "  GOTO IFEND_21_LEVEL_3_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_21_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  e12 := d94 ISUB d57\r\n" + //
                                    "  e26 := e12\r\n" + //
                                    "  GOTO IFEND_21_LEVEL_3_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_21_SEQ_1_LEVEL_3_0\r\n" + //
                                    "  LABEL IFEND_21_LEVEL_3_0_0_0\r\n" + //
                                    "  e14 := 25\r\n" + //
                                    "  e15 := e26 IRSHIFT e14\r\n" + //
                                    "  e16 := 1\r\n" + //
                                    "  e17 := e15 IAND e16\r\n" + //
                                    "  e18 := e17\r\n" + //
                                    "  e19 := 1\r\n" + //
                                    "  e20 := e18 EQ e19\r\n" + //
                                    "  IF e20 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_22_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFSTAT_22_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  e21 := 1\r\n" + //
                                    "  e22 := e23 IADD e21\r\n" + //
                                    "  e23 := e22\r\n" + //
                                    "  e24 := 1\r\n" + //
                                    "  e25 := e26 IRSHIFT e24\r\n" + //
                                    "  e26 := e25\r\n" + //
                                    "  GOTO IFEND_22_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFNEXT_22_SEQ_0_LEVEL_3_0_0\r\n" + //
                                    "  LABEL IFEND_22_LEVEL_3_0_0\r\n" + //
                                    "  e27 := 23\r\n" + //
                                    "  e28 := e23 ILSHIFT e27\r\n" + //
                                    "  e35 := e28\r\n" + //
                                    "  e30 := 31\r\n" + //
                                    "  e31 := d24 ILSHIFT e30\r\n" + //
                                    "  e32 := e35 IOR e31\r\n" + //
                                    "  e35 := e32\r\n" + //
                                    "  e34 := e35 IOR e26\r\n" + //
                                    "  e35 := e34\r\n" + //
                                    "  GOTO IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_15_SEQ_2_LEVEL_2_0\r\n" + //
                                    "  LABEL IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                    "  GOTO IFEND_9_LEVEL_1_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_9_SEQ_1_LEVEL_1_0\r\n" + //
                                    "  LABEL IFEND_9_LEVEL_1_0_0_0\r\n" + //
                                    "  GOTO IFEND_0_LEVEL_0_0_1_0\r\n" + //
                                    "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                    "  LABEL IFEND_0_LEVEL_0_0_1_0\r\n" + //
                                    "  CALL IntBinaryAsReal ( e35 -] e37 )\r\n" + //
                                    "  e36 [| b27\r\n" + //
                                    "  e43 := e36\r\n" + //
                                    "  b27 |[ e43\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL RealSign\r\n" + //
                                    "  b43 [- b42\r\n" + //
                                    "  b60 := 0\r\n" + //
                                    "  b55 := 0\r\n" + //
                                    "  CALL RealBinaryAsInt ( b43 -] b49 )\r\n" + //
                                    "  b48 [| b45\r\n" + //
                                    "  b55 := b48\r\n" + //
                                    "  b56 := 31\r\n" + //
                                    "  b57 := b55 IRSHIFT b56\r\n" + //
                                    "  b58 := 1\r\n" + //
                                    "  b59 := b57 IAND b58\r\n" + //
                                    "  b60 := b59\r\n" + //
                                    "  b45 |[ b60\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL RealBinaryAsInt\r\n" + //
                                    "  b50 [- b49\r\n" + //
                                    "  b53 := 0\r\n" + //
                                    "  b54 := 0.0\r\n" + //
                                    "  IPARAM b54\r\n" + //
                                    "IPARAM b50\r\n" + //
                                    "IASM \"LDR %r, %a\"\r\n" + //
                                    "  IPARAM b54\r\n" + //
                                    "IPARAM b53\r\n" + //
                                    "IASM \"STR %r, %a\"\r\n" + //
                                    "  b52 |[ b53\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL RealExponent\r\n" + //
                                    "  b66 [- b65\r\n" + //
                                    "  b77 := 0\r\n" + //
                                    "  b72 := 0\r\n" + //
                                    "  CALL RealBinaryAsInt ( b66 -] b49 )\r\n" + //
                                    "  b71 [| b68\r\n" + //
                                    "  b72 := b71\r\n" + //
                                    "  b73 := 23\r\n" + //
                                    "  b74 := b72 IRSHIFT b73\r\n" + //
                                    "  b75 := 255\r\n" + //
                                    "  b76 := b74 IAND b75\r\n" + //
                                    "  b77 := b76\r\n" + //
                                    "  b68 |[ b77\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL RealMantissa\r\n" + //
                                    "  b83 [- b82\r\n" + //
                                    "  b92 := 0\r\n" + //
                                    "  b89 := 0\r\n" + //
                                    "  CALL RealBinaryAsInt ( b83 -] b49 )\r\n" + //
                                    "  b88 [| b85\r\n" + //
                                    "  b89 := b88\r\n" + //
                                    "  b90 := 8388607\r\n" + //
                                    "  b91 := b89 IAND b90\r\n" + //
                                    "  b92 := b91\r\n" + //
                                    "  b85 |[ b92\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL IntBinaryAsReal\r\n" + //
                                    "  e38 [- e37\r\n" + //
                                    "  e41 := 0.0\r\n" + //
                                    "  e42 := 0\r\n" + //
                                    "  IPARAM e42\r\n" + //
                                    "IPARAM e38\r\n" + //
                                    "IASM \"LDR %r, %a\"\r\n" + //
                                    "  IPARAM e42\r\n" + //
                                    "IPARAM e41\r\n" + //
                                    "IASM \"STR %r, %a\"\r\n" + //
                                    "  e40 |[ e41\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL RMul\r\n" + //
                                    "  e47 [- e46\r\n" + //
                                    "  e49 [- e48\r\n" + //
                                    "  e94 := 0.0\r\n" + //
                                    "  e92 := 0\r\n" + //
                                    "  e64 := 0\r\n" + //
                                    "  e66 := 0\r\n" + //
                                    "  e68 := 0\r\n" + //
                                    "  e70 := 0\r\n" + //
                                    "  e72 := 0\r\n" + //
                                    "  e74 := 0\r\n" + //
                                    "  e79 := 0\r\n" + //
                                    "  e81 := 0\r\n" + //
                                    "  e83 := 0\r\n" + //
                                    "  CALL RealSign ( e47 -] b42 )\r\n" + //
                                    "  e63 [| e51\r\n" + //
                                    "  e64 := e63\r\n" + //
                                    "  CALL RealSign ( e49 -] b42 )\r\n" + //
                                    "  e65 [| e51\r\n" + //
                                    "  e66 := e65\r\n" + //
                                    "  CALL RealExponent ( e47 -] b65 )\r\n" + //
                                    "  e67 [| e51\r\n" + //
                                    "  e68 := e67\r\n" + //
                                    "  CALL RealExponent ( e49 -] b65 )\r\n" + //
                                    "  e69 [| e51\r\n" + //
                                    "  e70 := e69\r\n" + //
                                    "  CALL RealMantissa ( e47 -] b82 )\r\n" + //
                                    "  e71 [| e51\r\n" + //
                                    "  e72 := e71\r\n" + //
                                    "  CALL RealMantissa ( e49 -] b82 )\r\n" + //
                                    "  e73 [| e51\r\n" + //
                                    "  e74 := e73\r\n" + //
                                    "  e75 := e64 NE e66\r\n" + //
                                    "  IF e75 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  LABEL IFSTAT_26_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  e76 := 1\r\n" + //
                                    "  e79 := e76\r\n" + //
                                    "  GOTO IFEND_26_LEVEL_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_26_SEQ_0_LEVEL_0_0_0\r\n" + //
                                    "  e78 := 0\r\n" + //
                                    "  e79 := e78\r\n" + //
                                    "  GOTO IFEND_26_LEVEL_0_0_0_0\r\n" + //
                                    "  LABEL IFNEXT_26_SEQ_1_LEVEL_0_0\r\n" + //
                                    "  LABEL IFEND_26_LEVEL_0_0_0_0\r\n" + //
                                    "  e80 := e72 IMUL e74\r\n" + //
                                    "  e81 := e80\r\n" + //
                                    "  e82 := e68 IADD e70\r\n" + //
                                    "  e83 := e82\r\n" + //
                                    "  e84 := 23\r\n" + //
                                    "  e85 := e83 ILSHIFT e84\r\n" + //
                                    "  e92 := e85\r\n" + //
                                    "  e87 := 31\r\n" + //
                                    "  e88 := e79 ILSHIFT e87\r\n" + //
                                    "  e89 := e92 IOR e88\r\n" + //
                                    "  e92 := e89\r\n" + //
                                    "  e91 := e92 IOR e81\r\n" + //
                                    "  e92 := e91\r\n" + //
                                    "  CALL IntBinaryAsReal ( e92 -> e37 )\r\n" + //
                                    "  e93 <| e51\r\n" + //
                                    "  e94 := e93\r\n" + //
                                    "  e51 |< e94\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL p\r\n" + //
                                    "  e96 <- e95\r\n" + //
                                    "  e98 <- e97\r\n" + //
                                    "  e99 := 0\r\n" + //
                                    "  CALL IntToReal ( e98 -] X4 )\r\n" + //
                                    "  f11 <| f10\r\n" + //
                                    "  CALL RAdd ( e96 -> b22 , f11 -> b24 )\r\n" + //
                                    "  f12 <| f10\r\n" + //
                                    "  CALL Round ( f12 -> f14 )\r\n" + //
                                    "  f13 <| f10\r\n" + //
                                    "  e99 := f13\r\n" + //
                                    "  f10 |< e99\r\n" + //
                                    " RETURN\r\n" + //
                                    " PROC LABEL Round\r\n" + //
                                    "  f15 <- f14\r\n" + //
                                    "  f18 := 0.5\r\n" + //
                                    "  CALL RAdd ( f15 -> b22 , f18 -> b24 )\r\n" + //
                                    "  f19 <| f17\r\n" + //
                                    "  CALL Floor ( f19 -> k )\r\n" + //
                                    "  f20 <| l\r\n" + //
                                    "  f17 |< f20\r\n" + //
                                    " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }
}
