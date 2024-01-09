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
                     " e <| m\r\n" + //
                     " g := e IADD n\r\n" + //
                     "END\r\n" + //
                     "PROC SECTION\r\n" + //
                     " PROC LABEL func1\r\n" + //
                     "  h := 3\r\n" + //
                     "  i |< h\r\n" + //
                     " RETURN\r\n" + //
                     " PROC LABEL func2\r\n" + //
                     "  CALL func1 (  )\r\n" + //
                     "  f <| d\r\n" + //
                     "  l := f ISUB n\r\n" + //
                     "  m |< l\r\n" + //
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
                                " CALL WriteInt ( b -> a57 )\r\n" + //
                                " CALL WriteReal ( c -> a59 )\r\n" + //
                                " CALL WriteReal ( d -> a59 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Divide ( b -> a62 , a -> a64 )\r\n" + //
                                " a61 <| a67\r\n" + //
                                " CALL WriteReal ( a61 -> a59 )\r\n" + //
                                " m := 5\r\n" + //
                                " n := b IADD m\r\n" + //
                                " o := 6\r\n" + //
                                " p := b IADD o\r\n" + //
                                " q := n IMUL p\r\n" + //
                                " CALL WriteInt ( q -> a57 )\r\n" + //
                                " r := 4\r\n" + //
                                " s := a IADD r\r\n" + //
                                " t := 5.0\r\n" + //
                                " CALL IntToReal ( a -> a73 )\r\n" + //
                                " g51 <| a76\r\n" + //
                                " CALL RAdd ( g51 -> c96 , t -> c98 )\r\n" + //
                                " g52 <| d11\r\n" + //
                                " CALL IntToReal ( s -> a73 )\r\n" + //
                                " g53 <| a76\r\n" + //
                                " CALL RMul ( g53 -> b35 , g52 -> b37 )\r\n" + //
                                " g54 <| b40\r\n" + //
                                " CALL WriteReal ( g54 -> a59 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " y := 3.1415\r\n" + //
                                " CALL p ( b -> e , y -> f )\r\n" + //
                                " z <| g\r\n" + //
                                " a := z\r\n" + //
                                " CALL WriteReal ( d -> a59 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  a58 <- a57\r\n" + //
                                "  IPARAM a58\r\n" + //
                                "IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  a60 <- a59\r\n" + //
                                "  IPARAM a60\r\n" + //
                                "IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Divide\r\n" + //
                                "  a63 <- a62\r\n" + //
                                "  a65 <- a64\r\n" + //
                                "  a71 := 0\r\n" + //
                                "  g50 := 0\r\n" + //
                                "  CALL Div ( a63 -> a2 , a65 -> a3 )\r\n" + //
                                "  a70 <| a4\r\n" + //
                                "  a71 := a70\r\n" + //
                                "  CALL IntToReal ( a71 -> a73 )\r\n" + //
                                "  a72 <| a67\r\n" + //
                                "  g50 := a72\r\n" + //
                                "  a67 |< g50\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  a74 <- a73\r\n" + //
                                "  g49 := 0.0\r\n" + //
                                "  f86 := 0.0\r\n" + //
                                "  f89 := 0\r\n" + //
                                "  a96 := 0\r\n" + //
                                "  c94 := 0.0\r\n" + //
                                "  g35 := 0\r\n" + //
                                "  g45 := 0\r\n" + //
                                "  a84 := 1\r\n" + //
                                "  f86 := a84\r\n" + //
                                "  a86 := 1\r\n" + //
                                "  f89 := a86\r\n" + //
                                "  a88 := 23\r\n" + //
                                "  f92 := f89 LE a88\r\n" + //
                                "  IF f92 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  IF f92 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  a90 := 23\r\n" + //
                                "  a91 := a90 ISUB f89\r\n" + //
                                "  a92 := a74 IRSHIFT a91\r\n" + //
                                "  a96 := a92\r\n" + //
                                "  a94 := 1\r\n" + //
                                "  a95 := a96 IAND a94\r\n" + //
                                "  a96 := a95\r\n" + //
                                "  a97 := 2\r\n" + //
                                "  CALL INeg ( a87 -> a99 )\r\n" + //
                                "  a98 <| a76\r\n" + //
                                "  CALL RealExp ( a97 -> b19 , a98 -> b21 )\r\n" + //
                                "  b18 <| a76\r\n" + //
                                "  c93 := a96 IMUL b18\r\n" + //
                                "  c94 := c93\r\n" + //
                                "  CALL RAdd ( a85 -> c96 , c94 -> c98 )\r\n" + //
                                "  c95 <| a76\r\n" + //
                                "  f86 := c95\r\n" + //
                                "  f87 := 1\r\n" + //
                                "  f88 := f89 IADD f87\r\n" + //
                                "  f89 := f88\r\n" + //
                                "  f90 := 23\r\n" + //
                                "  f91 := f89 LE f90\r\n" + //
                                "  f92 := f91\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "  f93 := 23\r\n" + //
                                "  f94 := a74 IRSHIFT f93\r\n" + //
                                "  f95 := 255\r\n" + //
                                "  f96 := f94 IAND f95\r\n" + //
                                "  g35 := f96\r\n" + //
                                "  f98 := 2\r\n" + //
                                "  f99 := 127\r\n" + //
                                "  g10 := g35 ISUB f99\r\n" + //
                                "  CALL IntExp ( f98 -> g12 , g10 -> g14 )\r\n" + //
                                "  g11 <| a76\r\n" + //
                                "  g35 := g11\r\n" + //
                                "  g36 := 31\r\n" + //
                                "  g37 := a74 IRSHIFT g36\r\n" + //
                                "  g38 := 1\r\n" + //
                                "  g39 := g37 IAND g38\r\n" + //
                                "  g45 := g39\r\n" + //
                                "  g41 := 1\r\n" + //
                                "  g42 := g45 EQ g41\r\n" + //
                                "  IF g42 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  g43 := 1\r\n" + //
                                "  CALL INeg ( g43 -> a99 )\r\n" + //
                                "  g44 <| a76\r\n" + //
                                "  g45 := g44\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0\r\n" + //
                                "  g46 := g45 IMUL g35\r\n" + //
                                "  CALL IntToReal ( g46 -> a73 )\r\n" + //
                                "  g47 <| a76\r\n" + //
                                "  CALL RMul ( g47 -> b35 , f86 -> b37 )\r\n" + //
                                "  g48 <| a76\r\n" + //
                                "  g49 := g48\r\n" + //
                                "  a76 |< g49\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL INeg\r\n" + //
                                "  b10 <- a99\r\n" + //
                                "  b17 := 0\r\n" + //
                                "  b14 := INOT b10\r\n" + //
                                "  b15 := 1\r\n" + //
                                "  b16 := b14 IADD b15\r\n" + //
                                "  b17 := b16\r\n" + //
                                "  b12 |[ b17\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RealExp\r\n" + //
                                "  b20 <- b19\r\n" + //
                                "  b22 <- b21\r\n" + //
                                "  c87 := 0.0\r\n" + //
                                "  c90 := 0\r\n" + //
                                "  b27 := 1\r\n" + //
                                "  c87 := b27\r\n" + //
                                "  b29 := 0\r\n" + //
                                "  c90 := b29\r\n" + //
                                "  b31 := 0\r\n" + //
                                "  b32 := b20 GT b31\r\n" + //
                                "  IF b32 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_0 ELSE IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  c16 := c90 LT b20\r\n" + //
                                "  IF c16 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  IF c16 EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  CALL RMul ( b28 -> b35 , b22 -> b37 )\r\n" + //
                                "  b34 <| b24\r\n" + //
                                "  c87 := b34\r\n" + //
                                "  c12 := 1\r\n" + //
                                "  c13 := c90 IADD c12\r\n" + //
                                "  c90 := c13\r\n" + //
                                "  c15 := c90 LT b20\r\n" + //
                                "  c16 := c15\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  c17 := 0\r\n" + //
                                "  c18 := b20 LT c17\r\n" + //
                                "  IF c18 EQ TRUE THEN IFSTAT_2_SEQ_1_LEVEL_0 ELSE IFNEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_1_LEVEL_0\r\n" + //
                                "  c92 := c90 GT b20\r\n" + //
                                "  IF c92 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0\r\n" + //
                                "  IF c92 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0 ELSE WHILEEND_2_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  CALL RDivide ( c11 -] c21 , b22 -] c23 )\r\n" + //
                                "  c20 <| b24\r\n" + //
                                "  c87 := c20\r\n" + //
                                "  c88 := 1\r\n" + //
                                "  c89 := c90 ISUB c88\r\n" + //
                                "  c90 := c89\r\n" + //
                                "  c91 := c90 GT b20\r\n" + //
                                "  c92 := c91\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0\r\n" + //
                                "  GOTO IFEND_2_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_2_LEVEL_0\r\n" + //
                                "  b24 |[ c87\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RMul\r\n" + //
                                "  b36 <- b35\r\n" + //
                                "  b38 <- b37\r\n" + //
                                "  c10 := 0.0\r\n" + //
                                "  b55 := 0\r\n" + //
                                "  b60 := 0\r\n" + //
                                "  b65 := 0\r\n" + //
                                "  b70 := 0\r\n" + //
                                "  b79 := 0\r\n" + //
                                "  b82 := 0\r\n" + //
                                "  b87 := 0\r\n" + //
                                "  b89 := 0\r\n" + //
                                "  b91 := 0\r\n" + //
                                "  b51 := 31\r\n" + //
                                "  CALL IntToReal ( b51 -> a73 )\r\n" + //
                                "  b52 <| b40\r\n" + //
                                "  b53 := 1\r\n" + //
                                "  b54 := b52 IAND b53\r\n" + //
                                "  b55 := b54\r\n" + //
                                "  b56 := 31\r\n" + //
                                "  CALL IntToReal ( b56 -> a73 )\r\n" + //
                                "  b57 <| b40\r\n" + //
                                "  b58 := 1\r\n" + //
                                "  b59 := b57 IAND b58\r\n" + //
                                "  b60 := b59\r\n" + //
                                "  b61 := 23\r\n" + //
                                "  CALL IntToReal ( b61 -> a73 )\r\n" + //
                                "  b62 <| b40\r\n" + //
                                "  b63 := 255\r\n" + //
                                "  b64 := b62 IAND b63\r\n" + //
                                "  b65 := b64\r\n" + //
                                "  b66 := 23\r\n" + //
                                "  CALL IntToReal ( b66 -> a73 )\r\n" + //
                                "  b67 <| b40\r\n" + //
                                "  b68 := 255\r\n" + //
                                "  b69 := b67 IAND b68\r\n" + //
                                "  b70 := b69\r\n" + //
                                "  b71 := 8388607\r\n" + //
                                "  CALL IntToReal ( b71 -> a73 )\r\n" + //
                                "  b72 <| b40\r\n" + //
                                "  b79 := b72\r\n" + //
                                "  b74 := 8388607\r\n" + //
                                "  CALL IntToReal ( b74 -> a73 )\r\n" + //
                                "  b75 <| b40\r\n" + //
                                "  b82 := b75\r\n" + //
                                "  b77 := 8388608\r\n" + //
                                "  b78 := b79 IOR b77\r\n" + //
                                "  b79 := b78\r\n" + //
                                "  b80 := 8388608\r\n" + //
                                "  b81 := b82 IOR b80\r\n" + //
                                "  b82 := b81\r\n" + //
                                "  b83 := b55 NE b60\r\n" + //
                                "  IF b83 EQ TRUE THEN IFSTAT_26_SEQ_0_LEVEL_0 ELSE IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  b84 := 1\r\n" + //
                                "  b87 := b84\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_0_LEVEL_0\r\n" + //
                                "  b86 := 0\r\n" + //
                                "  b87 := b86\r\n" + //
                                "  GOTO IFEND_26_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_26_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_26_LEVEL_0\r\n" + //
                                "  b88 := b79 IMUL b82\r\n" + //
                                "  b89 := b88\r\n" + //
                                "  b90 := b65 IADD b70\r\n" + //
                                "  b91 := b90\r\n" + //
                                "  b92 := 23\r\n" + //
                                "  b93 := b91 ILSHIFT b92\r\n" + //
                                "  c10 := b93\r\n" + //
                                "  b95 := 31\r\n" + //
                                "  b96 := b87 ILSHIFT b95\r\n" + //
                                "  CALL IntToReal ( b96 -> a73 )\r\n" + //
                                "  b97 <| b40\r\n" + //
                                "  c10 := b97\r\n" + //
                                "  CALL IntToReal ( b89 -> a73 )\r\n" + //
                                "  b99 <| b40\r\n" + //
                                "  c10 := b99\r\n" + //
                                "  b40 |< c10\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RDivide\r\n" + //
                                "  c22 <- c21\r\n" + //
                                "  c24 <- c23\r\n" + //
                                "  c86 := 0.0\r\n" + //
                                "  c41 := 0\r\n" + //
                                "  c46 := 0\r\n" + //
                                "  c51 := 0\r\n" + //
                                "  c56 := 0\r\n" + //
                                "  c65 := 0\r\n" + //
                                "  c68 := 0\r\n" + //
                                "  c73 := 0\r\n" + //
                                "  c75 := 0\r\n" + //
                                "  c77 := 0\r\n" + //
                                "  c37 := 31\r\n" + //
                                "  CALL IntToReal ( c37 -> a73 )\r\n" + //
                                "  c38 <| c26\r\n" + //
                                "  c39 := 1\r\n" + //
                                "  c40 := c38 IAND c39\r\n" + //
                                "  c41 := c40\r\n" + //
                                "  c42 := 31\r\n" + //
                                "  CALL IntToReal ( c42 -> a73 )\r\n" + //
                                "  c43 <| c26\r\n" + //
                                "  c44 := 1\r\n" + //
                                "  c45 := c43 IAND c44\r\n" + //
                                "  c46 := c45\r\n" + //
                                "  c47 := 23\r\n" + //
                                "  CALL IntToReal ( c47 -> a73 )\r\n" + //
                                "  c48 <| c26\r\n" + //
                                "  c49 := 255\r\n" + //
                                "  c50 := c48 IAND c49\r\n" + //
                                "  c51 := c50\r\n" + //
                                "  c52 := 23\r\n" + //
                                "  CALL IntToReal ( c52 -> a73 )\r\n" + //
                                "  c53 <| c26\r\n" + //
                                "  c54 := 255\r\n" + //
                                "  c55 := c53 IAND c54\r\n" + //
                                "  c56 := c55\r\n" + //
                                "  c57 := 8388607\r\n" + //
                                "  CALL IntToReal ( c57 -> a73 )\r\n" + //
                                "  c58 <| c26\r\n" + //
                                "  c65 := c58\r\n" + //
                                "  c60 := 8388607\r\n" + //
                                "  CALL IntToReal ( c60 -> a73 )\r\n" + //
                                "  c61 <| c26\r\n" + //
                                "  c68 := c61\r\n" + //
                                "  c63 := 8388608\r\n" + //
                                "  c64 := c65 IOR c63\r\n" + //
                                "  c65 := c64\r\n" + //
                                "  c66 := 8388608\r\n" + //
                                "  c67 := c68 IOR c66\r\n" + //
                                "  c68 := c67\r\n" + //
                                "  c69 := c41 NE c46\r\n" + //
                                "  IF c69 EQ TRUE THEN IFSTAT_27_SEQ_0_LEVEL_0 ELSE IFNEXT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  c70 := 1\r\n" + //
                                "  c73 := c70\r\n" + //
                                "  GOTO IFEND_27_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_27_SEQ_0_LEVEL_0\r\n" + //
                                "  c72 := 0\r\n" + //
                                "  c73 := c72\r\n" + //
                                "  GOTO IFEND_27_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_27_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_27_LEVEL_0\r\n" + //
                                "  CALL Divide ( c65 -> a62 , c68 -> a64 )\r\n" + //
                                "  c74 <| c26\r\n" + //
                                "  c75 := c74\r\n" + //
                                "  c76 := c51 ISUB c56\r\n" + //
                                "  c77 := c76\r\n" + //
                                "  c78 := 23\r\n" + //
                                "  c79 := c77 ILSHIFT c78\r\n" + //
                                "  c86 := c79\r\n" + //
                                "  c81 := 31\r\n" + //
                                "  c82 := c73 ILSHIFT c81\r\n" + //
                                "  CALL IntToReal ( c82 -> a73 )\r\n" + //
                                "  c83 <| c26\r\n" + //
                                "  c86 := c83\r\n" + //
                                "  CALL IntToReal ( c75 -> a73 )\r\n" + //
                                "  c85 <| c26\r\n" + //
                                "  c86 := c85\r\n" + //
                                "  c26 |< c86\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL RAdd\r\n" + //
                                "  c97 <- c96\r\n" + //
                                "  c99 <- c98\r\n" + //
                                "  f85 := 0.0\r\n" + //
                                "  d28 := 0\r\n" + //
                                "  d33 := 0\r\n" + //
                                "  f50 := 0\r\n" + //
                                "  f51 := 0\r\n" + //
                                "  f14 := 0\r\n" + //
                                "  f54 := 0\r\n" + //
                                "  f17 := 0\r\n" + //
                                "  f73 := 0\r\n" + //
                                "  f68 := 0\r\n" + //
                                "  f76 := 0\r\n" + //
                                "  e74 := 0\r\n" + //
                                "  d24 := 31\r\n" + //
                                "  CALL IntToReal ( d24 -> a73 )\r\n" + //
                                "  d25 <| d11\r\n" + //
                                "  d26 := 1\r\n" + //
                                "\r\n" + //
                                "  d27 := d25 IAND d26\r\n" + //
                                "  d28 := d27\r\n" + //
                                "  d29 := 31\r\n" + //
                                "  CALL IntToReal ( d29 -> a73 )\r\n" + //
                                "  d30 <| d11\r\n" + //
                                "  d31 := 1\r\n" + //
                                "  d32 := d30 IAND d31\r\n" + //
                                "  d33 := d32\r\n" + //
                                "  d34 := 23\r\n" + //
                                "  CALL IntToReal ( d34 -> a73 )\r\n" + //
                                "  d35 <| d11\r\n" + //
                                "  d36 := 255\r\n" + //
                                "  d37 := d35 IAND d36\r\n" + //
                                "  f51 := d37\r\n" + //
                                "  d39 := 23\r\n" + //
                                "  CALL IntToReal ( d39 -> a73 )\r\n" + //
                                "  d40 <| d11\r\n" + //
                                "  d41 := 255\r\n" + //
                                "  d42 := d40 IAND d41\r\n" + //
                                "  f14 := d42\r\n" + //
                                "  d44 := 8388607\r\n" + //
                                "  CALL IntToReal ( d44 -> a73 )\r\n" + //
                                "  d45 <| d11\r\n" + //
                                "  f54 := d45\r\n" + //
                                "  d47 := 8388607\r\n" + //
                                "  CALL IntToReal ( d47 -> a73 )\r\n" + //
                                "  d48 <| d11\r\n" + //
                                "  f17 := d48\r\n" + //
                                "  d50 := 8388608\r\n" + //
                                "  d51 := f54 IOR d50\r\n" + //
                                "  f54 := d51\r\n" + //
                                "  d53 := 8388608\r\n" + //
                                "  d54 := f17 IOR d53\r\n" + //
                                "  f17 := d54\r\n" + //
                                "  d56 := d28 EQ d33\r\n" + //
                                "  IF d56 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  e74 := d28\r\n" + //
                                "  d58 := f51 EQ f14\r\n" + //
                                "  IF d58 EQ TRUE THEN IFSTAT_1_SEQ_0_LEVEL_1 ELSE IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  d59 := f54 IADD f17\r\n" + //
                                "  f76 := d59\r\n" + //
                                "  d61 := 25\r\n" + //
                                "  d62 := f76 IRSHIFT d61\r\n" + //
                                "  d63 := 1\r\n" + //
                                "  d64 := d62 IAND d63\r\n" + //
                                "  f68 := d64\r\n" + //
                                "  f73 := f51\r\n" + //
                                "  d67 := 1\r\n" + //
                                "  d68 := f68 EQ d67\r\n" + //
                                "  IF d68 EQ TRUE THEN IFSTAT_2_SEQ_0_LEVEL_2 ELSE IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  d69 := 1\r\n" + //
                                "  d70 := f73 IADD d69\r\n" + //
                                "  f73 := d70\r\n" + //
                                "  d72 := 1\r\n" + //
                                "  d73 := f76 IRSHIFT d72\r\n" + //
                                "  f76 := d73\r\n" + //
                                "  GOTO IFEND_2_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_2_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_2_LEVEL_2\r\n" + //
                                "  d75 := 23\r\n" + //
                                "  d76 := f73 ILSHIFT d75\r\n" + //
                                "  f85 := d76\r\n" + //
                                "  CALL IntToReal ( d74 -> a73 )\r\n" + //
                                "  d78 <| d11\r\n" + //
                                "  f85 := d78\r\n" + //
                                "  d80 := 31\r\n" + //
                                "  d81 := e74 ILSHIFT d80\r\n" + //
                                "  CALL IntToReal ( d81 -> a73 )\r\n" + //
                                "  d82 <| d11\r\n" + //
                                "  f85 := d82\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_0_LEVEL_1\r\n" + //
                                "  d84 := f51 GT f14\r\n" + //
                                "  IF d84 EQ TRUE THEN IFSTAT_1_SEQ_1_LEVEL_1 ELSE IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  d85 := f51 ISUB f14\r\n" + //
                                "  f50 := d85\r\n" + //
                                "  f14 := f51\r\n" + //
                                "  f73 := f51\r\n" + //
                                "  d89 := f17 IRSHIFT f50\r\n" + //
                                "  f17 := d89\r\n" + //
                                "  d91 := f54 IADD f17\r\n" + //
                                "  f76 := d91\r\n" + //
                                "  d93 := 25\r\n" + //
                                "  d94 := f76 IRSHIFT d93\r\n" + //
                                "  d95 := 1\r\n" + //
                                "  d96 := d94 IAND d95\r\n" + //
                                "  f68 := d96\r\n" + //
                                "  d98 := 1\r\n" + //
                                "  d99 := f68 EQ d98\r\n" + //
                                "  IF d99 EQ TRUE THEN IFSTAT_4_SEQ_0_LEVEL_2 ELSE IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  e10 := 1\r\n" + //
                                "  e11 := f73 IADD e10\r\n" + //
                                "  f73 := e11\r\n" + //
                                "  e13 := 1\r\n" + //
                                "  e14 := f76 IRSHIFT e13\r\n" + //
                                "  f76 := e14\r\n" + //
                                "  GOTO IFEND_4_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_4_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_4_LEVEL_2\r\n" + //
                                "  e16 := 23\r\n" + //
                                "  e17 := f73 ILSHIFT e16\r\n" + //
                                "  f85 := e17\r\n" + //
                                "  e19 := 31\r\n" + //
                                "  e20 := e74 ILSHIFT e19\r\n" + //
                                "  CALL IntToReal ( e20 -> a73 )\r\n" + //
                                "  e21 <| d11\r\n" + //
                                "  f85 := e21\r\n" + //
                                "  CALL IntToReal ( e15 -> a73 )\r\n" + //
                                "  e23 <| d11\r\n" + //
                                "  f85 := e23\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_1_LEVEL_1\r\n" + //
                                "  e25 := f14 ISUB f51\r\n" + //
                                "  f50 := e25\r\n" + //
                                "  f51 := f14\r\n" + //
                                "  f73 := f14\r\n" + //
                                "  e29 := f54 IRSHIFT f50\r\n" + //
                                "  f54 := e29\r\n" + //
                                "  e31 := f54 IADD f17\r\n" + //
                                "  f76 := e31\r\n" + //
                                "  e33 := 25\r\n" + //
                                "  e34 := f76 IRSHIFT e33\r\n" + //
                                "  e35 := 1\r\n" + //
                                "  e36 := e34 IAND e35\r\n" + //
                                "  f68 := e36\r\n" + //
                                "  e38 := 1\r\n" + //
                                "  e39 := f68 EQ e38\r\n" + //
                                "  IF e39 EQ TRUE THEN IFSTAT_6_SEQ_0_LEVEL_2 ELSE IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  e40 := 1\r\n" + //
                                "  e41 := f73 IADD e40\r\n" + //
                                "  f73 := e41\r\n" + //
                                "  e43 := 1\r\n" + //
                                "  e44 := f76 IRSHIFT e43\r\n" + //
                                "  f76 := e44\r\n" + //
                                "  GOTO IFEND_6_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_6_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_6_LEVEL_2\r\n" + //
                                "  e46 := 23\r\n" + //
                                "  e47 := f73 ILSHIFT e46\r\n" + //
                                "  f85 := e47\r\n" + //
                                "  e49 := 31\r\n" + //
                                "  e50 := e74 ILSHIFT e49\r\n" + //
                                "  CALL IntToReal ( e50 -> a73 )\r\n" + //
                                "  e51 <| d11\r\n" + //
                                "  f85 := e51\r\n" + //
                                "  CALL IntToReal ( e45 -> a73 )\r\n" + //
                                "  e53 <| d11\r\n" + //
                                "  f85 := e53\r\n" + //
                                "  GOTO IFEND_1_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_1_SEQ_2_LEVEL_1\r\n" + //
                                "  LABEL IFEND_1_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  e55 := 0\r\n" + //
                                "  e56 := d28 EQ e55\r\n" + //
                                "  e57 := 1\r\n" + //
                                "  e58 := d33 EQ e57\r\n" + //
                                "  e59 := e56 LAND e58\r\n" + //
                                "  IF e59 EQ TRUE THEN IFSTAT_9_SEQ_0_LEVEL_1 ELSE IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  LABEL IFSTAT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  e60 := f17 GT f54\r\n" + //
                                "  IF e60 EQ TRUE THEN IFSTAT_10_SEQ_0_LEVEL_2 ELSE IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  e61 := 1\r\n" + //
                                "  e74 := e61\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_0_LEVEL_2\r\n" + //
                                "  e63 := 0\r\n" + //
                                "  e74 := e63\r\n" + //
                                "  GOTO IFEND_10_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_10_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFEND_10_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_0_LEVEL_1\r\n" + //
                                "  e65 := 1\r\n" + //
                                "  e66 := d28 EQ e65\r\n" + //
                                "  e67 := 0\r\n" + //
                                "  e68 := d33 EQ e67\r\n" + //
                                "  e69 := e66 LAND e68\r\n" + //
                                "  IF e69 EQ TRUE THEN IFSTAT_12_SEQ_0_LEVEL_2 ELSE IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  e70 := f17 GE f54\r\n" + //
                                "  IF e70 EQ TRUE THEN IFSTAT_13_SEQ_0_LEVEL_3 ELSE IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  e71 := 0\r\n" + //
                                "  e74 := e71\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_0_LEVEL_3\r\n" + //
                                "  e73 := 1\r\n" + //
                                "  e74 := e73\r\n" + //
                                "  GOTO IFEND_13_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_13_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_13_LEVEL_3\r\n" + //
                                "  GOTO IFEND_12_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_12_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFEND_12_LEVEL_2\r\n" + //
                                "  e75 := f51 EQ f14\r\n" + //
                                "  IF e75 EQ TRUE THEN IFSTAT_15_SEQ_0_LEVEL_2 ELSE IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  e76 := 0\r\n" + //
                                "  f76 := e76\r\n" + //
                                "  e78 := 25\r\n" + //
                                "  e79 := f76 IRSHIFT e78\r\n" + //
                                "  e80 := 1\r\n" + //
                                "  e81 := e79 IAND e80\r\n" + //
                                "  f68 := e81\r\n" + //
                                "  f73 := f51\r\n" + //
                                "  e84 := 1\r\n" + //
                                "  e85 := f68 EQ e84\r\n" + //
                                "  IF e85 EQ TRUE THEN IFSTAT_16_SEQ_0_LEVEL_3 ELSE IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  e86 := 1\r\n" + //
                                "  e87 := f73 IADD e86\r\n" + //
                                "  f73 := e87\r\n" + //
                                "  e89 := 1\r\n" + //
                                "  e90 := f76 IRSHIFT e89\r\n" + //
                                "  f76 := e90\r\n" + //
                                "  GOTO IFEND_16_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_16_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_16_LEVEL_3\r\n" + //
                                "  e92 := 23\r\n" + //
                                "  e93 := f73 ILSHIFT e92\r\n" + //
                                "  f85 := e93\r\n" + //
                                "  CALL IntToReal ( e91 -> a73 )\r\n" + //
                                "  e95 <| d11\r\n" + //
                                "  f85 := e95\r\n" + //
                                "  e97 := 31\r\n" + //
                                "  e98 := e74 ILSHIFT e97\r\n" + //
                                "  CALL IntToReal ( e98 -> a73 )\r\n" + //
                                "  e99 <| d11\r\n" + //
                                "  f85 := e99\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_0_LEVEL_2\r\n" + //
                                "  f11 := f51 GT f14\r\n" + //
                                "  IF f11 EQ TRUE THEN IFSTAT_15_SEQ_1_LEVEL_2 ELSE IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  LABEL IFSTAT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  f12 := f51 ISUB f14\r\n" + //
                                "  f50 := f12\r\n" + //
                                "  f14 := f51\r\n" + //
                                "  f73 := f51\r\n" + //
                                "  f16 := f17 IRSHIFT f50\r\n" + //
                                "  f17 := f16\r\n" + //
                                "  f18 := 1\r\n" + //
                                "  f19 := d28 EQ f18\r\n" + //
                                "  f20 := 0\r\n" + //
                                "  f21 := d33 EQ f20\r\n" + //
                                "  f22 := f19 LAND f21\r\n" + //
                                "  IF f22 EQ TRUE THEN IFSTAT_18_SEQ_0_LEVEL_3 ELSE IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  f23 := f17 ISUB f54\r\n" + //
                                "  f76 := f23\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_0_LEVEL_3\r\n" + //
                                "  f25 := f54 ISUB f17\r\n" + //
                                "  f76 := f25\r\n" + //
                                "  GOTO IFEND_18_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_18_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_18_LEVEL_3\r\n" + //
                                "  f27 := 25\r\n" + //
                                "  f28 := f76 IRSHIFT f27\r\n" + //
                                "  f29 := 1\r\n" + //
                                "  f30 := f28 IAND f29\r\n" + //
                                "  f68 := f30\r\n" + //
                                "  f32 := 1\r\n" + //
                                "  f33 := f68 EQ f32\r\n" + //
                                "  IF f33 EQ TRUE THEN IFSTAT_19_SEQ_0_LEVEL_3 ELSE IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  f34 := 1\r\n" + //
                                "  f35 := f73 IADD f34\r\n" + //
                                "  f73 := f35\r\n" + //
                                "  f37 := 1\r\n" + //
                                "  f38 := f76 IRSHIFT f37\r\n" + //
                                "  f76 := f38\r\n" + //
                                "  GOTO IFEND_19_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_19_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_19_LEVEL_3\r\n" + //
                                "  f40 := 23\r\n" + //
                                "  f41 := f73 ILSHIFT f40\r\n" + //
                                "  f85 := f41\r\n" + //
                                "  f43 := 31\r\n" + //
                                "  f44 := e74 ILSHIFT f43\r\n" + //
                                "  CALL IntToReal ( f44 -> a73 )\r\n" + //
                                "  f45 <| d11\r\n" + //
                                "  f85 := f45\r\n" + //
                                "  CALL IntToReal ( f39 -> a73 )\r\n" + //
                                "  f47 <| d11\r\n" + //
                                "  f85 := f47\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_1_LEVEL_2\r\n" + //
                                "  f49 := f14 ISUB f51\r\n" + //
                                "  f50 := f49\r\n" + //
                                "  f51 := f14\r\n" + //
                                "  f73 := f14\r\n" + //
                                "  f53 := f54 IRSHIFT f50\r\n" + //
                                "  f54 := f53\r\n" + //
                                "  f55 := 1\r\n" + //
                                "  f56 := d28 EQ f55\r\n" + //
                                "  f57 := 0\r\n" + //
                                "  f58 := d33 EQ f57\r\n" + //
                                "  f59 := f56 LAND f58\r\n" + //
                                "  IF f59 EQ TRUE THEN IFSTAT_21_SEQ_0_LEVEL_3 ELSE IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  f60 := f17 ISUB f54\r\n" + //
                                "  f76 := f60\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_0_LEVEL_3\r\n" + //
                                "  f62 := f54 ISUB f17\r\n" + //
                                "  f76 := f62\r\n" + //
                                "  GOTO IFEND_21_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_21_SEQ_1_LEVEL_3\r\n" + //
                                "  LABEL IFEND_21_LEVEL_3\r\n" + //
                                "  f64 := 25\r\n" + //
                                "  f65 := f76 IRSHIFT f64\r\n" + //
                                "  f66 := 1\r\n" + //
                                "  f67 := f65 IAND f66\r\n" + //
                                "  f68 := f67\r\n" + //
                                "  f69 := 1\r\n" + //
                                "  f70 := f68 EQ f69\r\n" + //
                                "  IF f70 EQ TRUE THEN IFSTAT_22_SEQ_0_LEVEL_3 ELSE IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFSTAT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  f71 := 1\r\n" + //
                                "  f72 := f73 IADD f71\r\n" + //
                                "  f73 := f72\r\n" + //
                                "  f74 := 1\r\n" + //
                                "  f75 := f76 IRSHIFT f74\r\n" + //
                                "  f76 := f75\r\n" + //
                                "  GOTO IFEND_22_LEVEL_3\r\n" + //
                                "  LABEL IFNEXT_22_SEQ_0_LEVEL_3\r\n" + //
                                "  LABEL IFEND_22_LEVEL_3\r\n" + //
                                "  f77 := 23\r\n" + //
                                "  f78 := f73 ILSHIFT f77\r\n" + //
                                "  f85 := f78\r\n" + //
                                "  f80 := 31\r\n" + //
                                "  f81 := e74 ILSHIFT f80\r\n" + //
                                "  CALL IntToReal ( f81 -] a73 )\r\n" + //
                                "  f82 [| d11\r\n" + //
                                "  f85 := f82\r\n" + //
                                "  CALL IntToReal ( f76 -] a73 )\r\n" + //
                                "  f84 [| d11\r\n" + //
                                "  f85 := f84\r\n" + //
                                "  GOTO IFEND_15_LEVEL_2\r\n" + //
                                "  LABEL IFNEXT_15_SEQ_2_LEVEL_2\r\n" + //
                                "  LABEL IFEND_15_LEVEL_2\r\n" + //
                                "  GOTO IFEND_9_LEVEL_1\r\n" + //
                                "  LABEL IFNEXT_9_SEQ_1_LEVEL_1\r\n" + //
                                "  LABEL IFEND_9_LEVEL_1\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_1_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0\r\n" + //
                                "  d11 |[ f85\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntExp\r\n" + //
                                "  g13 [- g12\r\n" + //
                                "  g15 [- g14\r\n" + //
                                "  g27 := 0\r\n" + //
                                "  g31 := 0.0\r\n" + //
                                "  g20 := 1\r\n" + //
                                "  g27 := g20\r\n" + //
                                "  g22 := 0\r\n" + //
                                "  g31 := g22\r\n" + //
                                "  CALL IntToReal ( g13 -] a73 )\r\n" + //
                                "  g24 [| g17\r\n" + //
                                "  g34 := g31 LT g13\r\n" + //
                                "  IF g34 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0 ELSE WHILENEXT_4_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_4_SEQ_0_LEVEL_0\r\n" + //
                                "  IF g34 EQ TRUE THEN WHILESTAT_4_SEQ_0_LEVEL_0 ELSE WHILEEND_4_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_4_SEQ_0_LEVEL_0\r\n" + //
                                "  g26 := g27 IMUL g15\r\n" + //
                                "  g27 := g26\r\n" + //
                                "  g28 := 1\r\n" + //
                                "  CALL IntToReal ( g28 -] a73 )\r\n" + //
                                "  g29 [| g17\r\n" + //
                                "  CALL RAdd ( g29 -] c96 , g28 -] c98 )\r\n" + //
                                "  g30 [| g17\r\n" + //
                                "  g31 := g30\r\n" + //
                                "  CALL IntToReal ( g13 -] a73 )\r\n" + //
                                "  g32 [| g17\r\n" + //
                                "  g33 := g31 LT g13\r\n" + //
                                "  g34 := g33\r\n" + //
                                "  GOTO WHILECOND_4_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_4_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_4_LEVEL_0\r\n" + //
                                "  g17 |[ g27\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL p\r\n" + //
                                "  B [- f\r\n" + //
                                "  C [- e\r\n" + //
                                "  A := 0\r\n" + //
                                "  D := EXTERNAL CALL IntToReal(C)\r\n" + //
                                "  E := EXTERNAL CALL RAdd(D, C)\r\n" + //
                                "  F := EXTERNAL CALL Round(E)\r\n" + //
                                "  A := F\r\n" + //
                                "  g |[ A\r\n" + //
                                " RETURN\r\n" + //
                                "\r\n" + //
                                " at io.github.H20man13.DeClan.MyLinkerTest.linkTestProgram(MyLinkerTest.java:322)\r\n" + //
                                " at io.github.H20man13.DeClan.MyLinkerTest.testConversions(MyLinkerTest.java:332)\r\n" + //
                                "";
        linkTestProgram(expectedResult, progSrc);
    }
}
