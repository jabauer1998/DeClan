package io.github.H20man13.DeClan.assembler;

import org.junit.Test;

public class SubTest {
    @Test
    public void testSubInstr1(){
        String testString = "SUBS R0, R0, R1";
        String instructionResult = "11100000010100000000000000000001\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }

    @Test
    public void testSubInstr2(){
        String testString = "SUBS R0, R3, R4, ROR #3";
        String instructionResult = "11100000010100110000000111100100\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }

    @Test
    public void testSubInstr3(){
        String testString = "SUB R0, R0, R1";
        String instructionResult = "11100000010000000000000000000001\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }

    @Test
    public void testSubInstr4(){
        String testString = "SUBNES R1, R1, R1";
        
        String instructionResult = "00010000010100010001000000000001\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }

    @Test
    public void testSubInstr5(){
        String testString = "SUBNE R1, R1, R1";
        String instructionResult = "00010000010000010001000000000001\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }
}
