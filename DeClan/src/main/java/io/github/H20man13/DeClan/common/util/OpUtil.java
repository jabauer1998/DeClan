package io.github.H20man13.DeClan.common.util;

public class OpUtil {
    public static Object negate(Object val){
        if(val instanceof Integer) return -(Integer)val;
        else if(val instanceof Double) return -(Double)val;
        else return null;
    }

    public static Object not(Object val){
        if(val instanceof Boolean) return !(Boolean)val;
        else if(val instanceof Integer) return !((Integer)val != 0);
        else if(val instanceof Double) return !((Double) val != 0);
        else return null;
    }

    public static Object notEqual(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 != (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 != (Integer)val2;
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return (Boolean)val1 != (Boolean)val2;
        else return null;
    }

    public static Object equal(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 == (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 == (Integer)val2;
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return (Boolean)val1 == (Boolean)val2;
        else return null;
    }

    public static Object plus(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 + (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 + (Integer)val2;
        else return null;
    }

    public static Object minus(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 - (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 - (Integer)val2;
        else return null;
    }

    public static Object times(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 * (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 * (Integer)val2;
        else return null;
    }

    public static Object divide(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 / (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 / (Integer)val2;
        else return null;
    }

    public static Object lessThan(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 < (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 < (Integer)val2;
        else return null;
    }

    public static Object greaterThan(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 > (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 > (Integer)val2;
        else return null;
    }

    public static Object greaterThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 >= (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 >= (Integer)val2;
        else return null;
    }

    public static Object lessThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Double || val2 instanceof Double) return (Double)val1 <= (Double)val2;
        else if(val1 instanceof Integer || val2 instanceof Integer) return (Integer)val1 <= (Integer)val2;
        else return null;
    }

    public static Object and(Object val1, Object val2){
        if(val1 instanceof Boolean && val2 instanceof Boolean) return (Boolean)val1 && (Boolean)val2;
        else return null;
    }

    public static Object or(Object val1, Object val2){
        if(val1 instanceof Boolean && val2 instanceof Boolean) return (Boolean)val1 || (Boolean)val2;
        else return null;
    }
}
