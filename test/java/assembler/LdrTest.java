package declan.assembler;

import org.junit.Test;

public class LdrTest {
    @Test
    public void testBasicLoad(){
        String input = "B begin\n"+
                       "a: .WORD 255\n"+
                       "begin: LDR R1, a\n"+
                       "STP\n";
        String expectedOutput = "11101010000000000000000000001000\r\n" + //
                                "00000000000000000000000011111111\r\n" + //
                                "11100101000111110001000000001000\r\n" + //
                                "00000110000000000000000000010000\r\n";
        TestUtils.testAgainstInput(input, expectedOutput);
   }

   @Test
   public void testAdvancedLoad1(){
        String input = "B begin\n"
                 + "a: .WORD 255\n"
                 + "begin: LDR R1, [R14]\n"
                 + "STP\n";
        String expectedOutput = "11101010000000000000000000001000\r\n" + //
                                "00000000000000000000000011111111\r\n" + //
                                "11100101000111100001000000000000\r\n" + //
                                "00000110000000000000000000010000\r\n";
        TestUtils.testAgainstInput(input, expectedOutput);
   }

   @Test
   public void testAdvancedLoad2(){
       String input = "B begin\r\n"
                    + ".WORD 2\r\n"
                    + "begin: MOV R1, #0\r\n"
                    + "LDR R0, [R1,#4]\r\n"
                    + "SWI 1\r\n"
                    + "STP\r\n";
       String expectedOutput = "11101010000000000000000000001000\r\n" + //
                               "00000000000000000000000000000010\r\n" + //
                               "11100011101000000001000000000000\r\n" + //
                               "11100101100100010000000000000100\r\n" + //
                               "11101111000000000000000000000001\r\n" + //
                               "00000110000000000000000000010000\r\n";
       TestUtils.testAgainstInput(input, expectedOutput);
   }

   @Test
   public void testAdvancedLoad3(){
          String assembly = "B begin\r\n"
                          + ".WORD 530\r\n"
                          + "offset: .WORD 12\r\n"
                          + "base: .WORD 4\r\n"
                          + ".WORD 1040\r\n"
                          + "begin: LDR R1, offset\r\n"
                          + "LDR R2, base\r\n"
                          + "LDR R0, [R2, +R1]\r\n"
                          + "SWI 1\r\n"
                          + "STP\r\n";
          String expectedOutput = "11101010000000000000000000010100\r\n" + //
                                  "00000000000000000000001000010010\r\n" + //
                                  "00000000000000000000000000001100\r\n" + //
                                  "00000000000000000000000000000100\r\n" + //
                                  "00000000000000000000010000010000\r\n" + //
                                  "11100101000111110001000000010000\r\n" + //
                                  "11100101000111110010000000010000\r\n" + //
                                  "11100111100100100000000000000001\r\n" + //
                                  "11101111000000000000000000000001\r\n" + //
                                  "00000110000000000000000000010000\r\n";
          TestUtils.testAgainstInput(assembly, expectedOutput);
   }
}
