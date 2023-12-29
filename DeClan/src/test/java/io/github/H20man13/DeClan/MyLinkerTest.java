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
            Prog irCode = linker.performLinkage(prog, lib.ioLibrary(), lib.mathLibrary(), lib.conversionsLibrary(), lib.intLibrary(), lib.realLibrary());
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
                                " CALL WriteInt ( b -> m2 )\r\n" + //
                                " CALL WriteReal ( c -> m4 )\r\n" + //
                                " CALL WriteReal ( d -> m4 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " CALL Divide ( b -> m7 , a -> m9 )\r\n" + //
                                " m6 <| n8\r\n" + //
                                " CALL WriteReal ( m6 -> m4 )\r\n" + //
                                " m := 5\r\n" + //
                                " n := b IADD m\r\n" + //
                                " o := 6\r\n" + //
                                " p := b IADD o\r\n" + //
                                " q := n IMUL p\r\n" + //
                                " CALL WriteInt ( q -> m2 )\r\n" + //
                                " r := 4\r\n" + //
                                " s := a IADD r\r\n" + //
                                " t := 5.0\r\n" + //
                                " u := a RADD t\r\n" + //
                                " v := s RMUL u\r\n" + //
                                " CALL WriteReal ( v -> m4 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                " w := 3.1415\r\n" + //
                                " CALL p ( b -> e , w -> f )\r\n" + //
                                " x <| g\r\n" + //
                                " a := x\r\n" + //
                                " CALL WriteReal ( d -> m4 )\r\n" + //
                                " CALL WriteLn (  )\r\n" + //
                                "END\r\n" + //
                                "PROC SECTION\r\n" + //
                                " PROC LABEL WriteInt\r\n" + //
                                "  m3 <- m2\r\n" + //
                                "  IPARAM m3\r\n" + //
                                "IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 1\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteReal\r\n" + //
                                "  m5 <- m4\r\n" + //
                                "  IPARAM m5\r\n" + //
                                "IASM \"LDR R0, %a\"\r\n" + //
                                "  IASM \"SWI 2\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL WriteLn\r\n" + //
                                "  IASM \"SWI 4\"\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Div\r\n" + //
                                "  e0 <- g\r\n" + //
                                "  e2 <- h\r\n" + //
                                "  d8 := 0\r\n" + //
                                "  d9 := 0\r\n" + //
                                "  d8 := e0\r\n" + //
                                "  e1 := 0\r\n" + //
                                "  d9 := e1\r\n" + //
                                "  e3 := d8 ISUB e2\r\n" + //
                                "  e4 := 0\r\n" + //
                                "  e5 := e3 GT e4\r\n" + //
                                "  IF e5 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0 ELSE WHILENEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_2_SEQ_0_LEVEL_0\r\n" + //
                                "  IF e5 EQ TRUE THEN WHILESTAT_2_SEQ_0_LEVEL_0 ELSE WHILEEND_2_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  e6 := d8 ISUB e2\r\n" + //
                                "  d8 := e6\r\n" + //
                                "  e7 := 1\r\n" + //
                                "  e8 := d9 IADD e7\r\n" + //
                                "  d9 := e8\r\n" + //
                                "  e9 := d8 ISUB e2\r\n" + //
                                "  f0 := 0\r\n" + //
                                "  f1 := e9 GT f0\r\n" + //
                                "  e5 := f1\r\n" + //
                                "  GOTO WHILECOND_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_2_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_2_LEVEL_0\r\n" + //
                                "  i |< d9\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL IntToReal\r\n" + //
                                "  Q <- a\r\n" + //
                                "  F := 0.0\r\n" + //
                                "  G := 0.0\r\n" + //
                                "  H := 0\r\n" + //
                                "  I := 0\r\n" + //
                                "  J := 0.0\r\n" + //
                                "  K := 0\r\n" + //
                                "  L := 0\r\n" + //
                                "  M := 1\r\n" + //
                                "  G := M\r\n" + //
                                "  N := 1\r\n" + //
                                "  H := N\r\n" + //
                                "  O := 23\r\n" + //
                                "  P := H LE O\r\n" + //
                                "  IF P EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  IF P EQ TRUE THEN WHILESTAT_0_SEQ_0_LEVEL_0 ELSE WHILEEND_0_LEVEL_0\r\n" + //
                                "  LABEL WHILESTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  R := 23\r\n" + //
                                "  S := R ISUB H\r\n" + //
                                "  T := Q IRSHIFT S\r\n" + //
                                "  I := T\r\n" + //
                                "  U := 1\r\n" + //
                                "  V := I IAND U\r\n" + //
                                "  I := V\r\n" + //
                                "  W := 2\r\n" + //
                                "  X := EXTERNAL CALL INeg(H)\r\n" + //
                                "  CALL RealExp ( W -> p , X -> q )\r\n" + //
                                "  Y [| r\r\n" + //
                                "  Z := I RMUL Y\r\n" + //
                                "  J := Z\r\n" + //
                                "  a0 := G RADD J\r\n" + //
                                "  G := a0\r\n" + //
                                "  a1 := 1\r\n" + //
                                "  a2 := H IADD a1\r\n" + //
                                "  H := a2\r\n" + //
                                "  a3 := 23\r\n" + //
                                "  a4 := H LE a3\r\n" + //
                                "  P := a4\r\n" + //
                                "  GOTO WHILECOND_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILENEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL WHILEEND_0_LEVEL_0\r\n" + //
                                "  a5 := 23\r\n" + //
                                "  a6 := Q IRSHIFT a5\r\n" + //
                                "  a7 := 255\r\n" + //
                                "  a8 := a6 IAND a7\r\n" + //
                                "  K := a8\r\n" + //
                                "  a9 := 2\r\n" + //
                                "  b0 := 127\r\n" + //
                                "  b1 := K ISUB b0\r\n" + //
                                "  CALL IntExp ( a9 -> s , b1 -> t )\r\n" + //
                                "  b2 <| u\r\n" + //
                                "  K := b2\r\n" + //
                                "  b3 := 31\r\n" + //
                                "  b4 := Q IRSHIFT b3\r\n" + //
                                "  b5 := 1\r\n" + //
                                "  b6 := b4 IAND b5\r\n" + //
                                "  L := b6\r\n" + //
                                "  b7 := 1\r\n" + //
                                "  b8 := L EQ b7\r\n" + //
                                "  IF b8 EQ TRUE THEN IFSTAT_0_SEQ_0_LEVEL_0 ELSE IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFSTAT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  b9 := 1\r\n" + //
                                "  c0 := EXTERNAL CALL INeg(b9)\r\n" + //
                                "  L := c0\r\n" + //
                                "  GOTO IFEND_0_LEVEL_0\r\n" + //
                                "  LABEL IFNEXT_0_SEQ_0_LEVEL_0\r\n" + //
                                "  LABEL IFEND_0_LEVEL_0\r\n" + //
                                "  c1 := L IMUL K\r\n" + //
                                "  c2 := c1 RMUL G\r\n" + //
                                "  F := c2\r\n" + //
                                "  b |< F\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL Divide\r\n" + //
                                "  m8 <- m7\r\n" + //
                                "  n0 <- m9\r\n" + //
                                "  n4 := 0\r\n" + //
                                "  n7 := 0\r\n" + //
                                "  CALL Div ( m8 -> g , n0 -> h )\r\n" + //
                                "  n3 <| i\r\n" + //
                                "  n4 := n3\r\n" + //
                                "  CALL IntToReal ( n4 -> a )\r\n" + //
                                "  n5 <| b\r\n" + //
                                "  n7 := n5\r\n" + //
                                "  n8 |< n7\r\n" + //
                                " RETURN\r\n" + //
                                " PROC LABEL p\r\n" + //
                                "  z <- f\r\n" + //
                                "  A <- e\r\n" + //
                                "  y := 0\r\n" + //
                                "  B := z RADD A\r\n" + //
                                "  C := EXTERNAL CALL Round(B)\r\n" + //
                                "  y := C\r\n" + //
                                "  g |< y\r\n" + //
                                " RETURN\r\n";
        linkTestProgram(expectedResult, progSrc);
    }
}
