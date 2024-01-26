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
}
