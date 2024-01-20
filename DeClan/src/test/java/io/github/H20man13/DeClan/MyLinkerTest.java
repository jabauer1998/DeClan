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
                     " r INTERNAL lib1VariableName\r\n" + //
                     "DATA SECTION\r\n" + //
                     " a := 20\r\n" + //
                     " b := 500\r\n" + //
                     " r := 3\r\n" + //
                    "CODE SECTION\r\n" + //
                    " CALL func2 (  )\r\n" + //
                    " f [| i\r\n" + //
                    " g := f IADD r\r\n" + //
                    " LABEL begin2_1_1\r\n" + //
                    " IF g EQ r THEN begin_1_1 ELSE end_1_1\r\n" + //
                    " LABEL begin_1_1\r\n" + //
                    " g := f IADD r\r\n" + //
                    " GOTO begin2_1_1\r\n" + //
                    " LABEL end_1_1\r\n" + //
                    "END\r\n" + //
                    "PROC SECTION\r\n" + //
                    " PROC LABEL func2\r\n" + //
                    "  CALL func1 (  )\r\n" + //
                    "  j [| i\r\n" + //
                    "  m := j ISUB r\r\n" + //
                    "  LABEL begin2_1_0\r\n" + //
                    "  IF m EQ j THEN begin_1_0 ELSE end_1_0\r\n" + //
                    "  LABEL begin_1_0\r\n" + //
                    "  o := m IADD j\r\n" + //
                    "  GOTO begin2_1_0\r\n" + //
                    "  LABEL end_1_0\r\n" + //
                    "  i |[ o\r\n" + //
                    " RETURN\r\n" + //
                    " PROC LABEL func1\r\n" + //
                    "  l := 3\r\n" + //
                    "  LABEL begin2_0\r\n" + //
                    "  IF l EQ l THEN begin_0 ELSE end_0\r\n" + //
                    "  LABEL begin_0\r\n" + //
                    "  e := l IADD l\r\n" + //
                    "  GOTO begin2_0\r\n" + //
                    "  LABEL end_0\r\n" + //
                    "  k |[ e\r\n" + //
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
                                " CALL IntToReal ( a -> X6 )\r\n" + //
                                " b23 <| X9\r\n" + //
                                " CALL RAdd ( b23 -> b25 , t -> b27 )\r\n" + //
                                " b24 <| b30\r\n" + //
                                " CALL IntToReal ( s -> X6 )\r\n" + //
                                " e47 <| X9\r\n" + //
                                " CALL RMul ( e47 -> e49 , b24 -> e51 )\r\n" + //
                                " e48 <| e54\r\n" + //
                                " CALL WriteReal ( e48 -> W2 )\r\n" + //
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
                                "  X4 := 0\r\n" + //
                                "  b22 := 0\r\n" + //
                                "  CALL Div ( W6 -> c , W8 -> d )\r\n" + //
                                "  X3 <| e\r\n" + //
                                "  X4 := X3\r\n" + //
                                "  CALL IntToReal ( X4 -> X6 )\r\n" + //
                                "  X5 <| X0\r\n" + //
                                "  b22 := X5\r\n" + //
                                "  X0 |< b22\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  X7 <- X6\r\n" + //
                                "  b21 := 0.0\r\n" + //
                                "  a23 := FALSE\r\n" + //
                                "  b19 := 0\r\n" + //
                                "  a42 := 0\r\n" + //
                                "  a45 := 0\r\n" + //
                                "  a48 := 0\r\n" + //
                                "  a39 := 0\r\n" + //
                                "  a81 := 0\r\n" + //
                                "  a92 := 0\r\n" + //
                                "  a99 := 0\r\n" + //
                                "  a97 := 0\r\n" + //
                                "  Z1 := 0\r\n" + //
                                "  a48 := X7\r\n" + //
                                "  Z3 := 0\r\n" + //
                                "  a45 := Z3\r\n" + //
                                "  CALL IntIsNegative ( X7 -> Z6 )\r\n" + //
                                "  Z5 <| X9\r\n" + //
                                "  a23 := Z5\r\n" + //
                                "  CALL IntIsZero ( Z2 -> a25 )\r\n" + //
                                "  a24 <| X9\r\n" + //
                                "  a51 := BNOT a24\r\n" + //
                                "  IF a51 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  IF a51 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  a37 := 1\r\n" + //
                                "  a38 := a48 IAND a37\r\n" + //
                                "  a39 := a38\r\n" + //
                                "  a40 := 1\r\n" + //
                                "  a41 := a39 EQ a40\r\n" + //
                                "  IF a41 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  a42 := a45\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0\r\n" + //
                                "  a43 := 1\r\n" + //
                                "  a44 := a45 IADD a43\r\n" + //
                                "  a45 := a44\r\n" + //
                                "  a46 := 1\r\n" + //
                                "  a47 := a48 IRSHIFT a46\r\n" + //
                                "  a48 := a47\r\n" + //
                                "  CALL IntIsZero ( a48 -> a25 )\r\n" + //
                                "  a49 <| X9\r\n" + //
                                "  a50 := BNOT a49\r\n" + //
                                "  a51 := a50\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "  a52 := 23\r\n" + //
                                "  a53 := a42 LT a52\r\n" + //
                                "  IF a53 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "  a54 := 23\r\n" + //
                                "  a55 := a54 ISUB a42\r\n" + //
                                "  a81 := a55\r\n" + //
                                "  a57 := INOT a81\r\n" + //
                                "  a58 := 1\r\n" + //
                                "  a59 := a57 IADD a58\r\n" + //
                                "  a92 := a59\r\n" + //
                                "  a61 := -2147483648\r\n" + //
                                "  a62 := INOT a61\r\n" + //
                                "  a63 := a92 IAND a62\r\n" + //
                                "  a92 := a63\r\n" + //
                                "  a65 := 64\r\n" + //
                                "  a66 := a92 IOR a65\r\n" + //
                                "  a92 := a66\r\n" + //
                                "  a68 := 1\r\n" + //
                                "  a69 := a68 ILSHIFT a42\r\n" + //
                                "  a70 := 1\r\n" + //
                                "  a71 := a69 ISUB a70\r\n" + //
                                "  a97 := a71\r\n" + //
                                "  a73 := X7 IAND a97\r\n" + //
                                "  a99 := a73\r\n" + //
                                "  a75 := a99 ILSHIFT a81\r\n" + //
                                "  a99 := a75\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "  a77 := 23\r\n" + //
                                "  a78 := a42 GT a77\r\n" + //
                                "  IF a78 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a79 := 23\r\n" + //
                                "  a80 := a42 ISUB a79\r\n" + //
                                "  a81 := a80\r\n" + //
                                "  a92 := a81\r\n" + //
                                "  a83 := 1\r\n" + //
                                "  a84 := a83 ILSHIFT a42\r\n" + //
                                "  a85 := 1\r\n" + //
                                "  a86 := a84 ISUB a85\r\n" + //
                                "  a97 := a86\r\n" + //
                                "  a88 := X7 IAND a97\r\n" + //
                                "  a99 := a88\r\n" + //
                                "  a90 := a99 IRSHIFT a81\r\n" + //
                                "  a99 := a90\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  a92 := a42\r\n" + //
                                "  a93 := 1\r\n" + //
                                "  a94 := a93 ILSHIFT a42\r\n" + //
                                "  a95 := 1\r\n" + //
                                "  a96 := a94 ISUB a95\r\n" + //
                                "  a97 := a96\r\n" + //
                                "  a98 := X7 IAND a97\r\n" + //
                                "  a99 := a98\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0\r\n" + //
                                "  b19 := a99\r\n" + //
                                "  b11 := 23\r\n" + //
                                "  b12 := a92 ILSHIFT b11\r\n" + //
                                "  b13 := b19 IOR b12\r\n" + //
                                "  b19 := b13\r\n" + //
                                "  IF a23 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  b15 := 1\r\n" + //
                                "  b16 := 31\r\n" + //
                                "  b17 := b15 ILSHIFT b16\r\n" + //
                                "  b18 := b19 IOR b17\r\n" + //
                                "  b19 := b18\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0\r\n" + //
                                "  CALL IntBinaryAsReal ( b19 -> e40 )\r\n" + //
                                "  b20 <| e43\r\n" + //
                                "  b21 := b20\r\n" + //
                                "  X9 |< b21\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  Z7 <- Z6\r\n" + //
                                "  a22 := FALSE\r\n" + //
                                "  a16 := 0\r\n" + //
                                "  a12 := 31\r\n" + //
                                "  a13 := Z7 IRSHIFT a12\r\n" + //
                                "  a14 := 1\r\n" + //
                                "  a15 := a13 IAND a14\r\n" + //
                                "  a16 := a15\r\n" + //
                                "  a17 := 0\r\n" + //
                                "  a18 := a16 EQ a17\r\n" + //
                                "  IF a18 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  a19 := FALSE\r\n" + //
                                "  a22 := a19\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  a21 := TRUE\r\n" + //
                                "  a22 := a21\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0\r\n" + //
                                "  Z9 |< a22\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a26 <- a25\r\n" + //
                                "  a35 := FALSE\r\n" + //
                                "  a30 := 0\r\n" + //
                                "  a31 := a26 EQ a30\r\n" + //
                                "  IF a31 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0\r\n" + //
                                "  a32 := TRUE\r\n" + //
                                "  a35 := a32\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0\r\n" + //
                                "  a34 := FALSE\r\n" + //
                                "  a35 := a34\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0\r\n" + //
                                "  a28 |< a35\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  b26 <- b25\r\n" + //
                                "  b28 <- b27\r\n" + //
                                "  e46 := 0.0\r\n" + //
                                "  e38 := 0\r\n" + //
                                "  b64 := 0\r\n" + //
                                "  b66 := 0\r\n" + //
                                "  d93 := 0\r\n" + //
                                "  d94 := 0\r\n" + //
                                "  d57 := 0\r\n" + //
                                "  d97 := 0\r\n" + //
                                "  d60 := 0\r\n" + //
                                "  e26 := 0\r\n" + //
                                "  e21 := 0\r\n" + //
                                "  e29 := 0\r\n" + //
                                "  d27 := 0\r\n" + //
                                "  CALL RealSign ( b26 -> b45 )\r\n" + //
                                "  b44 <| b30\r\n" + //
                                "  b64 := b44\r\n" + //
                                "  CALL RealSign ( b28 -> b45 )\r\n" + //
                                "  b65 <| b30\r\n" + //
                                "  b66 := b65\r\n" + //
                                "  CALL RealExponent ( b26 -> b68 )\r\n" + //
                                "  b67 <| b30\r\n" + //
                                "  d94 := b67\r\n" + //
                                "  CALL RealExponent ( b28 -> b68 )\r\n" + //
                                "  b82 <| b30\r\n" + //
                                "  d57 := b82\r\n" + //
                                "  CALL RealMantissa ( b26 -> b85 )\r\n" + //
                                "  b84 <| b30\r\n" + //
                                "  d97 := b84\r\n" + //
                                "  CALL RealMantissa ( b28 -> b85 )\r\n" + //
                                "  b97 <| b30\r\n" + //
                                "  d60 := b97\r\n" + //
                                "  b99 := b64 EQ b66\r\n" + //
                                "  IF b99 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  d27 := b64\r\n" + //
                                "  c11 := d94 EQ d57\r\n" + //
                                "  IF c11 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  c12 := d97 IADD d60\r\n" + //
                                "  e29 := c12\r\n" + //
                                "  c14 := 25\r\n" + //
                                "  c15 := e29 IRSHIFT c14\r\n" + //
                                "  c16 := 1\r\n" + //
                                "  c17 := c15 IAND c16\r\n" + //
                                "  e21 := c17\r\n" + //
                                "  e26 := d94\r\n" + //
                                "  c20 := 1\r\n" + //
                                "  c21 := e21 EQ c20\r\n" + //
                                "  IF c21 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  c22 := 1\r\n" + //
                                "  c23 := e26 IADD c22\r\n" + //
                                "  e26 := c23\r\n" + //
                                "  c25 := 1\r\n" + //
                                "  c26 := e29 IRSHIFT c25\r\n" + //
                                "  e29 := c26\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  c28 := 23\r\n" + //
                                "  c29 := e26 ILSHIFT c28\r\n" + //
                                "  e38 := c29\r\n" + //
                                "  c31 := e38 IOR e29\r\n" + //
                                "  e38 := c31\r\n" + //
                                "  c33 := 31\r\n" + //
                                "  c34 := d27 ILSHIFT c33\r\n" + //
                                "  c35 := e38 IOR c34\r\n" + //
                                "  e38 := c35\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  c37 := d94 GT d57\r\n" + //
                                "  IF c37 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c38 := d94 ISUB d57\r\n" + //
                                "  d93 := c38\r\n" + //
                                "  d57 := d94\r\n" + //
                                "  e26 := d94\r\n" + //
                                "  c42 := d60 IRSHIFT d93\r\n" + //
                                "  d60 := c42\r\n" + //
                                "  c44 := d97 IADD d60\r\n" + //
                                "  e29 := c44\r\n" + //
                                "  c46 := 25\r\n" + //
                                "  c47 := e29 IRSHIFT c46\r\n" + //
                                "  c48 := 1\r\n" + //
                                "  c49 := c47 IAND c48\r\n" + //
                                "  e21 := c49\r\n" + //
                                "  c51 := 1\r\n" + //
                                "  c52 := e21 EQ c51\r\n" + //
                                "  IF c52 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  c53 := 1\r\n" + //
                                "  c54 := e26 IADD c53\r\n" + //
                                "  e26 := c54\r\n" + //
                                "  c56 := 1\r\n" + //
                                "  c57 := e29 IRSHIFT c56\r\n" + //
                                "  e29 := c57\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  c59 := 23\r\n" + //
                                "  c60 := e26 ILSHIFT c59\r\n" + //
                                "  e38 := c60\r\n" + //
                                "  c62 := 31\r\n" + //
                                "  c63 := d27 ILSHIFT c62\r\n" + //
                                "  c64 := e38 IOR c63\r\n" + //
                                "  e38 := c64\r\n" + //
                                "  c66 := e38 IOR e29\r\n" + //
                                "  e38 := c66\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  c68 := d57 ISUB d94\r\n" + //
                                "  d93 := c68\r\n" + //
                                "  d94 := d57\r\n" + //
                                "  e26 := d57\r\n" + //
                                "  c72 := d97 IRSHIFT d93\r\n" + //
                                "  d97 := c72\r\n" + //
                                "  c74 := d97 IADD d60\r\n" + //
                                "  e29 := c74\r\n" + //
                                "  c76 := 25\r\n" + //
                                "  c77 := e29 IRSHIFT c76\r\n" + //
                                "  c78 := 1\r\n" + //
                                "  c79 := c77 IAND c78\r\n" + //
                                "  e21 := c79\r\n" + //
                                "  c81 := 1\r\n" + //
                                "  c82 := e21 EQ c81\r\n" + //
                                "  IF c82 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  c83 := 1\r\n" + //
                                "  c84 := e26 IADD c83\r\n" + //
                                "  e26 := c84\r\n" + //
                                "  c86 := 1\r\n" + //
                                "  c87 := e29 IRSHIFT c86\r\n" + //
                                "  e29 := c87\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  c89 := 23\r\n" + //
                                "  c90 := e26 ILSHIFT c89\r\n" + //
                                "  e38 := c90\r\n" + //
                                "  c92 := 31\r\n" + //
                                "\r\n" + //
                                "  c93 := d27 ILSHIFT c92\r\n" + //
                                "  c94 := e38 IOR c93\r\n" + //
                                "  e38 := c94\r\n" + //
                                "  c96 := e38 IOR e29\r\n" + //
                                "  e38 := c96\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  c98 := 0\r\n" + //
                                "  c99 := b64 EQ c98\r\n" + //
                                "  d10 := 1\r\n" + //
                                "  d11 := b66 EQ d10\r\n" + //
                                "  d12 := c99 LAND d11\r\n" + //
                                "  IF d12 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  d13 := d60 GT d97\r\n" + //
                                "  IF d13 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  d14 := 1\r\n" + //
                                "  d27 := d14\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  d16 := 0\r\n" + //
                                "  d27 := d16\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  d18 := 1\r\n" + //
                                "  d19 := b64 EQ d18\r\n" + //
                                "  d20 := 0\r\n" + //
                                "  d21 := b66 EQ d20\r\n" + //
                                "  d22 := d19 LAND d21\r\n" + //
                                "  IF d22 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  d23 := d60 GE d97\r\n" + //
                                "  IF d23 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  d24 := 0\r\n" + //
                                "  d27 := d24\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  d26 := 1\r\n" + //
                                "  d27 := d26\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  d28 := d94 EQ d57\r\n" + //
                                "  IF d28 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  d29 := 0\r\n" + //
                                "  e29 := d29\r\n" + //
                                "  d31 := 25\r\n" + //
                                "  d32 := e29 IRSHIFT d31\r\n" + //
                                "  d33 := 1\r\n" + //
                                "  d34 := d32 IAND d33\r\n" + //
                                "  e21 := d34\r\n" + //
                                "  e26 := d94\r\n" + //
                                "  d37 := 1\r\n" + //
                                "  d38 := e21 EQ d37\r\n" + //
                                "  IF d38 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  d39 := 1\r\n" + //
                                "  d40 := e26 IADD d39\r\n" + //
                                "  e26 := d40\r\n" + //
                                "  d42 := 1\r\n" + //
                                "  d43 := e29 IRSHIFT d42\r\n" + //
                                "  e29 := d43\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  d45 := 23\r\n" + //
                                "  d46 := e26 ILSHIFT d45\r\n" + //
                                "  e38 := d46\r\n" + //
                                "  d48 := e38 IOR e29\r\n" + //
                                "  e38 := d48\r\n" + //
                                "  d50 := 31\r\n" + //
                                "  d51 := d27 ILSHIFT d50\r\n" + //
                                "  d52 := e38 IOR d51\r\n" + //
                                "  e38 := d52\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  d54 := d94 GT d57\r\n" + //
                                "  IF d54 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  d55 := d94 ISUB d57\r\n" + //
                                "  d93 := d55\r\n" + //
                                "  d57 := d94\r\n" + //
                                "  e26 := d94\r\n" + //
                                "  d59 := d60 IRSHIFT d93\r\n" + //
                                "  d60 := d59\r\n" + //
                                "  d61 := 1\r\n" + //
                                "  d62 := b64 EQ d61\r\n" + //
                                "  d63 := 0\r\n" + //
                                "  d64 := b66 EQ d63\r\n" + //
                                "  d65 := d62 LAND d64\r\n" + //
                                "  IF d65 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  d66 := d60 ISUB d97\r\n" + //
                                "  e29 := d66\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  d68 := d97 ISUB d60\r\n" + //
                                "  e29 := d68\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  d70 := 25\r\n" + //
                                "  d71 := e29 IRSHIFT d70\r\n" + //
                                "  d72 := 1\r\n" + //
                                "  d73 := d71 IAND d72\r\n" + //
                                "  e21 := d73\r\n" + //
                                "  d75 := 1\r\n" + //
                                "  d76 := e21 EQ d75\r\n" + //
                                "  IF d76 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  d77 := 1\r\n" + //
                                "  d78 := e26 IADD d77\r\n" + //
                                "  e26 := d78\r\n" + //
                                "  d80 := 1\r\n" + //
                                "  d81 := e29 IRSHIFT d80\r\n" + //
                                "  e29 := d81\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  d83 := 23\r\n" + //
                                "  d84 := e26 ILSHIFT d83\r\n" + //
                                "  e38 := d84\r\n" + //
                                "  d86 := 31\r\n" + //
                                "  d87 := d27 ILSHIFT d86\r\n" + //
                                "  d88 := e38 IOR d87\r\n" + //
                                "  e38 := d88\r\n" + //
                                "  d90 := e38 IOR e29\r\n" + //
                                "  e38 := d90\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  d92 := d57 ISUB d94\r\n" + //
                                "  d93 := d92\r\n" + //
                                "  d94 := d57\r\n" + //
                                "  e26 := d57\r\n" + //
                                "  d96 := d97 IRSHIFT d93\r\n" + //
                                "  d97 := d96\r\n" + //
                                "  d98 := 1\r\n" + //
                                "  d99 := b64 EQ d98\r\n" + //
                                "  e10 := 0\r\n" + //
                                "  e11 := b66 EQ e10\r\n" + //
                                "  e12 := d99 LAND e11\r\n" + //
                                "  IF e12 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  e13 := d60 ISUB d97\r\n" + //
                                "  e29 := e13\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  e15 := d97 ISUB d60\r\n" + //
                                "  e29 := e15\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  e17 := 25\r\n" + //
                                "  e18 := e29 IRSHIFT e17\r\n" + //
                                "  e19 := 1\r\n" + //
                                "  e20 := e18 IAND e19\r\n" + //
                                "  e21 := e20\r\n" + //
                                "  e22 := 1\r\n" + //
                                "  e23 := e21 EQ e22\r\n" + //
                                "  IF e23 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  e24 := 1\r\n" + //
                                "  e25 := e26 IADD e24\r\n" + //
                                "  e26 := e25\r\n" + //
                                "  e27 := 1\r\n" + //
                                "  e28 := e29 IRSHIFT e27\r\n" + //
                                "  e29 := e28\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  e30 := 23\r\n" + //
                                "  e31 := e26 ILSHIFT e30\r\n" + //
                                "  e38 := e31\r\n" + //
                                "  e33 := 31\r\n" + //
                                "  e34 := d27 ILSHIFT e33\r\n" + //
                                "  e35 := e38 IOR e34\r\n" + //
                                "  e38 := e35\r\n" + //
                                "  e37 := e38 IOR e29\r\n" + //
                                "  e38 := e37\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0\r\n" + //
                                "  CALL IntBinaryAsReal ( e38 -> e40 )\r\n" + //
                                "  e39 <| b30\r\n" + //
                                "  e46 := e39\r\n" + //
                                "  b30 |< e46\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  b46 <- b45\r\n" + //
                                "  b63 := 0\r\n" + //
                                "  b58 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b46 -> b52 )\r\n" + //
                                "  b51 <| b48\r\n" + //
                                "  b58 := b51\r\n" + //
                                "  b59 := 31\r\n" + //
                                "  b60 := b58 IRSHIFT b59\r\n" + //
                                "  b61 := 1\r\n" + //
                                "  b62 := b60 IAND b61\r\n" + //
                                "  b63 := b62\r\n" + //
                                "  b48 |< b63\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  b53 <- b52\r\n" + //
                                "  b56 := 0\r\n" + //
                                "  b57 := 0.0\r\n" + //
                                "  IPARAM b57\r\n" + //
                                "IPARAM b53\r\n" + //
                                "IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b57\r\n" + //
                                "IPARAM b56\r\n" + //
                                "IASM \"STR %r, %a\"\r\n" + //
                                "  b55 |< b56\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  b69 <- b68\r\n" + //
                                "  b80 := 0\r\n" + //
                                "  b75 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b69 -> b52 )\r\n" + //
                                "  b74 <| b71\r\n" + //
                                "  b75 := b74\r\n" + //
                                "  b76 := 23\r\n" + //
                                "  b77 := b75 IRSHIFT b76\r\n" + //
                                "  b78 := 255\r\n" + //
                                "  b79 := b77 IAND b78\r\n" + //
                                "  b80 := b79\r\n" + //
                                "  b71 |< b80\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b86 <- b85\r\n" + //
                                "  b95 := 0\r\n" + //
                                "  b92 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b86 -> b52 )\r\n" + //
                                "  b91 <| b88\r\n" + //
                                "  b92 := b91\r\n" + //
                                "  b93 := 8388607\r\n" + //
                                "  b94 := b92 IAND b93\r\n" + //
                                "  b95 := b94\r\n" + //
                                "  b88 |< b95\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  e41 <- e40\r\n" + //
                                "  e44 := 0.0\r\n" + //
                                "  e45 := 0\r\n" + //
                                "  IPARAM e45\r\n" + //
                                "IPARAM e41\r\n" + //
                                "IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM e45\r\n" + //
                                "IPARAM e44\r\n" + //
                                "IASM \"STR %r, %a\"\r\n" + //
                                "  e43 |< e44\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  e50 <- e49\r\n" + //
                                "  e52 <- e51\r\n" + //
                                "  e97 := 0.0\r\n" + //
                                "  e95 := 0\r\n" + //
                                "  e67 := 0\r\n" + //
                                "  e69 := 0\r\n" + //
                                "  e71 := 0\r\n" + //
                                "  e73 := 0\r\n" + //
                                "  e75 := 0\r\n" + //
                                "  e77 := 0\r\n" + //
                                "  e82 := 0\r\n" + //
                                "  e84 := 0\r\n" + //
                                "  e86 := 0\r\n" + //
                                "  CALL RealSign ( e50 -> b45 )\r\n" + //
                                "  e66 <| e54\r\n" + //
                                "  e67 := e66\r\n" + //
                                "  CALL RealSign ( e52 -> b45 )\r\n" + //
                                "  e68 <| e54\r\n" + //
                                "  e69 := e68\r\n" + //
                                "  CALL RealExponent ( e50 -> b68 )\r\n" + //
                                "  e70 <| e54\r\n" + //
                                "  e71 := e70\r\n" + //
                                "  CALL RealExponent ( e52 -> b68 )\r\n" + //
                                "  e72 <| e54\r\n" + //
                                "  e73 := e72\r\n" + //
                                "  CALL RealMantissa ( e50 -> b85 )\r\n" + //
                                "  e74 <| e54\r\n" + //
                                "  e75 := e74\r\n" + //
                                "  CALL RealMantissa ( e52 -> b85 )\r\n" + //
                                "  e76 <| e54\r\n" + //
                                "  e77 := e76\r\n" + //
                                "  e78 := e67 NE e69\r\n" + //
                                "  IF e78 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  e79 := 1\r\n" + //
                                "  e82 := e79\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  e81 := 0\r\n" + //
                                "  e82 := e81\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0\r\n" + //
                                "  e83 := e75 IMUL e77\r\n" + //
                                "  e84 := e83\r\n" + //
                                "  e85 := e71 IADD e73\r\n" + //
                                "  e86 := e85\r\n" + //
                                "  e87 := 23\r\n" + //
                                "  e88 := e86 ILSHIFT e87\r\n" + //
                                "  e95 := e88\r\n" + //
                                "  e90 := 31\r\n" + //
                                "  e91 := e82 ILSHIFT e90\r\n" + //
                                "  e92 := e95 IOR e91\r\n" + //
                                "  e95 := e92\r\n" + //
                                "  e94 := e95 IOR e84\r\n" + //
                                "  e95 := e94\r\n" + //
                                "  CALL IntBinaryAsReal ( e95 -> e40 )\r\n" + //
                                "  e96 <| e54\r\n" + //
                                "  e97 := e96\r\n" + //
                                "  e54 |< e97\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL p\r\n" + //
                                "  e99 <- e98\r\n" + //
                                "  f11 <- f10\r\n" + //
                                "  f25 := 0\r\n" + //
                                "  CALL IntToReal ( f11 -> X6 )\r\n" + //
                                "  f15 <| f13\r\n" + //
                                "  CALL RAdd ( e99 -> b25 , f15 -> b27 )\r\n" + //
                                "  f16 <| f13\r\n" + //
                                "  CALL Round ( f16 -> f18 )\r\n" + //
                                "  f17 <| f13\r\n" + //
                                "  f25 := f17\r\n" + //
                                "  f13 |< f25\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  f19 <- f18\r\n" + //
                                "  f22 := 0.5\r\n" + //
                                "  CALL RAdd ( f19 -> b25 , f22 -> b27 )\r\n" + //
                                "  f23 <| f21\r\n" + //
                                "  CALL Floor ( f23 -> k )\r\n" + //
                                "  f24 <| l\r\n" + //
                                "  f21 |< f24\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }
}
