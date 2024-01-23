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
                                " CALL WriteInt ( b -> W0 )\r\n" + //
                                " CALL WriteReal ( c -> W2 )\r\n" + //
                                " CALL WriteReal ( d -> W2 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Divide ( b -> W5 , a -> W7 )\r\n" + //
                                " W4 <| X0\r\n" + //
                                " CALL WriteReal ( W4 -> W2 )\r\n" + //
                                " m := 5\r\n" + //
                                " n := b IADD m\r\n" + //
                                " o := 6\r\n" + //
                                " p := b IADD o\r\n" + //
                                " q := n IMUL p\r\n" + //
                                " CALL WriteInt ( q -> W0 )\r\n" + //
                                " r := 4\r\n" + //
                                " s := a IADD r\r\n" + //
                                " t := 5.0\r\n" + //
                                " CALL IntToReal ( a -> Z1 )\r\n" + //
                                " a97 <| Z4\r\n" + //
                                " CALL RAdd ( a97 -> a99 , t -> b11 )\r\n" + //
                                " a98 <| b14\r\n" + //
                                " CALL IntToReal ( s -> Z1 )\r\n" + //
                                " c62 <| Z4\r\n" + //
                                " CALL RMul ( c62 -> c64 , a98 -> c65 )\r\n" + //
                                " c63 <| c66\r\n" + //
                                " CALL WriteReal ( c63 -> W2 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " y := 3.1415\r\n" + //
                                " CALL p ( b -> e , y -> f )\r\n" + //
                                " z <| g\r\n" + //
                                " a := z\r\n" + //
                                " CALL WriteReal ( d -> W2 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  W3 <- W2\r\n" + //
                                "  IPARAM W3\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
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
                                "  CALL Div ( W6 -> X2 , W8 -> X4 )\r\n" + //
                                "  Y9 <| X7\r\n" + //
                                "  X1 := Y9\r\n" + //
                                "  CALL IntToReal ( X1 -> Z1 )\r\n" + //
                                "  Z0 <| X0\r\n" + //
                                "  W9 := Z0\r\n" + //
                                "  X0 |< W9\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  X3 <- X2\r\n" + //
                                "  X5 <- X4\r\n" + //
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
                                "  X7 |< X6\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  Z2 <- Z1\r\n" + //
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
                                "  CALL IntIsNegative ( Z2 -> a18 )\r\n" + //
                                "  a17 <| Z4\r\n" + //
                                "  Z5 := a17\r\n" + //
                                "  CALL IntIsZero ( Z9 -> a32 )\r\n" + //
                                "  a31 <| Z4\r\n" + //
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
                                "  CALL IntIsZero ( Z9 -> a32 )\r\n" + //
                                "  a49 <| Z4\r\n" + //
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
                                "  a58 := 2147483648\r\n" + //
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
                                "  CALL IntBinaryAsReal ( Z6 -> a91 )\r\n" + //
                                "  a96 <| a94\r\n" + //
                                "  Z3 := a96\r\n" + //
                                "  Z4 |< Z3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  a19 <- a18\r\n" + //
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
                                "  a21 |< a20\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a33 <- a32\r\n" + //
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
                                "  a35 |< a34\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a92 <- a91\r\n" + //
                                "  a93 := 0.0\r\n" + //
                                "  a95 := 0\r\n" + //
                                "  IPARAM a95\r\n" + //
                                "  IPARAM a92\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a95\r\n" + //
                                "  IPARAM a93\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a94 |< a93\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  b10 <- a99\r\n" + //
                                "  b12 <- b11\r\n" + //
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
                                "  CALL RealSign ( b10 -> b28 )\r\n" + //
                                "  b27 <| b14\r\n" + //
                                "  b16 := b27\r\n" + //
                                "  CALL RealSign ( b12 -> b28 )\r\n" + //
                                "  b43 <| b14\r\n" + //
                                "  b17 := b43\r\n" + //
                                "  CALL RealExponent ( b10 -> b45 )\r\n" + //
                                "  b44 <| b14\r\n" + //
                                "  b19 := b44\r\n" + //
                                "  CALL RealExponent ( b12 -> b45 )\r\n" + //
                                "  b55 <| b14\r\n" + //
                                "  b20 := b55\r\n" + //
                                "  CALL RealMantissa ( b10 -> b57 )\r\n" + //
                                "  b56 <| b14\r\n" + //
                                "  b21 := b56\r\n" + //
                                "  CALL RealMantissa ( b12 -> b57 )\r\n" + //
                                "  b65 <| b14\r\n" + //
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
                                "  CALL IntBinaryAsReal ( b15 -> a91 )\r\n" + //
                                "  o7 <| b14\r\n" + //
                                "  b13 := o7\r\n" + //
                                "  b14 |< b13\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  b29 <- b28\r\n" + //
                                "  b30 := 0\r\n" + //
                                "  b32 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b29 -> b34 )\r\n" + //
                                "  b33 <| b31\r\n" + //
                                "  b32 := b33\r\n" + //
                                "  b39 := 31\r\n" + //
                                "  b40 := b32 IRSHIFT b39\r\n" + //
                                "  b41 := 1\r\n" + //
                                "  b42 := b40 IAND b41\r\n" + //
                                "  b30 := b42\r\n" + //
                                "  b31 |< b30\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  b35 <- b34\r\n" + //
                                "  b36 := 0\r\n" + //
                                "  b38 := 0.0\r\n" + //
                                "  IPARAM b38\r\n" + //
                                "  IPARAM b35\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b38\r\n" + //
                                "  IPARAM b36\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  b37 |< b36\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  b46 <- b45\r\n" + //
                                "  b47 := 0\r\n" + //
                                "  b49 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b46 -> b34 )\r\n" + //
                                "  b50 <| b48\r\n" + //
                                "  b49 := b50\r\n" + //
                                "  b51 := 23\r\n" + //
                                "  b52 := b49 IRSHIFT b51\r\n" + //
                                "  b53 := 255\r\n" + //
                                "  b54 := b52 IAND b53\r\n" + //
                                "  b47 := b54\r\n" + //
                                "  b48 |< b47\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b58 <- b57\r\n" + //
                                "  b59 := 0\r\n" + //
                                "  b61 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b58 -> b34 )\r\n" + //
                                "  b62 <| b60\r\n" + //
                                "  b61 := b62\r\n" + //
                                "  b63 := 8388607\r\n" + //
                                "  b64 := b61 IAND b63\r\n" + //
                                "  b59 := b64\r\n" + //
                                "  b60 |< b59\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  q4 <- c64\r\n" + //
                                "  q6 <- c65\r\n" + //
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
                                "  CALL RealSign ( q4 -> b28 )\r\n" + //
                                "  q5 <| c66\r\n" + //
                                "  p5 := q5\r\n" + //
                                "  CALL RealSign ( q6 -> b28 )\r\n" + //
                                "  q7 <| c66\r\n" + //
                                "  p6 := q7\r\n" + //
                                "  CALL RealExponent ( q4 -> b45 )\r\n" + //
                                "  q8 <| c66\r\n" + //
                                "  p7 := q8\r\n" + //
                                "  CALL RealExponent ( q6 -> b45 )\r\n" + //
                                "  q9 <| c66\r\n" + //
                                "  p8 := q9\r\n" + //
                                "  CALL RealMantissa ( q4 -> b57 )\r\n" + //
                                "  r0 <| c66\r\n" + //
                                "  p9 := r0\r\n" + //
                                "  CALL RealMantissa ( q6 -> b57 )\r\n" + //
                                "  r1 <| c66\r\n" + //
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
                                "  CALL IntBinaryAsReal ( p4 -> a91 )\r\n" + //
                                "  s3 <| c66\r\n" + //
                                "  p3 := s3\r\n" + //
                                "  c66 |< p3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL p\r\n" + //
                                "  c68 <- c67\r\n" + //
                                "  c70 <- c69\r\n" + //
                                "  c71 := 0\r\n" + //
                                "  CALL IntToReal ( c70 -> Z1 )\r\n" + //
                                "  c73 <| c72\r\n" + //
                                "  CALL RAdd ( c68 -> a99 , c73 -> b11 )\r\n" + //
                                "  c74 <| c72\r\n" + //
                                "  CALL Round ( c74 -> c76 )\r\n" + //
                                "  c75 <| c72\r\n" + //
                                "  c71 := c75\r\n" + //
                                "  c72 |< c71\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  c77 <- c76\r\n" + //
                                "  c80 := 0.5\r\n" + //
                                "  CALL RAdd ( c77 -> a99 , c80 -> b11 )\r\n" + //
                                "  c81 <| c79\r\n" + //
                                "  CALL Floor ( c81 -> c82 )\r\n" + //
                                "  c78 <| c84\r\n" + //
                                "  c79 |< c78\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Floor\r\n" + //
                                "  c83 <- c82\r\n" + //
                                "  c84 |< c83\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testExpressions(){
        String progSrc = "test_source/expressions.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                " b := a\r\n" + //
                                " c := 1.2\r\n" + //
                                " d := c\r\n" + //
                                " e := 3.14\r\n" + //
                                " CALL RNeg ( e -> W1 )\r\n" + //
                                " W0 <| W2\r\n" + //
                                " CALL IntToReal ( b -> W9 )\r\n" + //
                                " W8 <| X2\r\n" + //
                                " CALL RAdd ( W0 -> a76 , W8 -> a78 )\r\n" + //
                                " a75 <| a81\r\n" + //
                                " i := a75\r\n" + //
                                " j := 6\r\n" + //
                                " k := 6\r\n" + //
                                " l := 1\r\n" + //
                                " m := k IADD l\r\n" + //
                                " n := j IMUL m\r\n" + //
                                " o := n\r\n" + //
                                " c34 <| X2\r\n" + //
                                " CALL RMul ( c34 -> c36 , d -> c37 )\r\n" + //
                                " c35 <| c38\r\n" + //
                                " CALL IntToReal ( o -> W9 )\r\n" + //
                                " c39 <| X2\r\n" + //
                                " CALL RDivide ( i -> c41 , c39 -> c42 )\r\n" + //
                                " c40 <| c43\r\n" + //
                                " CALL RNotEqualTo ( c35 -> c62 , c40 -> c63 )\r\n" + //
                                " c61 <| c64\r\n" + //
                                " u := c61\r\n" + //
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
                                " CALL RDivide ( d -> c41 , d -> c42 )\r\n" + //
                                " c75 <| c43\r\n" + //
                                " v := c75\r\n" + //
                                " F := 10\r\n" + //
                                " CALL Mod ( o -> c77 , F -> c79 )\r\n" + //
                                " c76 <| c82\r\n" + //
                                " y := c76\r\n" + //
                                " CALL RNeg ( i -> W1 )\r\n" + //
                                " c91 <| W2\r\n" + //
                                " CALL IntToReal ( b -> W9 )\r\n" + //
                                " c92 <| X2\r\n" + //
                                " CALL RMul ( d -> c36 , c92 -> c37 )\r\n" + //
                                " c93 <| c38\r\n" + //
                                " CALL RSub ( c91 -> c95 , c93 -> c96 )\r\n" + //
                                " c94 <| c97\r\n" + //
                                " w := c94\r\n" + //
                                " CALL WriteInt ( b -> c98 )\r\n" + //
                                " CALL WriteReal ( v -> d10 )\r\n" + //
                                " CALL WriteReal ( v -> d10 )\r\n" + //
                                " CALL WriteReal ( w -> d10 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Div ( o -> c44 , y -> c46 )\r\n" + //
                                " d12 <| c49\r\n" + //
                                " z := d12\r\n" + //
                                " CALL Divide ( o -> d14 , y -> d16 )\r\n" + //
                                " d13 <| d19\r\n" + //
                                " x := d13\r\n" + //
                                " CALL WriteInt ( z -> c98 )\r\n" + //
                                " CALL WriteReal ( x -> d10 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Round ( i -> d24 )\r\n" + //
                                " d23 <| d27\r\n" + //
                                " x := d23\r\n" + //
                                " CALL WriteReal ( x -> d10 )\r\n" + //
                                " IF u EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_3 ELSE IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                " O := 2\r\n" + //
                                " P := 2\r\n" + //
                                " Q := O IMUL P\r\n" + //
                                " CALL WriteInt ( Q -> c98 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                " R := 10\r\n" + //
                                " CALL Divide ( o -> d14 , R -> d16 )\r\n" + //
                                " d33 <| d19\r\n" + //
                                " CALL WriteReal ( d33 -> d10 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0_2\r\n" + //
                                " LABEL IFEND_0_LEVEL_0_3\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " T := BNOT u\r\n" + //
                                " CALL IntToReal ( b -> W9 )\r\n" + //
                                " d34 <| X2\r\n" + //
                                " CALL RGreaterThan ( v -> d36 , d34 -> d37 )\r\n" + //
                                " d35 <| d38\r\n" + //
                                " W := T LAND d35\r\n" + //
                                " B := W\r\n" + //
                                " X := BNOT u\r\n" + //
                                " CALL IntToReal ( b -> W9 )\r\n" + //
                                " d69 <| X2\r\n" + //
                                " CALL RGreaterThanOrEqualTo ( v -> d71 , d69 -> d72 )\r\n" + //
                                " d70 <| d73\r\n" + //
                                " a0 := X LOR d70\r\n" + //
                                " C := a0\r\n" + //
                                " a1 := B EQ C\r\n" + //
                                " D := a1\r\n" + //
                                " IF B EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_2 ELSE IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                " LABEL IFSTAT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                " a2 := 4\r\n" + //
                                " CALL WriteInt ( a2 -> c98 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                " IF C EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                " LABEL IFSTAT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                " a3 := 5\r\n" + //
                                " CALL WriteInt ( a3 -> c98 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                " LABEL IFEND_1_LEVEL_0_2\r\n" + //
                                " IF D EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_2 ELSE IFNEXT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " LABEL IFSTAT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " a4 := 5\r\n" + //
                                " CALL WriteInt ( a4 -> c98 )\r\n" + //
                                " GOTO IFEND_2_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " a5 := 6\r\n" + //
                                " CALL WriteInt ( a5 -> c98 )\r\n" + //
                                " GOTO IFEND_2_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_2_SEQ_1_LEVEL_0_1\r\n" + //
                                " LABEL IFEND_2_LEVEL_0_2\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RNeg\r\n" + //
                                "  x5 <- W1\r\n" + //
                                "  w9 := 0\r\n" + //
                                "  x0 := 0\r\n" + //
                                "  x1 := 0\r\n" + //
                                "  x2 := 1\r\n" + //
                                "  x3 := 31\r\n" + //
                                "  x4 := x2 ILSHIFT x3\r\n" + //
                                "  w9 := x4\r\n" + //
                                "  CALL RealBinaryAsInt ( x5 -> W3 )\r\n" + //
                                "  x6 <| W6\r\n" + //
                                "  x0 := x6\r\n" + //
                                "  x7 := x0 IXOR w9\r\n" + //
                                "  x1 := x7\r\n" + //
                                "  W2 |< x1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  W4 <- W3\r\n" + //
                                "  W5 := 0\r\n" + //
                                "  W7 := 0.0\r\n" + //
                                "  IPARAM W7\r\n" + //
                                "  IPARAM W4\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM W7\r\n" + //
                                "  IPARAM W5\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  W6 |< W5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  X0 <- W9\r\n" + //
                                "  X1 := 0.0\r\n" + //
                                "  X3 := FALSE\r\n" + //
                                "  X4 := 0\r\n" + //
                                "  X5 := 0\r\n" + //
                                "  X6 := 0\r\n" + //
                                "  X7 := 0\r\n" + //
                                "  X8 := 0\r\n" + //
                                "  X9 := 0\r\n" + //
                                "  Y0 := 0\r\n" + //
                                "  Y1 := 0\r\n" + //
                                "  Y2 := 0\r\n" + //
                                "  Y3 := 0\r\n" + //
                                "  X7 := X0\r\n" + //
                                "  Y4 := 0\r\n" + //
                                "  X6 := Y4\r\n" + //
                                "  CALL IntIsNegative ( X0 -> Y6 )\r\n" + //
                                "  Y5 <| Y9\r\n" + //
                                "  X3 := Y5\r\n" + //
                                "  CALL IntIsZero ( X7 -> a10 )\r\n" + //
                                "  Z9 <| a13\r\n" + //
                                "  a18 := BNOT Z9\r\n" + //
                                "  IF a18 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a18 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a19 := 1\r\n" + //
                                "  a20 := X7 IAND a19\r\n" + //
                                "  X8 := a20\r\n" + //
                                "  a21 := 1\r\n" + //
                                "  a22 := X8 EQ a21\r\n" + //
                                "  IF a22 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  X5 := X6\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  a23 := 1\r\n" + //
                                "  a24 := X6 IADD a23\r\n" + //
                                "  X6 := a24\r\n" + //
                                "  a25 := 1\r\n" + //
                                "  a26 := X7 IRSHIFT a25\r\n" + //
                                "  X7 := a26\r\n" + //
                                "  CALL IntIsZero ( X7 -> a10 )\r\n" + //
                                "  a27 <| a13\r\n" + //
                                "  a28 := BNOT a27\r\n" + //
                                "  a18 := a28\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a29 := 23\r\n" + //
                                "  a30 := X5 LT a29\r\n" + //
                                "  IF a30 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a31 := 23\r\n" + //
                                "  a32 := a31 ISUB X5\r\n" + //
                                "  X9 := a32\r\n" + //
                                "  a33 := INOT X9\r\n" + //
                                "  a34 := 1\r\n" + //
                                "  a35 := a33 IADD a34\r\n" + //
                                "  Y0 := a35\r\n" + //
                                "  a36 := 2147483648\r\n" + //
                                "  a37 := INOT a36\r\n" + //
                                "  a38 := Y0 IAND a37\r\n" + //
                                "  Y0 := a38\r\n" + //
                                "  a39 := 64\r\n" + //
                                "  a40 := Y0 IOR a39\r\n" + //
                                "  Y0 := a40\r\n" + //
                                "  a41 := 1\r\n" + //
                                "  a42 := a41 ILSHIFT X5\r\n" + //
                                "  a43 := 1\r\n" + //
                                "  a44 := a42 ISUB a43\r\n" + //
                                "  Y2 := a44\r\n" + //
                                "  a45 := X0 IAND Y2\r\n" + //
                                "  Y1 := a45\r\n" + //
                                "  a46 := Y1 ILSHIFT X9\r\n" + //
                                "  Y1 := a46\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a47 := 23\r\n" + //
                                "  a48 := X5 GT a47\r\n" + //
                                "  IF a48 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a49 := 23\r\n" + //
                                "  a50 := X5 ISUB a49\r\n" + //
                                "  X9 := a50\r\n" + //
                                "  Y0 := X9\r\n" + //
                                "  a51 := 1\r\n" + //
                                "  a52 := a51 ILSHIFT X5\r\n" + //
                                "  a53 := 1\r\n" + //
                                "  a54 := a52 ISUB a53\r\n" + //
                                "  Y2 := a54\r\n" + //
                                "  a55 := X0 IAND Y2\r\n" + //
                                "  Y1 := a55\r\n" + //
                                "  a56 := Y1 IRSHIFT X9\r\n" + //
                                "  Y1 := a56\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  Y0 := X5\r\n" + //
                                "  a57 := 1\r\n" + //
                                "  a58 := a57 ILSHIFT X5\r\n" + //
                                "  a59 := 1\r\n" + //
                                "  a60 := a58 ISUB a59\r\n" + //
                                "  Y2 := a60\r\n" + //
                                "  a61 := X0 IAND Y2\r\n" + //
                                "  Y1 := a61\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  X4 := Y1\r\n" + //
                                "  a62 := 23\r\n" + //
                                "  a63 := Y0 ILSHIFT a62\r\n" + //
                                "  a64 := X4 IOR a63\r\n" + //
                                "  X4 := a64\r\n" + //
                                "  IF X3 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a65 := 1\r\n" + //
                                "  a66 := 31\r\n" + //
                                "  a67 := a65 ILSHIFT a66\r\n" + //
                                "  a68 := X4 IOR a67\r\n" + //
                                "  X4 := a68\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( X4 -> a69 )\r\n" + //
                                "  a74 <| a72\r\n" + //
                                "  X1 := a74\r\n" + //
                                "  X2 |< X1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  Y7 <- Y6\r\n" + //
                                "  Y8 := FALSE\r\n" + //
                                "  Z0 := 0\r\n" + //
                                "  Z1 := 31\r\n" + //
                                "  Z2 := Y7 IRSHIFT Z1\r\n" + //
                                "  Z3 := 1\r\n" + //
                                "  Z4 := Z2 IAND Z3\r\n" + //
                                "  Z0 := Z4\r\n" + //
                                "  Z5 := 0\r\n" + //
                                "  Z6 := Z0 EQ Z5\r\n" + //
                                "  IF Z6 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z7 := FALSE\r\n" + //
                                "  Y8 := Z7\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  Z8 := TRUE\r\n" + //
                                "  Y8 := Z8\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  Y9 |< Y8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a11 <- a10\r\n" + //
                                "  a12 := FALSE\r\n" + //
                                "  a14 := 0\r\n" + //
                                "  a15 := a11 EQ a14\r\n" + //
                                "  IF a15 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a16 := TRUE\r\n" + //
                                "  a12 := a16\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a17 := FALSE\r\n" + //
                                "  a12 := a17\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  a13 |< a12\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a70 <- a69\r\n" + //
                                "  a71 := 0.0\r\n" + //
                                "  a73 := 0\r\n" + //
                                "  IPARAM a73\r\n" + //
                                "  IPARAM a70\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a73\r\n" + //
                                "  IPARAM a71\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a72 |< a71\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  a77 <- a76\r\n" + //
                                "  a79 <- a78\r\n" + //
                                "  a80 := 0.0\r\n" + //
                                "  a82 := 0\r\n" + //
                                "  a83 := 0\r\n" + //
                                "  a84 := 0\r\n" + //
                                "  a85 := 0\r\n" + //
                                "  a86 := 0\r\n" + //
                                "  a87 := 0\r\n" + //
                                "  a88 := 0\r\n" + //
                                "  a89 := 0\r\n" + //
                                "  a90 := 0\r\n" + //
                                "  a91 := 0\r\n" + //
                                "  a92 := 0\r\n" + //
                                "  a93 := 0\r\n" + //
                                "  CALL RealSign ( a77 -> a95 )\r\n" + //
                                "  a94 <| a98\r\n" + //
                                "  a83 := a94\r\n" + //
                                "  CALL RealSign ( a79 -> a95 )\r\n" + //
                                "  b15 <| a98\r\n" + //
                                "  a84 := b15\r\n" + //
                                "  CALL RealExponent ( a77 -> b17 )\r\n" + //
                                "  b16 <| b20\r\n" + //
                                "  a86 := b16\r\n" + //
                                "  CALL RealExponent ( a79 -> b17 )\r\n" + //
                                "  b27 <| b20\r\n" + //
                                "  a87 := b27\r\n" + //
                                "  CALL RealMantissa ( a77 -> b29 )\r\n" + //
                                "  b28 <| b32\r\n" + //
                                "  a88 := b28\r\n" + //
                                "  CALL RealMantissa ( a79 -> b29 )\r\n" + //
                                "  b37 <| b32\r\n" + //
                                "  a89 := b37\r\n" + //
                                "  b38 := a83 EQ a84\r\n" + //
                                "  IF b38 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  a93 := a83\r\n" + //
                                "  b39 := a86 EQ a87\r\n" + //
                                "  IF b39 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b40 := a88 IADD a89\r\n" + //
                                "  a92 := b40\r\n" + //
                                "  b41 := 25\r\n" + //
                                "  b42 := a92 IRSHIFT b41\r\n" + //
                                "  b43 := 1\r\n" + //
                                "  b44 := b42 IAND b43\r\n" + //
                                "  a91 := b44\r\n" + //
                                "  a90 := a86\r\n" + //
                                "  b45 := 1\r\n" + //
                                "  b46 := a91 EQ b45\r\n" + //
                                "  IF b46 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  b47 := 1\r\n" + //
                                "  b48 := a90 IADD b47\r\n" + //
                                "  a90 := b48\r\n" + //
                                "  b49 := 1\r\n" + //
                                "  b50 := a92 IRSHIFT b49\r\n" + //
                                "  a92 := b50\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  b51 := 23\r\n" + //
                                "  b52 := a90 ILSHIFT b51\r\n" + //
                                "  a82 := b52\r\n" + //
                                "  b53 := a82 IOR a92\r\n" + //
                                "  a82 := b53\r\n" + //
                                "  b54 := 31\r\n" + //
                                "  b55 := a93 ILSHIFT b54\r\n" + //
                                "  b56 := a82 IOR b55\r\n" + //
                                "  a82 := b56\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b57 := a86 GT a87\r\n" + //
                                "  IF b57 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b58 := a86 ISUB a87\r\n" + //
                                "  a85 := b58\r\n" + //
                                "  a87 := a86\r\n" + //
                                "  a90 := a86\r\n" + //
                                "  b59 := a89 IRSHIFT a85\r\n" + //
                                "  a89 := b59\r\n" + //
                                "  b60 := a88 IADD a89\r\n" + //
                                "  a92 := b60\r\n" + //
                                "  b61 := 25\r\n" + //
                                "  b62 := a92 IRSHIFT b61\r\n" + //
                                "  b63 := 1\r\n" + //
                                "  b64 := b62 IAND b63\r\n" + //
                                "  a91 := b64\r\n" + //
                                "  b65 := 1\r\n" + //
                                "  b66 := a91 EQ b65\r\n" + //
                                "  IF b66 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  b67 := 1\r\n" + //
                                "  b68 := a90 IADD b67\r\n" + //
                                "  a90 := b68\r\n" + //
                                "  b69 := 1\r\n" + //
                                "  b70 := a92 IRSHIFT b69\r\n" + //
                                "  a92 := b70\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  b71 := 23\r\n" + //
                                "  b72 := a90 ILSHIFT b71\r\n" + //
                                "  a82 := b72\r\n" + //
                                "  b73 := 31\r\n" + //
                                "  b74 := a93 ILSHIFT b73\r\n" + //
                                "  b75 := a82 IOR b74\r\n" + //
                                "  a82 := b75\r\n" + //
                                "  b76 := a82 IOR a92\r\n" + //
                                "  a82 := b76\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b77 := a87 ISUB a86\r\n" + //
                                "  a85 := b77\r\n" + //
                                "  a86 := a87\r\n" + //
                                "  a90 := a87\r\n" + //
                                "  b78 := a88 IRSHIFT a85\r\n" + //
                                "  a88 := b78\r\n" + //
                                "  b79 := a88 IADD a89\r\n" + //
                                "  a92 := b79\r\n" + //
                                "  b80 := 25\r\n" + //
                                "  b81 := a92 IRSHIFT b80\r\n" + //
                                "  b82 := 1\r\n" + //
                                "  b83 := b81 IAND b82\r\n" + //
                                "  a91 := b83\r\n" + //
                                "  b84 := 1\r\n" + //
                                "  b85 := a91 EQ b84\r\n" + //
                                "  IF b85 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  b86 := 1\r\n" + //
                                "  b87 := a90 IADD b86\r\n" + //
                                "  a90 := b87\r\n" + //
                                "  b88 := 1\r\n" + //
                                "  b89 := a92 IRSHIFT b88\r\n" + //
                                "  a92 := b89\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  b90 := 23\r\n" + //
                                "  b91 := a90 ILSHIFT b90\r\n" + //
                                "  a82 := b91\r\n" + //
                                "  b92 := 31\r\n" + //
                                "  b93 := a93 ILSHIFT b92\r\n" + //
                                "  b94 := a82 IOR b93\r\n" + //
                                "  a82 := b94\r\n" + //
                                "  b95 := a82 IOR a92\r\n" + //
                                "  a82 := b95\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b96 := 0\r\n" + //
                                "  b97 := a83 EQ b96\r\n" + //
                                "  b98 := 1\r\n" + //
                                "  b99 := a84 EQ b98\r\n" + //
                                "  c10 := b97 LAND b99\r\n" + //
                                "  IF c10 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  c11 := a89 GT a88\r\n" + //
                                "  IF c11 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  c12 := 1\r\n" + //
                                "  a93 := c12\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  c13 := 0\r\n" + //
                                "  a93 := c13\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  c14 := 1\r\n" + //
                                "  c15 := a83 EQ c14\r\n" + //
                                "  c16 := 0\r\n" + //
                                "  c17 := a84 EQ c16\r\n" + //
                                "  c18 := c15 LAND c17\r\n" + //
                                "  IF c18 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  c19 := a89 GE a88\r\n" + //
                                "  IF c19 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  c20 := 0\r\n" + //
                                "  a93 := c20\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  c21 := 1\r\n" + //
                                "  a93 := c21\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  h8 := a86 EQ a87\r\n" + //
                                "  IF h8 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  h9 := 0\r\n" + //
                                "  a92 := h9\r\n" + //
                                "  i0 := 25\r\n" + //
                                "  i1 := a92 IRSHIFT i0\r\n" + //
                                "  i2 := 1\r\n" + //
                                "  i3 := i1 IAND i2\r\n" + //
                                "  a91 := i3\r\n" + //
                                "  a90 := a86\r\n" + //
                                "  c22 := 1\r\n" + //
                                "  c23 := a91 EQ c22\r\n" + //
                                "  IF c23 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  c24 := 1\r\n" + //
                                "  c25 := a90 IADD c24\r\n" + //
                                "  a90 := c25\r\n" + //
                                "  c26 := 1\r\n" + //
                                "  c27 := a92 IRSHIFT c26\r\n" + //
                                "  a92 := c27\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  c28 := 23\r\n" + //
                                "  c29 := a90 ILSHIFT c28\r\n" + //
                                "  a82 := c29\r\n" + //
                                "  c30 := a82 IOR a92\r\n" + //
                                "  a82 := c30\r\n" + //
                                "  c31 := 31\r\n" + //
                                "  c32 := a93 ILSHIFT c31\r\n" + //
                                "  c33 := a82 IOR c32\r\n" + //
                                "  a82 := c33\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  j6 := a86 GT a87\r\n" + //
                                "  IF j6 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  j7 := a86 ISUB a87\r\n" + //
                                "  a85 := j7\r\n" + //
                                "  a87 := a86\r\n" + //
                                "  a90 := a86\r\n" + //
                                "  j8 := a89 IRSHIFT a85\r\n" + //
                                "  a89 := j8\r\n" + //
                                "  j9 := 1\r\n" + //
                                "  k0 := a83 EQ j9\r\n" + //
                                "  k1 := 0\r\n" + //
                                "  k2 := a84 EQ k1\r\n" + //
                                "  k3 := k0 LAND k2\r\n" + //
                                "  IF k3 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  k4 := a89 ISUB a88\r\n" + //
                                "  a92 := k4\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  k5 := a88 ISUB a89\r\n" + //
                                "  a92 := k5\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  k6 := 25\r\n" + //
                                "  k7 := a92 IRSHIFT k6\r\n" + //
                                "  k8 := 1\r\n" + //
                                "  k9 := k7 IAND k8\r\n" + //
                                "  a91 := k9\r\n" + //
                                "  l0 := 1\r\n" + //
                                "  l1 := a91 EQ l0\r\n" + //
                                "  IF l1 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  l2 := 1\r\n" + //
                                "  l3 := a90 IADD l2\r\n" + //
                                "  a90 := l3\r\n" + //
                                "  l4 := 1\r\n" + //
                                "  l5 := a92 IRSHIFT l4\r\n" + //
                                "  a92 := l5\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  l6 := 23\r\n" + //
                                "  l7 := a90 ILSHIFT l6\r\n" + //
                                "  a82 := l7\r\n" + //
                                "  l8 := 31\r\n" + //
                                "  l9 := a93 ILSHIFT l8\r\n" + //
                                "  m0 := a82 IOR l9\r\n" + //
                                "  a82 := m0\r\n" + //
                                "  m1 := a82 IOR a92\r\n" + //
                                "  a82 := m1\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  m2 := a87 ISUB a86\r\n" + //
                                "  a85 := m2\r\n" + //
                                "  a86 := a87\r\n" + //
                                "  a90 := a87\r\n" + //
                                "  m3 := a88 IRSHIFT a85\r\n" + //
                                "  a88 := m3\r\n" + //
                                "  m4 := 1\r\n" + //
                                "  m5 := a83 EQ m4\r\n" + //
                                "  m6 := 0\r\n" + //
                                "  m7 := a84 EQ m6\r\n" + //
                                "  m8 := m5 LAND m7\r\n" + //
                                "  IF m8 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  m9 := a89 ISUB a88\r\n" + //
                                "  a92 := m9\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  n0 := a88 ISUB a89\r\n" + //
                                "  a92 := n0\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  n1 := 25\r\n" + //
                                "  n2 := a92 IRSHIFT n1\r\n" + //
                                "  n3 := 1\r\n" + //
                                "  n4 := n2 IAND n3\r\n" + //
                                "  a91 := n4\r\n" + //
                                "  n5 := 1\r\n" + //
                                "  n6 := a91 EQ n5\r\n" + //
                                "  IF n6 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  n7 := 1\r\n" + //
                                "  n8 := a90 IADD n7\r\n" + //
                                "  a90 := n8\r\n" + //
                                "  n9 := 1\r\n" + //
                                "  o0 := a92 IRSHIFT n9\r\n" + //
                                "  a92 := o0\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  o1 := 23\r\n" + //
                                "  o2 := a90 ILSHIFT o1\r\n" + //
                                "  a82 := o2\r\n" + //
                                "  o3 := 31\r\n" + //
                                "  o4 := a93 ILSHIFT o3\r\n" + //
                                "  o5 := a82 IOR o4\r\n" + //
                                "  a82 := o5\r\n" + //
                                "  o6 := a82 IOR a92\r\n" + //
                                "  a82 := o6\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( a82 -> a69 )\r\n" + //
                                "  o7 <| a72\r\n" + //
                                "  a80 := o7\r\n" + //
                                "  a81 |< a80\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a96 <- a95\r\n" + //
                                "  a97 := 0\r\n" + //
                                "  a99 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a96 -> W3 )\r\n" + //
                                "  b10 <| W6\r\n" + //
                                "  a99 := b10\r\n" + //
                                "  b11 := 31\r\n" + //
                                "  b12 := a99 IRSHIFT b11\r\n" + //
                                "  b13 := 1\r\n" + //
                                "  b14 := b12 IAND b13\r\n" + //
                                "  a97 := b14\r\n" + //
                                "  a98 |< a97\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  b18 <- b17\r\n" + //
                                "  b19 := 0\r\n" + //
                                "  b21 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b18 -> W3 )\r\n" + //
                                "  b22 <| W6\r\n" + //
                                "  b21 := b22\r\n" + //
                                "  b23 := 23\r\n" + //
                                "  b24 := b21 IRSHIFT b23\r\n" + //
                                "  b25 := 255\r\n" + //
                                "  b26 := b24 IAND b25\r\n" + //
                                "  b19 := b26\r\n" + //
                                "  b20 |< b19\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b30 <- b29\r\n" + //
                                "  b31 := 0\r\n" + //
                                "  b33 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b30 -> W3 )\r\n" + //
                                "  b34 <| W6\r\n" + //
                                "  b33 := b34\r\n" + //
                                "  b35 := 8388607\r\n" + //
                                "  b36 := b33 IAND b35\r\n" + //
                                "  b31 := b36\r\n" + //
                                "  b32 |< b31\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  q4 <- c36\r\n" + //
                                "  q6 <- c37\r\n" + //
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
                                "  CALL RealSign ( q4 -> a95 )\r\n" + //
                                "  q5 <| a98\r\n" + //
                                "  p5 := q5\r\n" + //
                                "  CALL RealSign ( q6 -> a95 )\r\n" + //
                                "  q7 <| a98\r\n" + //
                                "  p6 := q7\r\n" + //
                                "  CALL RealExponent ( q4 -> b17 )\r\n" + //
                                "  q8 <| b20\r\n" + //
                                "  p7 := q8\r\n" + //
                                "  CALL RealExponent ( q6 -> b17 )\r\n" + //
                                "  q9 <| b20\r\n" + //
                                "  p8 := q9\r\n" + //
                                "  CALL RealMantissa ( q4 -> b29 )\r\n" + //
                                "  r0 <| b32\r\n" + //
                                "  p9 := r0\r\n" + //
                                "  CALL RealMantissa ( q6 -> b29 )\r\n" + //
                                "  r1 <| b32\r\n" + //
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
                                "  CALL IntBinaryAsReal ( p4 -> a69 )\r\n" + //
                                "  s3 <| a72\r\n" + //
                                "  p3 := s3\r\n" + //
                                "  c38 |< p3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RDivide\r\n" + //
                                "  t5 <- c41\r\n" + //
                                "  t7 <- c42\r\n" + //
                                "  s4 := 0.0\r\n" + //
                                "  s5 := 0\r\n" + //
                                "  s6 := 0\r\n" + //
                                "  s7 := 0\r\n" + //
                                "  s8 := 0\r\n" + //
                                "  s9 := 0\r\n" + //
                                "  t0 := 0\r\n" + //
                                "  t1 := 0\r\n" + //
                                "  t2 := 0\r\n" + //
                                "  t3 := 0\r\n" + //
                                "  t4 := 0\r\n" + //
                                "  CALL RealSign ( t5 -> a95 )\r\n" + //
                                "  t6 <| a98\r\n" + //
                                "  s6 := t6\r\n" + //
                                "  CALL RealSign ( t7 -> a95 )\r\n" + //
                                "  t8 <| a98\r\n" + //
                                "  s7 := t8\r\n" + //
                                "  CALL RealExponent ( t5 -> b17 )\r\n" + //
                                "  t9 <| b20\r\n" + //
                                "  s8 := t9\r\n" + //
                                "  CALL RealExponent ( t7 -> b17 )\r\n" + //
                                "  u0 <| b20\r\n" + //
                                "  s9 := u0\r\n" + //
                                "  CALL RealMantissa ( t5 -> b29 )\r\n" + //
                                "  u1 <| b32\r\n" + //
                                "  t0 := u1\r\n" + //
                                "  CALL RealMantissa ( t7 -> b29 )\r\n" + //
                                "  u2 <| b32\r\n" + //
                                "  t1 := u2\r\n" + //
                                "  u3 := s6 NE s7\r\n" + //
                                "  IF u3 EQ TRUE THEN IFSTAT_27_SEQ_0_LEVEL_0 ELSE IFNEXT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  u4 := 1\r\n" + //
                                "  t2 := u4\r\n" + //
                                "  GOTO IFEND_27_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  u5 := 0\r\n" + //
                                "  t2 := u5\r\n" + //
                                "  GOTO IFEND_27_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_27_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_27_LEVEL_0\r\n" + //
                                "  CALL Div ( t0 -> c44 , t1 -> c46 )\r\n" + //
                                "  u6 <| c49\r\n" + //
                                "  t3 := u6\r\n" + //
                                "  u7 := s8 ISUB s9\r\n" + //
                                "  t4 := u7\r\n" + //
                                "  u8 := 23\r\n" + //
                                "  u9 := t4 ILSHIFT u8\r\n" + //
                                "  s5 := u9\r\n" + //
                                "  v0 := 31\r\n" + //
                                "  v1 := t2 ILSHIFT v0\r\n" + //
                                "  v2 := s5 IOR v1\r\n" + //
                                "  s5 := v2\r\n" + //
                                "  v3 := s5 IOR t3\r\n" + //
                                "  s5 := v3\r\n" + //
                                "  CALL IntBinaryAsReal ( s5 -> a69 )\r\n" + //
                                "  v4 <| a72\r\n" + //
                                "  s4 := v4\r\n" + //
                                "  c43 |< s4\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  c45 <- c44\r\n" + //
                                "  c47 <- c46\r\n" + //
                                "  c50 := 0\r\n" + //
                                "  c48 := 0\r\n" + //
                                "  c50 := c45\r\n" + //
                                "  c51 := 0\r\n" + //
                                "  c48 := c51\r\n" + //
                                "  c52 := c50 ISUB c47\r\n" + //
                                "  c53 := 0\r\n" + //
                                "  c54 := c52 GT c53\r\n" + //
                                "  IF c54 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF c54 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  c55 := c50 ISUB c47\r\n" + //
                                "  c50 := c55\r\n" + //
                                "  c56 := 1\r\n" + //
                                "  c57 := c48 IADD c56\r\n" + //
                                "  c48 := c57\r\n" + //
                                "  c58 := c50 ISUB c47\r\n" + //
                                "  c59 := 0\r\n" + //
                                "  c60 := c58 GT c59\r\n" + //
                                "  c54 := c60\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  c49 |< c48\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RNotEqualTo\r\n" + //
                                "  U7 <- c62\r\n" + //
                                "  U9 <- c63\r\n" + //
                                "  U1 := FALSE\r\n" + //
                                "  U2 := FALSE\r\n" + //
                                "  U3 := FALSE\r\n" + //
                                "  U4 := 0\r\n" + //
                                "  U5 := 0\r\n" + //
                                "  U6 := 0\r\n" + //
                                "  CALL RealIsZero ( U7 -> c65 )\r\n" + //
                                "  U8 <| c68\r\n" + //
                                "  U2 := U8\r\n" + //
                                "  CALL RealIsZero ( U9 -> c65 )\r\n" + //
                                "  V0 <| c68\r\n" + //
                                "  U3 := V0\r\n" + //
                                "  V1 := U2 LAND U3\r\n" + //
                                "  IF V1 EQ TRUE THEN IFSTAT_75_SEQ_0_LEVEL_0 ELSE IFNEXT_75_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_75_SEQ_0_LEVEL_0\r\n" + //
                                "  V2 := FALSE\r\n" + //
                                "  U1 := V2\r\n" + //
                                "  GOTO IFEND_75_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_75_SEQ_0_LEVEL_0\r\n" + //
                                "  CALL RealBinaryAsInt ( U7 -> W3 )\r\n" + //
                                "  V3 <| W6\r\n" + //
                                "  U5 := V3\r\n" + //
                                "  CALL RealBinaryAsInt ( U9 -> W3 )\r\n" + //
                                "  V4 <| W6\r\n" + //
                                "  U6 := V4\r\n" + //
                                "  V5 := U5 IXOR U6\r\n" + //
                                "  U4 := V5\r\n" + //
                                "  V6 := 0\r\n" + //
                                "  V7 := U4 EQ V6\r\n" + //
                                "  IF V7 EQ TRUE THEN IFSTAT_76_SEQ_0_LEVEL_1 ELSE IFNEXT_76_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_76_SEQ_0_LEVEL_1\r\n" + //
                                "  V8 := FALSE\r\n" + //
                                "  U1 := V8\r\n" + //
                                "  GOTO IFEND_76_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_76_SEQ_0_LEVEL_1\r\n" + //
                                "  V9 := TRUE\r\n" + //
                                "  U1 := V9\r\n" + //
                                "  GOTO IFEND_76_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_76_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_76_LEVEL_1\r\n" + //
                                "  GOTO IFEND_75_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_75_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_75_LEVEL_0\r\n" + //
                                "  c64 |< U1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsZero\r\n" + //
                                "  c66 <- c65\r\n" + //
                                "  c67 := FALSE\r\n" + //
                                "  c69 := 0\r\n" + //
                                "  CALL RealMantissa ( c66 -> b29 )\r\n" + //
                                "  c70 <| b32\r\n" + //
                                "  c69 := c70\r\n" + //
                                "  c71 := 0\r\n" + //
                                "  c72 := c69 EQ c71\r\n" + //
                                "  IF c72 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_2 ELSE IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  c73 := TRUE\r\n" + //
                                "  c67 := c73\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  c74 := FALSE\r\n" + //
                                "  c67 := c74\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_2\r\n" + //
                                "  c68 |< c67\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Mod\r\n" + //
                                "  c78 <- c77\r\n" + //
                                "  c80 <- c79\r\n" + //
                                "  c81 := 0\r\n" + //
                                "  c83 := 0\r\n" + //
                                "  c81 := c78\r\n" + //
                                "  c84 := c81 ISUB c80\r\n" + //
                                "  c85 := 0\r\n" + //
                                "  c86 := c84 GT c85\r\n" + //
                                "  IF c86 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF c86 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c87 := c81 ISUB c80\r\n" + //
                                "  c81 := c87\r\n" + //
                                "  c88 := c81 ISUB c80\r\n" + //
                                "  c89 := 0\r\n" + //
                                "  c90 := c88 GT c89\r\n" + //
                                "  c86 := c90\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  c82 |< c81\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RSub\r\n" + //
                                "  o9 <- c95\r\n" + //
                                "  p0 <- c96\r\n" + //
                                "  o8 := 0.0\r\n" + //
                                "  CALL RNeg ( p0 -> W1 )\r\n" + //
                                "  p1 <| W2\r\n" + //
                                "  CALL RAdd ( o9 -> a76 , p1 -> a78 )\r\n" + //
                                "  p2 <| a81\r\n" + //
                                "  o8 := p2\r\n" + //
                                "  c97 |< o8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  c99 <- c98\r\n" + //
                                "  IPARAM c99\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  d11 <- d10\r\n" + //
                                "  IPARAM d11\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Divide\r\n" + //
                                "  d15 <- d14\r\n" + //
                                "  d17 <- d16\r\n" + //
                                "  d20 := 0\r\n" + //
                                "  d18 := 0\r\n" + //
                                "  CALL Div ( d15 -> c44 , d17 -> c46 )\r\n" + //
                                "  d21 <| c49\r\n" + //
                                "  d20 := d21\r\n" + //
                                "  CALL IntToReal ( d20 -> W9 )\r\n" + //
                                "  d22 <| d19\r\n" + //
                                "  d18 := d22\r\n" + //
                                "  d19 |< d18\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  d25 <- d24\r\n" + //
                                "  d28 := 0.5\r\n" + //
                                "  CALL RAdd ( d25 -> a76 , d28 -> a78 )\r\n" + //
                                "  d29 <| d27\r\n" + //
                                "  CALL Floor ( d29 -> d30 )\r\n" + //
                                "  d26 <| d32\r\n" + //
                                "  d27 |< d26\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Floor\r\n" + //
                                "  d31 <- d30\r\n" + //
                                "  d32 |< d31\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThan\r\n" + //
                                "  J1 <- d36\r\n" + //
                                "  J3 <- d37\r\n" + //
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
                                "  CALL RealIsZero ( J1 -> c65 )\r\n" + //
                                "  J2 <| d38\r\n" + //
                                "  I1 := J2\r\n" + //
                                "  CALL RealIsZero ( J3 -> c65 )\r\n" + //
                                "  J4 <| d38\r\n" + //
                                "  I2 := J4\r\n" + //
                                "  CALL RealIsNegative ( J1 -> d39 )\r\n" + //
                                "  J5 <| d38\r\n" + //
                                "  I3 := J5\r\n" + //
                                "  CALL RealIsNegative ( J3 -> d39 )\r\n" + //
                                "  J6 <| d38\r\n" + //
                                "  I4 := J6\r\n" + //
                                "  CALL RealIsPositive ( J1 -> d49 )\r\n" + //
                                "  J7 <| d38\r\n" + //
                                "  I5 := J7\r\n" + //
                                "  CALL RealIsPositive ( J3 -> d49 )\r\n" + //
                                "  J8 <| d38\r\n" + //
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
                                "  CALL RealScore ( J1 -> d58 )\r\n" + //
                                "  K8 <| d38\r\n" + //
                                "  I7 := K8\r\n" + //
                                "  CALL RealScore ( J3 -> d58 )\r\n" + //
                                "  K9 <| d38\r\n" + //
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
                                "  CALL RealMantissa ( J1 -> b29 )\r\n" + //
                                "  L3 <| d38\r\n" + //
                                "  I9 := L3\r\n" + //
                                "  CALL RealMantissa ( J3 -> b29 )\r\n" + //
                                "  L4 <| d38\r\n" + //
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
                                "  CALL RealScore ( J1 -> d58 )\r\n" + //
                                "  M0 <| d38\r\n" + //
                                "  I7 := M0\r\n" + //
                                "  CALL RealScore ( J3 -> d58 )\r\n" + //
                                "  M1 <| d38\r\n" + //
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
                                "  CALL RealMantissa ( J1 -> b29 )\r\n" + //
                                "  M5 <| d38\r\n" + //
                                "  I9 := M5\r\n" + //
                                "  CALL RealMantissa ( J3 -> b29 )\r\n" + //
                                "  M6 <| d38\r\n" + //
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
                                "  d38 |< I0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsNegative\r\n" + //
                                "  d40 <- d39\r\n" + //
                                "  d41 := FALSE\r\n" + //
                                "  d43 := 0\r\n" + //
                                "  CALL RealSign ( d40 -> a95 )\r\n" + //
                                "  d44 <| a98\r\n" + //
                                "  d43 := d44\r\n" + //
                                "  d45 := 0\r\n" + //
                                "  d46 := d43 EQ d45\r\n" + //
                                "  IF d46 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d47 := FALSE\r\n" + //
                                "  d41 := d47\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d48 := TRUE\r\n" + //
                                "  d41 := d48\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_3_LEVEL_0_0\r\n" + //
                                "  d42 |< d41\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsPositive\r\n" + //
                                "  d50 <- d49\r\n" + //
                                "  b2 := FALSE\r\n" + //
                                "  b3 := 0\r\n" + //
                                "  d52 := 0\r\n" + //
                                "  CALL RealSign ( d50 -> a95 )\r\n" + //
                                "  d53 <| a98\r\n" + //
                                "  d52 := d53\r\n" + //
                                "  d54 := 0\r\n" + //
                                "  d55 := d52 EQ d54\r\n" + //
                                "  IF d55 EQ TRUE THEN IFSTAT_5_SEQ_0_LEVEL_0_0 ELSE IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d56 := TRUE\r\n" + //
                                "  b2 := d56\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d57 := FALSE\r\n" + //
                                "  b2 := d57\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_5_LEVEL_0_0\r\n" + //
                                "  d51 |< b2\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealScore\r\n" + //
                                "  c8 <- d58\r\n" + //
                                "  c1 := 0\r\n" + //
                                "  c2 := 0\r\n" + //
                                "  c3 := 0\r\n" + //
                                "  c4 := 0\r\n" + //
                                "  c5 := 0\r\n" + //
                                "  c6 := 0\r\n" + //
                                "  c7 := 0\r\n" + //
                                "  CALL RealExponent ( c8 -> b17 )\r\n" + //
                                "  c9 <| b20\r\n" + //
                                "  c2 := c9\r\n" + //
                                "  CALL RealMantissa ( c8 -> b29 )\r\n" + //
                                "  d0 <| b32\r\n" + //
                                "  c3 := d0\r\n" + //
                                "  d1 := 0\r\n" + //
                                "  d2 := c3 EQ d1\r\n" + //
                                "  IF d2 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_0_0 ELSE IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c1 := c3\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d3 := 0\r\n" + //
                                "  c6 := d3\r\n" + //
                                "  d4 := 0\r\n" + //
                                "  d5 := c3 NE d4\r\n" + //
                                "  IF d5 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  IF d5 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILEEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  d6 := 1\r\n" + //
                                "  d7 := c3 IAND d6\r\n" + //
                                "  c7 := d7\r\n" + //
                                "  d8 := 1\r\n" + //
                                "  d9 := c7 EQ d8\r\n" + //
                                "  IF d9 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_1 ELSE IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  c4 := c6\r\n" + //
                                "  GOTO IFEND_7_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFEND_7_LEVEL_1\r\n" + //
                                "  d60 := 1\r\n" + //
                                "  d61 := c3 IRSHIFT d60\r\n" + //
                                "  c3 := d61\r\n" + //
                                "  d62 := 1\r\n" + //
                                "  d63 := c6 IADD d62\r\n" + //
                                "  c6 := d63\r\n" + //
                                "  d64 := 0\r\n" + //
                                "  d65 := c3 NE d64\r\n" + //
                                "  d5 := d65\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_2\r\n" + //
                                "  d66 := 23\r\n" + //
                                "  d67 := d66 ISUB c4\r\n" + //
                                "  d68 := d67 IADD c2\r\n" + //
                                "  c1 := d68\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_6_LEVEL_0_0\r\n" + //
                                "  d59 |< c1\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThanOrEqualTo\r\n" + //
                                "  O2 <- d71\r\n" + //
                                "  O4 <- d72\r\n" + //
                                "  N1 := FALSE\r\n" + //
                                "  N2 := FALSE\r\n" + //
                                "  N3 := FALSE\r\n" + //
                                "  N4 := FALSE\r\n" + //
                                "  N5 := FALSE\r\n" + //
                                "  N6 := FALSE\r\n" + //
                                "  N7 := FALSE\r\n" + //
                                "  N8 := 0\r\n" + //
                                "  N9 := 0\r\n" + //
                                "  O0 := 0\r\n" + //
                                "  O1 := 0\r\n" + //
                                "  CALL RealIsZero ( O2 -> c65 )\r\n" + //
                                "  O3 <| d73\r\n" + //
                                "  N2 := O3\r\n" + //
                                "  CALL RealIsZero ( O4 -> c65 )\r\n" + //
                                "  O5 <| d73\r\n" + //
                                "  N3 := O5\r\n" + //
                                "  CALL RealIsNegative ( O2 -> d39 )\r\n" + //
                                "  O6 <| d73\r\n" + //
                                "  N4 := O6\r\n" + //
                                "  CALL RealIsNegative ( O4 -> d39 )\r\n" + //
                                "  O7 <| d73\r\n" + //
                                "  N5 := O7\r\n" + //
                                "  CALL RealIsPositive ( O2 -> d49 )\r\n" + //
                                "  O8 <| d73\r\n" + //
                                "  N6 := O8\r\n" + //
                                "  CALL RealIsPositive ( O4 -> d49 )\r\n" + //
                                "  O9 <| d73\r\n" + //
                                "  N7 := O9\r\n" + //
                                "  P0 := N2 LAND N3\r\n" + //
                                "  IF P0 EQ TRUE THEN IFSTAT_61_SEQ_0_LEVEL_0 ELSE IFNEXT_61_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_0_LEVEL_0\r\n" + //
                                "  P1 := FALSE\r\n" + //
                                "  N1 := P1\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_0_LEVEL_0\r\n" + //
                                "  P2 := N4 LAND N3\r\n" + //
                                "  IF P2 EQ TRUE THEN IFSTAT_61_SEQ_1_LEVEL_0 ELSE IFNEXT_61_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_1_LEVEL_0\r\n" + //
                                "  P3 := TRUE\r\n" + //
                                "  N1 := P3\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_1_LEVEL_0\r\n" + //
                                "  P4 := N4 LAND N7\r\n" + //
                                "  IF P4 EQ TRUE THEN IFSTAT_61_SEQ_2_LEVEL_0 ELSE IFNEXT_61_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_2_LEVEL_0\r\n" + //
                                "  P5 := TRUE\r\n" + //
                                "  N1 := P5\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_2_LEVEL_0\r\n" + //
                                "  P6 := N2 LAND N7\r\n" + //
                                "  IF P6 EQ TRUE THEN IFSTAT_61_SEQ_3_LEVEL_0 ELSE IFNEXT_61_SEQ_3_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_3_LEVEL_0\r\n" + //
                                "  P7 := TRUE\r\n" + //
                                "  N1 := P7\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_3_LEVEL_0\r\n" + //
                                "  P8 := N4 LAND N5\r\n" + //
                                "  IF P8 EQ TRUE THEN IFSTAT_61_SEQ_4_LEVEL_0 ELSE IFNEXT_61_SEQ_4_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_4_LEVEL_0\r\n" + //
                                "  CALL RealScore ( O2 -> d58 )\r\n" + //
                                "  P9 <| d73\r\n" + //
                                "  N8 := P9\r\n" + //
                                "  CALL RealScore ( O4 -> d58 )\r\n" + //
                                "  Q0 <| d73\r\n" + //
                                "  N9 := Q0\r\n" + //
                                "  Q1 := N8 LT N9\r\n" + //
                                "  IF Q1 EQ TRUE THEN IFSTAT_62_SEQ_0_LEVEL_1 ELSE IFNEXT_62_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_62_SEQ_0_LEVEL_1\r\n" + //
                                "  Q2 := TRUE\r\n" + //
                                "  N1 := Q2\r\n" + //
                                "  GOTO IFEND_62_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_62_SEQ_0_LEVEL_1\r\n" + //
                                "  Q3 := N8 EQ N9\r\n" + //
                                "  IF Q3 EQ TRUE THEN IFSTAT_62_SEQ_1_LEVEL_1 ELSE IFNEXT_62_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_62_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( O2 -> b29 )\r\n" + //
                                "  Q4 <| d73\r\n" + //
                                "  O0 := Q4\r\n" + //
                                "  CALL RealMantissa ( O4 -> b29 )\r\n" + //
                                "  Q5 <| d73\r\n" + //
                                "  O1 := Q5\r\n" + //
                                "  Q6 := O0 LE O1\r\n" + //
                                "  IF Q6 EQ TRUE THEN IFSTAT_63_SEQ_0_LEVEL_2 ELSE IFNEXT_63_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_63_SEQ_0_LEVEL_2\r\n" + //
                                "  Q7 := TRUE\r\n" + //
                                "  N1 := Q7\r\n" + //
                                "  GOTO IFEND_63_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_63_SEQ_0_LEVEL_2\r\n" + //
                                "  Q8 := FALSE\r\n" + //
                                "  N1 := Q8\r\n" + //
                                "  GOTO IFEND_63_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_63_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_63_LEVEL_2\r\n" + //
                                "  GOTO IFEND_62_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_62_SEQ_1_LEVEL_1\r\n" + //
                                "  Q9 := FALSE\r\n" + //
                                "  N1 := Q9\r\n" + //
                                "  GOTO IFEND_62_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_62_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_62_LEVEL_1\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_4_LEVEL_0\r\n" + //
                                "  R0 := N6 LAND N7\r\n" + //
                                "  IF R0 EQ TRUE THEN IFSTAT_61_SEQ_5_LEVEL_0 ELSE IFNEXT_61_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_5_LEVEL_0\r\n" + //
                                "  CALL RealScore ( O2 -> d58 )\r\n" + //
                                "  R1 <| d73\r\n" + //
                                "  N8 := R1\r\n" + //
                                "  CALL RealScore ( O4 -> d58 )\r\n" + //
                                "  R2 <| d73\r\n" + //
                                "  N8 := R2\r\n" + //
                                "  R3 := N8 GT N9\r\n" + //
                                "  IF R3 EQ TRUE THEN IFSTAT_67_SEQ_0_LEVEL_1 ELSE IFNEXT_67_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_67_SEQ_0_LEVEL_1\r\n" + //
                                "  R4 := TRUE\r\n" + //
                                "  N1 := R4\r\n" + //
                                "  GOTO IFEND_67_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_67_SEQ_0_LEVEL_1\r\n" + //
                                "  R5 := N8 EQ N9\r\n" + //
                                "  IF R5 EQ TRUE THEN IFSTAT_67_SEQ_1_LEVEL_1 ELSE IFNEXT_67_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_67_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( O2 -> b29 )\r\n" + //
                                "  R6 <| d73\r\n" + //
                                "  O0 := R6\r\n" + //
                                "  CALL RealMantissa ( O4 -> b29 )\r\n" + //
                                "  R7 <| d73\r\n" + //
                                "  O1 := R7\r\n" + //
                                "  R8 := O0 GE O1\r\n" + //
                                "  IF R8 EQ TRUE THEN IFSTAT_68_SEQ_0_LEVEL_2 ELSE IFNEXT_68_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_68_SEQ_0_LEVEL_2\r\n" + //
                                "  R9 := TRUE\r\n" + //
                                "  N1 := R9\r\n" + //
                                "  GOTO IFEND_68_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_68_SEQ_0_LEVEL_2\r\n" + //
                                "  S0 := FALSE\r\n" + //
                                "  N1 := S0\r\n" + //
                                "  GOTO IFEND_68_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_68_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_68_LEVEL_2\r\n" + //
                                "  GOTO IFEND_67_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_67_SEQ_1_LEVEL_1\r\n" + //
                                "  S1 := FALSE\r\n" + //
                                "  N1 := S1\r\n" + //
                                "  GOTO IFEND_67_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_67_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_67_LEVEL_1\r\n" + //
                                "  GOTO IFEND_61_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFEND_61_LEVEL_0\r\n" + //
                                "  d73 |< N1\r\n" + //
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
                                " CALL WriteInt ( b -> W0 )\r\n" + //
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
                                " CALL WriteInt ( b -> W0 )\r\n" + //
                                " k := 1\r\n" + //
                                " CALL INeg ( k -> W3 )\r\n" + //
                                " W2 <| W6\r\n" + //
                                " m := b IADD W2\r\n" + //
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
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }
}
