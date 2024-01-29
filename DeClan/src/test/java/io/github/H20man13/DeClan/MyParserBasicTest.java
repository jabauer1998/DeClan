package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Program;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.main.MyDeClanLexer;
import io.github.H20man13.DeClan.main.MyDeClanParser;

public class MyParserBasicTest {
    public void runParserOnSource(String sourcePath){
        File sourceFile = new File(sourcePath);
        assertTrue("Error file " + sourceFile.getAbsolutePath() + " doesnt exist!!", sourceFile.exists());

        
        try {
            FileReader reader = new FileReader(sourceFile);
            Source mySource = new ReaderSource(reader);
            ErrorLog errLog = new ErrorLog();
            MyDeClanLexer lexer = new MyDeClanLexer(mySource, errLog);
            MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
            Program prog = parser.parseProgram();
        } catch(Exception exp){
            assertTrue(exp.toString(), false);
        }
    }

    @Test
    public void testConversions(){
        runParserOnSource("test_source/conversions.dcl");
    }

    @Test
    public void testExpressions(){
        runParserOnSource("test_source/expressions.dcl");
    }

    @Test
    public void testLoops(){
        runParserOnSource("test_source/loops.dcl");
    }

    @Test
    public void testSample(){
        runParserOnSource("test_source/sample.dcl");
    }

    @Test
    public void testTest(){
        runParserOnSource("test_source/test.dcl");
    }

    @Test
    public void testTest2(){
        runParserOnSource("test_source/test2.dcl");
    }

    @Test
    public void testTest3(){
        runParserOnSource("test_source/test3.dcl");
    }

    @Test
    public void testTest4(){
        runParserOnSource("test_source/test4.dcl");
    }

    @Test
    public void testSingleConversion(){
        runParserOnSource("test_source/SingleConversion.dcl");
    }

    @Test
    public void testSingleConversion2(){
        runParserOnSource("test_source/SingleConversion2.dcl");
    }

    @Test
    public void testRealAddition(){
        runParserOnSource("test_source/RealAddition.dcl");
    }
}
