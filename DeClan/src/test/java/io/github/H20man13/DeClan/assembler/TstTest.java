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
        String answer = "00000000000000000000000000110010\r\n" + //
                        "00000000000000000000000000110010\r\n" + //
                        "11100101000111110000000000001100\r\n" + //
                        "11100101000111110001000000001100\r\n" + //
                        "11100001000100000000000000000001\r\n" + //
                        "00000110000000000000000000010000\r\n";
        TestUtils.testAgainstInput(testString, answer);
    }
}
