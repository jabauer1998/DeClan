package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
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
            parser.close();
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
                        " c64 CONST INTERNAL realBias\r\n" + //
                        "DATA SECTION\r\n" + //
                        " a := 0\r\n" + //
                        " b := 0\r\n" + //
                        " c := 0.0\r\n" + //
                        " d := 0.0\r\n" + //
                        " c63 := 127\r\n" + //
                        " c64 := c63\r\n" + //
                        "CODE SECTION\r\n" + //
                        " h := 1\r\n" + //
                        " b := h\r\n" + //
                        " i := 1.0\r\n" + //
                        " c := i\r\n" + //
                        " j := 2\r\n" + //
                        " a := j\r\n" + //
                        " k := 2.0\r\n" + //
                        " d := k\r\n" + //
                        " CALL WriteInt ( b -> b14 )\r\n" + //
                        " CALL WriteReal ( c -> b16 )\r\n" + //
                        " CALL WriteReal ( d -> b16 )\r\n" + //
                        " CALL WriteLn (  )\r\n" + //
                        " CALL Divide ( b -> b19 , a -> b21 )\r\n" + //
                        " b18 <| b24\r\n" + //
                        " CALL WriteReal ( b18 -> b16 )\r\n" + //
                        " m := 5\r\n" + //
                        " n := b IADD m\r\n" + //
                        " o := 6\r\n" + //
                        " p := b IADD o\r\n" + //
                        " q := n IMUL p\r\n" + //
                        " CALL WriteInt ( q -> b14 )\r\n" + //
                        " r := 4\r\n" + //
                        " s := a IADD r\r\n" + //
                        " t := 5.0\r\n" + //
                        " CALL IntToReal ( a -> b28 )\r\n" + //
                        " d32 <| b31\r\n" + //
                        " CALL RAdd ( d32 -> d34 , t -> d36 )\r\n" + //
                        " d33 <| d39\r\n" + //
                        " CALL IntToReal ( s -> b28 )\r\n" + //
                        " e42 <| b31\r\n" + //
                        " CALL RMul ( e42 -> e44 , d33 -> e45 )\r\n" + //
                        " e43 <| e47\r\n" + //
                        " CALL WriteReal ( e43 -> b16 )\r\n" + //
                        " CALL WriteLn (  )\r\n" + //
                        " y := 3.1415\r\n" + //
                        " CALL p ( b -> e61 , y -> e59 )\r\n" + //
                        " z <| e64\r\n" + //
                        " a := z\r\n" + //
                        " CALL WriteReal ( d -> b16 )\r\n" + //
                        " CALL WriteLn (  )\r\n" + //
                        "END\r\n" + //
                        "PROC SECTION\r\n" + //
                        " PROC LABEL WriteInt\r\n" + //
                        "  b15 <- b14\r\n" + //
                        "  IPARAM b15\r\n" + //
                        "  IASM \"LDR R0, %a\"\r\n" + //
                        "  IASM \"SWI 1\"\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL WriteReal\r\n" + //
                        "  b17 <- b16\r\n" + //
                        "  IPARAM b17\r\n" + //
                        "  IASM \"LDR R0, %a\"\r\n" + //
                        "  IASM \"SWI 2\"\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL WriteLn\r\n" + //
                        "  IASM \"SWI 4\"\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL Divide\r\n" + //
                        "  b20 <- b19\r\n" + //
                        "  b22 <- b21\r\n" + //
                        "  b23 := 0\r\n" + //
                        "  b25 := 0.0\r\n" + //
                        "  b26 := 0.0\r\n" + //
                        "  CALL IntToReal ( b20 -> b28 )\r\n" + //
                        "  b27 <| b31\r\n" + //
                        "  b25 := b27\r\n" + //
                        "  CALL IntToReal ( b22 -> b28 )\r\n" + //
                        "  c31 <| b31\r\n" + //
                        "  b26 := c31\r\n" + //
                        "  CALL RDivide ( b25 -> c33 , b26 -> c34 )\r\n" + //
                        "  c32 <| c35\r\n" + //
                        "  b23 := c32\r\n" + //
                        "  b24 |< b23\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntToReal\r\n" + //
                        "  b29 <- b28\r\n" + //
                        "  b30 := 0.0\r\n" + //
                        "  b32 := 0\r\n" + //
                        "  b33 := FALSE\r\n" + //
                        "  b34 := 0\r\n" + //
                        "  b35 := 0\r\n" + //
                        "  b36 := 0\r\n" + //
                        "  b37 := 0\r\n" + //
                        "  b38 := 0\r\n" + //
                        "  b39 := 0\r\n" + //
                        "  b40 := 0\r\n" + //
                        "  b41 := 0\r\n" + //
                        "  b42 := 0\r\n" + //
                        "  b43 := 0\r\n" + //
                        "  CALL Abs ( b29 -> b45 )\r\n" + //
                        "  b44 <| b48\r\n" + //
                        "  b32 := b44\r\n" + //
                        "  b37 := b32\r\n" + //
                        "  b59 := 0\r\n" + //
                        "  b36 := b59\r\n" + //
                        "  CALL IntIsNegative ( b29 -> b61 )\r\n" + //
                        "  b60 <| b64\r\n" + //
                        "  b33 := b60\r\n" + //
                        "  CALL IntIsZero ( b37 -> b75 )\r\n" + //
                        "  b74 <| b78\r\n" + //
                        "  b83 := BNOT b74\r\n" + //
                        "  IF b83 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF b83 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b84 := 1\r\n" + //
                        "  b85 := b37 IAND b84\r\n" + //
                        "  b38 := b85\r\n" + //
                        "  b86 := 1\r\n" + //
                        "  b87 := b38 EQ b86\r\n" + //
                        "  IF b87 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  b35 := b36\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                        "  b88 := 1\r\n" + //
                        "  b89 := b36 IADD b88\r\n" + //
                        "  b36 := b89\r\n" + //
                        "  b90 := 1\r\n" + //
                        "  b91 := b37 IRSHIFT b90\r\n" + //
                        "  b37 := b91\r\n" + //
                        "  CALL IntIsZero ( b37 -> b75 )\r\n" + //
                        "  b92 <| b78\r\n" + //
                        "  b93 := BNOT b92\r\n" + //
                        "  b83 := b93\r\n" + //
                        "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                        "  b94 := 23\r\n" + //
                        "  b95 := b35 GT b94\r\n" + //
                        "  IF b95 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                        "  b96 := 23\r\n" + //
                        "  b97 := b35 ISUB b96\r\n" + //
                        "  b98 := b32 IRSHIFT b97\r\n" + //
                        "  b32 := b98\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                        "  b99 := 23\r\n" + //
                        "  c10 := b35 LT b99\r\n" + //
                        "  IF c10 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_0\r\n" + //
                        "  c11 := 23\r\n" + //
                        "  c12 := c11 ISUB b35\r\n" + //
                        "  c13 := b32 ILSHIFT c12\r\n" + //
                        "  b32 := c13\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                        "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                        "  c14 := 127\r\n" + //
                        "  c15 := b35 IADD c14\r\n" + //
                        "  b40 := c15\r\n" + //
                        "  c16 := 4194303\r\n" + //
                        "  b42 := c16\r\n" + //
                        "  c17 := b32 IAND b42\r\n" + //
                        "  b41 := c17\r\n" + //
                        "  b34 := b41\r\n" + //
                        "  c18 := 23\r\n" + //
                        "  c19 := b40 ILSHIFT c18\r\n" + //
                        "  c20 := b34 IOR c19\r\n" + //
                        "  b34 := c20\r\n" + //
                        "  IF b33 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  c21 := 1\r\n" + //
                        "  c22 := 31\r\n" + //
                        "  c23 := c21 ILSHIFT c22\r\n" + //
                        "  c24 := b34 IOR c23\r\n" + //
                        "  b34 := c24\r\n" + //
                        "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                        "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                        "  CALL IntBinaryAsReal ( b34 -> c25 )\r\n" + //
                        "  c30 <| c28\r\n" + //
                        "  b30 := c30\r\n" + //
                        "  b31 |< b30\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL Abs\r\n" + //
                        "  b46 <- b45\r\n" + //
                        "  b47 := 0\r\n" + //
                        "  b49 := 0\r\n" + //
                        "  b50 := b46 GE b49\r\n" + //
                        "  IF b50 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b47 := b46\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  CALL INeg ( b46 -> b52 )\r\n" + //
                        "  b51 <| b55\r\n" + //
                        "  b47 := b51\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                        "  b48 |< b47\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL INeg\r\n" + //
                        "  b53 <- b52\r\n" + //
                        "  b54 := 0\r\n" + //
                        "  b56 := INOT b53\r\n" + //
                        "  b57 := 1\r\n" + //
                        "  b58 := b56 IADD b57\r\n" + //
                        "  b54 := b58\r\n" + //
                        "  b55 |< b54\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntIsNegative\r\n" + //
                        "  b62 <- b61\r\n" + //
                        "  b63 := FALSE\r\n" + //
                        "  b65 := 0\r\n" + //
                        "  b66 := 31\r\n" + //
                        "  b67 := b62 IRSHIFT b66\r\n" + //
                        "  b68 := 1\r\n" + //
                        "  b69 := b67 IAND b68\r\n" + //
                        "  b65 := b69\r\n" + //
                        "  b70 := 0\r\n" + //
                        "  b71 := b65 EQ b70\r\n" + //
                        "  IF b71 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b72 := FALSE\r\n" + //
                        "  b63 := b72\r\n" + //
                        "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b73 := TRUE\r\n" + //
                        "  b63 := b73\r\n" + //
                        "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                        "  b64 |< b63\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntIsZero\r\n" + //
                        "  b76 <- b75\r\n" + //
                        "  b77 := FALSE\r\n" + //
                        "  b79 := 0\r\n" + //
                        "  b80 := b76 EQ b79\r\n" + //
                        "  IF b80 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b81 := TRUE\r\n" + //
                        "  b77 := b81\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b82 := FALSE\r\n" + //
                        "  b77 := b82\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                        "  b78 |< b77\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntBinaryAsReal\r\n" + //
                        "  c26 <- c25\r\n" + //
                        "  c27 := 0.0\r\n" + //
                        "  c29 := 0\r\n" + //
                        "  IPARAM c29\r\n" + //
                        "  IPARAM c26\r\n" + //
                        "  IASM \"LDR %r, %a\"\r\n" + //
                        "  IPARAM c29\r\n" + //
                        "  IPARAM c27\r\n" + //
                        "  IASM \"STR %r, %a\"\r\n" + //
                        "  c28 |< c27\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RDivide\r\n" + //
                        "  y9 <- c33\r\n" + //
                        "  z1 <- c34\r\n" + //
                        "  w5 := 0.0\r\n" + //
                        "  w6 := 0\r\n" + //
                        "  w7 := 0\r\n" + //
                        "  w8 := 0\r\n" + //
                        "  w9 := 0\r\n" + //
                        "  x0 := 0\r\n" + //
                        "  x1 := 0\r\n" + //
                        "  x2 := 0\r\n" + //
                        "  x3 := 0\r\n" + //
                        "  x4 := 0\r\n" + //
                        "  x5 := 0\r\n" + //
                        "  x6 := 0\r\n" + //
                        "  x7 := 0\r\n" + //
                        "  x8 := 0\r\n" + //
                        "  x9 := 0\r\n" + //
                        "  y0 := 0\r\n" + //
                        "  y1 := 0\r\n" + //
                        "  y2 := 0\r\n" + //
                        "  y3 := 0\r\n" + //
                        "  y4 := 0\r\n" + //
                        "  y5 := 0\r\n" + //
                        "  y6 := 0\r\n" + //
                        "  y7 := 0\r\n" + //
                        "  y8 := 0\r\n" + //
                        "  CALL RealSign ( y9 -> c36 )\r\n" + //
                        "  z0 <| c39\r\n" + //
                        "  w7 := z0\r\n" + //
                        "  CALL RealSign ( z1 -> c36 )\r\n" + //
                        "  z2 <| c39\r\n" + //
                        "  w8 := z2\r\n" + //
                        "  CALL RealExponent ( y9 -> c51 )\r\n" + //
                        "  z3 <| c54\r\n" + //
                        "  w9 := z3\r\n" + //
                        "  CALL RealExponent ( z1 -> c51 )\r\n" + //
                        "  z4 <| c54\r\n" + //
                        "  x0 := z4\r\n" + //
                        "  CALL RealMantissa ( y9 -> c65 )\r\n" + //
                        "  z5 <| c68\r\n" + //
                        "  x1 := z5\r\n" + //
                        "  CALL RealMantissa ( z1 -> c65 )\r\n" + //
                        "  z6 <| c68\r\n" + //
                        "  x2 := z6\r\n" + //
                        "  z7 := 30\r\n" + //
                        "  z8 := 23\r\n" + //
                        "  z9 := z7 ISUB z8\r\n" + //
                        "  y2 := z9\r\n" + //
                        "  A0 := x1 ILSHIFT y2\r\n" + //
                        "  x1 := A0\r\n" + //
                        "  A1 := 0\r\n" + //
                        "  x6 := A1\r\n" + //
                        "  x8 := x2\r\n" + //
                        "  A2 := 1\r\n" + //
                        "  A3 := x8 IAND A2\r\n" + //
                        "  A4 := 0\r\n" + //
                        "  A5 := A3 EQ A4\r\n" + //
                        "  IF A5 EQ TRUE THEN WHILESTAT_12_SEQ_0_LEVEL_0 ELSE WHILENEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_12_SEQ_0_LEVEL_0\r\n" + //
                        "  IF A5 EQ TRUE THEN WHILESTAT_12_SEQ_0_LEVEL_0 ELSE WHILEEND_12_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  A6 := 1\r\n" + //
                        "  A7 := x8 IRSHIFT A6\r\n" + //
                        "  x8 := A7\r\n" + //
                        "  A8 := 1\r\n" + //
                        "  A9 := x6 IADD A8\r\n" + //
                        "  x6 := A9\r\n" + //
                        "  B0 := 1\r\n" + //
                        "  B1 := x8 IAND B0\r\n" + //
                        "  B2 := 0\r\n" + //
                        "  B3 := B1 EQ B2\r\n" + //
                        "  A5 := B3\r\n" + //
                        "  GOTO WHILECOND_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_12_LEVEL_0\r\n" + //
                        "  x9 := x6\r\n" + //
                        "  y2 := x9\r\n" + //
                        "  B4 := x2 IRSHIFT y2\r\n" + //
                        "  x2 := B4\r\n" + //
                        "  B5 := 0\r\n" + //
                        "  x6 := B5\r\n" + //
                        "  x8 := x2\r\n" + //
                        "  CALL IntIsZero ( x8 -> b75 )\r\n" + //
                        "  B6 <| b78\r\n" + //
                        "  B7 := BNOT B6\r\n" + //
                        "  IF B7 EQ TRUE THEN WHILESTAT_14_SEQ_0_LEVEL_0 ELSE WHILENEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_14_SEQ_0_LEVEL_0\r\n" + //
                        "  IF B7 EQ TRUE THEN WHILESTAT_14_SEQ_0_LEVEL_0 ELSE WHILEEND_14_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  B8 := 1\r\n" + //
                        "  B9 := x8 IAND B8\r\n" + //
                        "  x7 := B9\r\n" + //
                        "  C0 := 1\r\n" + //
                        "  C1 := x7 EQ C0\r\n" + //
                        "  IF C1 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_0 ELSE IFNEXT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  y0 := x6\r\n" + //
                        "  GOTO IFEND_16_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_16_LEVEL_0\r\n" + //
                        "  C2 := 1\r\n" + //
                        "  C3 := x6 IADD C2\r\n" + //
                        "  x6 := C3\r\n" + //
                        "  C4 := 1\r\n" + //
                        "  C5 := x8 IRSHIFT C4\r\n" + //
                        "  x8 := C5\r\n" + //
                        "  CALL IntIsZero ( x8 -> b75 )\r\n" + //
                        "  C6 <| b78\r\n" + //
                        "  C7 := BNOT C6\r\n" + //
                        "  B7 := C7\r\n" + //
                        "  GOTO WHILECOND_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_14_LEVEL_0\r\n" + //
                        "  y1 := y0\r\n" + //
                        "  C8 := w9 ISUB y1\r\n" + //
                        "  w9 := C8\r\n" + //
                        "  C9 := x0 ISUB y1\r\n" + //
                        "  x0 := C9\r\n" + //
                        "  D0 := 30\r\n" + //
                        "  D1 := D0 ISUB y1\r\n" + //
                        "  y8 := D1\r\n" + //
                        "  D2 := w7 NE w8\r\n" + //
                        "  IF D2 EQ TRUE THEN IFSTAT_17_SEQ_0_LEVEL_0 ELSE IFNEXT_17_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_17_SEQ_0_LEVEL_0\r\n" + //
                        "  D3 := 1\r\n" + //
                        "  x3 := D3\r\n" + //
                        "  GOTO IFEND_17_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_17_SEQ_0_LEVEL_0\r\n" + //
                        "  D4 := 0\r\n" + //
                        "  x3 := D4\r\n" + //
                        "  GOTO IFEND_17_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_17_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_17_LEVEL_0\r\n" + //
                        "  D5 := w9 ISUB x0\r\n" + //
                        "  x5 := D5\r\n" + //
                        "  CALL Div ( x1 -> c75 , x2 -> c77 )\r\n" + //
                        "  D6 <| c80\r\n" + //
                        "  x4 := D6\r\n" + //
                        "  D7 := 23\r\n" + //
                        "  D8 := y8 LT D7\r\n" + //
                        "  IF D8 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_0 ELSE IFNEXT_18_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_18_SEQ_0_LEVEL_0\r\n" + //
                        "  D9 := 23\r\n" + //
                        "  E0 := D9 ISUB y8\r\n" + //
                        "  E1 := 1\r\n" + //
                        "  E2 := E0 IADD E1\r\n" + //
                        "  y3 := E2\r\n" + //
                        "  E3 := x4 ILSHIFT y3\r\n" + //
                        "  x4 := E3\r\n" + //
                        "  GOTO IFEND_18_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_18_SEQ_0_LEVEL_0\r\n" + //
                        "  E4 := 23\r\n" + //
                        "  E5 := y8 GT E4\r\n" + //
                        "  IF E5 EQ TRUE THEN IFSTAT_18_SEQ_1_LEVEL_0 ELSE IFNEXT_18_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_18_SEQ_1_LEVEL_0\r\n" + //
                        "  E6 := 23\r\n" + //
                        "  E7 := y8 ISUB E6\r\n" + //
                        "  y3 := E7\r\n" + //
                        "  E8 := x4 IRSHIFT y3\r\n" + //
                        "  x4 := E8\r\n" + //
                        "  GOTO IFEND_18_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_18_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_18_LEVEL_0\r\n" + //
                        "  E9 := 0\r\n" + //
                        "  y6 := E9\r\n" + //
                        "  y7 := x4\r\n" + //
                        "  F0 := 0\r\n" + //
                        "  y4 := F0\r\n" + //
                        "  CALL IntIsZero ( y7 -> b75 )\r\n" + //
                        "  F1 <| b78\r\n" + //
                        "  F2 := BNOT F1\r\n" + //
                        "  IF F2 EQ TRUE THEN WHILESTAT_16_SEQ_0_LEVEL_0 ELSE WHILENEXT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_16_SEQ_0_LEVEL_0\r\n" + //
                        "  IF F2 EQ TRUE THEN WHILESTAT_16_SEQ_0_LEVEL_0 ELSE WHILEEND_16_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  F3 := 1\r\n" + //
                        "  F4 := y7 IAND F3\r\n" + //
                        "  y5 := F4\r\n" + //
                        "  F5 := 1\r\n" + //
                        "  F6 := y5 EQ F5\r\n" + //
                        "  IF F6 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_0 ELSE IFNEXT_19_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_19_SEQ_0_LEVEL_0\r\n" + //
                        "  y4 := y6\r\n" + //
                        "  GOTO IFEND_19_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_19_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_19_LEVEL_0\r\n" + //
                        "  F7 := 1\r\n" + //
                        "  F8 := y6 IADD F7\r\n" + //
                        "  y6 := F8\r\n" + //
                        "  F9 := 1\r\n" + //
                        "  G0 := y7 IRSHIFT F9\r\n" + //
                        "  y7 := G0\r\n" + //
                        "  CALL IntIsZero ( y7 -> b75 )\r\n" + //
                        "  G1 <| b78\r\n" + //
                        "  G2 := BNOT G1\r\n" + //
                        "  F2 := G2\r\n" + //
                        "  GOTO WHILECOND_16_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_16_LEVEL_0\r\n" + //
                        "  G3 := 23\r\n" + //
                        "  G4 := y4 GT G3\r\n" + //
                        "  IF G4 EQ TRUE THEN IFSTAT_20_SEQ_0_LEVEL_0 ELSE IFNEXT_20_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_20_SEQ_0_LEVEL_0\r\n" + //
                        "  G5 := 23\r\n" + //
                        "  G6 := y4 ISUB G5\r\n" + //
                        "  y1 := G6\r\n" + //
                        "  G7 := x4 IRSHIFT y1\r\n" + //
                        "  x4 := G7\r\n" + //
                        "  G8 := x5 IADD y1\r\n" + //
                        "  x5 := G8\r\n" + //
                        "  GOTO IFEND_20_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_20_SEQ_0_LEVEL_0\r\n" + //
                        "  G9 := 23\r\n" + //
                        "  H0 := y4 LT G9\r\n" + //
                        "  IF H0 EQ TRUE THEN IFSTAT_20_SEQ_1_LEVEL_0 ELSE IFNEXT_20_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_20_SEQ_1_LEVEL_0\r\n" + //
                        "  H1 := 23\r\n" + //
                        "  H2 := H1 ISUB y4\r\n" + //
                        "  y1 := H2\r\n" + //
                        "  H3 := x4 ILSHIFT y1\r\n" + //
                        "  x4 := H3\r\n" + //
                        "  H4 := x5 ISUB y1\r\n" + //
                        "  x5 := H4\r\n" + //
                        "  GOTO IFEND_20_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_20_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_20_LEVEL_0\r\n" + //
                        "  H6 := x5 IADD c64\r\n" + //
                        "  x5 := H6\r\n" + //
                        "  H7 := 255\r\n" + //
                        "  H8 := x5 IAND H7\r\n" + //
                        "  H9 := 23\r\n" + //
                        "  I0 := H8 ILSHIFT H9\r\n" + //
                        "  w6 := I0\r\n" + //
                        "  I1 := 31\r\n" + //
                        "  I2 := x3 ILSHIFT I1\r\n" + //
                        "  I3 := w6 IOR I2\r\n" + //
                        "  w6 := I3\r\n" + //
                        "  I4 := 8388607\r\n" + //
                        "  I5 := x4 IAND I4\r\n" + //
                        "  I6 := w6 IOR I5\r\n" + //
                        "  w6 := I6\r\n" + //
                        "  CALL IntBinaryAsReal ( w6 -> c25 )\r\n" + //
                        "  I7 <| c28\r\n" + //
                        "  w5 := I7\r\n" + //
                        "  c35 |< w5\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealSign\r\n" + //
                        "  c37 <- c36\r\n" + //
                        "  c38 := 0\r\n" + //
                        "  c40 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( c37 -> c42 )\r\n" + //
                        "  c41 <| c45\r\n" + //
                        "  c40 := c41\r\n" + //
                        "  c47 := 31\r\n" + //
                        "  c48 := c40 IRSHIFT c47\r\n" + //
                        "  c49 := 1\r\n" + //
                        "  c50 := c48 IAND c49\r\n" + //
                        "  c38 := c50\r\n" + //
                        "  c39 |< c38\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealBinaryAsInt\r\n" + //
                        "  c43 <- c42\r\n" + //
                        "  c44 := 0\r\n" + //
                        "  c46 := 0.0\r\n" + //
                        "  IPARAM c46\r\n" + //
                        "  IPARAM c43\r\n" + //
                        "  IASM \"LDR %r, %a\"\r\n" + //
                        "  IPARAM c46\r\n" + //
                        "  IPARAM c44\r\n" + //
                        "  IASM \"STR %r, %a\"\r\n" + //
                        "  c45 |< c44\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealExponent\r\n" + //
                        "  c52 <- c51\r\n" + //
                        "  c53 := 0\r\n" + //
                        "  c55 := 0\r\n" + //
                        "  c56 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( c52 -> c42 )\r\n" + //
                        "  c57 <| c45\r\n" + //
                        "  c55 := c57\r\n" + //
                        "  c58 := 23\r\n" + //
                        "  c59 := c55 IRSHIFT c58\r\n" + //
                        "  c60 := 255\r\n" + //
                        "  c61 := c59 IAND c60\r\n" + //
                        "  c56 := c61\r\n" + //
                        "  c62 := c56 ISUB c64\r\n" + //
                        "  c53 := c62\r\n" + //
                        "  c54 |< c53\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealMantissa\r\n" + //
                        "  c66 <- c65\r\n" + //
                        "  c69 := 0\r\n" + //
                        "  c70 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( c66 -> c42 )\r\n" + //
                        "  c71 <| c45\r\n" + //
                        "  c70 := c71\r\n" + //
                        "  c72 := 8388607\r\n" + //
                        "  c73 := c70 IAND c72\r\n" + //
                        "  c69 := c73\r\n" + //
                        "  c74 := 8388608\r\n" + //
                        "  c67 := c69 IOR c74\r\n" + //
                        "  c68 |< c67\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL Div\r\n" + //
                        "  c76 <- c75\r\n" + //
                        "  c78 <- c77\r\n" + //
                        "  c81 := 0\r\n" + //
                        "  c79 := 0\r\n" + //
                        "  c82 := 0\r\n" + //
                        "  c83 := 0\r\n" + //
                        "  c84 := 0\r\n" + //
                        "  c85 := 0\r\n" + //
                        "  c86 := 0\r\n" + //
                        "  c87 := 0\r\n" + //
                        "  c88 := 0\r\n" + //
                        "  c82 := c76\r\n" + //
                        "  c89 := 0\r\n" + //
                        "  c85 := c89\r\n" + //
                        "  c90 := 0\r\n" + //
                        "  c83 := c90\r\n" + //
                        "  CALL IntIsZero ( c82 -> b75 )\r\n" + //
                        "  c91 <| b78\r\n" + //
                        "  c92 := BNOT c91\r\n" + //
                        "  IF c92 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  IF c92 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  c93 := 1\r\n" + //
                        "  c94 := c82 IAND c93\r\n" + //
                        "  c84 := c94\r\n" + //
                        "  c95 := 1\r\n" + //
                        "  c96 := c84 EQ c95\r\n" + //
                        "  IF c96 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_2 ELSE IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  c83 := c85\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL IFEND_0_LEVEL_0_2\r\n" + //
                        "  c97 := 1\r\n" + //
                        "  c98 := c85 IADD c97\r\n" + //
                        "  c85 := c98\r\n" + //
                        "  c99 := 1\r\n" + //
                        "  d10 := c82 IRSHIFT c99\r\n" + //
                        "  c82 := d10\r\n" + //
                        "  CALL IntIsZero ( c82 -> b75 )\r\n" + //
                        "  d11 <| b78\r\n" + //
                        "  d12 := BNOT d11\r\n" + //
                        "  c92 := d12\r\n" + //
                        "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                        "  c86 := c83\r\n" + //
                        "  c82 := c76\r\n" + //
                        "  d13 := 0\r\n" + //
                        "  c88 := d13\r\n" + //
                        "  d14 := 0\r\n" + //
                        "  c79 := d14\r\n" + //
                        "  d15 := 0\r\n" + //
                        "  d16 := c86 GE d15\r\n" + //
                        "  IF d16 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF d16 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  d17 := c82 IRSHIFT c86\r\n" + //
                        "  d18 := 1\r\n" + //
                        "  d19 := d17 IAND d18\r\n" + //
                        "  c87 := d19\r\n" + //
                        "  d20 := 1\r\n" + //
                        "  d21 := c88 ILSHIFT d20\r\n" + //
                        "  d22 := d21 IOR c87\r\n" + //
                        "  c88 := d22\r\n" + //
                        "  d23 := c88 GE c78\r\n" + //
                        "  IF d23 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_2 ELSE IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_2\r\n" + //
                        "  d24 := 1\r\n" + //
                        "  d25 := d24 ILSHIFT c86\r\n" + //
                        "  d26 := c79 IOR d25\r\n" + //
                        "  c79 := d26\r\n" + //
                        "  d27 := c88 ISUB c78\r\n" + //
                        "  c88 := d27\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_2\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL IFEND_1_LEVEL_0_2\r\n" + //
                        "  d28 := 1\r\n" + //
                        "  d29 := c86 ISUB d28\r\n" + //
                        "  c86 := d29\r\n" + //
                        "  d30 := 0\r\n" + //
                        "  d31 := c86 GE d30\r\n" + //
                        "  d16 := d31\r\n" + //
                        "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                        "  c80 |< c79\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RAdd\r\n" + //
                        "  d35 <- d34\r\n" + //
                        "  d37 <- d36\r\n" + //
                        "  d38 := 0.0\r\n" + //
                        "  d40 := 0\r\n" + //
                        "  d41 := 0\r\n" + //
                        "  d42 := 0\r\n" + //
                        "  d43 := 0\r\n" + //
                        "  d44 := 0\r\n" + //
                        "  d45 := 0\r\n" + //
                        "  d46 := 0\r\n" + //
                        "  d47 := 0\r\n" + //
                        "  d48 := 0\r\n" + //
                        "  d49 := 0\r\n" + //
                        "  d50 := 0\r\n" + //
                        "  d51 := 0\r\n" + //
                        "  d52 := 0\r\n" + //
                        "  d53 := 0\r\n" + //
                        "  d54 := 0\r\n" + //
                        "  d55 := 0\r\n" + //
                        "  CALL RealSign ( d35 -> c36 )\r\n" + //
                        "  d56 <| c39\r\n" + //
                        "  d41 := d56\r\n" + //
                        "  CALL RealSign ( d37 -> c36 )\r\n" + //
                        "  d57 <| c39\r\n" + //
                        "  d42 := d57\r\n" + //
                        "  CALL RealExponent ( d35 -> c51 )\r\n" + //
                        "  d58 <| c54\r\n" + //
                        "  d44 := d58\r\n" + //
                        "  CALL RealExponent ( d37 -> c51 )\r\n" + //
                        "  d59 <| c54\r\n" + //
                        "  d45 := d59\r\n" + //
                        "  CALL RealMantissa ( d35 -> c65 )\r\n" + //
                        "  d60 <| c68\r\n" + //
                        "  d46 := d60\r\n" + //
                        "  CALL RealMantissa ( d37 -> c65 )\r\n" + //
                        "  d61 <| c68\r\n" + //
                        "  d47 := d61\r\n" + //
                        "  d62 := d44 LT d45\r\n" + //
                        "  IF d62 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_3 ELSE IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                        "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_3\r\n" + //
                        "  d63 := d45 IADD c64\r\n" + //
                        "  d48 := d63\r\n" + //
                        "  d64 := d45 ISUB d44\r\n" + //
                        "  d43 := d64\r\n" + //
                        "  d65 := d46 IRSHIFT d43\r\n" + //
                        "  d46 := d65\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_3\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                        "  d66 := d44 GT d45\r\n" + //
                        "  IF d66 EQ TRUE THEN IFSTAT_0_SEQ_1_LEVEL_0 ELSE IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                        "  LABEL IFSTAT_0_SEQ_1_LEVEL_0\r\n" + //
                        "  d67 := d44 IADD c64\r\n" + //
                        "  d48 := d67\r\n" + //
                        "  d68 := d44 ISUB d45\r\n" + //
                        "  d43 := d68\r\n" + //
                        "  d69 := d47 IRSHIFT d43\r\n" + //
                        "  d47 := d69\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_3\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                        "  d70 := d44 IADD c64\r\n" + //
                        "  d48 := d70\r\n" + //
                        "  GOTO IFEND_0_LEVEL_0_3\r\n" + //
                        "  LABEL IFNEXT_0_SEQ_2_LEVEL_0\r\n" + //
                        "  LABEL IFEND_0_LEVEL_0_3\r\n" + //
                        "  d71 := 0\r\n" + //
                        "  d72 := d41 EQ d71\r\n" + //
                        "  d73 := 1\r\n" + //
                        "  d74 := d42 EQ d73\r\n" + //
                        "  d75 := d72 LAND d74\r\n" + //
                        "  IF d75 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_3 ELSE IFNEXT_1_SEQ_0_LEVEL_0_3\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_3\r\n" + //
                        "  d76 := d46 GT d47\r\n" + //
                        "  IF d76 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_1 ELSE IFNEXT_2_SEQ_0_LEVEL_1\r\n" + //
                        "  LABEL IFSTAT_2_SEQ_0_LEVEL_1\r\n" + //
                        "  d77 := 0\r\n" + //
                        "  d51 := d77\r\n" + //
                        "  d78 := d46 ISUB d47\r\n" + //
                        "  d50 := d78\r\n" + //
                        "  GOTO IFEND_2_LEVEL_1\r\n" + //
                        "  LABEL IFNEXT_2_SEQ_0_LEVEL_1\r\n" + //
                        "  d79 := d46 LT d47\r\n" + //
                        "  IF d79 EQ TRUE THEN IFSTAT_2_SEQ_1_LEVEL_1 ELSE IFNEXT_2_SEQ_1_LEVEL_1\r\n" + //
                        "  LABEL IFSTAT_2_SEQ_1_LEVEL_1\r\n" + //
                        "  d80 := 1\r\n" + //
                        "  d51 := d80\r\n" + //
                        "  d81 := d47 ISUB d46\r\n" + //
                        "  d50 := d81\r\n" + //
                        "  GOTO IFEND_2_LEVEL_1\r\n" + //
                        "  LABEL IFNEXT_2_SEQ_1_LEVEL_1\r\n" + //
                        "  d82 := 0\r\n" + //
                        "  d51 := d82\r\n" + //
                        "  d83 := 0\r\n" + //
                        "  d50 := d83\r\n" + //
                        "  GOTO IFEND_2_LEVEL_1\r\n" + //
                        "  LABEL IFNEXT_2_SEQ_2_LEVEL_1\r\n" + //
                        "  LABEL IFEND_2_LEVEL_1\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_3\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_3\r\n" + //
                        "  d84 := 1\r\n" + //
                        "  d85 := d41 EQ d84\r\n" + //
                        "  d86 := 0\r\n" + //
                        "  d87 := d42 EQ d86\r\n" + //
                        "  d88 := d85 LAND d87\r\n" + //
                        "  IF d88 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                        "  d89 := d46 GT d47\r\n" + //
                        "  IF d89 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_1 ELSE IFNEXT_4_SEQ_0_LEVEL_1\r\n" + //
                        "  LABEL IFSTAT_4_SEQ_0_LEVEL_1\r\n" + //
                        "  d90 := 1\r\n" + //
                        "  d51 := d90\r\n" + //
                        "  d91 := d46 ISUB d47\r\n" + //
                        "  d50 := d91\r\n" + //
                        "  GOTO IFEND_4_LEVEL_1\r\n" + //
                        "  LABEL IFNEXT_4_SEQ_0_LEVEL_1\r\n" + //
                        "  d92 := d46 LT d47\r\n" + //
                        "  IF d92 EQ TRUE THEN IFSTAT_4_SEQ_1_LEVEL_1 ELSE IFNEXT_4_SEQ_1_LEVEL_1\r\n" + //
                        "  LABEL IFSTAT_4_SEQ_1_LEVEL_1\r\n" + //
                        "  d93 := 0\r\n" + //
                        "  d51 := d93\r\n" + //
                        "  d94 := d47 ISUB d46\r\n" + //
                        "  d50 := d94\r\n" + //
                        "  GOTO IFEND_4_LEVEL_1\r\n" + //
                        "  LABEL IFNEXT_4_SEQ_1_LEVEL_1\r\n" + //
                        "  d95 := 0\r\n" + //
                        "  d51 := d95\r\n" + //
                        "  d96 := 0\r\n" + //
                        "  d50 := d96\r\n" + //
                        "  GOTO IFEND_4_LEVEL_1\r\n" + //
                        "  LABEL IFNEXT_4_SEQ_2_LEVEL_1\r\n" + //
                        "  LABEL IFEND_4_LEVEL_1\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_3\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                        "  d51 := d41\r\n" + //
                        "  d97 := d46 IADD d47\r\n" + //
                        "  d50 := d97\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_3\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                        "  LABEL IFEND_1_LEVEL_0_3\r\n" + //
                        "  d98 := 24\r\n" + //
                        "  d99 := d50 IRSHIFT d98\r\n" + //
                        "  e10 := 1\r\n" + //
                        "  e11 := d99 IAND e10\r\n" + //
                        "  d49 := e11\r\n" + //
                        "  e12 := 1\r\n" + //
                        "  e13 := d49 EQ e12\r\n" + //
                        "  IF e13 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_0_0 ELSE IFNEXT_7_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_7_SEQ_0_LEVEL_0_0\r\n" + //
                        "  e14 := 1\r\n" + //
                        "  e15 := d50 IRSHIFT e14\r\n" + //
                        "  d50 := e15\r\n" + //
                        "  e16 := 1\r\n" + //
                        "  e17 := d48 IADD e16\r\n" + //
                        "  d48 := e17\r\n" + //
                        "  GOTO IFEND_7_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_7_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_7_LEVEL_0_0\r\n" + //
                        "  d52 := d50\r\n" + //
                        "  e18 := 0\r\n" + //
                        "  d55 := e18\r\n" + //
                        "  CALL IntIsZero ( d52 -> b75 )\r\n" + //
                        "  e19 <| b78\r\n" + //
                        "  e20 := BNOT e19\r\n" + //
                        "  IF e20 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  IF e20 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILEEND_0_LEVEL_0_2\r\n" + //
                        "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  e21 := 1\r\n" + //
                        "  e22 := d52 IAND e21\r\n" + //
                        "  d54 := e22\r\n" + //
                        "  e23 := 1\r\n" + //
                        "  e24 := d54 EQ e23\r\n" + //
                        "  IF e24 EQ TRUE THEN IFSTAT_8_SEQ_0_LEVEL_0_0 ELSE IFNEXT_8_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_8_SEQ_0_LEVEL_0_0\r\n" + //
                        "  d53 := d55\r\n" + //
                        "  GOTO IFEND_8_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_8_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_8_LEVEL_0_0\r\n" + //
                        "  e25 := 1\r\n" + //
                        "  e26 := d55 IADD e25\r\n" + //
                        "  d55 := e26\r\n" + //
                        "  g4 := 1\r\n" + //
                        "  g5 := d52 IRSHIFT g4\r\n" + //
                        "  d52 := g5\r\n" + //
                        "  CALL IntIsZero ( d52 -> b75 )\r\n" + //
                        "  g6 <| b78\r\n" + //
                        "  g7 := BNOT g6\r\n" + //
                        "  e20 := g7\r\n" + //
                        "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                        "  LABEL WHILEEND_0_LEVEL_0_2\r\n" + //
                        "  g8 := 23\r\n" + //
                        "  g9 := d53 LT g8\r\n" + //
                        "  IF g9 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_0_0 ELSE IFNEXT_9_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_9_SEQ_0_LEVEL_0_0\r\n" + //
                        "  e27 := 23\r\n" + //
                        "  e28 := e27 ISUB d53\r\n" + //
                        "  d43 := e28\r\n" + //
                        "  e29 := d50 ILSHIFT d43\r\n" + //
                        "  d50 := e29\r\n" + //
                        "  e30 := d48 ISUB d43\r\n" + //
                        "  d48 := e30\r\n" + //
                        "  GOTO IFEND_9_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_9_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_9_LEVEL_0_0\r\n" + //
                        "  e31 := 255\r\n" + //
                        "  e32 := d48 IAND e31\r\n" + //
                        "  e33 := 23\r\n" + //
                        "  e34 := e32 ILSHIFT e33\r\n" + //
                        "  d40 := e34\r\n" + //
                        "  e35 := 8388607\r\n" + //
                        "  e36 := d50 IAND e35\r\n" + //
                        "  e37 := d40 IOR e36\r\n" + //
                        "  d40 := e37\r\n" + //
                        "  e38 := 31\r\n" + //
                        "  e39 := d51 ILSHIFT e38\r\n" + //
                        "  e40 := d40 IOR e39\r\n" + //
                        "  d40 := e40\r\n" + //
                        "  CALL IntBinaryAsReal ( d40 -> c25 )\r\n" + //
                        "  e41 <| c28\r\n" + //
                        "  d38 := e41\r\n" + //
                        "  d39 |< d38\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RMul\r\n" + //
                        "  l5 <- e44\r\n" + //
                        "  l7 <- e45\r\n" + //
                        "  e46 := 0.0\r\n" + //
                        "  e48 := 0\r\n" + //
                        "  e49 := 0\r\n" + //
                        "  e50 := 0\r\n" + //
                        "  e51 := 0\r\n" + //
                        "  e52 := 0\r\n" + //
                        "  e53 := 0\r\n" + //
                        "  e54 := 0\r\n" + //
                        "  e55 := 0\r\n" + //
                        "  e56 := 0\r\n" + //
                        "  e57 := 0\r\n" + //
                        "  e58 := 0\r\n" + //
                        "  k2 := 0\r\n" + //
                        "  k3 := 0\r\n" + //
                        "  k4 := 0\r\n" + //
                        "  k5 := 0\r\n" + //
                        "  k6 := 0\r\n" + //
                        "  k7 := 0\r\n" + //
                        "  k8 := 0\r\n" + //
                        "  k9 := 0\r\n" + //
                        "  l0 := 0\r\n" + //
                        "  l1 := 0\r\n" + //
                        "  l2 := 0\r\n" + //
                        "  l3 := 0\r\n" + //
                        "  l4 := 0\r\n" + //
                        "  CALL RealSign ( l5 -> c36 )\r\n" + //
                        "  l6 <| c39\r\n" + //
                        "  e49 := l6\r\n" + //
                        "  CALL RealSign ( l7 -> c36 )\r\n" + //
                        "  l8 <| c39\r\n" + //
                        "  e50 := l8\r\n" + //
                        "  CALL RealExponent ( l5 -> c51 )\r\n" + //
                        "  l9 <| c54\r\n" + //
                        "  e51 := l9\r\n" + //
                        "  CALL RealExponent ( l7 -> c51 )\r\n" + //
                        "  m0 <| c54\r\n" + //
                        "  e52 := m0\r\n" + //
                        "  CALL RealMantissa ( l5 -> c65 )\r\n" + //
                        "  m1 <| c68\r\n" + //
                        "  e53 := m1\r\n" + //
                        "  CALL RealMantissa ( l7 -> c65 )\r\n" + //
                        "  m2 <| c68\r\n" + //
                        "  e54 := m2\r\n" + //
                        "  m3 := 1\r\n" + //
                        "  m4 := e53 IAND m3\r\n" + //
                        "  m5 := 1\r\n" + //
                        "  m6 := m4 NE m5\r\n" + //
                        "  IF m6 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  IF m6 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_1 ELSE WHILEEND_2_LEVEL_0_1\r\n" + //
                        "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  m7 := 1\r\n" + //
                        "  m8 := e53 IRSHIFT m7\r\n" + //
                        "  e53 := m8\r\n" + //
                        "  m9 := 1\r\n" + //
                        "  n0 := e53 IAND m9\r\n" + //
                        "  n1 := 1\r\n" + //
                        "  n2 := n0 NE n1\r\n" + //
                        "  m6 := n2\r\n" + //
                        "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                        "  LABEL WHILEEND_2_LEVEL_0_1\r\n" + //
                        "  n3 := 0\r\n" + //
                        "  k7 := n3\r\n" + //
                        "  l1 := e53\r\n" + //
                        "  CALL IntIsZero ( l1 -> b75 )\r\n" + //
                        "  n4 <| b78\r\n" + //
                        "  n5 := BNOT n4\r\n" + //
                        "  IF n5 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF n5 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_0 ELSE WHILEEND_4_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  n6 := 1\r\n" + //
                        "  n7 := l1 IAND n6\r\n" + //
                        "  l2 := n7\r\n" + //
                        "  n8 := 1\r\n" + //
                        "  n9 := l2 EQ n8\r\n" + //
                        "  IF n9 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_0_0 ELSE IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_10_SEQ_0_LEVEL_0_0\r\n" + //
                        "  k6 := k7\r\n" + //
                        "  GOTO IFEND_10_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_10_LEVEL_0_0\r\n" + //
                        "  o0 := 1\r\n" + //
                        "  o1 := k7 IADD o0\r\n" + //
                        "  k7 := o1\r\n" + //
                        "  o2 := 1\r\n" + //
                        "  o3 := l1 IRSHIFT o2\r\n" + //
                        "  l1 := o3\r\n" + //
                        "  CALL IntIsZero ( l1 -> b75 )\r\n" + //
                        "  o4 <| b78\r\n" + //
                        "  o5 := BNOT o4\r\n" + //
                        "  n5 := o5\r\n" + //
                        "  GOTO WHILECOND_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_4_LEVEL_0_0\r\n" + //
                        "  o6 := 1\r\n" + //
                        "  o7 := e54 IAND o6\r\n" + //
                        "  o8 := 1\r\n" + //
                        "  o9 := o7 NE o8\r\n" + //
                        "  IF o9 EQ TRUE THEN WHILESTAT_6_SEQ_0_LEVEL_0 ELSE WHILENEXT_6_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_6_SEQ_0_LEVEL_0\r\n" + //
                        "  IF o9 EQ TRUE THEN WHILESTAT_6_SEQ_0_LEVEL_0 ELSE WHILEEND_6_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_6_SEQ_0_LEVEL_0\r\n" + //
                        "  p0 := 1\r\n" + //
                        "  p1 := e54 IRSHIFT p0\r\n" + //
                        "  e54 := p1\r\n" + //
                        "  p2 := 1\r\n" + //
                        "  p3 := e54 IAND p2\r\n" + //
                        "  p4 := 1\r\n" + //
                        "  p5 := p3 NE p4\r\n" + //
                        "  o9 := p5\r\n" + //
                        "  GOTO WHILECOND_6_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_6_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_6_LEVEL_0\r\n" + //
                        "  p6 := 0\r\n" + //
                        "  k9 := p6\r\n" + //
                        "  l0 := e54\r\n" + //
                        "  CALL IntIsZero ( l0 -> b75 )\r\n" + //
                        "  p7 <| b78\r\n" + //
                        "  p8 := BNOT p7\r\n" + //
                        "  IF p8 EQ TRUE THEN WHILESTAT_8_SEQ_0_LEVEL_0 ELSE WHILENEXT_8_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_8_SEQ_0_LEVEL_0\r\n" + //
                        "  IF p8 EQ TRUE THEN WHILESTAT_8_SEQ_0_LEVEL_0 ELSE WHILEEND_8_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_8_SEQ_0_LEVEL_0\r\n" + //
                        "  p9 := 1\r\n" + //
                        "  q0 := l0 IAND p9\r\n" + //
                        "  l3 := q0\r\n" + //
                        "  q1 := 1\r\n" + //
                        "  q2 := l3 EQ q1\r\n" + //
                        "  IF q2 EQ TRUE THEN IFSTAT_11_SEQ_0_LEVEL_0 ELSE IFNEXT_11_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_11_SEQ_0_LEVEL_0\r\n" + //
                        "  k8 := k9\r\n" + //
                        "  GOTO IFEND_11_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_11_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_11_LEVEL_0\r\n" + //
                        "  q3 := 1\r\n" + //
                        "  q4 := k9 IADD q3\r\n" + //
                        "  k9 := q4\r\n" + //
                        "  q5 := 1\r\n" + //
                        "  q6 := l0 IRSHIFT q5\r\n" + //
                        "  l0 := q6\r\n" + //
                        "  CALL IntIsZero ( l0 -> b75 )\r\n" + //
                        "  q7 <| b78\r\n" + //
                        "  q8 := BNOT q7\r\n" + //
                        "  p8 := q8\r\n" + //
                        "  GOTO WHILECOND_8_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_8_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_8_LEVEL_0\r\n" + //
                        "  q9 := k6 IADD k8\r\n" + //
                        "  l4 := q9\r\n" + //
                        "  r0 := e49 NE e50\r\n" + //
                        "  IF r0 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_0 ELSE IFNEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  r1 := 1\r\n" + //
                        "  e55 := r1\r\n" + //
                        "  GOTO IFEND_12_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  r2 := 0\r\n" + //
                        "  e55 := r2\r\n" + //
                        "  GOTO IFEND_12_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_12_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_12_LEVEL_0\r\n" + //
                        "  r3 := e51 IADD e52\r\n" + //
                        "  e57 := r3\r\n" + //
                        "  r4 := e53 IMUL e54\r\n" + //
                        "  e56 := r4\r\n" + //
                        "  r5 := 23\r\n" + //
                        "  r6 := l4 LT r5\r\n" + //
                        "  IF r6 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_0 ELSE IFNEXT_13_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_13_SEQ_0_LEVEL_0\r\n" + //
                        "  r7 := 23\r\n" + //
                        "  r8 := r7 ISUB l4\r\n" + //
                        "  k5 := r8\r\n" + //
                        "  r9 := e56 ILSHIFT k5\r\n" + //
                        "  e56 := r9\r\n" + //
                        "  GOTO IFEND_13_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_13_SEQ_0_LEVEL_0\r\n" + //
                        "  s0 := 23\r\n" + //
                        "  s1 := l4 GT s0\r\n" + //
                        "  IF s1 EQ TRUE THEN IFSTAT_13_SEQ_1_LEVEL_0 ELSE IFNEXT_13_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_13_SEQ_1_LEVEL_0\r\n" + //
                        "  s2 := 23\r\n" + //
                        "  s3 := l4 ISUB s2\r\n" + //
                        "  k5 := s3\r\n" + //
                        "  s4 := e56 IRSHIFT k5\r\n" + //
                        "  e56 := s4\r\n" + //
                        "  GOTO IFEND_13_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_13_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_13_LEVEL_0\r\n" + //
                        "  s5 := 0\r\n" + //
                        "  e58 := s5\r\n" + //
                        "  k3 := e56\r\n" + //
                        "  CALL IntIsZero ( k3 -> b75 )\r\n" + //
                        "  s6 <| b78\r\n" + //
                        "  s7 := BNOT s6\r\n" + //
                        "  IF s7 EQ TRUE THEN WHILESTAT_10_SEQ_0_LEVEL_0 ELSE WHILENEXT_10_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_10_SEQ_0_LEVEL_0\r\n" + //
                        "  IF s7 EQ TRUE THEN WHILESTAT_10_SEQ_0_LEVEL_0 ELSE WHILEEND_10_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_10_SEQ_0_LEVEL_0\r\n" + //
                        "  s8 := 1\r\n" + //
                        "  s9 := k3 IAND s8\r\n" + //
                        "  k4 := s9\r\n" + //
                        "  t0 := 1\r\n" + //
                        "  t1 := k4 EQ t0\r\n" + //
                        "  IF t1 EQ TRUE THEN IFSTAT_14_SEQ_0_LEVEL_0 ELSE IFNEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  k2 := e58\r\n" + //
                        "  GOTO IFEND_14_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_14_LEVEL_0\r\n" + //
                        "  t2 := 1\r\n" + //
                        "  t3 := e58 IADD t2\r\n" + //
                        "  e58 := t3\r\n" + //
                        "  t4 := 1\r\n" + //
                        "  t5 := k3 IRSHIFT t4\r\n" + //
                        "  k3 := t5\r\n" + //
                        "  CALL IntIsZero ( k3 -> b75 )\r\n" + //
                        "  t6 <| b78\r\n" + //
                        "  t7 := BNOT t6\r\n" + //
                        "  s7 := t7\r\n" + //
                        "  GOTO WHILECOND_10_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_10_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_10_LEVEL_0\r\n" + //
                        "  t8 := 23\r\n" + //
                        "  t9 := k2 GT t8\r\n" + //
                        "  IF t9 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_0 ELSE IFNEXT_15_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_15_SEQ_0_LEVEL_0\r\n" + //
                        "  u0 := 23\r\n" + //
                        "  u1 := k2 ISUB u0\r\n" + //
                        "  k5 := u1\r\n" + //
                        "  u2 := e56 IRSHIFT k5\r\n" + //
                        "  e56 := u2\r\n" + //
                        "  u3 := e57 IADD k5\r\n" + //
                        "  e57 := u3\r\n" + //
                        "  GOTO IFEND_15_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_15_SEQ_0_LEVEL_0\r\n" + //
                        "  u4 := 23\r\n" + //
                        "  u5 := k2 LT u4\r\n" + //
                        "  IF u5 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_0 ELSE IFNEXT_15_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_15_SEQ_1_LEVEL_0\r\n" + //
                        "  u6 := 23\r\n" + //
                        "  u7 := u6 ISUB k2\r\n" + //
                        "  k5 := u7\r\n" + //
                        "  u8 := e56 ILSHIFT k5\r\n" + //
                        "  e56 := u8\r\n" + //
                        "  u9 := e57 ISUB k5\r\n" + //
                        "  e57 := u9\r\n" + //
                        "  GOTO IFEND_15_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_15_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_15_LEVEL_0\r\n" + //
                        "  v1 := e57 IADD c64\r\n" + //
                        "  e57 := v1\r\n" + //
                        "  v2 := 255\r\n" + //
                        "  v3 := e57 IAND v2\r\n" + //
                        "  v4 := 23\r\n" + //
                        "  v5 := v3 ILSHIFT v4\r\n" + //
                        "  e48 := v5\r\n" + //
                        "  v6 := 1\r\n" + //
                        "  v7 := e55 IAND v6\r\n" + //
                        "  v8 := 31\r\n" + //
                        "  v9 := v7 ILSHIFT v8\r\n" + //
                        "  w0 := e48 IOR v9\r\n" + //
                        "  e48 := w0\r\n" + //
                        "  w1 := 8388607\r\n" + //
                        "  w2 := e56 IAND w1\r\n" + //
                        "  w3 := e48 IOR w2\r\n" + //
                        "  e48 := w3\r\n" + //
                        "  CALL IntBinaryAsReal ( e48 -> c25 )\r\n" + //
                        "  w4 <| c28\r\n" + //
                        "  e46 := w4\r\n" + //
                        "  e47 |< e46\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL p\r\n" + //
                        "  e60 <- e59\r\n" + //
                        "  e62 <- e61\r\n" + //
                        "  e63 := 0\r\n" + //
                        "  CALL IntToReal ( e62 -> b28 )\r\n" + //
                        "  e65 <| b31\r\n" + //
                        "  CALL RAdd ( e60 -> d34 , e65 -> d36 )\r\n" + //
                        "  e66 <| d39\r\n" + //
                        "  CALL Round ( e66 -> e68 )\r\n" + //
                        "  e67 <| e71\r\n" + //
                        "  e63 := e67\r\n" + //
                        "  e64 |< e63\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL Round\r\n" + //
                        "  e69 <- e68\r\n" + //
                        "  e70 := 0\r\n" + //
                        "  e72 := 0.5\r\n" + //
                        "  CALL RAdd ( e69 -> d34 , e72 -> d36 )\r\n" + //
                        "  e73 <| d39\r\n" + //
                        "  CALL Floor ( e73 -> e74 )\r\n" + //
                        "  f17 <| e77\r\n" + //
                        "  e70 := f17\r\n" + //
                        "  e71 |< e70\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL Floor\r\n" + //
                        "  e75 <- e74\r\n" + //
                        "  e76 := 0\r\n" + //
                        "  CALL RealToInt ( e75 -> e79 )\r\n" + //
                        "  e78 <| e82\r\n" + //
                        "  e76 := e78\r\n" + //
                        "  e77 |< e76\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealToInt\r\n" + //
                        "  e80 <- e79\r\n" + //
                        "  e81 := 0\r\n" + //
                        "  e83 := 0\r\n" + //
                        "  e84 := 0\r\n" + //
                        "  e85 := 0\r\n" + //
                        "  e86 := 0\r\n" + //
                        "  CALL RealExponent ( e80 -> c51 )\r\n" + //
                        "  e87 <| c54\r\n" + //
                        "  e83 := e87\r\n" + //
                        "  CALL RealMantissa ( e80 -> c65 )\r\n" + //
                        "  e88 <| c68\r\n" + //
                        "  e84 := e88\r\n" + //
                        "  CALL RealSign ( e80 -> c36 )\r\n" + //
                        "  e89 <| c39\r\n" + //
                        "  e85 := e89\r\n" + //
                        "  e90 := 0\r\n" + //
                        "  e91 := e83 GT e90\r\n" + //
                        "  IF e91 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_3_SEQ_0_LEVEL_0_0\r\n" + //
                        "  e92 := e84 ILSHIFT e83\r\n" + //
                        "  e86 := e92\r\n" + //
                        "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                        "  e93 := 0\r\n" + //
                        "  e94 := e83 LT e93\r\n" + //
                        "  IF e94 EQ TRUE THEN IFSTAT_3_SEQ_1_LEVEL_0 ELSE IFNEXT_3_SEQ_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_3_SEQ_1_LEVEL_0\r\n" + //
                        "  e95 := e84 IRSHIFT e83\r\n" + //
                        "  e86 := e95\r\n" + //
                        "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_3_SEQ_1_LEVEL_0_0\r\n" + //
                        "  e86 := e84\r\n" + //
                        "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_3_SEQ_2_LEVEL_0\r\n" + //
                        "  LABEL IFEND_3_LEVEL_0_0\r\n" + //
                        "  e96 := 23\r\n" + //
                        "  e97 := e86 IRSHIFT e96\r\n" + //
                        "  e81 := e97\r\n" + //
                        "  e98 := 1\r\n" + //
                        "  e99 := e85 EQ e98\r\n" + //
                        "  IF e99 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_0_0 ELSE IFNEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  CALL IntToBool ( e81 -> f10 )\r\n" + //
                        "  f13 <| f12\r\n" + //
                        "  f14 := BNOT f13\r\n" + //
                        "  e81 := f14\r\n" + //
                        "  f15 := 1\r\n" + //
                        "  f16 := e81 IADD f15\r\n" + //
                        "  e81 := f16\r\n" + //
                        "  GOTO IFEND_4_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_4_LEVEL_0_0\r\n" + //
                        "  e82 |< e81\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntToBool\r\n" + //
                        "  f5 <- f10\r\n" + //
                        "  f11 := FALSE\r\n" + //
                        "  CALL IntIsZero ( f5 -> b75 )\r\n" + //
                        "  f6 <| b78\r\n" + //
                        "  IF f6 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_0 ELSE IFNEXT_7_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_7_SEQ_0_LEVEL_0\r\n" + //
                        "  f7 := FALSE\r\n" + //
                        "  f11 := f7\r\n" + //
                        "  GOTO IFEND_7_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_7_SEQ_0_LEVEL_0\r\n" + //
                        "  f8 := TRUE\r\n" + //
                        "  f11 := f8\r\n" + //
                        "  GOTO IFEND_7_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_7_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_7_LEVEL_0\r\n" + //
                        "  f12 |< f11\r\n" + //
                        " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testExpressions(){
        String progSrc = "test_source/expressions.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " c75 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 0\r\n" + //
                                " b := a\r\n" + //
                                " c := 1.2\r\n" + //
                                " d := c\r\n" + //
                                " e := 3.14\r\n" + //
                                " CALL RNeg ( e -> b15 )\r\n" + //
                                " b14 <| b16\r\n" + //
                                " CALL IntToReal ( b -> b28 )\r\n" + //
                                " b27 <| b31\r\n" + //
                                " c74 := 127\r\n" + //
                                " c75 := c74\r\n" + //
                                " CALL RAdd ( b14 -> c27 , b27 -> c29 )\r\n" + //
                                " c26 <| c32\r\n" + //
                                " i := c26\r\n" + //
                                " j := 6\r\n" + //
                                " k := 6\r\n" + //
                                " l := 1\r\n" + //
                                " m := k IADD l\r\n" + //
                                " n := j IMUL m\r\n" + //
                                " o := n\r\n" + //
                                " d69 <| b31\r\n" + //
                                " CALL RMul ( d69 -> d71 , d -> d72 )\r\n" + //
                                " d70 <| d74\r\n" + //
                                " CALL IntToReal ( o -> b28 )\r\n" + //
                                " d86 <| b31\r\n" + //
                                " CALL RDivide ( i -> d88 , d86 -> d89 )\r\n" + //
                                " d87 <| d90\r\n" + //
                                " CALL RNotEqualTo ( d70 -> e49 , d87 -> e50 )\r\n" + //
                                " e48 <| e51\r\n" + //
                                " u := e48\r\n" + //
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
                                " CALL RDivide ( d -> d88 , d -> d89 )\r\n" + //
                                " e62 <| d90\r\n" + //
                                " v := e62\r\n" + //
                                " F := 10\r\n" + //
                                " CALL Mod ( o -> e64 , F -> e66 )\r\n" + //
                                " e63 <| e69\r\n" + //
                                " y := e63\r\n" + //
                                " CALL RNeg ( i -> b15 )\r\n" + //
                                " e78 <| b16\r\n" + //
                                " CALL IntToReal ( b -> b28 )\r\n" + //
                                " e79 <| b31\r\n" + //
                                " CALL RMul ( d -> d71 , e79 -> d72 )\r\n" + //
                                " e80 <| d74\r\n" + //
                                " CALL RSub ( e78 -> e82 , e80 -> e84 )\r\n" + //
                                " e81 <| e87\r\n" + //
                                " w := e81\r\n" + //
                                " CALL WriteInt ( b -> e90 )\r\n" + //
                                " CALL WriteReal ( v -> e92 )\r\n" + //
                                " CALL WriteReal ( v -> e92 )\r\n" + //
                                " CALL WriteReal ( w -> e92 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Div ( o -> d91 , y -> d93 )\r\n" + //
                                " e94 <| d96\r\n" + //
                                " z := e94\r\n" + //
                                " CALL Divide ( o -> e96 , y -> e98 )\r\n" + //
                                " e95 <| f11\r\n" + //
                                " x := e95\r\n" + //
                                " CALL WriteInt ( z -> e90 )\r\n" + //
                                " CALL WriteReal ( x -> e92 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Round ( i -> f18 )\r\n" + //
                                " f17 <| f21\r\n" + //
                                " x := f17\r\n" + //
                                " CALL WriteReal ( x -> e92 )\r\n" + //
                                " IF u EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                " LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                " O := 2\r\n" + //
                                " P := 2\r\n" + //
                                " Q := O IMUL P\r\n" + //
                                " CALL WriteInt ( Q -> e90 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                " R := 10\r\n" + //
                                " CALL Divide ( o -> e96 , R -> e98 )\r\n" + //
                                " f58 <| f11\r\n" + //
                                " CALL WriteReal ( f58 -> e92 )\r\n" + //
                                " GOTO IFEND_0_LEVEL_0\r\n" + //
                                " LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL IFEND_0_LEVEL_0\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " T := BNOT u\r\n" + //
                                " CALL IntToReal ( b -> b28 )\r\n" + //
                                " f59 <| b31\r\n" + //
                                " CALL RGreaterThan ( v -> f61 , f59 -> f62 )\r\n" + //
                                " f60 <| f63\r\n" + //
                                " W := T LAND f60\r\n" + //
                                " B := W\r\n" + //
                                " X := BNOT u\r\n" + //
                                " CALL IntToReal ( b -> b28 )\r\n" + //
                                " f91 <| b31\r\n" + //
                                " CALL RGreaterThanOrEqualTo ( v -> f93 , f91 -> f94 )\r\n" + //
                                " f92 <| f95\r\n" + //
                                " a0 := X LOR f92\r\n" + //
                                " C := a0\r\n" + //
                                " a1 := B EQ C\r\n" + //
                                " D := a1\r\n" + //
                                " IF B EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_4 ELSE IFNEXT_1_SEQ_0_LEVEL_0_4\r\n" + //
                                " LABEL IFSTAT_1_SEQ_0_LEVEL_0_4\r\n" + //
                                " a2 := 4\r\n" + //
                                " CALL WriteInt ( a2 -> e90 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_4\r\n" + //
                                " LABEL IFNEXT_1_SEQ_0_LEVEL_0_4\r\n" + //
                                " IF C EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL IFSTAT_1_SEQ_1_LEVEL_0\r\n" + //
                                " a3 := 5\r\n" + //
                                " CALL WriteInt ( a3 -> e90 )\r\n" + //
                                " GOTO IFEND_1_LEVEL_0_4\r\n" + //
                                " LABEL IFNEXT_1_SEQ_1_LEVEL_0\r\n" + //
                                " LABEL IFEND_1_LEVEL_0_4\r\n" + //
                                " IF D EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_2 ELSE IFNEXT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " LABEL IFSTAT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " a4 := 5\r\n" + //
                                " CALL WriteInt ( a4 -> e90 )\r\n" + //
                                " GOTO IFEND_2_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_2_SEQ_0_LEVEL_0_2\r\n" + //
                                " a5 := 6\r\n" + //
                                " CALL WriteInt ( a5 -> e90 )\r\n" + //
                                " GOTO IFEND_2_LEVEL_0_2\r\n" + //
                                " LABEL IFNEXT_2_SEQ_1_LEVEL_0_1\r\n" + //
                                " LABEL IFEND_2_LEVEL_0_2\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RNeg\r\n" + //
                                "  K9 <- b15\r\n" + //
                                "  K2 := 0\r\n" + //
                                "  K3 := 0\r\n" + //
                                "  K4 := 0\r\n" + //
                                "  K5 := 0\r\n" + //
                                "  K6 := 1\r\n" + //
                                "  K7 := 31\r\n" + //
                                "  K8 := K6 ILSHIFT K7\r\n" + //
                                "  K2 := K8\r\n" + //
                                "  CALL RealBinaryAsInt ( K9 -> b17 )\r\n" + //
                                "  L0 <| b20\r\n" + //
                                "  K3 := L0\r\n" + //
                                "  L1 := K3 IXOR K2\r\n" + //
                                "  K5 := L1\r\n" + //
                                "  CALL IntBinaryAsReal ( K5 -> b22 )\r\n" + //
                                "  L2 <| b25\r\n" + //
                                "  K4 := L2\r\n" + //
                                "  b16 |< K4\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  b18 <- b17\r\n" + //
                                "  b19 := 0\r\n" + //
                                "  b21 := 0.0\r\n" + //
                                "  IPARAM b21\r\n" + //
                                "  IPARAM b18\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b21\r\n" + //
                                "  IPARAM b19\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  b20 |< b19\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  b23 <- b22\r\n" + //
                                "  b24 := 0.0\r\n" + //
                                "  b26 := 0\r\n" + //
                                "  IPARAM b26\r\n" + //
                                "  IPARAM b23\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM b26\r\n" + //
                                "  IPARAM b24\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  b25 |< b24\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  b29 <- b28\r\n" + //
                                "  b30 := 0.0\r\n" + //
                                "  b32 := 0\r\n" + //
                                "  b33 := FALSE\r\n" + //
                                "  b34 := 0\r\n" + //
                                "  b35 := 0\r\n" + //
                                "  b36 := 0\r\n" + //
                                "  b37 := 0\r\n" + //
                                "  b38 := 0\r\n" + //
                                "  b39 := 0\r\n" + //
                                "  b40 := 0\r\n" + //
                                "  b41 := 0\r\n" + //
                                "  b42 := 0\r\n" + //
                                "  b43 := 0\r\n" + //
                                "  CALL Abs ( b29 -> b45 )\r\n" + //
                                "  b44 <| b48\r\n" + //
                                "  b32 := b44\r\n" + //
                                "  b37 := b32\r\n" + //
                                "  b59 := 0\r\n" + //
                                "  b36 := b59\r\n" + //
                                "  CALL IntIsNegative ( b29 -> b61 )\r\n" + //
                                "  b60 <| b64\r\n" + //
                                "  b33 := b60\r\n" + //
                                "  CALL IntIsZero ( b37 -> b75 )\r\n" + //
                                "  b74 <| b78\r\n" + //
                                "  b83 := BNOT b74\r\n" + //
                                "  IF b83 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF b83 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b84 := 1\r\n" + //
                                "  b85 := b37 IAND b84\r\n" + //
                                "  b38 := b85\r\n" + //
                                "  b86 := 1\r\n" + //
                                "  b87 := b38 EQ b86\r\n" + //
                                "  IF b87 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_1 ELSE IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b35 := b36\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_1\r\n" + //
                                "  b88 := 1\r\n" + //
                                "  b89 := b36 IADD b88\r\n" + //
                                "  b36 := b89\r\n" + //
                                "  b90 := 1\r\n" + //
                                "  b91 := b37 IRSHIFT b90\r\n" + //
                                "  b37 := b91\r\n" + //
                                "  CALL IntIsZero ( b37 -> b75 )\r\n" + //
                                "  b92 <| b78\r\n" + //
                                "  b93 := BNOT b92\r\n" + //
                                "  b83 := b93\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  b94 := 23\r\n" + //
                                "  b95 := b35 GT b94\r\n" + //
                                "  IF b95 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_1 ELSE IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b96 := 23\r\n" + //
                                "  b97 := b35 ISUB b96\r\n" + //
                                "  b98 := b32 IRSHIFT b97\r\n" + //
                                "  b32 := b98\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_1\r\n" + //
                                "  b99 := 23\r\n" + //
                                "  c10 := b35 LT b99\r\n" + //
                                "  IF c10 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_0 ELSE IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  c11 := 23\r\n" + //
                                "  c12 := c11 ISUB b35\r\n" + //
                                "  c13 := b32 ILSHIFT c12\r\n" + //
                                "  b32 := c13\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_1\r\n" + //
                                "  c14 := 127\r\n" + //
                                "  c15 := b35 IADD c14\r\n" + //
                                "  b40 := c15\r\n" + //
                                "  c16 := 4194303\r\n" + //
                                "  b42 := c16\r\n" + //
                                "  c17 := b32 IAND b42\r\n" + //
                                "  b41 := c17\r\n" + //
                                "  b34 := b41\r\n" + //
                                "  c18 := 23\r\n" + //
                                "  c19 := b40 ILSHIFT c18\r\n" + //
                                "  c20 := b34 IOR c19\r\n" + //
                                "  b34 := c20\r\n" + //
                                "  IF b33 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_1 ELSE IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  c21 := 1\r\n" + //
                                "  c22 := 31\r\n" + //
                                "  c23 := c21 ILSHIFT c22\r\n" + //
                                "  c24 := b34 IOR c23\r\n" + //
                                "  b34 := c24\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_1\r\n" + //
                                "  CALL IntBinaryAsReal ( b34 -> b22 )\r\n" + //
                                "  c25 <| b25\r\n" + //
                                "  b30 := c25\r\n" + //
                                "  b31 |< b30\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Abs\r\n" + //
                                "  b46 <- b45\r\n" + //
                                "  b47 := 0\r\n" + //
                                "  b49 := 0\r\n" + //
                                "  b50 := b46 GE b49\r\n" + //
                                "  IF b50 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b47 := b46\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  CALL INeg ( b46 -> b52 )\r\n" + //
                                "  b51 <| b55\r\n" + //
                                "  b47 := b51\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  b48 |< b47\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  b53 <- b52\r\n" + //
                                "  b54 := 0\r\n" + //
                                "  b56 := INOT b53\r\n" + //
                                "  b57 := 1\r\n" + //
                                "  b58 := b56 IADD b57\r\n" + //
                                "  b54 := b58\r\n" + //
                                "  b55 |< b54\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsNegative\r\n" + //
                                "  b62 <- b61\r\n" + //
                                "  b63 := FALSE\r\n" + //
                                "  b65 := 0\r\n" + //
                                "  b66 := 31\r\n" + //
                                "  b67 := b62 IRSHIFT b66\r\n" + //
                                "  b68 := 1\r\n" + //
                                "  b69 := b67 IAND b68\r\n" + //
                                "  b65 := b69\r\n" + //
                                "  b70 := 0\r\n" + //
                                "  b71 := b65 EQ b70\r\n" + //
                                "  IF b71 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b72 := FALSE\r\n" + //
                                "  b63 := b72\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b73 := TRUE\r\n" + //
                                "  b63 := b73\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0_0\r\n" + //
                                "  b64 |< b63\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  b76 <- b75\r\n" + //
                                "  b77 := FALSE\r\n" + //
                                "  b79 := 0\r\n" + //
                                "  b80 := b76 EQ b79\r\n" + //
                                "  IF b80 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b81 := TRUE\r\n" + //
                                "  b77 := b81\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b82 := FALSE\r\n" + //
                                "  b77 := b82\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  b78 |< b77\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  c28 <- c27\r\n" + //
                                "  c30 <- c29\r\n" + //
                                "  c31 := 0.0\r\n" + //
                                "  c33 := 0\r\n" + //
                                "  c34 := 0\r\n" + //
                                "  c35 := 0\r\n" + //
                                "  c36 := 0\r\n" + //
                                "  c37 := 0\r\n" + //
                                "  c38 := 0\r\n" + //
                                "  c39 := 0\r\n" + //
                                "  c40 := 0\r\n" + //
                                "  c41 := 0\r\n" + //
                                "  c42 := 0\r\n" + //
                                "  c43 := 0\r\n" + //
                                "  c44 := 0\r\n" + //
                                "  c45 := 0\r\n" + //
                                "  c46 := 0\r\n" + //
                                "  c47 := 0\r\n" + //
                                "  c48 := 0\r\n" + //
                                "  CALL RealSign ( c28 -> c50 )\r\n" + //
                                "  c49 <| c53\r\n" + //
                                "  c34 := c49\r\n" + //
                                "  CALL RealSign ( c30 -> c50 )\r\n" + //
                                "  c60 <| c53\r\n" + //
                                "  c35 := c60\r\n" + //
                                "  CALL RealExponent ( c28 -> c62 )\r\n" + //
                                "  c61 <| c65\r\n" + //
                                "  c37 := c61\r\n" + //
                                "  CALL RealExponent ( c30 -> c62 )\r\n" + //
                                "  c76 <| c65\r\n" + //
                                "  c38 := c76\r\n" + //
                                "  CALL RealMantissa ( c28 -> c78 )\r\n" + //
                                "  c77 <| c81\r\n" + //
                                "  c39 := c77\r\n" + //
                                "  CALL RealMantissa ( c30 -> c78 )\r\n" + //
                                "  c88 <| c81\r\n" + //
                                "  c40 := c88\r\n" + //
                                "  c89 := c37 LT c38\r\n" + //
                                "  IF c89 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_2 ELSE IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  c90 := c38 IADD c75\r\n" + //
                                "  c41 := c90\r\n" + //
                                "  c91 := c38 ISUB c37\r\n" + //
                                "  c36 := c91\r\n" + //
                                "  c92 := c39 IRSHIFT c36\r\n" + //
                                "  c39 := c92\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  c93 := c37 GT c38\r\n" + //
                                "  IF c93 EQ TRUE THEN IFSTAT_0_SEQ_1_LEVEL_0 ELSE IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_1_LEVEL_0\r\n" + //
                                "  c94 := c37 IADD c75\r\n" + //
                                "  c41 := c94\r\n" + //
                                "  c95 := c37 ISUB c38\r\n" + //
                                "  c36 := c95\r\n" + //
                                "  c96 := c40 IRSHIFT c36\r\n" + //
                                "  c40 := c96\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_1\r\n" + //
                                "  c97 := c37 IADD c75\r\n" + //
                                "  c41 := c97\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_2\r\n" + //
                                "  c98 := 0\r\n" + //
                                "  c99 := c34 EQ c98\r\n" + //
                                "  d10 := 1\r\n" + //
                                "  d11 := c35 EQ d10\r\n" + //
                                "  d12 := c99 LAND d11\r\n" + //
                                "  IF d12 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_2 ELSE IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                "  d13 := c39 GT c40\r\n" + //
                                "  IF d13 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_1 ELSE IFNEXT_2_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_1\r\n" + //
                                "  d14 := 0\r\n" + //
                                "  c44 := d14\r\n" + //
                                "  d15 := c39 ISUB c40\r\n" + //
                                "  c43 := d15\r\n" + //
                                "  GOTO IFEND_2_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_1\r\n" + //
                                "  d16 := c39 LT c40\r\n" + //
                                "  IF d16 EQ TRUE THEN IFSTAT_2_SEQ_1_LEVEL_1 ELSE IFNEXT_2_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_1_LEVEL_1\r\n" + //
                                "  d17 := 1\r\n" + //
                                "  c44 := d17\r\n" + //
                                "  d18 := c40 ISUB c39\r\n" + //
                                "  c43 := d18\r\n" + //
                                "  GOTO IFEND_2_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_1\r\n" + //
                                "  d19 := 0\r\n" + //
                                "  c44 := d19\r\n" + //
                                "  d20 := 0\r\n" + //
                                "  c43 := d20\r\n" + //
                                "  GOTO IFEND_2_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_2_LEVEL_1\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_2\r\n" + //
                                "  d21 := 1\r\n" + //
                                "  d22 := c34 EQ d21\r\n" + //
                                "  d23 := 0\r\n" + //
                                "  d24 := c35 EQ d23\r\n" + //
                                "  d25 := d22 LAND d24\r\n" + //
                                "  IF d25 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_0_1 ELSE IFNEXT_1_SEQ_1_LEVEL_0_2\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_0_1\r\n" + //
                                "  d26 := c39 GT c40\r\n" + //
                                "  IF d26 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_1 ELSE IFNEXT_4_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_1\r\n" + //
                                "  d27 := 1\r\n" + //
                                "  c44 := d27\r\n" + //
                                "  d28 := c39 ISUB c40\r\n" + //
                                "  c43 := d28\r\n" + //
                                "  GOTO IFEND_4_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_1\r\n" + //
                                "  d29 := c39 LT c40\r\n" + //
                                "  IF d29 EQ TRUE THEN IFSTAT_4_SEQ_1_LEVEL_1 ELSE IFNEXT_4_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_1_LEVEL_1\r\n" + //
                                "  d30 := 0\r\n" + //
                                "  c44 := d30\r\n" + //
                                "  d31 := c40 ISUB c39\r\n" + //
                                "  c43 := d31\r\n" + //
                                "  GOTO IFEND_4_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_1_LEVEL_1\r\n" + //
                                "  d32 := 0\r\n" + //
                                "  c44 := d32\r\n" + //
                                "  d33 := 0\r\n" + //
                                "  c43 := d33\r\n" + //
                                "  GOTO IFEND_4_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_4_LEVEL_1\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_2\r\n" + //
                                "  c44 := c34\r\n" + //
                                "  d34 := c39 IADD c40\r\n" + //
                                "  c43 := d34\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_2\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_2\r\n" + //
                                "  d35 := 24\r\n" + //
                                "  d36 := c43 IRSHIFT d35\r\n" + //
                                "  d37 := 1\r\n" + //
                                "  d38 := d36 IAND d37\r\n" + //
                                "  c42 := d38\r\n" + //
                                "  d39 := 1\r\n" + //
                                "  d40 := c42 EQ d39\r\n" + //
                                "  IF d40 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_0_0 ELSE IFNEXT_7_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d41 := 1\r\n" + //
                                "  d42 := c43 IRSHIFT d41\r\n" + //
                                "  c43 := d42\r\n" + //
                                "  d43 := 1\r\n" + //
                                "  d44 := c41 IADD d43\r\n" + //
                                "  c41 := d44\r\n" + //
                                "  GOTO IFEND_7_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_7_LEVEL_0_0\r\n" + //
                                "  c45 := c43\r\n" + //
                                "  d45 := 0\r\n" + //
                                "  c48 := d45\r\n" + //
                                "  CALL IntIsZero ( c45 -> b75 )\r\n" + //
                                "  d46 <| b78\r\n" + //
                                "  d47 := BNOT d46\r\n" + //
                                "  IF d47 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF d47 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_1 ELSE WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  d48 := 1\r\n" + //
                                "  d49 := c45 IAND d48\r\n" + //
                                "  c47 := d49\r\n" + //
                                "  d50 := 1\r\n" + //
                                "  d51 := c47 EQ d50\r\n" + //
                                "  IF d51 EQ TRUE THEN IFSTAT_8_SEQ_0_LEVEL_0_0 ELSE IFNEXT_8_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_8_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c46 := c48\r\n" + //
                                "  GOTO IFEND_8_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_8_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_8_LEVEL_0_0\r\n" + //
                                "  d52 := 1\r\n" + //
                                "  d53 := c48 IADD d52\r\n" + //
                                "  c48 := d53\r\n" + //
                                "  g4 := 1\r\n" + //
                                "  g5 := c45 IRSHIFT g4\r\n" + //
                                "  c45 := g5\r\n" + //
                                "  CALL IntIsZero ( c45 -> b75 )\r\n" + //
                                "  g6 <| b78\r\n" + //
                                "  g7 := BNOT g6\r\n" + //
                                "  d47 := g7\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_1\r\n" + //
                                "  g8 := 23\r\n" + //
                                "  g9 := c46 LT g8\r\n" + //
                                "  IF g9 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_0_0 ELSE IFNEXT_9_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d54 := 23\r\n" + //
                                "  d55 := d54 ISUB c46\r\n" + //
                                "  c36 := d55\r\n" + //
                                "  d56 := c43 ILSHIFT c36\r\n" + //
                                "  c43 := d56\r\n" + //
                                "  d57 := c41 ISUB c36\r\n" + //
                                "  c41 := d57\r\n" + //
                                "  GOTO IFEND_9_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_9_LEVEL_0_0\r\n" + //
                                "  d58 := 255\r\n" + //
                                "  d59 := c41 IAND d58\r\n" + //
                                "  d60 := 23\r\n" + //
                                "  d61 := d59 ILSHIFT d60\r\n" + //
                                "  c33 := d61\r\n" + //
                                "  d62 := 8388607\r\n" + //
                                "  d63 := c43 IAND d62\r\n" + //
                                "  d64 := c33 IOR d63\r\n" + //
                                "  c33 := d64\r\n" + //
                                "  d65 := 31\r\n" + //
                                "  d66 := c44 ILSHIFT d65\r\n" + //
                                "  d67 := c33 IOR d66\r\n" + //
                                "  c33 := d67\r\n" + //
                                "  CALL IntBinaryAsReal ( c33 -> b22 )\r\n" + //
                                "  d68 <| b25\r\n" + //
                                "  c31 := d68\r\n" + //
                                "  c32 |< c31\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  c51 <- c50\r\n" + //
                                "  c52 := 0\r\n" + //
                                "  c54 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( c51 -> b17 )\r\n" + //
                                "  c55 <| b20\r\n" + //
                                "  c54 := c55\r\n" + //
                                "  c56 := 31\r\n" + //
                                "  c57 := c54 IRSHIFT c56\r\n" + //
                                "  c58 := 1\r\n" + //
                                "  c59 := c57 IAND c58\r\n" + //
                                "  c52 := c59\r\n" + //
                                "  c53 |< c52\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  c63 <- c62\r\n" + //
                                "  c64 := 0\r\n" + //
                                "  c66 := 0\r\n" + //
                                "  c67 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( c63 -> b17 )\r\n" + //
                                "  c68 <| b20\r\n" + //
                                "  c66 := c68\r\n" + //
                                "  c69 := 23\r\n" + //
                                "  c70 := c66 IRSHIFT c69\r\n" + //
                                "  c71 := 255\r\n" + //
                                "  c72 := c70 IAND c71\r\n" + //
                                "  c67 := c72\r\n" + //
                                "  c73 := c67 ISUB c75\r\n" + //
                                "  c64 := c73\r\n" + //
                                "  c65 |< c64\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  c79 <- c78\r\n" + //
                                "  c82 := 0\r\n" + //
                                "  c83 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( c79 -> b17 )\r\n" + //
                                "  c84 <| b20\r\n" + //
                                "  c83 := c84\r\n" + //
                                "  c85 := 8388607\r\n" + //
                                "  c86 := c83 IAND c85\r\n" + //
                                "  c82 := c86\r\n" + //
                                "  c87 := 8388608\r\n" + //
                                "  c80 := c82 IOR c87\r\n" + //
                                "  c81 |< c80\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  l5 <- d71\r\n" + //
                                "  l7 <- d72\r\n" + //
                                "  d73 := 0.0\r\n" + //
                                "  d75 := 0\r\n" + //
                                "  d76 := 0\r\n" + //
                                "  d77 := 0\r\n" + //
                                "  d78 := 0\r\n" + //
                                "  d79 := 0\r\n" + //
                                "  d80 := 0\r\n" + //
                                "  d81 := 0\r\n" + //
                                "  d82 := 0\r\n" + //
                                "  d83 := 0\r\n" + //
                                "  d84 := 0\r\n" + //
                                "  d85 := 0\r\n" + //
                                "  k2 := 0\r\n" + //
                                "  k3 := 0\r\n" + //
                                "  k4 := 0\r\n" + //
                                "  k5 := 0\r\n" + //
                                "  k6 := 0\r\n" + //
                                "  k7 := 0\r\n" + //
                                "  k8 := 0\r\n" + //
                                "  k9 := 0\r\n" + //
                                "  l0 := 0\r\n" + //
                                "  l1 := 0\r\n" + //
                                "  l2 := 0\r\n" + //
                                "  l3 := 0\r\n" + //
                                "  l4 := 0\r\n" + //
                                "  CALL RealSign ( l5 -> c50 )\r\n" + //
                                "  l6 <| c53\r\n" + //
                                "  d76 := l6\r\n" + //
                                "  CALL RealSign ( l7 -> c50 )\r\n" + //
                                "  l8 <| c53\r\n" + //
                                "  d77 := l8\r\n" + //
                                "  CALL RealExponent ( l5 -> c62 )\r\n" + //
                                "  l9 <| c65\r\n" + //
                                "  d78 := l9\r\n" + //
                                "  CALL RealExponent ( l7 -> c62 )\r\n" + //
                                "  m0 <| c65\r\n" + //
                                "  d79 := m0\r\n" + //
                                "  CALL RealMantissa ( l5 -> c78 )\r\n" + //
                                "  m1 <| c81\r\n" + //
                                "  d80 := m1\r\n" + //
                                "  CALL RealMantissa ( l7 -> c78 )\r\n" + //
                                "  m2 <| c81\r\n" + //
                                "  d81 := m2\r\n" + //
                                "  m3 := 1\r\n" + //
                                "  m4 := d80 IAND m3\r\n" + //
                                "  m5 := 1\r\n" + //
                                "  m6 := m4 NE m5\r\n" + //
                                "  IF m6 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF m6 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  m7 := 1\r\n" + //
                                "  m8 := d80 IRSHIFT m7\r\n" + //
                                "  d80 := m8\r\n" + //
                                "  m9 := 1\r\n" + //
                                "  n0 := d80 IAND m9\r\n" + //
                                "  n1 := 1\r\n" + //
                                "  n2 := n0 NE n1\r\n" + //
                                "  m6 := n2\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                                "  n3 := 0\r\n" + //
                                "  k7 := n3\r\n" + //
                                "  l1 := d80\r\n" + //
                                "  CALL IntIsZero ( l1 -> b75 )\r\n" + //
                                "  n4 <| b78\r\n" + //
                                "  n5 := BNOT n4\r\n" + //
                                "  IF n5 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF n5 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_0 ELSE WHILEEND_4_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  n6 := 1\r\n" + //
                                "  n7 := l1 IAND n6\r\n" + //
                                "  l2 := n7\r\n" + //
                                "  n8 := 1\r\n" + //
                                "  n9 := l2 EQ n8\r\n" + //
                                "  IF n9 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_0_0 ELSE IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_0_0\r\n" + //
                                "  k6 := k7\r\n" + //
                                "  GOTO IFEND_10_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_10_LEVEL_0_0\r\n" + //
                                "  o0 := 1\r\n" + //
                                "  o1 := k7 IADD o0\r\n" + //
                                "  k7 := o1\r\n" + //
                                "  o2 := 1\r\n" + //
                                "  o3 := l1 IRSHIFT o2\r\n" + //
                                "  l1 := o3\r\n" + //
                                "  CALL IntIsZero ( l1 -> b75 )\r\n" + //
                                "  o4 <| b78\r\n" + //
                                "  o5 := BNOT o4\r\n" + //
                                "  n5 := o5\r\n" + //
                                "  GOTO WHILECOND_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_4_LEVEL_0_0\r\n" + //
                                "  o6 := 1\r\n" + //
                                "  o7 := d81 IAND o6\r\n" + //
                                "  o8 := 1\r\n" + //
                                "  o9 := o7 NE o8\r\n" + //
                                "  IF o9 EQ TRUE THEN WHILESTAT_6_SEQ_0_LEVEL_0 ELSE WHILENEXT_6_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_6_SEQ_0_LEVEL_0\r\n" + //
                                "  IF o9 EQ TRUE THEN WHILESTAT_6_SEQ_0_LEVEL_0 ELSE WHILEEND_6_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_6_SEQ_0_LEVEL_0\r\n" + //
                                "  p0 := 1\r\n" + //
                                "  p1 := d81 IRSHIFT p0\r\n" + //
                                "  d81 := p1\r\n" + //
                                "  p2 := 1\r\n" + //
                                "  p3 := d81 IAND p2\r\n" + //
                                "  p4 := 1\r\n" + //
                                "  p5 := p3 NE p4\r\n" + //
                                "  o9 := p5\r\n" + //
                                "  GOTO WHILECOND_6_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_6_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_6_LEVEL_0\r\n" + //
                                "  p6 := 0\r\n" + //
                                "  k9 := p6\r\n" + //
                                "  l0 := d81\r\n" + //
                                "  CALL IntIsZero ( l0 -> b75 )\r\n" + //
                                "  p7 <| b78\r\n" + //
                                "  p8 := BNOT p7\r\n" + //
                                "  IF p8 EQ TRUE THEN WHILESTAT_8_SEQ_0_LEVEL_0 ELSE WHILENEXT_8_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_8_SEQ_0_LEVEL_0\r\n" + //
                                "  IF p8 EQ TRUE THEN WHILESTAT_8_SEQ_0_LEVEL_0 ELSE WHILEEND_8_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_8_SEQ_0_LEVEL_0\r\n" + //
                                "  p9 := 1\r\n" + //
                                "  q0 := l0 IAND p9\r\n" + //
                                "  l3 := q0\r\n" + //
                                "  q1 := 1\r\n" + //
                                "  q2 := l3 EQ q1\r\n" + //
                                "  IF q2 EQ TRUE THEN IFSTAT_11_SEQ_0_LEVEL_0 ELSE IFNEXT_11_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_11_SEQ_0_LEVEL_0\r\n" + //
                                "  k8 := k9\r\n" + //
                                "  GOTO IFEND_11_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_11_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_11_LEVEL_0\r\n" + //
                                "  q3 := 1\r\n" + //
                                "  q4 := k9 IADD q3\r\n" + //
                                "  k9 := q4\r\n" + //
                                "  q5 := 1\r\n" + //
                                "  q6 := l0 IRSHIFT q5\r\n" + //
                                "  l0 := q6\r\n" + //
                                "  CALL IntIsZero ( l0 -> b75 )\r\n" + //
                                "  q7 <| b78\r\n" + //
                                "  q8 := BNOT q7\r\n" + //
                                "  p8 := q8\r\n" + //
                                "  GOTO WHILECOND_8_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_8_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_8_LEVEL_0\r\n" + //
                                "  q9 := k6 IADD k8\r\n" + //
                                "  l4 := q9\r\n" + //
                                "  r0 := d76 NE d77\r\n" + //
                                "  IF r0 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_0 ELSE IFNEXT_12_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_0\r\n" + //
                                "  r1 := 1\r\n" + //
                                "  d82 := r1\r\n" + //
                                "  GOTO IFEND_12_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_0\r\n" + //
                                "  r2 := 0\r\n" + //
                                "  d82 := r2\r\n" + //
                                "  GOTO IFEND_12_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_12_LEVEL_0\r\n" + //
                                "  r3 := d78 IADD d79\r\n" + //
                                "  d84 := r3\r\n" + //
                                "  r4 := d80 IMUL d81\r\n" + //
                                "  d83 := r4\r\n" + //
                                "  r5 := 23\r\n" + //
                                "  r6 := l4 LT r5\r\n" + //
                                "  IF r6 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_0 ELSE IFNEXT_13_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_0\r\n" + //
                                "  r7 := 23\r\n" + //
                                "  r8 := r7 ISUB l4\r\n" + //
                                "  k5 := r8\r\n" + //
                                "  r9 := d83 ILSHIFT k5\r\n" + //
                                "  d83 := r9\r\n" + //
                                "  GOTO IFEND_13_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_0\r\n" + //
                                "  s0 := 23\r\n" + //
                                "  s1 := l4 GT s0\r\n" + //
                                "  IF s1 EQ TRUE THEN IFSTAT_13_SEQ_1_LEVEL_0 ELSE IFNEXT_13_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_1_LEVEL_0\r\n" + //
                                "  s2 := 23\r\n" + //
                                "  s3 := l4 ISUB s2\r\n" + //
                                "  k5 := s3\r\n" + //
                                "  s4 := d83 IRSHIFT k5\r\n" + //
                                "  d83 := s4\r\n" + //
                                "  GOTO IFEND_13_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_13_LEVEL_0\r\n" + //
                                "  s5 := 0\r\n" + //
                                "  d85 := s5\r\n" + //
                                "  k3 := d83\r\n" + //
                                "  CALL IntIsZero ( k3 -> b75 )\r\n" + //
                                "  s6 <| b78\r\n" + //
                                "  s7 := BNOT s6\r\n" + //
                                "  IF s7 EQ TRUE THEN WHILESTAT_10_SEQ_0_LEVEL_0 ELSE WHILENEXT_10_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_10_SEQ_0_LEVEL_0\r\n" + //
                                "  IF s7 EQ TRUE THEN WHILESTAT_10_SEQ_0_LEVEL_0 ELSE WHILEEND_10_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_10_SEQ_0_LEVEL_0\r\n" + //
                                "  s8 := 1\r\n" + //
                                "  s9 := k3 IAND s8\r\n" + //
                                "  k4 := s9\r\n" + //
                                "  t0 := 1\r\n" + //
                                "  t1 := k4 EQ t0\r\n" + //
                                "  IF t1 EQ TRUE THEN IFSTAT_14_SEQ_0_LEVEL_0 ELSE IFNEXT_14_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_14_SEQ_0_LEVEL_0\r\n" + //
                                "  k2 := d85\r\n" + //
                                "  GOTO IFEND_14_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_14_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_14_LEVEL_0\r\n" + //
                                "  t2 := 1\r\n" + //
                                "  t3 := d85 IADD t2\r\n" + //
                                "  d85 := t3\r\n" + //
                                "  t4 := 1\r\n" + //
                                "  t5 := k3 IRSHIFT t4\r\n" + //
                                "  k3 := t5\r\n" + //
                                "  CALL IntIsZero ( k3 -> b75 )\r\n" + //
                                "  t6 <| b78\r\n" + //
                                "  t7 := BNOT t6\r\n" + //
                                "  s7 := t7\r\n" + //
                                "  GOTO WHILECOND_10_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_10_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_10_LEVEL_0\r\n" + //
                                "  t8 := 23\r\n" + //
                                "  t9 := k2 GT t8\r\n" + //
                                "  IF t9 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_0 ELSE IFNEXT_15_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_0\r\n" + //
                                "  u0 := 23\r\n" + //
                                "  u1 := k2 ISUB u0\r\n" + //
                                "  k5 := u1\r\n" + //
                                "  u2 := d83 IRSHIFT k5\r\n" + //
                                "  d83 := u2\r\n" + //
                                "  u3 := d84 IADD k5\r\n" + //
                                "  d84 := u3\r\n" + //
                                "  GOTO IFEND_15_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_0\r\n" + //
                                "  u4 := 23\r\n" + //
                                "  u5 := k2 LT u4\r\n" + //
                                "  IF u5 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_0 ELSE IFNEXT_15_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_0\r\n" + //
                                "  u6 := 23\r\n" + //
                                "  u7 := u6 ISUB k2\r\n" + //
                                "  k5 := u7\r\n" + //
                                "  u8 := d83 ILSHIFT k5\r\n" + //
                                "  d83 := u8\r\n" + //
                                "  u9 := d84 ISUB k5\r\n" + //
                                "  d84 := u9\r\n" + //
                                "  GOTO IFEND_15_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_15_LEVEL_0\r\n" + //
                                "  v1 := d84 IADD c75\r\n" + //
                                "  d84 := v1\r\n" + //
                                "  v2 := 255\r\n" + //
                                "  v3 := d84 IAND v2\r\n" + //
                                "  v4 := 23\r\n" + //
                                "  v5 := v3 ILSHIFT v4\r\n" + //
                                "  d75 := v5\r\n" + //
                                "  v6 := 1\r\n" + //
                                "  v7 := d82 IAND v6\r\n" + //
                                "  v8 := 31\r\n" + //
                                "  v9 := v7 ILSHIFT v8\r\n" + //
                                "  w0 := d75 IOR v9\r\n" + //
                                "  d75 := w0\r\n" + //
                                "  w1 := 8388607\r\n" + //
                                "  w2 := d83 IAND w1\r\n" + //
                                "  w3 := d75 IOR w2\r\n" + //
                                "  d75 := w3\r\n" + //
                                "  CALL IntBinaryAsReal ( d75 -> b22 )\r\n" + //
                                "  w4 <| b25\r\n" + //
                                "  d73 := w4\r\n" + //
                                "  d74 |< d73\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RDivide\r\n" + //
                                "  y9 <- d88\r\n" + //
                                "  z1 <- d89\r\n" + //
                                "  w5 := 0.0\r\n" + //
                                "  w6 := 0\r\n" + //
                                "  w7 := 0\r\n" + //
                                "  w8 := 0\r\n" + //
                                "  w9 := 0\r\n" + //
                                "  x0 := 0\r\n" + //
                                "  x1 := 0\r\n" + //
                                "  x2 := 0\r\n" + //
                                "  x3 := 0\r\n" + //
                                "  x4 := 0\r\n" + //
                                "  x5 := 0\r\n" + //
                                "  x6 := 0\r\n" + //
                                "  x7 := 0\r\n" + //
                                "  x8 := 0\r\n" + //
                                "  x9 := 0\r\n" + //
                                "  y0 := 0\r\n" + //
                                "  y1 := 0\r\n" + //
                                "  y2 := 0\r\n" + //
                                "  y3 := 0\r\n" + //
                                "  y4 := 0\r\n" + //
                                "  y5 := 0\r\n" + //
                                "  y6 := 0\r\n" + //
                                "  y7 := 0\r\n" + //
                                "  y8 := 0\r\n" + //
                                "  CALL RealSign ( y9 -> c50 )\r\n" + //
                                "  z0 <| c53\r\n" + //
                                "  w7 := z0\r\n" + //
                                "  CALL RealSign ( z1 -> c50 )\r\n" + //
                                "  z2 <| c53\r\n" + //
                                "  w8 := z2\r\n" + //
                                "  CALL RealExponent ( y9 -> c62 )\r\n" + //
                                "  z3 <| c65\r\n" + //
                                "  w9 := z3\r\n" + //
                                "  CALL RealExponent ( z1 -> c62 )\r\n" + //
                                "  z4 <| c65\r\n" + //
                                "  x0 := z4\r\n" + //
                                "  CALL RealMantissa ( y9 -> c78 )\r\n" + //
                                "  z5 <| c81\r\n" + //
                                "  x1 := z5\r\n" + //
                                "  CALL RealMantissa ( z1 -> c78 )\r\n" + //
                                "  z6 <| c81\r\n" + //
                                "  x2 := z6\r\n" + //
                                "  z7 := 30\r\n" + //
                                "  z8 := 23\r\n" + //
                                "  z9 := z7 ISUB z8\r\n" + //
                                "  y2 := z9\r\n" + //
                                "  A0 := x1 ILSHIFT y2\r\n" + //
                                "  x1 := A0\r\n" + //
                                "  A1 := 0\r\n" + //
                                "  x6 := A1\r\n" + //
                                "  x8 := x2\r\n" + //
                                "  A2 := 1\r\n" + //
                                "  A3 := x8 IAND A2\r\n" + //
                                "  A4 := 0\r\n" + //
                                "  A5 := A3 EQ A4\r\n" + //
                                "  IF A5 EQ TRUE THEN WHILESTAT_12_SEQ_0_LEVEL_0 ELSE WHILENEXT_12_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_12_SEQ_0_LEVEL_0\r\n" + //
                                "  IF A5 EQ TRUE THEN WHILESTAT_12_SEQ_0_LEVEL_0 ELSE WHILEEND_12_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_12_SEQ_0_LEVEL_0\r\n" + //
                                "  A6 := 1\r\n" + //
                                "  A7 := x8 IRSHIFT A6\r\n" + //
                                "  x8 := A7\r\n" + //
                                "  A8 := 1\r\n" + //
                                "  A9 := x6 IADD A8\r\n" + //
                                "  x6 := A9\r\n" + //
                                "  B0 := 1\r\n" + //
                                "  B1 := x8 IAND B0\r\n" + //
                                "  B2 := 0\r\n" + //
                                "  B3 := B1 EQ B2\r\n" + //
                                "  A5 := B3\r\n" + //
                                "  GOTO WHILECOND_12_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_12_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_12_LEVEL_0\r\n" + //
                                "  x9 := x6\r\n" + //
                                "  y2 := x9\r\n" + //
                                "  B4 := x2 IRSHIFT y2\r\n" + //
                                "  x2 := B4\r\n" + //
                                "  B5 := 0\r\n" + //
                                "  x6 := B5\r\n" + //
                                "  x8 := x2\r\n" + //
                                "  CALL IntIsZero ( x8 -> b75 )\r\n" + //
                                "  B6 <| b78\r\n" + //
                                "  B7 := BNOT B6\r\n" + //
                                "  IF B7 EQ TRUE THEN WHILESTAT_14_SEQ_0_LEVEL_0 ELSE WHILENEXT_14_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_14_SEQ_0_LEVEL_0\r\n" + //
                                "  IF B7 EQ TRUE THEN WHILESTAT_14_SEQ_0_LEVEL_0 ELSE WHILEEND_14_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_14_SEQ_0_LEVEL_0\r\n" + //
                                "  B8 := 1\r\n" + //
                                "  B9 := x8 IAND B8\r\n" + //
                                "  x7 := B9\r\n" + //
                                "  C0 := 1\r\n" + //
                                "  C1 := x7 EQ C0\r\n" + //
                                "  IF C1 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_0 ELSE IFNEXT_16_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_0\r\n" + //
                                "  y0 := x6\r\n" + //
                                "  GOTO IFEND_16_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_16_LEVEL_0\r\n" + //
                                "  C2 := 1\r\n" + //
                                "  C3 := x6 IADD C2\r\n" + //
                                "  x6 := C3\r\n" + //
                                "  C4 := 1\r\n" + //
                                "  C5 := x8 IRSHIFT C4\r\n" + //
                                "  x8 := C5\r\n" + //
                                "  CALL IntIsZero ( x8 -> b75 )\r\n" + //
                                "  C6 <| b78\r\n" + //
                                "  C7 := BNOT C6\r\n" + //
                                "  B7 := C7\r\n" + //
                                "  GOTO WHILECOND_14_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_14_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_14_LEVEL_0\r\n" + //
                                "  y1 := y0\r\n" + //
                                "  C8 := w9 ISUB y1\r\n" + //
                                "  w9 := C8\r\n" + //
                                "  C9 := x0 ISUB y1\r\n" + //
                                "  x0 := C9\r\n" + //
                                "  D0 := 30\r\n" + //
                                "  D1 := D0 ISUB y1\r\n" + //
                                "  y8 := D1\r\n" + //
                                "  D2 := w7 NE w8\r\n" + //
                                "  IF D2 EQ TRUE THEN IFSTAT_17_SEQ_0_LEVEL_0 ELSE IFNEXT_17_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_17_SEQ_0_LEVEL_0\r\n" + //
                                "  D3 := 1\r\n" + //
                                "  x3 := D3\r\n" + //
                                "  GOTO IFEND_17_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_17_SEQ_0_LEVEL_0\r\n" + //
                                "  D4 := 0\r\n" + //
                                "  x3 := D4\r\n" + //
                                "  GOTO IFEND_17_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_17_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_17_LEVEL_0\r\n" + //
                                "  D5 := w9 ISUB x0\r\n" + //
                                "  x5 := D5\r\n" + //
                                "  CALL Div ( x1 -> d91 , x2 -> d93 )\r\n" + //
                                "  D6 <| d96\r\n" + //
                                "  x4 := D6\r\n" + //
                                "  D7 := 23\r\n" + //
                                "  D8 := y8 LT D7\r\n" + //
                                "  IF D8 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_0 ELSE IFNEXT_18_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_0\r\n" + //
                                "  D9 := 23\r\n" + //
                                "  E0 := D9 ISUB y8\r\n" + //
                                "  E1 := 1\r\n" + //
                                "  E2 := E0 IADD E1\r\n" + //
                                "  y3 := E2\r\n" + //
                                "  E3 := x4 ILSHIFT y3\r\n" + //
                                "  x4 := E3\r\n" + //
                                "  GOTO IFEND_18_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_0\r\n" + //
                                "  E4 := 23\r\n" + //
                                "  E5 := y8 GT E4\r\n" + //
                                "  IF E5 EQ TRUE THEN IFSTAT_18_SEQ_1_LEVEL_0 ELSE IFNEXT_18_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_1_LEVEL_0\r\n" + //
                                "  E6 := 23\r\n" + //
                                "  E7 := y8 ISUB E6\r\n" + //
                                "  y3 := E7\r\n" + //
                                "  E8 := x4 IRSHIFT y3\r\n" + //
                                "  x4 := E8\r\n" + //
                                "  GOTO IFEND_18_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_18_LEVEL_0\r\n" + //
                                "  E9 := 0\r\n" + //
                                "  y6 := E9\r\n" + //
                                "  y7 := x4\r\n" + //
                                "  F0 := 0\r\n" + //
                                "  y4 := F0\r\n" + //
                                "  CALL IntIsZero ( y7 -> b75 )\r\n" + //
                                "  F1 <| b78\r\n" + //
                                "  F2 := BNOT F1\r\n" + //
                                "  IF F2 EQ TRUE THEN WHILESTAT_16_SEQ_0_LEVEL_0 ELSE WHILENEXT_16_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_16_SEQ_0_LEVEL_0\r\n" + //
                                "  IF F2 EQ TRUE THEN WHILESTAT_16_SEQ_0_LEVEL_0 ELSE WHILEEND_16_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_16_SEQ_0_LEVEL_0\r\n" + //
                                "  F3 := 1\r\n" + //
                                "  F4 := y7 IAND F3\r\n" + //
                                "  y5 := F4\r\n" + //
                                "  F5 := 1\r\n" + //
                                "  F6 := y5 EQ F5\r\n" + //
                                "  IF F6 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_0 ELSE IFNEXT_19_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_0\r\n" + //
                                "  y4 := y6\r\n" + //
                                "  GOTO IFEND_19_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_19_LEVEL_0\r\n" + //
                                "  F7 := 1\r\n" + //
                                "  F8 := y6 IADD F7\r\n" + //
                                "  y6 := F8\r\n" + //
                                "  F9 := 1\r\n" + //
                                "  G0 := y7 IRSHIFT F9\r\n" + //
                                "  y7 := G0\r\n" + //
                                "  CALL IntIsZero ( y7 -> b75 )\r\n" + //
                                "  G1 <| b78\r\n" + //
                                "  G2 := BNOT G1\r\n" + //
                                "  F2 := G2\r\n" + //
                                "  GOTO WHILECOND_16_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_16_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_16_LEVEL_0\r\n" + //
                                "  G3 := 23\r\n" + //
                                "  G4 := y4 GT G3\r\n" + //
                                "  IF G4 EQ TRUE THEN IFSTAT_20_SEQ_0_LEVEL_0 ELSE IFNEXT_20_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_20_SEQ_0_LEVEL_0\r\n" + //
                                "  G5 := 23\r\n" + //
                                "  G6 := y4 ISUB G5\r\n" + //
                                "  y1 := G6\r\n" + //
                                "  G7 := x4 IRSHIFT y1\r\n" + //
                                "  x4 := G7\r\n" + //
                                "  G8 := x5 IADD y1\r\n" + //
                                "  x5 := G8\r\n" + //
                                "  GOTO IFEND_20_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_20_SEQ_0_LEVEL_0\r\n" + //
                                "  G9 := 23\r\n" + //
                                "  H0 := y4 LT G9\r\n" + //
                                "  IF H0 EQ TRUE THEN IFSTAT_20_SEQ_1_LEVEL_0 ELSE IFNEXT_20_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_20_SEQ_1_LEVEL_0\r\n" + //
                                "  H1 := 23\r\n" + //
                                "  H2 := H1 ISUB y4\r\n" + //
                                "  y1 := H2\r\n" + //
                                "  H3 := x4 ILSHIFT y1\r\n" + //
                                "  x4 := H3\r\n" + //
                                "  H4 := x5 ISUB y1\r\n" + //
                                "  x5 := H4\r\n" + //
                                "  GOTO IFEND_20_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_20_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_20_LEVEL_0\r\n" + //
                                "  H6 := x5 IADD c75\r\n" + //
                                "  x5 := H6\r\n" + //
                                "  H7 := 255\r\n" + //
                                "  H8 := x5 IAND H7\r\n" + //
                                "  H9 := 23\r\n" + //
                                "  I0 := H8 ILSHIFT H9\r\n" + //
                                "  w6 := I0\r\n" + //
                                "  I1 := 31\r\n" + //
                                "  I2 := x3 ILSHIFT I1\r\n" + //
                                "  I3 := w6 IOR I2\r\n" + //
                                "  w6 := I3\r\n" + //
                                "  I4 := 8388607\r\n" + //
                                "  I5 := x4 IAND I4\r\n" + //
                                "  I6 := w6 IOR I5\r\n" + //
                                "  w6 := I6\r\n" + //
                                "  CALL IntBinaryAsReal ( w6 -> b22 )\r\n" + //
                                "  I7 <| b25\r\n" + //
                                "  w5 := I7\r\n" + //
                                "  d90 |< w5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  d92 <- d91\r\n" + //
                                "  d94 <- d93\r\n" + //
                                "  d97 := 0\r\n" + //
                                "  d95 := 0\r\n" + //
                                "  d98 := 0\r\n" + //
                                "  d99 := 0\r\n" + //
                                "  e10 := 0\r\n" + //
                                "  e11 := 0\r\n" + //
                                "  e12 := 0\r\n" + //
                                "  e13 := 0\r\n" + //
                                "  e14 := 0\r\n" + //
                                "  d98 := d92\r\n" + //
                                "  e15 := 0\r\n" + //
                                "  e11 := e15\r\n" + //
                                "  e16 := 0\r\n" + //
                                "  d99 := e16\r\n" + //
                                "  CALL IntIsZero ( d98 -> b75 )\r\n" + //
                                "  e17 <| b78\r\n" + //
                                "  e18 := BNOT e17\r\n" + //
                                "  IF e18 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  IF e18 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_2 ELSE WHILEEND_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  e19 := 1\r\n" + //
                                "  e20 := d98 IAND e19\r\n" + //
                                "  e10 := e20\r\n" + //
                                "  e21 := 1\r\n" + //
                                "  e22 := e10 EQ e21\r\n" + //
                                "  IF e22 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_3 ELSE IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  d99 := e11\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_3\r\n" + //
                                "  e23 := 1\r\n" + //
                                "  e24 := e11 IADD e23\r\n" + //
                                "  e11 := e24\r\n" + //
                                "  e25 := 1\r\n" + //
                                "  e26 := d98 IRSHIFT e25\r\n" + //
                                "  d98 := e26\r\n" + //
                                "  CALL IntIsZero ( d98 -> b75 )\r\n" + //
                                "  e27 <| b78\r\n" + //
                                "  e28 := BNOT e27\r\n" + //
                                "  e18 := e28\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_2\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_2\r\n" + //
                                "  e12 := d99\r\n" + //
                                "  d98 := d92\r\n" + //
                                "  e29 := 0\r\n" + //
                                "  e14 := e29\r\n" + //
                                "  e30 := 0\r\n" + //
                                "  d95 := e30\r\n" + //
                                "  e31 := 0\r\n" + //
                                "  e32 := e12 GE e31\r\n" + //
                                "  IF e32 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF e32 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_1 ELSE WHILEEND_2_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  e33 := d98 IRSHIFT e12\r\n" + //
                                "  e34 := 1\r\n" + //
                                "  e35 := e33 IAND e34\r\n" + //
                                "  e13 := e35\r\n" + //
                                "  e36 := 1\r\n" + //
                                "  e37 := e14 ILSHIFT e36\r\n" + //
                                "  e38 := e37 IOR e13\r\n" + //
                                "  e14 := e38\r\n" + //
                                "  e39 := e14 GE d94\r\n" + //
                                "  IF e39 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_3 ELSE IFNEXT_1_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_3\r\n" + //
                                "  e40 := 1\r\n" + //
                                "  e41 := e40 ILSHIFT e12\r\n" + //
                                "  e42 := d95 IOR e41\r\n" + //
                                "  d95 := e42\r\n" + //
                                "  e43 := e14 ISUB d94\r\n" + //
                                "  e14 := e43\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_3\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_3\r\n" + //
                                "  e44 := 1\r\n" + //
                                "  e45 := e12 ISUB e44\r\n" + //
                                "  e12 := e45\r\n" + //
                                "  e46 := 0\r\n" + //
                                "  e47 := e12 GE e46\r\n" + //
                                "  e32 := e47\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0_1\r\n" + //
                                "  d96 |< d95\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RNotEqualTo\r\n" + //
                                "  a91 <- e49\r\n" + //
                                "  a93 <- e50\r\n" + //
                                "  a85 := FALSE\r\n" + //
                                "  a86 := FALSE\r\n" + //
                                "  a87 := FALSE\r\n" + //
                                "  a88 := 0\r\n" + //
                                "  a89 := 0\r\n" + //
                                "  a90 := 0\r\n" + //
                                "  CALL RealIsZero ( a91 -> e52 )\r\n" + //
                                "  a92 <| e55\r\n" + //
                                "  a86 := a92\r\n" + //
                                "  CALL RealIsZero ( a93 -> e52 )\r\n" + //
                                "  a94 <| e55\r\n" + //
                                "  a87 := a94\r\n" + //
                                "  a95 := a86 LAND a87\r\n" + //
                                "  IF a95 EQ TRUE THEN IFSTAT_68_SEQ_0_LEVEL_0 ELSE IFNEXT_68_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_68_SEQ_0_LEVEL_0\r\n" + //
                                "  a96 := FALSE\r\n" + //
                                "  a85 := a96\r\n" + //
                                "  GOTO IFEND_68_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_68_SEQ_0_LEVEL_0\r\n" + //
                                "  CALL RealBinaryAsInt ( a91 -> b17 )\r\n" + //
                                "  a97 <| b20\r\n" + //
                                "  a89 := a97\r\n" + //
                                "  CALL RealBinaryAsInt ( a93 -> b17 )\r\n" + //
                                "  a98 <| b20\r\n" + //
                                "  a90 := a98\r\n" + //
                                "  a99 := a89 IXOR a90\r\n" + //
                                "  a88 := a99\r\n" + //
                                "  b10 := 0\r\n" + //
                                "  b11 := a88 EQ b10\r\n" + //
                                "  IF b11 EQ TRUE THEN IFSTAT_69_SEQ_0_LEVEL_1 ELSE IFNEXT_69_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_69_SEQ_0_LEVEL_1\r\n" + //
                                "  b12 := FALSE\r\n" + //
                                "  a85 := b12\r\n" + //
                                "  GOTO IFEND_69_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_69_SEQ_0_LEVEL_1\r\n" + //
                                "  b13 := TRUE\r\n" + //
                                "  a85 := b13\r\n" + //
                                "  GOTO IFEND_69_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_69_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_69_LEVEL_1\r\n" + //
                                "  GOTO IFEND_68_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_68_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_68_LEVEL_0\r\n" + //
                                "  e51 |< a85\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsZero\r\n" + //
                                "  e53 <- e52\r\n" + //
                                "  e54 := FALSE\r\n" + //
                                "  e56 := 0\r\n" + //
                                "  CALL RealMantissa ( e53 -> c78 )\r\n" + //
                                "  e57 <| c81\r\n" + //
                                "  e56 := e57\r\n" + //
                                "  e58 := 0\r\n" + //
                                "  e59 := e56 EQ e58\r\n" + //
                                "  IF e59 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_4 ELSE IFNEXT_0_SEQ_0_LEVEL_0_4\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_4\r\n" + //
                                "  e60 := TRUE\r\n" + //
                                "  e54 := e60\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_4\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_4\r\n" + //
                                "  e61 := FALSE\r\n" + //
                                "  e54 := e61\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_4\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_2\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_4\r\n" + //
                                "  e55 |< e54\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Mod\r\n" + //
                                "  e65 <- e64\r\n" + //
                                "  e67 <- e66\r\n" + //
                                "  e68 := 0\r\n" + //
                                "  e70 := 0\r\n" + //
                                "  e68 := e65\r\n" + //
                                "  e71 := e68 ISUB e67\r\n" + //
                                "  e72 := 0\r\n" + //
                                "  e73 := e71 GE e72\r\n" + //
                                "  IF e73 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_1 ELSE WHILENEXT_4_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILECOND_4_SEQ_0_LEVEL_0_1\r\n" + //
                                "  IF e73 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_1 ELSE WHILEEND_4_LEVEL_0_1\r\n" + //
                                "  LABEL WHILESTAT_4_SEQ_0_LEVEL_0_1\r\n" + //
                                "  e74 := e68 ISUB e67\r\n" + //
                                "  e68 := e74\r\n" + //
                                "  e75 := e68 ISUB e67\r\n" + //
                                "  e76 := 0\r\n" + //
                                "  e77 := e75 GE e76\r\n" + //
                                "  e73 := e77\r\n" + //
                                "  GOTO WHILECOND_4_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILENEXT_4_SEQ_0_LEVEL_0_1\r\n" + //
                                "  LABEL WHILEEND_4_LEVEL_0_1\r\n" + //
                                "  e69 |< e68\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RSub\r\n" + //
                                "  e83 <- e82\r\n" + //
                                "  e85 <- e84\r\n" + //
                                "  e86 := 0.0\r\n" + //
                                "  CALL RNeg ( e85 -> b15 )\r\n" + //
                                "  e88 <| b16\r\n" + //
                                "  CALL RAdd ( e83 -> c27 , e88 -> c29 )\r\n" + //
                                "  e89 <| c32\r\n" + //
                                "  e86 := e89\r\n" + //
                                "  e87 |< e86\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  e91 <- e90\r\n" + //
                                "  IPARAM e91\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  e93 <- e92\r\n" + //
                                "  IPARAM e93\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Divide\r\n" + //
                                "  e97 <- e96\r\n" + //
                                "  e99 <- e98\r\n" + //
                                "  f10 := 0\r\n" + //
                                "  f12 := 0.0\r\n" + //
                                "  f13 := 0.0\r\n" + //
                                "  CALL IntToReal ( e97 -> b28 )\r\n" + //
                                "  f14 <| b31\r\n" + //
                                "  f12 := f14\r\n" + //
                                "  CALL IntToReal ( e99 -> b28 )\r\n" + //
                                "  f15 <| b31\r\n" + //
                                "  f13 := f15\r\n" + //
                                "  CALL RDivide ( f12 -> d88 , f13 -> d89 )\r\n" + //
                                "  f16 <| d90\r\n" + //
                                "  f10 := f16\r\n" + //
                                "  f11 |< f10\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Round\r\n" + //
                                "  f19 <- f18\r\n" + //
                                "  f20 := 0\r\n" + //
                                "  f22 := 0.5\r\n" + //
                                "  CALL RAdd ( f19 -> c27 , f22 -> c29 )\r\n" + //
                                "  f23 <| c32\r\n" + //
                                "  CALL Floor ( f23 -> f24 )\r\n" + //
                                "  f57 <| f27\r\n" + //
                                "  f20 := f57\r\n" + //
                                "  f21 |< f20\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Floor\r\n" + //
                                "  f25 <- f24\r\n" + //
                                "  f26 := 0\r\n" + //
                                "  CALL RealToInt ( f25 -> f29 )\r\n" + //
                                "  f28 <| f32\r\n" + //
                                "  f26 := f28\r\n" + //
                                "  f27 |< f26\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealToInt\r\n" + //
                                "  f30 <- f29\r\n" + //
                                "  f31 := 0\r\n" + //
                                "  f33 := 0\r\n" + //
                                "  f34 := 0\r\n" + //
                                "  f35 := 0\r\n" + //
                                "  f36 := 0\r\n" + //
                                "  CALL RealExponent ( f30 -> c62 )\r\n" + //
                                "  f37 <| c65\r\n" + //
                                "  f33 := f37\r\n" + //
                                "  CALL RealMantissa ( f30 -> c78 )\r\n" + //
                                "  f38 <| c81\r\n" + //
                                "  f34 := f38\r\n" + //
                                "  CALL RealSign ( f30 -> c50 )\r\n" + //
                                "  f39 <| c53\r\n" + //
                                "  f35 := f39\r\n" + //
                                "  f40 := 0\r\n" + //
                                "  f41 := f33 GT f40\r\n" + //
                                "  IF f41 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  f42 := f34 ILSHIFT f33\r\n" + //
                                "  f36 := f42\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  f43 := 0\r\n" + //
                                "  f44 := f33 LT f43\r\n" + //
                                "  IF f44 EQ TRUE THEN IFSTAT_3_SEQ_1_LEVEL_0 ELSE IFNEXT_3_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_1_LEVEL_0\r\n" + //
                                "  f45 := f34 IRSHIFT f33\r\n" + //
                                "  f36 := f45\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_1_LEVEL_0_0\r\n" + //
                                "  f36 := f34\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_3_LEVEL_0_0\r\n" + //
                                "  f46 := 23\r\n" + //
                                "  f47 := f36 IRSHIFT f46\r\n" + //
                                "  f31 := f47\r\n" + //
                                "  f48 := 1\r\n" + //
                                "  f49 := f35 EQ f48\r\n" + //
                                "  IF f49 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_0_0 ELSE IFNEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  CALL IntToBool ( f31 -> f50 )\r\n" + //
                                "  f53 <| f52\r\n" + //
                                "  f54 := BNOT f53\r\n" + //
                                "  f31 := f54\r\n" + //
                                "  f55 := 1\r\n" + //
                                "  f56 := f31 IADD f55\r\n" + //
                                "  f31 := f56\r\n" + //
                                "  GOTO IFEND_4_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_4_LEVEL_0_0\r\n" + //
                                "  f32 |< f31\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToBool\r\n" + //
                                "  f5 <- f50\r\n" + //
                                "  f51 := FALSE\r\n" + //
                                "  CALL IntIsZero ( f5 -> b75 )\r\n" + //
                                "  f6 <| b78\r\n" + //
                                "  IF f6 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_0 ELSE IFNEXT_7_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_0\r\n" + //
                                "  f7 := FALSE\r\n" + //
                                "  f51 := f7\r\n" + //
                                "  GOTO IFEND_7_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_0\r\n" + //
                                "  f8 := TRUE\r\n" + //
                                "  f51 := f8\r\n" + //
                                "  GOTO IFEND_7_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_7_LEVEL_0\r\n" + //
                                "  f52 |< f51\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThan\r\n" + //
                                "  W6 <- f61\r\n" + //
                                "  W8 <- f62\r\n" + //
                                "  V5 := FALSE\r\n" + //
                                "  V6 := FALSE\r\n" + //
                                "  V7 := FALSE\r\n" + //
                                "  V8 := FALSE\r\n" + //
                                "  V9 := FALSE\r\n" + //
                                "  W0 := FALSE\r\n" + //
                                "  W1 := FALSE\r\n" + //
                                "  W2 := 0\r\n" + //
                                "  W3 := 0\r\n" + //
                                "  W4 := 0\r\n" + //
                                "  W5 := 0\r\n" + //
                                "  CALL RealIsZero ( W6 -> e52 )\r\n" + //
                                "  W7 <| e55\r\n" + //
                                "  V6 := W7\r\n" + //
                                "  CALL RealIsZero ( W8 -> e52 )\r\n" + //
                                "  W9 <| e55\r\n" + //
                                "  V7 := W9\r\n" + //
                                "  CALL RealIsNegative ( W6 -> f64 )\r\n" + //
                                "  X0 <| f67\r\n" + //
                                "  V8 := X0\r\n" + //
                                "  CALL RealIsNegative ( W8 -> f64 )\r\n" + //
                                "  X1 <| f67\r\n" + //
                                "  V9 := X1\r\n" + //
                                "  CALL RealIsPositive ( W6 -> f74 )\r\n" + //
                                "  X2 <| f77\r\n" + //
                                "  W0 := X2\r\n" + //
                                "  CALL RealIsPositive ( W8 -> f74 )\r\n" + //
                                "  X3 <| f77\r\n" + //
                                "  W1 := X3\r\n" + //
                                "  X4 := V6 LAND V7\r\n" + //
                                "  IF X4 EQ TRUE THEN IFSTAT_43_SEQ_0_LEVEL_0 ELSE IFNEXT_43_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_43_SEQ_0_LEVEL_0\r\n" + //
                                "  X5 := FALSE\r\n" + //
                                "  V5 := X5\r\n" + //
                                "  GOTO IFEND_43_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_43_SEQ_0_LEVEL_0\r\n" + //
                                "  X6 := V8 LAND V7\r\n" + //
                                "  IF X6 EQ TRUE THEN IFSTAT_43_SEQ_1_LEVEL_0 ELSE IFNEXT_43_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_43_SEQ_1_LEVEL_0\r\n" + //
                                "  X7 := TRUE\r\n" + //
                                "  V5 := X7\r\n" + //
                                "  GOTO IFEND_43_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_43_SEQ_1_LEVEL_0\r\n" + //
                                "  X8 := V8 LAND W1\r\n" + //
                                "  IF X8 EQ TRUE THEN IFSTAT_43_SEQ_2_LEVEL_0 ELSE IFNEXT_43_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_43_SEQ_2_LEVEL_0\r\n" + //
                                "  X9 := TRUE\r\n" + //
                                "  V5 := X9\r\n" + //
                                "  GOTO IFEND_43_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_43_SEQ_2_LEVEL_0\r\n" + //
                                "  Y0 := V6 LAND W1\r\n" + //
                                "  IF Y0 EQ TRUE THEN IFSTAT_43_SEQ_3_LEVEL_0 ELSE IFNEXT_43_SEQ_3_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_43_SEQ_3_LEVEL_0\r\n" + //
                                "  Y1 := TRUE\r\n" + //
                                "  V5 := Y1\r\n" + //
                                "  GOTO IFEND_43_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_43_SEQ_3_LEVEL_0\r\n" + //
                                "  Y2 := V8 LAND V9\r\n" + //
                                "  IF Y2 EQ TRUE THEN IFSTAT_43_SEQ_4_LEVEL_0 ELSE IFNEXT_43_SEQ_4_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_43_SEQ_4_LEVEL_0\r\n" + //
                                "  CALL RealScore ( W6 -> f80 )\r\n" + //
                                "  Y3 <| f81\r\n" + //
                                "  W2 := Y3\r\n" + //
                                "  CALL RealScore ( W8 -> f80 )\r\n" + //
                                "  Y4 <| f81\r\n" + //
                                "  W3 := Y4\r\n" + //
                                "  Y5 := W2 LT W3\r\n" + //
                                "  IF Y5 EQ TRUE THEN IFSTAT_44_SEQ_0_LEVEL_1 ELSE IFNEXT_44_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_44_SEQ_0_LEVEL_1\r\n" + //
                                "  Y6 := TRUE\r\n" + //
                                "  V5 := Y6\r\n" + //
                                "  GOTO IFEND_44_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_44_SEQ_0_LEVEL_1\r\n" + //
                                "  Y7 := W2 EQ W3\r\n" + //
                                "  IF Y7 EQ TRUE THEN IFSTAT_44_SEQ_1_LEVEL_1 ELSE IFNEXT_44_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_44_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( W6 -> c78 )\r\n" + //
                                "  Y8 <| c81\r\n" + //
                                "  W4 := Y8\r\n" + //
                                "  CALL RealMantissa ( W8 -> c78 )\r\n" + //
                                "  Y9 <| c81\r\n" + //
                                "  W5 := Y9\r\n" + //
                                "  Z0 := W4 LT W5\r\n" + //
                                "  IF Z0 EQ TRUE THEN IFSTAT_45_SEQ_0_LEVEL_2 ELSE IFNEXT_45_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_45_SEQ_0_LEVEL_2\r\n" + //
                                "  Z1 := TRUE\r\n" + //
                                "  V5 := Z1\r\n" + //
                                "  GOTO IFEND_45_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_45_SEQ_0_LEVEL_2\r\n" + //
                                "  Z2 := FALSE\r\n" + //
                                "  V5 := Z2\r\n" + //
                                "  GOTO IFEND_45_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_45_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_45_LEVEL_2\r\n" + //
                                "  GOTO IFEND_44_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_44_SEQ_1_LEVEL_1\r\n" + //
                                "  Z3 := FALSE\r\n" + //
                                "  V5 := Z3\r\n" + //
                                "  GOTO IFEND_44_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_44_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_44_LEVEL_1\r\n" + //
                                "  GOTO IFEND_43_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_43_SEQ_4_LEVEL_0\r\n" + //
                                "  Z4 := W0 LAND W1\r\n" + //
                                "  IF Z4 EQ TRUE THEN IFSTAT_43_SEQ_5_LEVEL_0 ELSE IFNEXT_43_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_43_SEQ_5_LEVEL_0\r\n" + //
                                "  CALL RealScore ( W6 -> f80 )\r\n" + //
                                "  Z5 <| f81\r\n" + //
                                "  W2 := Z5\r\n" + //
                                "  CALL RealScore ( W8 -> f80 )\r\n" + //
                                "  Z6 <| f81\r\n" + //
                                "  W2 := Z6\r\n" + //
                                "  Z7 := W2 GT W3\r\n" + //
                                "  IF Z7 EQ TRUE THEN IFSTAT_49_SEQ_0_LEVEL_1 ELSE IFNEXT_49_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_49_SEQ_0_LEVEL_1\r\n" + //
                                "  Z8 := TRUE\r\n" + //
                                "  V5 := Z8\r\n" + //
                                "  GOTO IFEND_49_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_49_SEQ_0_LEVEL_1\r\n" + //
                                "  Z9 := W2 EQ W3\r\n" + //
                                "  IF Z9 EQ TRUE THEN IFSTAT_49_SEQ_1_LEVEL_1 ELSE IFNEXT_49_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_49_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( W6 -> c78 )\r\n" + //
                                "  f90 <| c81\r\n" + //
                                "  W4 := c75\r\n" + //
                                "  CALL RealMantissa ( W8 -> c78 )\r\n" + //
                                "  a10 <| c81\r\n" + //
                                "  W5 := a10\r\n" + //
                                "  a11 := W4 GT W5\r\n" + //
                                "  IF a11 EQ TRUE THEN IFSTAT_50_SEQ_0_LEVEL_2 ELSE IFNEXT_50_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_50_SEQ_0_LEVEL_2\r\n" + //
                                "  a12 := TRUE\r\n" + //
                                "  V5 := a12\r\n" + //
                                "  GOTO IFEND_50_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_0_LEVEL_2\r\n" + //
                                "  a13 := FALSE\r\n" + //
                                "  V5 := a13\r\n" + //
                                "  GOTO IFEND_50_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_50_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_50_LEVEL_2\r\n" + //
                                "  GOTO IFEND_49_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_49_SEQ_1_LEVEL_1\r\n" + //
                                "  a14 := FALSE\r\n" + //
                                "  V5 := a14\r\n" + //
                                "  GOTO IFEND_49_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_49_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_49_LEVEL_1\r\n" + //
                                "  GOTO IFEND_43_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_43_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFEND_43_LEVEL_0\r\n" + //
                                "  f63 |< V5\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsNegative\r\n" + //
                                "  f65 <- f64\r\n" + //
                                "  f66 := FALSE\r\n" + //
                                "  f68 := 0\r\n" + //
                                "  CALL RealSign ( f65 -> c50 )\r\n" + //
                                "  f69 <| c53\r\n" + //
                                "  f68 := f69\r\n" + //
                                "  f70 := 0\r\n" + //
                                "  f71 := f68 EQ f70\r\n" + //
                                "  IF f71 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_0_LEVEL_0\r\n" + //
                                "  f72 := FALSE\r\n" + //
                                "  f66 := f72\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_0_LEVEL_0\r\n" + //
                                "  f73 := TRUE\r\n" + //
                                "  f66 := f73\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_3_LEVEL_0\r\n" + //
                                "  f67 |< f66\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealIsPositive\r\n" + //
                                "  f75 <- f74\r\n" + //
                                "  f76 := FALSE\r\n" + //
                                "  f78 := 0\r\n" + //
                                "  f79 := 0\r\n" + //
                                "  CALL RealSign ( f75 -> c50 )\r\n" + //
                                "  c2 <| c53\r\n" + //
                                "  f79 := c2\r\n" + //
                                "  c3 := 0\r\n" + //
                                "  c4 := f79 EQ c3\r\n" + //
                                "  IF c4 EQ TRUE THEN IFSTAT_5_SEQ_0_LEVEL_0_0 ELSE IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c5 := TRUE\r\n" + //
                                "  f76 := c5\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c6 := FALSE\r\n" + //
                                "  f76 := c6\r\n" + //
                                "  GOTO IFEND_5_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_5_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_5_LEVEL_0_0\r\n" + //
                                "  f77 |< f76\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealScore\r\n" + //
                                "  d4 <- f80\r\n" + //
                                "  c7 := 0\r\n" + //
                                "  c8 := 0\r\n" + //
                                "  c9 := 0\r\n" + //
                                "  d0 := 0\r\n" + //
                                "  d1 := 0\r\n" + //
                                "  d2 := 0\r\n" + //
                                "  d3 := 0\r\n" + //
                                "  CALL RealExponent ( d4 -> c62 )\r\n" + //
                                "  d5 <| c65\r\n" + //
                                "  c8 := d5\r\n" + //
                                "  CALL RealMantissa ( d4 -> c78 )\r\n" + //
                                "  d6 <| c81\r\n" + //
                                "  c9 := d6\r\n" + //
                                "  d7 := 0\r\n" + //
                                "  d8 := c9 EQ d7\r\n" + //
                                "  IF d8 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_0_0 ELSE IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  c7 := c9\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_0_0\r\n" + //
                                "  d9 := 0\r\n" + //
                                "  d2 := d9\r\n" + //
                                "  e0 := 0\r\n" + //
                                "  e1 := c9 NE e0\r\n" + //
                                "  IF e1 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_3 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  IF e1 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_3 ELSE WHILEEND_0_LEVEL_0_3\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  e2 := 1\r\n" + //
                                "  e3 := c9 IAND e2\r\n" + //
                                "  d3 := e3\r\n" + //
                                "  e4 := 1\r\n" + //
                                "  e5 := d3 EQ e4\r\n" + //
                                "  IF e5 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_1 ELSE IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  d0 := d2\r\n" + //
                                "  GOTO IFEND_7_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFEND_7_LEVEL_1\r\n" + //
                                "  f82 := 1\r\n" + //
                                "  f83 := c9 IRSHIFT f82\r\n" + //
                                "  c9 := f83\r\n" + //
                                "  f84 := 1\r\n" + //
                                "  f85 := d2 IADD f84\r\n" + //
                                "  d2 := f85\r\n" + //
                                "  f86 := 0\r\n" + //
                                "  f87 := c9 NE f86\r\n" + //
                                "  e1 := f87\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_3\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_3\r\n" + //
                                "  f88 := 23\r\n" + //
                                "  f89 := f88 ISUB d0\r\n" + //
                                "  f4 := f89 IADD c8\r\n" + //
                                "  c7 := f4\r\n" + //
                                "  GOTO IFEND_6_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_6_LEVEL_0_0\r\n" + //
                                "  f81 |< c7\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RGreaterThanOrEqualTo\r\n" + //
                                "  a26 <- f93\r\n" + //
                                "  a28 <- f94\r\n" + //
                                "  a15 := FALSE\r\n" + //
                                "  a16 := FALSE\r\n" + //
                                "  a17 := FALSE\r\n" + //
                                "  a18 := FALSE\r\n" + //
                                "  a19 := FALSE\r\n" + //
                                "  a20 := FALSE\r\n" + //
                                "  a21 := FALSE\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  a23 := 0\r\n" + //
                                "  a24 := 0\r\n" + //
                                "  a25 := 0\r\n" + //
                                "  CALL RealIsZero ( a26 -> e52 )\r\n" + //
                                "  a27 <| e55\r\n" + //
                                "  a16 := a27\r\n" + //
                                "  CALL RealIsZero ( a28 -> e52 )\r\n" + //
                                "  a29 <| e55\r\n" + //
                                "  a17 := a29\r\n" + //
                                "  CALL RealIsNegative ( a26 -> f64 )\r\n" + //
                                "  a30 <| f67\r\n" + //
                                "  a18 := a30\r\n" + //
                                "  CALL RealIsNegative ( a28 -> f64 )\r\n" + //
                                "  a31 <| f67\r\n" + //
                                "  a19 := a31\r\n" + //
                                "  CALL RealIsPositive ( a26 -> f74 )\r\n" + //
                                "  a32 <| f77\r\n" + //
                                "  a20 := a32\r\n" + //
                                "  CALL RealIsPositive ( a28 -> f74 )\r\n" + //
                                "  a33 <| f77\r\n" + //
                                "  a21 := a33\r\n" + //
                                "  a34 := a16 LAND a17\r\n" + //
                                "  IF a34 EQ TRUE THEN IFSTAT_54_SEQ_0_LEVEL_0 ELSE IFNEXT_54_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_54_SEQ_0_LEVEL_0\r\n" + //
                                "  a35 := FALSE\r\n" + //
                                "  a15 := a35\r\n" + //
                                "  GOTO IFEND_54_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_54_SEQ_0_LEVEL_0\r\n" + //
                                "  a36 := a18 LAND a17\r\n" + //
                                "  IF a36 EQ TRUE THEN IFSTAT_54_SEQ_1_LEVEL_0 ELSE IFNEXT_54_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_54_SEQ_1_LEVEL_0\r\n" + //
                                "  a37 := TRUE\r\n" + //
                                "  a15 := a37\r\n" + //
                                "  GOTO IFEND_54_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_54_SEQ_1_LEVEL_0\r\n" + //
                                "  a38 := a18 LAND a21\r\n" + //
                                "  IF a38 EQ TRUE THEN IFSTAT_54_SEQ_2_LEVEL_0 ELSE IFNEXT_54_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_54_SEQ_2_LEVEL_0\r\n" + //
                                "  a39 := TRUE\r\n" + //
                                "  a15 := a39\r\n" + //
                                "  GOTO IFEND_54_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_54_SEQ_2_LEVEL_0\r\n" + //
                                "  a40 := a16 LAND a21\r\n" + //
                                "  IF a40 EQ TRUE THEN IFSTAT_54_SEQ_3_LEVEL_0 ELSE IFNEXT_54_SEQ_3_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_54_SEQ_3_LEVEL_0\r\n" + //
                                "  a41 := TRUE\r\n" + //
                                "  a15 := a41\r\n" + //
                                "  GOTO IFEND_54_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_54_SEQ_3_LEVEL_0\r\n" + //
                                "  a42 := a18 LAND a19\r\n" + //
                                "  IF a42 EQ TRUE THEN IFSTAT_54_SEQ_4_LEVEL_0 ELSE IFNEXT_54_SEQ_4_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_54_SEQ_4_LEVEL_0\r\n" + //
                                "  CALL RealScore ( a26 -> f80 )\r\n" + //
                                "  a43 <| f81\r\n" + //
                                "  a22 := a43\r\n" + //
                                "  CALL RealScore ( a28 -> f80 )\r\n" + //
                                "  a44 <| f81\r\n" + //
                                "  a23 := a44\r\n" + //
                                "  a45 := a22 LT a23\r\n" + //
                                "  IF a45 EQ TRUE THEN IFSTAT_55_SEQ_0_LEVEL_1 ELSE IFNEXT_55_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_55_SEQ_0_LEVEL_1\r\n" + //
                                "  a46 := TRUE\r\n" + //
                                "  a15 := a46\r\n" + //
                                "  GOTO IFEND_55_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_55_SEQ_0_LEVEL_1\r\n" + //
                                "  a47 := a22 EQ a23\r\n" + //
                                "  IF a47 EQ TRUE THEN IFSTAT_55_SEQ_1_LEVEL_1 ELSE IFNEXT_55_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_55_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( a26 -> c78 )\r\n" + //
                                "  a48 <| c81\r\n" + //
                                "  a24 := a48\r\n" + //
                                "  CALL RealMantissa ( a28 -> c78 )\r\n" + //
                                "  a49 <| c81\r\n" + //
                                "  a25 := a49\r\n" + //
                                "  a50 := a24 LE a25\r\n" + //
                                "  IF a50 EQ TRUE THEN IFSTAT_56_SEQ_0_LEVEL_2 ELSE IFNEXT_56_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_56_SEQ_0_LEVEL_2\r\n" + //
                                "  a51 := TRUE\r\n" + //
                                "  a15 := a51\r\n" + //
                                "  GOTO IFEND_56_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_0_LEVEL_2\r\n" + //
                                "  a52 := FALSE\r\n" + //
                                "  a15 := a52\r\n" + //
                                "  GOTO IFEND_56_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_56_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_56_LEVEL_2\r\n" + //
                                "  GOTO IFEND_55_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_55_SEQ_1_LEVEL_1\r\n" + //
                                "  a53 := FALSE\r\n" + //
                                "  a15 := a53\r\n" + //
                                "  GOTO IFEND_55_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_55_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_55_LEVEL_1\r\n" + //
                                "  GOTO IFEND_54_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_54_SEQ_4_LEVEL_0\r\n" + //
                                "  a54 := a20 LAND a21\r\n" + //
                                "  IF a54 EQ TRUE THEN IFSTAT_54_SEQ_5_LEVEL_0 ELSE IFNEXT_54_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_54_SEQ_5_LEVEL_0\r\n" + //
                                "  CALL RealScore ( a26 -> f80 )\r\n" + //
                                "  a55 <| f81\r\n" + //
                                "  a22 := a55\r\n" + //
                                "  CALL RealScore ( a28 -> f80 )\r\n" + //
                                "  a56 <| f81\r\n" + //
                                "  a22 := a56\r\n" + //
                                "  a57 := a22 GT a23\r\n" + //
                                "  IF a57 EQ TRUE THEN IFSTAT_60_SEQ_0_LEVEL_1 ELSE IFNEXT_60_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_60_SEQ_0_LEVEL_1\r\n" + //
                                "  a58 := TRUE\r\n" + //
                                "  a15 := a58\r\n" + //
                                "  GOTO IFEND_60_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_60_SEQ_0_LEVEL_1\r\n" + //
                                "  a59 := a22 EQ a23\r\n" + //
                                "  IF a59 EQ TRUE THEN IFSTAT_60_SEQ_1_LEVEL_1 ELSE IFNEXT_60_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_60_SEQ_1_LEVEL_1\r\n" + //
                                "  CALL RealMantissa ( a26 -> c78 )\r\n" + //
                                "  a60 <| c81\r\n" + //
                                "  a24 := a60\r\n" + //
                                "  CALL RealMantissa ( a28 -> c78 )\r\n" + //
                                "  a61 <| c81\r\n" + //
                                "  a25 := a61\r\n" + //
                                "  a62 := a24 GE a25\r\n" + //
                                "  IF a62 EQ TRUE THEN IFSTAT_61_SEQ_0_LEVEL_2 ELSE IFNEXT_61_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_61_SEQ_0_LEVEL_2\r\n" + //
                                "  a63 := TRUE\r\n" + //
                                "  a15 := a63\r\n" + //
                                "  GOTO IFEND_61_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_0_LEVEL_2\r\n" + //
                                "  a64 := FALSE\r\n" + //
                                "  a15 := a64\r\n" + //
                                "  GOTO IFEND_61_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_61_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_61_LEVEL_2\r\n" + //
                                "  GOTO IFEND_60_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_60_SEQ_1_LEVEL_1\r\n" + //
                                "  a65 := FALSE\r\n" + //
                                "  a15 := a65\r\n" + //
                                "  GOTO IFEND_60_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_60_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_60_LEVEL_1\r\n" + //
                                "  GOTO IFEND_54_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_54_SEQ_5_LEVEL_0\r\n" + //
                                "  LABEL IFEND_54_LEVEL_0\r\n" + //
                                "  f95 |< a15\r\n" + //
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

    @Test
    public void testSingleConversion2(){
        String progSrc = "test_source/SingleConversion2.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " a23 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 6.5\r\n" + //
                                " b := a\r\n" + //
                                " c := 0\r\n" + //
                                " a22 := 127\r\n" + //
                                " a23 := a22\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL RealToInt ( b -> Y6 )\r\n" + //
                                " Y5 <| Y9\r\n" + //
                                " c := Y5\r\n" + //
                                " CALL WriteInt ( c -> a75 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RealToInt\r\n" + //
                                "  Y7 <- Y6\r\n" + //
                                "  Y8 := 0\r\n" + //
                                "  Z0 := 0\r\n" + //
                                "  Z1 := 0\r\n" + //
                                "  Z2 := 0\r\n" + //
                                "  Z3 := 0\r\n" + //
                                "  CALL RealExponent ( Y7 -> Z5 )\r\n" + //
                                "  Z4 <| Z8\r\n" + //
                                "  Z0 := Z4\r\n" + //
                                "  CALL RealMantissa ( Y7 -> a25 )\r\n" + //
                                "  a24 <| a28\r\n" + //
                                "  Z1 := a24\r\n" + //
                                "  CALL RealSign ( Y7 -> a36 )\r\n" + //
                                "  a35 <| a39\r\n" + //
                                "  Z2 := a35\r\n" + //
                                "  a46 := 0\r\n" + //
                                "  a47 := Z0 GT a46\r\n" + //
                                "  IF a47 EQ TRUE THEN IFSTAT_3_SEQ_0_LEVEL_0_0 ELSE IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a48 := Z1 ILSHIFT Z0\r\n" + //
                                "  Z3 := a48\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a49 := 0\r\n" + //
                                "  a50 := Z0 LT a49\r\n" + //
                                "  IF a50 EQ TRUE THEN IFSTAT_3_SEQ_1_LEVEL_0 ELSE IFNEXT_3_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_3_SEQ_1_LEVEL_0\r\n" + //
                                "  a51 := Z1 IRSHIFT Z0\r\n" + //
                                "  Z3 := a51\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_1_LEVEL_0_0\r\n" + //
                                "  Z3 := Z1\r\n" + //
                                "  GOTO IFEND_3_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_3_SEQ_2_LEVEL_0\r\n" + //
                                "  LABEL IFEND_3_LEVEL_0_0\r\n" + //
                                "  a52 := 23\r\n" + //
                                "  a53 := Z3 IRSHIFT a52\r\n" + //
                                "  Y8 := a53\r\n" + //
                                "  a54 := 1\r\n" + //
                                "  a55 := Z2 EQ a54\r\n" + //
                                "  IF a55 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_0_0 ELSE IFNEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  CALL IntToBool ( Y8 -> a56 )\r\n" + //
                                "  a71 <| a59\r\n" + //
                                "  a72 := BNOT a71\r\n" + //
                                "  Y8 := a72\r\n" + //
                                "  a73 := 1\r\n" + //
                                "  a74 := Y8 IADD a73\r\n" + //
                                "  Y8 := a74\r\n" + //
                                "  GOTO IFEND_4_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_4_LEVEL_0_0\r\n" + //
                                "  Y9 |< Y8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  Z6 <- Z5\r\n" + //
                                "  Z7 := 0\r\n" + //
                                "  Z9 := 0\r\n" + //
                                "  a10 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( Z6 -> a12 )\r\n" + //
                                "  a11 <| a15\r\n" + //
                                "  Z9 := a11\r\n" + //
                                "  a17 := 23\r\n" + //
                                "  a18 := Z9 IRSHIFT a17\r\n" + //
                                "  a19 := 255\r\n" + //
                                "  a20 := a18 IAND a19\r\n" + //
                                "  a10 := a20\r\n" + //
                                "  a21 := a10 ISUB a23\r\n" + //
                                "  Z7 := a21\r\n" + //
                                "  Z8 |< Z7\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  a13 <- a12\r\n" + //
                                "  a14 := 0\r\n" + //
                                "  a16 := 0.0\r\n" + //
                                "  IPARAM a16\r\n" + //
                                "  IPARAM a13\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a16\r\n" + //
                                "  IPARAM a14\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a15 |< a14\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  a26 <- a25\r\n" + //
                                "  a29 := 0\r\n" + //
                                "  a30 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a26 -> a12 )\r\n" + //
                                "  a31 <| a15\r\n" + //
                                "  a30 := a31\r\n" + //
                                "  a32 := 8388607\r\n" + //
                                "  a33 := a30 IAND a32\r\n" + //
                                "  a29 := a33\r\n" + //
                                "  a34 := 8388608\r\n" + //
                                "  a27 := a29 IOR a34\r\n" + //
                                "  a28 |< a27\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a37 <- a36\r\n" + //
                                "  a38 := 0\r\n" + //
                                "  a40 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a37 -> a12 )\r\n" + //
                                "  a41 <| a15\r\n" + //
                                "  a40 := a41\r\n" + //
                                "  a42 := 31\r\n" + //
                                "  a43 := a40 IRSHIFT a42\r\n" + //
                                "  a44 := 1\r\n" + //
                                "  a45 := a43 IAND a44\r\n" + //
                                "  a38 := a45\r\n" + //
                                "  a39 |< a38\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToBool\r\n" + //
                                "  a57 <- a56\r\n" + //
                                "  a58 := FALSE\r\n" + //
                                "  CALL IntIsZero ( a57 -> a61 )\r\n" + //
                                "  a60 <| a64\r\n" + //
                                "  IF a60 EQ TRUE THEN IFSTAT_7_SEQ_0_LEVEL_0 ELSE IFNEXT_7_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_7_SEQ_0_LEVEL_0\r\n" + //
                                "  a69 := FALSE\r\n" + //
                                "  a58 := a69\r\n" + //
                                "  GOTO IFEND_7_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_0_LEVEL_0\r\n" + //
                                "  a70 := TRUE\r\n" + //
                                "  a58 := a70\r\n" + //
                                "  GOTO IFEND_7_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_7_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_7_LEVEL_0\r\n" + //
                                "  a59 |< a58\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntIsZero\r\n" + //
                                "  a62 <- a61\r\n" + //
                                "  a63 := FALSE\r\n" + //
                                "  a65 := 0\r\n" + //
                                "  a66 := a62 EQ a65\r\n" + //
                                "  IF a66 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a67 := TRUE\r\n" + //
                                "  a63 := a67\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a68 := FALSE\r\n" + //
                                "  a63 := a68\r\n" + //
                                "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                                "  a64 |< a63\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  a76 <- a75\r\n" + //
                                "  IPARAM a76\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRealAddition(){
        String progSrc = "test_source/RealAddition.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " a45 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 4.5\r\n" + //
                                " b := a\r\n" + //
                                " c := 8.5\r\n" + //
                                " d := c\r\n" + //
                                " e := 0.0\r\n" + //
                                " a44 := 127\r\n" + //
                                " a45 := a44\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL RAdd ( b -> Y6 , d -> Y8 )\r\n" + //
                                " Y5 <| Z1\r\n" + //
                                " e := Y5\r\n" + //
                                " CALL WriteReal ( e -> b45 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  Y7 <- Y6\r\n" + //
                                "  Y9 <- Y8\r\n" + //
                                "  Z0 := 0.0\r\n" + //
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
                                "  a12 := 0\r\n" + //
                                "  a13 := 0\r\n" + //
                                "  CALL RealSign ( Y7 -> a15 )\r\n" + //
                                "  a14 <| a18\r\n" + //
                                "  Z3 := a14\r\n" + //
                                "  CALL RealSign ( Y9 -> a15 )\r\n" + //
                                "  a30 <| a18\r\n" + //
                                "  Z4 := a30\r\n" + //
                                "  CALL RealExponent ( Y7 -> a32 )\r\n" + //
                                "  a31 <| a35\r\n" + //
                                "  Z6 := a31\r\n" + //
                                "  CALL RealExponent ( Y9 -> a32 )\r\n" + //
                                "  a46 <| a35\r\n" + //
                                "  Z7 := a46\r\n" + //
                                "  CALL RealMantissa ( Y7 -> a48 )\r\n" + //
                                "  a47 <| a51\r\n" + //
                                "  Z8 := a47\r\n" + //
                                "  CALL RealMantissa ( Y9 -> a48 )\r\n" + //
                                "  a58 <| a51\r\n" + //
                                "  Z9 := a58\r\n" + //
                                "  a59 := Z3 EQ Z4\r\n" + //
                                "  IF a59 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a13 := Z3\r\n" + //
                                "  a60 := Z6 EQ Z7\r\n" + //
                                "  IF a60 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  a61 := Z8 IADD Z9\r\n" + //
                                "  a12 := a61\r\n" + //
                                "  a62 := 25\r\n" + //
                                "  a63 := a12 IRSHIFT a62\r\n" + //
                                "  a64 := 1\r\n" + //
                                "  a65 := a63 IAND a64\r\n" + //
                                "  a11 := a65\r\n" + //
                                "  a66 := Z6 IADD a45\r\n" + //
                                "  a10 := a66\r\n" + //
                                "  a67 := 1\r\n" + //
                                "  a68 := a11 EQ a67\r\n" + //
                                "  IF a68 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  a69 := 1\r\n" + //
                                "  a70 := a10 IADD a69\r\n" + //
                                "  a10 := a70\r\n" + //
                                "  a71 := 1\r\n" + //
                                "  a72 := a12 IRSHIFT a71\r\n" + //
                                "  a12 := a72\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  a73 := 255\r\n" + //
                                "  a74 := a10 IAND a73\r\n" + //
                                "  a75 := 23\r\n" + //
                                "  a76 := a74 ILSHIFT a75\r\n" + //
                                "  Z2 := a76\r\n" + //
                                "  a77 := Z2 IOR a12\r\n" + //
                                "  Z2 := a77\r\n" + //
                                "  a78 := 31\r\n" + //
                                "  a79 := a13 ILSHIFT a78\r\n" + //
                                "  a80 := Z2 IOR a79\r\n" + //
                                "  Z2 := a80\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  a81 := Z6 GT Z7\r\n" + //
                                "  IF a81 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  a82 := Z6 ISUB Z7\r\n" + //
                                "  Z5 := a82\r\n" + //
                                "  Z7 := Z6\r\n" + //
                                "  a83 := Z6 IADD a45\r\n" + //
                                "  a10 := a83\r\n" + //
                                "  a84 := Z9 IRSHIFT Z5\r\n" + //
                                "  Z9 := a84\r\n" + //
                                "  a85 := Z8 IADD Z9\r\n" + //
                                "  a12 := a85\r\n" + //
                                "  a86 := 25\r\n" + //
                                "  a87 := a12 IRSHIFT a86\r\n" + //
                                "  a88 := 1\r\n" + //
                                "  a89 := a87 IAND a88\r\n" + //
                                "  a11 := a89\r\n" + //
                                "  a90 := 1\r\n" + //
                                "  a91 := a11 EQ a90\r\n" + //
                                "  IF a91 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  a92 := 1\r\n" + //
                                "  a93 := a10 IADD a92\r\n" + //
                                "  a10 := a93\r\n" + //
                                "  a94 := 1\r\n" + //
                                "  a95 := a12 IRSHIFT a94\r\n" + //
                                "  a12 := a95\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  a96 := 255\r\n" + //
                                "  a97 := a10 IAND a96\r\n" + //
                                "  a98 := 23\r\n" + //
                                "  a99 := a97 ILSHIFT a98\r\n" + //
                                "  Z2 := a99\r\n" + //
                                "  b10 := 31\r\n" + //
                                "  b11 := a13 ILSHIFT b10\r\n" + //
                                "  b12 := Z2 IOR b11\r\n" + //
                                "  Z2 := b12\r\n" + //
                                "  b13 := Z2 IOR a12\r\n" + //
                                "  Z2 := b13\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b14 := Z7 ISUB Z6\r\n" + //
                                "  Z5 := b14\r\n" + //
                                "  Z6 := Z7\r\n" + //
                                "  b15 := Z7 IADD a45\r\n" + //
                                "  a10 := b15\r\n" + //
                                "  b16 := Z8 IRSHIFT Z5\r\n" + //
                                "  Z8 := b16\r\n" + //
                                "  b17 := Z8 IADD Z9\r\n" + //
                                "  a12 := b17\r\n" + //
                                "  b18 := 25\r\n" + //
                                "  b19 := a12 IRSHIFT b18\r\n" + //
                                "  b20 := 1\r\n" + //
                                "  b21 := b19 IAND b20\r\n" + //
                                "  a11 := b21\r\n" + //
                                "  b22 := 1\r\n" + //
                                "  b23 := a11 EQ b22\r\n" + //
                                "  IF b23 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  b24 := 1\r\n" + //
                                "  b25 := a10 IADD b24\r\n" + //
                                "  a10 := b25\r\n" + //
                                "  b26 := 1\r\n" + //
                                "  b27 := a12 IRSHIFT b26\r\n" + //
                                "  a12 := b27\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  g4 := 255\r\n" + //
                                "  g5 := a10 IAND g4\r\n" + //
                                "  g6 := 23\r\n" + //
                                "  b28 := g5 ILSHIFT g6\r\n" + //
                                "  Z2 := b28\r\n" + //
                                "  b29 := 31\r\n" + //
                                "  b30 := a13 ILSHIFT b29\r\n" + //
                                "  b31 := Z2 IOR b30\r\n" + //
                                "  Z2 := b31\r\n" + //
                                "  b32 := Z2 IOR a12\r\n" + //
                                "  Z2 := b32\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b33 := 0\r\n" + //
                                "  b34 := Z3 EQ b33\r\n" + //
                                "  b35 := 1\r\n" + //
                                "  b36 := Z4 EQ b35\r\n" + //
                                "  b37 := b34 LAND b36\r\n" + //
                                "  IF b37 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  b38 := Z9 GT Z8\r\n" + //
                                "  IF b38 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  b39 := 1\r\n" + //
                                "  a13 := b39\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  b40 := 0\r\n" + //
                                "  a13 := b40\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  b41 := 1\r\n" + //
                                "  b42 := Z3 EQ b41\r\n" + //
                                "  i2 := 0\r\n" + //
                                "  i3 := Z4 EQ i2\r\n" + //
                                "  i4 := b42 LAND i3\r\n" + //
                                "  IF i4 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  i5 := Z9 GE Z8\r\n" + //
                                "  IF i5 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  i6 := 0\r\n" + //
                                "  a13 := i6\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  i7 := 1\r\n" + //
                                "  a13 := i7\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  i8 := Z6 EQ Z7\r\n" + //
                                "  IF i8 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  i9 := 0\r\n" + //
                                "  a12 := i9\r\n" + //
                                "  j0 := 25\r\n" + //
                                "  j1 := a12 IRSHIFT j0\r\n" + //
                                "  j2 := 1\r\n" + //
                                "  j3 := j1 IAND j2\r\n" + //
                                "  a11 := j3\r\n" + //
                                "  j4 := Z6 IADD a45\r\n" + //
                                "  a10 := j4\r\n" + //
                                "  j5 := 1\r\n" + //
                                "  j6 := a11 EQ j5\r\n" + //
                                "  IF j6 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  j7 := 1\r\n" + //
                                "  j8 := a10 IADD j7\r\n" + //
                                "  a10 := j8\r\n" + //
                                "  j9 := 1\r\n" + //
                                "  k0 := a12 IRSHIFT j9\r\n" + //
                                "  a12 := k0\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  k1 := 255\r\n" + //
                                "  k2 := a10 IAND k1\r\n" + //
                                "  k3 := 23\r\n" + //
                                "  k4 := k2 ILSHIFT k3\r\n" + //
                                "  Z2 := k4\r\n" + //
                                "  k5 := Z2 IOR a12\r\n" + //
                                "  Z2 := k5\r\n" + //
                                "  k6 := 31\r\n" + //
                                "  k7 := a13 ILSHIFT k6\r\n" + //
                                "  k8 := Z2 IOR k7\r\n" + //
                                "  Z2 := k8\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  k9 := Z6 GT Z7\r\n" + //
                                "  IF k9 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  l0 := Z6 ISUB Z7\r\n" + //
                                "  Z5 := l0\r\n" + //
                                "  Z7 := Z6\r\n" + //
                                "  l1 := Z6 IADD a45\r\n" + //
                                "  a10 := l1\r\n" + //
                                "  l2 := Z9 IRSHIFT Z5\r\n" + //
                                "  Z9 := l2\r\n" + //
                                "  l3 := 1\r\n" + //
                                "  l4 := Z3 EQ l3\r\n" + //
                                "  l5 := 0\r\n" + //
                                "  l6 := Z4 EQ l5\r\n" + //
                                "  l7 := l4 LAND l6\r\n" + //
                                "  IF l7 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  l8 := Z9 ISUB Z8\r\n" + //
                                "  a12 := l8\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  l9 := Z8 ISUB Z9\r\n" + //
                                "  a12 := l9\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  m0 := 25\r\n" + //
                                "  m1 := a12 IRSHIFT m0\r\n" + //
                                "  m2 := 1\r\n" + //
                                "  m3 := m1 IAND m2\r\n" + //
                                "  a11 := m3\r\n" + //
                                "  m4 := 1\r\n" + //
                                "  m5 := a11 EQ m4\r\n" + //
                                "  IF m5 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  m6 := 1\r\n" + //
                                "  m7 := a10 IADD m6\r\n" + //
                                "  a10 := m7\r\n" + //
                                "  m8 := 1\r\n" + //
                                "  m9 := a12 IRSHIFT m8\r\n" + //
                                "  a12 := m9\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  n0 := 23\r\n" + //
                                "  n1 := a10 ILSHIFT n0\r\n" + //
                                "  Z2 := n1\r\n" + //
                                "  n2 := 31\r\n" + //
                                "  n3 := a13 ILSHIFT n2\r\n" + //
                                "  n4 := Z2 IOR n3\r\n" + //
                                "  Z2 := n4\r\n" + //
                                "  n5 := Z2 IOR a12\r\n" + //
                                "  Z2 := n5\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  n6 := Z7 ISUB Z6\r\n" + //
                                "  Z5 := n6\r\n" + //
                                "  Z6 := Z7\r\n" + //
                                "  n7 := Z7 IADD a45\r\n" + //
                                "  a10 := n7\r\n" + //
                                "  n8 := Z8 IRSHIFT Z5\r\n" + //
                                "  Z8 := n8\r\n" + //
                                "  n9 := 1\r\n" + //
                                "  o0 := Z3 EQ n9\r\n" + //
                                "  o1 := 0\r\n" + //
                                "  o2 := Z4 EQ o1\r\n" + //
                                "  o3 := o0 LAND o2\r\n" + //
                                "  IF o3 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  o4 := Z9 ISUB Z8\r\n" + //
                                "  a12 := o4\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  o5 := Z8 ISUB Z9\r\n" + //
                                "  a12 := o5\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  o6 := 25\r\n" + //
                                "  o7 := a12 IRSHIFT o6\r\n" + //
                                "  o8 := 1\r\n" + //
                                "  o9 := o7 IAND o8\r\n" + //
                                "  a11 := o9\r\n" + //
                                "  p0 := 1\r\n" + //
                                "  p1 := a11 EQ p0\r\n" + //
                                "  IF p1 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  p2 := 1\r\n" + //
                                "  p3 := a10 IADD p2\r\n" + //
                                "  a10 := p3\r\n" + //
                                "  p4 := 1\r\n" + //
                                "  p5 := a12 IRSHIFT p4\r\n" + //
                                "  a12 := p5\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  p6 := 255\r\n" + //
                                "  p7 := a10 IAND p6\r\n" + //
                                "  p8 := 23\r\n" + //
                                "  p9 := p7 ILSHIFT p8\r\n" + //
                                "  Z2 := p9\r\n" + //
                                "  q0 := 31\r\n" + //
                                "  q1 := a13 ILSHIFT q0\r\n" + //
                                "  q2 := Z2 IOR q1\r\n" + //
                                "  Z2 := q2\r\n" + //
                                "  q3 := Z2 IOR a12\r\n" + //
                                "  Z2 := q3\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" +
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  CALL IntBinaryAsReal ( Z2 -> b43 )\r\n" + //
                                "  q4 <| b44\r\n" + //
                                "  Z0 := q4\r\n" + //
                                "  Z1 |< Z0\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a16 <- a15\r\n" + //
                                "  a17 := 0\r\n" + //
                                "  a19 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a16 -> a21 )\r\n" + //
                                "  a20 <| a24\r\n" + //
                                "  a19 := a20\r\n" + //
                                "  a26 := 31\r\n" + //
                                "  a27 := a19 IRSHIFT a26\r\n" + //
                                "  a28 := 1\r\n" + //
                                "  a29 := a27 IAND a28\r\n" + //
                                "  a17 := a29\r\n" + //
                                "  a18 |< a17\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  a22 <- a21\r\n" + //
                                "  a23 := 0\r\n" + //
                                "  a25 := 0.0\r\n" + //
                                "  IPARAM a25\r\n" + //
                                "  IPARAM a22\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a25\r\n" + //
                                "  IPARAM a23\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a24 |< a23\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  a33 <- a32\r\n" + //
                                "  a34 := 0\r\n" + //
                                "  a36 := 0\r\n" + //
                                "  a37 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a33 -> a21 )\r\n" + //
                                "  a38 <| a24\r\n" + //
                                "  a36 := a38\r\n" + //
                                "  a39 := 23\r\n" + //
                                "  a40 := a36 IRSHIFT a39\r\n" + //
                                "  a41 := 255\r\n" + //
                                "  a42 := a40 IAND a41\r\n" + //
                                "  a37 := a42\r\n" + //
                                "  a43 := a37 ISUB a45\r\n" + //
                                "  a34 := a43\r\n" + //
                                "  a35 |< a34\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  a49 <- a48\r\n" + //
                                "  a52 := 0\r\n" + //
                                "  a53 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a49 -> a21 )\r\n" + //
                                "  a54 <| a24\r\n" + //
                                "  a53 := a54\r\n" + //
                                "  a55 := 8388607\r\n" + //
                                "  a56 := a53 IAND a55\r\n" + //
                                "  a52 := a56\r\n" + //
                                "  a57 := 8388608\r\n" + //
                                "  a50 := a52 IOR a57\r\n" + //
                                "  a51 |< a50\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  g9 <- b43\r\n" + //
                                "  g7 := 0.0\r\n" + //
                                "  g8 := 0\r\n" + //
                                "  IPARAM g8\r\n" + //
                                "  IPARAM g9\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM g8\r\n" + //
                                "  IPARAM g7\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  b44 |< g7\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  b46 <- b45\r\n" + //
                                "  IPARAM b46\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRealAddition2(){
        String progSrc = "test_source/RealAddition2.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " a67 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 7.5\r\n" + //
                                " b := a\r\n" + //
                                " c := 49.5\r\n" + //
                                " d := c\r\n" + //
                                " e := 0.0\r\n" + //
                                " a66 := 127\r\n" + //
                                " a67 := a66\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL RSub ( d -> Z8 , b -> Z9 )\r\n" + //
                                " Z7 <| a10\r\n" + //
                                " e := Z7\r\n" + //
                                " CALL WriteReal ( e -> b62 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RSub\r\n" + //
                                "  r8 <- Z8\r\n" + //
                                "  r9 <- Z9\r\n" + //
                                "  r7 := 0.0\r\n" + //
                                "  CALL RNeg ( r9 -> a11 )\r\n" + //
                                "  s0 <| a12\r\n" + //
                                "  CALL RAdd ( r8 -> a23 , s0 -> a25 )\r\n" + //
                                "  s1 <| a28\r\n" + //
                                "  r7 := s1\r\n" + //
                                "  a10 |< r7\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RNeg\r\n" + //
                                "  B1 <- a11\r\n" + //
                                "  A4 := 0\r\n" + //
                                "  A5 := 0\r\n" + //
                                "  A6 := 0\r\n" + //
                                "  A7 := 0\r\n" + //
                                "  A8 := 1\r\n" + //
                                "  A9 := 31\r\n" + //
                                "  B0 := A8 ILSHIFT A9\r\n" + //
                                "  A4 := B0\r\n" + //
                                "  CALL RealBinaryAsInt ( B1 -> a13 )\r\n" + //
                                "  B2 <| a16\r\n" + //
                                "  A5 := B2\r\n" + //
                                "  B3 := A5 IXOR A4\r\n" + //
                                "  A7 := B3\r\n" + //
                                "  CALL IntBinaryAsReal ( A7 -> a18 )\r\n" + //
                                "  B4 <| a21\r\n" + //
                                "  A6 := B4\r\n" + //
                                "  a12 |< A6\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  a14 <- a13\r\n" + //
                                "  a15 := 0\r\n" + //
                                "  a17 := 0.0\r\n" + //
                                "  IPARAM a17\r\n" + //
                                "  IPARAM a14\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a17\r\n" + //
                                "  IPARAM a15\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a16 |< a15\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a19 <- a18\r\n" + //
                                "  a20 := 0.0\r\n" + //
                                "  a22 := 0\r\n" + //
                                "  IPARAM a22\r\n" + //
                                "  IPARAM a19\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a22\r\n" + //
                                "  IPARAM a20\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a21 |< a20\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  a24 <- a23\r\n" + //
                                "  a26 <- a25\r\n" + //
                                "  a27 := 0.0\r\n" + //
                                "  a29 := 0\r\n" + //
                                "  a30 := 0\r\n" + //
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
                                "  CALL RealSign ( a24 -> a42 )\r\n" + //
                                "  a41 <| a45\r\n" + //
                                "  a30 := a41\r\n" + //
                                "  CALL RealSign ( a26 -> a42 )\r\n" + //
                                "  a52 <| a45\r\n" + //
                                "  a31 := a52\r\n" + //
                                "  CALL RealExponent ( a24 -> a54 )\r\n" + //
                                "  a53 <| a57\r\n" + //
                                "  a33 := a53\r\n" + //
                                "  CALL RealExponent ( a26 -> a54 )\r\n" + //
                                "  a68 <| a57\r\n" + //
                                "  a34 := a68\r\n" + //
                                "  CALL RealMantissa ( a24 -> a70 )\r\n" + //
                                "  a69 <| a73\r\n" + //
                                "  a35 := a69\r\n" + //
                                "  CALL RealMantissa ( a26 -> a70 )\r\n" + //
                                "  a80 <| a73\r\n" + //
                                "  a36 := a80\r\n" + //
                                "  a81 := a30 EQ a31\r\n" + //
                                "  IF a81 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a40 := a30\r\n" + //
                                "  a82 := a33 EQ a34\r\n" + //
                                "  IF a82 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  a83 := a35 IADD a36\r\n" + //
                                "  a39 := a83\r\n" + //
                                "  a84 := 25\r\n" + //
                                "  a85 := a39 IRSHIFT a84\r\n" + //
                                "  a86 := 1\r\n" + //
                                "  a87 := a85 IAND a86\r\n" + //
                                "  a38 := a87\r\n" + //
                                "  a88 := a33 IADD a67\r\n" + //
                                "  a37 := a88\r\n" + //
                                "  a89 := 1\r\n" + //
                                "  a90 := a38 EQ a89\r\n" + //
                                "  IF a90 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  a91 := 1\r\n" + //
                                "  a92 := a37 IADD a91\r\n" + //
                                "  a37 := a92\r\n" + //
                                "  a93 := 1\r\n" + //
                                "  a94 := a39 IRSHIFT a93\r\n" + //
                                "  a39 := a94\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  a95 := 255\r\n" + //
                                "  a96 := a37 IAND a95\r\n" + //
                                "  a97 := 23\r\n" + //
                                "  a98 := a96 ILSHIFT a97\r\n" + //
                                "  a29 := a98\r\n" + //
                                "  a99 := 8388607\r\n" + //
                                "  b10 := a39 IAND a99\r\n" + //
                                "  b11 := a29 IOR b10\r\n" + //
                                "  a29 := b11\r\n" + //
                                "  b12 := 31\r\n" + //
                                "  b13 := a40 ILSHIFT b12\r\n" + //
                                "  b14 := a29 IOR b13\r\n" + //
                                "  a29 := b14\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b15 := a33 GT a34\r\n" + //
                                "  IF b15 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b16 := a33 ISUB a34\r\n" + //
                                "  a32 := b16\r\n" + //
                                "  a34 := a33\r\n" + //
                                "  b17 := a33 IADD a67\r\n" + //
                                "  a37 := b17\r\n" + //
                                "  b18 := a36 IRSHIFT a32\r\n" + //
                                "  a36 := b18\r\n" + //
                                "  b19 := a35 IADD a36\r\n" + //
                                "  a39 := b19\r\n" + //
                                "  b20 := 25\r\n" + //
                                "  b21 := a39 IRSHIFT b20\r\n" + //
                                "  b22 := 1\r\n" + //
                                "  b23 := b21 IAND b22\r\n" + //
                                "  a38 := b23\r\n" + //
                                "  b24 := 1\r\n" + //
                                "  b25 := a38 EQ b24\r\n" + //
                                "  IF b25 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  b26 := 1\r\n" + //
                                "  b27 := a37 IADD b26\r\n" + //
                                "  a37 := b27\r\n" + //
                                "  b28 := 1\r\n" + //
                                "  b29 := a39 IRSHIFT b28\r\n" + //
                                "  a39 := b29\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  b30 := 255\r\n" + //
                                "  b31 := a37 IAND b30\r\n" + //
                                "  b32 := 23\r\n" + //
                                "  b33 := b31 ILSHIFT b32\r\n" + //
                                "  a29 := b33\r\n" + //
                                "  b34 := 31\r\n" + //
                                "  b35 := a40 ILSHIFT b34\r\n" + //
                                "  b36 := a29 IOR b35\r\n" + //
                                "  a29 := b36\r\n" + //
                                "  b37 := 8388607\r\n" + //
                                "  b38 := a39 IAND b37\r\n" + //
                                "  b39 := a29 IOR b38\r\n" + //
                                "  a29 := b39\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b40 := a34 ISUB a33\r\n" + //
                                "  a32 := b40\r\n" + //
                                "  a33 := a34\r\n" + //
                                "  b41 := a34 IADD a67\r\n" + //
                                "  a37 := b41\r\n" + //
                                "  b42 := a35 IRSHIFT a32\r\n" + //
                                "  a35 := b42\r\n" + //
                                "  b43 := a35 IADD a36\r\n" + //
                                "  a39 := b43\r\n" + //
                                "  b44 := 25\r\n" + //
                                "  b45 := a39 IRSHIFT b44\r\n" + //
                                "  b46 := 1\r\n" + //
                                "  b47 := b45 IAND b46\r\n" + //
                                "  a38 := b47\r\n" + //
                                "  b48 := 1\r\n" + //
                                "  b49 := a38 EQ b48\r\n" + //
                                "  IF b49 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  g4 := 1\r\n" + //
                                "  g5 := a37 IADD g4\r\n" + //
                                "  a37 := g5\r\n" + //
                                "  g6 := 1\r\n" + //
                                "  g7 := a39 IRSHIFT g6\r\n" + //
                                "  a39 := g7\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  g8 := 255\r\n" + //
                                "  g9 := a37 IAND g8\r\n" + //
                                "  b50 := 23\r\n" + //
                                "  b51 := g9 ILSHIFT b50\r\n" + //
                                "  a29 := b51\r\n" + //
                                "  b52 := 31\r\n" + //
                                "  b53 := a40 ILSHIFT b52\r\n" + //
                                "  b54 := a29 IOR b53\r\n" + //
                                "  a29 := b54\r\n" + //
                                "  b55 := 8388607\r\n" + //
                                "  b56 := a39 IAND b55\r\n" + //
                                "  b57 := a29 IOR b56\r\n" + //
                                "  a29 := b57\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b58 := 0\r\n" + //
                                "  b59 := a30 EQ b58\r\n" + //
                                "  b60 := 1\r\n" + //
                                "  b61 := a31 EQ b60\r\n" + //
                                "  i2 := b59 LAND b61\r\n" + //
                                "  IF i2 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  i3 := a36 GT a35\r\n" + //
                                "  IF i3 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  i4 := 1\r\n" + //
                                "  a40 := i4\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  i5 := 0\r\n" + //
                                "  a40 := i5\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  i6 := 1\r\n" + //
                                "  i7 := a30 EQ i6\r\n" + //
                                "  i8 := 0\r\n" + //
                                "  i9 := a31 EQ i8\r\n" + //
                                "  j0 := i7 LAND i9\r\n" + //
                                "  IF j0 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  j1 := a36 GE a35\r\n" + //
                                "  IF j1 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  j2 := 0\r\n" + //
                                "  a40 := j2\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  j3 := 1\r\n" + //
                                "  a40 := j3\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  j4 := a33 EQ a34\r\n" + //
                                "  IF j4 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  j5 := 0\r\n" + //
                                "  a39 := j5\r\n" + //
                                "  j6 := 25\r\n" + //
                                "  j7 := a39 IRSHIFT j6\r\n" + //
                                "  j8 := 1\r\n" + //
                                "  j9 := j7 IAND j8\r\n" + //
                                "  a38 := j9\r\n" + //
                                "  k0 := a33 IADD a67\r\n" + //
                                "  a37 := k0\r\n" + //
                                "  k1 := 1\r\n" + //
                                "  k2 := a38 EQ k1\r\n" + //
                                "  IF k2 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  k3 := 1\r\n" + //
                                "  k4 := a37 IADD k3\r\n" + //
                                "  a37 := k4\r\n" + //
                                "  k5 := 1\r\n" + //
                                "  k6 := a39 IRSHIFT k5\r\n" + //
                                "  a39 := k6\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  k7 := 255\r\n" + //
                                "  k8 := a37 IAND k7\r\n" + //
                                "  k9 := 23\r\n" + //
                                "  l0 := k8 ILSHIFT k9\r\n" + //
                                "  a29 := l0\r\n" + //
                                "  l1 := 8388607\r\n" + //
                                "  l2 := a39 IAND l1\r\n" + //
                                "  l3 := a29 IOR l2\r\n" + //
                                "  a29 := l3\r\n" + //
                                "  l4 := 31\r\n" + //
                                "  l5 := a40 ILSHIFT l4\r\n" + //
                                "  l6 := a29 IOR l5\r\n" + //
                                "  a29 := l6\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  l7 := a33 GT a34\r\n" + //
                                "  IF l7 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  l8 := a33 ISUB a34\r\n" + //
                                "  a32 := l8\r\n" + //
                                "  a34 := a33\r\n" + //
                                "  l9 := a33 IADD a67\r\n" + //
                                "  a37 := l9\r\n" + //
                                "  m0 := a36 IRSHIFT a32\r\n" + //
                                "  a36 := m0\r\n" + //
                                "  m1 := 1\r\n" + //
                                "  m2 := a30 EQ m1\r\n" + //
                                "  m3 := 0\r\n" + //
                                "  m4 := a31 EQ m3\r\n" + //
                                "  m5 := m2 LAND m4\r\n" + //
                                "  IF m5 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  m6 := a36 ISUB a35\r\n" + //
                                "  a39 := m6\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  m7 := a35 ISUB a36\r\n" + //
                                "  a39 := m7\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  m8 := 25\r\n" + //
                                "  m9 := a39 IRSHIFT m8\r\n" + //
                                "  n0 := 1\r\n" + //
                                "  n1 := m9 IAND n0\r\n" + //
                                "  a38 := n1\r\n" + //
                                "  n2 := 1\r\n" + //
                                "  n3 := a38 EQ n2\r\n" + //
                                "  IF n3 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  n4 := 1\r\n" + //
                                "  n5 := a37 IADD n4\r\n" + //
                                "  a37 := n5\r\n" + //
                                "  n6 := 1\r\n" + //
                                "  n7 := a39 IRSHIFT n6\r\n" + //
                                "  a39 := n7\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  n8 := 23\r\n" + //
                                "  n9 := a37 ILSHIFT n8\r\n" + //
                                "  a29 := n9\r\n" + //
                                "  o0 := 31\r\n" + //
                                "  o1 := a40 ILSHIFT o0\r\n" + //
                                "  o2 := a29 IOR o1\r\n" + //
                                "  a29 := o2\r\n" + //
                                "  o3 := 8388607\r\n" + //
                                "  o4 := a39 IAND o3\r\n" + //
                                "  o5 := a29 IOR o4\r\n" + //
                                "  a29 := o5\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  o6 := a34 ISUB a33\r\n" + //
                                "  a32 := o6\r\n" + //
                                "  a33 := a34\r\n" + //
                                "  o7 := a34 IADD a67\r\n" + //
                                "  a37 := o7\r\n" + //
                                "  o8 := a35 IRSHIFT a32\r\n" + //
                                "  a35 := o8\r\n" + //
                                "  o9 := 1\r\n" + //
                                "  p0 := a30 EQ o9\r\n" + //
                                "  p1 := 0\r\n" + //
                                "  p2 := a31 EQ p1\r\n" + //
                                "  p3 := p0 LAND p2\r\n" + //
                                "  IF p3 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  p4 := a36 ISUB a35\r\n" + //
                                "  a39 := p4\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  p5 := a35 ISUB a36\r\n" + //
                                "  a39 := p5\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  p6 := 25\r\n" + //
                                "  p7 := a39 IRSHIFT p6\r\n" + //
                                "  p8 := 1\r\n" + //
                                "  p9 := p7 IAND p8\r\n" + //
                                "  a38 := p9\r\n" + //
                                "  q0 := 1\r\n" + //
                                "  q1 := a38 EQ q0\r\n" + //
                                "  IF q1 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  q2 := 1\r\n" + //
                                "  q3 := a37 IADD q2\r\n" + //
                                "  a37 := q3\r\n" + //
                                "  q4 := 1\r\n" + //
                                "  q5 := a39 IRSHIFT q4\r\n" + //
                                "  a39 := q5\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  q6 := 255\r\n" + //
                                "  q7 := a37 IAND q6\r\n" + //
                                "  q8 := 23\r\n" + //
                                "  q9 := q7 ILSHIFT q8\r\n" + //
                                "  a29 := q9\r\n" + //
                                "  r0 := 31\r\n" + //
                                "  r1 := a40 ILSHIFT r0\r\n" + //
                                "  r2 := a29 IOR r1\r\n" + //
                                "  a29 := r2\r\n" + //
                                "  r3 := 8388607\r\n" + //
                                "  r4 := a39 IAND r3\r\n" + //
                                "  r5 := a29 IOR r4\r\n" + //
                                "  a29 := r5\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  CALL IntBinaryAsReal ( a29 -> a18 )\r\n" + //
                                "  r6 <| a21\r\n" + //
                                "  a27 := r6\r\n" + //
                                "  a28 |< a27\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a43 <- a42\r\n" + //
                                "  a44 := 0\r\n" + //
                                "  a46 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a43 -> a13 )\r\n" + //
                                "  a47 <| a16\r\n" + //
                                "  a46 := a47\r\n" + //
                                "  a48 := 31\r\n" + //
                                "  a49 := a46 IRSHIFT a48\r\n" + //
                                "  a50 := 1\r\n" + //
                                "  a51 := a49 IAND a50\r\n" + //
                                "  a44 := a51\r\n" + //
                                "  a45 |< a44\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  a55 <- a54\r\n" + //
                                "  a56 := 0\r\n" + //
                                "  a58 := 0\r\n" + //
                                "  a59 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a55 -> a13 )\r\n" + //
                                "  a60 <| a16\r\n" + //
                                "  a58 := a60\r\n" + //
                                "  a61 := 23\r\n" + //
                                "  a62 := a58 IRSHIFT a61\r\n" + //
                                "  a63 := 255\r\n" + //
                                "  a64 := a62 IAND a63\r\n" + //
                                "  a59 := a64\r\n" + //
                                "  a65 := a59 ISUB a67\r\n" + //
                                "  a56 := a65\r\n" + //
                                "  a57 |< a56\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  a71 <- a70\r\n" + //
                                "  a74 := 0\r\n" + //
                                "  a75 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a71 -> a13 )\r\n" + //
                                "  a76 <| a16\r\n" + //
                                "  a75 := a76\r\n" + //
                                "  a77 := 8388607\r\n" + //
                                "  a78 := a75 IAND a77\r\n" + //
                                "  a74 := a78\r\n" + //
                                "  a79 := 8388608\r\n" + //
                                "  a72 := a74 IOR a79\r\n" + //
                                "  a73 |< a72\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  b63 <- b62\r\n" + //
                                "  IPARAM b63\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRealAddition3(){
        String progSrc = "test_source/RealAddition3.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " b12 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 50.0\r\n" + //
                                " b := a\r\n" + //
                                " c := 92.0\r\n" + //
                                " d := c\r\n" + //
                                " e := 0.0\r\n" + //
                                " b11 := 127\r\n" + //
                                " b12 := b11\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL RSub ( b -> a43 , d -> a44 )\r\n" + //
                                " a42 <| a45\r\n" + //
                                " e := a42\r\n" + //
                                " CALL WriteReal ( e -> c14 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RSub\r\n" + //
                                "  v4 <- a43\r\n" + //
                                "  v5 <- a44\r\n" + //
                                "  v3 := 0.0\r\n" + //
                                "  CALL RNeg ( v5 -> a46 )\r\n" + //
                                "  v6 <| a47\r\n" + //
                                "  CALL RAdd ( v4 -> a58 , v6 -> a60 )\r\n" + //
                                "  v7 <| a63\r\n" + //
                                "  v3 := v7\r\n" + //
                                "  a45 |< v3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RNeg\r\n" + //
                                "  E7 <- a46\r\n" + //
                                "  E0 := 0\r\n" + //
                                "  E1 := 0\r\n" + //
                                "  E2 := 0\r\n" + //
                                "  E3 := 0\r\n" + //
                                "  E4 := 1\r\n" + //
                                "  E5 := 31\r\n" + //
                                "  E6 := E4 ILSHIFT E5\r\n" + //
                                "  E0 := E6\r\n" + //
                                "  CALL RealBinaryAsInt ( E7 -> a48 )\r\n" + //
                                "  E8 <| a51\r\n" + //
                                "  E1 := E8\r\n" + //
                                "  E9 := E1 IXOR E0\r\n" + //
                                "  E3 := E9\r\n" + //
                                "  CALL IntBinaryAsReal ( E3 -> a53 )\r\n" + //
                                "  F0 <| a56\r\n" + //
                                "  E2 := F0\r\n" + //
                                "  a47 |< E2\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  a49 <- a48\r\n" + //
                                "  a50 := 0\r\n" + //
                                "  a52 := 0.0\r\n" + //
                                "  IPARAM a52\r\n" + //
                                "  IPARAM a49\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a52\r\n" + //
                                "  IPARAM a50\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a51 |< a50\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  a54 <- a53\r\n" + //
                                "  a55 := 0.0\r\n" + //
                                "  a57 := 0\r\n" + //
                                "  IPARAM a57\r\n" + //
                                "  IPARAM a54\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM a57\r\n" + //
                                "  IPARAM a55\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  a56 |< a55\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  a59 <- a58\r\n" + //
                                "  a61 <- a60\r\n" + //
                                "  a62 := 0.0\r\n" + //
                                "  a64 := 0\r\n" + //
                                "  a65 := 0\r\n" + //
                                "  a66 := 0\r\n" + //
                                "  a67 := 0\r\n" + //
                                "  a68 := 0\r\n" + //
                                "  a69 := 0\r\n" + //
                                "  a70 := 0\r\n" + //
                                "  a71 := 0\r\n" + //
                                "  a72 := 0\r\n" + //
                                "  a73 := 0\r\n" + //
                                "  a74 := 0\r\n" + //
                                "  a75 := 0\r\n" + //
                                "  CALL RealSign ( a59 -> a77 )\r\n" + //
                                "  a76 <| a80\r\n" + //
                                "  a65 := a76\r\n" + //
                                "  CALL RealSign ( a61 -> a77 )\r\n" + //
                                "  a87 <| a80\r\n" + //
                                "  a66 := a87\r\n" + //
                                "  CALL RealExponent ( a59 -> a89 )\r\n" + //
                                "  a88 <| a92\r\n" + //
                                "  a68 := a88\r\n" + //
                                "  CALL RealExponent ( a61 -> a89 )\r\n" + //
                                "  b13 <| a92\r\n" + //
                                "  a69 := b13\r\n" + //
                                "  CALL RealMantissa ( a59 -> b15 )\r\n" + //
                                "  b14 <| b18\r\n" + //
                                "  a70 := b14\r\n" + //
                                "  CALL RealMantissa ( a61 -> b15 )\r\n" + //
                                "  b25 <| b18\r\n" + //
                                "  a71 := b25\r\n" + //
                                "  b26 := a65 EQ a66\r\n" + //
                                "  IF b26 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a75 := a65\r\n" + //
                                "  b27 := a68 EQ a69\r\n" + //
                                "  IF b27 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b28 := a70 IADD a71\r\n" + //
                                "  a74 := b28\r\n" + //
                                "  b29 := 25\r\n" + //
                                "  b30 := a74 IRSHIFT b29\r\n" + //
                                "  b31 := 1\r\n" + //
                                "  b32 := b30 IAND b31\r\n" + //
                                "  a73 := b32\r\n" + //
                                "  b33 := a68 IADD b12\r\n" + //
                                "  a72 := b33\r\n" + //
                                "  b34 := 1\r\n" + //
                                "  b35 := a73 EQ b34\r\n" + //
                                "  IF b35 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  b36 := 1\r\n" + //
                                "  b37 := a72 IADD b36\r\n" + //
                                "  a72 := b37\r\n" + //
                                "  b38 := 1\r\n" + //
                                "  b39 := a74 IRSHIFT b38\r\n" + //
                                "  a74 := b39\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  b40 := 255\r\n" + //
                                "  b41 := a72 IAND b40\r\n" + //
                                "  b42 := 23\r\n" + //
                                "  b43 := b41 ILSHIFT b42\r\n" + //
                                "  a64 := b43\r\n" + //
                                "  b44 := 8388607\r\n" + //
                                "  b45 := a74 IAND b44\r\n" + //
                                "  b46 := a64 IOR b45\r\n" + //
                                "  a64 := b46\r\n" + //
                                "  b47 := 31\r\n" + //
                                "  b48 := a75 ILSHIFT b47\r\n" + //
                                "  b49 := a64 IOR b48\r\n" + //
                                "  a64 := b49\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  b50 := a68 GT a69\r\n" + //
                                "  IF b50 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b51 := a68 ISUB a69\r\n" + //
                                "  a67 := b51\r\n" + //
                                "  a69 := a68\r\n" + //
                                "  b52 := a68 IADD b12\r\n" + //
                                "  a72 := b52\r\n" + //
                                "  b53 := a71 IRSHIFT a67\r\n" + //
                                "  a71 := b53\r\n" + //
                                "  b54 := a70 IADD a71\r\n" + //
                                "  a74 := b54\r\n" + //
                                "  b55 := 25\r\n" + //
                                "  b56 := a74 IRSHIFT b55\r\n" + //
                                "  b57 := 1\r\n" + //
                                "  b58 := b56 IAND b57\r\n" + //
                                "  a73 := b58\r\n" + //
                                "  b59 := 1\r\n" + //
                                "  b60 := a73 EQ b59\r\n" + //
                                "  IF b60 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  b61 := 1\r\n" + //
                                "  b62 := a72 IADD b61\r\n" + //
                                "  a72 := b62\r\n" + //
                                "  b63 := 1\r\n" + //
                                "  b64 := a74 IRSHIFT b63\r\n" + //
                                "  a74 := b64\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  b65 := 255\r\n" + //
                                "  b66 := a72 IAND b65\r\n" + //
                                "  b67 := 23\r\n" + //
                                "  b68 := b66 ILSHIFT b67\r\n" + //
                                "  a64 := b68\r\n" + //
                                "  b69 := 31\r\n" + //
                                "  b70 := a75 ILSHIFT b69\r\n" + //
                                "  b71 := a64 IOR b70\r\n" + //
                                "  a64 := b71\r\n" + //
                                "  b72 := 8388607\r\n" + //
                                "  b73 := a74 IAND b72\r\n" + //
                                "  b74 := a64 IOR b73\r\n" + //
                                "  a64 := b74\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  b75 := a69 ISUB a68\r\n" + //
                                "  a67 := b75\r\n" + //
                                "  a68 := a69\r\n" + //
                                "  b76 := a69 IADD b12\r\n" + //
                                "  a72 := b76\r\n" + //
                                "  b77 := a70 IRSHIFT a67\r\n" + //
                                "  a70 := b77\r\n" + //
                                "  b78 := a70 IADD a71\r\n" + //
                                "  a74 := b78\r\n" + //
                                "  b79 := 25\r\n" + //
                                "  b80 := a74 IRSHIFT b79\r\n" + //
                                "  b81 := 1\r\n" + //
                                "  b82 := b80 IAND b81\r\n" + //
                                "  a73 := b82\r\n" + //
                                "  b83 := 1\r\n" + //
                                "  b84 := a73 EQ b83\r\n" + //
                                "  IF b84 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  g4 := 1\r\n" + //
                                "  g5 := a72 IADD g4\r\n" + //
                                "  a72 := g5\r\n" + //
                                "  g6 := 1\r\n" + //
                                "  g7 := a74 IRSHIFT g6\r\n" + //
                                "  a74 := g7\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  g8 := 255\r\n" + //
                                "  g9 := a72 IAND g8\r\n" + //
                                "  b85 := 23\r\n" + //
                                "  b86 := g9 ILSHIFT b85\r\n" + //
                                "  a64 := b86\r\n" + //
                                "  b87 := 31\r\n" + //
                                "  b88 := a75 ILSHIFT b87\r\n" + //
                                "  b89 := a64 IOR b88\r\n" + //
                                "  a64 := b89\r\n" + //
                                "  b90 := 8388607\r\n" + //
                                "  b91 := a74 IAND b90\r\n" + //
                                "  b92 := a64 IOR b91\r\n" + //
                                "  a64 := b92\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  b93 := a68 EQ a69\r\n" + //
                                "  IF b93 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  b94 := 0\r\n" + //
                                "  b95 := a65 EQ b94\r\n" + //
                                "  b96 := 1\r\n" + //
                                "  i2 := a66 EQ b96\r\n" + //
                                "  i3 := b95 LAND i2\r\n" + //
                                "  IF i3 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  i4 := a71 GT a70\r\n" + //
                                "  IF i4 EQ TRUE THEN IFSTAT_11_SEQ_0_LEVEL_3 ELSE IFNEXT_11_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_11_SEQ_0_LEVEL_3\r\n" + //
                                "  i5 := 1\r\n" + //
                                "  a75 := i5\r\n" + //
                                "  GOTO IFEND_11_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_11_SEQ_0_LEVEL_3\r\n" + //
                                "  i6 := 0\r\n" + //
                                "  a75 := i6\r\n" + //
                                "  GOTO IFEND_11_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_11_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_11_LEVEL_3\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  i7 := 1\r\n" + //
                                "  i8 := a65 EQ i7\r\n" + //
                                "  i9 := 0\r\n" + //
                                "  j0 := a66 EQ i9\r\n" + //
                                "  j1 := i8 LAND j0\r\n" + //
                                "  IF j1 EQ TRUE THEN IFSTAT_10_SEQ_1_LEVEL_2 ELSE IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  j2 := a71 GE a70\r\n" + //
                                "  IF j2 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  j3 := 0\r\n" + //
                                "  a75 := j3\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  j4 := 1\r\n" + //
                                "  a75 := j4\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  j5 := 0\r\n" + //
                                "  a74 := j5\r\n" + //
                                "  j6 := 25\r\n" + //
                                "  j7 := a74 IRSHIFT j6\r\n" + //
                                "  j8 := 1\r\n" + //
                                "  j9 := j7 IAND j8\r\n" + //
                                "  a73 := j9\r\n" + //
                                "  k0 := a68 IADD b12\r\n" + //
                                "  a72 := k0\r\n" + //
                                "  k1 := 1\r\n" + //
                                "  k2 := a73 EQ k1\r\n" + //
                                "  IF k2 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  k3 := 1\r\n" + //
                                "  k4 := a72 IADD k3\r\n" + //
                                "  a72 := k4\r\n" + //
                                "  k5 := 1\r\n" + //
                                "  k6 := a74 IRSHIFT k5\r\n" + //
                                "  a74 := k6\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  k7 := 255\r\n" + //
                                "  k8 := a72 IAND k7\r\n" + //
                                "  k9 := 23\r\n" + //
                                "  l0 := k8 ILSHIFT k9\r\n" + //
                                "  a64 := l0\r\n" + //
                                "  l1 := 8388607\r\n" + //
                                "  l2 := a74 IAND l1\r\n" + //
                                "  l3 := a64 IOR l2\r\n" + //
                                "  a64 := l3\r\n" + //
                                "  l4 := 31\r\n" + //
                                "  l5 := a75 ILSHIFT l4\r\n" + //
                                "  l6 := a64 IOR l5\r\n" + //
                                "  a64 := l6\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  l7 := a68 GT a69\r\n" + //
                                "  IF l7 EQ TRUE THEN IFSTAT_9_SEQ_1_LEVEL_1 ELSE IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  l8 := a68 ISUB a69\r\n" + //
                                "  a67 := l8\r\n" + //
                                "  a69 := a68\r\n" + //
                                "  l9 := a68 IADD b12\r\n" + //
                                "  a72 := l9\r\n" + //
                                "  m0 := a71 IRSHIFT a67\r\n" + //
                                "  a71 := m0\r\n" + //
                                "  m1 := 0\r\n" + //
                                "  m2 := a65 EQ m1\r\n" + //
                                "  m3 := 1\r\n" + //
                                "  m4 := a66 EQ m3\r\n" + //
                                "  m5 := m2 LAND m4\r\n" + //
                                "  IF m5 EQ TRUE THEN IFSTAT_17_SEQ_0_LEVEL_2 ELSE IFNEXT_17_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_17_SEQ_0_LEVEL_2\r\n" + //
                                "  m6 := a71 GT a70\r\n" + //
                                "  IF m6 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  m7 := 1\r\n" + //
                                "  a75 := m7\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  m8 := 0\r\n" + //
                                "  a75 := m8\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  GOTO IFEND_17_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_17_SEQ_0_LEVEL_2\r\n" + //
                                "  m9 := 1\r\n" + //
                                "  n0 := a65 EQ m9\r\n" + //
                                "  n1 := 0\r\n" + //
                                "  n2 := a66 EQ n1\r\n" + //
                                "  n3 := n0 LAND n2\r\n" + //
                                "  IF n3 EQ TRUE THEN IFSTAT_17_SEQ_1_LEVEL_2 ELSE IFNEXT_17_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_17_SEQ_1_LEVEL_2\r\n" + //
                                "  n4 := a71 GE a70\r\n" + //
                                "  IF n4 EQ TRUE THEN IFSTAT_20_SEQ_0_LEVEL_3 ELSE IFNEXT_20_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_20_SEQ_0_LEVEL_3\r\n" + //
                                "  n5 := 0\r\n" + //
                                "  a75 := n5\r\n" + //
                                "  GOTO IFEND_20_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_20_SEQ_0_LEVEL_3\r\n" + //
                                "  n6 := 1\r\n" + //
                                "  a75 := n6\r\n" + //
                                "  GOTO IFEND_20_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_20_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_20_LEVEL_3\r\n" +
                                "  GOTO IFEND_17_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_17_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_17_LEVEL_2\r\n" + //
                                "  n7 := 1\r\n" + //
                                "  n8 := a65 EQ n7\r\n" + //
                                "  n9 := 0\r\n" + //
                                "  o0 := a66 EQ n9\r\n" + //
                                "  o1 := n8 LAND o0\r\n" + //
                                "  IF o1 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_2 ELSE IFNEXT_22_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_2\r\n" + //
                                "  CALL INeg ( a70 -> b97 )\r\n" + //
                                "  o2 <| c10\r\n" + //
                                "  o3 := o2 IADD a71\r\n" + //
                                "  a74 := o3\r\n" + //
                                "  GOTO IFEND_22_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_2\r\n" + //
                                "  CALL INeg ( a71 -> b97 )\r\n" + //
                                "  o4 <| c10\r\n" + //
                                "  o5 := a70 IADD o4\r\n" + //
                                "  a74 := o5\r\n" + //
                                "  GOTO IFEND_22_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_22_LEVEL_2\r\n" + //
                                "  o6 := 25\r\n" + //
                                "  o7 := a74 IRSHIFT o6\r\n" + //
                                "  o8 := 1\r\n" + //
                                "  o9 := o7 IAND o8\r\n" + //
                                "  a73 := o9\r\n" + //
                                "  p0 := 1\r\n" + //
                                "  p1 := a73 EQ p0\r\n" + //
                                "  IF p1 EQ TRUE THEN IFSTAT_23_SEQ_0_LEVEL_2 ELSE IFNEXT_23_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_23_SEQ_0_LEVEL_2\r\n" + //
                                "  p2 := 1\r\n" + //
                                "  p3 := a72 IADD p2\r\n" + //
                                "  a72 := p3\r\n" + //
                                "  p4 := 1\r\n" + //
                                "  p5 := a74 IRSHIFT p4\r\n" + //
                                "  a74 := p5\r\n" + //
                                "  GOTO IFEND_23_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_23_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_23_LEVEL_2\r\n" + //
                                "  p6 := 23\r\n" + //
                                "  p7 := a72 ILSHIFT p6\r\n" + //
                                "  a64 := p7\r\n" + //
                                "  p8 := 31\r\n" + //
                                "  p9 := a75 ILSHIFT p8\r\n" + //
                                "  q0 := a64 IOR p9\r\n" + //
                                "  a64 := q0\r\n" + //
                                "  q1 := 8388607\r\n" + //
                                "  q2 := a74 IAND q1\r\n" + //
                                "  q3 := a64 IOR q2\r\n" + //
                                "  a64 := q3\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  q4 := a69 ISUB a68\r\n" + //
                                "  a67 := q4\r\n" + //
                                "  a68 := a69\r\n" + //
                                "  q5 := a69 IADD b12\r\n" + //
                                "  a72 := q5\r\n" + //
                                "  q6 := a70 IRSHIFT a67\r\n" + //
                                "  a70 := q6\r\n" + //
                                "  q7 := 0\r\n" + //
                                "  q8 := a65 EQ q7\r\n" + //
                                "  q9 := 1\r\n" + //
                                "  r0 := a66 EQ q9\r\n" + //
                                "  r1 := q8 LAND r0\r\n" + //
                                "  IF r1 EQ TRUE THEN IFSTAT_25_SEQ_0_LEVEL_2 ELSE IFNEXT_25_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_25_SEQ_0_LEVEL_2\r\n" + //
                                "  r2 := a71 GT a70\r\n" + //
                                "  IF r2 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_3 ELSE IFNEXT_26_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_3\r\n" + //
                                "  r3 := 1\r\n" + //
                                "  a75 := r3\r\n" + //
                                "  GOTO IFEND_26_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_3\r\n" + //
                                "  r4 := 0\r\n" + //
                                "  a75 := r4\r\n" + //
                                "  GOTO IFEND_26_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_26_LEVEL_3\r\n" + //
                                "  GOTO IFEND_25_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_25_SEQ_0_LEVEL_2\r\n" + //
                                "  r5 := 1\r\n" + //
                                "  r6 := a65 EQ r5\r\n" + //
                                "  r7 := 0\r\n" + //
                                "  r8 := a66 EQ r7\r\n" + //
                                "  r9 := r6 LAND r8\r\n" + //
                                "  IF r9 EQ TRUE THEN IFSTAT_25_SEQ_1_LEVEL_2 ELSE IFNEXT_25_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_25_SEQ_1_LEVEL_2\r\n" + //
                                "  s0 := a71 GE a70\r\n" + //
                                "  IF s0 EQ TRUE THEN IFSTAT_28_SEQ_0_LEVEL_3 ELSE IFNEXT_28_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_28_SEQ_0_LEVEL_3\r\n" + //
                                "  s1 := 0\r\n" + //
                                "  a75 := s1\r\n" + //
                                "  GOTO IFEND_28_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_28_SEQ_0_LEVEL_3\r\n" + //
                                "  s2 := 1\r\n" + //
                                "  a75 := s2\r\n" + //
                                "  GOTO IFEND_28_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_28_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_28_LEVEL_3\r\n" + //
                                "  GOTO IFEND_25_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_25_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_25_LEVEL_2\r\n" + //
                                "  s3 := 1\r\n" + //
                                "  s4 := a65 EQ s3\r\n" + //
                                "  s5 := 0\r\n" + //
                                "  s6 := a66 EQ s5\r\n" + //
                                "  s7 := s4 LAND s6\r\n" + //
                                "  IF s7 EQ TRUE THEN IFSTAT_30_SEQ_0_LEVEL_2 ELSE IFNEXT_30_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_30_SEQ_0_LEVEL_2\r\n" + //
                                "  CALL INeg ( a70 -> b97 )\r\n" + //
                                "  s8 <| c10\r\n" + //
                                "  s9 := s8 IADD a71\r\n" + //
                                "  a74 := s9\r\n" + //
                                "  GOTO IFEND_30_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_30_SEQ_0_LEVEL_2\r\n" + //
                                "  CALL INeg ( a71 -> b97 )\r\n" + //
                                "  t0 <| c10\r\n" + //
                                "  t1 := a70 IADD t0\r\n" + //
                                "  a74 := t1\r\n" + //
                                "  GOTO IFEND_30_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_30_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_30_LEVEL_2\r\n" + //
                                "  t2 := 25\r\n" + //
                                "  t3 := a74 IRSHIFT t2\r\n" + //
                                "  t4 := 1\r\n" + //
                                "  t5 := t3 IAND t4\r\n" + //
                                "  a73 := t5\r\n" + //
                                "  t6 := 1\r\n" + //
                                "  t7 := a73 EQ t6\r\n" + //
                                "  IF t7 EQ TRUE THEN IFSTAT_31_SEQ_0_LEVEL_2 ELSE IFNEXT_31_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_31_SEQ_0_LEVEL_2\r\n" + //
                                "  t8 := 1\r\n" + //
                                "  t9 := a72 IADD t8\r\n" + //
                                "  a72 := t9\r\n" + //
                                "  u0 := 1\r\n" + //
                                "  u1 := a74 IRSHIFT u0\r\n" + //
                                "  a74 := u1\r\n" + //
                                "  GOTO IFEND_31_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_31_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_31_LEVEL_2\r\n" + //
                                "  u2 := 255\r\n" + //
                                "  u3 := a72 IAND u2\r\n" + //
                                "  u4 := 23\r\n" + //
                                "  u5 := u3 ILSHIFT u4\r\n" + //
                                "  a64 := u5\r\n" + //
                                "  u6 := 31\r\n" + //
                                "  u7 := a75 ILSHIFT u6\r\n" + //
                                "  u8 := a64 IOR u7\r\n" + //
                                "  a64 := u8\r\n" + //
                                "  u9 := 8388607\r\n" + //
                                "  v0 := a74 IAND u9\r\n" + //
                                "  v1 := a64 IOR v0\r\n" + //
                                "  a64 := v1\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0_0\r\n" + //
                                "  CALL IntBinaryAsReal ( a64 -> a53 )\r\n" + //
                                "  v2 <| a56\r\n" + //
                                "  a62 := v2\r\n" + //
                                "  a63 |< a62\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  a78 <- a77\r\n" + //
                                "  a79 := 0\r\n" + //
                                "  a81 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a78 -> a48 )\r\n" + //
                                "  a82 <| a51\r\n" + //
                                "  a81 := a82\r\n" + //
                                "  a83 := 31\r\n" + //
                                "  a84 := a81 IRSHIFT a83\r\n" + //
                                "  a85 := 1\r\n" + //
                                "  a86 := a84 IAND a85\r\n" + //
                                "  a79 := a86\r\n" + //
                                "  a80 |< a79\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  a90 <- a89\r\n" + //
                                "  a91 := 0\r\n" + //
                                "  a93 := 0\r\n" + //
                                "  a94 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( a90 -> a48 )\r\n" + //
                                "  a95 <| a51\r\n" + //
                                "  a93 := a95\r\n" + //
                                "  a96 := 23\r\n" + //
                                "  a97 := a93 IRSHIFT a96\r\n" + //
                                "  a98 := 255\r\n" + //
                                "  a99 := a97 IAND a98\r\n" + //
                                "  a94 := a99\r\n" + //
                                "  b10 := a94 ISUB b12\r\n" + //
                                "  a91 := b10\r\n" + //
                                "  a92 |< a91\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  b16 <- b15\r\n" + //
                                "  b19 := 0\r\n" + //
                                "  b20 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( b16 -> a48 )\r\n" + //
                                "  b21 <| a51\r\n" + //
                                "  b20 := b21\r\n" + //
                                "  b22 := 8388607\r\n" + //
                                "  b23 := b20 IAND b22\r\n" + //
                                "  b19 := b23\r\n" + //
                                "  b24 := 8388608\r\n" + //
                                "  b17 := b19 IOR b24\r\n" + //
                                "  b18 |< b17\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  b98 <- b97\r\n" + //
                                "  b99 := 0\r\n" + //
                                "  c11 := INOT b98\r\n" + //
                                "  c12 := 1\r\n" + //
                                "  c13 := c11 IADD c12\r\n" + //
                                "  b99 := c13\r\n" + //
                                "  c10 |< b99\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  c15 <- c14\r\n" + //
                                "  IPARAM c15\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRealMultiplication(){
        String progSrc = "test_source/RealMultiplication.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                " U9 CONST INTERNAL realBias\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 21.0\r\n" + //
                                " b := a\r\n" + //
                                " c := 2.0\r\n" + //
                                " d := c\r\n" + //
                                " e := 0.0\r\n" + //
                                " U8 := 127\r\n" + //
                                " U9 := U8\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL RMul ( b -> Q6 , d -> Q8 )\r\n" + //
                                " Q5 <| R0\r\n" + //
                                " e := Q5\r\n" + //
                                " CALL WriteReal ( e -> W5 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  Q7 <- Q6\r\n" + //
                                "  k3 <- Q8\r\n" + //
                                "  Q9 := 0.0\r\n" + //
                                "  R1 := 0\r\n" + //
                                "  R2 := 0\r\n" + //
                                "  R3 := 0\r\n" + //
                                "  R4 := 0\r\n" + //
                                "  R5 := 0\r\n" + //
                                "  R6 := 0\r\n" + //
                                "  R7 := 0\r\n" + //
                                "  R8 := 0\r\n" + //
                                "  R9 := 0\r\n" + //
                                "  S0 := 0\r\n" + //
                                "  CALL RealSign ( Q7 -> S1 )\r\n" + //
                                "  k2 <| S4\r\n" + //
                                "  R2 := k2\r\n" + //
                                "  CALL RealSign ( k3 -> S1 )\r\n" + //
                                "  k4 <| S4\r\n" + //
                                "  R3 := k4\r\n" + //
                                "  CALL RealExponent ( Q7 -> T6 )\r\n" + //
                                "  k5 <| T9\r\n" + //
                                "  R4 := k5\r\n" + //
                                "  CALL RealExponent ( k3 -> T6 )\r\n" + //
                                "  k6 <| T9\r\n" + //
                                "  R5 := k6\r\n" + //
                                "  CALL RealMantissa ( Q7 -> V0 )\r\n" + //
                                "  k7 <| V3\r\n" + //
                                "  R6 := k7\r\n" + //
                                "  CALL RealMantissa ( k3 -> V0 )\r\n" + //
                                "  k8 <| V3\r\n" + //
                                "  R7 := k8\r\n" + //
                                "  k9 := R2 NE R3\r\n" + //
                                "  IF k9 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_0_0 ELSE IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_0_0\r\n" + //
                                "  l0 := 1\r\n" + //
                                "  R8 := l0\r\n" + //
                                "  GOTO IFEND_10_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                                "  l1 := 0\r\n" + //
                                "  R8 := l1\r\n" + //
                                "  GOTO IFEND_10_LEVEL_0_0\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_0_0\r\n" + //
                                "  LABEL IFEND_10_LEVEL_0_0\r\n" + //
                                "  l2 := R6 IMUL R7\r\n" + //
                                "  R9 := l2\r\n" + //
                                "  l3 := R4 IADD R5\r\n" + //
                                "  l5 := l3 IADD U9\r\n" + //
                                "  S0 := l5\r\n" + //
                                "  l6 := 23\r\n" + //
                                "  l7 := S0 ILSHIFT l6\r\n" + //
                                "  R1 := l7\r\n" + //
                                "  l8 := 31\r\n" + //
                                "  l9 := R8 ILSHIFT l8\r\n" + //
                                "  m0 := R1 IOR l9\r\n" + //
                                "  R1 := m0\r\n" + //
                                "  m1 := R1 IOR R9\r\n" + //
                                "  R1 := m1\r\n" + //
                                "  CALL IntBinaryAsReal ( R1 -> W0 )\r\n" + //
                                "  m2 <| W3\r\n" + //
                                "  Q9 := m2\r\n" + //
                                "  R0 |< Q9\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealSign\r\n" + //
                                "  S2 <- S1\r\n" + //
                                "  S3 := 0\r\n" + //
                                "  S5 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( S2 -> S7 )\r\n" + //
                                "  S6 <| T0\r\n" + //
                                "  S5 := S6\r\n" + //
                                "  T2 := 31\r\n" + //
                                "  T3 := S5 IRSHIFT T2\r\n" + //
                                "  T4 := 1\r\n" + //
                                "  T5 := T3 IAND T4\r\n" + //
                                "  S3 := T5\r\n" + //
                                "  S4 |< S3\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealBinaryAsInt\r\n" + //
                                "  S8 <- S7\r\n" + //
                                "  S9 := 0\r\n" + //
                                "  T1 := 0.0\r\n" + //
                                "  IPARAM T1\r\n" + //
                                "  IPARAM S8\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM T1\r\n" + //
                                "  IPARAM S9\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  T0 |< S9\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExponent\r\n" + //
                                "  T7 <- T6\r\n" + //
                                "  T8 := 0\r\n" + //
                                "  U0 := 0\r\n" + //
                                "  U1 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( T7 -> S7 )\r\n" + //
                                "  U2 <| T0\r\n" + //
                                "  U0 := U2\r\n" + //
                                "  U3 := 23\r\n" + //
                                "  U4 := U0 IRSHIFT U3\r\n" + //
                                "  U5 := 255\r\n" + //
                                "  U6 := U4 IAND U5\r\n" + //
                                "  U1 := U6\r\n" + //
                                "  U7 := U1 ISUB U9\r\n" + //
                                "  T8 := U7\r\n" + //
                                "  T9 |< T8\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealMantissa\r\n" + //
                                "  V1 <- V0\r\n" + //
                                "  V4 := 0\r\n" + //
                                "  V5 := 0\r\n" + //
                                "  CALL RealBinaryAsInt ( V1 -> S7 )\r\n" + //
                                "  V6 <| T0\r\n" + //
                                "  V5 := V6\r\n" + //
                                "  V7 := 8388607\r\n" + //
                                "  V8 := V5 IAND V7\r\n" + //
                                "  V4 := V8\r\n" + //
                                "  V9 := 8388608\r\n" + //
                                "  V2 := V4 IOR V9\r\n" + //
                                "  V3 |< V2\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntBinaryAsReal\r\n" + //
                                "  W1 <- W0\r\n" + //
                                "  W2 := 0.0\r\n" + //
                                "  W4 := 0\r\n" + //
                                "  IPARAM W4\r\n" + //
                                "  IPARAM W1\r\n" + //
                                "  IASM \"LDR %r, %a\"\r\n" + //
                                "  IPARAM W4\r\n" + //
                                "  IPARAM W2\r\n" + //
                                "  IASM \"STR %r, %a\"\r\n" + //
                                "  W3 |< W2\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  W6 <- W5\r\n" + //
                                "  IPARAM W6\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRealMultiplication2(){
        String progSrc = "test_source/RealMultiplication2.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                        " a60 CONST INTERNAL realBias\r\n" + //
                        "DATA SECTION\r\n" + //
                        " a := 80.56\r\n" + //
                        " b := a\r\n" + //
                        " c := 0.05\r\n" + //
                        " d := c\r\n" + //
                        " e := 0.0\r\n" + //
                        " a59 := 127\r\n" + //
                        " a60 := a59\r\n" + //
                        "CODE SECTION\r\n" + //
                        " CALL RMul ( b -> a17 , d -> a18 )\r\n" + //
                        " a16 <| a20\r\n" + //
                        " e := a16\r\n" + //
                        " CALL WriteReal ( e -> a84 )\r\n" + //
                        "END\r\n" + //
                        "PROC SECTION\r\n" + //
                        " PROC LABEL RMul\r\n" + //
                        "  l5 <- a17\r\n" + //
                        "  l7 <- a18\r\n" + //
                        "  a19 := 0.0\r\n" + //
                        "  a21 := 0\r\n" + //
                        "  a22 := 0\r\n" + //
                        "  a23 := 0\r\n" + //
                        "  a24 := 0\r\n" + //
                        "  a25 := 0\r\n" + //
                        "  a26 := 0\r\n" + //
                        "  a27 := 0\r\n" + //
                        "  a28 := 0\r\n" + //
                        "  a29 := 0\r\n" + //
                        "  a30 := 0\r\n" + //
                        "  a31 := 0\r\n" + //
                        "  k2 := 0\r\n" + //
                        "  k3 := 0\r\n" + //
                        "  k4 := 0\r\n" + //
                        "  k5 := 0\r\n" + //
                        "  k6 := 0\r\n" + //
                        "  k7 := 0\r\n" + //
                        "  k8 := 0\r\n" + //
                        "  k9 := 0\r\n" + //
                        "  l0 := 0\r\n" + //
                        "  l1 := 0\r\n" + //
                        "  l2 := 0\r\n" + //
                        "  l3 := 0\r\n" + //
                        "  l4 := 0\r\n" + //
                        "  CALL RealSign ( l5 -> a32 )\r\n" + //
                        "  l6 <| a35\r\n" + //
                        "  a22 := l6\r\n" + //
                        "  CALL RealSign ( l7 -> a32 )\r\n" + //
                        "  l8 <| a35\r\n" + //
                        "  a23 := l8\r\n" + //
                        "  CALL RealExponent ( l5 -> a47 )\r\n" + //
                        "  l9 <| a50\r\n" + //
                        "  a24 := l9\r\n" + //
                        "  CALL RealExponent ( l7 -> a47 )\r\n" + //
                        "  m0 <| a50\r\n" + //
                        "  a25 := m0\r\n" + //
                        "  CALL RealMantissa ( l5 -> a61 )\r\n" + //
                        "  m1 <| a64\r\n" + //
                        "  a26 := m1\r\n" + //
                        "  CALL RealMantissa ( l7 -> a61 )\r\n" + //
                        "  m2 <| a64\r\n" + //
                        "  a27 := m2\r\n" + //
                        "  m3 := 1\r\n" + //
                        "  m4 := a26 IAND m3\r\n" + //
                        "  m5 := 1\r\n" + //
                        "  m6 := m4 NE m5\r\n" + //
                        "  IF m6 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF m6 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0_0 ELSE WHILEEND_2_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  m7 := 1\r\n" + //
                        "  m8 := a26 IRSHIFT m7\r\n" + //
                        "  a26 := m8\r\n" + //
                        "  m9 := 1\r\n" + //
                        "  n0 := a26 IAND m9\r\n" + //
                        "  n1 := 1\r\n" + //
                        "  n2 := n0 NE n1\r\n" + //
                        "  m6 := n2\r\n" + //
                        "  GOTO WHILECOND_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_2_LEVEL_0_0\r\n" + //
                        "  n3 := 0\r\n" + //
                        "  k7 := n3\r\n" + //
                        "  l1 := a26\r\n" + //
                        "  CALL IntIsZero ( l1 -> a71 )\r\n" + //
                        "  n4 <| a74\r\n" + //
                        "  n5 := BNOT n4\r\n" + //
                        "  IF n5 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF n5 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0_0 ELSE WHILEEND_4_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  n6 := 1\r\n" + //
                        "  n7 := l1 IAND n6\r\n" + //
                        "  l2 := n7\r\n" + //
                        "  n8 := 1\r\n" + //
                        "  n9 := l2 EQ n8\r\n" + //
                        "  IF n9 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_0_0 ELSE IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_10_SEQ_0_LEVEL_0_0\r\n" + //
                        "  k6 := k7\r\n" + //
                        "  GOTO IFEND_10_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_10_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_10_LEVEL_0_0\r\n" + //
                        "  o0 := 1\r\n" + //
                        "  o1 := k7 IADD o0\r\n" + //
                        "  k7 := o1\r\n" + //
                        "  o2 := 1\r\n" + //
                        "  o3 := l1 IRSHIFT o2\r\n" + //
                        "  l1 := o3\r\n" + //
                        "  CALL IntIsZero ( l1 -> a71 )\r\n" + //
                        "  o4 <| a74\r\n" + //
                        "  o5 := BNOT o4\r\n" + //
                        "  n5 := o5\r\n" + //
                        "  GOTO WHILECOND_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_4_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_4_LEVEL_0_0\r\n" + //
                        "  o6 := 1\r\n" + //
                        "  o7 := a27 IAND o6\r\n" + //
                        "  o8 := 1\r\n" + //
                        "  o9 := o7 NE o8\r\n" + //
                        "  IF o9 EQ TRUE THEN WHILESTAT_6_SEQ_0_LEVEL_0 ELSE WHILENEXT_6_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_6_SEQ_0_LEVEL_0\r\n" + //
                        "  IF o9 EQ TRUE THEN WHILESTAT_6_SEQ_0_LEVEL_0 ELSE WHILEEND_6_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_6_SEQ_0_LEVEL_0\r\n" + //
                        "  p0 := 1\r\n" + //
                        "  p1 := a27 IRSHIFT p0\r\n" + //
                        "  a27 := p1\r\n" + //
                        "  p2 := 1\r\n" + //
                        "  p3 := a27 IAND p2\r\n" + //
                        "  p4 := 1\r\n" + //
                        "  p5 := p3 NE p4\r\n" + //
                        "  o9 := p5\r\n" + //
                        "  GOTO WHILECOND_6_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_6_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_6_LEVEL_0\r\n" + //
                        "  p6 := 0\r\n" + //
                        "  k9 := p6\r\n" + //
                        "  l0 := a27\r\n" + //
                        "  CALL IntIsZero ( l0 -> a71 )\r\n" + //
                        "  p7 <| a74\r\n" + //
                        "  p8 := BNOT p7\r\n" + //
                        "  IF p8 EQ TRUE THEN WHILESTAT_8_SEQ_0_LEVEL_0 ELSE WHILENEXT_8_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_8_SEQ_0_LEVEL_0\r\n" + //
                        "  IF p8 EQ TRUE THEN WHILESTAT_8_SEQ_0_LEVEL_0 ELSE WHILEEND_8_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_8_SEQ_0_LEVEL_0\r\n" + //
                        "  p9 := 1\r\n" + //
                        "  q0 := l0 IAND p9\r\n" + //
                        "  l3 := q0\r\n" + //
                        "  q1 := 1\r\n" + //
                        "  q2 := l3 EQ q1\r\n" + //
                        "  IF q2 EQ TRUE THEN IFSTAT_11_SEQ_0_LEVEL_0 ELSE IFNEXT_11_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_11_SEQ_0_LEVEL_0\r\n" + //
                        "  k8 := k9\r\n" + //
                        "  GOTO IFEND_11_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_11_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_11_LEVEL_0\r\n" + //
                        "  q3 := 1\r\n" + //
                        "  q4 := k9 IADD q3\r\n" + //
                        "  k9 := q4\r\n" + //
                        "  q5 := 1\r\n" + //
                        "  q6 := l0 IRSHIFT q5\r\n" + //
                        "  l0 := q6\r\n" + //
                        "  CALL IntIsZero ( l0 -> a71 )\r\n" + //
                        "  q7 <| a74\r\n" + //
                        "  q8 := BNOT q7\r\n" + //
                        "  p8 := q8\r\n" + //
                        "  GOTO WHILECOND_8_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_8_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_8_LEVEL_0\r\n" + //
                        "  q9 := k6 IADD k8\r\n" + //
                        "  l4 := q9\r\n" + //
                        "  r0 := a22 NE a23\r\n" + //
                        "  IF r0 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_0 ELSE IFNEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  r1 := 1\r\n" + //
                        "  a28 := r1\r\n" + //
                        "  GOTO IFEND_12_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  r2 := 0\r\n" + //
                        "  a28 := r2\r\n" + //
                        "  GOTO IFEND_12_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_12_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_12_LEVEL_0\r\n" + //
                        "  r3 := a24 IADD a25\r\n" + //
                        "  a30 := r3\r\n" + //
                        "  r4 := a26 IMUL a27\r\n" + //
                        "  a29 := r4\r\n" + //
                        "  r5 := 23\r\n" + //
                        "  r6 := l4 LT r5\r\n" + //
                        "  IF r6 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_0 ELSE IFNEXT_13_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_13_SEQ_0_LEVEL_0\r\n" + //
                        "  r7 := 23\r\n" + //
                        "  r8 := r7 ISUB l4\r\n" + //
                        "  k5 := r8\r\n" + //
                        "  r9 := a29 ILSHIFT k5\r\n" + //
                        "  a29 := r9\r\n" + //
                        "  GOTO IFEND_13_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_13_SEQ_0_LEVEL_0\r\n" + //
                        "  s0 := 23\r\n" + //
                        "  s1 := l4 GT s0\r\n" + //
                        "  IF s1 EQ TRUE THEN IFSTAT_13_SEQ_1_LEVEL_0 ELSE IFNEXT_13_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_13_SEQ_1_LEVEL_0\r\n" + //
                        "  s2 := 23\r\n" + //
                        "  s3 := l4 ISUB s2\r\n" + //
                        "  k5 := s3\r\n" + //
                        "  s4 := a29 IRSHIFT k5\r\n" + //
                        "  a29 := s4\r\n" + //
                        "  GOTO IFEND_13_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_13_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_13_LEVEL_0\r\n" + //
                        "  s5 := 0\r\n" + //
                        "  a31 := s5\r\n" + //
                        "  k3 := a29\r\n" + //
                        "  CALL IntIsZero ( k3 -> a71 )\r\n" + //
                        "  s6 <| a74\r\n" + //
                        "  s7 := BNOT s6\r\n" + //
                        "  IF s7 EQ TRUE THEN WHILESTAT_10_SEQ_0_LEVEL_0 ELSE WHILENEXT_10_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_10_SEQ_0_LEVEL_0\r\n" + //
                        "  IF s7 EQ TRUE THEN WHILESTAT_10_SEQ_0_LEVEL_0 ELSE WHILEEND_10_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_10_SEQ_0_LEVEL_0\r\n" + //
                        "  s8 := 1\r\n" + //
                        "  s9 := k3 IAND s8\r\n" + //
                        "  k4 := s9\r\n" + //
                        "  t0 := 1\r\n" + //
                        "  t1 := k4 EQ t0\r\n" + //
                        "  IF t1 EQ TRUE THEN IFSTAT_14_SEQ_0_LEVEL_0 ELSE IFNEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  k2 := a31\r\n" + //
                        "  GOTO IFEND_14_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_14_LEVEL_0\r\n" + //
                        "  t2 := 1\r\n" + //
                        "  t3 := a31 IADD t2\r\n" + //
                        "  a31 := t3\r\n" + //
                        "  t4 := 1\r\n" + //
                        "  t5 := k3 IRSHIFT t4\r\n" + //
                        "  k3 := t5\r\n" + //
                        "  CALL IntIsZero ( k3 -> a71 )\r\n" + //
                        "  t6 <| a74\r\n" + //
                        "  t7 := BNOT t6\r\n" + //
                        "  s7 := t7\r\n" + //
                        "  GOTO WHILECOND_10_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_10_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_10_LEVEL_0\r\n" + //
                        "  t8 := 23\r\n" + //
                        "  t9 := k2 GT t8\r\n" + //
                        "  IF t9 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_0 ELSE IFNEXT_15_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_15_SEQ_0_LEVEL_0\r\n" + //
                        "  u0 := 23\r\n" + //
                        "  u1 := k2 ISUB u0\r\n" + //
                        "  k5 := u1\r\n" + //
                        "  u2 := a29 IRSHIFT k5\r\n" + //
                        "  a29 := u2\r\n" + //
                        "  u3 := a30 IADD k5\r\n" + //
                        "  a30 := u3\r\n" + //
                        "  GOTO IFEND_15_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_15_SEQ_0_LEVEL_0\r\n" + //
                        "  u4 := 23\r\n" + //
                        "  u5 := k2 LT u4\r\n" + //
                        "  IF u5 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_0 ELSE IFNEXT_15_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_15_SEQ_1_LEVEL_0\r\n" + //
                        "  u6 := 23\r\n" + //
                        "  u7 := u6 ISUB k2\r\n" + //
                        "  k5 := u7\r\n" + //
                        "  u8 := a29 ILSHIFT k5\r\n" + //
                        "  a29 := u8\r\n" + //
                        "  u9 := a30 ISUB k5\r\n" + //
                        "  a30 := u9\r\n" + //
                        "  GOTO IFEND_15_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_15_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_15_LEVEL_0\r\n" + //
                        "  v1 := a30 IADD a60\r\n" + //
                        "  a30 := v1\r\n" + //
                        "  v2 := 255\r\n" + //
                        "  v3 := a30 IAND v2\r\n" + //
                        "  v4 := 23\r\n" + //
                        "  v5 := v3 ILSHIFT v4\r\n" + //
                        "  a21 := v5\r\n" + //
                        "  v6 := 1\r\n" + //
                        "  v7 := a28 IAND v6\r\n" + //
                        "  v8 := 31\r\n" + //
                        "  v9 := v7 ILSHIFT v8\r\n" + //
                        "  w0 := a21 IOR v9\r\n" + //
                        "  a21 := w0\r\n" + //
                        "  w1 := 8388607\r\n" + //
                        "  w2 := a29 IAND w1\r\n" + //
                        "  w3 := a21 IOR w2\r\n" + //
                        "  a21 := w3\r\n" + //
                        "  CALL IntBinaryAsReal ( a21 -> a79 )\r\n" + //
                        "  w4 <| a82\r\n" + //
                        "  a19 := w4\r\n" + //
                        "  a20 |< a19\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealSign\r\n" + //
                        "  a33 <- a32\r\n" + //
                        "  a34 := 0\r\n" + //
                        "  a36 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( a33 -> a38 )\r\n" + //
                        "  a37 <| a41\r\n" + //
                        "  a36 := a37\r\n" + //
                        "  a43 := 31\r\n" + //
                        "  a44 := a36 IRSHIFT a43\r\n" + //
                        "  a45 := 1\r\n" + //
                        "  a46 := a44 IAND a45\r\n" + //
                        "  a34 := a46\r\n" + //
                        "  a35 |< a34\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealBinaryAsInt\r\n" + //
                        "  a39 <- a38\r\n" + //
                        "  a40 := 0\r\n" + //
                        "  a42 := 0.0\r\n" + //
                        "  IPARAM a42\r\n" + //
                        "  IPARAM a39\r\n" + //
                        "  IASM \"LDR %r, %a\"\r\n" + //
                        "  IPARAM a42\r\n" + //
                        "  IPARAM a40\r\n" + //
                        "  IASM \"STR %r, %a\"\r\n" + //
                        "  a41 |< a40\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealExponent\r\n" + //
                        "  a48 <- a47\r\n" + //
                        "  a49 := 0\r\n" + //
                        "  a51 := 0\r\n" + //
                        "  a52 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( a48 -> a38 )\r\n" + //
                        "  a53 <| a41\r\n" + //
                        "  a51 := a53\r\n" + //
                        "  a54 := 23\r\n" + //
                        "  a55 := a51 IRSHIFT a54\r\n" + //
                        "  a56 := 255\r\n" + //
                        "  a57 := a55 IAND a56\r\n" + //
                        "  a52 := a57\r\n" + //
                        "  a58 := a52 ISUB a60\r\n" + //
                        "  a49 := a58\r\n" + //
                        "  a50 |< a49\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealMantissa\r\n" + //
                        "  a62 <- a61\r\n" + //
                        "  a65 := 0\r\n" + //
                        "  a66 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( a62 -> a38 )\r\n" + //
                        "  a67 <| a41\r\n" + //
                        "  a66 := a67\r\n" + //
                        "  a68 := 8388607\r\n" + //
                        "  a69 := a66 IAND a68\r\n" + //
                        "  a65 := a69\r\n" + //
                        "  a70 := 8388608\r\n" + //
                        "  a63 := a65 IOR a70\r\n" + //
                        "  a64 |< a63\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntIsZero\r\n" + //
                        "  a72 <- a71\r\n" + //
                        "  a73 := FALSE\r\n" + //
                        "  a75 := 0\r\n" + //
                        "  a76 := a72 EQ a75\r\n" + //
                        "  IF a76 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  a77 := TRUE\r\n" + //
                        "  a73 := a77\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  a78 := FALSE\r\n" + //
                        "  a73 := a78\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                        "  a74 |< a73\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntBinaryAsReal\r\n" + //
                        "  a80 <- a79\r\n" + //
                        "  a81 := 0.0\r\n" + //
                        "  a83 := 0\r\n" + //
                        "  IPARAM a83\r\n" + //
                        "  IPARAM a80\r\n" + //
                        "  IASM \"LDR %r, %a\"\r\n" + //
                        "  IPARAM a83\r\n" + //
                        "  IPARAM a81\r\n" + //
                        "  IASM \"STR %r, %a\"\r\n" + //
                        "  a82 |< a81\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL WriteReal\r\n" + //
                        "  a85 <- a84\r\n" + //
                        "  IPARAM a85\r\n" + //
                        "  IASM \"LDR R0, %a\"\r\n" + //
                        "  IASM \"SWI 2\"\r\n" + //
                        " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testIntegerDiv(){
        String progSrc = "test_source/IntegerDiv.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                        "DATA SECTION\r\n" + //
                        " a := 20\r\n" + //
                        " b := a\r\n" + //
                        " c := 5\r\n" + //
                        " d := c\r\n" + //
                        " e := 0\r\n" + //
                        "CODE SECTION\r\n" + //
                        " CALL Div ( b -> a80 , d -> a82 )\r\n" + //
                        " a79 <| a85\r\n" + //
                        " e := a79\r\n" + //
                        " CALL WriteInt ( e -> a97 )\r\n" + //
                        "END\r\n" + //
                        "PROC SECTION\r\n" + //
                        " PROC LABEL Div\r\n" + //
                        "  a81 <- a80\r\n" + //
                        "  a83 <- a82\r\n" + //
                        "  a86 := 0\r\n" + //
                        "  a84 := 0\r\n" + //
                        "  a86 := a81\r\n" + //
                        "  a87 := 0\r\n" + //
                        "  a84 := a87\r\n" + //
                        "  a88 := a86 ISUB a83\r\n" + //
                        "  a89 := 0\r\n" + //
                        "  a90 := a88 GT a89\r\n" + //
                        "  IF a90 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF a90 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  a91 := a86 ISUB a83\r\n" + //
                        "  a86 := a91\r\n" + //
                        "  a92 := 1\r\n" + //
                        "  a93 := a84 IADD a92\r\n" + //
                        "  a84 := a93\r\n" + //
                        "  a94 := a86 ISUB a83\r\n" + //
                        "  a95 := 0\r\n" + //
                        "  a96 := a94 GT a95\r\n" + //
                        "  a90 := a96\r\n" + //
                        "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                        "  a85 |< a84\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL WriteInt\r\n" + //
                        "  a98 <- a97\r\n" + //
                        "  IPARAM a98\r\n" + //
                        "  IASM \"LDR R0, %a\"\r\n" + //
                        "  IASM \"SWI 1\"\r\n" + //
                        " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testIntegerDiv2(){
        String progSrc = "test_source/IntegerDiv2.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                                "DATA SECTION\r\n" + //
                                " a := 30\r\n" + //
                                " b := a\r\n" + //
                                " c := 8\r\n" + //
                                " d := c\r\n" + //
                                " e := 0\r\n" + //
                                "CODE SECTION\r\n" + //
                                " CALL Div ( b -> a80 , d -> a82 )\r\n" + //
                                " a79 <| a85\r\n" + //
                                " e := a79\r\n" + //
                                " CALL WriteInt ( e -> a97 )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  a81 <- a80\r\n" + //
                                "  a83 <- a82\r\n" + //
                                "  a86 := 0\r\n" + //
                                "  a84 := 0\r\n" + //
                                "  a86 := a81\r\n" + //
                                "  a87 := 0\r\n" + //
                                "  a84 := a87\r\n" + //
                                "  a88 := a86 ISUB a83\r\n" + //
                                "  a89 := 0\r\n" + //
                                "  a90 := a88 GE a89\r\n" + //
                                "  IF a90 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  IF a90 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  a91 := a86 ISUB a83\r\n" + //
                                "  a86 := a91\r\n" + //
                                "  a92 := 1\r\n" + //
                                "  a93 := a84 IADD a92\r\n" + //
                                "  a84 := a93\r\n" + //
                                "  a94 := a86 ISUB a83\r\n" + //
                                "  a95 := 0\r\n" + //
                                "  a96 := a94 GE a95\r\n" + //
                                "  a90 := a96\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                                "  a85 |< a84\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  a98 <- a97\r\n" + //
                                "  IPARAM a98\r\n" + //
                                "  IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }

    @Test
    public void testRealDivision(){
        String progSrc = "test_source/RealDivision.dcl";
        String expectedResult = "SYMBOL SECTION\r\n" + //
                        " b21 CONST INTERNAL realBias\r\n" + //
                        "DATA SECTION\r\n" + //
                        " a := 30.0\r\n" + //
                        " b := a\r\n" + //
                        " c := 2.0\r\n" + //
                        " d := c\r\n" + //
                        " e := 0.0\r\n" + //
                        " b20 := 127\r\n" + //
                        " b21 := b20\r\n" + //
                        "CODE SECTION\r\n" + //
                        " CALL RDivide ( b -> a80 , d -> a81 )\r\n" + //
                        " a79 <| a82\r\n" + //
                        " e := a79\r\n" + //
                        " CALL WriteReal ( e -> b62 )\r\n" + //
                        "END\r\n" + //
                        "PROC SECTION\r\n" + //
                        " PROC LABEL RDivide\r\n" + //
                        "  y5 <- a80\r\n" + //
                        "  y7 <- a81\r\n" + //
                        "  w5 := 0.0\r\n" + //
                        "  w6 := 0\r\n" + //
                        "  w7 := 0\r\n" + //
                        "  w8 := 0\r\n" + //
                        "  w9 := 0\r\n" + //
                        "  x0 := 0\r\n" + //
                        "  x1 := 0\r\n" + //
                        "  x2 := 0\r\n" + //
                        "  x3 := 0\r\n" + //
                        "  x4 := 0\r\n" + //
                        "  x5 := 0\r\n" + //
                        "  x6 := 0\r\n" + //
                        "  x7 := 0\r\n" + //
                        "  x8 := 0\r\n" + //
                        "  x9 := 0\r\n" + //
                        "  y0 := 0\r\n" + //
                        "  y1 := 0\r\n" + //
                        "  y2 := 0\r\n" + //
                        "  y3 := 0\r\n" + //
                        "  y4 := 0\r\n" + //
                        "  CALL RealSign ( y5 -> a83 )\r\n" + //
                        "  y6 <| a86\r\n" + //
                        "  w7 := y6\r\n" + //
                        "  CALL RealSign ( y7 -> a83 )\r\n" + //
                        "  y8 <| a86\r\n" + //
                        "  w8 := y8\r\n" + //
                        "  CALL RealExponent ( y5 -> a98 )\r\n" + //
                        "  y9 <| b11\r\n" + //
                        "  w9 := y9\r\n" + //
                        "  CALL RealExponent ( y7 -> a98 )\r\n" + //
                        "  z0 <| b11\r\n" + //
                        "  x0 := z0\r\n" + //
                        "  CALL RealMantissa ( y5 -> b22 )\r\n" + //
                        "  z1 <| b25\r\n" + //
                        "  x1 := z1\r\n" + //
                        "  CALL RealMantissa ( y7 -> b22 )\r\n" + //
                        "  z2 <| b25\r\n" + //
                        "  x2 := z2\r\n" + //
                        "  z3 := 7\r\n" + //
                        "  z4 := x1 ILSHIFT z3\r\n" + //
                        "  x1 := z4\r\n" + //
                        "  z5 := 7\r\n" + //
                        "  z6 := x2 ILSHIFT z5\r\n" + //
                        "  x2 := z6\r\n" + //
                        "  z7 := 0\r\n" + //
                        "  x7 := z7\r\n" + //
                        "  x8 := x2\r\n" + //
                        "  z8 := 1\r\n" + //
                        "  z9 := x8 IAND z8\r\n" + //
                        "  A0 := 0\r\n" + //
                        "  A1 := z9 EQ A0\r\n" + //
                        "  IF A1 EQ TRUE THEN WHILESTAT_12_SEQ_0_LEVEL_0 ELSE WHILENEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_12_SEQ_0_LEVEL_0\r\n" + //
                        "  IF A1 EQ TRUE THEN WHILESTAT_12_SEQ_0_LEVEL_0 ELSE WHILEEND_12_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  A2 := 1\r\n" + //
                        "  A3 := x7 IADD A2\r\n" + //
                        "  x7 := A3\r\n" + //
                        "  A4 := 1\r\n" + //
                        "  A5 := x8 IRSHIFT A4\r\n" + //
                        "  x8 := A5\r\n" + //
                        "  A6 := 1\r\n" + //
                        "  A7 := x8 IAND A6\r\n" + //
                        "  A8 := 0\r\n" + //
                        "  A9 := A7 EQ A8\r\n" + //
                        "  A1 := A9\r\n" + //
                        "  GOTO WHILECOND_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_12_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_12_LEVEL_0\r\n" + //
                        "  B0 := 30\r\n" + //
                        "  B1 := B0 ISUB x7\r\n" + //
                        "  x9 := B1\r\n" + //
                        "  B2 := x0 ISUB x9\r\n" + //
                        "  x0 := B2\r\n" + //
                        "  B3 := w9 ISUB x9\r\n" + //
                        "  w9 := B3\r\n" + //
                        "  B4 := w7 NE w8\r\n" + //
                        "  IF B4 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_0 ELSE IFNEXT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  B5 := 1\r\n" + //
                        "  x3 := B5\r\n" + //
                        "  GOTO IFEND_16_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_16_SEQ_0_LEVEL_0\r\n" + //
                        "  B6 := 0\r\n" + //
                        "  x3 := B6\r\n" + //
                        "  GOTO IFEND_16_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_16_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_16_LEVEL_0\r\n" + //
                        "  CALL Div ( x1 -> b32 , x2 -> b34 )\r\n" + //
                        "  B7 <| b37\r\n" + //
                        "  x4 := B7\r\n" + //
                        "  B8 := 23\r\n" + //
                        "  B9 := x7 LT B8\r\n" + //
                        "  IF B9 EQ TRUE THEN IFSTAT_17_SEQ_0_LEVEL_0 ELSE IFNEXT_17_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_17_SEQ_0_LEVEL_0\r\n" + //
                        "  C0 := 23\r\n" + //
                        "  C1 := C0 ISUB x7\r\n" + //
                        "  C2 := x4 ILSHIFT C1\r\n" + //
                        "  x4 := C2\r\n" + //
                        "  GOTO IFEND_17_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_17_SEQ_0_LEVEL_0\r\n" + //
                        "  C3 := 23\r\n" + //
                        "  C4 := x7 GT C3\r\n" + //
                        "  IF C4 EQ TRUE THEN IFSTAT_17_SEQ_1_LEVEL_0 ELSE IFNEXT_17_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_17_SEQ_1_LEVEL_0\r\n" + //
                        "  C5 := 23\r\n" + //
                        "  C6 := x7 ISUB C5\r\n" + //
                        "  C7 := x4 IRSHIFT C6\r\n" + //
                        "  x4 := C7\r\n" + //
                        "  GOTO IFEND_17_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_17_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_17_LEVEL_0\r\n" + //
                        "  C8 := w9 ISUB x0\r\n" + //
                        "  x5 := C8\r\n" + //
                        "  C9 := 0\r\n" + //
                        "  y0 := C9\r\n" + //
                        "  y1 := x4\r\n" + //
                        "  CALL IntIsZero ( y1 -> b49 )\r\n" + //
                        "  D0 <| b52\r\n" + //
                        "  D1 := BNOT D0\r\n" + //
                        "  IF D1 EQ TRUE THEN WHILESTAT_14_SEQ_0_LEVEL_0 ELSE WHILENEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILECOND_14_SEQ_0_LEVEL_0\r\n" + //
                        "  IF D1 EQ TRUE THEN WHILESTAT_14_SEQ_0_LEVEL_0 ELSE WHILEEND_14_LEVEL_0\r\n" + //
                        "  LABEL WHILESTAT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  D2 := 1\r\n" + //
                        "  D3 := y1 IAND D2\r\n" + //
                        "  y3 := D3\r\n" + //
                        "  D4 := 1\r\n" + //
                        "  D5 := y3 EQ D4\r\n" + //
                        "  IF D5 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_0 ELSE IFNEXT_18_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_18_SEQ_0_LEVEL_0\r\n" + //
                        "  y2 := y0\r\n" + //
                        "  GOTO IFEND_18_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_18_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFEND_18_LEVEL_0\r\n" + //
                        "  D6 := 1\r\n" + //
                        "  D7 := y0 IADD D6\r\n" + //
                        "  y0 := D7\r\n" + //
                        "  D8 := 1\r\n" + //
                        "  D9 := y1 IRSHIFT D8\r\n" + //
                        "  y1 := D9\r\n" + //
                        "  CALL IntIsZero ( y1 -> b49 )\r\n" + //
                        "  E0 <| b52\r\n" + //
                        "  E1 := BNOT E0\r\n" + //
                        "  D1 := E1\r\n" + //
                        "  GOTO WHILECOND_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILENEXT_14_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL WHILEEND_14_LEVEL_0\r\n" + //
                        "  E2 := 23\r\n" + //
                        "  E3 := y2 GT E2\r\n" + //
                        "  IF E3 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_0 ELSE IFNEXT_19_SEQ_0_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_19_SEQ_0_LEVEL_0\r\n" + //
                        "  E4 := 23\r\n" + //
                        "  E5 := y2 ISUB E4\r\n" + //
                        "  y4 := E5\r\n" + //
                        "  E6 := x4 IRSHIFT y4\r\n" + //
                        "  x4 := E6\r\n" + //
                        "  E7 := x5 IADD y4\r\n" + //
                        "  x5 := E7\r\n" + //
                        "  GOTO IFEND_19_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_19_SEQ_0_LEVEL_0\r\n" + //
                        "  E8 := 23\r\n" + //
                        "  E9 := y2 LT E8\r\n" + //
                        "  IF E9 EQ TRUE THEN IFSTAT_19_SEQ_1_LEVEL_0 ELSE IFNEXT_19_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFSTAT_19_SEQ_1_LEVEL_0\r\n" + //
                        "  F0 := 23\r\n" + //
                        "  F1 := F0 ISUB y2\r\n" + //
                        "  y4 := F1\r\n" + //
                        "  F2 := x4 ILSHIFT y4\r\n" + //
                        "  x4 := F2\r\n" + //
                        "  F3 := x5 ISUB y4\r\n" + //
                        "  x5 := F3\r\n" + //
                        "  GOTO IFEND_19_LEVEL_0\r\n" + //
                        "  LABEL IFNEXT_19_SEQ_1_LEVEL_0\r\n" + //
                        "  LABEL IFEND_19_LEVEL_0\r\n" + //
                        "  F4 := 255\r\n" + //
                        "  F5 := x5 IAND F4\r\n" + //
                        "  F6 := 23\r\n" + //
                        "  F7 := F5 ILSHIFT F6\r\n" + //
                        "  w6 := F7\r\n" + //
                        "  F8 := 31\r\n" + //
                        "  F9 := x3 ILSHIFT F8\r\n" + //
                        "  G0 := w6 IOR F9\r\n" + //
                        "  w6 := G0\r\n" + //
                        "  G1 := w6 IOR x4\r\n" + //
                        "  w6 := G1\r\n" + //
                        "  CALL IntBinaryAsReal ( w6 -> b57 )\r\n" + //
                        "  G2 <| b60\r\n" + //
                        "  w5 := G2\r\n" + //
                        "  a82 |< w5\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealSign\r\n" + //
                        "  a84 <- a83\r\n" + //
                        "  a85 := 0\r\n" + //
                        "  a87 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( a84 -> a89 )\r\n" + //
                        "  a88 <| a92\r\n" + //
                        "  a87 := a88\r\n" + //
                        "  a94 := 31\r\n" + //
                        "  a95 := a87 IRSHIFT a94\r\n" + //
                        "  a96 := 1\r\n" + //
                        "  a97 := a95 IAND a96\r\n" + //
                        "  a85 := a97\r\n" + //
                        "  a86 |< a85\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealBinaryAsInt\r\n" + //
                        "  a90 <- a89\r\n" + //
                        "  a91 := 0\r\n" + //
                        "  a93 := 0.0\r\n" + //
                        "  IPARAM a93\r\n" + //
                        "  IPARAM a90\r\n" + //
                        "  IASM \"LDR %r, %a\"\r\n" + //
                        "  IPARAM a93\r\n" + //
                        "  IPARAM a91\r\n" + //
                        "  IASM \"STR %r, %a\"\r\n" + //
                        "  a92 |< a91\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealExponent\r\n" + //
                        "  a99 <- a98\r\n" + //
                        "  b10 := 0\r\n" + //
                        "  b12 := 0\r\n" + //
                        "  b13 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( a99 -> a89 )\r\n" + //
                        "  b14 <| a92\r\n" + //
                        "  b12 := b14\r\n" + //
                        "  b15 := 23\r\n" + //
                        "  b16 := b12 IRSHIFT b15\r\n" + //
                        "  b17 := 255\r\n" + //
                        "  b18 := b16 IAND b17\r\n" + //
                        "  b13 := b18\r\n" + //
                        "  b19 := b13 ISUB b21\r\n" + //
                        "  b10 := b19\r\n" + //
                        "  b11 |< b10\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL RealMantissa\r\n" + //
                        "  b23 <- b22\r\n" + //
                        "  b26 := 0\r\n" + //
                        "  b27 := 0\r\n" + //
                        "  CALL RealBinaryAsInt ( b23 -> a89 )\r\n" + //
                        "  b28 <| a92\r\n" + //
                        "  b27 := b28\r\n" + //
                        "  b29 := 8388607\r\n" + //
                        "  b30 := b27 IAND b29\r\n" + //
                        "  b26 := b30\r\n" + //
                        "  b31 := 8388608\r\n" + //
                        "  b24 := b26 IOR b31\r\n" + //
                        "  b25 |< b24\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL Div\r\n" + //
                        "  b33 <- b32\r\n" + //
                        "  b35 <- b34\r\n" + //
                        "  b38 := 0\r\n" + //
                        "  b36 := 0\r\n" + //
                        "  b38 := b33\r\n" + //
                        "  b39 := 0\r\n" + //
                        "  b36 := b39\r\n" + //
                        "  b40 := b38 ISUB b35\r\n" + //
                        "  b41 := 0\r\n" + //
                        "  b42 := b40 GE b41\r\n" + //
                        "  IF b42 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  IF b42 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0_0 ELSE WHILEEND_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b43 := b38 ISUB b35\r\n" + //
                        "  b38 := b43\r\n" + //
                        "  b44 := 1\r\n" + //
                        "  b45 := b36 IADD b44\r\n" + //
                        "  b36 := b45\r\n" + //
                        "  b46 := b38 ISUB b35\r\n" + //
                        "  b47 := 0\r\n" + //
                        "  b48 := b46 GE b47\r\n" + //
                        "  b42 := b48\r\n" + //
                        "  GOTO WHILECOND_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL WHILEEND_0_LEVEL_0_0\r\n" + //
                        "  b37 |< b36\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntIsZero\r\n" + //
                        "  b50 <- b49\r\n" + //
                        "  b51 := FALSE\r\n" + //
                        "  b53 := 0\r\n" + //
                        "  b54 := b50 EQ b53\r\n" + //
                        "  IF b54 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_0_0 ELSE IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  LABEL IFSTAT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b55 := TRUE\r\n" + //
                        "  b51 := b55\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_0_LEVEL_0_0\r\n" + //
                        "  b56 := FALSE\r\n" + //
                        "  b51 := b56\r\n" + //
                        "  GOTO IFEND_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFNEXT_1_SEQ_1_LEVEL_0_0\r\n" + //
                        "  LABEL IFEND_1_LEVEL_0_0\r\n" + //
                        "  b52 |< b51\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL IntBinaryAsReal\r\n" + //
                        "  b58 <- b57\r\n" + //
                        "  b59 := 0.0\r\n" + //
                        "  b61 := 0\r\n" + //
                        "  IPARAM b61\r\n" + //
                        "  IPARAM b58\r\n" + //
                        "  IASM \"LDR %r, %a\"\r\n" + //
                        "  IPARAM b61\r\n" + //
                        "  IPARAM b59\r\n" + //
                        "  IASM \"STR %r, %a\"\r\n" + //
                        "  b60 |< b59\r\n" + //
                        " RETURN\r\n" + //
                        " PROC LABEL WriteReal\r\n" + //
                        "  b63 <- b62\r\n" + //
                        "  IPARAM b63\r\n" + //
                        "  IASM \"LDR R0, %a\"\r\n" + //
                        "  IASM \"SWI 2\"\r\n" + //
                        " RETURN\r\n";

        linkTestProgram(expectedResult, progSrc);
    }
}
