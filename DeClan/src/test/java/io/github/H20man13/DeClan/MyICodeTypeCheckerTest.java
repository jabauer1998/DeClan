package io.github.H20man13.DeClan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ErrorLog.LogItem;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.source.ReaderSource;
import io.github.H20man13.DeClan.common.source.Source;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyICodeTypeChecker;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrLinker;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

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
        
        MyDeClanLexer lexer = new MyDeClanLexer(typeCheckerSource, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        Program prog = parser.parseProgram();
        MyICodeGenerator iGen = new MyICodeGenerator(errLog);
        Prog program = iGen.generateProgramIr(prog);
        parser.close();

        MyStandardLibrary lib = new MyStandardLibrary(errLog);
        MyIrLinker linker = new MyIrLinker(errLog);

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
                      + "DEF f := d IMOD e <INT>\n"
                      + "DEF g := a IMUL b <INT>\n"
                      + "DEF k := (GLOBAL c2) LOR (GLOBAL c3) <BOOL>\n"
                      + "DEF l := (GLOBAL b) IOR g <INT>\n"
                      + "DEF m := l IDIV (GLOBAL a) <INT>\n"
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
        assertTypeCheckerQualities(tC, "f", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "g", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "k", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "l", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "m", TypeCheckerQualities.INTEGER);
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
                      + "RETURN\n"
                      + "PROC LABEL func2\n"
                      + "DEF d := (PARAM param2) <INT>\n"
                      + "DEF e := d IMUL d <INT>\n"
                      + "DEF f := e IMOD d <INT>\n"
                      + "DEF RETURN return2 := f <INT>\n"
                      + "RETURN\n";

        MyICodeTypeChecker tC = runTypeCheckerOnIrStringSource(source);
        assertTypeCheckerQualities(tC, "a", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "b", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "param1", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "param2", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "c", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "return2", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "return1", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "d", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "e", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "f", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "g", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "h", TypeCheckerQualities.INTEGER);
    }

    @Test
    public void testConversions(){
        String source = "test/declan/conversions.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testExpressions(){
        String source = "test/declan/expressions.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopAdvanced(){
        String source = "test/declan/ForLoopAdvanced.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic(){
        String source = "test/declan/ForLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic2(){
        String source = "test/declan/ForLoopBasic2.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic3(){
        String source = "test/declan/ForLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void ifStatementAdvanced(){
        String source = "test/declan/IfStatementAdvanced.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void ifStatementBasic(){
        String source = "test/declan/IfStatementBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testLoops(){
        String source = "test/declan/loops.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testRepeatLoopBasic(){
        String source = "test/declan/RepeatLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testSample(){
        String source = "test/declan/sample.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testTest(){
        String source = "test/declan/test.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }
}
