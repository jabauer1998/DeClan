package io.github.H20man13.DeClan.common.util;

public class OpUtil {
    public static Object negate(Object val){
        if(val instanceof Integer) return -(Integer)val;
        else if(val instanceof Double) return -(Double)val;
        else return null;
    }

    public static Object rNegate(Object val){
        Double dVal = Utils.toDouble(val);
        return -dVal;
    }

    public static Object iNegate(Object val){
        Integer iVal = Utils.toInt(val);
        return -iVal;
    }

    public static Object not(Object val){
        return !Utils.toBool(val);
    }

    public static Object notEqual(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return Utils.toDouble(val1) != Utils.toDouble(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) != Utils.toInt(val2);
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return Utils.toBool(val1) != Utils.toBool(val2);
        else return null;
    }

    public static Object equal(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return Utils.toDouble(val1) == Utils.toDouble(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) == Utils.toInt(val2);
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return Utils.toBool(val1) == Utils.toBool(val2);
        else return null;
    }

    public static Object plus(Object val1, Object val2){
        if(val1 instanceof Double && val2 instanceof Double) return (Double)val1 + (Double)val2;
        else if(val1 instanceof Integer && val2 instanceof Double) return (Integer)val1 + (Double)val2;
        else if(val1 instanceof Double && val2 instanceof Integer) return (Double)val1 + (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 + (Integer)val2;
        else return null;
    }

    public static Object rAdd(Object val1, Object val2){
        Double rval1 = Utils.toDouble(val1);
        Double rval2 = Utils.toDouble(val2);
        return rval1 + rval2;
    }

    public static Object iAdd(Object val1, Object val2){
        Integer ival1 = Utils.toInt(val1);
        Integer ival2 = Utils.toInt(val2);
        return ival1 + ival2;
    }

    public static Object minus(Object val1, Object val2){
        if(val1 instanceof Double && val2 instanceof Double) return (Double)val1 - (Double)val2;
        else if(val1 instanceof Integer && val2 instanceof Double) return (Integer)val1 - (Double)val2;
        else if(val1 instanceof Double && val2 instanceof Integer) return (Double)val1 - (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 - (Integer)val2;
        else return null;
    }

    public static Object rSub(Object val1, Object val2){
        Double rval1 = Utils.toDouble(val1);
        Double rval2 = Utils.toDouble(val2);
        return rval1 - rval2;
    }

    public static Object iSub(Object val1, Object val2){
        Integer ival1 = Utils.toInt(val1);
        Integer ival2 = Utils.toInt(val2);
        return ival1 - ival2;
    }

    public static Object times(Object val1, Object val2){
        if(val1 instanceof Double && val2 instanceof Double) return (Double)val1 * (Double)val2;
        else if(val1 instanceof Integer && val2 instanceof Double) return (Integer)val1 * (Double)val2;
        else if(val1 instanceof Double && val2 instanceof Integer) return (Double)val1 * (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 * (Integer)val2;
        else return null;
    }

    public static Object rMul(Object val1, Object val2){
        Double rVal1 = Utils.toDouble(val1);
        Double rVal2 = Utils.toDouble(val2);
        return rVal1 * rVal2;
    }

    public static Object iMul(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);
        return iVal1 * iVal2;
    }

    public static Object divide(Object val1, Object val2){
        if(val1 instanceof Double && val2 instanceof Double) return (Double)val1 / (Double)val2;
        else if(val1 instanceof Integer && val2 instanceof Double) return (Integer)val1 / (Double)val2;
        else if(val1 instanceof Double && val2 instanceof Integer) return (Double)val1 / (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 / (Integer)val2;
        else return null;
    }

    public static Object div(Object val1, Object val2){
        if(val1 instanceof Double && val2 instanceof Double) return Utils.toInt((Double)val1 / (Double)val2);
        else if(val1 instanceof Integer && val2 instanceof Double) return Utils.toInt((Integer)val1 / (Double)val2);
        else if(val1 instanceof Double && val2 instanceof Integer) return Utils.toInt((Double)val1 / (Integer)val2);
        else if(val1 instanceof Integer && val2 instanceof Integer) return Utils.toInt((Integer)val1 / (Integer)val2);
        else return null;
    }

    public static Object rDiv(Object val1, Object val2){
        Double rVal1 = Utils.toDouble(val1);
        Double rVal2 = Utils.toDouble(val2);
        return Utils.toInt(rVal1 / rVal2);
    }

    public static Object iDiv(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);
        return Utils.toInt(iVal1 / iVal2);
    }

    public static Object rDivide(Object val1, Object val2){
        Double rVal1 = Utils.toDouble(val1);
        Double rVal2 = Utils.toDouble(val2);
        return rVal1 / rVal2;
    }

    public static Object iDivide(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);
        return iVal1 / iVal2;
    }

    public static Object mod(Object val1, Object val2){
        if(val1 instanceof Double && val2 instanceof Double) return (Double)val1 % (Double)val2;
        else if(val1 instanceof Integer && val2 instanceof Double) return (Integer)val1 % (Double)val2;
        else if(val1 instanceof Double && val2 instanceof Integer) return (Double)val1 % (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 % (Integer)val2;
        else return null;
    }

    public static Object iMod(Object val1, Object val2){
        Integer iVal1 = Utils.toInt(val1);
        Integer iVal2 = Utils.toInt(val2);
        return iVal1 % iVal2;
    }

    public static Object lessThan(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return Utils.toDouble(val1) < Utils.toDouble(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) < Utils.toInt(val2);
        else return null;
    }

    public static Object greaterThan(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return Utils.toDouble(val1) > Utils.toDouble(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) > Utils.toInt(val2);
        else return null;
    }

    public static Object greaterThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return Utils.toDouble(val1) >= Utils.toDouble(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) >= Utils.toInt(val2);
        else return null;
    }

    public static Object lessThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return Utils.toDouble(val1) <= Utils.toDouble(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return Utils.toInt(val1) > Utils.toInt(val2);
        else return null;
    }

    public static Object and(Object val1, Object val2){
        return Utils.toBool(val1) && Utils.toBool(val2);
    }

    public static Object or(Object val1, Object val2){
        return Utils.toBool(val1) || Utils.toBool(val2);
    }
}
