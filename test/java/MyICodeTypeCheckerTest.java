package declan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import declan.utils.ErrorLog;
import declan.utils.ErrorLog.LogItem;
import declan.frontend.ast.Program;
import declan.middleware.icode.Prog;
import declan.utils.source.ReaderSource;
import declan.utils.source.Source;
import declan.utils.symboltable.entry.TypeCheckerQualities;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;
import declan.frontend.MyICodeGenerator;
import declan.middleware.MyICodeTypeChecker;
import declan.frontend.MyIrLexer;
import declan.middleware.MyIrLinker;
import declan.frontend.MyIrParser;
import declan.utils.MyStandardLibrary;

public class MyICodeTypeCheckerTest {

    private MyICodeTypeChecker runTypeCheckerOnIrStringSource(String source){
        StringReader stringReader = new StringReader(source);
        ReaderSource typeCheckerSource = new ReaderSource(stringReader);
        return runTypeCheckerOnIrSource(typeCheckerSource);
    }

    private MyICodeTypeChecker runTypeCheckerOnDeClanFileSource(String path){
        try{
            FileReader fileReader = new FileReader(path);
            ReaderSource typeCheckerSource = new ReaderSource(fileReader);
            return runTypeCheckerOnDeClanSource(typeCheckerSource);
        } catch(FileNotFoundException exp){
            assertTrue(exp.toString(), false);
            return null;
        }
    }

    private MyICodeTypeChecker runTypeCheckerOnDeClanSource(Source typeCheckerSource){
        ErrorLog errLog = new ErrorLog();
        
        MyDeClanLexer lexer = new MyDeClanLexer(typeCheckerSource, null, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        Program prog = parser.parseProgram();
        MyICodeGenerator iGen = new MyICodeGenerator(null, errLog);
        Prog program = iGen.generateProgramIr(prog);
        parser.close();

        MyStandardLibrary lib = new MyStandardLibrary(errLog);
        MyIrLinker linker = new MyIrLinker(null, errLog);

        Prog icodeProg = linker.performLinkage(program, lib.irIoLibrary(), lib.irMathLibrary(), lib.irConversionsLibrary(), lib.irIntLibrary(), lib.irRealLibrary(), lib.irUtilsLibrary());

        MyICodeTypeChecker typeChecker = new MyICodeTypeChecker(icodeProg, errLog);
        typeChecker.runTypeChecker();

        for(LogItem item : errLog){
            assertTrue(item.toString(), false);
        }

        return typeChecker;
    }

    private MyICodeTypeChecker runTypeCheckerOnIrSource(Source typeCheckerSource){
        ErrorLog errLog = new ErrorLog();
        

        MyIrLexer lexer = new MyIrLexer(typeCheckerSource, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);

        Prog prog = parser.parseProgram();

        MyICodeTypeChecker typeChecker = new MyICodeTypeChecker(prog, errLog);
        typeChecker.runTypeChecker();

        for(LogItem item : errLog){
            assertTrue(item.toString(), false);
        }

        return typeChecker;
    }

    private void assertTypeCheckerQualities(MyICodeTypeChecker typeChecker, String identifier, Integer typeCheckerQualities){
        boolean typeCheckerContainsQualities = typeChecker.identContainsQualities(identifier, typeCheckerQualities);
        TypeCheckerQualities checkQualities = new TypeCheckerQualities(typeCheckerQualities);
        assertTrue("Error " + identifier + " does not contain qualities " + checkQualities, typeCheckerContainsQualities);
    }

    @Test
    public void testProg1(){
        String source = "SYMBOL SECTION\n"
                      + "DATA SECTION\n"
                      + "DEF GLOBAL a := 456 <INT>\n"
                      + "DEF GLOBAL b := 48393 <INT>\n"
                      + "DEF GLOBAL c := 8.23 <REAL>\n"
                      + "DEF GLOBAL c2 := TRUE <BOOL>\n"
                      + "DEF GLOBAL c3 := FALSE <BOOL>\n"
                      + "DEF d := a IADD b <INT>\n"
                      + "DEF e := b ISUB a <INT>\n"
                      + "DEF k := (GLOBAL c2) LOR (GLOBAL c3) <BOOL>\n"
                      + "DEF l := (GLOBAL b) IOR g <INT>\n"
                      + "BSS SECTION\n"
                      + "CODE SECTION\n"
                      + "DEF o := k LAND (GLOBAL c3) <BOOL>\n"
                      + "DEF p := (GLOBAL a) IAND (GLOBAL a) <INT>\n"
                      + "DEF q := p ILSHIFT (GLOBAL b) <INT>\n"
                      + "DEF r := (GLOBAL b) IRSHIFT f <INT>\n"
                      + "DEF s := r LT (GLOBAL c) <BOOL>\n"
                      + "END\n"
                      + "PROC SECTION\n";
        MyICodeTypeChecker tC = runTypeCheckerOnIrStringSource(source);
        assertTypeCheckerQualities(tC, "a", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "b", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "c", TypeCheckerQualities.REAL);
        assertTypeCheckerQualities(tC, "c2", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "c3", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "d", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "e", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "k", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "l", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "o", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "p", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "q", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "r", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "s", TypeCheckerQualities.BOOLEAN);
    }

    @Test
    public void testProg2(){
        String source = "SYMBOL SECTION\n"
                      + "DATA SECTION\n"
                      + "BSS SECTION\n"
                      + "CODE SECTION\n"
                      + "DEF g := 30 <INT>\n"
                      + "CALL func1 ([g -> param1]<INT>)\n"
                      + "DEF h := (RETURN return1) <INT>\n"
                      + "END\n"
                      + "PROC SECTION\n"
                      + "PROC LABEL func1\n"
                      + "DEF a := (PARAM param1) <INT>\n"
                      + "DEF b := a IADD a <INT>\n"
                      + "CALL func2 ([b->param2]<INT>)\n"
                      + "DEF c := (RETURN return2) <INT>\n"
                      + "DEF RETURN return1 := c <INT>\n"
                      + "RETURN FROM func1\n"
                      + "PROC LABEL func2\n"
                      + "DEF d := (PARAM param2) <INT>\n"
                      + "DEF RETURN return2 := d <INT>\n"
                      + "RETURN FROM func2\n";

        MyICodeTypeChecker tC = runTypeCheckerOnIrStringSource(source);
        assertTypeCheckerQualities(tC, "a", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "b", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "param1", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "param2", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "c", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "return2", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "return1", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "d", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "g", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "h", TypeCheckerQualities.INTEGER);
    }

    @Test
    public void testConversions(){
        String source = "src/declan/test/declan/conversions.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testExpressions(){
        String source = "src/declan/test/declan/expressions.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopAdvanced(){
        String source = "src/declan/test/declan/ForLoopAdvanced.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic(){
        String source = "src/declan/test/declan/ForLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic2(){
        String source = "src/declan/test/declan/ForLoopBasic2.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic3(){
        String source = "src/declan/test/declan/ForLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void ifStatementAdvanced(){
        String source = "src/declan/test/declan/IfStatementAdvanced.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void ifStatementBasic(){
        String source = "src/declan/test/declan/IfStatementBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testLoops(){
        String source = "src/declan/test/declan/loops.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testRepeatLoopBasic(){
        String source = "src/declan/test/declan/RepeatLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testSample(){
        String source = "src/declan/test/declan/sample.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testTest(){
        String source = "src/declan/test/declan/test.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }
}
