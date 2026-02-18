package io.github.h20man13.DeClan.assembler;

import org.junit.Test;

public class BXTest {
    @Test
    public void testBxInstr1(){
        String testString = "BXGT R2";
        String instructionOutput = "11000001001011111111111100010010\r\n";
        TestUtils.testAgainstInput(testString, instructionOutput);
    }

    @Test
    public void testBxInstr2(){
        String testString = "BX R2";
        String instructionOutput = "11100001001011111111111100010010\r\n";
        TestUtils.testAgainstInput(testString, instructionOutput);
    }
}
