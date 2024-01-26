package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyICodeMachine;
import io.github.H20man13.DeClan.main.MyInterpreter;
import io.github.H20man13.DeClan.main.MyIrLinker;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class InterpreterICodeTest {
    private static StringReader nullReader = new StringReader("");

    private void testInterpreterWithICode(String sourceFile, Reader standardInInt, Reader standardInICode){
        StringWriter intOut = new StringWriter();
        StringWriter icodeOut = new StringWriter();
        StringWriter errOut = new StringWriter();

        try{
            Source source = new ReaderSource(new FileReader(sourceFile));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();

            MyStandardLibrary lib = new MyStandardLibrary(errLog);
        
            MyInterpreter interpreter = new MyInterpreter(errLog, intOut, errOut, standardInInt);
            lib.ioLibrary().accept(interpreter);
            lib.mathLibrary().accept(interpreter);
            lib.realLibrary().accept(interpreter);
            lib.conversionsLibrary().accept(interpreter);
            lib.utilsLibrary().accept(interpreter);
            lib.intLibrary().accept(interpreter);
            prog.accept(interpreter);

            for(LogItem errItem : errLog){
                assertTrue(errItem.toString(), false);
            }

            IrRegisterGenerator gen = new IrRegisterGenerator();
            MyIrLinker linker = new MyIrLinker(errLog);

            Prog program = linker.performLinkage(prog, lib.ioLibrary(), lib.mathLibrary(), lib.conversionsLibrary(), lib.realLibrary(), lib.utilsLibrary(), lib.intLibrary());

            MyICodeMachine vm = new MyICodeMachine(errLog, icodeOut, errOut, standardInICode);
            vm.interpretICode(program);

            for(LogItem errItem : errLog){
                assertTrue(errItem.toString(), false);
            }
        } catch(FileNotFoundException exp){
            assertTrue(exp.toString(), false);
        }
        assertTrue("Expected icode output to be the same as the interpreter output \n\n Interpreter Output is \n\n " + intOut.toString() + " \n\n while icode output is \n\n " + icodeOut.toString(), icodeOut.toString().equals(intOut.toString()));
    }

    @Test
    public void testConversions(){
        testInterpreterWithICode("test_source/conversions.dcl", nullReader, nullReader);
    }

    @Test
    public void testExpressions(){
        testInterpreterWithICode("test_source/expressions.dcl", nullReader, nullReader);
    }

    @Test
    public void testIfStatementBasic(){
        testInterpreterWithICode("test_source/IfStatementBasic.dcl", nullReader, nullReader);
    }

    @Test
    public void testIfStatementAdvanced(){
        testInterpreterWithICode("test_source/IfStatementAdvanced.dcl", nullReader, nullReader);
    }

    
    @Test
    public void testLoops(){
        testInterpreterWithICode("test_source/loops.dcl", nullReader, nullReader);
    }

    @Test
    public void testWhileLoopBasic(){
        testInterpreterWithICode("test_source/WhileLoopBasic.dcl", nullReader, nullReader);
    }

    @Test
    public void testWhileLoopAdvanced(){
        testInterpreterWithICode("test_source/WhileLoopAdvanced.dcl", nullReader, nullReader);
    }

    @Test
    public void testForLoopBasic(){
        testInterpreterWithICode("test_source/ForLoopBasic.dcl", nullReader, nullReader);
    }

    @Test
    public void testForLoopBasic2(){
        testInterpreterWithICode("test_source/ForLoopBasic2.dcl", nullReader, nullReader);
    }

    @Test
    public void testForLoopBasic3(){
        testInterpreterWithICode("test_source/ForLoopBasic3.dcl", nullReader, nullReader);
    }

    @Test
    public void testForLoopAdvanced(){
        testInterpreterWithICode("test_source/ForLoopAdvanced.dcl", nullReader, nullReader);
    }

    @Test
    public void testRepeatLoopBasic(){
        testInterpreterWithICode("test_source/RepeatLoopBasic.dcl", nullReader, nullReader);
    }

    @Test
    public void testSample(){
        testInterpreterWithICode("test_source/sample.dcl", nullReader, nullReader);
    }

    @Test
    public void testTest(){
        testInterpreterWithICode("test_source/test.dcl", nullReader, nullReader);
    }

    @Test
    public void testTest2(){
        testInterpreterWithICode("test_source/test2.dcl", nullReader, nullReader);
    }

    @Test
    public void testTest3(){
        testInterpreterWithICode("test_source/test3.dcl", nullReader, nullReader);
    }

    @Test
    public void testSimpleConversion(){
        StringReader realReader = new StringReader("5.0\n");
        StringReader icodeReader = new StringReader("5.0\n");
        testInterpreterWithICode("test_source/SingleConversion.dcl", realReader, icodeReader);
    }

    @Test
    public void testTest4(){
        StringReader intReader = new StringReader("2\n");
        StringReader icodeReader = new StringReader("2\n");
        testInterpreterWithICode("test_source/test4.dcl", intReader, icodeReader);
    }
}
