package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.util.OpUtil;

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
        BoolExp value = bool.value;
        results.addEntry(place, value.trueFalse);
    }

    public void interpretLetString(LetString string){
        String place = string.place;
        StrExp value = string.value;
        results.addEntry(place, value.value);
    }

    public void interpretLetInt(LetInt integer){
        String place = integer.place;
        IntExp value = integer.value;
        results.addEntry(place, value.value);
    }

    public void interpretLetReal(LetReal real){
        String place = real.place;
        RealExp value = real.value;
        results.addEntry(place, value.realValue);
    }

    public void interpretLetVar(LetVar var){
        String place = var.place;
        IdentExp objVar = var.var;
        Object objVal = results.getEntry(objVar.ident);
        results.addEntry(place, objVal);
    }

    public void interpretLetUn(LetUn var){
        String place = var.place;
        
        UnExp locVar = var.unExp;
        Object val = results.getEntry(locVar.right.toString());

        switch(locVar.op){
            case NEG:
                Object val1 = OpUtil.negate(val);
                results.addEntry(place, val1);
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
        
        BinExp exp = var.exp;
        Object leftVal = results.getEntry(exp.left.toString());
        Object rightVal = results.getEntry(exp.right.toString());

        switch(exp.op){
            case BAND:
                Object val0 = OpUtil.and(leftVal, rightVal);
                results.addEntry(place, val0);
                break;
            case BOR:
                Object val1 = OpUtil.or(leftVal, rightVal);
                results.addEntry(place, val1);
                break;
            case DIV:
                Object val2 = OpUtil.divide(leftVal, rightVal);
                results.addEntry(place, val2);
                break;
            case ADD:
                Object val3 = OpUtil.plus(leftVal, rightVal);
                results.addEntry(place, val3);
                break;
            case SUB:
                Object val4 = OpUtil.minus(leftVal, rightVal);
                results.addEntry(place, val4);
                break;
            case MUL:
                Object val5 = OpUtil.times(leftVal, rightVal);
                results.addEntry(place, val5);
                break;
            case GE:
                Object val6 = OpUtil.greaterThanOrEqualTo(leftVal, rightVal);
                results.addEntry(place, val6);
                break;
            case GT:
                Object val7 = OpUtil.greaterThan(leftVal, rightVal);
                results.addEntry(place, val7);
                break;
            case NE:
                Object val8 = OpUtil.notEqual(leftVal, rightVal);
                results.addEntry(place, val8);
                break;
            case LT:
                Object val9 = OpUtil.lessThan(leftVal, rightVal);
                results.addEntry(place, val9);
                break;
            case LE:
                Object val10 = OpUtil.lessThanOrEqualTo(leftVal, rightVal);
                results.addEntry(place, val10);
                break;
            default:
                break; 
        }
    }




}
