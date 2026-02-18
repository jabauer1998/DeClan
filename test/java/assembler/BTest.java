package declan.assembler;

import org.junit.Test;

public class BTest {
    @Test
    public void testBranchLoop(){
        String testString = "a: B b\n"
                          + "b: B a\n"
                          + "STP\n";
        String result = "11101010000000000000000000000100\r\n" + //
                        "11101010000000000000000000000000\r\n" + //
                        "00000110000000000000000000010000\r\n";
                        
        TestUtils.testAgainstInput(testString, result);
    }
}
