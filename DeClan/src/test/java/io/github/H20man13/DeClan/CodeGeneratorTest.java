package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

import javax.swing.JDialog;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.TestRig;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.main.MyCodeGenerator;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyOptimizer;
import io.github.H20man13.DeClan.main.MyStandardLibrary;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerLexer;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser;

public class CodeGeneratorTest {
    private void testDeclanFile(String fileName){
        ErrorLog errLog = new ErrorLog();
        String expectedResultFile = fileName.replace("test/declan", "test/assembly").replace(".dcl", ".a"); 
        try{
            FileReader reader = new FileReader(fileName);
            ReaderSource source = new ReaderSource(reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            MyStandardLibrary stdLib = new MyStandardLibrary(errLog);

            Program prog = parser.parseProgram();
            parser.close();
            
            MyICodeGenerator gen = new MyICodeGenerator(errLog);
            Prog program = gen.generateProgramIr(prog);

            MyOptimizer optimizer = new MyOptimizer(program);
            optimizer.performCommonSubExpressionElimination();
            optimizer.performDeadCodeElimination();

            String outputFile = fileName.replace("test/declan", "test/temp").replace(".dcl", ".tmp");

            MyCodeGenerator codeGenerator = new MyCodeGenerator(outputFile, optimizer.getLiveVariableAnalysis(), program, errLog); 
            codeGenerator.codeGen();

            for(LogItem item : errLog){
                assertTrue(item.toString(), false);
            }

            FileReader outputStringReader = new FileReader(outputFile);
            FileReader expectedResultReader = new FileReader(expectedResultFile);

            Scanner outputStringScanner = new Scanner(outputStringReader);
            Scanner expectedResultScanner = new Scanner(expectedResultReader);
            
            int lineNumber = 0;
            while(outputStringScanner.hasNext() && expectedResultScanner.hasNext()){
                String outputLine = outputStringScanner.nextLine();
                String expectedLine = expectedResultScanner.nextLine();
                assertTrue("At line " + lineNumber + " expected-\n" + expectedLine + "\n but found \n" + outputLine, outputLine.equals(expectedLine));
                lineNumber++;
            }

            outputStringReader.close();

            FileReader assemblerStringReader = new FileReader(outputFile);
            ANTLRInputStream inputString = new ANTLRInputStream(assemblerStringReader);
            ArmAssemblerLexer armLexer = new ArmAssemblerLexer(inputString);
            CommonTokenStream tokStream = new CommonTokenStream(armLexer);
            ArmAssemblerParser armParser = new ArmAssemblerParser(tokStream);
            armParser.program();

            outputStringScanner.close();
            expectedResultScanner.close();
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        testDeclanFile("test/declan/conversions.dcl");
    }

    @Test
    public void testExpressions(){
        testDeclanFile("test_source/expressions.dcl");
    }

    @Test
    public void testForLoopAdvanced(){
        testDeclanFile("test/declan/ForLoopAdvanced.dcl");
    }

    @Test
    public void testForLoopBasic(){
        testDeclanFile("test/declan/ForLoopBasic.dcl");
    }

    @Test
    public void testForLoopBasic2(){
        testDeclanFile("test/declan/ForLoopBasic2.dcl");
    }

    @Test
    public void testForLoopBasic3(){
        testDeclanFile("test/declan/ForLoopBasic3.dcl");
    }

    @Test
    public void testIfStatementAdvanced(){
        testDeclanFile("test/declan/IfStatementAdvanced.dcl");
    }

    @Test
    public void testIfStatementBasic(){
        testDeclanFile("test/declan/IfStatementBasic.dcl");
    }

    @Test
    public void testLoops(){
        testDeclanFile("test/declan/loops.dcl");
    }

    @Test
    public void testRepeatLoopBasic(){
        testDeclanFile("test/declan/RepeatLoopBasic.dcl");
    }

    @Test
    public void testSample(){
        testDeclanFile("test/declan/sample.dcl");
    }

    @Test
    public void testTest(){
        testDeclanFile("test/declan/test.dcl");
    }

    @Test
    public void testTest2(){
        testDeclanFile("test/declan/test2.dcl");
    }

    @Test
    public void testTest3(){
        testDeclanFile("test/declan/test3.dcl");
    }

    @Test
    public void testTest4(){
        testDeclanFile("test/declan/test4.dcl");
    }

    @Test
    public void testWhileLoopAdvanced(){
        testDeclanFile("test/declan/WhileLoopAdvanced.dcl");
    }

    @Test
    public void testWhileLoopBasic(){
        testDeclanFile("test/declan/WhileLoopBasic.dcl");
    }
}
