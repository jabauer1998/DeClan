package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyICodeMachine;
import io.github.H20man13.DeClan.main.MyInterpreter;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class InterpreterICodeTest {
    private void testInterpreterWithICode(String sourceFile){
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
        
            MyInterpreter interpreter = new MyInterpreter(errLog, intOut, errOut);
            lib.ioLibrary().accept(interpreter);
            lib.mathLibrary().accept(interpreter);
            prog.accept(interpreter);

            IrRegisterGenerator gen = new IrRegisterGenerator();
            MyICodeGenerator iGen = new MyICodeGenerator(errLog, gen);
            lib.ioLibrary().accept(iGen);
            lib.mathLibrary().accept(iGen);
            prog.accept(iGen);

            List<ICode> generatedICode = iGen.getICode();

            MyICodeMachine vm = new MyICodeMachine(errLog, icodeOut, errOut);
            vm.interpretICode(generatedICode);

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
        testInterpreterWithICode("test_source/conversions.dcl");
    }

    @Test
    public void testExpressions(){
        testInterpreterWithICode("test_source/expressions.dcl");
    }

    @Test
    public void testLoops(){
        testInterpreterWithICode("test_source/loops.dcl");
    }

    @Test
    public void testSample(){
        testInterpreterWithICode("test_source/sample.dcl");
    }

    @Test
    public void testTest(){
        testInterpreterWithICode("test_source/test.dcl");
    }

    @Test
    public void testTest2(){
        testInterpreterWithICode("test_source/test2.dcl");
    }

    @Test
    public void testTest3(){
        testInterpreterWithICode("test_source/test3.dcl");
    }

    @Test
    public void testTest4(){
        testInterpreterWithICode("test_source/test4.dcl");
    }
}
