package io.github.h20man13.DeClan;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import io.github.h20man13.DeClan.common.ErrorLog;
import io.github.h20man13.DeClan.common.ErrorLog.LogItem;
import io.github.h20man13.DeClan.common.ast.Program;
import io.github.h20man13.DeClan.common.icode.Prog;
import io.github.h20man13.DeClan.common.source.ReaderSource;
import io.github.h20man13.DeClan.common.source.Source;
import io.github.h20man13.DeClan.main.MyDeClanLexer;
import io.github.h20man13.DeClan.main.MyDeClanParser;
import io.github.h20man13.DeClan.main.MyICodeMachine;
import io.github.h20man13.DeClan.main.MyInterpreter;
import io.github.h20man13.DeClan.main.MyIrLinker;
import io.github.h20man13.DeClan.main.MyOptimizer;
import io.github.h20man13.DeClan.main.MyStandardLibrary;

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
    	testInterpreterWithICode("test/declan/conversions.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testExpressions(){
        testInterpreterWithICode("test/declan/expressions.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIfStatementBasic(){
        testInterpreterWithICode("test/declan/IfStatementBasic.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIfStatementAdvanced(){
        testInterpreterWithICode("test/declan/IfStatementAdvanced.dcl", nullReader, nullReader, nullReader, false);
    }

    
    @Test
    public void testLoops(){
        testInterpreterWithICode("test/declan/loops.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testWhileLoopBasic(){
        testInterpreterWithICode("test/declan/WhileLoopBasic.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testWhileLoopAdvanced(){
        testInterpreterWithICode("test/declan/WhileLoopAdvanced.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopBasic(){
        testInterpreterWithICode("test/declan/ForLoopBasic.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopBasic2(){
        testInterpreterWithICode("test/declan/ForLoopBasic2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopBasic3(){
        testInterpreterWithICode("test/declan/ForLoopBasic3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testForLoopAdvanced(){
        testInterpreterWithICode("test/declan/ForLoopAdvanced.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRepeatLoopBasic(){
        testInterpreterWithICode("test/declan/RepeatLoopBasic.dcl", nullReader, nullReader, nullReader, false);
    }
    @Test
    public void testSample(){
        testInterpreterWithICode("test/declan/sample.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest(){
        testInterpreterWithICode("test/declan/test.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest2(){
        testInterpreterWithICode("test/declan/test2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest3(){
        testInterpreterWithICode("test/declan/test3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testSimpleConversion(){
        testInterpreterWithICode("test/declan/SingleConversion.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testSimpleConversion2(){
        testInterpreterWithICode("test/declan/SingleConversion2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testTest4(){
        StringReader intReader = new StringReader("2\n");
        StringReader icodeReader = new StringReader("2\n");
        StringReader optReader = new StringReader("2\n");
        testInterpreterWithICode("test/declan/test4.dcl", intReader, icodeReader, optReader, false);
    }

    @Test
    public void testBooleanExpression1(){
        testInterpreterWithICode("test/declan/BoolExpression1.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testBooleanExpression2(){
        testInterpreterWithICode("test/declan/BoolExpression2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition(){
        testInterpreterWithICode("test/declan/RealAddition.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition2(){
        testInterpreterWithICode("test/declan/RealAddition2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition3(){
        testInterpreterWithICode("test/declan/RealAddition3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealAddition4(){
        testInterpreterWithICode("test/declan/RealAddition4.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealExpression(){
        testInterpreterWithICode("test/declan/RealExpression.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealMultiplication(){
        testInterpreterWithICode("test/declan/RealMultiplication.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealMultiplication2(){
        testInterpreterWithICode("test/declan/RealMultiplication2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealMultiplication3(){
        testInterpreterWithICode("test/declan/RealMultiplication3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIntegerDiv(){
        testInterpreterWithICode("test/declan/IntegerDiv.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testIntegerDiv2(){
        testInterpreterWithICode("test/declan/IntegerDiv2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision(){
        testInterpreterWithICode("test/declan/RealDivision.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision2(){
        testInterpreterWithICode("test/declan/RealDivision2.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision3(){
        testInterpreterWithICode("test/declan/RealDivision3.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision4(){
        testInterpreterWithICode("test/declan/RealDivision4.dcl", nullReader, nullReader, nullReader, false);
    }

    @Test
    public void testRealDivision5(){
        testInterpreterWithICode("test/declan/RealDivision5.dcl", nullReader, nullReader, nullReader, false);
    }
    
    @Test
    public void testConversionsOpt(){
    	testInterpreterWithICode("test/declan/conversions.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testExpressionsOpt(){
        testInterpreterWithICode("test/declan/expressions.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIfStatementBasicOpt(){
        testInterpreterWithICode("test/declan/IfStatementBasic.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIfStatementAdvancedOpt(){
        testInterpreterWithICode("test/declan/IfStatementAdvanced.dcl", nullReader, nullReader, nullReader, true);
    }

    
    @Test
    public void testLoopsOpt(){
        testInterpreterWithICode("test/declan/loops.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testWhileLoopBasicOpt(){
        testInterpreterWithICode("test/declan/WhileLoopBasic.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testWhileLoopAdvancedOpt(){
        testInterpreterWithICode("test/declan/WhileLoopAdvanced.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopBasicOpt(){
        testInterpreterWithICode("test/declan/ForLoopBasic.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopBasic2Opt(){
        testInterpreterWithICode("test/declan/ForLoopBasic2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopBasic3Opt(){
        testInterpreterWithICode("test/declan/ForLoopBasic3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testForLoopAdvancedOpt(){
        testInterpreterWithICode("test/declan/ForLoopAdvanced.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRepeatLoopBasicOpt(){
        testInterpreterWithICode("test/declan/RepeatLoopBasic.dcl", nullReader, nullReader, nullReader, true);
    }
    @Test
    public void testSampleOpt(){
        testInterpreterWithICode("test/declan/sample.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTestOpt(){
        testInterpreterWithICode("test/declan/test.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTest2Opt(){
        testInterpreterWithICode("test/declan/test2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTest3Opt(){
        testInterpreterWithICode("test/declan/test3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testSimpleConversionOpt(){
        testInterpreterWithICode("test/declan/SingleConversion.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testSimpleConversion2Opt(){
        testInterpreterWithICode("test/declan/SingleConversion2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testTest4Opt(){
        StringReader intReader = new StringReader("2\n");
        StringReader icodeReader = new StringReader("2\n");
        StringReader optReader = new StringReader("2\n");
        testInterpreterWithICode("test/declan/test4.dcl", intReader, icodeReader, optReader, true);
    }

    @Test
    public void testBooleanExpression1Opt(){
        testInterpreterWithICode("test/declan/BoolExpression1.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testBooleanExpression2Opt(){
        testInterpreterWithICode("test/declan/BoolExpression2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAdditionOpt(){
        testInterpreterWithICode("test/declan/RealAddition.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAddition2Opt(){
        testInterpreterWithICode("test/declan/RealAddition2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAddition3Opt(){
        testInterpreterWithICode("test/declan/RealAddition3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealAddition4Opt(){
        testInterpreterWithICode("test/declan/RealAddition4.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealExpressionOpt(){
        testInterpreterWithICode("test/declan/RealExpression.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealMultiplicationOpt(){
        testInterpreterWithICode("test/declan/RealMultiplication.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealMultiplication2Opt(){
        testInterpreterWithICode("test/declan/RealMultiplication2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealMultiplication3Opt(){
        testInterpreterWithICode("test/declan/RealMultiplication3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIntegerDivOpt(){
        testInterpreterWithICode("test/declan/IntegerDiv.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testIntegerDiv2Opt(){
        testInterpreterWithICode("test/declan/IntegerDiv2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivisionOpt(){
        testInterpreterWithICode("test/declan/RealDivision.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision2Opt(){
        testInterpreterWithICode("test/declan/RealDivision2.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision3Opt(){
        testInterpreterWithICode("test/declan/RealDivision3.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision4Opt(){
        testInterpreterWithICode("test/declan/RealDivision4.dcl", nullReader, nullReader, nullReader, true);
    }

    @Test
    public void testRealDivision5Opt(){
        testInterpreterWithICode("test/declan/RealDivision5.dcl", nullReader, nullReader, nullReader, true);
    }
}
