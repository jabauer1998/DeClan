package io.github.h20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.TestRig;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.junit.Test;

import io.github.h20man13.DeClan.common.ErrorLog;
import io.github.h20man13.DeClan.common.ErrorLog.LogItem;
import io.github.h20man13.DeClan.common.ast.Program;
import io.github.h20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.Lib;
import io.github.h20man13.DeClan.common.icode.Prog;
import io.github.h20man13.DeClan.common.source.ElaborateReaderSource;
import io.github.h20man13.DeClan.common.source.ReaderSource;
import io.github.h20man13.DeClan.common.util.Utils;
import io.github.h20man13.DeClan.main.MyCodeGenerator;
import io.github.h20man13.DeClan.main.MyDeClanLexer;
import io.github.h20man13.DeClan.main.MyDeClanParser;
import io.github.h20man13.DeClan.main.MyICodeGenerator;
import io.github.h20man13.DeClan.main.MyIrLexer;
import io.github.h20man13.DeClan.main.MyIrLinker;
import io.github.h20man13.DeClan.main.MyIrParser;
import io.github.h20man13.DeClan.main.MyOptimizer;
import io.github.h20man13.DeClan.main.MyStandardLibrary;
import io.github.h20man13.DeClan.main.assembler.ArmAssemblerLexer;
import io.github.h20man13.DeClan.main.assembler.ArmAssemblerParser;

public class CodeGeneratorTest {
    private void testIrFile(String fileName) throws Exception{
        ErrorLog errLog = new ErrorLog();
        String expectedResultFile = fileName.replace("test/ir/optimized", "test/temp").replace(".ir", ".a");
        FileReader input = new FileReader(fileName);
        ReaderSource source = new ElaborateReaderSource(fileName, input);
        MyIrLexer lexer = new MyIrLexer(source, errLog);
        MyIrParser parser = new MyIrParser(lexer, errLog);
        Prog program = parser.parseProgram();
        parser.close();

        MyOptimizer optimizer = new MyOptimizer(null, program);
        MyCodeGenerator codeGenerator = new MyCodeGenerator(expectedResultFile, program, optimizer, errLog, null); 
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
    }
    
    @Test
    public void testCodeGeneratorExample() throws Exception {
    	testIrFile("test/ir/optimized/CodeGeneratorExample.ir");
    }

    @Test
    public void testConversions() throws Exception{
        testIrFile("test/ir/optimized/conversions.ir");
    }
    
    @Test
    public void testBoolExpression1() throws Exception {
    	testIrFile("test/ir/optimized/BoolExpression1.ir");
    }

    @Test
    public void testExpressions() throws Exception{
        testIrFile("test/ir/optimized/expressions.ir");
    }

    @Test
    public void testForLoopAdvanced() throws Exception{
        testIrFile("test/ir/optimized/ForLoopAdvanced.ir");
    }

    @Test
    public void testForLoopBasic() throws Exception{
        testIrFile("test/ir/optimized/ForLoopBasic.ir");
    }

    @Test
    public void testForLoopBasic2() throws Exception{
        testIrFile("test/ir/optimized/ForLoopBasic2.ir");
    }

    @Test
    public void testForLoopBasic3() throws Exception{
        testIrFile("test/ir/optimized/ForLoopBasic3.ir");
    }

    @Test
    public void testIfStatementAdvanced() throws Exception{
        testIrFile("test/ir/optimized/IfStatementAdvanced.ir");
    }

    @Test
    public void testIfStatementBasic() throws Exception{
        testIrFile("test/ir/optimized/IfStatementBasic.ir");
    }

    @Test
    public void testLoops() throws Exception{
        testIrFile("test/ir/optimized/loops.ir");
    }

    @Test
    public void testRepeatLoopBasic() throws Exception{
        testIrFile("test/ir/optimized/RepeatLoopBasic.ir");
    }

    @Test
    public void testSample() throws Exception{
        testIrFile("test/ir/optimized/sample.ir");
    }

    @Test
    public void testTest() throws Exception{
        testIrFile("test/ir/optimized/test.ir");
    }

    @Test
    public void testTest2() throws Exception{
        testIrFile("test/ir/optimized/test2.ir");
    }

    @Test
    public void testTest3() throws Exception{
        testIrFile("test/ir/optimized/test3.ir");
    }

    @Test
    public void testTest4() throws Exception{
        testIrFile("test/ir/optimized/test4.ir");
    }

    @Test
    public void testWhileLoopAdvanced() throws Exception{
        testIrFile("test/ir/optimized/WhileLoopAdvanced.ir");
    }

    @Test
    public void testWhileLoopBasic() throws Exception{
        testIrFile("test/ir/optimized/WhileLoopBasic.ir");
    }
}
