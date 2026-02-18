package io.github.h20man13.DeClan.common.util;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.ast.Expression;
import io.github.h20man13.DeClan.common.ast.FunctionCall;
import io.github.h20man13.DeClan.common.ast.Identifier;
import io.github.h20man13.DeClan.common.exception.ConversionException;
import io.github.h20man13.DeClan.common.exception.OperationException;
import io.github.h20man13.DeClan.common.position.Position;

public class OpUtil {
    public static Object negate(Object val){
        if(val instanceof Integer) return -(Integer)val;
        else if(val instanceof Float) return -(Float)val;
        else throw new OperationException("negate", val.getClass().getName());
    }

    public static Object rNegate(Object val){
        Float dVal = ConversionUtils.toReal(val);
        return -dVal;
    }

    public static Object iNegate(Object val){
        Integer iVal = ConversionUtils.toInt(val);
        return -iVal;
    }

    public static Object not(Object val){
        return !ConversionUtils.toBool(val);
    }

    public static Object bitwiseNot(Object val1){
        return ~ConversionUtils.toRawInt(val1);
    }

    public static Object notEqual(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return !ConversionUtils.toReal(val1).equals(ConversionUtils.toReal(val2));
        else if(val1 instanceof Integer || val2 instanceof Integer) return !ConversionUtils.toInt(val1).equals(ConversionUtils.toInt(val2));
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return !ConversionUtils.toBool(val1).equals(ConversionUtils.toBool(val2));
        else throw new OperationException("notEqual", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object equal(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return ConversionUtils.toReal(val1).equals(ConversionUtils.toReal(val2));
        else if(val1 instanceof Integer || val2 instanceof Integer) return ConversionUtils.toInt(val1).equals(ConversionUtils.toInt(val2));
        else if(val1 instanceof Boolean || val2 instanceof Boolean) return ConversionUtils.toBool(val1).equals(ConversionUtils.toBool(val2));
        else throw new OperationException("equal", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object plus(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 + (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 + (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 + (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 + (Integer)val2;
        else throw new OperationException("plus", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object rAdd(Object val1, Object val2){
        Float rval1 = ConversionUtils.toReal(val1);
        Float rval2 = ConversionUtils.toReal(val2);
        return rval1 + rval2;
    }

    public static Object iAdd(Object val1, Object val2){
        Integer ival1 = ConversionUtils.toInt(val1);
        Integer ival2 = ConversionUtils.toInt(val2);
        return ival1 + ival2;
    }

    public static Object minus(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 - (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 - (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 - (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 - (Integer)val2;
        else throw new OperationException("minus", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object rSub(Object val1, Object val2){
        Float rval1 = ConversionUtils.toReal(val1);
        Float rval2 = ConversionUtils.toReal(val2);
        return rval1 - rval2;
    }

    public static Object iSub(Object val1, Object val2){
        Integer ival1 = ConversionUtils.toInt(val1);
        Integer ival2 = ConversionUtils.toInt(val2);
        return ival1 - ival2;
    }

    public static Object times(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 * (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 * (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 * (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 * (Integer)val2;
        else throw new OperationException("times", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object rMul(Object val1, Object val2){
        Float rVal1 = ConversionUtils.toReal(val1);
        Float rVal2 = ConversionUtils.toReal(val2);
        return rVal1 * rVal2;
    }

    public static Object iMul(Object val1, Object val2){
        Integer iVal1 = ConversionUtils.toInt(val1);
        Integer iVal2 = ConversionUtils.toInt(val2);

        return iVal1 * iVal2;
    }

    public static Object divide(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 / (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 / (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 / (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return ((Integer)val1).floatValue() / ((Integer)val2).floatValue();
        else throw new OperationException("divide", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object div(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return ConversionUtils.toInt((Float)val1 / (Float)val2);
        else if(val1 instanceof Integer && val2 instanceof Float) return ConversionUtils.toInt((Integer)val1 / (Float)val2);
        else if(val1 instanceof Float && val2 instanceof Integer) return ConversionUtils.toInt((Float)val1 / (Integer)val2);
        else if(val1 instanceof Integer && val2 instanceof Integer) return ConversionUtils.toInt(((Integer)val1).floatValue() / ((Integer)val2).floatValue());
        else throw new OperationException("div", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object iDiv(Object val1, Object val2){
        Integer iVal1 = ConversionUtils.toInt(val1);
        Integer iVal2 = ConversionUtils.toInt(val2);
        return ConversionUtils.toInt(iVal1 / iVal2);
    }

    public static Object rDivide(Object val1, Object val2){
        Float rVal1 = ConversionUtils.toReal(val1);
        Float rVal2 = ConversionUtils.toReal(val2);
        return rVal1 / rVal2;
    }

    public static Object mod(Object val1, Object val2){
        if(val1 instanceof Float && val2 instanceof Float) return (Float)val1 % (Float)val2;
        else if(val1 instanceof Integer && val2 instanceof Float) return (Integer)val1 % (Float)val2;
        else if(val1 instanceof Float && val2 instanceof Integer) return (Float)val1 % (Integer)val2;
        else if(val1 instanceof Integer && val2 instanceof Integer) return (Integer)val1 % (Integer)val2;
        else throw new OperationException("mod", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object iMod(Object val1, Object val2){
        Integer iVal1 = ConversionUtils.toInt(val1);
        Integer iVal2 = ConversionUtils.toInt(val2);
        return iVal1 % iVal2;
    }

    public static Object lessThan(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return ConversionUtils.toReal(val1) < ConversionUtils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return ConversionUtils.toInt(val1) < ConversionUtils.toInt(val2);
        else throw new OperationException("lessThan", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object greaterThan(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return ConversionUtils.toReal(val1) > ConversionUtils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return ConversionUtils.toInt(val1) > ConversionUtils.toInt(val2);
        else throw new OperationException("greaterThan", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object greaterThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return ConversionUtils.toReal(val1) >= ConversionUtils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return ConversionUtils.toInt(val1) >= ConversionUtils.toInt(val2);
        else throw new OperationException("greaterThanOrEqualTo", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object lessThanOrEqualTo(Object val1, Object val2){
        if(val1 instanceof Float || val2 instanceof Float) return ConversionUtils.toReal(val1) <= ConversionUtils.toReal(val2);
        else if(val1 instanceof Integer || val2 instanceof Integer) return ConversionUtils.toInt(val1) > ConversionUtils.toInt(val2);
        else throw new OperationException("lessThanOrEqualTo", val1.getClass().getName(), val2.getClass().getName());
    }

    public static Object and(Object val1, Object val2){
        return ConversionUtils.toBool(val1) && ConversionUtils.toBool(val2);
    }

    public static Object bitwiseAnd(Object val1, Object val2){
        Integer int1 = ConversionUtils.toRawInt(val1);
        Integer int2 = ConversionUtils.toRawInt(val2);

        return int1 & int2;
    }

    public static Object bitwiseOr(Object val1, Object val2){
        Integer int1 = ConversionUtils.toRawInt(val1);
        Integer int2 = ConversionUtils.toRawInt(val2);
        return int1 | int2;
    }

    public static Object bitwiseXor(Object val1, Object val2){
        Integer int1 = ConversionUtils.toRawInt(val1);
        Integer int2 = ConversionUtils.toRawInt(val2);
        return int1 ^ int2;
    }

    public static Object leftShift(Object val1, Object val2){
        Integer int1 = ConversionUtils.toRawInt(val1);
        Integer int2 = ConversionUtils.toRawInt(val2);
        return int1 << int2;
    }

    public static Object rightShift(Object val1, Object val2){
        Integer int1 = ConversionUtils.toRawInt(val1);
        Integer int2 = ConversionUtils.toRawInt(val2);
        return int1 >> int2;
    }

    public static Object or(Object val1, Object val2){
        return ConversionUtils.toBool(val1) || ConversionUtils.toBool(val2);
    }
    
    public static FunctionCall binaryOpFunction(Position pos, String name, Expression val1, Expression val2) {
    	List<Expression> exps = new LinkedList<Expression>();
    	exps.add(val1);
    	exps.add(val2);
    	return new FunctionCall(pos, new Identifier(pos, name), exps);
    }
    
    public static FunctionCall unaryOpFunction(Position pos, String name, Expression val1) {
    	List<Expression> exps = new LinkedList<Expression>();
    	exps.add(val1);
    	return new FunctionCall(pos, new Identifier(pos, name), exps);
    }
}
