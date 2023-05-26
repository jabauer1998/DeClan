package io.github.H20man13.DeClan.common.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.model.SymbolTable;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNullNode;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.dag.DagValueNode;
import io.github.H20man13.DeClan.common.dag.DagVariableNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp.Operator;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.LiveInfo;
import io.github.H20man13.DeClan.common.token.IrTokenType;

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

    public static boolean setContainsExp(Set<Exp> returnSet, Exp exp){
        for(Exp expInSet : returnSet){
            if(expInSet.equals(exp)){
                return true;
            }
        }
        return false;
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

    public static BinExp.Operator toBinOp(IrTokenType type){
        switch(type){
            case NE: return BinExp.Operator.NE;
            case EQ: return BinExp.Operator.EQ;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LE: return BinExp.Operator.LE;
            case LT: return BinExp.Operator.LT;
            case ADD: return BinExp.Operator.ADD;
            case SUB: return BinExp.Operator.SUB;
            case MUL: return BinExp.Operator.MUL;
            case DIV: return BinExp.Operator.DIV;
            case MOD: return BinExp.Operator.MOD;
            case BOR: return BinExp.Operator.BOR;
            case BAND: return BinExp.Operator.BAND;
            default: return null;
        }
    }

    public static ICode getICodeFromExpression(String register, Exp latestInstructionOutput) {
        if(latestInstructionOutput instanceof BoolExp){
            return new LetBool(register, (BoolExp)latestInstructionOutput);
        } else if(latestInstructionOutput instanceof IntExp){
            return new LetInt(register, (IntExp)latestInstructionOutput);
        } else if(latestInstructionOutput instanceof RealExp){
            return new LetReal(register, (RealExp)latestInstructionOutput);
        } else if(latestInstructionOutput instanceof StrExp){
            return new LetString(register, (StrExp)latestInstructionOutput);
        } else if(latestInstructionOutput instanceof UnExp){
            return new LetUn(register, (UnExp)latestInstructionOutput);
        } else if(latestInstructionOutput instanceof BinExp){
            return new LetBin(register, (BinExp)latestInstructionOutput);
        } else {
            return null;
        }
    }

    public static Exp getExpressionFromICode(ICode elem) {
        if(elem instanceof LetBool){
            return ((LetBool)elem).value;
        } else if(elem instanceof LetString){
            return ((LetString)elem).value;
        } else if(elem instanceof LetInt){
            return ((LetInt)elem).value;
        } else if(elem instanceof LetReal){
            return ((LetReal)elem).value;
        } else if(elem instanceof LetBin){
            return ((LetBin)elem).exp;
        } else if(elem instanceof LetUn){
            return ((LetUn)elem).unExp;
        } else {
            return null;
        }
    }

    public static Object getValueFromSet(Set<Tuple<String, Object>> tuples, String name){
        for(Tuple<String, Object> tuple : tuples){
            if(tuple.source.equals(name)){
                return tuple.dest;
            }
        }
        return null;
    }

    public static BinExp.Operator getBinOp(DagOperationNode.Op op){
        switch(op){
            case ADD: return BinExp.Operator.ADD;
            case SUB: return BinExp.Operator.SUB;
            case MUL: return BinExp.Operator.MUL;
            case DIV: return BinExp.Operator.DIV;
            case MOD: return BinExp.Operator.MOD;
            case BAND: return BinExp.Operator.BAND;
            case BOR: return BinExp.Operator.BOR;
            case EQ: return BinExp.Operator.EQ;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LE: return BinExp.Operator.LE;
            case LT: return BinExp.Operator.LT;
            default: return null;
        }
    }

    public static UnExp.Operator getUnOp(DagOperationNode.Op op){
        switch(op){
            case NEG: return UnExp.Operator.NEG;
            case BNOT: return UnExp.Operator.BNOT;
            default: return null;
        }
    }

    public static String getIdentifier(DagNode node, Environment<String, LiveInfo> table){
        List<String> identifiers = node.getIdentifiers();
        for(String identifier : identifiers){
            if(table.entryExists(identifier)){
                LiveInfo life = table.getEntry(identifier);
                if(life.isAlive){
                    return identifier;
                }
            }
        }

        if(identifiers.size() > 0){
            return identifiers.get(0);
        } else {
            return null;
        }
    }
}
