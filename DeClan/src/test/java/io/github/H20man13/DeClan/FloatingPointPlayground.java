package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.github.H20man13.DeClan.common.util.Utils;

public class FloatingPointPlayground {
    @Test
    public void testFloatingPointNegativeAndPositive(){
        Float f1 = 0.5f;
        Float f2 = -0.5f;

        String binF1 = Utils.to32BitBinary(f1);
        String binF2 = Utils.to32BitBinary(f2);

        String binF1Result = "00111111000000000000000000000000";
        String binF2Result = "10111111000000000000000000000000";

        //Check to see that the 31st bit flips when converting from negative to positive
        assertTrue("Error expected \n" + binF1Result + "\n but found \n" + binF1, binF1.equals(binF1Result));
        assertTrue("Error expected \n" + binF2Result + "\n but found \n" + binF2, binF2.equals(binF2Result));
    }

    @Test
    public void testFloatingPointNegativeAndPositive2(){
        Float f1 = 5.0f;
        Float f2 = 5.0f;

        String binF1 = Utils.to32BitBinary(f1);
        String binF2 = Utils.to32BitBinary(f2);

        String binF1Result = "01000000101000000000000000000000";
        String binF2Result = "01000000101000000000000000000000";

        assertTrue("Error expected \n" + binF1Result + "\n but found \n" + binF1, binF1.equals(binF1Result));
        assertTrue("Error expected \n" + binF2Result + "\n but found \n" + binF2, binF2.equals(binF2Result));
    }

    @Test
    public void testFloatingPointNegativeAndPositiveAddition(){
        Float f1 = 50.0f;
        Float f2 = 92.0f;

        Float result = f1 - f2;

        String arg1Bin = Utils.to32BitBinary(f1);
        String arg2Bin = Utils.to32BitBinary(f2);

        String operationBin = Utils.to32BitBinary(result);

        String actualBinArg1 = "01000010010010000000000000000000";
        String actualBinArg2 = "01000010101110000000000000000000";

        assertTrue("Error expected \n" + actualBinArg1 + "\n but found \n" + arg1Bin, actualBinArg1.equals(arg1Bin));
        assertTrue("Error expected \n" + actualBinArg2 + "\n but found \n" + arg2Bin, actualBinArg2.equals(arg2Bin));


        String resultBin = "11000010001010000000000000000000";
        String actualResult = "101011000000000000000000";

        assertTrue("Error expected \n" + resultBin + "\n but found \n" + operationBin, resultBin.equals(operationBin));
    }

    @Test
    public void testFloatingPointMultiplication(){
        Float f1 = 21.0f;
        Float f2 = 2.0f;

        Float result = f1 * f2;

        String f1AsBin = Utils.to32BitBinary(f1);
        String f2AsBin = Utils.to32BitBinary(f2);

        String f1ExpectedBin = "01000001101010000000000000000000";
        String f2ExpectedBin = "01000000000000000000000000000000";

        assertTrue("Expected \n" + f1ExpectedBin + "\n but found \n" + f1AsBin, f1ExpectedBin.equals(f1AsBin));
        assertTrue("Expected \n" + f2ExpectedBin + "\n but found \n" + f2AsBin, f2ExpectedBin.equals(f2AsBin));

        String resultBin = Utils.to32BitBinary(result);
        String expectedResult = "01000010001010000000000000000000";
        
        Float actual = Float.parseFloat("3.5232154E8");
        String actualBinary = Utils.to32BitBinary(actual);

        String actualStr = "01001101101010000000000000000000";

        assertTrue("Expected \n" + actualStr + "\n but found \n" + actualBinary, actualStr.equals(actualBinary));

        assertTrue("Expected \n" + expectedResult + "\n but found \n" + resultBin, expectedResult.equals(resultBin));
    }
}
