package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
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
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
            return null;
        }
    }

    private MyICodeTypeChecker runTypeCheckerOnDeClanSource(Source typeCheckerSource){
        ErrorLog errLog = new ErrorLog();
        
        MyDeClanLexer lexer = new MyDeClanLexer(typeCheckerSource, errLog);
        MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
        Program prog = parser.parseProgram();
        parser.close();

        MyStandardLibrary lib = new MyStandardLibrary(errLog);
        MyIrLinker linker = new MyIrLinker(errLog);

        Prog icodeProg = linker.performLinkage(prog, lib.ioLibrary(), lib.mathLibrary(), lib.conversionsLibrary(), lib.intLibrary(), lib.realLibrary(), lib.utilsLibrary());

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
                      + "GLOBAL a := 456 [INT]\n"
                      + "GLOBAL b := 48393 [INT]\n"
                      + "GLOBAL c := 8.23 [REAL]\n"
                      + "GLOBAL c2 := TRUE [BOOL]\n"
                      + "GLOBAL c3 := FALSE [BOOL]\n"
                      + "GLOBAL d := a IADD b [INT]\n"
                      + "GLOBAL e := b ISUB a [INT]\n"
                      + "GLOBAL f := d IMOD e [INT]\n"
                      + "GLOBAL g := a IMUL b [INT]\n"
                      + "h := c RADD f [REAL]\n"
                      + "i := h RSUB c [REAL]\n"
                      + "j := b RMUL i [REAL]\n"
                      + "k := c2 LOR c3 [BOOL]\n"
                      + "l := b IOR g [INT]\n"
                      + "m := l IDIV a [INT]\n"
                      + "n := m RDIVIDE c [REAL]\n"
                      + "o := k LAND c3 [BOOL]\n"
                      + "p := a IAND a [INT]\n"
                      + "q := p ILSHIFT b [INT]\n"
                      + "r := b IRSHIFT f [INT]\n"
                      + "s := r LT c [BOOL]\n"
                      + "t := i GT g [BOOL]\n"
                      + "u := p LE j [BOOL]\n"
                      + "v := r GE n [BOOL]\n"
                      + "CODE SECTION\n"
                      + "w := n NE n [BOOL]\n"
                      + "x := j EQ h [BOOL]\n"
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
        assertTypeCheckerQualities(tC, "h", TypeCheckerQualities.REAL);
        assertTypeCheckerQualities(tC, "i", TypeCheckerQualities.REAL);
        assertTypeCheckerQualities(tC, "j", TypeCheckerQualities.REAL);
        assertTypeCheckerQualities(tC, "k", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "l", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "m", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "n", TypeCheckerQualities.REAL);
        assertTypeCheckerQualities(tC, "o", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "p", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "q", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "r", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "s", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "t", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "u", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "v", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "w", TypeCheckerQualities.BOOLEAN);
        assertTypeCheckerQualities(tC, "x", TypeCheckerQualities.BOOLEAN);
    }

    @Test
    public void testProg2(){
        String source = "SYMBOL SECTION\n"
                      + "DATA SECTION\n"
                      + "CODE SECTION\n"
                      + "g := 30 [INT]\n"
                      + "CALL func1 ((g -> param1)[INT])\n"
                      + "EXTERNAL RETURN h := return1 [INT]\n"
                      + "END\n"
                      + "PROC SECTION\n"
                      + "PROC LABEL func1\n"
                      + "PARAM a := param1 [INT]\n"
                      + "PARAM b := a IADD a [INT]\n"
                      + "CALL func2 ((b->param2)[INT])\n"
                      + "EXTERNAL RETURN c := return2 [INT]\n"
                      + "INTERNAL RETURN return1 := c [INT]\n"
                      + "RETURN\n"
                      + "PROC LABEL func2\n"
                      + "PARAM d := param2 [INT]\n"
                      + "e := d IMUL d [INT]\n"
                      + "f := e IMOD d [INT]\n"
                      + "INTERNAL RETURN return2 := f [INT]\n"
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
