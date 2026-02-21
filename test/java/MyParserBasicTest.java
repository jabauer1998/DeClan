package declan;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import declan.utils.ErrorLog;
import declan.utils.source.ReaderSource;
import declan.utils.source.Source;
import declan.frontend.MyDeClanLexer;
import declan.frontend.MyDeClanParser;

public class MyParserBasicTest {
    public void runParserOnSource(String sourcePath){
        File sourceFile = new File(sourcePath);
        assertTrue("Error file " + sourceFile.getAbsolutePath() + " doesnt exist!!", sourceFile.exists());

        
        try {
            FileReader reader = new FileReader(sourceFile);
            Source mySource = new ReaderSource(reader);
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, null, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            parser.parseProgram();
            parser.close();
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        runParserOnSource("src/declan/test/conversions.dcl");
    }

    @Test
    public void testExpressions(){
        runParserOnSource("src/declan/test/expressions.dcl");
    }

    @Test
    public void testLoops(){
        runParserOnSource("src/declan/test/loops.dcl");
    }

    @Test
    public void testSample(){
        runParserOnSource("src/declan/test/sample.dcl");
    }

    @Test
    public void testTest(){
        runParserOnSource("src/declan/test/test.dcl");
    }

    @Test
    public void testTest2(){
        runParserOnSource("src/declan/test/test2.dcl");
    }

    @Test
    public void testTest3(){
        runParserOnSource("src/declan/test/test3.dcl");
    }

    @Test
    public void testTest4(){
        runParserOnSource("src/declan/test/test4.dcl");
    }

    @Test
    public void testSingleConversion(){
        runParserOnSource("src/declan/test/SingleConversion.dcl");
    }

    @Test
    public void testSingleConversion2(){
        runParserOnSource("src/declan/test/SingleConversion2.dcl");
    }

    @Test
    public void testRealAddition(){
        runParserOnSource("src/declan/test/RealAddition.dcl");
    }

    @Test
    public void testRealAddition2(){
        runParserOnSource("src/declan/test/RealAddition2.dcl");
    }

    @Test
    public void testRealAddition3(){
        runParserOnSource("src/declan/test/RealMultiplication.dcl");
    }

    @Test
    public void testRealMultiplication(){
        runParserOnSource("src/declan/test/RealMultiplication.dcl");
    }

    @Test
    public void testRealMultiplication2(){
        runParserOnSource("src/declan/test/RealMultiplication2.dcl");
    }

    @Test
    public void testIntegerDiv(){
        runParserOnSource("src/declan/test/IntegerDiv.dcl");
    }

    @Test
    public void testIntegerDiv2(){
        runParserOnSource("src/declan/test/IntegerDiv2.dcl");
    }

    @Test
    public void testRealDivision(){
        runParserOnSource("src/declan/test/RealDivision.dcl");
    }

    @Test
    public void testRealDivision2(){
        runParserOnSource("src/declan/test/RealDivision2.dcl");
    }
}
