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
                     " d <| e\r\n" + //
                     " g := d IADD f\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func\r\n" + //
                     "  c := 3\r\n" + //
                     "  e |< c\r\n" + //
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
                    " j INTERNAL lib1VariableName\r\n" + //
                    "DATA SECTION\r\n" + //
                    " a := 20\r\n" + //
                    " b := 500\r\n" + //
                    " j := 3\r\n" + //
                    "CODE SECTION\r\n" + //
                    " CALL func2 (  )\r\n" + //
                    " e <| d\r\n" + //
                    " g := e IADD j\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n" + //
                    " PROC LABEL func2\r\n" + //
                    "  CALL func1 (  )\r\n" + //
                    "  f <| i\r\n" + //
                    "  c := f ISUB j\r\n" + //
                    "  d |< c\r\n" + //
                    " RETURN\r\n" + //
                    " PROC LABEL func1\r\n" + //
                    "  h := 3\r\n" + //
                    "  i |< h\r\n" + //
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
                    " l INTERNAL lib1VariableName\r\n" + //
                    "DATA SECTION\r\n" + //
                    " a := 20\r\n" + //
                    " b := 500\r\n" + //
                    " l := 3\r\n" + //
                    "CODE SECTION\r\n" + //
                    " CALL func2 (  )\r\n" + //
                    " f <| d\r\n" + //
                    " g := f IADD l\r\n" + //
                    " LABEL begin2\r\n" + //
                    " IF g EQ l THEN begin ELSE end\r\n" + //
                    " LABEL begin\r\n" + //
                    " g := f IADD l\r\n" + //
                    " GOTO begin2\r\n" + //
                    " LABEL end\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n" + //
                    " PROC LABEL func2\r\n" + //
                    "  CALL func1 (  )\r\n" + //
                    "  i <| j\r\n" + //
                    "  c := i ISUB l\r\n" + //
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
                                " b83 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                " b := 0\r\n" + //
                                " c := 0.0\r\n" + //
                                " d := 0.0\r\n" + //
                                " b82 := 127\r\n" + //
                                " b83 := b82\r\n" + //
                                "CODE SECTION\r\n" + //
                                " h := 1\r\n" + //
                                " b := h\r\n" + //
                                " i := 1.0\r\n" + //
                                " c := i\r\n" + //
                                " j := 2\r\n" + //
                                " a := j\r\n" + //
                                " k := 2.0\r\n" + //
                                " d := k\r\n" + //
                                " CALL WriteInt ( b -] Y5 )\r\n" + //
                                " CALL WriteReal ( c -] Y7 )\r\n" + //
                                " CALL WriteReal ( d -] Y7 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Divide ( b -] Z0 , a -] Z2 )\r\n" + //
                                " Y9 [| Z5\r\n" + //
                                " CALL WriteReal ( Y9 -] Y7 )\r\n" + //
                                " m := 5\r\n" + //
                                " n := b IADD m\r\n" + //
                                " o := 6\r\n" + //
                                " p := b IADD o\r\n" + //
                                " q := n IMUL p\r\n" + //
                                " CALL WriteInt ( q -] Y5 )\r\n" + //
                                " r := 4\r\n" + //
                                " s := a IADD r\r\n" + //
                                " t := 5.0\r\n" + //
                                " CALL IntToReal ( a -] a26 )\r\n" + //
                                " b32 [| a29\r\n" + //
                                " CALL RAdd ( b32 -] b34 , t -] b36 )\r\n" + //
                                " b33 [| b39\r\n" + //
                                " CALL IntToReal ( s -] a26 )\r\n" + //
                                " c90 [| a29\r\n" + //
                                " CALL RMul ( c90 -] c92 , b33 -] c93 )\r\n" + //
                                " c91 [| c94\r\n" + //
                                " CALL WriteReal ( c91 -] Y7 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " y := 3.1415\r\n" + //
                                " CALL p ( b -] c97 , y -] c95 )\r\n" + //
                                " z [| d10\r\n" + //
                                " a := z\r\n" + //
                                " CALL WriteReal ( d -] Y7 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  Y6 [- Y5\r\n" + //
                                "  IPARAM Y6\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  Y8 [- Y7\r\n" + //
                                "  IPARAM Y8\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Divide\r\n" + //
                                "  Z1 [- Z0\r\n" + //
                                "  Z3 [- Z2\r\n" + //
                                "  Z6 := 0\r\n" + //
                                "  Z4 := 0\r\n" + //
                                "  CALL Div ( Z1 -] Z7 , Z3 -] Z9 )\r\n" + //
                                "  a24 [| a12\r\n" + //
                                "  Z6 := a24\r\n" + //
                                "  CALL IntToReal ( Z6 -] a26 )\r\n" + //
                                "  a25 [| a29\r\n" + //
                                "  Z4 := a25\r\n" + //
                                "  Z5 |[ Z4\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  Z8 [- Z7\r\n" + //
                                "  a10 [- Z9\r\n" + //
                                "  a13 := 0\r\n" + //
                                "  a11 := 0\r\n" + //
                                "  a13 := Z8\r\n" + //
                                "  a14 := 0\r\n" + //
                                "  a11 := a14\r\n" + //
                                "  a15 := a13 ISUB a10\r\n" + //
                                "  a16 := 0\r\n" + //
                                "  a17 := a15 GT a16\r\n" + //
                                "  IF a17 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a17 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a18 := a13 ISUB a10\r\n" + //
                                "  a13 := a18\r\n" + //
                                "  a19 := 1\r\n" + //
                                "  a20 := a11 IADD a19\r\n" + //
                                "  a11 := a20\r\n" + //
                                "  a21 := a13 ISUB a10\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  a23 := a21 GT a22\r\n" + //
                                "  a17 := a23\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a12 |[ a11\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  a27 [- a26\r\n" + //
                                "  a28 := 0.0\r\n" + //
                                "  a30 := FALSE\r\n" + //
                                "  a31 := 0\r\n" + //
                                "  a32 := 0\r\n" + //
                                "  a33 := 0\r\n" + //
                                "  a34 := 0\r\n" + //
                                "  a35 := 0\r\n" + //
                                "  a36 := 0\r\n" + //
                                "  a37 := 0\r\n" + //
                                "  a38 := 0\r\n" + //
                                "  a39 := 0\r\n" + //
                                "  a40 := 0\r\n" + //
                                "  a34 := a27\r\n" + //
                                "  a41 := 0\r\n" + //
                                "  a33 := a41\r\n" + //
                                "  CALL IntIsNegative ( a27 -] a43 )\r\n" + //
                                "  a42 [| a46\r\n" + //
                                "  a30 := a42\r\n" + //
                                "  CALL IntIsZero ( a34 -] a57 )\r\n" + //
                                "  a56 [| a60\r\n" + //
                                "  a65 := BNOT a56\r\n" + //
                                "  IF a65 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF a65 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a66 := 1\r\n" + //
                                "  a67 := a34 IAND a66\r\n" + //
                                "  a35 := a67\r\n" + //
                                "  a68 := 1\r\n" + //
                                "  a69 := a35 EQ a68\r\n" + //
                                "  IF a69 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a32 := a33\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  a70 := 1\r\n" + //
                                "  a71 := a33 IADD a70\r\n" + //
                                "  a33 := a71\r\n" + //
                                "  a72 := 1\r\n" + //
                                "  a73 := a34 IRSHIFT a72\r\n" + //
                                "  a34 := a73\r\n" + //
                                "  CALL IntIsZero ( a34 -] a57 )\r\n" + //
                                "  a74 [| a60\r\n" + //
                                "  a75 := BNOT a74\r\n" + //
                                "  a65 := a75\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  a76 := 23\r\n" + //
                                "  a77 := a32 LT a76\r\n" + //
                                "  IF a77 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a78 := 23\r\n" + //
                                "  a79 := a78 ISUB a32\r\n" + //
                                "  a36 := a79\r\n" + //
                                "  a80 := INOT a36\r\n" + //
                                "  a81 := 1\r\n" + //
                                "  a82 := a80 IADD a81\r\n" + //
                                "  a37 := a82\r\n" + //
                                "  a83 := 2147483648\r\n" + //
                                "  a84 := INOT a83\r\n" + //
                                "  a85 := a37 IAND a84\r\n" + //
                                "  a37 := a85\r\n" + //
                                "  a86 := 64\r\n" + //
                                "  a87 := a37 IOR a86\r\n" + //
                                "  a37 := a87\r\n" + //
                                "  a88 := 1\r\n" + //
                                "  a89 := a88 ILSHIFT a32\r\n" + //
                                "  a90 := 1\r\n" + //
                                "  a91 := a89 ISUB a90\r\n" + //
                                "  a39 := a91\r\n" + //
                                "  a92 := a27 IAND a39\r\n" + //
                                "  a38 := a92\r\n" + //
                                "  a93 := a38 ILSHIFT a36\r\n" + //
                                "  a38 := a93\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a94 := 23\r\n" + //
                                "  a95 := a32 GT a94\r\n" + //
                                "  IF a95 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a96 := 23\r\n" + //
                                "  a97 := a32 ISUB a96\r\n" + //
                                "  a36 := a97\r\n" + //
                                "  a37 := a36\r\n" + //
                                "  a98 := 1\r\n" + //
                                "  a99 := a98 ILSHIFT a32\r\n" + //
                                "  b10 := 1\r\n" + //
                                "  b11 := a99 ISUB b10\r\n" + //
                                "  a39 := b11\r\n" + //
                                "  b12 := a27 IAND a39\r\n" + //
                                "  a38 := b12\r\n" + //
                                "  b13 := a38 IRSHIFT a36\r\n" + //
                                "  a38 := b13\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a37 := a32\r\n" + //
                                "  b14 := 1\r\n" + //
                                "  b15 := b14 ILSHIFT a32\r\n" + //
                                "  b16 := 1\r\n" + //
                                "  b17 := b15 ISUB b16\r\n" + //
                                "  a39 := b17\r\n" + //
                                "  b18 := a27 IAND a39\r\n" + //
                                "  a38 := b18\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  a31 := a38\r\n" + //
                                "  b19 := 23\r\n" + //
                                "  b20 := a37 ILSHIFT b19\r\n" + //
                                "  b21 := a31 IOR b20\r\n" + //
                                "  a31 := b21\r\n" + //
                                "  IF a30 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b22 := 1\r\n" + //
                                "  b23 := 31\r\n" + //
                                "  b24 := b22 ILSHIFT b23\r\n" + //
                                "  b25 := a31 IOR b24\r\n" + //
                                "  a31 := b25\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( a31 -] b26 )\r\n" + //
                                "  b31 [| b29\r\n" + //
                                "  a28 := b31\r\n" + //
                                "  a29 |[ a28\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  a44 [- a43\r\n" + //
                                "  a45 := FALSE\r\n" + //
                                "  a47 := 0\r\n" + //
                                "  a48 := 31\r\n" + //
                                "  a49 := a44 IRSHIFT a48\r\n" + //
                                "  a50 := 1\r\n" + //
                                "  a51 := a49 IAND a50\r\n" + //
                                "  a47 := a51\r\n" + //
                                "  a52 := 0\r\n" + //
                                "  a53 := a47 EQ a52\r\n" + //
                                "  IF a53 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a54 := FALSE\r\n" + //
                                "  a45 := a54\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a55 := TRUE\r\n" + //
                                "  a45 := a55\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  a46 |[ a45\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a58 [- a57\r\n" + //
                                "  a59 := FALSE\r\n" + //
                                "  a61 := 0\r\n" + //
                                "  a62 := a58 EQ a61\r\n" + //
                                "  IF a62 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a63 := TRUE\r\n" + //
                                "  a59 := a63\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a64 := FALSE\r\n" + //
                                "  a59 := a64\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  a60 |[ a59\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  b27 [- b26\r\n" + //
                                "  b28 := 0.0\r\n" + //
                                "  b30 := 0\r\n" + //
                                "  IPARAM b30\r\n" + //
                                "  IPARAM b27\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b30\r\n" + //
                                "  IPARAM b28\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  b29 |[ b28\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  b35 [- b34\r\n" + //
                                "  b37 [- b36\r\n" + //
                                "  b38 := 0.0\r\n" + //
                                "  b40 := 0\r\n" + //
                                "  b41 := 0\r\n" + //
                                "  b42 := 0\r\n" + //
                                "  b43 := 0\r\n" + //
                                "  b44 := 0\r\n" + //
                                "  b45 := 0\r\n" + //
                                "  b46 := 0\r\n" + //
                                "  b47 := 0\r\n" + //
                                "  b48 := 0\r\n" + //
                                "  b49 := 0\r\n" + //
                                "  b50 := 0\r\n" + //
                                "  b51 := 0\r\n" + //
                                "  CALL RealSign ( b35 -] b53 )\r\n" + //
                                "  b52 [| b56\r\n" + //
                                "  b41 := b52\r\n" + //
                                "  CALL RealSign ( b37 -] b53 )\r\n" + //
                                "  b68 [| b56\r\n" + //
                                "  b42 := b68\r\n" + //
                                "  CALL RealExponent ( b35 -] b70 )\r\n" + //
                                "  b69 [| b73\r\n" + //
                                "  b44 := b69\r\n" + //
                                "  CALL RealExponent ( b37 -] b70 )\r\n" + //
                                "  b84 [| b73\r\n" + //
                                "  b45 := b84\r\n" + //
                                "  CALL RealMantissa ( b35 -] b86 )\r\n" + //
                                "  b85 [| b89\r\n" + //
                                "  b46 := b85\r\n" + //
                                "  CALL RealMantissa ( b37 -] b86 )\r\n" + //
                                "  b94 [| b89\r\n" + //
                                "  b47 := b94\r\n" + //
                                "  b95 := b41 EQ b42\r\n" + //
                                "  IF b95 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b51 := b41\r\n" + //
                                "  b96 := b44 EQ b45\r\n" + //
                                "  IF b96 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b97 := b46 IADD b47\r\n" + //
                                "  b50 := b97\r\n" + //
                                "  b98 := 25\r\n" + //
                                "  b99 := b50 IRSHIFT b98\r\n" + //
                                "  c10 := 1\r\n" + //
                                "  c11 := b99 IAND c10\r\n" + //
                                "  b49 := c11\r\n" + //
                                "  c12 := b44 IADD b83\r\n" + //
                                "  b48 := c12\r\n" + //
                                "  c13 := 1\r\n" + //
                                "  c14 := b49 EQ c13\r\n" + //
                                "  IF c14 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  c15 := 1\r\n" + //
                                "  c16 := b48 IADD c15\r\n" + //
                                "  b48 := c16\r\n" + //
                                "  c17 := 1\r\n" + //
                                "  c18 := b50 IRSHIFT c17\r\n" + //
                                "  b50 := c18\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  c19 := 255\r\n" + //
                                "  c20 := b48 IAND c19\r\n" + //
                                "  c21 := 23\r\n" + //
                                "  c22 := c20 ILSHIFT c21\r\n" + //
                                "  b40 := c22\r\n" + //
                                "  c23 := b40 IOR b50\r\n" + //
                                "  b40 := c23\r\n" + //
                                "  c24 := 31\r\n" + //
                                "  c25 := b51 ILSHIFT c24\r\n" + //
                                "  c26 := b40 IOR c25\r\n" + //
                                "  b40 := c26\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  c27 := b44 GT b45\r\n" + //
                                "  IF c27 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c28 := b44 ISUB b45\r\n" + //
                                "  b43 := c28\r\n" + //
                                "  b45 := b44\r\n" + //
                                "  c29 := b44 IADD b83\r\n" + //
                                "  b48 := c29\r\n" + //
                                "  c30 := b47 IRSHIFT b43\r\n" + //
                                "  b47 := c30\r\n" + //
                                "  c31 := b46 IADD b47\r\n" + //
                                "  b50 := c31\r\n" + //
                                "  c32 := 25\r\n" + //
                                "  c33 := b50 IRSHIFT c32\r\n" + //
                                "  c34 := 1\r\n" + //
                                "  c35 := c33 IAND c34\r\n" + //
                                "  b49 := c35\r\n" + //
                                "  c36 := 1\r\n" + //
                                "\r\n" + //
                                "  c37 := b49 EQ c36\r\n" + //
                                "  IF c37 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  c38 := 1\r\n" + //
                                "  c39 := b48 IADD c38\r\n" + //
                                "  b48 := c39\r\n" + //
                                "  c40 := 1\r\n" + //
                                "  c41 := b50 IRSHIFT c40\r\n" + //
                                "  b50 := c41\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  c42 := 255\r\n" + //
                                "  c43 := b48 IAND c42\r\n" + //
                                "  c44 := 23\r\n" + //
                                "  c45 := c43 ILSHIFT c44\r\n" + //
                                "  b40 := c45\r\n" + //
                                "  c46 := 31\r\n" + //
                                "  c47 := b51 ILSHIFT c46\r\n" + //
                                "  c48 := b40 IOR c47\r\n" + //
                                "  b40 := c48\r\n" + //
                                "  c49 := b40 IOR b50\r\n" + //
                                "  b40 := c49\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c50 := b45 ISUB b44\r\n" + //
                                "  b43 := c50\r\n" + //
                                "  b44 := b45\r\n" + //
                                "  c51 := b45 IADD b83\r\n" + //
                                "  b48 := c51\r\n" + //
                                "  c52 := b46 IRSHIFT b43\r\n" + //
                                "  b46 := c52\r\n" + //
                                "  c53 := b46 IADD b47\r\n" + //
                                "  b50 := c53\r\n" + //
                                "  c54 := 25\r\n" + //
                                "  c55 := b50 IRSHIFT c54\r\n" + //
                                "  c56 := 1\r\n" + //
                                "  c57 := c55 IAND c56\r\n" + //
                                "  b49 := c57\r\n" + //
                                "  c58 := 1\r\n" + //
                                "  c59 := b49 EQ c58\r\n" + //
                                "  IF c59 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  c60 := 1\r\n" + //
                                "  c61 := b48 IADD c60\r\n" + //
                                "  b48 := c61\r\n" + //
                                "  c62 := 1\r\n" + //
                                "  c63 := b50 IRSHIFT c62\r\n" + //
                                "  b50 := c63\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  c64 := 255\r\n" + //
                                "  c65 := b48 IAND c64\r\n" + //
                                "  c66 := 23\r\n" + //
                                "  c67 := c65 ILSHIFT c66\r\n" + //
                                "  b40 := c67\r\n" + //
                                "  c68 := 31\r\n" + //
                                "  c69 := b51 ILSHIFT c68\r\n" + //
                                "  c70 := b40 IOR c69\r\n" + //
                                "  b40 := c70\r\n" + //
                                "  c71 := b40 IOR b50\r\n" + //
                                "  b40 := c71\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  c72 := 0\r\n" + //
                                "  c73 := b41 EQ c72\r\n" + //
                                "  c74 := 1\r\n" + //
                                "  c75 := b42 EQ c74\r\n" + //
                                "  c76 := c73 LAND c75\r\n" + //
                                "  IF c76 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  c77 := b47 GT b46\r\n" + //
                                "  IF c77 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  h8 := 1\r\n" + //
                                "  b51 := h8\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  h9 := 0\r\n" + //
                                "  b51 := h9\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  i0 := 1\r\n" + //
                                "  i1 := b41 EQ i0\r\n" + //
                                "  i2 := 0\r\n" + //
                                "  i3 := b42 EQ i2\r\n" + //
                                "  c78 := i1 LAND i3\r\n" + //
                                "  IF c78 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  c79 := b47 GE b46\r\n" + //
                                "  IF c79 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  c80 := 0\r\n" + //
                                "  b51 := c80\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  c81 := 1\r\n" + //
                                "  b51 := c81\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  c82 := b44 EQ b45\r\n" + //
                                "  IF c82 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  c83 := 0\r\n" + //
                                "  b50 := c83\r\n" + //
                                "  c84 := 25\r\n" + //
                                "  c85 := b50 IRSHIFT c84\r\n" + //
                                "  c86 := 1\r\n" + //
                                "  c87 := c85 IAND c86\r\n" + //
                                "  b49 := c87\r\n" + //
                                "  c88 := b44 IADD b83\r\n" + //
                                "  b48 := c88\r\n" + //
                                "  c89 := 1\r\n" + //
                                "  j6 := b49 EQ c89\r\n" + //
                                "  IF j6 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  j7 := 1\r\n" + //
                                "  j8 := b48 IADD j7\r\n" + //
                                "  b48 := j8\r\n" + //
                                "  j9 := 1\r\n" + //
                                "  k0 := b50 IRSHIFT j9\r\n" + //
                                "  b50 := k0\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  k1 := 255\r\n" + //
                                "  k2 := b48 IAND k1\r\n" + //
                                "  k3 := 23\r\n" + //
                                "  k4 := k2 ILSHIFT k3\r\n" + //
                                "  b40 := k4\r\n" + //
                                "  k5 := b40 IOR b50\r\n" + //
                                "  b40 := k5\r\n" + //
                                "  k6 := 31\r\n" + //
                                "  k7 := b51 ILSHIFT k6\r\n" + //
                                "  k8 := b40 IOR k7\r\n" + //
                                "  b40 := k8\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  k9 := b44 GT b45\r\n" + //
                                "  IF k9 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  l0 := b44 ISUB b45\r\n" + //
                                "  b43 := l0\r\n" + //
                                "  b45 := b44\r\n" + //
                                "  l1 := b44 IADD b83\r\n" + //
                                "  b48 := l1\r\n" + //
                                "  l2 := b47 IRSHIFT b43\r\n" + //
                                "  b47 := l2\r\n" + //
                                "  l3 := 1\r\n" + //
                                "  l4 := b41 EQ l3\r\n" + //
                                "  l5 := 0\r\n" + //
                                "  l6 := b42 EQ l5\r\n" + //
                                "  l7 := l4 LAND l6\r\n" + //
                                "  IF l7 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  l8 := b47 ISUB b46\r\n" + //
                                "  b50 := l8\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  l9 := b46 ISUB b47\r\n" + //
                                "  b50 := l9\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  m0 := 25\r\n" + //
                                "  m1 := b50 IRSHIFT m0\r\n" + //
                                "  m2 := 1\r\n" + //
                                "  m3 := m1 IAND m2\r\n" + //
                                "  b49 := m3\r\n" + //
                                "  m4 := 1\r\n" + //
                                "  m5 := b49 EQ m4\r\n" + //
                                "  IF m5 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  m6 := 1\r\n" + //
                                "  m7 := b48 IADD m6\r\n" + //
                                "  b48 := m7\r\n" + //
                                "  m8 := 1\r\n" + //
                                "  m9 := b50 IRSHIFT m8\r\n" + //
                                "  b50 := m9\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  n0 := 23\r\n" + //
                                "  n1 := b48 ILSHIFT n0\r\n" + //
                                "  b40 := n1\r\n" + //
                                "  n2 := 31\r\n" + //
                                "  n3 := b51 ILSHIFT n2\r\n" + //
                                "  n4 := b40 IOR n3\r\n" + //
                                "  b40 := n4\r\n" + //
                                "  n5 := b40 IOR b50\r\n" + //
                                "  b40 := n5\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  n6 := b45 ISUB b44\r\n" + //
                                "  b43 := n6\r\n" + //
                                "  b44 := b45\r\n" + //
                                "  n7 := b45 IADD b83\r\n" + //
                                "  b48 := n7\r\n" + //
                                "  n8 := b46 IRSHIFT b43\r\n" + //
                                "  b46 := n8\r\n" + //
                                "  n9 := 1\r\n" + //
                                "  o0 := b41 EQ n9\r\n" + //
                                "  o1 := 0\r\n" + //
                                "  o2 := b42 EQ o1\r\n" + //
                                "  o3 := o0 LAND o2\r\n" + //
                                "  IF o3 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  o4 := b47 ISUB b46\r\n" + //
                                "  b50 := o4\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  o5 := b46 ISUB b47\r\n" + //
                                "  b50 := o5\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  o6 := 25\r\n" + //
                                "  o7 := b50 IRSHIFT o6\r\n" + //
                                "  o8 := 1\r\n" + //
                                "  o9 := o7 IAND o8\r\n" + //
                                "  b49 := o9\r\n" + //
                                "  p0 := 1\r\n" + //
                                "  p1 := b49 EQ p0\r\n" + //
                                "  IF p1 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  p2 := 1\r\n" + //
                                "  p3 := b48 IADD p2\r\n" + //
                                "  b48 := p3\r\n" + //
                                "  p4 := 1\r\n" + //
                                "  p5 := b50 IRSHIFT p4\r\n" + //
                                "  b50 := p5\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  p6 := 255\r\n" + //
                                "  p7 := b48 IAND p6\r\n" + //
                                "  p8 := 23\r\n" + //
                                "  p9 := p7 ILSHIFT p8\r\n" + //
                                "  b40 := p9\r\n" + //
                                "  q0 := 31\r\n" + //
                                "  q1 := b51 ILSHIFT q0\r\n" + //
                                "  q2 := b40 IOR q1\r\n" + //
                                "  b40 := q2\r\n" + //
                                "  q3 := b40 IOR b50\r\n" + //
                                "  b40 := q3\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( b40 -] b26 )\r\n" + //
                                "  q4 [| b29\r\n" + //
                                "  b38 := q4\r\n" + //
                                "  b39 |[ b38\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  b54 [- b53\r\n" + //
                                "  b55 := 0\r\n" + //
                                "  b57 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b54 -] b59 )\r\n" + //
                                "  b58 [| b62\r\n" + //
                                "  b57 := b58\r\n" + //
                                "  b64 := 31\r\n" + //
                                "  b65 := b57 IRSHIFT b64\r\n" + //
                                "  b66 := 1\r\n" + //
                                "  b67 := b65 IAND b66\r\n" + //
                                "  b55 := b67\r\n" + //
                                "  b56 |[ b55\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  b60 [- b59\r\n" + //
                                "  b61 := 0\r\n" + //
                                "  b63 := 0.0\r\n" + //
                                "  IPARAM b63\r\n" + //
                                "  IPARAM b60\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b63\r\n" + //
                                "  IPARAM b61\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  b62 |[ b61\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  b71 [- b70\r\n" + //
                                "  b72 := 0\r\n" + //
                                "  b74 := 0\r\n" + //
                                "  b75 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b71 -] b59 )\r\n" + //
                                "  b76 [| b62\r\n" + //
                                "  b74 := b76\r\n" + //
                                "  b77 := 23\r\n" + //
                                "  b78 := b74 IRSHIFT b77\r\n" + //
                                "  b79 := 255\r\n" + //
                                "  b80 := b78 IAND b79\r\n" + //
                                "  b75 := b80\r\n" + //
                                "  b81 := b75 ISUB b83\r\n" + //
                                "  b72 := b81\r\n" + //
                                "  b73 |[ b72\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b87 [- b86\r\n" + //
                                "  b88 := 0\r\n" + //
                                "  b90 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b87 -] b59 )\r\n" + //
                                "  b91 [| b62\r\n" + //
                                "  b90 := b91\r\n" + //
                                "  b92 := 8388607\r\n" + //
                                "  b93 := b90 IAND b92\r\n" + //
                                "  b88 := b93\r\n" + //
                                "  b89 |[ b88\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  s1 [- c92\r\n" + //
                                "  s3 [- c93\r\n" + //
                                "  r0 := 0.0\r\n" + //
                                "  r1 := 0\r\n" + //
                                "  r2 := 0\r\n" + //
                                "  r3 := 0\r\n" + //
                                "  r4 := 0\r\n" + //
                                "  r5 := 0\r\n" + //
                                "  r6 := 0\r\n" + //
                                "  r7 := 0\r\n" + //
                                "  r8 := 0\r\n" + //
                                "  r9 := 0\r\n" + //
                                "  s0 := 0\r\n" + //
                                "  CALL RealSign ( s1 -] b53 )\r\n" + //
                                "  s2 [| b56\r\n" + //
                                "  r2 := s2\r\n" + //
                                "  CALL RealSign ( s3 -] b53 )\r\n" + //
                                "  s4 [| b56\r\n" + //
                                "  r3 := s4\r\n" + //
                                "  CALL RealExponent ( s1 -] b70 )\r\n" + //
                                "  s5 [| b73\r\n" + //
                                "  r4 := s5\r\n" + //
                                "  CALL RealExponent ( s3 -] b70 )\r\n" + //
                                "  s6 [| b73\r\n" + //
                                "  r5 := s6\r\n" + //
                                "  CALL RealMantissa ( s1 -] b86 )\r\n" + //
                                "  s7 [| b89\r\n" + //
                                "  r6 := s7\r\n" + //
                                "  CALL RealMantissa ( s3 -] b86 )\r\n" + //
                                "  s8 [| b89\r\n" + //
                                "  r7 := s8\r\n" + //
                                "  s9 := r2 NE r3\r\n" + //
                                "  IF s9 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  t0 := 1\r\n" + //
                                "  r8 := t0\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  t1 := 0\r\n" + //
                                "  r8 := t1\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0\r\n" + //
                                "  t2 := r6 IMUL r7\r\n" + //
                                "  r9 := t2\r\n" + //
                                "  t3 := r4 IADD r5\r\n" + //
                                "  t5 := t3 IADD b83\r\n" + //
                                "  s0 := t5\r\n" + //
                                "  t6 := 23\r\n" + //
                                "  t7 := s0 ILSHIFT t6\r\n" + //
                                "  r1 := t7\r\n" + //
                                "  t8 := 31\r\n" + //
                                "  t9 := r8 ILSHIFT t8\r\n" + //
                                "  u0 := r1 IOR t9\r\n" + //
                                "  r1 := u0\r\n" + //
                                "  u1 := r1 IOR r9\r\n" + //
                                "  r1 := u1\r\n" + //
                                "  CALL IntBinaryAsReal ( r1 -] b26 )\r\n" + //
                                "  u2 [| b29\r\n" + //
                                "  r0 := u2\r\n" + //
                                "  c94 |[ r0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL p\r\n" + //
                                "  c96 [- c95\r\n" + //
                                "  c98 [- c97\r\n" + //
                                "  c99 := 0\r\n" + //
                                "  CALL IntToReal ( c98 -] a26 )\r\n" + //
                                "  d11 [| a29\r\n" + //
                                "  CALL RAdd ( c96 -] b34 , d11 -] b36 )\r\n" + //
                                "  d12 [| b39\r\n" + //
                                "  CALL Round ( d12 -] d14 )\r\n" + //
                                "  d13 [| d17\r\n" + //
                                "  c99 := d13\r\n" + //
                                "  d10 |[ c99\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  d15 [- d14\r\n" + //
                                "  d18 := 0.5\r\n" + //
                                "  CALL RAdd ( d15 -] b34 , d18 -] b36 )\r\n" + //
                                "  d19 [| b39\r\n" + //
                                "  CALL Floor ( d19 -] d20 )\r\n" + //
                                "  d16 [| d22\r\n" + //
                                "  d17 |[ d16\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Floor\r\n" + //
                                "  d21 [- d20\r\n" + //
                                "  d22 |[ d21\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testExpressions(){
        String progSrc = "test_source/expressions.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " b55 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                " b := a\r\n" + //
                                " c := 1.2\r\n" + //
                                " d := c\r\n" + //
                                " e := 3.14\r\n" + //
                                " CALL RNeg ( e -] Y6 )\r\n" + //
                                " Y5 [| Y7\r\n" + //
                                " CALL IntToReal ( b -] Z9 )\r\n" + //
                                " Z8 [| a12\r\n" + //
                                " b54 := 127\r\n" + //
                                " b55 := b54\r\n" + //
                                " CALL RAdd ( Y5 -] b11 , Z8 -] b13 )\r\n" + //
                                " b10 [| b16\r\n" + //
                                " i := b10\r\n" + //
                                " j := 6\r\n" + //
                                " k := 6\r\n" + //
                                " l := 1\r\n" + //
                                " m := k IADD l\r\n" + //
                                " n := j IMUL m\r\n" + //
                                " o := n\r\n" + //
                                " c62 [| a12\r\n" + //
                                " CALL RMul ( c62 -] c64 , d -] c65 )\r\n" + //
                                " c63 [| c66\r\n" + //
                                " CALL IntToReal ( o -] Z9 )\r\n" + //
                                " c67 [| a12\r\n" + //
                                " CALL RDivide ( i -] c69 , c67 -] c70 )\r\n" + //
                                " c68 [| c71\r\n" + //
                                " CALL RNotEqualTo ( c63 -] c90 , c68 -] c91 )\r\n" + //
                                " c89 [| c92\r\n" + //
                                " u := c89\r\n" + //
                                " v := 0.0\r\n" + //
                                " w := 0.0\r\n" + //
                                " x := 0.0\r\n" + //
                                " y := 0\r\n" + //
                                " z := 0\r\n" + //
                                " A := 0\r\n" + //
                                " B := FALSE\r\n" + //
                                " C := FALSE\r\n" + //
                                " D := FALSE\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL RDivide ( d -] c69 , d -] c70 )\r\n" + //
                                " d13 [| c71\r\n" + //
                                " v := d13\r\n" + //
                                " F := 10\r\n" + //
                                " CALL Mod ( o -] d15 , F -] d17 )\r\n" + //
                                " d14 [| d20\r\n" + //
                                " y := d14\r\n" + //
                                " CALL RNeg ( i -] Y6 )\r\n" + //
                                " d29 [| Y7\r\n" + //
                                " CALL IntToReal ( b -] Z9 )\r\n" + //
                                " d30 [| a12\r\n" + //
                                " CALL RMul ( d -] c64 , d30 -] c65 )\r\n" + //
                                " d31 [| c66\r\n" + //
                                " CALL RSub ( d29 -] d33 , d31 -] d34 )\r\n" + //
                                " d32 [| d35\r\n" + //
                                " w := d32\r\n" + //
                                " CALL WriteInt ( b -] d36 )\r\n" + //
                                " CALL WriteReal ( v -] d38 )\r\n" + //
                                " CALL WriteReal ( v -] d38 )\r\n" + //
                                " CALL WriteReal ( w -] d38 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Div ( o -] c72 , y -] c74 )\r\n" + //
                                " d40 [| c77\r\n" + //
                                " z := d40\r\n" + //
                                " CALL Divide ( o -] d42 , y -] d44 )\r\n" + //
                                " d41 [| d47\r\n" + //
                                " x := d41\r\n" + //
                                " CALL WriteInt ( z -] d36 )\r\n" + //
                                " CALL WriteReal ( x -] d38 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Round ( i -] d52 )\r\n" + //
                                " d51 [| d55\r\n" + //
                                " x := d51\r\n" + //
                                " CALL WriteReal ( x -] d38 )\r\n" + //
                                " IF u EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_3 ELSE IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                " O := 2\r\n" + //
                                " P := 2\r\n" + //
                                " Q := O IMUL P\r\n" + //
                                " CALL WriteInt ( Q -] d36 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                " R := 10\r\n" + //
                                " CALL Divide ( o -] d42 , R -] d44 )\r\n" + //
                                " d61 [| d47\r\n" + //
                                " CALL WriteReal ( d61 -] d38 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0_2\r\n" + //
                                " LABEL IFEND_0_LEVEL_0_3\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " T := BNOT u\r\n" + //
                                " CALL IntToReal ( b -] Z9 )\r\n" + //
                                " d62 [| a12\r\n" + //
                                " CALL RGreaterThan ( v -] d64 , d62 -] d65 )\r\n" + //
                                " d63 [| d66\r\n" + //
                                " W := T LAND d63\r\n" + //
                                " B := W\r\n" + //
                                " X := BNOT u\r\n" + //
                                " CALL IntToReal ( b -] Z9 )\r\n" + //
                                " d99 [| a12\r\n" + //
                                " CALL RGreaterThanOrEqualTo ( v -] e11 , d99 -] e12 )\r\n" + //
                                " e10 [| e13\r\n" + //
                                " a0 := X LOR e10\r\n" + //
                                " C := a0\r\n" + //
                                " a1 := B EQ C\r\n" + //
                                " D := a1\r\n" + //
                                " IF B EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_2 ELSE IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                " LABEL IFSTAT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                " a2 := 4\r\n" + //
                                " CALL WriteInt ( a2 -] d36 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                " IF C EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                " a3 := 5\r\n" + //
                                " CALL WriteInt ( a3 -] d36 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL IFEND_1_LEVEL_0_2\r\n" + //
                                " IF D EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_2 ELSE IFNEXT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " LABEL IFSTAT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " a4 := 5\r\n" + //
                                " CALL WriteInt ( a4 -] d36 )\r\n" + //
                                " GOTO IFEND_2_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " a5 := 6\r\n" + //
                                " CALL WriteInt ( a5 -] d36 )\r\n" + //
                                " GOTO IFEND_2_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_2_SEQ_1_LEVEL_0_1\r\n" + //
                                " LABEL IFEND_2_LEVEL_0_2\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RNeg\r\n" + //
                                "  z9 [- Y6\r\n" + //
                                "  z2 := 0\r\n" + //
                                "  z3 := 0\r\n" + //
                                "  z4 := 0\r\n" + //
                                "  z5 := 0\r\n" + //
                                "  z6 := 1\r\n" + //
                                "  z7 := 31\r\n" + //
                                "  z8 := z6 ILSHIFT z7\r\n" + //
                                "  z2 := z8\r\n" + //
                                "  CALL RealBinaryAsInt ( z9 -] Y8 )\r\n" + //
                                "  A0 [| Z1\r\n" + //
                                "  z3 := A0\r\n" + //
                                "  A1 := z3 IXOR z2\r\n" + //
                                "  z5 := A1\r\n" + //
                                "  CALL IntBinaryAsReal ( z5 -] Z3 )\r\n" + //
                                "  A2 [| Z6\r\n" + //
                                "  z4 := A2\r\n" + //
                                "  Y7 |[ z4\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  Y9 [- Y8\r\n" + //
                                "  Z0 := 0\r\n" + //
                                "  Z2 := 0.0\r\n" + //
                                "  IPARAM Z2\r\n" + //
                                "  IPARAM Y9\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM Z2\r\n" + //
                                "  IPARAM Z0\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  Z1 |[ Z0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  Z4 [- Z3\r\n" + //
                                "  Z5 := 0.0\r\n" + //
                                "  Z7 := 0\r\n" + //
                                "  IPARAM Z7\r\n" + //
                                "  IPARAM Z4\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM Z7\r\n" + //
                                "  IPARAM Z5\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  Z6 |[ Z5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  a10 [- Z9\r\n" + //
                                "  a11 := 0.0\r\n" + //
                                "  a13 := FALSE\r\n" + //
                                "  a14 := 0\r\n" + //
                                "  a15 := 0\r\n" + //
                                "  a16 := 0\r\n" + //
                                "  a17 := 0\r\n" + //
                                "  a18 := 0\r\n" + //
                                "  a19 := 0\r\n" + //
                                "  a20 := 0\r\n" + //
                                "  a21 := 0\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  a23 := 0\r\n" + //
                                "  a17 := a10\r\n" + //
                                "  a24 := 0\r\n" + //
                                "  a16 := a24\r\n" + //
                                "  CALL IntIsNegative ( a10 -] a26 )\r\n" + //
                                "  a25 [| a29\r\n" + //
                                "  a13 := a25\r\n" + //
                                "  CALL IntIsZero ( a17 -] a40 )\r\n" + //
                                "  a39 [| a43\r\n" + //
                                "  a48 := BNOT a39\r\n" + //
                                "  IF a48 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a48 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a49 := 1\r\n" + //
                                "  a50 := a17 IAND a49\r\n" + //
                                "  a18 := a50\r\n" + //
                                "  a51 := 1\r\n" + //
                                "  a52 := a18 EQ a51\r\n" + //
                                "  IF a52 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a15 := a16\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  a53 := 1\r\n" + //
                                "  a54 := a16 IADD a53\r\n" + //
                                "  a16 := a54\r\n" + //
                                "  a55 := 1\r\n" + //
                                "  a56 := a17 IRSHIFT a55\r\n" + //
                                "  a17 := a56\r\n" + //
                                "  CALL IntIsZero ( a17 -] a40 )\r\n" + //
                                "  a57 [| a43\r\n" + //
                                "  a58 := BNOT a57\r\n" + //
                                "  a48 := a58\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a59 := 23\r\n" + //
                                "  a60 := a15 LT a59\r\n" + //
                                "  IF a60 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a61 := 23\r\n" + //
                                "  a62 := a61 ISUB a15\r\n" + //
                                "  a19 := a62\r\n" + //
                                "  a63 := INOT a19\r\n" + //
                                "  a64 := 1\r\n" + //
                                "  a65 := a63 IADD a64\r\n" + //
                                "  a20 := a65\r\n" + //
                                "  a66 := 2147483648\r\n" + //
                                "  a67 := INOT a66\r\n" + //
                                "  a68 := a20 IAND a67\r\n" + //
                                "  a20 := a68\r\n" + //
                                "  a69 := 64\r\n" + //
                                "  a70 := a20 IOR a69\r\n" + //
                                "  a20 := a70\r\n" + //
                                "  a71 := 1\r\n" + //
                                "  a72 := a71 ILSHIFT a15\r\n" + //
                                "  a73 := 1\r\n" + //
                                "  a74 := a72 ISUB a73\r\n" + //
                                "  a22 := a74\r\n" + //
                                "  a75 := a10 IAND a22\r\n" + //
                                "  a21 := a75\r\n" + //
                                "  a76 := a21 ILSHIFT a19\r\n" + //
                                "  a21 := a76\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a77 := 23\r\n" + //
                                "  a78 := a15 GT a77\r\n" + //
                                "  IF a78 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  a79 := 23\r\n" + //
                                "  a80 := a15 ISUB a79\r\n" + //
                                "  a19 := a80\r\n" + //
                                "  a20 := a19\r\n" + //
                                "  a81 := 1\r\n" + //
                                "  a82 := a81 ILSHIFT a15\r\n" + //
                                "  a83 := 1\r\n" + //
                                "  a84 := a82 ISUB a83\r\n" + //
                                "  a22 := a84\r\n" + //
                                "  a85 := a10 IAND a22\r\n" + //
                                "  a21 := a85\r\n" + //
                                "  a86 := a21 IRSHIFT a19\r\n" + //
                                "  a21 := a86\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                "  a20 := a15\r\n" + //
                                "  a87 := 1\r\n" + //
                                "  a88 := a87 ILSHIFT a15\r\n" + //
                                "  a89 := 1\r\n" + //
                                "  a90 := a88 ISUB a89\r\n" + //
                                "  a22 := a90\r\n" + //
                                "  a91 := a10 IAND a22\r\n" + //
                                "  a21 := a91\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  a14 := a21\r\n" + //
                                "  a92 := 23\r\n" + //
                                "  a93 := a20 ILSHIFT a92\r\n" + //
                                "  a94 := a14 IOR a93\r\n" + //
                                "  a14 := a94\r\n" + //
                                "  IF a13 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a95 := 1\r\n" + //
                                "  a96 := 31\r\n" + //
                                "  a97 := a95 ILSHIFT a96\r\n" + //
                                "  a98 := a14 IOR a97\r\n" + //
                                "  a14 := a98\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( a14 -] Z3 )\r\n" + //
                                "  a99 [| Z6\r\n" + //
                                "  a11 := a99\r\n" + //
                                "  a12 |[ a11\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  a27 [- a26\r\n" + //
                                "  a28 := FALSE\r\n" + //
                                "  a30 := 0\r\n" + //
                                "  a31 := 31\r\n" + //
                                "  a32 := a27 IRSHIFT a31\r\n" + //
                                "  a33 := 1\r\n" + //
                                "  a34 := a32 IAND a33\r\n" + //
                                "  a30 := a34\r\n" + //
                                "  a35 := 0\r\n" + //
                                "  a36 := a30 EQ a35\r\n" + //
                                "  IF a36 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a37 := FALSE\r\n" + //
                                "  a28 := a37\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a38 := TRUE\r\n" + //
                                "  a28 := a38\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  a29 |[ a28\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a41 [- a40\r\n" + //
                                "  a42 := FALSE\r\n" + //
                                "  a44 := 0\r\n" + //
                                "  a45 := a41 EQ a44\r\n" + //
                                "  IF a45 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a46 := TRUE\r\n" + //
                                "  a42 := a46\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a47 := FALSE\r\n" + //
                                "  a42 := a47\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  a43 |[ a42\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  b12 [- b11\r\n" + //
                                "  b14 [- b13\r\n" + //
                                "  b15 := 0.0\r\n" + //
                                "  b17 := 0\r\n" + //
                                "  b18 := 0\r\n" + //
                                "  b19 := 0\r\n" + //
                                "  b20 := 0\r\n" + //
                                "  b21 := 0\r\n" + //
                                "  b22 := 0\r\n" + //
                                "  b23 := 0\r\n" + //
                                "  b24 := 0\r\n" + //
                                "  b25 := 0\r\n" + //
                                "  b26 := 0\r\n" + //
                                "  b27 := 0\r\n" + //
                                "  b28 := 0\r\n" + //
                                "  CALL RealSign ( b12 -] b30 )\r\n" + //
                                "  b29 [| b33\r\n" + //
                                "  b18 := b29\r\n" + //
                                "  CALL RealSign ( b14 -] b30 )\r\n" + //
                                "  b40 [| b33\r\n" + //
                                "  b19 := b40\r\n" + //
                                "  CALL RealExponent ( b12 -] b42 )\r\n" + //
                                "  b41 [| b45\r\n" + //
                                "  b21 := b41\r\n" + //
                                "  CALL RealExponent ( b14 -] b42 )\r\n" + //
                                "  b56 [| b45\r\n" + //
                                "  b22 := b56\r\n" + //
                                "  CALL RealMantissa ( b12 -] b58 )\r\n" + //
                                "  b57 [| b61\r\n" + //
                                "  b23 := b57\r\n" + //
                                "  CALL RealMantissa ( b14 -] b58 )\r\n" + //
                                "\r\n" + //
                                "  b66 [| b61\r\n" + //
                                "  b24 := b66\r\n" + //
                                "  b67 := b18 EQ b19\r\n" + //
                                "  IF b67 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b28 := b18\r\n" + //
                                "  b68 := b21 EQ b22\r\n" + //
                                "  IF b68 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b69 := b23 IADD b24\r\n" + //
                                "  b27 := b69\r\n" + //
                                "  b70 := 25\r\n" + //
                                "  b71 := b27 IRSHIFT b70\r\n" + //
                                "  b72 := 1\r\n" + //
                                "  b73 := b71 IAND b72\r\n" + //
                                "  b26 := b73\r\n" + //
                                "  b74 := b21 IADD b55\r\n" + //
                                "  b25 := b74\r\n" + //
                                "  b75 := 1\r\n" + //
                                "  b76 := b26 EQ b75\r\n" + //
                                "  IF b76 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  b77 := 1\r\n" + //
                                "  b78 := b25 IADD b77\r\n" + //
                                "  b25 := b78\r\n" + //
                                "  b79 := 1\r\n" + //
                                "  b80 := b27 IRSHIFT b79\r\n" + //
                                "  b27 := b80\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  b81 := 255\r\n" + //
                                "  b82 := b25 IAND b81\r\n" + //
                                "  b83 := 23\r\n" + //
                                "  b84 := b82 ILSHIFT b83\r\n" + //
                                "  b17 := b84\r\n" + //
                                "  b85 := b17 IOR b27\r\n" + //
                                "  b17 := b85\r\n" + //
                                "  b86 := 31\r\n" + //
                                "  b87 := b28 ILSHIFT b86\r\n" + //
                                "  b88 := b17 IOR b87\r\n" + //
                                "  b17 := b88\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b89 := b21 GT b22\r\n" + //
                                "  IF b89 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b90 := b21 ISUB b22\r\n" + //
                                "  b20 := b90\r\n" + //
                                "  b22 := b21\r\n" + //
                                "  b91 := b21 IADD b55\r\n" + //
                                "  b25 := b91\r\n" + //
                                "  b92 := b24 IRSHIFT b20\r\n" + //
                                "  b24 := b92\r\n" + //
                                "  b93 := b23 IADD b24\r\n" + //
                                "  b27 := b93\r\n" + //
                                "  b94 := 25\r\n" + //
                                "  b95 := b27 IRSHIFT b94\r\n" + //
                                "  b96 := 1\r\n" + //
                                "  b97 := b95 IAND b96\r\n" + //
                                "  b26 := b97\r\n" + //
                                "  b98 := 1\r\n" + //
                                "  b99 := b26 EQ b98\r\n" + //
                                "  IF b99 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  c10 := 1\r\n" + //
                                "  c11 := b25 IADD c10\r\n" + //
                                "  b25 := c11\r\n" + //
                                "  c12 := 1\r\n" + //
                                "  c13 := b27 IRSHIFT c12\r\n" + //
                                "  b27 := c13\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  c14 := 255\r\n" + //
                                "  c15 := b25 IAND c14\r\n" + //
                                "  c16 := 23\r\n" + //
                                "  c17 := c15 ILSHIFT c16\r\n" + //
                                "  b17 := c17\r\n" + //
                                "  c18 := 31\r\n" + //
                                "  c19 := b28 ILSHIFT c18\r\n" + //
                                "  c20 := b17 IOR c19\r\n" + //
                                "  b17 := c20\r\n" + //
                                "  c21 := b17 IOR b27\r\n" + //
                                "  b17 := c21\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c22 := b22 ISUB b21\r\n" + //
                                "  b20 := c22\r\n" + //
                                "  b21 := b22\r\n" + //
                                "  c23 := b22 IADD b55\r\n" + //
                                "  b25 := c23\r\n" + //
                                "  c24 := b23 IRSHIFT b20\r\n" + //
                                "  b23 := c24\r\n" + //
                                "  c25 := b23 IADD b24\r\n" + //
                                "  b27 := c25\r\n" + //
                                "  c26 := 25\r\n" + //
                                "  c27 := b27 IRSHIFT c26\r\n" + //
                                "  c28 := 1\r\n" + //
                                "  c29 := c27 IAND c28\r\n" + //
                                "  b26 := c29\r\n" + //
                                "  c30 := 1\r\n" + //
                                "  c31 := b26 EQ c30\r\n" + //
                                "  IF c31 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  c32 := 1\r\n" + //
                                "  c33 := b25 IADD c32\r\n" + //
                                "  b25 := c33\r\n" + //
                                "  c34 := 1\r\n" + //
                                "  c35 := b27 IRSHIFT c34\r\n" + //
                                "  b27 := c35\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  c36 := 255\r\n" + //
                                "  c37 := b25 IAND c36\r\n" + //
                                "  c38 := 23\r\n" + //
                                "  c39 := c37 ILSHIFT c38\r\n" + //
                                "  b17 := c39\r\n" + //
                                "  c40 := 31\r\n" + //
                                "  c41 := b28 ILSHIFT c40\r\n" + //
                                "  c42 := b17 IOR c41\r\n" + //
                                "  b17 := c42\r\n" + //
                                "  c43 := b17 IOR b27\r\n" + //
                                "  b17 := c43\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  c44 := 0\r\n" + //
                                "  c45 := b18 EQ c44\r\n" + //
                                "  c46 := 1\r\n" + //
                                "  c47 := b19 EQ c46\r\n" + //
                                "  c48 := c45 LAND c47\r\n" + //
                                "  IF c48 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  c49 := b24 GT b23\r\n" + //
                                "  IF c49 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  h8 := 1\r\n" + //
                                "  b28 := h8\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  h9 := 0\r\n" + //
                                "  b28 := h9\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  i0 := 1\r\n" + //
                                "  i1 := b18 EQ i0\r\n" + //
                                "  i2 := 0\r\n" + //
                                "  i3 := b19 EQ i2\r\n" + //
                                "  c50 := i1 LAND i3\r\n" + //
                                "  IF c50 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  c51 := b24 GE b23\r\n" + //
                                "  IF c51 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  c52 := 0\r\n" + //
                                "  b28 := c52\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  c53 := 1\r\n" + //
                                "  b28 := c53\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  c54 := b21 EQ b22\r\n" + //
                                "  IF c54 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  c55 := 0\r\n" + //
                                "  b27 := c55\r\n" + //
                                "  c56 := 25\r\n" + //
                                "  c57 := b27 IRSHIFT c56\r\n" + //
                                "  c58 := 1\r\n" + //
                                "  c59 := c57 IAND c58\r\n" + //
                                "  b26 := c59\r\n" + //
                                "  c60 := b21 IADD b55\r\n" + //
                                "  b25 := c60\r\n" + //
                                "  c61 := 1\r\n" + //
                                "  j6 := b26 EQ c61\r\n" + //
                                "  IF j6 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  j7 := 1\r\n" + //
                                "  j8 := b25 IADD j7\r\n" + //
                                "  b25 := j8\r\n" + //
                                "  j9 := 1\r\n" + //
                                "  k0 := b27 IRSHIFT j9\r\n" + //
                                "  b27 := k0\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  k1 := 255\r\n" + //
                                "  k2 := b25 IAND k1\r\n" + //
                                "  k3 := 23\r\n" + //
                                "  k4 := k2 ILSHIFT k3\r\n" + //
                                "  b17 := k4\r\n" + //
                                "  k5 := b17 IOR b27\r\n" + //
                                "  b17 := k5\r\n" + //
                                "  k6 := 31\r\n" + //
                                "  k7 := b28 ILSHIFT k6\r\n" + //
                                "  k8 := b17 IOR k7\r\n" + //
                                "  b17 := k8\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  k9 := b21 GT b22\r\n" + //
                                "  IF k9 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  l0 := b21 ISUB b22\r\n" + //
                                "  b20 := l0\r\n" + //
                                "  b22 := b21\r\n" + //
                                "  l1 := b21 IADD b55\r\n" + //
                                "  b25 := l1\r\n" + //
                                "  l2 := b24 IRSHIFT b20\r\n" + //
                                "  b24 := l2\r\n" + //
                                "  l3 := 1\r\n" + //
                                "  l4 := b18 EQ l3\r\n" + //
                                "  l5 := 0\r\n" + //
                                "  l6 := b19 EQ l5\r\n" + //
                                "  l7 := l4 LAND l6\r\n" + //
                                "  IF l7 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  l8 := b24 ISUB b23\r\n" + //
                                "  b27 := l8\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  l9 := b23 ISUB b24\r\n" + //
                                "  b27 := l9\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  m0 := 25\r\n" + //
                                "  m1 := b27 IRSHIFT m0\r\n" + //
                                "  m2 := 1\r\n" + //
                                "  m3 := m1 IAND m2\r\n" + //
                                "  b26 := m3\r\n" + //
                                "  m4 := 1\r\n" + //
                                "  m5 := b26 EQ m4\r\n" + //
                                "  IF m5 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  m6 := 1\r\n" + //
                                "  m7 := b25 IADD m6\r\n" + //
                                "  b25 := m7\r\n" + //
                                "  m8 := 1\r\n" + //
                                "  m9 := b27 IRSHIFT m8\r\n" + //
                                "  b27 := m9\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  n0 := 23\r\n" + //
                                "  n1 := b25 ILSHIFT n0\r\n" + //
                                "  b17 := n1\r\n" + //
                                "  n2 := 31\r\n" + //
                                "  n3 := b28 ILSHIFT n2\r\n" + //
                                "  n4 := b17 IOR n3\r\n" + //
                                "  b17 := n4\r\n" + //
                                "  n5 := b17 IOR b27\r\n" + //
                                "  b17 := n5\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  n6 := b22 ISUB b21\r\n" + //
                                "  b20 := n6\r\n" + //
                                "  b21 := b22\r\n" + //
                                "  n7 := b22 IADD b55\r\n" + //
                                "  b25 := n7\r\n" + //
                                "  n8 := b23 IRSHIFT b20\r\n" + //
                                "  b23 := n8\r\n" + //
                                "  n9 := 1\r\n" + //
                                "  o0 := b18 EQ n9\r\n" + //
                                "  o1 := 0\r\n" + //
                                "  o2 := b19 EQ o1\r\n" + //
                                "  o3 := o0 LAND o2\r\n" + //
                                "  IF o3 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  o4 := b24 ISUB b23\r\n" + //
                                "  b27 := o4\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  o5 := b23 ISUB b24\r\n" + //
                                "  b27 := o5\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  o6 := 25\r\n" + //
                                "  o7 := b27 IRSHIFT o6\r\n" + //
                                "  o8 := 1\r\n" + //
                                "  o9 := o7 IAND o8\r\n" + //
                                "  b26 := o9\r\n" + //
                                "  p0 := 1\r\n" + //
                                "  p1 := b26 EQ p0\r\n" + //
                                "  IF p1 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  p2 := 1\r\n" + //
                                "  p3 := b25 IADD p2\r\n" + //
                                "  b25 := p3\r\n" + //
                                "  p4 := 1\r\n" + //
                                "  p5 := b27 IRSHIFT p4\r\n" + //
                                "  b27 := p5\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  p6 := 255\r\n" + //
                                "  p7 := b25 IAND p6\r\n" + //
                                "  p8 := 23\r\n" + //
                                "  p9 := p7 ILSHIFT p8\r\n" + //
                                "  b17 := p9\r\n" + //
                                "  q0 := 31\r\n" + //
                                "  q1 := b28 ILSHIFT q0\r\n" + //
                                "  q2 := b17 IOR q1\r\n" + //
                                "  b17 := q2\r\n" + //
                                "  q3 := b17 IOR b27\r\n" + //
                                "  b17 := q3\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( b17 -] Z3 )\r\n" + //
                                "  q4 [| Z6\r\n" + //
                                "  b15 := q4\r\n" + //
                                "  b16 |[ b15\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  b31 [- b30\r\n" + //
                                "  b32 := 0\r\n" + //
                                "  b34 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b31 -] Y8 )\r\n" + //
                                "  b35 [| Z1\r\n" + //
                                "  b34 := b35\r\n" + //
                                "  b36 := 31\r\n" + //
                                "  b37 := b34 IRSHIFT b36\r\n" + //
                                "  b38 := 1\r\n" + //
                                "  b39 := b37 IAND b38\r\n" + //
                                "  b32 := b39\r\n" + //
                                "  b33 |[ b32\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  b43 [- b42\r\n" + //
                                "  b44 := 0\r\n" + //
                                "  b46 := 0\r\n" + //
                                "  b47 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b43 -] Y8 )\r\n" + //
                                "  b48 [| Z1\r\n" + //
                                "  b46 := b48\r\n" + //
                                "  b49 := 23\r\n" + //
                                "  b50 := b46 IRSHIFT b49\r\n" + //
                                "  b51 := 255\r\n" + //
                                "  b52 := b50 IAND b51\r\n" + //
                                "  b47 := b52\r\n" + //
                                "  b53 := b47 ISUB b55\r\n" + //
                                "  b44 := b53\r\n" + //
                                "  b45 |[ b44\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b59 [- b58\r\n" + //
                                "  b60 := 0\r\n" + //
                                "  b62 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b59 -] Y8 )\r\n" + //
                                "  b63 [| Z1\r\n" + //
                                "  b62 := b63\r\n" + //
                                "  b64 := 8388607\r\n" + //
                                "  b65 := b62 IAND b64\r\n" + //
                                "  b60 := b65\r\n" + //
                                "  b61 |[ b60\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  s1 [- c64\r\n" + //
                                "  s3 [- c65\r\n" + //
                                "  r0 := 0.0\r\n" + //
                                "  r1 := 0\r\n" + //
                                "  r2 := 0\r\n" + //
                                "  r3 := 0\r\n" + //
                                "  r4 := 0\r\n" + //
                                "  r5 := 0\r\n" + //
                                "  r6 := 0\r\n" + //
                                "  r7 := 0\r\n" + //
                                "  r8 := 0\r\n" + //
                                "  r9 := 0\r\n" + //
                                "  s0 := 0\r\n" + //
                                "  CALL RealSign ( s1 -] b30 )\r\n" + //
                                "  s2 [| b33\r\n" + //
                                "  r2 := s2\r\n" + //
                                "  CALL RealSign ( s3 -] b30 )\r\n" + //
                                "  s4 [| b33\r\n" + //
                                "  r3 := s4\r\n" + //
                                "  CALL RealExponent ( s1 -] b42 )\r\n" + //
                                "  s5 [| b45\r\n" + //
                                "  r4 := s5\r\n" + //
                                "  CALL RealExponent ( s3 -] b42 )\r\n" + //
                                "  s6 [| b45\r\n" + //
                                "  r5 := s6\r\n" + //
                                "  CALL RealMantissa ( s1 -] b58 )\r\n" + //
                                "  s7 [| b61\r\n" + //
                                "  r6 := s7\r\n" + //
                                "  CALL RealMantissa ( s3 -] b58 )\r\n" + //
                                "  s8 [| b61\r\n" + //
                                "  r7 := s8\r\n" + //
                                "  s9 := r2 NE r3\r\n" + //
                                "  IF s9 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  t0 := 1\r\n" + //
                                "  r8 := t0\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  t1 := 0\r\n" + //
                                "  r8 := t1\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0\r\n" + //
                                "  t2 := r6 IMUL r7\r\n" + //
                                "  r9 := t2\r\n" + //
                                "  t3 := r4 IADD r5\r\n" + //
                                "  t5 := t3 IADD b55\r\n" + //
                                "  s0 := t5\r\n" + //
                                "  t6 := 23\r\n" + //
                                "  t7 := s0 ILSHIFT t6\r\n" + //
                                "  r1 := t7\r\n" + //
                                "  t8 := 31\r\n" + //
                                "  t9 := r8 ILSHIFT t8\r\n" + //
                                "  u0 := r1 IOR t9\r\n" + //
                                "  r1 := u0\r\n" + //
                                "  u1 := r1 IOR r9\r\n" + //
                                "  r1 := u1\r\n" + //
                                "  CALL IntBinaryAsReal ( r1 -] Z3 )\r\n" + //
                                "  u2 [| Z6\r\n" + //
                                "  r0 := u2\r\n" + //
                                "  c66 |[ r0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RDivide\r\n" + //
                                "  v4 [- c69\r\n" + //
                                "  v6 [- c70\r\n" + //
                                "  u3 := 0.0\r\n" + //
                                "  u4 := 0\r\n" + //
                                "  u5 := 0\r\n" + //
                                "  u6 := 0\r\n" + //
                                "  u7 := 0\r\n" + //
                                "  u8 := 0\r\n" + //
                                "  u9 := 0\r\n" + //
                                "  v0 := 0\r\n" + //
                                "  v1 := 0\r\n" + //
                                "  v2 := 0\r\n" + //
                                "  v3 := 0\r\n" + //
                                "  CALL RealSign ( v4 -] b30 )\r\n" + //
                                "  v5 [| b33\r\n" + //
                                "  u5 := v5\r\n" + //
                                "  CALL RealSign ( v6 -] b30 )\r\n" + //
                                "  v7 [| b33\r\n" + //
                                "  u6 := v7\r\n" + //
                                "  CALL RealExponent ( v4 -] b42 )\r\n" + //
                                "  v8 [| b45\r\n" + //
                                "  u7 := v8\r\n" + //
                                "  CALL RealExponent ( v6 -] b42 )\r\n" + //
                                "  v9 [| b45\r\n" + //
                                "  u8 := v9\r\n" + //
                                "  CALL RealMantissa ( v4 -] b58 )\r\n" + //
                                "  w0 [| b61\r\n" + //
                                "  u9 := w0\r\n" + //
                                "  CALL RealMantissa ( v6 -] b58 )\r\n" + //
                                "  w1 [| b61\r\n" + //
                                "  v0 := w1\r\n" + //
                                "  w2 := u5 NE u6\r\n" + //
                                "  IF w2 EQ TRUE THEN IFSTAT_27_SEQ_0_LEVEL_0 ELSE IFNEXT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  w3 := 1\r\n" + //
                                "  v1 := w3\r\n" + //
                                "  GOTO IFEND_27_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  w4 := 0\r\n" + //
                                "  v1 := w4\r\n" + //
                                "  GOTO IFEND_27_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_27_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_27_LEVEL_0\r\n" + //
                                "  CALL Div ( u9 -] c72 , v0 -] c74 )\r\n" + //
                                "  w5 [| c77\r\n" + //
                                "  v2 := w5\r\n" + //
                                "  w6 := u7 ISUB u8\r\n" + //
                                "  w8 := w6 IADD b55\r\n" + //
                                "  v3 := w8\r\n" + //
                                "  w9 := 255\r\n" + //
                                "  x0 := v3 IAND w9\r\n" + //
                                "  x1 := 23\r\n" + //
                                "  x2 := x0 ILSHIFT x1\r\n" + //
                                "  u4 := x2\r\n" + //
                                "  x3 := 31\r\n" + //
                                "  x4 := v1 ILSHIFT x3\r\n" + //
                                "  x5 := u4 IOR x4\r\n" + //
                                "  u4 := x5\r\n" + //
                                "  x6 := u4 IOR v2\r\n" + //
                                "  u4 := x6\r\n" + //
                                "  CALL IntBinaryAsReal ( u4 -] Z3 )\r\n" + //
                                "  x7 [| Z6\r\n" + //
                                "  u3 := x7\r\n" + //
                                "  c71 |[ u3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  c73 [- c72\r\n" + //
                                "  c75 [- c74\r\n" + //
                                "  c78 := 0\r\n" + //
                                "  c76 := 0\r\n" + //
                                "  c78 := c73\r\n" + //
                                "  c79 := 0\r\n" + //
                                "  c76 := c79\r\n" + //
                                "  c80 := c78 ISUB c75\r\n" + //
                                "  c81 := 0\r\n" + //
                                "  c82 := c80 GT c81\r\n" + //
                                "  IF c82 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF c82 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  c83 := c78 ISUB c75\r\n" + //
                                "  c78 := c83\r\n" + //
                                "  c84 := 1\r\n" + //
                                "  c85 := c76 IADD c84\r\n" + //
                                "  c76 := c85\r\n" + //
                                "  c86 := c78 ISUB c75\r\n" + //
                                "  c87 := 0\r\n" + //
                                "  c88 := c86 GT c87\r\n" + //
                                "  c82 := c88\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  c77 |[ c76\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RNotEqualTo\r\n" + //
                                "  X2 [- c90\r\n" + //
                                "  X4 [- c91\r\n" + //
                                "  W6 := FALSE\r\n" + //
                                "  W7 := FALSE\r\n" + //
                                "  W8 := FALSE\r\n" + //
                                "  W9 := 0\r\n" + //
                                "  X0 := 0\r\n" + //
                                "  X1 := 0\r\n" + //
                                "  CALL RealIsZero ( X2 -] c93 )\r\n" + //
                                "  X3 [| c96\r\n" + //
                                "  W7 := X3\r\n" + //
                                "  CALL RealIsZero ( X4 -] c93 )\r\n" + //
                                "  X5 [| c96\r\n" + //
                                "  W8 := X5\r\n" + //
                                "  X6 := W7 LAND W8\r\n" + //
                                "  IF X6 EQ TRUE THEN IFSTAT_75_SEQ_0_LEVEL_0 ELSE IFNEXT_75_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_75_SEQ_0_LEVEL_0\r\n" + //
                                "  X7 := FALSE\r\n" + //
                                "  W6 := X7\r\n" + //
                                "  GOTO IFEND_75_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_75_SEQ_0_LEVEL_0\r\n" + //
                                "  CALL RealBinaryAsInt ( X2 -] Y8 )\r\n" + //
                                "  X8 [| Z1\r\n" + //
                                "  X0 := X8\r\n" + //
                                "  CALL RealBinaryAsInt ( X4 -] Y8 )\r\n" + //
                                "  X9 [| Z1\r\n" + //
                                "  X1 := X9\r\n" + //
                                "  Y0 := X0 IXOR X1\r\n" + //
                                "  W9 := Y0\r\n" + //
                                "  Y1 := 0\r\n" + //
                                "  Y2 := W9 EQ Y1\r\n" + //
                                "  IF Y2 EQ TRUE THEN IFSTAT_76_SEQ_0_LEVEL_1 ELSE IFNEXT_76_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_76_SEQ_0_LEVEL_1\r\n" + //
                                "  Y3 := FALSE\r\n" + //
                                "  W6 := Y3\r\n" + //
                                "  GOTO IFEND_76_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_76_SEQ_0_LEVEL_1\r\n" + //
                                "  Y4 := TRUE\r\n" + //
                                "  W6 := Y4\r\n" + //
                                "  GOTO IFEND_76_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_76_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_76_LEVEL_1\r\n" + //
                                "  GOTO IFEND_75_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_75_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_75_LEVEL_0\r\n" + //
                                "  c92 |[ W6\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsZero\r\n" + //
                                "  c94 [- c93\r\n" + //
                                "  c95 := FALSE\r\n" + //
                                "  c97 := 0\r\n" + //
                                "  CALL RealMantissa ( c94 -] b58 )\r\n" + //
                                "  c98 [| b61\r\n" + //
                                "  c97 := c98\r\n" + //
                                "  c99 := 0\r\n" + //
                                "  d10 := c97 EQ c99\r\n" + //
                                "  IF d10 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_2 ELSE IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  d11 := TRUE\r\n" + //
                                "  c95 := d11\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  d12 := FALSE\r\n" + //
                                "  c95 := d12\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_2\r\n" + //
                                "  c96 |[ c95\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Mod\r\n" + //
                                "  d16 [- d15\r\n" + //
                                "  d18 [- d17\r\n" + //
                                "  d19 := 0\r\n" + //
                                "  d21 := 0\r\n" + //
                                "  d19 := d16\r\n" + //
                                "  d22 := d19 ISUB d18\r\n" + //
                                "  d23 := 0\r\n" + //
                                "  d24 := d22 GT d23\r\n" + //
                                "  IF d24 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF d24 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d25 := d19 ISUB d18\r\n" + //
                                "  d19 := d25\r\n" + //
                                "  d26 := d19 ISUB d18\r\n" + //
                                "  d27 := 0\r\n" + //
                                "  d28 := d26 GT d27\r\n" + //
                                "  d24 := d28\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  d20 |[ d19\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RSub\r\n" + //
                                "  q6 [- d33\r\n" + //
                                "  q7 [- d34\r\n" + //
                                "  q5 := 0.0\r\n" + //
                                "  CALL RNeg ( q7 -] Y6 )\r\n" + //
                                "  q8 [| Y7\r\n" + //
                                "  CALL RAdd ( q6 -] b11 , q8 -] b13 )\r\n" + //
                                "  q9 [| b16\r\n" + //
                                "  q5 := q9\r\n" + //
                                "  d35 |[ q5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  d37 [- d36\r\n" + //
                                "  IPARAM d37\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  d39 [- d38\r\n" + //
                                "  IPARAM d39\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Divide\r\n" + //
                                "  d43 [- d42\r\n" + //
                                "  d45 [- d44\r\n" + //
                                "  d48 := 0\r\n" + //
                                "  d46 := 0\r\n" + //
                                "  CALL Div ( d43 -] c72 , d45 -] c74 )\r\n" + //
                                "  d49 [| c77\r\n" + //
                                "  d48 := d49\r\n" + //
                                "  CALL IntToReal ( d48 -] Z9 )\r\n" + //
                                "  d50 [| a12\r\n" + //
                                "  d46 := d50\r\n" + //
                                "  d47 |[ d46\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  d53 [- d52\r\n" + //
                                "  d56 := 0.5\r\n" + //
                                "  CALL RAdd ( d53 -] b11 , d56 -] b13 )\r\n" + //
                                "  d57 [| b16\r\n" + //
                                "  CALL Floor ( d57 -] d58 )\r\n" + //
                                "  d54 [| d60\r\n" + //
                                "  d55 |[ d54\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Floor\r\n" + //
                                "  d59 [- d58\r\n" + //
                                "  d60 |[ d59\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThan\r\n" + //
                                "  L6 [- d64\r\n" + //
                                "  L8 [- d65\r\n" + //
                                "  K5 := FALSE\r\n" + //
                                "  K6 := FALSE\r\n" + //
                                "  K7 := FALSE\r\n" + //
                                "  K8 := FALSE\r\n" + //
                                "  K9 := FALSE\r\n" + //
                                "  L0 := FALSE\r\n" + //
                                "  L1 := FALSE\r\n" + //
                                "  L2 := 0\r\n" + //
                                "  L3 := 0\r\n" + //
                                "  L4 := 0\r\n" + //
                                "  L5 := 0\r\n" + //
                                "  CALL RealIsZero ( L6 -] c93 )\r\n" + //
                                "  L7 [| c96\r\n" + //
                                "  K6 := L7\r\n" + //
                                "  CALL RealIsZero ( L8 -] c93 )\r\n" + //
                                "  L9 [| c96\r\n" + //
                                "  K7 := L9\r\n" + //
                                "  CALL RealIsNegative ( L6 -] d67 )\r\n" + //
                                "  M0 [| d70\r\n" + //
                                "  K8 := M0\r\n" + //
                                "  CALL RealIsNegative ( L8 -] d67 )\r\n" + //
                                "  M1 [| d70\r\n" + //
                                "  K9 := M1\r\n" + //
                                "  CALL RealIsPositive ( L6 -] d77 )\r\n" + //
                                "  M2 [| d80\r\n" + //
                                "  L0 := M2\r\n" + //
                                "  CALL RealIsPositive ( L8 -] d77 )\r\n" + //
                                "  M3 [| d80\r\n" + //
                                "  L1 := M3\r\n" + //
                                "  M4 := K6 LAND K7\r\n" + //
                                "  IF M4 EQ TRUE THEN IFSTAT_50_SEQ_0_LEVEL_0 ELSE IFNEXT_50_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_0_LEVEL_0\r\n" + //
                                "  M5 := FALSE\r\n" + //
                                "  K5 := M5\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_0_LEVEL_0\r\n" + //
                                "  M6 := K8 LAND K7\r\n" + //
                                "  IF M6 EQ TRUE THEN IFSTAT_50_SEQ_1_LEVEL_0 ELSE IFNEXT_50_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_1_LEVEL_0\r\n" + //
                                "  M7 := TRUE\r\n" + //
                                "  K5 := M7\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_1_LEVEL_0\r\n" + //
                                "  M8 := K8 LAND L1\r\n" + //
                                "  IF M8 EQ TRUE THEN IFSTAT_50_SEQ_2_LEVEL_0 ELSE IFNEXT_50_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_2_LEVEL_0\r\n" + //
                                "  M9 := TRUE\r\n" + //
                                "  K5 := M9\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_2_LEVEL_0\r\n" + //
                                "  N0 := K6 LAND L1\r\n" + //
                                "  IF N0 EQ TRUE THEN IFSTAT_50_SEQ_3_LEVEL_0 ELSE IFNEXT_50_SEQ_3_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_3_LEVEL_0\r\n" + //
                                "  N1 := TRUE\r\n" + //
                                "  K5 := N1\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_3_LEVEL_0\r\n" + //
                                "  N2 := K8 LAND K9\r\n" + //
                                "  IF N2 EQ TRUE THEN IFSTAT_50_SEQ_4_LEVEL_0 ELSE IFNEXT_50_SEQ_4_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_4_LEVEL_0\r\n" + //
                                "  CALL RealScore ( L6 -] d86 )\r\n" + //
                                "  N3 [| d87\r\n" + //
                                "  L2 := N3\r\n" + //
                                "  CALL RealScore ( L8 -] d86 )\r\n" + //
                                "  N4 [| d87\r\n" + //
                                "  L3 := N4\r\n" + //
                                "  N5 := L2 LT L3\r\n" + //
                                "  IF N5 EQ TRUE THEN IFSTAT_51_SEQ_0_LEVEL_1 ELSE IFNEXT_51_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_51_SEQ_0_LEVEL_1\r\n" + //
                                "  N6 := TRUE\r\n" + //
                                "  K5 := N6\r\n" + //
                                "  GOTO IFEND_51_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_51_SEQ_0_LEVEL_1\r\n" + //
                                "  N7 := L2 EQ L3\r\n" + //
                                "  IF N7 EQ TRUE THEN IFSTAT_51_SEQ_1_LEVEL_1 ELSE IFNEXT_51_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_51_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( L6 -] b58 )\r\n" + //
                                "  N8 [| b61\r\n" + //
                                "  L4 := N8\r\n" + //
                                "  CALL RealMantissa ( L8 -] b58 )\r\n" + //
                                "  N9 [| b61\r\n" + //
                                "  L5 := N9\r\n" + //
                                "  O0 := L4 LT L5\r\n" + //
                                "  IF O0 EQ TRUE THEN IFSTAT_52_SEQ_0_LEVEL_2 ELSE IFNEXT_52_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_52_SEQ_0_LEVEL_2\r\n" + //
                                "  O1 := TRUE\r\n" + //
                                "  K5 := O1\r\n" + //
                                "  GOTO IFEND_52_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_52_SEQ_0_LEVEL_2\r\n" + //
                                "  O2 := FALSE\r\n" + //
                                "  K5 := O2\r\n" + //
                                "  GOTO IFEND_52_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_52_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_52_LEVEL_2\r\n" + //
                                "  GOTO IFEND_51_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_51_SEQ_1_LEVEL_1\r\n" + //
                                "  O3 := FALSE\r\n" + //
                                "  K5 := O3\r\n" + //
                                "  GOTO IFEND_51_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_51_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_51_LEVEL_1\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_4_LEVEL_0\r\n" + //
                                "  O4 := L0 LAND L1\r\n" + //
                                "  IF O4 EQ TRUE THEN IFSTAT_50_SEQ_5_LEVEL_0 ELSE IFNEXT_50_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_5_LEVEL_0\r\n" + //
                                "  CALL RealScore ( L6 -] d86 )\r\n" + //
                                "  O5 [| d87\r\n" + //
                                "  L2 := O5\r\n" + //
                                "  CALL RealScore ( L8 -] d86 )\r\n" + //
                                "  O6 [| d87\r\n" + //
                                "  L2 := O6\r\n" + //
                                "  O7 := L2 GT L3\r\n" + //
                                "  IF O7 EQ TRUE THEN IFSTAT_56_SEQ_0_LEVEL_1 ELSE IFNEXT_56_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_56_SEQ_0_LEVEL_1\r\n" + //
                                "  O8 := TRUE\r\n" + //
                                "  K5 := O8\r\n" + //
                                "  GOTO IFEND_56_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_0_LEVEL_1\r\n" + //
                                "  O9 := L2 EQ L3\r\n" + //
                                "  IF O9 EQ TRUE THEN IFSTAT_56_SEQ_1_LEVEL_1 ELSE IFNEXT_56_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_56_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( L6 -] b58 )\r\n" + //
                                "  P0 [| b61\r\n" + //
                                "  L4 := P0\r\n" + //
                                "  CALL RealMantissa ( L8 -] b58 )\r\n" + //
                                "  P1 [| b61\r\n" + //
                                "  L5 := P1\r\n" + //
                                "  P2 := L4 GT L5\r\n" + //
                                "  IF P2 EQ TRUE THEN IFSTAT_57_SEQ_0_LEVEL_2 ELSE IFNEXT_57_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_57_SEQ_0_LEVEL_2\r\n" + //
                                "  P3 := TRUE\r\n" + //
                                "  K5 := P3\r\n" + //
                                "  GOTO IFEND_57_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_57_SEQ_0_LEVEL_2\r\n" + //
                                "  P4 := FALSE\r\n" + //
                                "  K5 := P4\r\n" + //
                                "  GOTO IFEND_57_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_57_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_57_LEVEL_2\r\n" + //
                                "  GOTO IFEND_56_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_1_LEVEL_1\r\n" + //
                                "  P5 := FALSE\r\n" + //
                                "  K5 := P5\r\n" + //
                                "  GOTO IFEND_56_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_56_LEVEL_1\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFEND_50_LEVEL_0\r\n" + //
                                "  d66 |[ K5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsNegative\r\n" + //
                                "  d68 [- d67\r\n" + //
                                "  d69 := FALSE\r\n" + //
                                "  d71 := 0\r\n" + //
                                "  CALL RealSign ( d68 -] b30 )\r\n" + //
                                "  d72 [| b33\r\n" + //
                                "  d71 := d72\r\n" + //
                                "  d73 := 0\r\n" + //
                                "  d74 := d71 EQ d73\r\n" + //
                                "  IF d74 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d75 := FALSE\r\n" + //
                                "  d69 := d75\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d76 := TRUE\r\n" + //
                                "  d69 := d76\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_3_LEVEL_0_0\r\n" + //
                                "  d70 |[ d69\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsPositive\r\n" + //
                                "  d78 [- d77\r\n" + //
                                "  d79 := FALSE\r\n" + //
                                "  d81 := 0\r\n" + //
                                "  d82 := 0\r\n" + //
                                "  CALL RealSign ( d78 -] b30 )\r\n" + //
                                "  d83 [| b33\r\n" + //
                                "  d82 := d83\r\n" + //
                                "  d84 := 0\r\n" + //
                                "  d85 := d82 EQ d84\r\n" + //
                                "  IF d85 EQ TRUE THEN IFSTAT_5_SEQ_0_LEVEL_0_0 ELSE IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c1 := TRUE\r\n" + //
                                "  d79 := c1\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c2 := FALSE\r\n" + //
                                "  d79 := c2\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_5_LEVEL_0_0\r\n" + //
                                "  d80 |[ d79\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealScore\r\n" + //
                                "  d0 [- d86\r\n" + //
                                "  c3 := 0\r\n" + //
                                "  c4 := 0\r\n" + //
                                "  c5 := 0\r\n" + //
                                "  c6 := 0\r\n" + //
                                "  c7 := 0\r\n" + //
                                "  c8 := 0\r\n" + //
                                "  c9 := 0\r\n" + //
                                "  CALL RealExponent ( d0 -] b42 )\r\n" + //
                                "  d1 [| b45\r\n" + //
                                "  c4 := d1\r\n" + //
                                "  CALL RealMantissa ( d0 -] b58 )\r\n" + //
                                "  d2 [| b61\r\n" + //
                                "  c5 := d2\r\n" + //
                                "  d3 := 0\r\n" + //
                                "  d4 := c5 EQ d3\r\n" + //
                                "  IF d4 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_0_0 ELSE IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c3 := c5\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d5 := 0\r\n" + //
                                "  c8 := d5\r\n" + //
                                "  d6 := 0\r\n" + //
                                "  d7 := c5 NE d6\r\n" + //
                                "  IF d7 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  IF d7 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILEEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  d8 := 1\r\n" + //
                                "  d9 := c5 IAND d8\r\n" + //
                                "  c9 := d9\r\n" + //
                                "  d88 := 1\r\n" + //
                                "  d89 := c9 EQ d88\r\n" + //
                                "  IF d89 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_1 ELSE IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  c6 := c8\r\n" + //
                                "  GOTO IFEND_7_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFEND_7_LEVEL_1\r\n" + //
                                "  d90 := 1\r\n" + //
                                "  d91 := c5 IRSHIFT d90\r\n" + //
                                "  c5 := d91\r\n" + //
                                "  d92 := 1\r\n" + //
                                "  d93 := c8 IADD d92\r\n" + //
                                "  c8 := d93\r\n" + //
                                "  d94 := 0\r\n" + //
                                "  d95 := c5 NE d94\r\n" + //
                                "  d7 := d95\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_2\r\n" + //
                                "  d96 := 23\r\n" + //
                                "  d97 := d96 ISUB c6\r\n" + //
                                "  d98 := d97 IADD c4\r\n" + //
                                "  c3 := d98\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_6_LEVEL_0_0\r\n" + //
                                "  d87 |[ c3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThanOrEqualTo\r\n" + //
                                "  Q7 [- e11\r\n" + //
                                "  Q9 [- e12\r\n" + //
                                "  P6 := FALSE\r\n" + //
                                "  P7 := FALSE\r\n" + //
                                "  P8 := FALSE\r\n" + //
                                "  P9 := FALSE\r\n" + //
                                "  Q0 := FALSE\r\n" + //
                                "  Q1 := FALSE\r\n" + //
                                "  Q2 := FALSE\r\n" + //
                                "  Q3 := 0\r\n" + //
                                "  Q4 := 0\r\n" + //
                                "  Q5 := 0\r\n" + //
                                "  Q6 := 0\r\n" + //
                                "  CALL RealIsZero ( Q7 -] c93 )\r\n" + //
                                "  Q8 [| c96\r\n" + //
                                "  P7 := Q8\r\n" + //
                                "  CALL RealIsZero ( Q9 -] c93 )\r\n" + //
                                "  R0 [| c96\r\n" + //
                                "  P8 := R0\r\n" + //
                                "  CALL RealIsNegative ( Q7 -] d67 )\r\n" + //
                                "  R1 [| d70\r\n" + //
                                "  P9 := R1\r\n" + //
                                "  CALL RealIsNegative ( Q9 -] d67 )\r\n" + //
                                "  R2 [| d70\r\n" + //
                                "  Q0 := R2\r\n" + //
                                "  CALL RealIsPositive ( Q7 -] d77 )\r\n" + //
                                "  R3 [| d80\r\n" + //
                                "  Q1 := R3\r\n" + //
                                "  CALL RealIsPositive ( Q9 -] d77 )\r\n" + //
                                "  R4 [| d80\r\n" + //
                                "  Q2 := R4\r\n" + //
                                "  R5 := P7 LAND P8\r\n" + //
                                "  IF R5 EQ TRUE THEN IFSTAT_61_SEQ_0_LEVEL_0 ELSE IFNEXT_61_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_0_LEVEL_0\r\n" + //
                                "  R6 := FALSE\r\n" + //
                                "  P6 := R6\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_0_LEVEL_0\r\n" + //
                                "  R7 := P9 LAND P8\r\n" + //
                                "  IF R7 EQ TRUE THEN IFSTAT_61_SEQ_1_LEVEL_0 ELSE IFNEXT_61_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_1_LEVEL_0\r\n" + //
                                "  R8 := TRUE\r\n" + //
                                "  P6 := R8\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_1_LEVEL_0\r\n" + //
                                "  R9 := P9 LAND Q2\r\n" + //
                                "  IF R9 EQ TRUE THEN IFSTAT_61_SEQ_2_LEVEL_0 ELSE IFNEXT_61_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_2_LEVEL_0\r\n" + //
                                "  S0 := TRUE\r\n" + //
                                "  P6 := S0\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_2_LEVEL_0\r\n" + //
                                "  S1 := P7 LAND Q2\r\n" + //
                                "  IF S1 EQ TRUE THEN IFSTAT_61_SEQ_3_LEVEL_0 ELSE IFNEXT_61_SEQ_3_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_3_LEVEL_0\r\n" + //
                                "  S2 := TRUE\r\n" + //
                                "  P6 := S2\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_3_LEVEL_0\r\n" + //
                                "  S3 := P9 LAND Q0\r\n" + //
                                "  IF S3 EQ TRUE THEN IFSTAT_61_SEQ_4_LEVEL_0 ELSE IFNEXT_61_SEQ_4_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_4_LEVEL_0\r\n" + //
                                "  CALL RealScore ( Q7 -] d86 )\r\n" + //
                                "  S4 [| d87\r\n" + //
                                "  Q3 := S4\r\n" + //
                                "  CALL RealScore ( Q9 -] d86 )\r\n" + //
                                "  S5 [| d87\r\n" + //
                                "  Q4 := S5\r\n" + //
                                "  S6 := Q3 LT Q4\r\n" + //
                                "  IF S6 EQ TRUE THEN IFSTAT_62_SEQ_0_LEVEL_1 ELSE IFNEXT_62_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_62_SEQ_0_LEVEL_1\r\n" + //
                                "  S7 := TRUE\r\n" + //
                                "  P6 := S7\r\n" + //
                                "  GOTO IFEND_62_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_62_SEQ_0_LEVEL_1\r\n" + //
                                "  S8 := Q3 EQ Q4\r\n" + //
                                "  IF S8 EQ TRUE THEN IFSTAT_62_SEQ_1_LEVEL_1 ELSE IFNEXT_62_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_62_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( Q7 -] b58 )\r\n" + //
                                "  S9 [| b61\r\n" + //
                                "  Q5 := S9\r\n" + //
                                "  CALL RealMantissa ( Q9 -] b58 )\r\n" + //
                                "  T0 [| b61\r\n" + //
                                "  Q6 := T0\r\n" + //
                                "  T1 := Q5 LE Q6\r\n" + //
                                "  IF T1 EQ TRUE THEN IFSTAT_63_SEQ_0_LEVEL_2 ELSE IFNEXT_63_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_63_SEQ_0_LEVEL_2\r\n" + //
                                "  T2 := TRUE\r\n" + //
                                "  P6 := T2\r\n" + //
                                "  GOTO IFEND_63_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_63_SEQ_0_LEVEL_2\r\n" + //
                                "  T3 := FALSE\r\n" + //
                                "  P6 := T3\r\n" + //
                                "  GOTO IFEND_63_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_63_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_63_LEVEL_2\r\n" + //
                                "  GOTO IFEND_62_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_62_SEQ_1_LEVEL_1\r\n" + //
                                "  T4 := FALSE\r\n" + //
                                "  P6 := T4\r\n" + //
                                "  GOTO IFEND_62_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_62_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_62_LEVEL_1\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_4_LEVEL_0\r\n" + //
                                "  T5 := Q1 LAND Q2\r\n" + //
                                "  IF T5 EQ TRUE THEN IFSTAT_61_SEQ_5_LEVEL_0 ELSE IFNEXT_61_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_5_LEVEL_0\r\n" + //
                                "  CALL RealScore ( Q7 -] d86 )\r\n" + //
                                "  T6 [| d87\r\n" + //
                                "  Q3 := T6\r\n" + //
                                "  CALL RealScore ( Q9 -] d86 )\r\n" + //
                                "  T7 [| d87\r\n" + //
                                "  Q3 := T7\r\n" + //
                                "  T8 := Q3 GT Q4\r\n" + //
                                "  IF T8 EQ TRUE THEN IFSTAT_67_SEQ_0_LEVEL_1 ELSE IFNEXT_67_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_67_SEQ_0_LEVEL_1\r\n" + //
                                "  T9 := TRUE\r\n" + //
                                "  P6 := T9\r\n" + //
                                "  GOTO IFEND_67_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_67_SEQ_0_LEVEL_1\r\n" + //
                                "  U0 := Q3 EQ Q4\r\n" + //
                                "  IF U0 EQ TRUE THEN IFSTAT_67_SEQ_1_LEVEL_1 ELSE IFNEXT_67_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_67_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( Q7 -] b58 )\r\n" + //
                                "  U1 [| b61\r\n" + //
                                "  Q5 := U1\r\n" + //
                                "  CALL RealMantissa ( Q9 -] b58 )\r\n" + //
                                "  U2 [| b61\r\n" + //
                                "  Q6 := U2\r\n" + //
                                "  U3 := Q5 GE Q6\r\n" + //
                                "  IF U3 EQ TRUE THEN IFSTAT_68_SEQ_0_LEVEL_2 ELSE IFNEXT_68_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_68_SEQ_0_LEVEL_2\r\n" + //
                                "  U4 := TRUE\r\n" + //
                                "  P6 := U4\r\n" + //
                                "  GOTO IFEND_68_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_68_SEQ_0_LEVEL_2\r\n" + //
                                "  U5 := FALSE\r\n" + //
                                "  P6 := U5\r\n" + //
                                "  GOTO IFEND_68_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_68_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_68_LEVEL_2\r\n" + //
                                "  GOTO IFEND_67_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_67_SEQ_1_LEVEL_1\r\n" + //
                                "  U6 := FALSE\r\n" + //
                                "  P6 := U6\r\n" + //
                                "  GOTO IFEND_67_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_67_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_67_LEVEL_1\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFEND_61_LEVEL_0\r\n" + //
                                "  e13 |[ P6\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testForLoopAdvanced(){
        String progSrc = "test_source/ForLoopAdvanced.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                " b := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " c := 1\r\n" + //
                                " a := c\r\n" + //
                                " d := 10\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF a NE d THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " e := 1\r\n" + //
                                " b := e\r\n" + //
                                " f := 10\r\n" + //
                                " LABEL FORBEG_1_LEVEL_1\r\n" + //
                                " IF b LT f THEN FORLOOP_1_LEVEL_1 ELSE FOREND_1_LEVEL_1\r\n" + //
                                " LABEL FORLOOP_1_LEVEL_1\r\n" + //
                                " CALL WriteInt ( b -> Y5 )\r\n" + //
                                " g := 1\r\n" + //
                                " h := b IADD g\r\n" + //
                                " b := h\r\n" + //
                                " GOTO FORBEG_1_LEVEL_1\r\n" + //
                                " LABEL FOREND_1_LEVEL_1\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " i := 10\r\n" + //
                                " b := i\r\n" + //
                                " j := 0\r\n" + //
                                " LABEL FORBEG_2_LEVEL_1\r\n" + //
                                " IF b GT j THEN FORLOOP_2_LEVEL_1 ELSE FOREND_2_LEVEL_1\r\n" + //
                                " LABEL FORLOOP_2_LEVEL_1\r\n" + //
                                " CALL WriteInt ( b -> Y5 )\r\n" + //
                                " k := 1\r\n" + //
                                " CALL INeg ( k -> Y8 )\r\n" + //
                                " Y7 <| Z1\r\n" + //
                                " m := b IADD Y7\r\n" + //
                                " b := m\r\n" + //
                                " GOTO FORBEG_2_LEVEL_1\r\n" + //
                                " LABEL FOREND_2_LEVEL_1\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " n := 1\r\n" + //
                                " o := a IADD n\r\n" + //
                                " a := o\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  Y6 <- Y5\r\n" + //
                                "  IPARAM Y6\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  Y9 <- Y8\r\n" + //
                                "  Z0 := 0\r\n" + //
                                "  Z2 := INOT Y9\r\n" + //
                                "  Z3 := 1\r\n" + //
                                "  Z4 := Z2 IADD Z3\r\n" + //
                                "  Z0 := Z4\r\n" + //
                                "  Z1 |< Z0\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testForLoopBasic(){
        String progSrc = "test_source/ForLoopBasic.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " b := 1\r\n" + //
                                " a := b\r\n" + //
                                " c := 10\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF a NE c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " d := 1\r\n" + //
                                " e := a IADD d\r\n" + //
                                " a := e\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testFoorLoopBasic2(){
        String progSrc = "test_source/ForLoopBasic2.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " b := 1\r\n" + //
                                " a := b\r\n" + //
                                " c := 10\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF a LT c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " d := 1\r\n" + //
                                " e := a IADD d\r\n" + //
                                " a := e\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testForLoopBasic3(){
        String progSrc = "test_source/ForLoopBasic3.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " b := 10\r\n" + //
                                " a := b\r\n" + //
                                " c := 1\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF a GT c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " d := 1\r\n" + //
                                " CALL INeg ( d -> W3 )\r\n" + //
                                " W2 <| W6\r\n" + //
                                " f := a IADD W2\r\n" + //
                                " a := f\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  W4 <- W3\r\n" + //
                                "  W5 := 0\r\n" + //
                                "  W7 := INOT W4\r\n" + //
                                "  W8 := 1\r\n" + //
                                "  W9 := W7 IADD W8\r\n" + //
                                "  W5 := W9\r\n" + //
                                "  W6 |< W5\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testIfStatementAdvanced(){
        String progSrc = "test_source/IfStatementAdvanced.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := TRUE\r\n" + //
                                " b := a\r\n" + //
                                " c := FALSE\r\n" + //
                                " d := c\r\n" + //
                                "CODE SECTION\r\n" + //
                                " IF b EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF d EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1_0 ELSE IFNEXT_1_SEQ_0_LEVEL_1_0\r\n" + //
                                " LABEL IFSTAT_1_SEQ_0_LEVEL_1_0\r\n" + //
                                " e := 5\r\n" + //
                                " CALL WriteInt ( e -> W0 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_1_0\r\n" + //
                                " LABEL IFNEXT_1_SEQ_0_LEVEL_1_0\r\n" + //
                                " f := 6\r\n" + //
                                " CALL WriteInt ( f -> W0 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_1_0\r\n" + //
                                " LABEL IFNEXT_1_SEQ_1_LEVEL_1_0\r\n" + //
                                " LABEL IFEND_1_LEVEL_1_0\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF d EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_1 ELSE IFNEXT_3_SEQ_0_LEVEL_1\r\n" + //
                                " LABEL IFSTAT_3_SEQ_0_LEVEL_1\r\n" + //
                                " g := 7\r\n" + //
                                " CALL WriteInt ( g -> W0 )\r\n" + //
                                " GOTO IFEND_3_LEVEL_1\r\n" + //
                                " LABEL IFNEXT_3_SEQ_0_LEVEL_1\r\n" + //
                                " h := 8\r\n" + //
                                " CALL WriteInt ( h -> W0 )\r\n" + //
                                " GOTO IFEND_3_LEVEL_1\r\n" + //
                                " LABEL IFNEXT_3_SEQ_1_LEVEL_1\r\n" + //
                                " LABEL IFEND_3_LEVEL_1\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                " LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testIfStatementBasic(){
        String progSrc = "test_source/IfStatementBasic.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := TRUE\r\n" + //
                                " b := a\r\n" + //
                                " c := FALSE\r\n" + //
                                " d := c\r\n" + //
                                "CODE SECTION\r\n" + //
                                " IF b EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " e := 4\r\n" + //
                                " CALL WriteInt ( e -> W0 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " f := 5\r\n" + //
                                " CALL WriteInt ( f -> W0 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                " LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                " IF d EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                " g := 2\r\n" + //
                                " CALL WriteInt ( g -> W0 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF b EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                " LABEL IFSTAT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                " h := 5\r\n" + //
                                " CALL WriteInt ( h -> W0 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                " i := 6\r\n" + //
                                " CALL WriteInt ( i -> W0 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_1_SEQ_2_LEVEL_0_0\r\n" + //
                                " LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testLoops(){
        String progSrc = "test_source/loops.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " b := 1\r\n" + //
                                " a := b\r\n" + //
                                " c := 10\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF a LT c THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " d := 1\r\n" + //
                                " e := a IADD d\r\n" + //
                                " a := e\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " f := 1\r\n" + //
                                " a := f\r\n" + //
                                " g := 10\r\n" + //
                                " LABEL FORBEG_1_LEVEL_0\r\n" + //
                                " IF a LT g THEN FORLOOP_1_LEVEL_0 ELSE FOREND_1_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_1_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " h := 2\r\n" + //
                                " i := a IADD h\r\n" + //
                                " a := i\r\n" + //
                                " GOTO FORBEG_1_LEVEL_0\r\n" + //
                                " LABEL FOREND_1_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " j := 10\r\n" + //
                                " a := j\r\n" + //
                                " k := 1\r\n" + //
                                " LABEL FORBEG_2_LEVEL_0\r\n" + //
                                " IF a GT k THEN FORLOOP_2_LEVEL_0 ELSE FOREND_2_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_2_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " l := 2\r\n" + //
                                " CALL INeg ( l -> W3 )\r\n" + //
                                " W2 <| W6\r\n" + //
                                " n := a IADD W2\r\n" + //
                                " a := n\r\n" + //
                                " GOTO FORBEG_2_LEVEL_0\r\n" + //
                                " LABEL FOREND_2_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " o := 10\r\n" + //
                                " a := o\r\n" + //
                                " p := 1\r\n" + //
                                " LABEL FORBEG_3_LEVEL_0\r\n" + //
                                " IF a GT p THEN FORLOOP_3_LEVEL_0 ELSE FOREND_3_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_3_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " q := 1\r\n" + //
                                " CALL INeg ( q -> W3 )\r\n" + //
                                " X0 <| W6\r\n" + //
                                " s := a IADD X0\r\n" + //
                                " a := s\r\n" + //
                                " GOTO FORBEG_3_LEVEL_0\r\n" + //
                                " LABEL FOREND_3_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " t := 1\r\n" + //
                                " a := t\r\n" + //
                                " u := 10\r\n" + //
                                " v := a LE u\r\n" + //
                                " IF v EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF v EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " w := 1\r\n" + //
                                " x := a IADD w\r\n" + //
                                " a := x\r\n" + //
                                " y := 10\r\n" + //
                                " z := a LE y\r\n" + //
                                " v := z\r\n" + //
                                " GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " A := 1\r\n" + //
                                " a := A\r\n" + //
                                " B := 10\r\n" + //
                                " C := a LE B\r\n" + //
                                " D := 2\r\n" + //
                                " CALL Mod ( a -> X2 , D -> X4 )\r\n" + //
                                " X1 <| X7\r\n" + //
                                " F := 1\r\n" + //
                                " G := X1 EQ F\r\n" + //
                                " H := C LAND G\r\n" + //
                                " IF H EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                " LABEL WHILECOND_2_SEQ_0_LEVEL_0_1\r\n" + //
                                " IF H EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_1 ELSE WHILEEND_2_LEVEL_0_1\r\n" + //
                                " LABEL WHILESTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " I := 1\r\n" + //
                                " J := a IADD I\r\n" + //
                                " a := J\r\n" + //
                                " K := 10\r\n" + //
                                " L := a LE K\r\n" + //
                                " M := 2\r\n" + //
                                " CALL Mod ( a -> X2 , M -> X4 )\r\n" + //
                                " Y6 <| X7\r\n" + //
                                " O := 1\r\n" + //
                                " P := Y6 EQ O\r\n" + //
                                " Q := L LAND P\r\n" + //
                                " H := Q\r\n" + //
                                " GOTO WHILECOND_2_SEQ_0_LEVEL_0_1\r\n" + //
                                " LABEL WHILENEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                " R := 10\r\n" + //
                                " S := a LE R\r\n" + //
                                " IF S EQ TRUE THEN WHILESTAT_2_SEQ_1_LEVEL_0 ELSE WHILENEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILECOND_2_SEQ_1_LEVEL_0\r\n" + //
                                " IF S EQ TRUE THEN WHILESTAT_2_SEQ_1_LEVEL_0 ELSE WHILEEND_2_LEVEL_0_1\r\n" + //
                                " LABEL WHILESTAT_2_SEQ_1_LEVEL_0\r\n" + //
                                " T := 1\r\n" + //
                                " U := a IADD T\r\n" + //
                                " a := U\r\n" + //
                                " V := 10\r\n" + //
                                " W := a LE V\r\n" + //
                                " S := W\r\n" + //
                                " GOTO WHILECOND_2_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILENEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILEEND_2_LEVEL_0_1\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " X := 10\r\n" + //
                                " a := X\r\n" + //
                                " Y := 1\r\n" + //
                                " Z := a LT Y\r\n" + //
                                " LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                                " IF Z EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                                " LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " a0 := 2\r\n" + //
                                " a1 := a ISUB a0\r\n" + //
                                " a := a1\r\n" + //
                                " a2 := 1\r\n" + //
                                " a3 := a LT a2\r\n" + //
                                " Z := a3\r\n" + //
                                " GOTO REPEATBEG_0_LEVEL_0\r\n" + //
                                " LABEL REPEATEND_0_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " a4 := 10\r\n" + //
                                " a := a4\r\n" + //
                                " a5 := 1\r\n" + //
                                " a6 := a GE a5\r\n" + //
                                " LABEL REPEATBEG_1_LEVEL_0\r\n" + //
                                " IF a6 EQ TRUE THEN REPEATEND_1_LEVEL_0 ELSE REPEATLOOP_1_LEVEL_0\r\n" + //
                                " LABEL REPEATLOOP_1_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " a7 := 1\r\n" + //
                                " a8 := a IADD a7\r\n" + //
                                " a := a8\r\n" + //
                                " a9 := 1\r\n" + //
                                " b0 := a GE a9\r\n" + //
                                " a6 := b0\r\n" + //
                                " GOTO REPEATBEG_1_LEVEL_0\r\n" + //
                                " LABEL REPEATEND_1_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  W4 <- W3\r\n" + //
                                "  W5 := 0\r\n" + //
                                "  W7 := INOT W4\r\n" + //
                                "  W8 := 1\r\n" + //
                                "  W9 := W7 IADD W8\r\n" + //
                                "  W5 := W9\r\n" + //
                                "  W6 |< W5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Mod\r\n" + //
                                "  X3 <- X2\r\n" + //
                                "  X5 <- X4\r\n" + //
                                "  X6 := 0\r\n" + //
                                "  X8 := 0\r\n" + //
                                "  X6 := X3\r\n" + //
                                "  X9 := X6 ISUB X5\r\n" + //
                                "  Y0 := 0\r\n" + //
                                "  Y1 := X9 GT Y0\r\n" + //
                                "  IF Y1 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF Y1 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Y2 := X6 ISUB X5\r\n" + //
                                "  X6 := Y2\r\n" + //
                                "  Y3 := X6 ISUB X5\r\n" + //
                                "  Y4 := 0\r\n" + //
                                "  Y5 := Y3 GT Y4\r\n" + //
                                "  Y1 := Y5\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  X7 |< X6\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRepeatLoop(){
        String progSrc = "test_source/RepeatLoopBasic.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " b := 1\r\n" + //
                                " a := b\r\n" + //
                                " c := 10\r\n" + //
                                " d := a GE c\r\n" + //
                                " LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                                " IF d EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                                " LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( a -> W0 )\r\n" + //
                                " e := 1\r\n" + //
                                " f := a IADD e\r\n" + //
                                " a := f\r\n" + //
                                " g := 10\r\n" + //
                                " h := a GE g\r\n" + //
                                " d := h\r\n" + //
                                " GOTO REPEATBEG_0_LEVEL_0\r\n" + //
                                " LABEL REPEATEND_0_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testSample(){
        String progSrc = "test_source/Sample.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 6\r\n" + //
                                " b := a\r\n" + //
                                " c := 7\r\n" + //
                                " d := c\r\n" + //
                                " e := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL gcd ( b -> W0 , d -> W2 )\r\n" + //
                                " i <| W4\r\n" + //
                                " e := i\r\n" + //
                                " j := b IMUL d\r\n" + //
                                " k := j IMUL e\r\n" + //
                                " e := k\r\n" + //
                                " l := 1.0\r\n" + //
                                " CALL IntToReal ( e -> X1 )\r\n" + //
                                " X0 <| X4\r\n" + //
                                " CALL RMul ( X0 -> a78 , l -> a79 )\r\n" + //
                                " a77 <| a80\r\n" + //
                                " CALL WriteReal ( a77 -> b24 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL gcd\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  W3 <- W2\r\n" + //
                                "  W5 := W1 NE W3\r\n" + //
                                "  IF W5 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF W5 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  W6 := W1 GT W3\r\n" + //
                                "  IF W6 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  W7 := W1 ISUB W3\r\n" + //
                                "  W1 := W7\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  W8 := W3 ISUB W1\r\n" + //
                                "  W3 := W8\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  W9 := W1 NE W3\r\n" + //
                                "  W5 := W9\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  W4 |< W1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  X2 <- X1\r\n" + //
                                "  X3 := 0.0\r\n" + //
                                "  X5 := FALSE\r\n" + //
                                "  X6 := 0\r\n" + //
                                "  X7 := 0\r\n" + //
                                "  X8 := 0\r\n" + //
                                "  X9 := 0\r\n" + //
                                "  Y0 := 0\r\n" + //
                                "  Y1 := 0\r\n" + //
                                "  Y2 := 0\r\n" + //
                                "  Y3 := 0\r\n" + //
                                "  Y4 := 0\r\n" + //
                                "  Y5 := 0\r\n" + //
                                "  X9 := X2\r\n" + //
                                "  Y6 := 0\r\n" + //
                                "  X8 := Y6\r\n" + //
                                "  CALL IntIsNegative ( X2 -> Y8 )\r\n" + //
                                "  Y7 <| Z1\r\n" + //
                                "  X5 := Y7\r\n" + //
                                "  CALL IntIsZero ( X9 -> a12 )\r\n" + //
                                "  a11 <| a15\r\n" + //
                                "  a20 := BNOT a11\r\n" + //
                                "  IF a20 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF a20 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a21 := 1\r\n" + //
                                "  a22 := X9 IAND a21\r\n" + //
                                "  Y0 := a22\r\n" + //
                                "  a23 := 1\r\n" + //
                                "  a24 := Y0 EQ a23\r\n" + //
                                "  IF a24 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  X7 := X8\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  a25 := 1\r\n" + //
                                "  a26 := X8 IADD a25\r\n" + //
                                "  X8 := a26\r\n" + //
                                "  a27 := 1\r\n" + //
                                "  a28 := X9 IRSHIFT a27\r\n" + //
                                "  X9 := a28\r\n" + //
                                "  CALL IntIsZero ( X9 -> a12 )\r\n" + //
                                "  a29 <| a15\r\n" + //
                                "  a30 := BNOT a29\r\n" + //
                                "  a20 := a30\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  a31 := 23\r\n" + //
                                "  a32 := X7 LT a31\r\n" + //
                                "  IF a32 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a33 := 23\r\n" + //
                                "  a34 := a33 ISUB X7\r\n" + //
                                "  Y1 := a34\r\n" + //
                                "  a35 := INOT Y1\r\n" + //
                                "  a36 := 1\r\n" + //
                                "  a37 := a35 IADD a36\r\n" + //
                                "  Y2 := a37\r\n" + //
                                "  a38 := 2147483648\r\n" + //
                                "  a39 := INOT a38\r\n" + //
                                "  a40 := Y2 IAND a39\r\n" + //
                                "  Y2 := a40\r\n" + //
                                "  a41 := 64\r\n" + //
                                "  a42 := Y2 IOR a41\r\n" + //
                                "  Y2 := a42\r\n" + //
                                "  a43 := 1\r\n" + //
                                "  a44 := a43 ILSHIFT X7\r\n" + //
                                "  a45 := 1\r\n" + //
                                "  a46 := a44 ISUB a45\r\n" + //
                                "  Y4 := a46\r\n" + //
                                "  a47 := X2 IAND Y4\r\n" + //
                                "  Y3 := a47\r\n" + //
                                "  a48 := Y3 ILSHIFT Y1\r\n" + //
                                "  Y3 := a48\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a49 := 23\r\n" + //
                                "  a50 := X7 GT a49\r\n" + //
                                "  IF a50 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a51 := 23\r\n" + //
                                "  a52 := X7 ISUB a51\r\n" + //
                                "  Y1 := a52\r\n" + //
                                "  Y2 := Y1\r\n" + //
                                "  a53 := 1\r\n" + //
                                "  a54 := a53 ILSHIFT X7\r\n" + //
                                "  a55 := 1\r\n" + //
                                "  a56 := a54 ISUB a55\r\n" + //
                                "  Y4 := a56\r\n" + //
                                "  a57 := X2 IAND Y4\r\n" + //
                                "  Y3 := a57\r\n" + //
                                "  a58 := Y3 IRSHIFT Y1\r\n" + //
                                "  Y3 := a58\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  Y2 := X7\r\n" + //
                                "  a59 := 1\r\n" + //
                                "  a60 := a59 ILSHIFT X7\r\n" + //
                                "  a61 := 1\r\n" + //
                                "  a62 := a60 ISUB a61\r\n" + //
                                "  Y4 := a62\r\n" + //
                                "  a63 := X2 IAND Y4\r\n" + //
                                "  Y3 := a63\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  X6 := Y3\r\n" + //
                                "  a64 := 23\r\n" + //
                                "  a65 := Y2 ILSHIFT a64\r\n" + //
                                "  a66 := X6 IOR a65\r\n" + //
                                "  X6 := a66\r\n" + //
                                "  IF X5 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a67 := 1\r\n" + //
                                "  a68 := 31\r\n" + //
                                "  a69 := a67 ILSHIFT a68\r\n" + //
                                "  a70 := X6 IOR a69\r\n" + //
                                "  X6 := a70\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( X6 -> a71 )\r\n" + //
                                "  a76 <| a74\r\n" + //
                                "  X3 := a76\r\n" + //
                                "  X4 |< X3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  Y9 <- Y8\r\n" + //
                                "  Z0 := FALSE\r\n" + //
                                "  Z2 := 0\r\n" + //
                                "  Z3 := 31\r\n" + //
                                "  Z4 := Y9 IRSHIFT Z3\r\n" + //
                                "  Z5 := 1\r\n" + //
                                "  Z6 := Z4 IAND Z5\r\n" + //
                                "  Z2 := Z6\r\n" + //
                                "  Z7 := 0\r\n" + //
                                "  Z8 := Z2 EQ Z7\r\n" + //
                                "  IF Z8 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z9 := FALSE\r\n" + //
                                "  Z0 := Z9\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a10 := TRUE\r\n" + //
                                "  Z0 := a10\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  Z1 |< Z0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a13 <- a12\r\n" + //
                                "  a14 := FALSE\r\n" + //
                                "  a16 := 0\r\n" + //
                                "  a17 := a13 EQ a16\r\n" + //
                                "  IF a17 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a18 := TRUE\r\n" + //
                                "  a14 := a18\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a19 := FALSE\r\n" + //
                                "  a14 := a19\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  a15 |< a14\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a72 <- a71\r\n" + //
                                "  a73 := 0.0\r\n" + //
                                "  a75 := 0\r\n" + //
                                "  IPARAM a75\r\n" + //
                                "  IPARAM a72\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a75\r\n" + //
                                "  IPARAM a73\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a74 |< a73\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  q4 <- a78\r\n" + //
                                "  q6 <- a79\r\n" + //
                                "  p3 := 0.0\r\n" + //
                                "  p4 := 0\r\n" + //
                                "  p5 := 0\r\n" + //
                                "  p6 := 0\r\n" + //
                                "  p7 := 0\r\n" + //
                                "  p8 := 0\r\n" + //
                                "  p9 := 0\r\n" + //
                                "  q0 := 0\r\n" + //
                                "  q1 := 0\r\n" + //
                                "  q2 := 0\r\n" + //
                                "  q3 := 0\r\n" + //
                                "  CALL RealSign ( q4 -> a81 )\r\n" + //
                                "  q5 <| a84\r\n" + //
                                "  p5 := q5\r\n" + //
                                "  CALL RealSign ( q6 -> a81 )\r\n" + //
                                "  q7 <| a84\r\n" + //
                                "  p6 := q7\r\n" + //
                                "  CALL RealExponent ( q4 -> a96 )\r\n" + //
                                "  q8 <| a99\r\n" + //
                                "  p7 := q8\r\n" + //
                                "  CALL RealExponent ( q6 -> a96 )\r\n" + //
                                "  q9 <| a99\r\n" + //
                                "  p8 := q9\r\n" + //
                                "  CALL RealMantissa ( q4 -> b16 )\r\n" + //
                                "  r0 <| b19\r\n" + //
                                "  p9 := r0\r\n" + //
                                "  CALL RealMantissa ( q6 -> b16 )\r\n" + //
                                "  r1 <| b19\r\n" + //
                                "  q0 := r1\r\n" + //
                                "  r2 := p5 NE p6\r\n" + //
                                "  IF r2 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  r3 := 1\r\n" + //
                                "  q1 := r3\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  r4 := 0\r\n" + //
                                "  q1 := r4\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0\r\n" + //
                                "  r5 := p9 IMUL q0\r\n" + //
                                "  q2 := r5\r\n" + //
                                "  r6 := p7 IADD p8\r\n" + //
                                "  q3 := r6\r\n" + //
                                "  r7 := 23\r\n" + //
                                "  r8 := q3 ILSHIFT r7\r\n" + //
                                "  p4 := r8\r\n" + //
                                "  r9 := 31\r\n" + //
                                "  s0 := q1 ILSHIFT r9\r\n" + //
                                "  s1 := p4 IOR s0\r\n" + //
                                "  p4 := s1\r\n" + //
                                "  s2 := p4 IOR q2\r\n" + //
                                "  p4 := s2\r\n" + //
                                "  CALL IntBinaryAsReal ( p4 -> a71 )\r\n" + //
                                "  s3 <| a74\r\n" + //
                                "  p3 := s3\r\n" + //
                                "  a80 |< p3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a82 <- a81\r\n" + //
                                "  a83 := 0\r\n" + //
                                "  a85 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a82 -> a87 )\r\n" + //
                                "  a86 <| a90\r\n" + //
                                "  a85 := a86\r\n" + //
                                "  a92 := 31\r\n" + //
                                "  a93 := a85 IRSHIFT a92\r\n" + //
                                "  a94 := 1\r\n" + //
                                "  a95 := a93 IAND a94\r\n" + //
                                "  a83 := a95\r\n" + //
                                "  a84 |< a83\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  a88 <- a87\r\n" + //
                                "  a89 := 0\r\n" + //
                                "  a91 := 0.0\r\n" + //
                                "  IPARAM a91\r\n" + //
                                "  IPARAM a88\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a91\r\n" + //
                                "  IPARAM a89\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a90 |< a89\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  a97 <- a96\r\n" + //
                                "  a98 := 0\r\n" + //
                                "  b10 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a97 -> a87 )\r\n" + //
                                "  b11 <| a90\r\n" + //
                                "  b10 := b11\r\n" + //
                                "  b12 := 23\r\n" + //
                                "  b13 := b10 IRSHIFT b12\r\n" + //
                                "  b14 := 255\r\n" + //
                                "  b15 := b13 IAND b14\r\n" + //
                                "  a98 := b15\r\n" + //
                                "  a99 |< a98\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b17 <- b16\r\n" + //
                                "  b18 := 0\r\n" + //
                                "  b20 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b17 -> a87 )\r\n" + //
                                "  b21 <| a90\r\n" + //
                                "  b20 := b21\r\n" + //
                                "  b22 := 8388607\r\n" + //
                                "  b23 := b20 IAND b22\r\n" + //
                                "  b18 := b23\r\n" + //
                                "  b19 |< b18\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  b25 <- b24\r\n" + //
                                "  IPARAM b25\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testTest(){
        String progSrc = "test_source/test.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 42\r\n" + //
                                " CALL INeg ( a -> W1 )\r\n" + //
                                " W0 <| W4\r\n" + //
                                " c := W0\r\n" + //
                                " d := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " d := c\r\n" + //
                                " g := 0\r\n" + //
                                " h := d LT g\r\n" + //
                                " IF h EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " CALL INeg ( d -> W1 )\r\n" + //
                                " W8 <| W4\r\n" + //
                                " d := W8\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " j := 0\r\n" + //
                                " k := d GT j\r\n" + //
                                " IF k EQ TRUE THEN IFSTAT_0_SEQ_1_LEVEL_0 ELSE IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                " LABEL IFSTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                " CALL Display ( d -> W9 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                " LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  W2 <- W1\r\n" + //
                                "  W3 := 0\r\n" + //
                                "  W5 := INOT W2\r\n" + //
                                "  W6 := 1\r\n" + //
                                "  W7 := W5 IADD W6\r\n" + //
                                "  W3 := W7\r\n" + //
                                "  W4 |< W3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Display\r\n" + //
                                "  X0 <- W9\r\n" + //
                                "  X1 := FALSE\r\n" + //
                                "  X2 := 2\r\n" + //
                                "  CALL Mod ( X0 -> X4 , X2 -> X6 )\r\n" + //
                                "  X3 <| X9\r\n" + //
                                "  Y8 := 0\r\n" + //
                                "  Y9 := X3 NE Y8\r\n" + //
                                "  X1 := Y9\r\n" + //
                                "  IF X1 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z0 := 1\r\n" + //
                                "  CALL WriteInt ( Z0 -> Z1 )\r\n" + //
                                "  Z3 := 1\r\n" + //
                                "  Z4 := X0 ISUB Z3\r\n" + //
                                "  Z5 := 2\r\n" + //
                                "  CALL Div ( Z4 -> Z7 , Z5 -> Z9 )\r\n" + //
                                "  Z6 <| a12\r\n" + //
                                "  X0 := Z6\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a24 := 0\r\n" + //
                                "  a25 := X0 EQ a24\r\n" + //
                                "  IF a25 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  a26 := 1\r\n" + //
                                "  CALL INeg ( a26 -> W1 )\r\n" + //
                                "  a27 <| W4\r\n" + //
                                "  CALL WriteInt ( a27 -> Z1 )\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  a28 := 0\r\n" + //
                                "  CALL WriteInt ( a28 -> Z1 )\r\n" + //
                                "  a29 := 2\r\n" + //
                                "  CALL Div ( X0 -> Z7 , a29 -> Z9 )\r\n" + //
                                "  a30 <| a12\r\n" + //
                                "  X0 := a30\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Mod\r\n" + //
                                "  X5 <- X4\r\n" + //
                                "  X7 <- X6\r\n" + //
                                "  X8 := 0\r\n" + //
                                "  Y0 := 0\r\n" + //
                                "  X8 := X5\r\n" + //
                                "  Y1 := X8 ISUB X7\r\n" + //
                                "  Y2 := 0\r\n" + //
                                "  Y3 := Y1 GT Y2\r\n" + //
                                "  IF Y3 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF Y3 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Y4 := X8 ISUB X7\r\n" + //
                                "  X8 := Y4\r\n" + //
                                "  Y5 := X8 ISUB X7\r\n" + //
                                "  Y6 := 0\r\n" + //
                                "  Y7 := Y5 GT Y6\r\n" + //
                                "  Y3 := Y7\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  X9 |< X8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  Z2 <- Z1\r\n" + //
                                "  IPARAM Z2\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  Z8 <- Z7\r\n" + //
                                "  a10 <- Z9\r\n" + //
                                "  a13 := 0\r\n" + //
                                "  a11 := 0\r\n" + //
                                "  a13 := Z8\r\n" + //
                                "  a14 := 0\r\n" + //
                                "  a11 := a14\r\n" + //
                                "  a15 := a13 ISUB a10\r\n" + //
                                "  a16 := 0\r\n" + //
                                "  a17 := a15 GT a16\r\n" + //
                                "  IF a17 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a17 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a18 := a13 ISUB a10\r\n" + //
                                "  a13 := a18\r\n" + //
                                "  a19 := 1\r\n" + //
                                "  a20 := a11 IADD a19\r\n" + //
                                "  a11 := a20\r\n" + //
                                "  a21 := a13 ISUB a10\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  a23 := a21 GT a22\r\n" + //
                                "  a17 := a23\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a12 |< a11\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testTest2(){
        String progSrc = "test_source/test2.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 42\r\n" + //
                                " CALL INeg ( a -> W1 )\r\n" + //
                                " W0 <| W4\r\n" + //
                                " c := W0\r\n" + //
                                " d := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " d := c\r\n" + //
                                " g := 0\r\n" + //
                                " h := d LT g\r\n" + //
                                " IF h EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF h EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " CALL INeg ( d -> W1 )\r\n" + //
                                " W8 <| W4\r\n" + //
                                " d := W8\r\n" + //
                                " j := 0\r\n" + //
                                " k := d LT j\r\n" + //
                                " h := k\r\n" + //
                                " GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " l := 0\r\n" + //
                                " m := d GT l\r\n" + //
                                " IF m EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                " IF m EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILESTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                " CALL Display ( d -> W9 )\r\n" + //
                                " n := 0\r\n" + //
                                " o := d GT n\r\n" + //
                                " m := o\r\n" + //
                                " GOTO WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                " p := 10\r\n" + //
                                " d := p\r\n" + //
                                " q := 2\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF d GT q THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " CALL WriteInt ( d -> Z1 )\r\n" + //
                                " r := 1\r\n" + //
                                " CALL INeg ( r -> W1 )\r\n" + //
                                " a31 <| W4\r\n" + //
                                " t := d IADD a31\r\n" + //
                                " d := t\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                " u := TRUE\r\n" + //
                                " LABEL REPEATBEG_0_LEVEL_0\r\n" + //
                                " IF u EQ TRUE THEN REPEATEND_0_LEVEL_0 ELSE REPEATLOOP_0_LEVEL_0\r\n" + //
                                " LABEL REPEATLOOP_0_LEVEL_0\r\n" + //
                                " CALL Display ( d -> W9 )\r\n" + //
                                " v := TRUE\r\n" + //
                                " u := v\r\n" + //
                                " GOTO REPEATBEG_0_LEVEL_0\r\n" + //
                                " LABEL REPEATEND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  W2 <- W1\r\n" + //
                                "  W3 := 0\r\n" + //
                                "  W5 := INOT W2\r\n" + //
                                "  W6 := 1\r\n" + //
                                "  W7 := W5 IADD W6\r\n" + //
                                "  W3 := W7\r\n" + //
                                "  W4 |< W3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Display\r\n" + //
                                "  X0 <- W9\r\n" + //
                                "  X1 := FALSE\r\n" + //
                                "  X2 := 2\r\n" + //
                                "  CALL Mod ( X0 -> X4 , X2 -> X6 )\r\n" + //
                                "  X3 <| X9\r\n" + //
                                "  Y8 := 0\r\n" + //
                                "  Y9 := X3 NE Y8\r\n" + //
                                "  X1 := Y9\r\n" + //
                                "  IF X1 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z0 := 1\r\n" + //
                                "  CALL WriteInt ( Z0 -> Z1 )\r\n" + //
                                "  Z3 := 1\r\n" + //
                                "  Z4 := X0 ISUB Z3\r\n" + //
                                "  Z5 := 2\r\n" + //
                                "  CALL Div ( Z4 -> Z7 , Z5 -> Z9 )\r\n" + //
                                "  Z6 <| a12\r\n" + //
                                "  X0 := Z6\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a24 := 0\r\n" + //
                                "  a25 := X0 EQ a24\r\n" + //
                                "  IF a25 EQ TRUE THEN IFSTAT_0_SEQ_1_LEVEL_0 ELSE IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "  a26 := 1\r\n" + //
                                "  CALL INeg ( a26 -> W1 )\r\n" + //
                                "  a27 <| W4\r\n" + //
                                "  CALL WriteInt ( a27 -> Z1 )\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  a28 := 0\r\n" + //
                                "  CALL WriteInt ( a28 -> Z1 )\r\n" + //
                                "  a29 := 2\r\n" + //
                                "  CALL Div ( X0 -> Z7 , a29 -> Z9 )\r\n" + //
                                "  a30 <| a12\r\n" + //
                                "  X0 := a30\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Mod\r\n" + //
                                "  X5 <- X4\r\n" + //
                                "  X7 <- X6\r\n" + //
                                "  X8 := 0\r\n" + //
                                "  Y0 := 0\r\n" + //
                                "  X8 := X5\r\n" + //
                                "  Y1 := X8 ISUB X7\r\n" + //
                                "  Y2 := 0\r\n" + //
                                "  Y3 := Y1 GT Y2\r\n" + //
                                "  IF Y3 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF Y3 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Y4 := X8 ISUB X7\r\n" + //
                                "  X8 := Y4\r\n" + //
                                "  Y5 := X8 ISUB X7\r\n" + //
                                "  Y6 := 0\r\n" + //
                                "  Y7 := Y5 GT Y6\r\n" + //
                                "  Y3 := Y7\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  X9 |< X8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  Z2 <- Z1\r\n" + //
                                "  IPARAM Z2\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  Z8 <- Z7\r\n" + //
                                "  a10 <- Z9\r\n" + //
                                "  a13 := 0\r\n" + //
                                "  a11 := 0\r\n" + //
                                "  a13 := Z8\r\n" + //
                                "  a14 := 0\r\n" + //
                                "  a11 := a14\r\n" + //
                                "  a15 := a13 ISUB a10\r\n" + //
                                "  a16 := 0\r\n" + //
                                "  a17 := a15 GT a16\r\n" + //
                                "  IF a17 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF a17 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a18 := a13 ISUB a10\r\n" + //
                                "  a13 := a18\r\n" + //
                                "  a19 := 1\r\n" + //
                                "  a20 := a11 IADD a19\r\n" + //
                                "  a11 := a20\r\n" + //
                                "  a21 := a13 ISUB a10\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  a23 := a21 GT a22\r\n" + //
                                "  a17 := a23\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  a12 |< a11\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testTest3(){
        String progSrc = "test_source/test3.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 42\r\n" + //
                                " b := a\r\n" + //
                                " c := 42.0\r\n" + //
                                " d := c\r\n" + //
                                " b22 := 0\r\n" + //
                                " f := 0\r\n" + //
                                " g := 0.0\r\n" + //
                                " h := 0.0\r\n" + //
                                " b25 := FALSE\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL IntToReal ( b -> W1 )\r\n" + //
                                " W0 <| W4\r\n" + //
                                " CALL RMul ( W0 -> a68 , h -> a69 )\r\n" + //
                                " a67 <| a70\r\n" + //
                                " CALL Foo ( b22 -> b14 , g -> k , a67 -> l )\r\n" + //
                                " q <| b17\r\n" + //
                                " b25 := q\r\n" + //
                                " IF b25 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_2 ELSE IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                " CALL WriteInt ( b22 -> c15 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                " CALL IntToReal ( b -> W1 )\r\n" + //
                                " c17 <| W4\r\n" + //
                                " CALL RSub ( d -> c19 , c17 -> c20 )\r\n" + //
                                " c18 <| c21\r\n" + //
                                " CALL WriteReal ( c18 -> d25 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0_2\r\n" + //
                                " LABEL IFEND_0_LEVEL_0_2\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  W2 <- W1\r\n" + //
                                "  W3 := 0.0\r\n" + //
                                "  W5 := FALSE\r\n" + //
                                "  W6 := 0\r\n" + //
                                "  W7 := 0\r\n" + //
                                "  W8 := 0\r\n" + //
                                "  W9 := 0\r\n" + //
                                "  X0 := 0\r\n" + //
                                "  X1 := 0\r\n" + //
                                "  X2 := 0\r\n" + //
                                "  X3 := 0\r\n" + //
                                "  X4 := 0\r\n" + //
                                "  X5 := 0\r\n" + //
                                "  W9 := W2\r\n" + //
                                "  X6 := 0\r\n" + //
                                "  W8 := X6\r\n" + //
                                "  CALL IntIsNegative ( W2 -> X8 )\r\n" + //
                                "  X7 <| Y1\r\n" + //
                                "  W5 := X7\r\n" + //
                                "  CALL IntIsZero ( W9 -> Z2 )\r\n" + //
                                "  Z1 <| Z5\r\n" + //
                                "  a10 := BNOT Z1\r\n" + //
                                "  IF a10 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a10 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a11 := 1\r\n" + //
                                "  a12 := W9 IAND a11\r\n" + //
                                "  X0 := a12\r\n" + //
                                "  a13 := 1\r\n" + //
                                "  a14 := X0 EQ a13\r\n" + //
                                "  IF a14 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  W7 := W8\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  a15 := 1\r\n" + //
                                "  a16 := W8 IADD a15\r\n" + //
                                "  W8 := a16\r\n" + //
                                "  a17 := 1\r\n" + //
                                "  a18 := W9 IRSHIFT a17\r\n" + //
                                "  W9 := a18\r\n" + //
                                "  CALL IntIsZero ( W9 -> Z2 )\r\n" + //
                                "  a19 <| Z5\r\n" + //
                                "  a20 := BNOT a19\r\n" + //
                                "  a10 := a20\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a21 := 23\r\n" + //
                                "  a22 := W7 LT a21\r\n" + //
                                "  IF a22 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a23 := 23\r\n" + //
                                "  a24 := a23 ISUB W7\r\n" + //
                                "  X1 := a24\r\n" + //
                                "  a25 := INOT X1\r\n" + //
                                "  a26 := 1\r\n" + //
                                "  a27 := a25 IADD a26\r\n" + //
                                "  X2 := a27\r\n" + //
                                "  a28 := 2147483648\r\n" + //
                                "  a29 := INOT a28\r\n" + //
                                "  a30 := X2 IAND a29\r\n" + //
                                "  X2 := a30\r\n" + //
                                "  a31 := 64\r\n" + //
                                "  a32 := X2 IOR a31\r\n" + //
                                "  X2 := a32\r\n" + //
                                "  a33 := 1\r\n" + //
                                "  a34 := a33 ILSHIFT W7\r\n" + //
                                "  a35 := 1\r\n" + //
                                "  a36 := a34 ISUB a35\r\n" + //
                                "  X4 := a36\r\n" + //
                                "  a37 := W2 IAND X4\r\n" + //
                                "  X3 := a37\r\n" + //
                                "  a38 := X3 ILSHIFT X1\r\n" + //
                                "  X3 := a38\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a39 := 23\r\n" + //
                                "  a40 := W7 GT a39\r\n" + //
                                "  IF a40 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a41 := 23\r\n" + //
                                "  a42 := W7 ISUB a41\r\n" + //
                                "  X1 := a42\r\n" + //
                                "  X2 := X1\r\n" + //
                                "  a43 := 1\r\n" + //
                                "  a44 := a43 ILSHIFT W7\r\n" + //
                                "  a45 := 1\r\n" + //
                                "  a46 := a44 ISUB a45\r\n" + //
                                "  X4 := a46\r\n" + //
                                "  a47 := W2 IAND X4\r\n" + //
                                "  X3 := a47\r\n" + //
                                "  a48 := X3 IRSHIFT X1\r\n" + //
                                "  X3 := a48\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  X2 := W7\r\n" + //
                                "  a49 := 1\r\n" + //
                                "  a50 := a49 ILSHIFT W7\r\n" + //
                                "  a51 := 1\r\n" + //
                                "  a52 := a50 ISUB a51\r\n" + //
                                "  X4 := a52\r\n" + //
                                "  a53 := W2 IAND X4\r\n" + //
                                "  X3 := a53\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  W6 := X3\r\n" + //
                                "  a54 := 23\r\n" + //
                                "  a55 := X2 ILSHIFT a54\r\n" + //
                                "  a56 := W6 IOR a55\r\n" + //
                                "  W6 := a56\r\n" + //
                                "  IF W5 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a57 := 1\r\n" + //
                                "  a58 := 31\r\n" + //
                                "  a59 := a57 ILSHIFT a58\r\n" + //
                                "  a60 := W6 IOR a59\r\n" + //
                                "  W6 := a60\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( W6 -> a61 )\r\n" + //
                                "  a66 <| a64\r\n" + //
                                "  W3 := a66\r\n" + //
                                "  W4 |< W3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  X9 <- X8\r\n" + //
                                "  Y0 := FALSE\r\n" + //
                                "  Y2 := 0\r\n" + //
                                "  Y3 := 31\r\n" + //
                                "  Y4 := X9 IRSHIFT Y3\r\n" + //
                                "  Y5 := 1\r\n" + //
                                "  Y6 := Y4 IAND Y5\r\n" + //
                                "  Y2 := Y6\r\n" + //
                                "  Y7 := 0\r\n" + //
                                "  Y8 := Y2 EQ Y7\r\n" + //
                                "  IF Y8 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Y9 := FALSE\r\n" + //
                                "  Y0 := Y9\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z0 := TRUE\r\n" + //
                                "  Y0 := Z0\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  Y1 |< Y0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  Z3 <- Z2\r\n" + //
                                "  Z4 := FALSE\r\n" + //
                                "  Z6 := 0\r\n" + //
                                "  Z7 := Z3 EQ Z6\r\n" + //
                                "  IF Z7 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z8 := TRUE\r\n" + //
                                "  Z4 := Z8\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z9 := FALSE\r\n" + //
                                "  Z4 := Z9\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  Z5 |< Z4\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a62 <- a61\r\n" + //
                                "  a63 := 0.0\r\n" + //
                                "  a65 := 0\r\n" + //
                                "  IPARAM a65\r\n" + //
                                "  IPARAM a62\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a65\r\n" + //
                                "  IPARAM a63\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a64 |< a63\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  q4 <- a68\r\n" + //
                                "  q6 <- a69\r\n" + //
                                "  p3 := 0.0\r\n" + //
                                "  p4 := 0\r\n" + //
                                "  p5 := 0\r\n" + //
                                "  p6 := 0\r\n" + //
                                "  p7 := 0\r\n" + //
                                "  p8 := 0\r\n" + //
                                "  p9 := 0\r\n" + //
                                "  q0 := 0\r\n" + //
                                "  q1 := 0\r\n" + //
                                "  q2 := 0\r\n" + //
                                "  q3 := 0\r\n" + //
                                "  CALL RealSign ( q4 -> a71 )\r\n" + //
                                "  q5 <| a74\r\n" + //
                                "  p5 := q5\r\n" + //
                                "  CALL RealSign ( q6 -> a71 )\r\n" + //
                                "  q7 <| a74\r\n" + //
                                "  p6 := q7\r\n" + //
                                "  CALL RealExponent ( q4 -> a86 )\r\n" + //
                                "  q8 <| a89\r\n" + //
                                "  p7 := q8\r\n" + //
                                "  CALL RealExponent ( q6 -> a86 )\r\n" + //
                                "  q9 <| a89\r\n" + //
                                "  p8 := q9\r\n" + //
                                "  CALL RealMantissa ( q4 -> a96 )\r\n" + //
                                "  r0 <| a99\r\n" + //
                                "  p9 := r0\r\n" + //
                                "  CALL RealMantissa ( q6 -> a96 )\r\n" + //
                                "  r1 <| a99\r\n" + //
                                "  q0 := r1\r\n" + //
                                "  r2 := p5 NE p6\r\n" + //
                                "  IF r2 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  r3 := 1\r\n" + //
                                "  q1 := r3\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  r4 := 0\r\n" + //
                                "  q1 := r4\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0\r\n" + //
                                "  r5 := p9 IMUL q0\r\n" + //
                                "  q2 := r5\r\n" + //
                                "  r6 := p7 IADD p8\r\n" + //
                                "  q3 := r6\r\n" + //
                                "  r7 := 23\r\n" + //
                                "  r8 := q3 ILSHIFT r7\r\n" + //
                                "  p4 := r8\r\n" + //
                                "  r9 := 31\r\n" + //
                                "  s0 := q1 ILSHIFT r9\r\n" + //
                                "  s1 := p4 IOR s0\r\n" + //
                                "  p4 := s1\r\n" + //
                                "  s2 := p4 IOR q2\r\n" + //
                                "  p4 := s2\r\n" + //
                                "  CALL IntBinaryAsReal ( p4 -> a61 )\r\n" + //
                                "  s3 <| a64\r\n" + //
                                "  p3 := s3\r\n" + //
                                "  a70 |< p3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a72 <- a71\r\n" + //
                                "  a73 := 0\r\n" + //
                                "  a75 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a72 -> a77 )\r\n" + //
                                "  a76 <| a80\r\n" + //
                                "  a75 := a76\r\n" + //
                                "  a82 := 31\r\n" + //
                                "  a83 := a75 IRSHIFT a82\r\n" + //
                                "  a84 := 1\r\n" + //
                                "  a85 := a83 IAND a84\r\n" + //
                                "  a73 := a85\r\n" + //
                                "  a74 |< a73\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  a78 <- a77\r\n" + //
                                "  a79 := 0\r\n" + //
                                "  a81 := 0.0\r\n" + //
                                "  IPARAM a81\r\n" + //
                                "  IPARAM a78\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a81\r\n" + //
                                "  IPARAM a79\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a80 |< a79\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  a87 <- a86\r\n" + //
                                "  a88 := 0\r\n" + //
                                "  a90 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a87 -> a77 )\r\n" + //
                                "  a91 <| a80\r\n" + //
                                "  a90 := a91\r\n" + //
                                "  a92 := 23\r\n" + //
                                "  a93 := a90 IRSHIFT a92\r\n" + //
                                "  a94 := 255\r\n" + //
                                "  a95 := a93 IAND a94\r\n" + //
                                "  a88 := a95\r\n" + //
                                "  a89 |< a88\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  a97 <- a96\r\n" + //
                                "  a98 := 0\r\n" + //
                                "  b10 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a97 -> a77 )\r\n" + //
                                "  b11 <| a80\r\n" + //
                                "  b10 := b11\r\n" + //
                                "  b12 := 8388607\r\n" + //
                                "  b13 := b10 IAND b12\r\n" + //
                                "  a98 := b13\r\n" + //
                                "  a99 |< a98\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Foo\r\n" + //
                                "  b15 <- b14\r\n" + //
                                "  b18 := 0\r\n" + //
                                "  b18 := b\r\n" + //
                                "  LABEL FORBEG_0_LEVEL_0\r\n" + //
                                "  IF b18 GT b15 THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                "  LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                "  CALL Bar (  )\r\n" + //
                                "  b29 := 20\r\n" + //
                                "  CALL INeg ( b29 -> b31 )\r\n" + //
                                "  b30 <| b34\r\n" + //
                                "  b38 := b18 IADD b30\r\n" + //
                                "  b18 := b38\r\n" + //
                                "  GOTO FORBEG_0_LEVEL_0\r\n" + //
                                "  LABEL FOREND_0_LEVEL_0\r\n" + //
                                "  b39 := 3.1415927\r\n" + //
                                "  CALL RGreaterThan ( z -> b41 , b39 -> b42 )\r\n" + //
                                "  b40 <| b43\r\n" + //
                                "  b16 := y LAND b40\r\n" + //
                                "  b17 |< b16\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Bar\r\n" + //
                                "  b19 := FALSE\r\n" + //
                                "  b20 := 1\r\n" + //
                                "  b21 := b22 IADD b20\r\n" + //
                                "  b22 := b21\r\n" + //
                                "  b23 := BNOT b25\r\n" + //
                                "  IF b23 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_2 ELSE IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                "  b24 := TRUE\r\n" + //
                                "  b25 := b24\r\n" + //
                                "  b26 := 0\r\n" + //
                                "  b27 := 0\r\n" + //
                                "  CALL Foo ( f -> b14 , b26 -> k , b27 -> l )\r\n" + //
                                "  b28 <| b17\r\n" + //
                                "  b19 := b28\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_2\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  b32 <- b31\r\n" + //
                                "  b33 := 0\r\n" + //
                                "  b35 := INOT b32\r\n" + //
                                "  b36 := 1\r\n" + //
                                "  b37 := b35 IADD b36\r\n" + //
                                "  b33 := b37\r\n" + //
                                "  b34 |< b33\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThan\r\n" + //
                                "  J1 <- b41\r\n" + //
                                "  J3 <- b42\r\n" + //
                                "  I0 := FALSE\r\n" + //
                                "  I1 := FALSE\r\n" + //
                                "  I2 := FALSE\r\n" + //
                                "  I3 := FALSE\r\n" + //
                                "  I4 := FALSE\r\n" + //
                                "  I5 := FALSE\r\n" + //
                                "  I6 := FALSE\r\n" + //
                                "  I7 := 0\r\n" + //
                                "  I8 := 0\r\n" + //
                                "  I9 := 0\r\n" + //
                                "  J0 := 0\r\n" + //
                                "  CALL RealIsZero ( J1 -> b44 )\r\n" + //
                                "  J2 <| b47\r\n" + //
                                "  I1 := J2\r\n" + //
                                "  CALL RealIsZero ( J3 -> b44 )\r\n" + //
                                "  J4 <| b47\r\n" + //
                                "  I2 := J4\r\n" + //
                                "  CALL RealIsNegative ( J1 -> b54 )\r\n" + //
                                "  J5 <| b57\r\n" + //
                                "  I3 := J5\r\n" + //
                                "  CALL RealIsNegative ( J3 -> b54 )\r\n" + //
                                "  J6 <| b57\r\n" + //
                                "  I4 := J6\r\n" + //
                                "  CALL RealIsPositive ( J1 -> b64 )\r\n" + //
                                "  J7 <| b67\r\n" + //
                                "  I5 := J7\r\n" + //
                                "  CALL RealIsPositive ( J3 -> b64 )\r\n" + //
                                "  J8 <| b67\r\n" + //
                                "  I6 := J8\r\n" + //
                                "  J9 := I1 LAND I2\r\n" + //
                                "  IF J9 EQ TRUE THEN IFSTAT_50_SEQ_0_LEVEL_0 ELSE IFNEXT_50_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_0_LEVEL_0\r\n" + //
                                "  K0 := FALSE\r\n" + //
                                "  I0 := K0\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_0_LEVEL_0\r\n" + //
                                "  K1 := I3 LAND I2\r\n" + //
                                "  IF K1 EQ TRUE THEN IFSTAT_50_SEQ_1_LEVEL_0 ELSE IFNEXT_50_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_1_LEVEL_0\r\n" + //
                                "  K2 := TRUE\r\n" + //
                                "  I0 := K2\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_1_LEVEL_0\r\n" + //
                                "  K3 := I3 LAND I6\r\n" + //
                                "  IF K3 EQ TRUE THEN IFSTAT_50_SEQ_2_LEVEL_0 ELSE IFNEXT_50_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_2_LEVEL_0\r\n" + //
                                "  K4 := TRUE\r\n" + //
                                "  I0 := K4\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_2_LEVEL_0\r\n" + //
                                "  K5 := I1 LAND I6\r\n" + //
                                "  IF K5 EQ TRUE THEN IFSTAT_50_SEQ_3_LEVEL_0 ELSE IFNEXT_50_SEQ_3_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_3_LEVEL_0\r\n" + //
                                "  K6 := TRUE\r\n" + //
                                "  I0 := K6\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_3_LEVEL_0\r\n" + //
                                "  K7 := I3 LAND I4\r\n" + //
                                "  IF K7 EQ TRUE THEN IFSTAT_50_SEQ_4_LEVEL_0 ELSE IFNEXT_50_SEQ_4_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_4_LEVEL_0\r\n" + //
                                "  CALL RealScore ( J1 -> b75 )\r\n" + //
                                "  K8 <| b78\r\n" + //
                                "  I7 := K8\r\n" + //
                                "  CALL RealScore ( J3 -> b75 )\r\n" + //
                                "  K9 <| b78\r\n" + //
                                "  I8 := K9\r\n" + //
                                "  L0 := I7 LT I8\r\n" + //
                                "  IF L0 EQ TRUE THEN IFSTAT_51_SEQ_0_LEVEL_1 ELSE IFNEXT_51_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_51_SEQ_0_LEVEL_1\r\n" + //
                                "  L1 := TRUE\r\n" + //
                                "  I0 := L1\r\n" + //
                                "  GOTO IFEND_51_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_51_SEQ_0_LEVEL_1\r\n" + //
                                "  L2 := I7 EQ I8\r\n" + //
                                "  IF L2 EQ TRUE THEN IFSTAT_51_SEQ_1_LEVEL_1 ELSE IFNEXT_51_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_51_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( J1 -> a96 )\r\n" + //
                                "  L3 <| a99\r\n" + //
                                "  I9 := L3\r\n" + //
                                "  CALL RealMantissa ( J3 -> a96 )\r\n" + //
                                "  L4 <| a99\r\n" + //
                                "  J0 := L4\r\n" + //
                                "  L5 := I9 LT J0\r\n" + //
                                "  IF L5 EQ TRUE THEN IFSTAT_52_SEQ_0_LEVEL_2 ELSE IFNEXT_52_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_52_SEQ_0_LEVEL_2\r\n" + //
                                "  L6 := TRUE\r\n" + //
                                "  I0 := L6\r\n" + //
                                "  GOTO IFEND_52_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_52_SEQ_0_LEVEL_2\r\n" + //
                                "  L7 := FALSE\r\n" + //
                                "  I0 := L7\r\n" + //
                                "  GOTO IFEND_52_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_52_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_52_LEVEL_2\r\n" + //
                                "  GOTO IFEND_51_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_51_SEQ_1_LEVEL_1\r\n" + //
                                "  L8 := FALSE\r\n" + //
                                "  I0 := L8\r\n" + //
                                "  GOTO IFEND_51_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_51_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_51_LEVEL_1\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_4_LEVEL_0\r\n" + //
                                "  L9 := I5 LAND I6\r\n" + //
                                "  IF L9 EQ TRUE THEN IFSTAT_50_SEQ_5_LEVEL_0 ELSE IFNEXT_50_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_5_LEVEL_0\r\n" + //
                                "  CALL RealScore ( J1 -> b75 )\r\n" + //
                                "  M0 <| b78\r\n" + //
                                "  I7 := M0\r\n" + //
                                "  CALL RealScore ( J3 -> b75 )\r\n" + //
                                "  M1 <| b78\r\n" + //
                                "  I7 := M1\r\n" + //
                                "  M2 := I7 GT I8\r\n" + //
                                "  IF M2 EQ TRUE THEN IFSTAT_56_SEQ_0_LEVEL_1 ELSE IFNEXT_56_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_56_SEQ_0_LEVEL_1\r\n" + //
                                "  M3 := TRUE\r\n" + //
                                "  I0 := M3\r\n" + //
                                "  GOTO IFEND_56_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_0_LEVEL_1\r\n" + //
                                "  M4 := I7 EQ I8\r\n" + //
                                "  IF M4 EQ TRUE THEN IFSTAT_56_SEQ_1_LEVEL_1 ELSE IFNEXT_56_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_56_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( J1 -> a96 )\r\n" + //
                                "  M5 <| a99\r\n" + //
                                "  I9 := M5\r\n" + //
                                "  CALL RealMantissa ( J3 -> a96 )\r\n" + //
                                "  M6 <| a99\r\n" + //
                                "  J0 := M6\r\n" + //
                                "  M7 := I9 GT J0\r\n" + //
                                "  IF M7 EQ TRUE THEN IFSTAT_57_SEQ_0_LEVEL_2 ELSE IFNEXT_57_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_57_SEQ_0_LEVEL_2\r\n" + //
                                "  M8 := TRUE\r\n" + //
                                "  I0 := M8\r\n" + //
                                "  GOTO IFEND_57_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_57_SEQ_0_LEVEL_2\r\n" + //
                                "  M9 := FALSE\r\n" + //
                                "  I0 := M9\r\n" + //
                                "  GOTO IFEND_57_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_57_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_57_LEVEL_2\r\n" + //
                                "  GOTO IFEND_56_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_1_LEVEL_1\r\n" + //
                                "  N0 := FALSE\r\n" + //
                                "  I0 := N0\r\n" + //
                                "  GOTO IFEND_56_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_56_LEVEL_1\r\n" + //
                                "  GOTO IFEND_50_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFEND_50_LEVEL_0\r\n" + //
                                "  b43 |< I0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsZero\r\n" + //
                                "  b45 <- b44\r\n" + //
                                "  b46 := FALSE\r\n" + //
                                "  b48 := 0\r\n" + //
                                "  CALL RealMantissa ( b45 -> a96 )\r\n" + //
                                "  b49 <| a99\r\n" + //
                                "  b48 := b49\r\n" + //
                                "  b50 := 0\r\n" + //
                                "  b51 := b48 EQ b50\r\n" + //
                                "  IF b51 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b52 := TRUE\r\n" + //
                                "  b46 := b52\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b53 := FALSE\r\n" + //
                                "  b46 := b53\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  b47 |< b46\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsNegative\r\n" + //
                                "  b55 <- b54\r\n" + //
                                "  b56 := FALSE\r\n" + //
                                "  b58 := 0\r\n" + //
                                "  CALL RealSign ( b55 -> a71 )\r\n" + //
                                "  b59 <| a74\r\n" + //
                                "  b58 := b59\r\n" + //
                                "  b60 := 0\r\n" + //
                                "  b61 := b58 EQ b60\r\n" + //
                                "  IF b61 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b62 := FALSE\r\n" + //
                                "  b56 := b62\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b63 := TRUE\r\n" + //
                                "  b56 := b63\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_3_LEVEL_0_0\r\n" + //
                                "  b57 |< b56\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsPositive\r\n" + //
                                "  b65 <- b64\r\n" + //
                                "  b66 := FALSE\r\n" + //
                                "  b68 := 0\r\n" + //
                                "  b69 := 0\r\n" + //
                                "  CALL RealSign ( b65 -> a71 )\r\n" + //
                                "  b70 <| a74\r\n" + //
                                "  b69 := b70\r\n" + //
                                "  b71 := 0\r\n" + //
                                "  b72 := b69 EQ b71\r\n" + //
                                "  IF b72 EQ TRUE THEN IFSTAT_5_SEQ_0_LEVEL_0_0 ELSE IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b73 := TRUE\r\n" + //
                                "  b66 := b73\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b74 := FALSE\r\n" + //
                                "  b66 := b74\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_5_LEVEL_0_0\r\n" + //
                                "  b67 |< b66\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealScore\r\n" + //
                                "  b76 <- b75\r\n" + //
                                "  b77 := 0\r\n" + //
                                "  b79 := 0\r\n" + //
                                "  b80 := 0\r\n" + //
                                "  b81 := 0\r\n" + //
                                "  b82 := 0\r\n" + //
                                "  b83 := 0\r\n" + //
                                "  b84 := 0\r\n" + //
                                "  CALL RealExponent ( b76 -> a86 )\r\n" + //
                                "  b85 <| a89\r\n" + //
                                "  b79 := b85\r\n" + //
                                "  CALL RealMantissa ( b76 -> a96 )\r\n" + //
                                "  b86 <| a99\r\n" + //
                                "  b80 := b86\r\n" + //
                                "  b87 := 0\r\n" + //
                                "  b88 := b80 EQ b87\r\n" + //
                                "  IF b88 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_0_0 ELSE IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b77 := b80\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b89 := 0\r\n" + //
                                "  b83 := b89\r\n" + //
                                "  b90 := 0\r\n" + //
                                "  b91 := b80 NE b90\r\n" + //
                                "  IF b91 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF b91 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b92 := 1\r\n" + //
                                "  b93 := b80 IAND b92\r\n" + //
                                "  b84 := b93\r\n" + //
                                "  b94 := 1\r\n" + //
                                "  b95 := b84 EQ b94\r\n" + //
                                "  IF b95 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_1 ELSE IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  b81 := b83\r\n" + //
                                "  GOTO IFEND_7_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFEND_7_LEVEL_1\r\n" + //
                                "  b96 := 1\r\n" + //
                                "  b97 := b80 IRSHIFT b96\r\n" + //
                                "  b80 := b97\r\n" + //
                                "  b98 := 1\r\n" + //
                                "  b99 := b83 IADD b98\r\n" + //
                                "  b83 := b99\r\n" + //
                                "  c10 := 0\r\n" + //
                                "  c11 := b80 NE c10\r\n" + //
                                "  b91 := c11\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  c12 := 23\r\n" + //
                                "  c13 := c12 ISUB b81\r\n" + //
                                "  c14 := c13 IADD b79\r\n" + //
                                "  b77 := c14\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_6_LEVEL_0_0\r\n" + //
                                "  b78 |< b77\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  c16 <- c15\r\n" + //
                                "  IPARAM c16\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RSub\r\n" + //
                                "  o9 <- c19\r\n" + //
                                "  p0 <- c20\r\n" + //
                                "  o8 := 0.0\r\n" + //
                                "  CALL RNeg ( p0 -> c22 )\r\n" + //
                                "  p1 <| c23\r\n" + //
                                "  CALL RAdd ( o9 -> c24 , p1 -> c26 )\r\n" + //
                                "  p2 <| c29\r\n" + //
                                "  o8 := p2\r\n" + //
                                "  c21 |< o8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RNeg\r\n" + //
                                "  x5 <- c22\r\n" + //
                                "  w9 := 0\r\n" + //
                                "  x0 := 0\r\n" + //
                                "  x1 := 0\r\n" + //
                                "  x2 := 1\r\n" + //
                                "  x3 := 31\r\n" + //
                                "  x4 := x2 ILSHIFT x3\r\n" + //
                                "  w9 := x4\r\n" + //
                                "  CALL RealBinaryAsInt ( x5 -> a77 )\r\n" + //
                                "  x6 <| a80\r\n" + //
                                "  x0 := x6\r\n" + //
                                "  x7 := x0 IXOR w9\r\n" + //
                                "  x1 := x7\r\n" + //
                                "  c23 |< x1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  c25 <- c24\r\n" + //
                                "  c27 <- c26\r\n" + //
                                "  c28 := 0.0\r\n" + //
                                "  c30 := 0\r\n" + //
                                "  c31 := 0\r\n" + //
                                "  c32 := 0\r\n" + //
                                "  c33 := 0\r\n" + //
                                "  c34 := 0\r\n" + //
                                "  c35 := 0\r\n" + //
                                "  c36 := 0\r\n" + //
                                "  c37 := 0\r\n" + //
                                "  c38 := 0\r\n" + //
                                "  c39 := 0\r\n" + //
                                "  c40 := 0\r\n" + //
                                "  c41 := 0\r\n" + //
                                "  CALL RealSign ( c25 -> a71 )\r\n" + //
                                "  c42 <| a74\r\n" + //
                                "  c31 := c42\r\n" + //
                                "  CALL RealSign ( c27 -> a71 )\r\n" + //
                                "  c43 <| a74\r\n" + //
                                "  c32 := c43\r\n" + //
                                "  CALL RealExponent ( c25 -> a86 )\r\n" + //
                                "  c44 <| a89\r\n" + //
                                "  c34 := c44\r\n" + //
                                "  CALL RealExponent ( c27 -> a86 )\r\n" + //
                                "  c45 <| a89\r\n" + //
                                "  c35 := c45\r\n" + //
                                "  CALL RealMantissa ( c25 -> a96 )\r\n" + //
                                "  c46 <| a99\r\n" + //
                                "  c36 := c46\r\n" + //
                                "  CALL RealMantissa ( c27 -> a96 )\r\n" + //
                                "  c47 <| a99\r\n" + //
                                "  c37 := c47\r\n" + //
                                "  c48 := c31 EQ c32\r\n" + //
                                "  IF c48 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_3 ELSE IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  c41 := c31\r\n" + //
                                "  c49 := c34 EQ c35\r\n" + //
                                "  IF c49 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  c50 := c36 IADD c37\r\n" + //
                                "  c40 := c50\r\n" + //
                                "  c51 := 25\r\n" + //
                                "  c52 := c40 IRSHIFT c51\r\n" + //
                                "  c53 := 1\r\n" + //
                                "  c54 := c52 IAND c53\r\n" + //
                                "  c39 := c54\r\n" + //
                                "  c38 := c34\r\n" + //
                                "  c55 := 1\r\n" + //
                                "  c56 := c39 EQ c55\r\n" + //
                                "  IF c56 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  c57 := 1\r\n" + //
                                "  c58 := c38 IADD c57\r\n" + //
                                "  c38 := c58\r\n" + //
                                "  c59 := 1\r\n" + //
                                "  c60 := c40 IRSHIFT c59\r\n" + //
                                "  c40 := c60\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  c61 := 23\r\n" + //
                                "  c62 := c38 ILSHIFT c61\r\n" + //
                                "  c30 := c62\r\n" + //
                                "  c63 := c30 IOR c40\r\n" + //
                                "  c30 := c63\r\n" + //
                                "  c64 := 31\r\n" + //
                                "  c1 := c41 ILSHIFT c64\r\n" + //
                                "  c2 := c30 IOR c1\r\n" + //
                                "  c30 := c2\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  c3 := c34 GT c35\r\n" + //
                                "  IF c3 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c4 := c34 ISUB c35\r\n" + //
                                "  c33 := c4\r\n" + //
                                "  c35 := c34\r\n" + //
                                "  c38 := c34\r\n" + //
                                "  c5 := c37 IRSHIFT c33\r\n" + //
                                "  c37 := c5\r\n" + //
                                "  c6 := c36 IADD c37\r\n" + //
                                "  c40 := c6\r\n" + //
                                "  c7 := 25\r\n" + //
                                "  c8 := c40 IRSHIFT c7\r\n" + //
                                "  c9 := 1\r\n" + //
                                "  d0 := c8 IAND c9\r\n" + //
                                "  c39 := d0\r\n" + //
                                "  d1 := 1\r\n" + //
                                "  d2 := c39 EQ d1\r\n" + //
                                "  IF d2 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  d3 := 1\r\n" + //
                                "  d4 := c38 IADD d3\r\n" + //
                                "  c38 := d4\r\n" + //
                                "  d5 := 1\r\n" + //
                                "  d6 := c40 IRSHIFT d5\r\n" + //
                                "  c40 := d6\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  d7 := 23\r\n" + //
                                "  d8 := c38 ILSHIFT d7\r\n" + //
                                "  c30 := d8\r\n" + //
                                "  d9 := 31\r\n" + //
                                "  c65 := c41 ILSHIFT d9\r\n" + //
                                "  c66 := c30 IOR c65\r\n" + //
                                "  c30 := c66\r\n" + //
                                "  c67 := c30 IOR c40\r\n" + //
                                "  c30 := c67\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c68 := c35 ISUB c34\r\n" + //
                                "  c33 := c68\r\n" + //
                                "  c34 := c35\r\n" + //
                                "  c38 := c35\r\n" + //
                                "  c69 := c36 IRSHIFT c33\r\n" + //
                                "  c36 := c69\r\n" + //
                                "  c70 := c36 IADD c37\r\n" + //
                                "  c40 := c70\r\n" + //
                                "  c71 := 25\r\n" + //
                                "  c72 := c40 IRSHIFT c71\r\n" + //
                                "  c73 := 1\r\n" + //
                                "  c74 := c72 IAND c73\r\n" + //
                                "  c39 := c74\r\n" + //
                                "  c75 := 1\r\n" + //
                                "  c76 := c39 EQ c75\r\n" + //
                                "  IF c76 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  c77 := 1\r\n" + //
                                "  c78 := c38 IADD c77\r\n" + //
                                "  c38 := c78\r\n" + //
                                "  c79 := 1\r\n" + //
                                "  c80 := c40 IRSHIFT c79\r\n" + //
                                "  c40 := c80\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  c81 := 23\r\n" + //
                                "  c82 := c38 ILSHIFT c81\r\n" + //
                                "  c30 := c82\r\n" + //
                                "  c83 := 31\r\n" + //
                                "  c84 := c41 ILSHIFT c83\r\n" + //
                                "  c85 := c30 IOR c84\r\n" + //
                                "  c30 := c85\r\n" + //
                                "  c86 := c30 IOR c40\r\n" + //
                                "  c30 := c86\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  c87 := 0\r\n" + //
                                "  c88 := c31 EQ c87\r\n" + //
                                "  c89 := 1\r\n" + //
                                "  c90 := c32 EQ c89\r\n" + //
                                "  c91 := c88 LAND c90\r\n" + //
                                "  IF c91 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  c92 := c37 GT c36\r\n" + //
                                "  IF c92 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  c93 := 1\r\n" + //
                                "  c41 := c93\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  c94 := 0\r\n" + //
                                "  c41 := c94\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  c95 := 1\r\n" + //
                                "  c96 := c31 EQ c95\r\n" + //
                                "  c97 := 0\r\n" + //
                                "  c98 := c32 EQ c97\r\n" + //
                                "  c99 := c96 LAND c98\r\n" + //
                                "  IF c99 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  d10 := c37 GE c36\r\n" + //
                                "  IF d10 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  d11 := 0\r\n" + //
                                "  c41 := d11\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  d12 := 1\r\n" + //
                                "  c41 := d12\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  h8 := c34 EQ c35\r\n" + //
                                "  IF h8 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  h9 := 0\r\n" + //
                                "  c40 := h9\r\n" + //
                                "  i0 := 25\r\n" + //
                                "  i1 := c40 IRSHIFT i0\r\n" + //
                                "  i2 := 1\r\n" + //
                                "  i3 := i1 IAND i2\r\n" + //
                                "  c39 := i3\r\n" + //
                                "  c38 := c34\r\n" + //
                                "  d13 := 1\r\n" + //
                                "  d14 := c39 EQ d13\r\n" + //
                                "  IF d14 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  d15 := 1\r\n" + //
                                "  d16 := c38 IADD d15\r\n" + //
                                "  c38 := d16\r\n" + //
                                "  d17 := 1\r\n" + //
                                "  d18 := c40 IRSHIFT d17\r\n" + //
                                "  c40 := d18\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  d19 := 23\r\n" + //
                                "  d20 := c38 ILSHIFT d19\r\n" + //
                                "  c30 := d20\r\n" + //
                                "  d21 := c30 IOR c40\r\n" + //
                                "  c30 := d21\r\n" + //
                                "  d22 := 31\r\n" + //
                                "  d23 := c41 ILSHIFT d22\r\n" + //
                                "  d24 := c30 IOR d23\r\n" + //
                                "  c30 := d24\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  j6 := c34 GT c35\r\n" + //
                                "  IF j6 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  j7 := c34 ISUB c35\r\n" + //
                                "  c33 := j7\r\n" + //
                                "  c35 := c34\r\n" + //
                                "  c38 := c34\r\n" + //
                                "  j8 := c37 IRSHIFT c33\r\n" + //
                                "  c37 := j8\r\n" + //
                                "  j9 := 1\r\n" + //
                                "  k0 := c31 EQ j9\r\n" + //
                                "  k1 := 0\r\n" + //
                                "  k2 := c32 EQ k1\r\n" + //
                                "  k3 := k0 LAND k2\r\n" + //
                                "  IF k3 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  k4 := c37 ISUB c36\r\n" + //
                                "  c40 := k4\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  k5 := c36 ISUB c37\r\n" + //
                                "  c40 := k5\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  k6 := 25\r\n" + //
                                "  k7 := c40 IRSHIFT k6\r\n" + //
                                "  k8 := 1\r\n" + //
                                "  k9 := k7 IAND k8\r\n" + //
                                "  c39 := k9\r\n" + //
                                "  l0 := 1\r\n" + //
                                "  l1 := c39 EQ l0\r\n" + //
                                "  IF l1 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  l2 := 1\r\n" + //
                                "  l3 := c38 IADD l2\r\n" + //
                                "  c38 := l3\r\n" + //
                                "  l4 := 1\r\n" + //
                                "  l5 := c40 IRSHIFT l4\r\n" + //
                                "  c40 := l5\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  l6 := 23\r\n" + //
                                "  l7 := c38 ILSHIFT l6\r\n" + //
                                "  c30 := l7\r\n" + //
                                "  l8 := 31\r\n" + //
                                "  l9 := c41 ILSHIFT l8\r\n" + //
                                "  m0 := c30 IOR l9\r\n" + //
                                "  c30 := m0\r\n" + //
                                "  m1 := c30 IOR c40\r\n" + //
                                "  c30 := m1\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  m2 := c35 ISUB c34\r\n" + //
                                "  c33 := m2\r\n" + //
                                "  c34 := c35\r\n" + //
                                "  c38 := c35\r\n" + //
                                "  m3 := c36 IRSHIFT c33\r\n" + //
                                "  c36 := m3\r\n" + //
                                "  m4 := 1\r\n" + //
                                "  m5 := c31 EQ m4\r\n" + //
                                "  m6 := 0\r\n" + //
                                "  m7 := c32 EQ m6\r\n" + //
                                "  m8 := m5 LAND m7\r\n" + //
                                "  IF m8 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  m9 := c37 ISUB c36\r\n" + //
                                "  c40 := m9\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  n0 := c36 ISUB c37\r\n" + //
                                "  c40 := n0\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  n1 := 25\r\n" + //
                                "  n2 := c40 IRSHIFT n1\r\n" + //
                                "  n3 := 1\r\n" + //
                                "  n4 := n2 IAND n3\r\n" + //
                                "  c39 := n4\r\n" + //
                                "  n5 := 1\r\n" + //
                                "  n6 := c39 EQ n5\r\n" + //
                                "  IF n6 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  n7 := 1\r\n" + //
                                "  n8 := c38 IADD n7\r\n" + //
                                "  c38 := n8\r\n" + //
                                "  n9 := 1\r\n" + //
                                "  o0 := c40 IRSHIFT n9\r\n" + //
                                "  c40 := o0\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  o1 := 23\r\n" + //
                                "  o2 := c38 ILSHIFT o1\r\n" + //
                                "  c30 := o2\r\n" + //
                                "  o3 := 31\r\n" + //
                                "  o4 := c41 ILSHIFT o3\r\n" + //
                                "  o5 := c30 IOR o4\r\n" + //
                                "  c30 := o5\r\n" + //
                                "  o6 := c30 IOR c40\r\n" + //
                                "  c30 := o6\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_3\r\n" + //
                                "  CALL IntBinaryAsReal ( c30 -> a61 )\r\n" + //
                                "  o7 <| a64\r\n" + //
                                "  c28 := o7\r\n" + //
                                "  c29 |< c28\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  d26 <- d25\r\n" + //
                                "  IPARAM d26\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testTest4(){
        String progSrc = "test_source/test4.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                " b := 0\r\n" + //
                                " c := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL ReadInt (  )\r\n" + //
                                " W0 <| W2\r\n" + //
                                " a := W0\r\n" + //
                                " k := 0\r\n" + //
                                " b := k\r\n" + //
                                " LABEL FORBEG_0_LEVEL_0\r\n" + //
                                " IF b LT a THEN FORLOOP_0_LEVEL_0 ELSE FOREND_0_LEVEL_0\r\n" + //
                                " LABEL FORLOOP_0_LEVEL_0\r\n" + //
                                " CALL F ( b -> W3 )\r\n" + //
                                " l <| W6\r\n" + //
                                " c := l\r\n" + //
                                " CALL WriteInt ( c -> Y9 )\r\n" + //
                                " CALL Fact ( b -> Z1 )\r\n" + //
                                " m <| Z4\r\n" + //
                                " c := m\r\n" + //
                                " CALL WriteInt ( c -> Y9 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " n := 1\r\n" + //
                                " o := b IADD n\r\n" + //
                                " b := o\r\n" + //
                                " GOTO FORBEG_0_LEVEL_0\r\n" + //
                                " LABEL FOREND_0_LEVEL_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL ReadInt\r\n" + //
                                "  W1 := 0\r\n" + //
                                "  IASM \"SWI 3\"\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"STR R0, %a\"\r\n" + //
                                "  W2 |< W1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL F\r\n" + //
                                "  W4 <- W3\r\n" + //
                                "  W7 := 0\r\n" + //
                                "  W5 := 0\r\n" + //
                                "  W8 := 0\r\n" + //
                                "  W9 := W4 EQ W8\r\n" + //
                                "  IF W9 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  X0 := 1\r\n" + //
                                "  W5 := X0\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  X1 := 1\r\n" + //
                                "  X2 := W4 ISUB X1\r\n" + //
                                "  CALL F ( X2 -> W3 )\r\n" + //
                                "  X3 <| W6\r\n" + //
                                "  W7 := X3\r\n" + //
                                "  CALL M ( W7 -> X4 )\r\n" + //
                                "  Y7 <| X7\r\n" + //
                                "  W5 := Y7\r\n" + //
                                "  Y8 := W4 ISUB W5\r\n" + //
                                "  W5 := Y8\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  W6 |< W5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL M\r\n" + //
                                "  X5 <- X4\r\n" + //
                                "  X8 := 0\r\n" + //
                                "  X6 := 0\r\n" + //
                                "  X9 := 0\r\n" + //
                                "  Y0 := X5 EQ X9\r\n" + //
                                "  IF Y0 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Y1 := 0\r\n" + //
                                "  X6 := Y1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Y2 := 1\r\n" + //
                                "  Y3 := X5 ISUB Y2\r\n" + //
                                "  CALL M ( Y3 -> X4 )\r\n" + //
                                "  Y4 <| X7\r\n" + //
                                "  X8 := Y4\r\n" + //
                                "  CALL F ( X8 -> W3 )\r\n" + //
                                "  Y5 <| W6\r\n" + //
                                "  X6 := Y5\r\n" + //
                                "  Y6 := X5 ISUB X6\r\n" + //
                                "  X6 := Y6\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  X7 |< X6\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  Z0 <- Y9\r\n" + //
                                "  IPARAM Z0\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Fact\r\n" + //
                                "  Z2 <- Z1\r\n" + //
                                "  Z5 := 0\r\n" + //
                                "  Z3 := 0\r\n" + //
                                "  Z6 := 0\r\n" + //
                                "  Z7 := Z2 EQ Z6\r\n" + //
                                "  IF Z7 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z8 := 1\r\n" + //
                                "  Z3 := Z8\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z9 := 1\r\n" + //
                                "  a10 := Z2 ISUB Z9\r\n" + //
                                "  CALL Fact ( a10 -> Z1 )\r\n" + //
                                "  a11 <| Z4\r\n" + //
                                "  Z5 := a11\r\n" + //
                                "  a12 := Z2 IMUL Z5\r\n" + //
                                "  Z3 := a12\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  Z4 |< Z3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String progSrc = "test_source/WhileLoopAdvanced.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 10\r\n" + //
                                " b := a\r\n" + //
                                " c := 0\r\n" + //
                                " d := c\r\n" + //
                                " e := 0\r\n" + //
                                " f := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " e := d\r\n" + //
                                " g := e GT b\r\n" + //
                                " IF g EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF g EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " CALL WriteInt ( e -> W0 )\r\n" + //
                                " h := 1\r\n" + //
                                " i := e IADD h\r\n" + //
                                " e := i\r\n" + //
                                " j := e GT b\r\n" + //
                                " g := j\r\n" + //
                                " GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " k := e LT b\r\n" + //
                                " IF k EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                " IF k EQ TRUE THEN WHILESTAT_0_SEQ_1_LEVEL_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILESTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                " f := d\r\n" + //
                                " l := f LT b\r\n" + //
                                " IF l EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_1 ELSE WHILENEXT_2_SEQ_0_LEVEL_1\r\n" + //
                                " LABEL WHILECOND_2_SEQ_0_LEVEL_1\r\n" + //
                                " IF l EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_1 ELSE WHILEEND_2_LEVEL_1\r\n" + //
                                " LABEL WHILESTAT_2_SEQ_0_LEVEL_1\r\n" + //
                                " CALL WriteInt ( f -> W0 )\r\n" + //
                                " m := 1\r\n" + //
                                " n := f IADD m\r\n" + //
                                " f := n\r\n" + //
                                " o := f LT b\r\n" + //
                                " l := o\r\n" + //
                                " GOTO WHILECOND_2_SEQ_0_LEVEL_1\r\n" + //
                                " LABEL WHILENEXT_2_SEQ_0_LEVEL_1\r\n" + //
                                " LABEL WHILEEND_2_LEVEL_1\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL WriteInt ( e -> W0 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " p := 1\r\n" + //
                                " q := e IADD p\r\n" + //
                                " e := q\r\n" + //
                                " r := e LT b\r\n" + //
                                " k := r\r\n" + //
                                " GOTO WHILECOND_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILENEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testWhileLoopBasic(){
        String progSrc = "test_source/WhileLoopBasic.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 10\r\n" + //
                                " b := a\r\n" + //
                                " c := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " d := 0\r\n" + //
                                " c := d\r\n" + //
                                " e := c LT b\r\n" + //
                                " IF e EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " IF e EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " CALL WriteInt ( c -> W0 )\r\n" + //
                                " f := 1\r\n" + //
                                " g := c IADD f\r\n" + //
                                " c := g\r\n" + //
                                " h := c LT b\r\n" + //
                                " e := h\r\n" + //
                                " GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                " LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testSingleConversion(){
        String progSrc = "test_source/SingleConversion.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " a74 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 5\r\n" + //
                                " b := a\r\n" + //
                                " c := 0.0\r\n" + //
                                " a73 := 127\r\n" + //
                                " a74 := a73\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL IntToReal ( b -> Y6 )\r\n" + //
                                " Y5 <| Y9\r\n" + //
                                " c := Y5\r\n" + //
                                " CALL WriteReal ( c -> a88 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  Y7 <- Y6\r\n" + //
                                "  Y8 := 0.0\r\n" + //
                                "  Z0 := 0\r\n" + //
                                "  Z1 := FALSE\r\n" + //
                                "  Z2 := 0\r\n" + //
                                "  Z3 := 0\r\n" + //
                                "  Z4 := 0\r\n" + //
                                "  Z5 := 0\r\n" + //
                                "  Z6 := 0\r\n" + //
                                "  Z7 := 0\r\n" + //
                                "  Z8 := 0\r\n" + //
                                "  Z9 := 0\r\n" + //
                                "  a10 := 0\r\n" + //
                                "  a11 := 0\r\n" + //
                                "  CALL Abs ( Y7 -> a13 )\r\n" + //
                                "  a12 <| a16\r\n" + //
                                "  Z0 := a12\r\n" + //
                                "  Z5 := Z0\r\n" + //
                                "  a27 := 0\r\n" + //
                                "  Z4 := a27\r\n" + //
                                "  CALL IntIsNegative ( Y7 -> a29 )\r\n" + //
                                "  a28 <| a32\r\n" + //
                                "  Z1 := a28\r\n" + //
                                "  CALL IntIsZero ( Z5 -> a43 )\r\n" + //
                                "  a42 <| a46\r\n" + //
                                "  a51 := BNOT a42\r\n" + //
                                "  IF a51 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a51 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a52 := 1\r\n" + //
                                "  a53 := Z5 IAND a52\r\n" + //
                                "  Z6 := a53\r\n" + //
                                "  a54 := 1\r\n" + //
                                "  a55 := Z6 EQ a54\r\n" + //
                                "  IF a55 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  Z3 := Z4\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  a56 := 1\r\n" + //
                                "  a57 := Z4 IADD a56\r\n" + //
                                "  Z4 := a57\r\n" + //
                                "  a58 := 1\r\n" + //
                                "  a59 := Z5 IRSHIFT a58\r\n" + //
                                "  Z5 := a59\r\n" + //
                                "  CALL IntIsZero ( Z5 -> a43 )\r\n" + //
                                "  a60 <| a46\r\n" + //
                                "  a61 := BNOT a60\r\n" + //
                                "  a51 := a61\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a62 := 23\r\n" + //
                                "  a63 := Z3 GT a62\r\n" + //
                                "  IF a63 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a64 := 23\r\n" + //
                                "  a65 := Z3 ISUB a64\r\n" + //
                                "  a66 := Z0 IRSHIFT a65\r\n" + //
                                "  Z0 := a66\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a67 := 23\r\n" + //
                                "  a68 := Z3 LT a67\r\n" + //
                                "  IF a68 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a69 := 23\r\n" + //
                                "  a70 := a69 ISUB Z3\r\n" + //
                                "  a71 := Z0 IRSHIFT a70\r\n" + //
                                "  Z0 := a71\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  a72 := Z3 IADD a74\r\n" + //
                                "  Z8 := a72\r\n" + //
                                "  Z9 := Z0\r\n" + //
                                "  Z2 := Z9\r\n" + //
                                "  a75 := 23\r\n" + //
                                "  a76 := Z8 ILSHIFT a75\r\n" + //
                                "  a77 := Z2 IOR a76\r\n" + //
                                "  Z2 := a77\r\n" + //
                                "  IF Z1 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a78 := 1\r\n" + //
                                "  a79 := 31\r\n" + //
                                "  a80 := a78 ILSHIFT a79\r\n" + //
                                "  a81 := Z2 IOR a80\r\n" + //
                                "  Z2 := a81\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( Z2 -> a82 )\r\n" + //
                                "  a87 <| a85\r\n" + //
                                "  Y8 := a87\r\n" + //
                                "  Y9 |< Y8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Abs\r\n" + //
                                "  a14 <- a13\r\n" + //
                                "  a15 := 0\r\n" + //
                                "  a17 := 0\r\n" + //
                                "  a18 := a14 GE a17\r\n" + //
                                "  IF a18 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a15 := a14\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  CALL INeg ( a14 -> a20 )\r\n" + //
                                "  a19 <| a23\r\n" + //
                                "  a15 := a19\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  a16 |< a15\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  a21 <- a20\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  a24 := INOT a21\r\n" + //
                                "  a25 := 1\r\n" + //
                                "  a26 := a24 IADD a25\r\n" + //
                                "  a22 := a26\r\n" + //
                                "  a23 |< a22\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  a30 <- a29\r\n" + //
                                "  a31 := FALSE\r\n" + //
                                "  a33 := 0\r\n" + //
                                "  a34 := 31\r\n" + //
                                "  a35 := a30 IRSHIFT a34\r\n" + //
                                "  a36 := 1\r\n" + //
                                "  a37 := a35 IAND a36\r\n" + //
                                "  a33 := a37\r\n" + //
                                "  a38 := 0\r\n" + //
                                "  a39 := a33 EQ a38\r\n" + //
                                "  IF a39 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a40 := FALSE\r\n" + //
                                "  a31 := a40\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a41 := TRUE\r\n" + //
                                "  a31 := a41\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  a32 |< a31\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a44 <- a43\r\n" + //
                                "  a45 := FALSE\r\n" + //
                                "  a47 := 0\r\n" + //
                                "  a48 := a44 EQ a47\r\n" + //
                                "  IF a48 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a49 := TRUE\r\n" + //
                                "  a45 := a49\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a50 := FALSE\r\n" + //
                                "  a45 := a50\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  a46 |< a45\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a83 <- a82\r\n" + //
                                "  a84 := 0.0\r\n" + //
                                "  a86 := 0\r\n" + //
                                "  IPARAM a86\r\n" + //
                                "  IPARAM a83\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a86\r\n" + //
                                "  IPARAM a84\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a85 |< a84\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  a89 <- a88\r\n" + //
                                "  IPARAM a89\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }
}
