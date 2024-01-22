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
                                " CALL IntToReal ( a -] a11 )\r\n" + //
                                " b53 [| a14\r\n" + //
                                " CALL RAdd ( b53 -] b55 , t -] b57 )\r\n" + //
                                " b54 [| b60\r\n" + //
                                " CALL IntToReal ( s -] a11 )\r\n" + //
                                " e71 [| a14\r\n" + //
                                " CALL RMul ( e71 -] e73 , b54 -] e75 )\r\n" + //
                                " e72 [| e78\r\n" + //
                                " CALL WriteReal ( e72 -] W2 )\r\n" + //
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
                                "IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  W3 [- W2\r\n" + //
                                "  IPARAM W3\r\n" + //
                                "IASM \"LDR R0, %a\"\r\n" + //
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
                                "  Y9 [| a9\r\n" + //
                                "  X1 := Y9\r\n" + //
                                "  CALL IntToReal ( X1 -] a11 )\r\n" + //
                                "  a10 [| X0\r\n" + //
                                "  W9 := a10\r\n" + //
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
                                "  a9 |[ X6\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  a12 [- a11\r\n" + //
                                "  b52 := 0.0\r\n" + //
                                "  a48 := FALSE\r\n" + //
                                "  b44 := 0\r\n" + //
                                "  a67 := 0\r\n" + //
                                "  a70 := 0\r\n" + //
                                "  a73 := 0\r\n" + //
                                "  a64 := 0\r\n" + //
                                "  b16 := 0\r\n" + //
                                "  b27 := 0\r\n" + //
                                "  b34 := 0\r\n" + //
                                "  b32 := 0\r\n" + //
                                "  a26 := 0\r\n" + //
                                "  a73 := a12\r\n" + //
                                "  a28 := 0\r\n" + //
                                "  a70 := a28\r\n" + //
                                "  CALL IntIsNegative ( a12 -] a31 )\r\n" + //
                                "  a30 [| a14\r\n" + //
                                "  a48 := a30\r\n" + //
                                "  CALL IntIsZero ( a27 -] a50 )\r\n" + //
                                "  a49 [| a14\r\n" + //
                                "  a76 := BNOT a49\r\n" + //
                                "  IF a76 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1_0\r\n" + //
                                "  IF a76 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1_0_0 ELSE WHILEEND_0_LEVEL_0_1_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1_0_0\r\n" + //
                                "  a62 := 1\r\n" + //
                                "  a63 := a73 IAND a62\r\n" + //
                                "  a64 := a63\r\n" + //
                                "  a65 := 1\r\n" + //
                                "  a66 := a64 EQ a65\r\n" + //
                                "  IF a66 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  a67 := a70\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0_0\r\n" + //
                                "  a68 := 1\r\n" + //
                                "  a69 := a70 IADD a68\r\n" + //
                                "  a70 := a69\r\n" + //
                                "  a71 := 1\r\n" + //
                                "  a72 := a73 IRSHIFT a71\r\n" + //
                                "  a73 := a72\r\n" + //
                                "  CALL IntIsZero ( a73 -] a50 )\r\n" + //
                                "  a74 [| a14\r\n" + //
                                "  a75 := BNOT a74\r\n" + //
                                "  a76 := a75\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1_0\r\n" + //
                                "  a77 := 23\r\n" + //
                                "  a78 := a67 LT a77\r\n" + //
                                "  IF a78 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  a79 := 23\r\n" + //
                                "  a80 := a79 ISUB a67\r\n" + //
                                "  b16 := a80\r\n" + //
                                "  a82 := INOT b16\r\n" + //
                                "  a83 := 1\r\n" + //
                                "  a84 := a82 IADD a83\r\n" + //
                                "  b27 := a84\r\n" + //
                                "  a86 := -2147483648\r\n" + //
                                "  a87 := INOT a86\r\n" + //
                                "  a88 := b27 IAND a87\r\n" + //
                                "  b27 := a88\r\n" + //
                                "  a90 := 64\r\n" + //
                                "  a91 := b27 IOR a90\r\n" + //
                                "  b27 := a91\r\n" + //
                                "  a93 := 1\r\n" + //
                                "  a94 := a93 ILSHIFT a67\r\n" + //
                                "  a95 := 1\r\n" + //
                                "  a96 := a94 ISUB a95\r\n" + //
                                "  b32 := a96\r\n" + //
                                "  a98 := a12 IAND b32\r\n" + //
                                "  b34 := a98\r\n" + //
                                "  b10 := b34 ILSHIFT b16\r\n" + //
                                "  b34 := b10\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  b12 := 23\r\n" + //
                                "  b13 := a67 GT b12\r\n" + //
                                "  IF b13 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_1_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_0_0\r\n" + //
                                "  b14 := 23\r\n" + //
                                "  b15 := a67 ISUB b14\r\n" + //
                                "  b16 := b15\r\n" + //
                                "  b27 := b16\r\n" + //
                                "  b18 := 1\r\n" + //
                                "  b19 := b18 ILSHIFT a67\r\n" + //
                                "  b20 := 1\r\n" + //
                                "  b21 := b19 ISUB b20\r\n" + //
                                "  b32 := b21\r\n" + //
                                "  b23 := a12 IAND b32\r\n" + //
                                "  b34 := b23\r\n" + //
                                "  b25 := b34 IRSHIFT b16\r\n" + //
                                "  b34 := b25\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_1_0\r\n" + //
                                "  b27 := a67\r\n" + //
                                "  b28 := 1\r\n" + //
                                "  b29 := b28 ILSHIFT a67\r\n" + //
                                "  b30 := 1\r\n" + //
                                "  b31 := b29 ISUB b30\r\n" + //
                                "  b32 := b31\r\n" + //
                                "  b33 := a12 IAND b32\r\n" + //
                                "  b34 := b33\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0_0_1_0\r\n" + //
                                "  b44 := b34\r\n" + //
                                "  b36 := 23\r\n" + //
                                "  b37 := b27 ILSHIFT b36\r\n" + //
                                "  b38 := b44 IOR b37\r\n" + //
                                "  b44 := b38\r\n" + //
                                "  IF a48 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  b40 := 1\r\n" + //
                                "  b41 := 31\r\n" + //
                                "  b42 := b40 ILSHIFT b41\r\n" + //
                                "  b43 := b44 IOR b42\r\n" + //
                                "  b44 := b43\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0_0\r\n" + //
                                "  CALL IntBinaryAsReal ( b44 -] b45 )\r\n" + //
                                "  b51 [| b48\r\n" + //
                                "  b52 := b51\r\n" + //
                                "  a14 |[ b52\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  a32 [- a31\r\n" + //
                                "  a47 := FALSE\r\n" + //
                                "  a41 := 0\r\n" + //
                                "  a37 := 31\r\n" + //
                                "  a38 := a32 IRSHIFT a37\r\n" + //
                                "  a39 := 1\r\n" + //
                                "  a40 := a38 IAND a39\r\n" + //
                                "  a41 := a40\r\n" + //
                                "  a42 := 0\r\n" + //
                                "  a43 := a41 EQ a42\r\n" + //
                                "  IF a43 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  a44 := FALSE\r\n" + //
                                "  a47 := a44\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  a46 := TRUE\r\n" + //
                                "  a47 := a46\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0_0_0\r\n" + //
                                "  a34 |[ a47\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a51 [- a50\r\n" + //
                                "  a60 := FALSE\r\n" + //
                                "  a55 := 0\r\n" + //
                                "  a56 := a51 EQ a55\r\n" + //
                                "  IF a56 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  a57 := TRUE\r\n" + //
                                "  a60 := a57\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  a59 := FALSE\r\n" + //
                                "  a60 := a59\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0_0_0\r\n" + //
                                "  a53 |[ a60\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  b46 [- b45\r\n" + //
                                "  b49 := 0.0\r\n" + //
                                "  b50 := 0\r\n" + //
                                "  IPARAM b50\r\n" + //
                                "IPARAM b46\r\n" + //
                                "IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b50\r\n" + //
                                "IPARAM b49\r\n" + //
                                "IASM \"STR %r, %a\"\r\n" + //
                                "  b48 |[ b49\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  b56 [- b55\r\n" + //
                                "  b58 [- b57\r\n" + //
                                "  e70 := 0.0\r\n" + //
                                "  e68 := 0\r\n" + //
                                "  b94 := 0\r\n" + //
                                "  b96 := 0\r\n" + //
                                "  e33 := 0\r\n" + //
                                "  e34 := 0\r\n" + //
                                "  d87 := 0\r\n" + //
                                "  e37 := 0\r\n" + //
                                "  d90 := 0\r\n" + //
                                "  e56 := 0\r\n" + //
                                "  e51 := 0\r\n" + //
                                "  e59 := 0\r\n" + //
                                "  d57 := 0\r\n" + //
                                "  CALL RealSign ( b56 -] b75 )\r\n" + //
                                "  b74 [| b60\r\n" + //
                                "  b94 := b74\r\n" + //
                                "  CALL RealSign ( b58 -] b75 )\r\n" + //
                                "  b95 [| b60\r\n" + //
                                "  b96 := b95\r\n" + //
                                "  CALL RealExponent ( b56 -] b98 )\r\n" + //
                                "  b97 [| b60\r\n" + //
                                "  e34 := b97\r\n" + //
                                "  CALL RealExponent ( b58 -] b98 )\r\n" + //
                                "  c22 [| b60\r\n" + //
                                "  d87 := c22\r\n" + //
                                "  CALL RealMantissa ( b56 -] c25 )\r\n" + //
                                "  c24 [| b60\r\n" + //
                                "  e37 := c24\r\n" + //
                                "  CALL RealMantissa ( b58 -] c25 )\r\n" + //
                                "  c37 [| b60\r\n" + //
                                "  d90 := c37\r\n" + //
                                "  c39 := b94 EQ b96\r\n" + //
                                "  IF c39 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  d57 := b94\r\n" + //
                                "  c41 := e34 EQ d87\r\n" + //
                                "  IF c41 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_1_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1_0_0\r\n" + //
                                "  c42 := e37 IADD d90\r\n" + //
                                "  e59 := c42\r\n" + //
                                "  c44 := 25\r\n" + //
                                "  c45 := e59 IRSHIFT c44\r\n" + //
                                "  c46 := 1\r\n" + //
                                "  c47 := c45 IAND c46\r\n" + //
                                "  e51 := c47\r\n" + //
                                "  e56 := e34\r\n" + //
                                "  c50 := 1\r\n" + //
                                "  c51 := e51 EQ c50\r\n" + //
                                "  IF c51 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  c52 := 1\r\n" + //
                                "  c53 := e56 IADD c52\r\n" + //
                                "  e56 := c53\r\n" + //
                                "  c55 := 1\r\n" + //
                                "  c56 := e59 IRSHIFT c55\r\n" + //
                                "  e59 := c56\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2_0_0\r\n" + //
                                "  c58 := 23\r\n" + //
                                "  c59 := e56 ILSHIFT c58\r\n" + //
                                "  e68 := c59\r\n" + //
                                "  c61 := e68 IOR e59\r\n" + //
                                "  e68 := c61\r\n" + //
                                "  c63 := 31\r\n" + //
                                "  c64 := d57 ILSHIFT c63\r\n" + //
                                "  c65 := e68 IOR c64\r\n" + //
                                "  e68 := c65\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1_0_0\r\n" + //
                                "  c67 := e34 GT d87\r\n" + //
                                "  IF c67 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_1_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1_0_0\r\n" + //
                                "  c68 := e34 ISUB d87\r\n" + //
                                "  e33 := c68\r\n" + //
                                "  d87 := e34\r\n" + //
                                "  e56 := e34\r\n" + //
                                "  c72 := d90 IRSHIFT e33\r\n" + //
                                "  d90 := c72\r\n" + //
                                "  c74 := e37 IADD d90\r\n" + //
                                "  e59 := c74\r\n" + //
                                "  c76 := 25\r\n" + //
                                "  c77 := e59 IRSHIFT c76\r\n" + //
                                "  c78 := 1\r\n" + //
                                "  c79 := c77 IAND c78\r\n" + //
                                "  e51 := c79\r\n" + //
                                "  c81 := 1\r\n" + //
                                "  c82 := e51 EQ c81\r\n" + //
                                "  IF c82 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_4_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  c83 := 1\r\n" + //
                                "  c84 := e56 IADD c83\r\n" + //
                                "  e56 := c84\r\n" + //
                                "  c86 := 1\r\n" + //
                                "  c87 := e59 IRSHIFT c86\r\n" + //
                                "  e59 := c87\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2_0_0\r\n" + //
                                "  c89 := 23\r\n" + //
                                "  c90 := e56 ILSHIFT c89\r\n" + //
                                "  e68 := c90\r\n" + //
                                "  c92 := 31\r\n" + //
                                "  c93 := d57 ILSHIFT c92\r\n" + //
                                "  c94 := e68 IOR c93\r\n" + //
                                "  e68 := c94\r\n" + //
                                "  c96 := e68 IOR e59\r\n" + //
                                "  e68 := c96\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1_0_0\r\n" + //
                                "  c98 := d87 ISUB e34\r\n" + //
                                "  e33 := c98\r\n" + //
                                "  e34 := d87\r\n" + //
                                "  e56 := d87\r\n" + //
                                "  d12 := e37 IRSHIFT e33\r\n" + //
                                "  e37 := d12\r\n" + //
                                "  d14 := e37 IADD d90\r\n" + //
                                "  e59 := d14\r\n" + //
                                "  d16 := 25\r\n" + //
                                "  d17 := e59 IRSHIFT d16\r\n" + //
                                "  d18 := 1\r\n" + //
                                "  d19 := d17 IAND d18\r\n" + //
                                "  e51 := d19\r\n" + //
                                "  d21 := 1\r\n" + //
                                "  d22 := e51 EQ d21\r\n" + //
                                "  IF d22 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_6_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  d23 := 1\r\n" + //
                                "  d24 := e56 IADD d23\r\n" + //
                                "  e56 := d24\r\n" + //
                                "  d26 := 1\r\n" + //
                                "  d27 := e59 IRSHIFT d26\r\n" + //
                                "  e59 := d27\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2_0_0\r\n" + //
                                "  d29 := 23\r\n" + //
                                "  d30 := e56 ILSHIFT d29\r\n" + //
                                "  e68 := d30\r\n" + //
                                "  d32 := 31\r\n" + //
                                "  d33 := d57 ILSHIFT d32\r\n" + //
                                "  d34 := e68 IOR d33\r\n" + //
                                "  e68 := d34\r\n" + //
                                "  d36 := e68 IOR e59\r\n" + //
                                "  e68 := d36\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1_0_0_0_0\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0_1_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0_1\r\n" + //
                                "  d38 := 0\r\n" + //
                                "  d39 := b94 EQ d38\r\n" + //
                                "  d40 := 1\r\n" + //
                                "  d41 := b96 EQ d40\r\n" + //
                                "  d42 := d39 LAND d41\r\n" + //
                                "  IF d42 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1_0_0 ELSE IFNEXT_9_SEQ_0_LEVEL_1_0_0\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1_0_0\r\n" + //
                                "  d43 := d90 GT e37\r\n" + //
                                "  IF d43 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_10_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  d44 := 1\r\n" + //
                                "  d57 := d44\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2_0_0_0\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  d46 := 0\r\n" + //
                                "  d57 := d46\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2_0_0_0\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2_0\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2_0_0_0\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1_0_0_0\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1_0_0\r\n" + //
                                "  d48 := 1\r\n" + //
                                "  d49 := b94 EQ d48\r\n" + //
                                "  d50 := 0\r\n" + //
                                "  d51 := b96 EQ d50\r\n" + //
                                "  d52 := d49 LAND d51\r\n" + //
                                "  IF d52 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_12_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  d53 := d90 GE e37\r\n" + //
                                "  IF d53 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_13_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  d54 := 0\r\n" + //
                                "  d57 := d54\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3_0_0_0\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  d56 := 1\r\n" + //
                                "  d57 := d56\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3_0_0_0\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3_0\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3_0_0_0\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2_0_0\r\n" + //
                                "  d58 := e34 EQ d87\r\n" + //
                                "  IF d58 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2_0_0 ELSE IFNEXT_15_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  d59 := 0\r\n" + //
                                "  e59 := d59\r\n" + //
                                "  d61 := 25\r\n" + //
                                "  d62 := e59 IRSHIFT d61\r\n" + //
                                "  d63 := 1\r\n" + //
                                "  d64 := d62 IAND d63\r\n" + //
                                "  e51 := d64\r\n" + //
                                "  e56 := e34\r\n" + //
                                "  d67 := 1\r\n" + //
                                "  d68 := e51 EQ d67\r\n" + //
                                "  IF d68 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_16_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  d69 := 1\r\n" + //
                                "  d70 := e56 IADD d69\r\n" + //
                                "  e56 := d70\r\n" + //
                                "  d72 := 1\r\n" + //
                                "  d73 := e59 IRSHIFT d72\r\n" + //
                                "  e59 := d73\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3_0_0\r\n" + //
                                "  d75 := 23\r\n" + //
                                "  d76 := e56 ILSHIFT d75\r\n" + //
                                "  e68 := d76\r\n" + //
                                "  d78 := e68 IOR e59\r\n" + //
                                "  e68 := d78\r\n" + //
                                "  d80 := 31\r\n" + //
                                "  d81 := d57 ILSHIFT d80\r\n" + //
                                "  d82 := e68 IOR d81\r\n" + //
                                "  e68 := d82\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2_0_0\r\n" + //
                                "  d84 := e34 GT d87\r\n" + //
                                "  IF d84 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2_0_0 ELSE IFNEXT_15_SEQ_1_LEVEL_2_0_0\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2_0_0\r\n" + //
                                "  d85 := e34 ISUB d87\r\n" + //
                                "  e33 := d85\r\n" + //
                                "  d87 := e34\r\n" + //
                                "  e56 := e34\r\n" + //
                                "  d89 := d90 IRSHIFT e33\r\n" + //
                                "  d90 := d89\r\n" + //
                                "  d91 := 1\r\n" + //
                                "  d92 := b94 EQ d91\r\n" + //
                                "  d93 := 0\r\n" + //
                                "  d94 := b96 EQ d93\r\n" + //
                                "  d95 := d92 LAND d94\r\n" + //
                                "  IF d95 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_18_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  d96 := d90 ISUB e37\r\n" + //
                                "  e59 := d96\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3_0_0_0\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  d98 := e37 ISUB d90\r\n" + //
                                "  e59 := d98\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3_0_0_0\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3_0\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3_0_0_0\r\n" + //
                                "  e10 := 25\r\n" + //
                                "  e11 := e59 IRSHIFT e10\r\n" + //
                                "  e12 := 1\r\n" + //
                                "  e13 := e11 IAND e12\r\n" + //
                                "  e51 := e13\r\n" + //
                                "  e15 := 1\r\n" + //
                                "  e16 := e51 EQ e15\r\n" + //
                                "  IF e16 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_19_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  e17 := 1\r\n" + //
                                "  e18 := e56 IADD e17\r\n" + //
                                "  e56 := e18\r\n" + //
                                "  e20 := 1\r\n" + //
                                "  e21 := e59 IRSHIFT e20\r\n" + //
                                "  e59 := e21\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3_0_0\r\n" + //
                                "  e23 := 23\r\n" + //
                                "  e24 := e56 ILSHIFT e23\r\n" + //
                                "  e68 := e24\r\n" + //
                                "  e26 := 31\r\n" + //
                                "  e27 := d57 ILSHIFT e26\r\n" + //
                                "  e28 := e68 IOR e27\r\n" + //
                                "  e68 := e28\r\n" + //
                                "  e30 := e68 IOR e59\r\n" + //
                                "  e68 := e30\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2_0_0\r\n" + //
                                "  e32 := d87 ISUB e34\r\n" + //
                                "  e33 := e32\r\n" + //
                                "  e34 := d87\r\n" + //
                                "  e56 := d87\r\n" + //
                                "  e36 := e37 IRSHIFT e33\r\n" + //
                                "  e37 := e36\r\n" + //
                                "  e38 := 1\r\n" + //
                                "  e39 := b94 EQ e38\r\n" + //
                                "  e40 := 0\r\n" + //
                                "  e41 := b96 EQ e40\r\n" + //
                                "  e42 := e39 LAND e41\r\n" + //
                                "  IF e42 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_21_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  e43 := d90 ISUB e37\r\n" + //
                                "  e59 := e43\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3_0_0_0\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  e45 := e37 ISUB d90\r\n" + //
                                "  e59 := e45\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3_0_0_0\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3_0\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3_0_0_0\r\n" + //
                                "  e47 := 25\r\n" + //
                                "  e48 := e59 IRSHIFT e47\r\n" + //
                                "  e49 := 1\r\n" + //
                                "  e50 := e48 IAND e49\r\n" + //
                                "  e51 := e50\r\n" + //
                                "  e52 := 1\r\n" + //
                                "  e53 := e51 EQ e52\r\n" + //
                                "  IF e53 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3_0_0 ELSE IFNEXT_22_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  e54 := 1\r\n" + //
                                "  e55 := e56 IADD e54\r\n" + //
                                "  e56 := e55\r\n" + //
                                "  e57 := 1\r\n" + //
                                "  e58 := e59 IRSHIFT e57\r\n" + //
                                "  e59 := e58\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3_0_0\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3_0_0\r\n" + //
                                "  e60 := 23\r\n" + //
                                "  e61 := e56 ILSHIFT e60\r\n" + //
                                "  e68 := e61\r\n" + //
                                "  e63 := 31\r\n" + //
                                "  e64 := d57 ILSHIFT e63\r\n" + //
                                "  e65 := e68 IOR e64\r\n" + //
                                "  e68 := e65\r\n" + //
                                "  e67 := e68 IOR e59\r\n" + //
                                "  e68 := e67\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2_0\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2_0_0_0_0\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1_0_0_0\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1_0\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1_0_0_0\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0_1_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0_1_0\r\n" + //
                                "  CALL IntBinaryAsReal ( e68 -] b45 )\r\n" + //
                                "  e69 [| b60\r\n" + //
                                "  e70 := e69\r\n" + //
                                "  b60 |[ e70\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  b76 [- b75\r\n" + //
                                "  b93 := 0\r\n" + //
                                "  b88 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b76 -] b82 )\r\n" + //
                                "  b81 [| b78\r\n" + //
                                "  b88 := b81\r\n" + //
                                "  b89 := 31\r\n" + //
                                "  b90 := b88 IRSHIFT b89\r\n" + //
                                "  b91 := 1\r\n" + //
                                "  b92 := b90 IAND b91\r\n" + //
                                "  b93 := b92\r\n" + //
                                "  b78 |[ b93\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  b83 [- b82\r\n" + //
                                "  b86 := 0\r\n" + //
                                "  b87 := 0.0\r\n" + //
                                "  IPARAM b87\r\n" + //
                                "IPARAM b83\r\n" + //
                                "IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b87\r\n" + //
                                "IPARAM b86\r\n" + //
                                "IASM \"STR %r, %a\"\r\n" + //
                                "  b85 |[ b86\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  b99 [- b98\r\n" + //
                                "  c20 := 0\r\n" + //
                                "  c15 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b99 -] b82 )\r\n" + //
                                "  c14 [| c11\r\n" + //
                                "  c15 := c14\r\n" + //
                                "  c16 := 23\r\n" + //
                                "  c17 := c15 IRSHIFT c16\r\n" + //
                                "  c18 := 255\r\n" + //
                                "  c19 := c17 IAND c18\r\n" + //
                                "  c20 := c19\r\n" + //
                                "  c11 |[ c20\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  c26 [- c25\r\n" + //
                                "  c35 := 0\r\n" + //
                                "  c32 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( c26 -] b82 )\r\n" + //
                                "  c31 [| c28\r\n" + //
                                "  c32 := c31\r\n" + //
                                "  c33 := 8388607\r\n" + //
                                "  c34 := c32 IAND c33\r\n" + //
                                "  c35 := c34\r\n" + //
                                "  c28 |[ c35\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  e74 [- e73\r\n" + //
                                "  e76 [- e75\r\n" + //
                                "  f31 := 0.0\r\n" + //
                                "  f29 := 0\r\n" + //
                                "  e91 := 0\r\n" + //
                                "  e93 := 0\r\n" + //
                                "  e95 := 0\r\n" + //
                                "  e97 := 0\r\n" + //
                                "  e99 := 0\r\n" + //
                                "  f11 := 0\r\n" + //
                                "  f16 := 0\r\n" + //
                                "  f18 := 0\r\n" + //
                                "  f20 := 0\r\n" + //
                                "  CALL RealSign ( e74 -] b75 )\r\n" + //
                                "  e90 [| e78\r\n" + //
                                "  e91 := e90\r\n" + //
                                "  CALL RealSign ( e76 -] b75 )\r\n" + //
                                "  e92 [| e78\r\n" + //
                                "  e93 := e92\r\n" + //
                                "  CALL RealExponent ( e74 -] b98 )\r\n" + //
                                "  e94 [| e78\r\n" + //
                                "  e95 := e94\r\n" + //
                                "  CALL RealExponent ( e76 -] b98 )\r\n" + //
                                "  e96 [| e78\r\n" + //
                                "  e97 := e96\r\n" + //
                                "  CALL RealMantissa ( e74 -] c25 )\r\n" + //
                                "  e98 [| e78\r\n" + //
                                "  e99 := e98\r\n" + //
                                "  CALL RealMantissa ( e76 -] c25 )\r\n" + //
                                "  f10 [| e78\r\n" + //
                                "  f11 := f10\r\n" + //
                                "  f12 := e91 NE e93\r\n" + //
                                "  IF f12 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0_0_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  f13 := 1\r\n" + //
                                "  f16 := f13\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0_0_0\r\n" + //
                                "  f15 := 0\r\n" + //
                                "  f16 := f15\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0_0_0_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0_0_0_0\r\n" + //
                                "  f17 := e99 IMUL f11\r\n" + //
                                "  f18 := f17\r\n" + //
                                "  f19 := e95 IADD e97\r\n" + //
                                "  f20 := f19\r\n" + //
                                "  f21 := 23\r\n" + //
                                "  f22 := f20 ILSHIFT f21\r\n" + //
                                "  f29 := f22\r\n" + //
                                "  f24 := 31\r\n" + //
                                "  f25 := f16 ILSHIFT f24\r\n" + //
                                "  f26 := f29 IOR f25\r\n" + //
                                "  f29 := f26\r\n" + //
                                "  f28 := f29 IOR f18\r\n" + //
                                "  f29 := f28\r\n" + //
                                "  CALL IntBinaryAsReal ( f29 -] b45 )\r\n" + //
                                "  f30 [| e78\r\n" + //
                                "  f31 := f30\r\n" + //
                                "  e78 |[ f31\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL p\r\n" + //
                                "  f33 [- f32\r\n" + //
                                "  f35 [- f34\r\n" + //
                                "  f36 := 0\r\n" + //
                                "  CALL IntToReal ( f35 -] a11 )\r\n" + //
                                "  f38 [| f37\r\n" + //
                                "  CALL RAdd ( f33 -] b55 , f38 -] b57 )\r\n" + //
                                "  f39 [| f37\r\n" + //
                                "  CALL Round ( f39 -] f41 )\r\n" + //
                                "  f40 [| f37\r\n" + //
                                "  f36 := f40\r\n" + //
                                "  f37 |[ f36\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  f42 [- f41\r\n" + //
                                "  f45 := 0.5\r\n" + //
                                "  CALL RAdd ( f42 -] b55 , f45 -] b57 )\r\n" + //
                                "  f46 [| f44\r\n" + //
                                "  CALL Floor ( f46 -] f47 )\r\n" + //
                                "  f51 [| f50\r\n" + //
                                "  f44 |[ f51\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Floor\r\n" + //
                                "  f49 [- f47\r\n" + //
                                "  f50 |[ f49\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }
}
