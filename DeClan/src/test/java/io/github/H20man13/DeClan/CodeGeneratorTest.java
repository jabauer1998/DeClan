package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

import javax.swing.JDialog;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.TestRig;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.junit.Test;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ErrorLog.LogItem;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.source.ElaborateReaderSource;
import io.github.H20man13.DeClan.common.source.ReaderSource;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.main.MyCodeGenerator;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyICodeGenerator;
import io.github.H20man13.DeClan.main.MyIrLexer;
import io.github.H20man13.DeClan.main.MyIrLinker;
import io.github.H20man13.DeClan.main.MyIrParser;
import io.github.H20man13.DeClan.main.MyOptimizer;
import io.github.H20man13.DeClan.main.MyStandardLibrary;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerLexer;
import io.github.H20man13.DeClan.main.assembler.ArmAssemblerParser;

public class CodeGeneratorTest {
    private void testDeclanFile(String fileName){
        ErrorLog errLog = new ErrorLog();
        String expectedResultFile = fileName.replace("test/ir/linked", "test/temp").replace(".ir", ".a"); 
        try{
            FileReader input = new FileReader(fileName);
            ReaderSource source = new ElaborateReaderSource(fileName, input);
            MyIrLexer lexer = new MyIrLexer(source, errLog);
            MyIrParser parser = new MyIrParser(lexer, errLog);
            Prog program = parser.parseProgram();
            parser.close();

            MyOptimizer optimizer = new MyOptimizer(program);
            optimizer.runLiveVariableAnalysis();

            MyCodeGenerator codeGenerator = new MyCodeGenerator(expectedResultFile, optimizer.getLiveVariableAnalysis(), program, errLog); 
            codeGenerator.codeGen();

            for(LogItem item : errLog){
                assertTrue(item.toString(), false);
            }

            FileReader assemblerStringReader = new FileReader(expectedResultFile);
            ANTLRInputStream inputString = new ANTLRInputStream(assemblerStringReader);
            ArmAssemblerLexer armLexer = new ArmAssemblerLexer(inputString);
            CommonTokenStream tokStream = new CommonTokenStream(armLexer);
            ArmAssemblerParser armParser = new ArmAssemblerParser(tokStream);
            armParser.program();
            
            assertTrue("Error syntax errors discovered", armParser.getNumberOfSyntaxErrors() == 0);
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        testDeclanFile("test/ir/linked/conversions.ir");
    }

    @Test
    public void testExpressions(){
        testDeclanFile("test/ir/linked/expressions.ir");
    }

    @Test
    public void testForLoopAdvanced(){
        testDeclanFile("test/ir/linked/ForLoopAdvanced.ir");
    }

    @Test
    public void testForLoopBasic(){
        testDeclanFile("test/ir/linked/ForLoopBasic.ir");
    }

    @Test
    public void testForLoopBasic2(){
        testDeclanFile("test/ir/linked/ForLoopBasic2.ir");
    }

    @Test
    public void testForLoopBasic3(){
        testDeclanFile("test/ir/linked/ForLoopBasic3.ir");
    }

    @Test
    public void testIfStatementAdvanced(){
        testDeclanFile("test/ir/linked/IfStatementAdvanced.ir");
    }

    @Test
    public void testIfStatementBasic(){
        testDeclanFile("test/ir/linked/IfStatementBasic.ir");
    }

    @Test
    public void testLoops(){
        testDeclanFile("test/ir/linked/loops.ir");
    }

    @Test
    public void testRepeatLoopBasic(){
        testDeclanFile("test/ir/linked/RepeatLoopBasic.ir");
    }

    @Test
    public void testSample(){
        testDeclanFile("test/ir/linked/sample.ir");
    }

    @Test
    public void testTest(){
        testDeclanFile("test/ir/linked/test.ir");
    }

    @Test
    public void testTest2(){
        testDeclanFile("test/ir/linked/test2.ir");
    }

    @Test
    public void testTest3(){
        testDeclanFile("test/ir/linked/test3.ir");
    }

    @Test
    public void testTest4(){
        testDeclanFile("test/ir/linked/test4.ir");
    }

    @Test
    public void testWhileLoopAdvanced(){
        testDeclanFile("test/ir/linked/WhileLoopAdvanced.ir");
    }

    @Test
    public void testWhileLoopBasic(){
        testDeclanFile("test/ir/linked/WhileLoopBasic.ir");
    }
}
