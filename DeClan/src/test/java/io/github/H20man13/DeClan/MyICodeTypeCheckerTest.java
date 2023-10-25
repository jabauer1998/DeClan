package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyICodeTypeChecker;
import io.github.H20man13.DeClan.main.MyIrLexer;
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
        MyICodeGenerator gen = new MyICodeGenerator(errLog);

        lib.ioLibrary().accept(gen);
        lib.mathLibrary().accept(gen);
        prog.accept(gen);

        List<ICode> icodeProg = gen.getICode();

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

        List<ICode> prog = parser.parseProgram();

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
        String source = "a := 456\n"
                      + "b := 48393\n"
                      + "c := 8.23\n"
                      + "c2 := TRUE\n"
                      + "c3 := FALSE\n"
                      + "d := a IADD b\n"
                      + "e := b ISUB a\n"
                      + "f := d IMOD e\n"
                      + "g := a IMUL b\n"
                      + "h := c RADD f\n"
                      + "i := h RSUB c\n"
                      + "j := b RMUL i\n"
                      + "k := c2 LOR c3\n"
                      + "l := b IOR g\n"
                      + "m := l IDIV a\n"
                      + "n := m RDIVIDE c\n"
                      + "o := k LAND c3\n"
                      + "p := a IAND a\n"
                      + "q := p ILSHIFT b\n"
                      + "r := b IRSHIFT f\n"
                      + "s := r LT c\n"
                      + "t := i GT g\n"
                      + "u := p LE j\n"
                      + "v := r GE n\n"
                      + "w := n NE n\n"
                      + "x := j EQ h\n"
                      + "END\n";
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
        String source = "LABEL func1\n"
                      + "a <- param1\n"
                      + "b := a IADD a\n"
                      + "PROC func2 ( b -> param2 )\n"
                      + "c <& return2\n"
                      + "return1 := c RADD c\n"
                      + "RETURN\n"
                      + "LABEL func2\n"
                      + "d <- param2\n"
                      + "e := d IMUL d\n"
                      + "f := e IMOD d\n"
                      + "return2 := f\n"
                      + "RETURN\n"
                      + "g := 30\n"
                      + "PROC func1 ( g -> param1 )\n"
                      + "h <& return1\n"
                      + "END\n";
        MyICodeTypeChecker tC = runTypeCheckerOnIrStringSource(source);
        assertTypeCheckerQualities(tC, "a", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "b", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "param1", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "param2", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "c", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "return2", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "return1", TypeCheckerQualities.REAL);
        assertTypeCheckerQualities(tC, "d", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "e", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "f", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "g", TypeCheckerQualities.INTEGER);
        assertTypeCheckerQualities(tC, "h", TypeCheckerQualities.REAL);
    }

    @Test
    public void testConversions(){
        String source = "test_source/conversions.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testExpressions(){
        String source = "test_source/expressions.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopAdvanced(){
        String source = "test_source/ForLoopAdvanced.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic(){
        String source = "test_source/ForLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic2(){
        String source = "test_source/ForLoopBasic2.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testForLoopBasic3(){
        String source = "test_source/ForLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void ifStatementAdvanced(){
        String source = "test_source/IfStatementAdvanced.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void ifStatementBasic(){
        String source = "test_source/IfStatementBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testLoops(){
        String source = "test_source/loops.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testRepeatLoopBasic(){
        String source = "test_source/RepeatLoopBasic.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testSample(){
        String source = "test_source/sample.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }

    @Test
    public void testTest(){
        String source = "test_source/test.dcl";
        runTypeCheckerOnDeClanFileSource(source);
    }
}
