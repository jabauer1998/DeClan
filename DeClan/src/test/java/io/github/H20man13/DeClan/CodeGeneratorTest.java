package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.main.MyCodeGenerator;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyOptimizer;
import io.github.H20man13.DeClan.main.MyStandardLibrary;

public class CodeGeneratorTest {
    private void testDeclanFile(String fileName){
        ErrorLog errLog = new ErrorLog();
        try{
            FileReader reader = new FileReader(fileName);
            ReaderSource source = new ReaderSource(reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            MyStandardLibrary stdLib = new MyStandardLibrary(errLog);

            Program prog = parser.parseProgram();

            IrRegisterGenerator rGen = new IrRegisterGenerator();
            MyICodeGenerator gen = new MyICodeGenerator(errLog, rGen);

            stdLib.ioLibrary().accept(gen);
            stdLib.mathLibrary().accept(gen);

            prog.accept(gen);

            List<ICode> generatedICode = gen.getICode();

            MyOptimizer optimizer = new MyOptimizer(generatedICode, rGen);
            optimizer.runDataFlowAnalysis();
            optimizer.performDeadCodeElimination();

            List<ICode> optimizedICode = optimizer.getICode();

            MyCodeGenerator codeGenerator = new MyCodeGenerator(optimizer.getLiveVariableAnalysis(), optimizedICode, rGen, errLog);

            

            StringWriter writer = new StringWriter();
            codeGenerator.codeGen(writer);

            for(LogItem item : errLog){
                assertTrue(item.toString(), false);
            }
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        testDeclanFile("test_source/conversions.dcl");
    }

    @Test
    public void testExpressions(){
        testDeclanFile("test_source/expressions.dcl");
    }

    @Test
    public void testForLoopAdvanced(){
        testDeclanFile("test_source/ForLoopAdvanced.dcl");
    }

    @Test
    public void testForLoopBasic(){
        testDeclanFile("test_source/ForLoopBasic.dcl");
    }

    @Test
    public void testForLoopBasic2(){
        testDeclanFile("test_source/ForLoopBasic2.dcl");
    }

    @Test
    public void testForLoopBasic3(){
        testDeclanFile("test_source/ForLoopBasic3.dcl");
    }

    @Test
    public void testIfStatementAdvanced(){
        testDeclanFile("test_source/IfStatementAdvanced.dcl");
    }

    @Test
    public void testIfStatementBasic(){
        testDeclanFile("test_source/IfStatementBasic.dcl");
    }

    @Test
    public void testLoops(){
        testDeclanFile("test_source/loops.dcl");
    }

    @Test
    public void testRepeatLoopBasic(){
        testDeclanFile("test_source/RepeatLoopBasic.dcl");
    }

    @Test
    public void testSample(){
        testDeclanFile("test_source/sample.dcl");
    }

    @Test
    public void testTest(){
        testDeclanFile("test_source/test.dcl");
    }

    @Test
    public void testTest2(){
        testDeclanFile("test_source/test2.dcl");
    }

    @Test
    public void testTest3(){
        testDeclanFile("test_source/test3.dcl");
    }

    @Test
    public void testTest4(){
        testDeclanFile("test_source/test4.dcl");
    }

    @Test
    public void testWhileLoopAdvanced(){
        testDeclanFile("test_source/WhileLoopAdvanced.dcl");
    }

    @Test
    public void testWhileLoopBasic(){
        testDeclanFile("test_source/WhileLoopBasic.dcl");
    }
}
