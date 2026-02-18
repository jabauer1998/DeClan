package declan.assembler;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class MovTest{
    @Test
    public void testMovInstr1(){
        String testString = "MOV R0, #255";
        String instructionString = "11100011101000000000000011111111\r\n";
        TestUtils.testAgainstInput(testString, instructionString);
    }

    @Test
    public void testMovInstr2(){
        String testString = "MOVS R0, #-1";
        String instructionString = "11100011101100000000000000000000\r\n";
        TestUtils.testAgainstInput(testString, instructionString);
    }

    @Test
    public void testMovInstr3(){
        String testString = "MOV R1, R2, ROR #127";
        String instructionResult = "11100001101000000011111111100010\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }

    @Test
    public void testMovInstr4(){
        String testString = "MOV R2, #12";
        String instructionResult = "11100011101000000010000000001100\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }

    @Test
    public void testMovInstr5(){
        String testString = "MOV R2, R1, ASR R3";
        String instructionResult = "11100001101000000010001101010001\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }
}
