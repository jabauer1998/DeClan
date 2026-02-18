package declan;

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
import org.junit.Test;

import declan.utils.ErrorLog;
import declan.utils.ErrorLog.LogItem;
import declan.frontend.ast.Program;
import declan.frontend.IrRegisterGenerator;
import declan.middleware.icode.ICode;
import declan.middleware.icode.Lib;
import declan.middleware.icode.Prog;
import declan.utils.source.ElaborateReaderSource;
import declan.utils.source.ReaderSource;
import declan.utils.Utils;
import declan.backend.MyCodeGenerator;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;
import declan.frontend.MyICodeGenerator;
import declan.frontend.MyIrLexer;
import declan.middleware.MyIrLinker;
import declan.frontend.MyIrParser;
import declan.middleware.MyOptimizer;
import declan.utils.MyStandardLibrary;
import declan.backend.assembler.ArmAssemblerLexer;
import declan.backend.assembler.ArmAssemblerParser;

public class CodeGeneratorTest {
    private void testIrFile(String fileName) throws Exception{
        ErrorLog errLog = new ErrorLog();
        String expectedResultFile = fileName.replace("src/ir/optimized", "test/temp").replace(".ir", ".a");
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
        testIrFile("src/ir/optimized/CodeGeneratorExample.ir");
    }

    @Test
    public void testConversions() throws Exception{
        testIrFile("src/ir/optimized/conversions.ir");
    }
    
    @Test
    public void testBoolExpression1() throws Exception {
        testIrFile("src/ir/optimized/BoolExpression1.ir");
    }

    @Test
    public void testExpressions() throws Exception{
        testIrFile("src/ir/optimized/expressions.ir");
    }

    @Test
    public void testForLoopAdvanced() throws Exception{
        testIrFile("src/ir/optimized/ForLoopAdvanced.ir");
    }

    @Test
    public void testForLoopBasic() throws Exception{
        testIrFile("src/ir/optimized/ForLoopBasic.ir");
    }

    @Test
    public void testForLoopBasic2() throws Exception{
        testIrFile("src/ir/optimized/ForLoopBasic2.ir");
    }

    @Test
    public void testForLoopBasic3() throws Exception{
        testIrFile("src/ir/optimized/ForLoopBasic3.ir");
    }

    @Test
    public void testIfStatementAdvanced() throws Exception{
        testIrFile("src/ir/optimized/IfStatementAdvanced.ir");
    }

    @Test
    public void testIfStatementBasic() throws Exception{
        testIrFile("src/ir/optimized/IfStatementBasic.ir");
    }

    @Test
    public void testLoops() throws Exception{
        testIrFile("src/ir/optimized/loops.ir");
    }

    @Test
    public void testRepeatLoopBasic() throws Exception{
        testIrFile("src/ir/optimized/RepeatLoopBasic.ir");
    }

    @Test
    public void testSample() throws Exception{
        testIrFile("src/ir/optimized/sample.ir");
    }

    @Test
    public void testTest() throws Exception{
        testIrFile("src/ir/optimized/test.ir");
    }

    @Test
    public void testTest2() throws Exception{
        testIrFile("src/ir/optimized/test2.ir");
    }

    @Test
    public void testTest3() throws Exception{
        testIrFile("src/ir/optimized/test3.ir");
    }

    @Test
    public void testTest4() throws Exception{
        testIrFile("src/ir/optimized/test4.ir");
    }

    @Test
    public void testWhileLoopAdvanced() throws Exception{
        testIrFile("src/ir/optimized/WhileLoopAdvanced.ir");
    }

    @Test
    public void testWhileLoopBasic() throws Exception{
        testIrFile("src/ir/optimized/WhileLoopBasic.ir");
    }
}
