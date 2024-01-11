package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ElaborateReaderSource;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;
import io.github.H20man13.DeClan.main.MyTypeChecker;

public class MyTypeCheckerTest {
    private static void runTypeCheckerOnSource(String fileSource){
        ErrorLog errLog = new ErrorLog();
        try{
            FileReader reader = new FileReader(fileSource);
            Source source = new ElaborateReaderSource(fileSource, reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            
            for(LogItem item: errLog){
                assertTrue(item.toString(), false);
            }

            MyTypeChecker typeChecker = new MyTypeChecker(errLog);
            prog.accept(typeChecker);

            for(LogItem item: errLog){
                assertTrue(item.toString(), false);
            }
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        String fileSource = "test_source/conversions.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testExpressions(){
        String fileSource = "test_source/expressions.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopAdvanced(){
        String fileSource = "test_source/ForLoopAdvanced.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopBasic(){
        String fileSource = "test_source/ForLoopBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopBasic2(){
        String fileSource = "test_source/ForLoopBasic2.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopBasic3(){
        String fileSource = "test_source/ForLoopBasic3.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testIfStatementAdvanced(){
        String fileSource = "test_source/IfStatementAdvanced.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testIfStatementBasic(){
        String fileSource = "test_source/IfStatementBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testLoops(){
        String fileSource = "test_source/loops.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testRepeatLoopBasic(){
        String fileSource = "test_source/RepeatLoopBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testSample(){
        String fileSource = "test_source/sample.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest(){
        String fileSource = "test_source/test.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest2(){
        String fileSource = "test_source/test2.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest3(){
        String fileSource = "test_source/test3.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest4(){
        String fileSource = "test_source/test4.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String fileSource = "test_source/WhileLoopAdvanced.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testWhileLoopBasic(){
        String fileSource = "test_source/WhileLoopBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }
}
