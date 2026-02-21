package declan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import declan.utils.ErrorLog;
import declan.utils.ErrorLog.LogItem;
import declan.frontend.ast.Program;
import declan.middleware.icode.Prog;
import declan.utils.source.ReaderSource;
import declan.utils.source.Source;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;
import declan.middleware.MyICodeMachine;
import declan.frontend.MyInterpreter;
import declan.middleware.MyIrLinker;
import declan.middleware.MyOptimizer;
import declan.utils.MyStandardLibrary;

public class InterpreterICodeTest {
    private static StringReader nullReader = new StringReader("");

    private void testInterpreterWithICode(String sourceFile, Reader standardInInt, Reader standardInICode, Reader standardInOpt, boolean opt){
        StringWriter intOut = new StringWriter();
        StringWriter icodeOut = new StringWriter();
        StringWriter errOut = new StringWriter();
        StringWriter optOut = new StringWriter();

        try{
            Source source = new ReaderSource(new FileReader(sourceFile));
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(source, null, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            parser.close();

            MyStandardLibrary lib = new MyStandardLibrary(errLog);
        
            MyInterpreter interpreter = new MyInterpreter(errLog, intOut, errOut, standardInInt);
            lib.declanIoLibrary().accept(interpreter);
            lib.declanMathLibrary().accept(interpreter);
            lib.declanRealLibrary().accept(interpreter);
            lib.declanConversionsLibrary().accept(interpreter);
            lib.declanUtilsLibrary().accept(interpreter);
            lib.declanIntLibrary().accept(interpreter);
            prog.accept(interpreter);

            for(LogItem errItem : errLog){
                assertTrue(errItem.toString(), false);
            }
            
            MyIrLinker linker = new MyIrLinker(null, errLog);
            Prog program = linker.performLinkage(prog, lib.declanIoLibrary(), lib.declanMathLibrary(), lib.declanConversionsLibrary(), lib.declanRealLibrary(), lib.declanUtilsLibrary(), lib.declanIntLibrary());

            MyICodeMachine vm = new MyICodeMachine(errLog, icodeOut, errOut, standardInICode);
            vm.interpretICode(program);
            
            if(!opt)
                assertTrue("Expected icode output to be the same as the interpreter output \n\n Interpreter Output is \n\n " + intOut.toString() + " \n\n while icode output is \n\n " + icodeOut.toString(), icodeOut.toString().equals(intOut.toString()));
            
            if(opt) {
                MyOptimizer optt = new MyOptimizer(null, program);
                optt.performCommonSubExpressionElimination();
                optt.performConstantPropogation();
                optt.performDeadCodeElimination();
                optt.performPartialRedundancyElimination();
                optt.performMoveConstants();
                Prog optProg = optt.getICode();
                
                MyICodeMachine optimizedRun = new MyICodeMachine(errLog, optOut, errOut, standardInOpt);
                optimizedRun.interpretICode(optProg);
                
                for(LogItem errItem : errLog){
                    assertTrue(errItem.toString(), false);
                }
                
                assertTrue("Expected optimized output to be the same as the interpreter output \n\n Unoptomized Output is \n\n " + icodeOut.toString() + " \n\n while optimized output is \n\n " + optOut.toString(), optOut.toString().equals(icodeOut.toString()));
            } else {
                for(LogItem errItem : errLog){
                    assertTrue(errItem.toString(), false);
                }
            }
        } catch(FileNotFoundException exp){
            assertTrue(exp.toString(), false);
        } catch (IOException e) {
                assertTrue(e.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        testInterpreterWithICode("src/declan/test/conversions.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testExpressions(){
        testInterpreterWithICode("src/declan/test/expressions.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIfStatementBasic(){
        testInterpreterWithICode("src/declan/test/IfStatementBasic.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIfStatementAdvanced(){
        testInterpreterWithICode("src/declan/test/IfStatementAdvanced.dcl", nullReader, nullReader, nullReader, false);
    }

    
    @Test
    public void testLoops(){
        testInterpreterWithICode("src/declan/test/loops.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testWhileLoopBasic(){
        testInterpreterWithICode("src/declan/test/WhileLoopBasic.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testWhileLoopAdvanced(){
        testInterpreterWithICode("src/declan/test/WhileLoopAdvanced.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopBasic(){
        testInterpreterWithICode("src/declan/test/ForLoopBasic.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopBasic2(){
        testInterpreterWithICode("src/declan/test/ForLoopBasic2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopBasic3(){
        testInterpreterWithICode("src/declan/test/ForLoopBasic3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopAdvanced(){
        testInterpreterWithICode("src/declan/test/ForLoopAdvanced.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRepeatLoopBasic(){
        testInterpreterWithICode("src/declan/test/RepeatLoopBasic.dcl", nullReader, nullReader, nullReader, false);
    }
    @Test
    public void testSample(){
        testInterpreterWithICode("src/declan/test/sample.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest(){
        testInterpreterWithICode("src/declan/test/test.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest2(){
        testInterpreterWithICode("src/declan/test/test2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest3(){
        testInterpreterWithICode("src/declan/test/test3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testSimpleConversion(){
        testInterpreterWithICode("src/declan/test/SingleConversion.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testSimpleConversion2(){
        testInterpreterWithICode("src/declan/test/SingleConversion2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest4(){
        StringReader intReader = new StringReader("2\n");
        StringReader icodeReader = new StringReader("2\n");
        StringReader optReader = new StringReader("2\n");
        testInterpreterWithICode("src/declan/test/test4.dcl", intReader, icodeReader, optReader, false);
    }

    @Test
    public void testBooleanExpression1(){
        testInterpreterWithICode("src/declan/test/BoolExpression1.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testBooleanExpression2(){
        testInterpreterWithICode("src/declan/test/BoolExpression2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition(){
        testInterpreterWithICode("src/declan/test/RealAddition.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition2(){
        testInterpreterWithICode("src/declan/test/RealAddition2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition3(){
        testInterpreterWithICode("src/declan/test/RealAddition3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition4(){
        testInterpreterWithICode("src/declan/test/RealAddition4.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealExpression(){
        testInterpreterWithICode("src/declan/test/RealExpression.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealMultiplication(){
        testInterpreterWithICode("src/declan/test/RealMultiplication.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealMultiplication2(){
        testInterpreterWithICode("src/declan/test/RealMultiplication2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealMultiplication3(){
        testInterpreterWithICode("src/declan/test/RealMultiplication3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIntegerDiv(){
        testInterpreterWithICode("src/declan/test/IntegerDiv.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIntegerDiv2(){
        testInterpreterWithICode("src/declan/test/IntegerDiv2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision(){
        testInterpreterWithICode("src/declan/test/RealDivision.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision2(){
        testInterpreterWithICode("src/declan/test/RealDivision2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision3(){
        testInterpreterWithICode("src/declan/test/RealDivision3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision4(){
        testInterpreterWithICode("src/declan/test/RealDivision4.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision5(){
        testInterpreterWithICode("src/declan/test/RealDivision5.dcl", nullReader, nullReader, nullReader, false);
    }
    
    @Test
    public void testConversionsOpt(){
        testInterpreterWithICode("src/declan/test/conversions.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testExpressionsOpt(){
        testInterpreterWithICode("src/declan/test/expressions.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIfStatementBasicOpt(){
        testInterpreterWithICode("src/declan/test/IfStatementBasic.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIfStatementAdvancedOpt(){
        testInterpreterWithICode("src/declan/test/IfStatementAdvanced.dcl", nullReader, nullReader, nullReader, true);
    }

    
    @Test
    public void testLoopsOpt(){
        testInterpreterWithICode("src/declan/test/loops.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testWhileLoopBasicOpt(){
        testInterpreterWithICode("src/declan/test/WhileLoopBasic.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testWhileLoopAdvancedOpt(){
        testInterpreterWithICode("src/declan/test/WhileLoopAdvanced.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopBasicOpt(){
        testInterpreterWithICode("src/declan/test/ForLoopBasic.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopBasic2Opt(){
        testInterpreterWithICode("src/declan/test/ForLoopBasic2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopBasic3Opt(){
        testInterpreterWithICode("src/declan/test/ForLoopBasic3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopAdvancedOpt(){
        testInterpreterWithICode("src/declan/test/ForLoopAdvanced.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRepeatLoopBasicOpt(){
        testInterpreterWithICode("src/declan/test/RepeatLoopBasic.dcl", nullReader, nullReader, nullReader, true);
    }
    @Test
    public void testSampleOpt(){
        testInterpreterWithICode("src/declan/test/sample.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTestOpt(){
        testInterpreterWithICode("src/declan/test/test.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTest2Opt(){
        testInterpreterWithICode("src/declan/test/test2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTest3Opt(){
        testInterpreterWithICode("src/declan/test/test3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testSimpleConversionOpt(){
        testInterpreterWithICode("src/declan/test/SingleConversion.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testSimpleConversion2Opt(){
        testInterpreterWithICode("src/declan/test/SingleConversion2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTest4Opt(){
        StringReader intReader = new StringReader("2\n");
        StringReader icodeReader = new StringReader("2\n");
        StringReader optReader = new StringReader("2\n");
        testInterpreterWithICode("src/declan/test/test4.dcl", intReader, icodeReader, optReader, true);
    }

    @Test
    public void testBooleanExpression1Opt(){
        testInterpreterWithICode("src/declan/test/BoolExpression1.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testBooleanExpression2Opt(){
        testInterpreterWithICode("src/declan/test/BoolExpression2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAdditionOpt(){
        testInterpreterWithICode("src/declan/test/RealAddition.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAddition2Opt(){
        testInterpreterWithICode("src/declan/test/RealAddition2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAddition3Opt(){
        testInterpreterWithICode("src/declan/test/RealAddition3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAddition4Opt(){
        testInterpreterWithICode("src/declan/test/RealAddition4.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealExpressionOpt(){
        testInterpreterWithICode("src/declan/test/RealExpression.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealMultiplicationOpt(){
        testInterpreterWithICode("src/declan/test/RealMultiplication.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealMultiplication2Opt(){
        testInterpreterWithICode("src/declan/test/RealMultiplication2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealMultiplication3Opt(){
        testInterpreterWithICode("src/declan/test/RealMultiplication3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIntegerDivOpt(){
        testInterpreterWithICode("src/declan/test/IntegerDiv.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIntegerDiv2Opt(){
        testInterpreterWithICode("src/declan/test/IntegerDiv2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivisionOpt(){
        testInterpreterWithICode("src/declan/test/RealDivision.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision2Opt(){
        testInterpreterWithICode("src/declan/test/RealDivision2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision3Opt(){
        testInterpreterWithICode("src/declan/test/RealDivision3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision4Opt(){
        testInterpreterWithICode("src/declan/test/RealDivision4.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision5Opt(){
        testInterpreterWithICode("src/declan/test/RealDivision5.dcl", nullReader, nullReader, nullReader, true);
    }
}
