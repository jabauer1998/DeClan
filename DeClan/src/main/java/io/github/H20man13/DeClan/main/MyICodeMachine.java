package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.OpUtil;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.symboltable.Environment;

public class MyICodeMachine {
    Environment<String, Object> results;
    
    public MyICodeMachine(){
        results = new Environment<String, Object>();
        results.addScope();
    }

    public MyICodeMachine(Environment<String, Object> results){
        this.results = results;
    }

    public void interpretLetBool(LetBool bool){
        String place = bool.place;
        boolean value = bool.value;
        results.addEntry(place, value);
    }

    public void interpretLetString(LetString string){
        String place = string.place;
        String value = string.value;
        results.addEntry(place, value);
    }

    public void interpretLetInt(LetInt integer){
        String place = integer.place;
        int value = integer.value;
        results.addEntry(place, value);
    }

    public void interpretLetReal(LetReal real){
        String place = real.place;
        double value = real.value;
        results.addEntry(place, value);
    }

    public void interpretLetVar(LetVar var){
        String place = var.place;
        String objVar = var.var;
        Object objVal = results.getEntry(objVar);
        results.addEntry(place, objVal);
    }

    public void interpretLetUn(LetUn var){
        String place = var.place;
        
        String locVar = var.value;
        Object val = results.getEntry(locVar);

        LetUn.Op op = var.op;

        switch(op){
            case INEG:
                Object val1 = OpUtil.negate(val);
                results.addEntry(locVar, val1);
                break;
            case RNEG:
                Object val2 = OpUtil.negate(val);
                results.addEntry(place, val2);
                break;
            case BNOT:
                Object val3 = OpUtil.not(val);
                results.addEntry(place, val3);
                break;
            default:
                results.addEntry(place, val);
                break;
        } 
    }

    public void interpretLetBin(LetBin var){
        String place = var.place;
        
        String left = var.left;
        Object leftVal = results.getEntry(left);

        String right = var.right;
        Object rightVal = results.getEntry(right);

        LetBin.Op op = var.op;

        switch(op){
            case BAND:
                //Not implemented Yet
                break;
            case BOR:
                //Not implemented yet
                break;
            case IDIV:
                Object val1 = OpUtil.divide(leftVal, rightVal);
                results.addEntry(place, val1);
                break;
            case IADD:
                Object val2 = OpUtil.plus(leftVal, rightVal);
                results.addEntry(place, val2);
                break;
            case ISUB:
                Object val3 = OpUtil.minus(leftVal, rightVal);
                results.addEntry(place, val3);
                break;
            case IMUL:
                Object val4 = OpUtil.times(leftVal, rightVal);
                results.addEntry(place, val4);
                break;
            case IGE:
                Object val5 = OpUtil.greaterThanOrEqualTo(leftVal, rightVal);
                results.addEntry(place, val5);
                break;
            case IGT:
                Object val6 = OpUtil.greaterThan(leftVal, rightVal);
                results.addEntry(place, val6);
                break;
            case INE:
                Object val7 = OpUtil.notEqual(leftVal, rightVal);
                results.addEntry(place, val7);
                break;
            case ILT:
                Object val8 = OpUtil.lessThan(leftVal, rightVal);
                results.addEntry(place, val8);
                break;
            case ILE:
                Object val9 = OpUtil.lessThanOrEqualTo(leftVal, rightVal);
                results.addEntry(place, val9);
                break;
            default:
                break; 
        }
    }




}
