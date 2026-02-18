package declan;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;

import org.junit.Test;

import declan.utils.ErrorLog;
import declan.utils.ErrorLog.LogItem;
import declan.frontend.ast.Program;
import declan.utils.source.ElaborateReaderSource;
import declan.utils.source.Source;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;
import declan.frontend.MyTypeChecker;

public class MyTypeCheckerTest {
    private static void runTypeCheckerOnSource(String fileSource){
        ErrorLog errLog = new ErrorLog();
        try{
            FileReader reader = new FileReader(fileSource);
            Source source = new ElaborateReaderSource(fileSource, reader);
            MyDeClanLexer lexer = new MyDeClanLexer(source, null, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
            parser.close();
            
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
        String fileSource = "src/declan/test/declan/conversions.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testExpressions(){
        String fileSource = "src/declan/test/declan/expressions.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopAdvanced(){
        String fileSource = "src/declan/test/declan/ForLoopAdvanced.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopBasic(){
        String fileSource = "src/declan/test/declan/ForLoopBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopBasic2(){
        String fileSource = "src/declan/test/declan/ForLoopBasic2.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testForLoopBasic3(){
        String fileSource = "src/declan/test/declan/ForLoopBasic3.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testIfStatementAdvanced(){
        String fileSource = "src/declan/test/declan/IfStatementAdvanced.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testIfStatementBasic(){
        String fileSource = "src/declan/test/declan/IfStatementBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testLoops(){
        String fileSource = "src/declan/test/declan/loops.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testRepeatLoopBasic(){
        String fileSource = "src/declan/test/declan/RepeatLoopBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testSample(){
        String fileSource = "src/declan/test/declan/sample.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest(){
        String fileSource = "src/declan/test/declan/test.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest2(){
        String fileSource = "src/declan/test/declan/test2.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest3(){
        String fileSource = "src/declan/test/declan/test3.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testTest4(){
        String fileSource = "src/declan/test/declan/test4.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testWhileLoopAdvanced(){
        String fileSource = "src/declan/test/declan/WhileLoopAdvanced.dcl";
        runTypeCheckerOnSource(fileSource);
    }

    @Test
    public void testWhileLoopBasic(){
        String fileSource = "src/declan/test/declan/WhileLoopBasic.dcl";
        runTypeCheckerOnSource(fileSource);
    }
}
