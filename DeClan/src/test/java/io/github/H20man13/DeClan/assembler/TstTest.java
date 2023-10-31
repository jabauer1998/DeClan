package io.github.H20man13.DeClan.assembler;

import org.junit.Test;

public class TstTest {
    @Test
    public void testTstContainsBit20ByDefault(){
        String testString = "x: .WORD 50\r\n" +
                            "y: .WORD 50\r\n" +
                            "LDR R0, x\r\n" +
                            "LDR R1, y\r\n" +
                            "TST R0, R1\r\n" +
                            "STP\r\n";
        String answer = "";
        TestUtils.testAgainstInput(testString, answer);
    }
}
