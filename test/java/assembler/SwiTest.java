package declan.assembler;

import org.junit.Test;

public class SwiTest {
    @Test
    public void testSwiInstr(){
        String testString = "SWI 1";
        String instructionResult = "11101111000000000000000000000001\r\n";
        TestUtils.testAgainstInput(testString, instructionResult);
    }
}
