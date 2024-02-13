package io.github.H20man13.DeClan.common.util;

import java.math.BigInteger;

public class OpUtil {
    public static Object negate(Object val){
        if(val instanceof Integer) return -(Integer)val;
        else if(val instanceof Float) return -(Float)val;
        else return null;
    }

    public static Object rNegate(Object val){
        Float dVal = Utils.toReal(val);
        return -dVal;
    }

    public static Object iNegate(Object val){
        Integer iVal = Utils.toInt(val);
        return -iVal;
    }

    public static Object not(Object val){
        return !Utils.toBool(val);
    }

    public static Object bitwiseNot(Object val1){
        return ~Utils.toRawInt(val1);
    }

    public static Object notEqual(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return Utils.toReal(val1) != Utils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) != Utils.toInt(val2);
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return Utils.toBool(val1) != Utils.toBool(val2);
        else return null;
    }

    public static Object equal(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return Utils.toReal(val1) == Utils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) == Utils.toInt(val2);
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return Utils.toBool(val1) == Utils.toBool(val2);
        else return null;
    }

    public static Object plus(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 + (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 + (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 + (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 + (Integer)val2;
        else return null;
    }

    public static Object rAdd(Object val1, Object val2){
        Float rval1 = Utils.toReal(val1);
        Float rval2 = Utils.toReal(val2);
        return rval1 + rval2;
    }

    public static Object iAdd(Object val1, Object val2){
        Integer ival1 = Utils.toInt(val1);
        Integer ival2 = Utils.toInt(val2);
        return ival1 + ival2;
    }

    public static Object minus(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 - (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 - (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 - (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 - (Integer)val2;
        else return null;
    }

    public static Object rSub(Object val1, Object val2){
        Float rval1 = Utils.toReal(val1);
        Float rval2 = Utils.toReal(val2);
        return rval1 - rval2;
    }

    public static Object iSub(Object val1, Object val2){
        Integer ival1 = Utils.toInt(val1);
        Integer ival2 = Utils.toInt(val2);
        return ival1 - ival2;
    }

    public static Object times(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 * (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 * (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 * (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 * (Integer)val2;
        else return null;
    }

    public static Object rMul(Object val1, Object val2){
        Float rVal1 = Utils.toReal(val1);
        Float rVal2 = Utils.toReal(val2);
        return rVal1 * rVal2;
    }

    public static Object iMul(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);

        return iVal1 * iVal2;
    }

    public static Object divide(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 / (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 / (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 / (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return ((Integer)val1).floatValue() / ((Integer)val2).floatValue();
        else return null;
    }

    public static Object div(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return Utils.toInt((Float)val1 / (Float)val2);
        else if(val1 instanceof Integer && val2 instanceof Float) return Utils.toInt((Integer)val1 / (Float)val2);
        else if(val1 instanceof Float && val2 instanceof Integer) return Utils.toInt((Float)val1 / (Integer)val2);
        else if(val1 instanceof Integer && val2 instanceof Integer) return Utils.toInt(((Integer)val1).floatValue() / ((Integer)val2).floatValue());
        else return null;
    }

    public static Object iDiv(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);
        return Utils.toInt(iVal1 / iVal2);
    }

    public static Object rDivide(Object val1, Object val2){
        Float rVal1 = Utils.toReal(val1);
        Float rVal2 = Utils.toReal(val2);
        return rVal1 / rVal2;
    }

    public static Object mod(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 % (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 % (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 % (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 % (Integer)val2;
        else return null;
    }

    public static Object iMod(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);
        return iVal1 % iVal2;
    }

    public static Object lessThan(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return Utils.toReal(val1) < Utils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) < Utils.toInt(val2);
        else return null;
    }

    public static Object greaterThan(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return Utils.toReal(val1) > Utils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) > Utils.toInt(val2);
        else return null;
    }

    public static Object greaterThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return Utils.toReal(val1) >= Utils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) >= Utils.toInt(val2);
        else return null;
    }

    public static Object lessThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return Utils.toReal(val1) <= Utils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) > Utils.toInt(val2);
        else return null;
    }

    public static Object and(Object val1, Object val2){
        return Utils.toBool(val1) && Utils.toBool(val2);
    }

    public static Object bitwiseAnd(Object val1, Object val2){
        Integer int1 = Utils.toRawInt(val1);
        Integer int2 = Utils.toRawInt(val2);

        return int1 & int2;
    }

    public static Object bitwiseOr(Object val1, Object val2){
        Integer int1 = Utils.toRawInt(val1);
        Integer int2 = Utils.toRawInt(val2);
        return int1 | int2;
    }

    public static Object bitwiseXor(Object val1, Object val2){
        Integer int1 = Utils.toRawInt(val1);
        Integer int2 = Utils.toRawInt(val2);
        return int1 ^ int2;
    }

    public static Object leftShift(Object val1, Object val2){
        Integer int1 = Utils.toRawInt(val1);
        Integer int2 = Utils.toRawInt(val2);
        return int1 << int2;
    }

    public static Object rightShift(Object val1, Object val2){
        Integer int1 = Utils.toRawInt(val1);
        Integer int2 = Utils.toRawInt(val2);
        return int1 >> int2;
    }

    public static Object or(Object val1, Object val2){
        return Utils.toBool(val1) || Utils.toBool(val2);
    }
}
