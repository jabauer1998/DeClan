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
                " CALL IntToReal ( a -] Z1 )\r\n" + //
                " a97 [| Z4\r\n" + //
                " CALL RAdd ( a97 -] a99 , t -] b11 )\r\n" + //
                " a98 [| b14\r\n" + //
                " CALL IntToReal ( s -] Z1 )\r\n" + //
                " c62 [| Z4\r\n" + //
                " CALL RMul ( c62 -] c64 , a98 -] c65 )\r\n" + //
                " c63 [| c66\r\n" + //
                " CALL WriteReal ( c63 -] W2 )\r\n" + //
                " CALL WriteLn (  )\r\n" + //
                " y := 3.1415\r\n" + //
                " CALL p ( b -] e , y -] f )\r\n" + //
                " z [| g\r\n" + //
                " a := z\r\n" + //
                " CALL WriteReal ( d -] W2 )\r\n" + //
                " CALL WriteLn (  )\r\n" + //
                "END\r\n" + //
                "PROC SECTION\r\n" + //
                " PROC LABEL WriteInt\r\n" + //
                "  W1 [- W0\r\n" + //
                "  IPARAM W1\r\n" + //
                "  IASM \"LDR R0, %a\"\r\n" + //
                "  IASM \"SWI 1\"\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL WriteReal\r\n" + //
                "  W3 [- W2\r\n" + //
                "  IPARAM W3\r\n" + //
                "  IASM \"LDR R0, %a\"\r\n" + //
                "  IASM \"SWI 2\"\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL WriteLn\r\n" + //
                "  IASM \"SWI 4\"\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL Divide\r\n" + //
                "  W6 [- W5\r\n" + //
                "  W8 [- W7\r\n" + //
                "  X1 := 0\r\n" + //
                "  W9 := 0\r\n" + //
                "  CALL Div ( W6 -] X2 , W8 -] X4 )\r\n" + //
                "  Y9 [| X7\r\n" + //
                "  X1 := Y9\r\n" + //
                "  CALL IntToReal ( X1 -] Z1 )\r\n" + //
                "  Z0 [| X0\r\n" + //
                "  W9 := Z0\r\n" + //
                "  X0 |[ W9\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL Div\r\n" + //
                "  X3 [- X2\r\n" + //
                "  X5 [- X4\r\n" + //
                "  X8 := 0\r\n" + //
                "  X6 := 0\r\n" + //
                "  X8 := X3\r\n" + //
                "  X9 := 0\r\n" + //
                "  X6 := X9\r\n" + //
                "  Y0 := X8 ISUB X5\r\n" + //
                "  Y1 := 0\r\n" + //
                "  Y2 := Y0 GT Y1\r\n" + //
                "  IF Y2 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  IF Y2 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  Y3 := X8 ISUB X5\r\n" + //
                "  X8 := Y3\r\n" + //
                "  Y4 := 1\r\n" + //
                "  Y5 := X6 IADD Y4\r\n" + //
                "  X6 := Y5\r\n" + //
                "  Y6 := X8 ISUB X5\r\n" + //
                "  Y7 := 0\r\n" + //
                "  Y8 := Y6 GT Y7\r\n" + //
                "  Y2 := Y8\r\n" + //
                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                "  X7 |[ X6\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL IntToReal\r\n" + //
                "  Z2 [- Z1\r\n" + //
                "  Z3 := 0.0\r\n" + //
                "  Z5 := FALSE\r\n" + //
                "  Z6 := 0\r\n" + //
                "  Z7 := 0\r\n" + //
                "  Z8 := 0\r\n" + //
                "  Z9 := 0\r\n" + //
                "  a10 := 0\r\n" + //
                "  a11 := 0\r\n" + //
                "  a12 := 0\r\n" + //
                "  a13 := 0\r\n" + //
                "  a14 := 0\r\n" + //
                "  a15 := 0\r\n" + //
                "  Z9 := Z2\r\n" + //
                "  a16 := 0\r\n" + //
                "  Z8 := a16\r\n" + //
                "  CALL IntIsNegative ( Z2 -] a18 )\r\n" + //
                "  a17 [| Z4\r\n" + //
                "  Z5 := a17\r\n" + //
                "  CALL IntIsZero ( Z9 -] a32 )\r\n" + //
                "  a31 [| Z4\r\n" + //
                "  a40 := BNOT a31\r\n" + //
                "  IF a40 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  IF a40 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  a41 := 1\r\n" + //
                "  a42 := Z9 IAND a41\r\n" + //
                "  a10 := a42\r\n" + //
                "  a43 := 1\r\n" + //
                "  a44 := a10 EQ a43\r\n" + //
                "  IF a44 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  Z7 := Z8\r\n" + //
                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                "  a45 := 1\r\n" + //
                "  a46 := Z8 IADD a45\r\n" + //
                "  Z8 := a46\r\n" + //
                "  a47 := 1\r\n" + //
                "  a48 := Z9 IRSHIFT a47\r\n" + //
                "  Z9 := a48\r\n" + //
                "  CALL IntIsZero ( Z9 -] a32 )\r\n" + //
                "  a49 [| Z4\r\n" + //
                "  a50 := BNOT a49\r\n" + //
                "  a40 := a50\r\n" + //
                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                "  a51 := 23\r\n" + //
                "  a52 := Z7 LT a51\r\n" + //
                "  IF a52 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                "  a53 := 23\r\n" + //
                "  a54 := a53 ISUB Z7\r\n" + //
                "  a11 := a54\r\n" + //
                "  a55 := INOT a11\r\n" + //
                "  a56 := 1\r\n" + //
                "  a57 := a55 IADD a56\r\n" + //
                "  a12 := a57\r\n" + //
                "  a58 := -2147483648\r\n" + //
                "  a59 := INOT a58\r\n" + //
                "  a60 := a12 IAND a59\r\n" + //
                "  a12 := a60\r\n" + //
                "  a61 := 64\r\n" + //
                "  a62 := a12 IOR a61\r\n" + //
                "  a12 := a62\r\n" + //
                "  a63 := 1\r\n" + //
                "  a64 := a63 ILSHIFT Z7\r\n" + //
                "  a65 := 1\r\n" + //
                "  a66 := a64 ISUB a65\r\n" + //
                "  a14 := a66\r\n" + //
                "  a67 := Z2 IAND a14\r\n" + //
                "  a13 := a67\r\n" + //
                "  a68 := a13 ILSHIFT a11\r\n" + //
                "  a13 := a68\r\n" + //
                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                "  a69 := 23\r\n" + //
                "  a70 := Z7 GT a69\r\n" + //
                "  IF a70 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                "  a71 := 23\r\n" + //
                "  a72 := Z7 ISUB a71\r\n" + //
                "  a11 := a72\r\n" + //
                "  a12 := a11\r\n" + //
                "  a73 := 1\r\n" + //
                "  a74 := a73 ILSHIFT Z7\r\n" + //
                "  a75 := 1\r\n" + //
                "  a76 := a74 ISUB a75\r\n" + //
                "  a14 := a76\r\n" + //
                "  a77 := Z2 IAND a14\r\n" + //
                "  a13 := a77\r\n" + //
                "  a78 := a13 IRSHIFT a11\r\n" + //
                "  a13 := a78\r\n" + //
                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                "  a12 := Z7\r\n" + //
                "  a79 := 1\r\n" + //
                "  a80 := a79 ILSHIFT Z7\r\n" + //
                "  a81 := 1\r\n" + //
                "  a82 := a80 ISUB a81\r\n" + //
                "  a14 := a82\r\n" + //
                "  a83 := Z2 IAND a14\r\n" + //
                "  a13 := a83\r\n" + //
                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                "  Z6 := a13\r\n" + //
                "  a84 := 23\r\n" + //
                "  a85 := a12 ILSHIFT a84\r\n" + //
                "  a86 := Z6 IOR a85\r\n" + //
                "  Z6 := a86\r\n" + //
                "  IF Z5 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                "  a87 := 1\r\n" + //
                "  a88 := 31\r\n" + //
                "  a89 := a87 ILSHIFT a88\r\n" + //
                "  a90 := Z6 IOR a89\r\n" + //
                "  Z6 := a90\r\n" + //
                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                "  CALL IntBinaryAsReal ( Z6 -] a91 )\r\n" + //
                "  a96 [| a94\r\n" + //
                "  Z3 := a96\r\n" + //
                "  Z4 |[ Z3\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL IntIsNegative\r\n" + //
                "  a19 [- a18\r\n" + //
                "  a20 := FALSE\r\n" + //
                "  a22 := 0\r\n" + //
                "  a23 := 31\r\n" + //
                "  a24 := a19 IRSHIFT a23\r\n" + //
                "  a25 := 1\r\n" + //
                "  a26 := a24 IAND a25\r\n" + //
                "  a22 := a26\r\n" + //
                "  a27 := 0\r\n" + //
                "  a28 := a22 EQ a27\r\n" + //
                "  IF a28 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                "  a29 := FALSE\r\n" + //
                "  a20 := a29\r\n" + //
                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                "  a30 := TRUE\r\n" + //
                "  a20 := a30\r\n" + //
                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                "  a21 |[ a20\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL IntIsZero\r\n" + //
                "  a33 [- a32\r\n" + //
                "  a34 := FALSE\r\n" + //
                "  a36 := 0\r\n" + //
                "  a37 := a33 EQ a36\r\n" + //
                "  IF a37 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                "  a38 := TRUE\r\n" + //
                "  a34 := a38\r\n" + //
                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                "  a39 := FALSE\r\n" + //
                "  a34 := a39\r\n" + //
                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                "  a35 |[ a34\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL IntBinaryAsReal\r\n" + //
                "  a92 [- a91\r\n" + //
                "  a93 := 0.0\r\n" + //
                "  a95 := 0\r\n" + //
                "  IPARAM a95\r\n" + //
                "  IPARAM a92\r\n" + //
                "  IASM \"LDR %r, %a\"\r\n" + //
                "  IPARAM a95\r\n" + //
                "  IPARAM a93\r\n" + //
                "  IASM \"STR %r, %a\"\r\n" + //
                "  a94 |[ a93\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL RAdd\r\n" + //
                "  b10 [- a99\r\n" + //
                "  b12 [- b11\r\n" + //
                "  b13 := 0.0\r\n" + //
                "  b15 := 0\r\n" + //
                "  b16 := 0\r\n" + //
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
                "  CALL RealSign ( b10 -] b28 )\r\n" + //
                "  b27 [| b14\r\n" + //
                "  b16 := b27\r\n" + //
                "  CALL RealSign ( b12 -] b28 )\r\n" + //
                "  b43 [| b14\r\n" + //
                "  b17 := b43\r\n" + //
                "  CALL RealExponent ( b10 -] b45 )\r\n" + //
                "  b44 [| b14\r\n" + //
                "  b19 := b44\r\n" + //
                "  CALL RealExponent ( b12 -] b45 )\r\n" + //
                "  b55 [| b14\r\n" + //
                "  b20 := b55\r\n" + //
                "  CALL RealMantissa ( b10 -] b57 )\r\n" + //
                "  b56 [| b14\r\n" + //
                "  b21 := b56\r\n" + //
                "  CALL RealMantissa ( b12 -] b57 )\r\n" + //
                "  b65 [| b14\r\n" + //
                "  b22 := b65\r\n" + //
                "  b66 := b16 EQ b17\r\n" + //
                "  IF b66 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  b26 := b16\r\n" + //
                "  b67 := b19 EQ b20\r\n" + //
                "  IF b67 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                "  b68 := b21 IADD b22\r\n" + //
                "  b25 := b68\r\n" + //
                "  b69 := 25\r\n" + //
                "  b70 := b25 IRSHIFT b69\r\n" + //
                "  b71 := 1\r\n" + //
                "  b72 := b70 IAND b71\r\n" + //
                "  b24 := b72\r\n" + //
                "  b23 := b19\r\n" + //
                "  b73 := 1\r\n" + //
                "  b74 := b24 EQ b73\r\n" + //
                "  IF b74 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                "  b75 := 1\r\n" + //
                "  b76 := b23 IADD b75\r\n" + //
                "  b23 := b76\r\n" + //
                "  b77 := 1\r\n" + //
                "  b78 := b25 IRSHIFT b77\r\n" + //
                "  b25 := b78\r\n" + //
                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                "  b79 := 23\r\n" + //
                "  b80 := b23 ILSHIFT b79\r\n" + //
                "  b15 := b80\r\n" + //
                "  b81 := b15 IOR b25\r\n" + //
                "  b15 := b81\r\n" + //
                "  b82 := 31\r\n" + //
                "  b83 := b26 ILSHIFT b82\r\n" + //
                "  b84 := b15 IOR b83\r\n" + //
                "  b15 := b84\r\n" + //
                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                "  b85 := b19 GT b20\r\n" + //
                "  IF b85 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                "  b86 := b19 ISUB b20\r\n" + //
                "  b18 := b86\r\n" + //
                "  b20 := b19\r\n" + //
                "  b23 := b19\r\n" + //
                "  b87 := b22 IRSHIFT b18\r\n" + //
                "  b22 := b87\r\n" + //
                "  b88 := b21 IADD b22\r\n" + //
                "  b25 := b88\r\n" + //
                "  b89 := 25\r\n" + //
                "  b90 := b25 IRSHIFT b89\r\n" + //
                "  b91 := 1\r\n" + //
                "  b92 := b90 IAND b91\r\n" + //
                "  b24 := b92\r\n" + //
                "  b93 := 1\r\n" + //
                "  b94 := b24 EQ b93\r\n" + //
                "  IF b94 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                "  b95 := 1\r\n" + //
                "  b96 := b23 IADD b95\r\n" + //
                "  b23 := b96\r\n" + //
                "  b97 := 1\r\n" + //
                "  b98 := b25 IRSHIFT b97\r\n" + //
                "  b25 := b98\r\n" + //
                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                "  b99 := 23\r\n" + //
                "  c10 := b23 ILSHIFT b99\r\n" + //
                "  b15 := c10\r\n" + //
                "  c11 := 31\r\n" + //
                "  c12 := b26 ILSHIFT c11\r\n" + //
                "  c13 := b15 IOR c12\r\n" + //
                "  b15 := c13\r\n" + //
                "  c14 := b15 IOR b25\r\n" + //
                "  b15 := c14\r\n" + //
                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                "  c15 := b20 ISUB b19\r\n" + //
                "  b18 := c15\r\n" + //
                "  b19 := b20\r\n" + //
                "  b23 := b20\r\n" + //
                "  c16 := b21 IRSHIFT b18\r\n" + //
                "  b21 := c16\r\n" + //
                "  c17 := b21 IADD b22\r\n" + //
                "  b25 := c17\r\n" + //
                "  c18 := 25\r\n" + //
                "  c19 := b25 IRSHIFT c18\r\n" + //
                "  c20 := 1\r\n" + //
                "  c21 := c19 IAND c20\r\n" + //
                "  b24 := c21\r\n" + //
                "  c22 := 1\r\n" + //
                "  c23 := b24 EQ c22\r\n" + //
                "  IF c23 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                "  c24 := 1\r\n" + //
                "  c25 := b23 IADD c24\r\n" + //
                "  b23 := c25\r\n" + //
                "  c26 := 1\r\n" + //
                "  c27 := b25 IRSHIFT c26\r\n" + //
                "  b25 := c27\r\n" + //
                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                "  c28 := 23\r\n" + //
                "  c29 := b23 ILSHIFT c28\r\n" + //
                "  b15 := c29\r\n" + //
                "  c30 := 31\r\n" + //
                "  c31 := b26 ILSHIFT c30\r\n" + //
                "  c32 := b15 IOR c31\r\n" + //
                "  b15 := c32\r\n" + //
                "  c33 := b15 IOR b25\r\n" + //
                "  b15 := c33\r\n" + //
                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                "  c34 := 0\r\n" + //
                "  c35 := b16 EQ c34\r\n" + //
                "  c36 := 1\r\n" + //
                "  c37 := b17 EQ c36\r\n" + //
                "  c38 := c35 LAND c37\r\n" + //
                "  IF c38 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                "  c39 := b22 GT b21\r\n" + //
                "  IF c39 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                "  c40 := 1\r\n" + //
                "  b26 := c40\r\n" + //
                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                "  c41 := 0\r\n" + //
                "  b26 := c41\r\n" + //
                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                "  c42 := 1\r\n" + //
                "  c43 := b16 EQ c42\r\n" + //
                "  c44 := 0\r\n" + //
                "  c45 := b17 EQ c44\r\n" + //
                "  c46 := c43 LAND c45\r\n" + //
                "  IF c46 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                "  c47 := b22 GE b21\r\n" + //
                "  IF c47 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                "  c48 := 0\r\n" + //
                "  b26 := c48\r\n" + //
                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                "  c49 := 1\r\n" + //
                "  b26 := c49\r\n" + //
                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                "  h8 := b19 EQ b20\r\n" + //
                "  IF h8 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                "  h9 := 0\r\n" + //
                "  b25 := h9\r\n" + //
                "  i0 := 25\r\n" + //
                "  i1 := b25 IRSHIFT i0\r\n" + //
                "  i2 := 1\r\n" + //
                "  i3 := i1 IAND i2\r\n" + //
                "  b24 := i3\r\n" + //
                "  b23 := b19\r\n" + //
                "  c50 := 1\r\n" + //
                "  c51 := b24 EQ c50\r\n" + //
                "  IF c51 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                "  c52 := 1\r\n" + //
                "  c53 := b23 IADD c52\r\n" + //
                "  b23 := c53\r\n" + //
                "  c54 := 1\r\n" + //
                "  c55 := b25 IRSHIFT c54\r\n" + //
                "  b25 := c55\r\n" + //
                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                "  c56 := 23\r\n" + //
                "  c57 := b23 ILSHIFT c56\r\n" + //
                "  b15 := c57\r\n" + //
                "  c58 := b15 IOR b25\r\n" + //
                "  b15 := c58\r\n" + //
                "  c59 := 31\r\n" + //
                "  c60 := b26 ILSHIFT c59\r\n" + //
                "  c61 := b15 IOR c60\r\n" + //
                "  b15 := c61\r\n" + //
                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                "  j6 := b19 GT b20\r\n" + //
                "  IF j6 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                "  j7 := b19 ISUB b20\r\n" + //
                "  b18 := j7\r\n" + //
                "  b20 := b19\r\n" + //
                "  b23 := b19\r\n" + //
                "  j8 := b22 IRSHIFT b18\r\n" + //
                "  b22 := j8\r\n" + //
                "  j9 := 1\r\n" + //
                "  k0 := b16 EQ j9\r\n" + //
                "  k1 := 0\r\n" + //
                "  k2 := b17 EQ k1\r\n" + //
                "  k3 := k0 LAND k2\r\n" + //
                "  IF k3 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                "  k4 := b22 ISUB b21\r\n" + //
                "  b25 := k4\r\n" + //
                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                "  k5 := b21 ISUB b22\r\n" + //
                "  b25 := k5\r\n" + //
                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                "  k6 := 25\r\n" + //
                "  k7 := b25 IRSHIFT k6\r\n" + //
                "  k8 := 1\r\n" + //
                "  k9 := k7 IAND k8\r\n" + //
                "  b24 := k9\r\n" + //
                "  l0 := 1\r\n" + //
                "  l1 := b24 EQ l0\r\n" + //
                "  IF l1 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                "  l2 := 1\r\n" + //
                "  l3 := b23 IADD l2\r\n" + //
                "  b23 := l3\r\n" + //
                "  l4 := 1\r\n" + //
                "  l5 := b25 IRSHIFT l4\r\n" + //
                "  b25 := l5\r\n" + //
                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                "  l6 := 23\r\n" + //
                "  l7 := b23 ILSHIFT l6\r\n" + //
                "  b15 := l7\r\n" + //
                "  l8 := 31\r\n" + //
                "  l9 := b26 ILSHIFT l8\r\n" + //
                "  m0 := b15 IOR l9\r\n" + //
                "  b15 := m0\r\n" + //
                "  m1 := b15 IOR b25\r\n" + //
                "  b15 := m1\r\n" + //
                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                "  m2 := b20 ISUB b19\r\n" + //
                "  b18 := m2\r\n" + //
                "  b19 := b20\r\n" + //
                "  b23 := b20\r\n" + //
                "  m3 := b21 IRSHIFT b18\r\n" + //
                "  b21 := m3\r\n" + //
                "  m4 := 1\r\n" + //
                "  m5 := b16 EQ m4\r\n" + //
                "  m6 := 0\r\n" + //
                "  m7 := b17 EQ m6\r\n" + //
                "  m8 := m5 LAND m7\r\n" + //
                "  IF m8 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                "  m9 := b22 ISUB b21\r\n" + //
                "  b25 := m9\r\n" + //
                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                "  n0 := b21 ISUB b22\r\n" + //
                "  b25 := n0\r\n" + //
                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                "  n1 := 25\r\n" + //
                "  n2 := b25 IRSHIFT n1\r\n" + //
                "  n3 := 1\r\n" + //
                "  n4 := n2 IAND n3\r\n" + //
                "  b24 := n4\r\n" + //
                "  n5 := 1\r\n" + //
                "  n6 := b24 EQ n5\r\n" + //
                "  IF n6 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                "  n7 := 1\r\n" + //
                "  n8 := b23 IADD n7\r\n" + //
                "  b23 := n8\r\n" + //
                "  n9 := 1\r\n" + //
                "  o0 := b25 IRSHIFT n9\r\n" + //
                "  b25 := o0\r\n" + //
                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                "  o1 := 23\r\n" + //
                "  o2 := b23 ILSHIFT o1\r\n" + //
                "  b15 := o2\r\n" + //
                "  o3 := 31\r\n" + //
                "  o4 := b26 ILSHIFT o3\r\n" + //
                "  o5 := b15 IOR o4\r\n" + //
                "  b15 := o5\r\n" + //
                "  o6 := b15 IOR b25\r\n" + //
                "  b15 := o6\r\n" + //
                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                "  CALL IntBinaryAsReal ( b15 -] a91 )\r\n" + //
                "  o7 [| b14\r\n" + //
                "  b13 := o7\r\n" + //
                "  b14 |[ b13\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL RealSign\r\n" + //
                "  b29 [- b28\r\n" + //
                "  b30 := 0\r\n" + //
                "  b32 := 0\r\n" + //
                "  CALL RealBinaryAsInt ( b29 -] b34 )\r\n" + //
                "  b33 [| b31\r\n" + //
                "  b32 := b33\r\n" + //
                "  b39 := 31\r\n" + //
                "  b40 := b32 IRSHIFT b39\r\n" + //
                "  b41 := 1\r\n" + //
                "  b42 := b40 IAND b41\r\n" + //
                "  b30 := b42\r\n" + //
                "  b31 |[ b30\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL RealBinaryAsInt\r\n" + //
                "  b35 [- b34\r\n" + //
                "  b36 := 0\r\n" + //
                "  b38 := 0.0\r\n" + //
                "  IPARAM b38\r\n" + //
                "  IPARAM b35\r\n" + //
                "  IASM \"LDR %r, %a\"\r\n" + //
                "  IPARAM b38\r\n" + //
                "  IPARAM b36\r\n" + //
                "  IASM \"STR %r, %a\"\r\n" + //
                "  b37 |[ b36\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL RealExponent\r\n" + //
                "  b46 [- b45\r\n" + //
                "  b47 := 0\r\n" + //
                "  b49 := 0\r\n" + //
                "  CALL RealBinaryAsInt ( b46 -] b34 )\r\n" + //
                "  b50 [| b48\r\n" + //
                "  b49 := b50\r\n" + //
                "  b51 := 23\r\n" + //
                "  b52 := b49 IRSHIFT b51\r\n" + //
                "  b53 := 255\r\n" + //
                "  b54 := b52 IAND b53\r\n" + //
                "  b47 := b54\r\n" + //
                "  b48 |[ b47\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL RealMantissa\r\n" + //
                "  b58 [- b57\r\n" + //
                "  b59 := 0\r\n" + //
                "  b61 := 0\r\n" + //
                "  CALL RealBinaryAsInt ( b58 -] b34 )\r\n" + //
                "  b62 [| b60\r\n" + //
                "  b61 := b62\r\n" + //
                "  b63 := 8388607\r\n" + //
                "  b64 := b61 IAND b63\r\n" + //
                "  b59 := b64\r\n" + //
                "  b60 |[ b59\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL RMul\r\n" + //
                "  q4 [- c64\r\n" + //
                "  q6 [- c65\r\n" + //
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
                "  CALL RealSign ( q4 -] b28 )\r\n" + //
                "  q5 [| c66\r\n" + //
                "  p5 := q5\r\n" + //
                "  CALL RealSign ( q6 -] b28 )\r\n" + //
                "  q7 [| c66\r\n" + //
                "  p6 := q7\r\n" + //
                "  CALL RealExponent ( q4 -] b45 )\r\n" + //
                "  q8 [| c66\r\n" + //
                "  p7 := q8\r\n" + //
                "  CALL RealExponent ( q6 -] b45 )\r\n" + //
                "  q9 [| c66\r\n" + //
                "  p8 := q9\r\n" + //
                "  CALL RealMantissa ( q4 -] b57 )\r\n" + //
                "  r0 [| c66\r\n" + //
                "  p9 := r0\r\n" + //
                "  CALL RealMantissa ( q6 -] b57 )\r\n" + //
                "  r1 [| c66\r\n" + //
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
                "  CALL IntBinaryAsReal ( p4 -] a91 )\r\n" + //
                "  s3 [| c66\r\n" + //
                "  p3 := s3\r\n" + //
                "  c66 |[ p3\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL p\r\n" + //
                "  c68 [- c67\r\n" + //
                "  c70 [- c69\r\n" + //
                "  c71 := 0\r\n" + //
                "  CALL IntToReal ( c70 -] Z1 )\r\n" + //
                "  c73 [| c72\r\n" + //
                "  CALL RAdd ( c68 -] a99 , c73 -] b11 )\r\n" + //
                "  c74 [| c72\r\n" + //
                "  CALL Round ( c74 -] c76 )\r\n" + //
                "  c75 [| c72\r\n" + //
                "  c71 := c75\r\n" + //
                "  c72 |[ c71\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL Round\r\n" + //
                "  c77 [- c76\r\n" + //
                "  c80 := 0.5\r\n" + //
                "  CALL RAdd ( c77 -] a99 , c80 -] b11 )\r\n" + //
                "  c81 [| c79\r\n" + //
                "  CALL Floor ( c81 -] c82 )\r\n" + //
                "  c78 [| c84\r\n" + //
                "  c79 |[ c78\r\n" + //
                " RETURN\r\n" + //
                " PROC LABEL Floor\r\n" + //
                "  c83 [- c82\r\n" + //
                "  c84 |[ c83\r\n" + //
                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }
}
