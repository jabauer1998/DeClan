package io.github.h20man13.DeClan.assembler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import io.github.h20man13.DeClan.main.assembler.ArmAssemblerLexer;
import io.github.h20man13.DeClan.main.assembler.ArmAssemblerParser;
import io.github.h20man13.DeClan.main.assembler.AssemblerVisitor;
import io.github.h20man13.DeClan.main.assembler.ArmAssemblerParser.ProgramContext;

/**
 * Unit test for simple App.
 */
public class TestUtils{
    public static void testAgainstInput(String testString, String expectedOutput){
        StringReader testReader = new StringReader(testString);

        try {
            ANTLRInputStream charStream = new ANTLRInputStream(testReader);
            ArmAssemblerLexer lex = new ArmAssemblerLexer(charStream);
            CommonTokenStream tokStream = new CommonTokenStream(lex);
            
            ArmAssemblerParser parse = new ArmAssemblerParser(tokStream);
            ProgramContext ctx = parse.program();
            
            assertTrue("Error: Parser contains Syntax Errors", parse.getNumberOfSyntaxErrors() == 0);
            assertTrue("Error: Program Context is equal to null", ctx != null);

            AssemblerVisitor assembler = new AssemblerVisitor();
            List<Integer> assembledCode = assembler.assembleCode(ctx);

            StringBuilder resultStringBuilder = new StringBuilder();
            for(Integer result : assembledCode){
                String resultString = Integer.toBinaryString(result);
                int leftOver = 32 - resultString.length();
                for(int i = 0; i < leftOver; i++){
                    resultStringBuilder.append('0');
                }
                resultStringBuilder.append(resultString);
                resultStringBuilder.append("\r\n");
            }
            assertTrue("Error: \n\nExpected -\n" + expectedOutput + "\n but found...\n" + resultStringBuilder.toString(), expectedOutput.equals(resultStringBuilder.toString()));
        } catch(IOException exp){
            assertTrue(exp.toString(), false);
        } 
        
    }
}
