package io.github.H20man13.DeClan.common.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.analysis.exp.BinExp;
import io.github.H20man13.DeClan.common.analysis.exp.BoolExp;
import io.github.H20man13.DeClan.common.analysis.exp.Exp;
import io.github.H20man13.DeClan.common.analysis.exp.IdentExp;
import io.github.H20man13.DeClan.common.analysis.exp.IntExp;
import io.github.H20man13.DeClan.common.analysis.exp.RealExp;
import io.github.H20man13.DeClan.common.analysis.exp.StrExp;
import io.github.H20man13.DeClan.common.analysis.exp.UnExp;
import io.github.H20man13.DeClan.common.analysis.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.LetUn.Op;

public class Utils {
    public static List<ICode> stripFromListExcept(List<ICode> list, ICode item){
        List<ICode> linkedList = new LinkedList<ICode>();

        for(ICode listItem : list){
            if(listItem.hashCode() != item.hashCode()){
                linkedList.add(listItem);
            }
        }

        return linkedList;
    }

    public static BinExp.Operator getOp(LetBin.Op op) {
        switch(op){
            case ADD: return BinExp.Operator.ADD;
            case SUB: return BinExp.Operator.SUB;
            case MUL: return BinExp.Operator.MUL;
            case DIV: return BinExp.Operator.DIV;
            case MOD: return BinExp.Operator.MOD;
            case BAND: return BinExp.Operator.BAND;
            case BOR: return BinExp.Operator.BOR;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LT: return BinExp.Operator.LT;
            case LE: return BinExp.Operator.LE;
            case EQ: return BinExp.Operator.EQ;
            case NE: return BinExp.Operator.NE;
            default: return null;
        }
    }

    public static UnExp.Operator getOp(LetUn.Op op){
        switch(op){
            case NEG: return UnExp.Operator.NEG;
            case BNOT: return UnExp.Operator.BNOT;
            default: return null;
        }
    }

    public static boolean setContainsExp(Set<Exp> returnSet, Exp exp){
        for(Exp expInSet : returnSet){
            if(expInSet.equals(exp)){
                return true;
            }
        }
        return false;
    }

    public static LetUn.Op getOp(UnExp.Operator op) {
        switch(op){
            case BNOT: return LetUn.Op.BNOT;
            case NEG: return LetUn.Op.NEG;
            default: return null;
        }
    }

    public static LetBin.Op getOp(BinExp.Operator op){
        switch(op){
            case ADD: return LetBin.Op.ADD;
            case SUB: return LetBin.Op.SUB;
            case MUL: return LetBin.Op.MUL;
            case DIV: return LetBin.Op.DIV;
            case MOD: return LetBin.Op.MOD;
            case BAND: return LetBin.Op.BAND;
            case BOR: return LetBin.Op.BOR;
            case GE: return LetBin.Op.GE;
            case GT: return LetBin.Op.GT;
            case LT: return LetBin.Op.LT;
            case LE: return LetBin.Op.LE;
            case EQ: return LetBin.Op.EQ;
            case NE: return LetBin.Op.NE;
            default: return null;
        }
    }

    public static Exp getExpressionFromICode(ICode icode){
        if(icode instanceof LetVar){
            return new IdentExp(((LetVar)icode).var);
        } else if(icode instanceof LetReal){
           return new RealExp(((LetReal)icode).value);
        } else if(icode instanceof LetInt){
           return new IntExp(((LetInt)icode).value);
        } else if(icode instanceof LetBool){
            return new BoolExp(((LetBool)icode).value);
        } else if(icode instanceof LetString){
            return new StrExp(((LetString)icode).value);
        } else if(icode instanceof LetBin){
            LetBin binOp = (LetBin)icode;
            IdentExp left = new IdentExp(binOp.left);
            BinExp.Operator op = Utils.getOp(binOp.op);
            IdentExp right = new IdentExp(binOp.right);
            BinExp bin = new BinExp(left, op, right);
            return bin;
        } else if(icode instanceof LetUn){
            LetUn unOp = (LetUn)icode;
            IdentExp right = new IdentExp(unOp.value);
            UnExp.Operator op = Utils.getOp(unOp.op);
            UnExp unExp = new UnExp(op, right);
            return unExp;
        } else {
            return null;
        }
    }

    public static String getPlace(ICode icode){
        if(icode instanceof LetBool){
            LetBool boolICode = (LetBool)icode;
            return boolICode.place;
        } else if(icode instanceof LetInt){
            LetInt intICode = (LetInt)icode;
            return intICode.place;
        } else if(icode instanceof LetReal){
            LetReal realICode = (LetReal)icode;
            return realICode.place;
        } else if(icode instanceof LetString){
            LetString strICode = (LetString)icode;
            return strICode.place;
        } else if(icode instanceof LetVar){
            LetVar varICode = (LetVar)icode;
            return varICode.place;
        } else if(icode instanceof LetUn){
            LetUn unICode = (LetUn)icode;
            return unICode.place;
        } else if(icode instanceof LetBin){
            LetBin binICode = (LetBin)icode;
            return binICode.place;
        } else {
            return null;
        }
    }

    public static ICode getICodeFromExpression(String place, Exp expression){
        if(expression instanceof BoolExp){
            BoolExp boolLatest = (BoolExp)expression;
            return new LetBool(place, boolLatest.trueFalse);
        } else if(expression instanceof IntExp){
            IntExp intLatest = (IntExp)expression;
            return new LetInt(place, intLatest.value);
        } else if(expression instanceof RealExp){
            RealExp realLatest = (RealExp)expression;
            return new LetReal(place, realLatest.realValue);
        } else if(expression instanceof StrExp){
            StrExp strLatest = (StrExp)expression;
            return new LetString(place, strLatest.value);
        } else if(expression instanceof IdentExp){
            IdentExp identLatest = (IdentExp)expression;
            return new LetVar(place, identLatest.ident);
        } else if(expression instanceof UnExp){
            UnExp unLatest = (UnExp)expression;
            IdentExp iExp = unLatest.right;
            return new LetUn(place, Utils.getOp(unLatest.op), iExp.ident);
        } else if(expression instanceof BinExp){
            BinExp binLatest = (BinExp)expression;
            IdentExp iExp1 = binLatest.left;
            IdentExp iExp2 = binLatest.right;
            return new LetBin(place, iExp1.ident, Utils.getOp(binLatest.op), iExp2.ident);
        } else {
            return null;
        }
    }
}
