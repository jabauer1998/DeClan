package io.github.H20man13.DeClan.common.util;

import io.github.H20man13.DeClan.common.dag.DagNode.ScopeType;
import io.github.H20man13.DeClan.common.dag.DagNode.ValueType;
import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.common.token.IrTokenType;

public class ConversionUtils {
    public static BinExp.Operator getBinOp(DagOperationNode.Op op){
        switch(op){
            case IADD: return BinExp.Operator.IADD;
            case ISUB: return BinExp.Operator.ISUB;
            case IMUL: return BinExp.Operator.IMUL;
            case IDIV: return BinExp.Operator.IDIV;
            case RADD: return BinExp.Operator.RADD;
            case RSUB: return BinExp.Operator.RSUB;
            case RMUL: return BinExp.Operator.RMUL;
            case RDIVIDE: return BinExp.Operator.RDIVIDE;
            case IMOD: return BinExp.Operator.IMOD;
            case LAND: return BinExp.Operator.LAND;
            case LOR: return BinExp.Operator.LOR;
            case IAND: return BinExp.Operator.IAND;
            case IOR: return BinExp.Operator.IOR;
            case IXOR: return BinExp.Operator.IXOR;
            case ILSHIFT: return BinExp.Operator.ILSHIFT;
            case IRSHIFT: return BinExp.Operator.IRSHIFT;
            case EQ: return BinExp.Operator.EQ;
            case NE: return BinExp.Operator.NE;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LE: return BinExp.Operator.LE;
            case LT: return BinExp.Operator.LT;
            default: return null;
        }
    }

    public static UnExp.Operator getUnOp(DagOperationNode.Op op){
        switch(op){
            case INEG: return UnExp.Operator.INEG;
            case RNEG: return UnExp.Operator.RNEG;
            case BNOT: return UnExp.Operator.BNOT;
            case INOT: return UnExp.Operator.INOT;
            default: return null;
        }
    }

    public static Exp valueToExp(Object result) {
        if(result instanceof Boolean){
            return new BoolExp((boolean)result);
        } else if(result instanceof Integer){
            return new IntExp((int)result);
        } else if(result instanceof String){
            return new StrExp((String)result);
        } else if(result instanceof Float){
            return new RealExp((float)result);
        } else {
            return null;
        }
    }

    public static Object getValue(Exp value) {
        if(value instanceof BoolExp){
            return ((BoolExp)value).trueFalse;
        } else if(value instanceof IntExp){
            return ((IntExp)value).value;
        } else if(value instanceof RealExp){
            return ((RealExp)value).realValue;
        } else if(value instanceof StrExp){
            return ((StrExp)value).value;
        } else {
            return null;
        }
    }

    public static P binOpToPattern(BinExp.Operator op){
        switch(op){
            case IADD: return P.IADD();
            case ISUB: return P.ISUB();
            case IMUL: return P.IMUL();
            case IDIV: return P.IDIV();
            case LAND: return P.BAND();
            case IMOD: return P.IMOD();
            case RMUL: return P.RMUL();
            case RDIVIDE: return P.RDIVIDE();
            case RADD: return P.RADD();
            case RSUB: return P.RSUB();
            case IAND: return P.IAND();
            case IOR: return P.IOR();
            case IXOR: return P.IXOR();
            case ILSHIFT: return P.LSHIFT();
            case IRSHIFT: return P.RSHIFT();
            case LOR: return P.BOR();
            case GT: return P.GT();
            case GE: return P.GE();
            case LT: return P.LT();
            case LE: return P.LE();
            case EQ: return P.EQ();
            case NE: return P.NE();
            default: return null;
        }
    }

    public static P unOpToPattern(UnExp.Operator op){
        switch(op){
            case INEG: return P.INEG();
            case BNOT: return P.BNOT();
            case RNEG: return P.RNEG();
            case INOT: return P.INOT();
            default: return null;
        }
    }

    public static Assign.Type typeCheckerQualitiesToAssignType(TypeCheckerQualities type){
        if(type.containsQualities(TypeCheckerQualities.BOOLEAN)) return Assign.Type.BOOL;
        else if(type.containsQualities(TypeCheckerQualities.STRING)) return Assign.Type.STRING;
        else if(type.containsQualities(TypeCheckerQualities.REAL)) return Assign.Type.REAL;
        else return Assign.Type.INT;
    }

    public static ValueType assignTypeToDagValueType(Assign.Type type){
        if(type == Assign.Type.BOOL) return ValueType.BOOL;
        else if(type == Assign.Type.STRING) return ValueType.STRING;
        else if(type == Assign.Type.REAL) return ValueType.REAL;
        else return ValueType.INT;
    }

    public static ScopeType assignScopeToDagScopeType(Assign.Scope scope){
        if(scope == Assign.Scope.GLOBAL) return ScopeType.GLOBAL;
        else if(scope == Assign.Scope.PARAM) return ScopeType.PARAM;
        else if(scope == Assign.Scope.EXTERNAL_RETURN) return ScopeType.EXTERNAL_RETURN;
        else if(scope == Assign.Scope.INTERNAL_RETURN) return ScopeType.INTERNAL_RETURN;
        else return ScopeType.LOCAL;
    }

    public static Assign.Scope dagScopeTypeToAssignScope(ScopeType scope){
        if(scope == ScopeType.GLOBAL) return Assign.Scope.GLOBAL;
        else if(scope == ScopeType.PARAM) return Assign.Scope.PARAM;
        else if(scope == ScopeType.EXTERNAL_RETURN) return Assign.Scope.EXTERNAL_RETURN;
        else if(scope == ScopeType.INTERNAL_RETURN) return Assign.Scope.INTERNAL_RETURN;
        else return Assign.Scope.LOCAL;
    }

    public static Assign.Type dagValueTypeToAssignType(ValueType type){
        if(type == ValueType.BOOL) return Assign.Type.BOOL;
        else if(type == ValueType.STRING) return Assign.Type.STRING;
        else if(type == ValueType.REAL) return Assign.Type.REAL;
        else return Assign.Type.INT;
    }

    public static BinExp.Operator toBinOp(IrTokenType type){
        switch(type){
            case NE: return BinExp.Operator.NE;
            case EQ: return BinExp.Operator.EQ;
            case GE: return BinExp.Operator.GE;
            case GT: return BinExp.Operator.GT;
            case LE: return BinExp.Operator.LE;
            case LT: return BinExp.Operator.LT;
            case RADD: return BinExp.Operator.RADD;
            case RSUB: return BinExp.Operator.RSUB;
            case RMUL: return BinExp.Operator.RMUL;
            case IADD: return BinExp.Operator.IADD;
            case ISUB: return BinExp.Operator.ISUB;
            case IMUL: return BinExp.Operator.IMUL;
            case IDIV: return BinExp.Operator.IDIV;
            case RDIVIDE: return BinExp.Operator.RDIVIDE;
            case IXOR: return BinExp.Operator.IXOR;
            case IMOD: return BinExp.Operator.IMOD;
            case LOR: return BinExp.Operator.LOR;
            case LAND: return BinExp.Operator.LAND;
            case IAND: return BinExp.Operator.IAND;
            case IOR: return BinExp.Operator.IOR;
            case IRSHIFT: return BinExp.Operator.IRSHIFT;
            case ILSHIFT: return BinExp.Operator.ILSHIFT;
            default: return null;
        }
    }
}
