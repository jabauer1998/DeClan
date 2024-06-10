package io.github.H20man13.DeClan.common.util;

import io.github.H20man13.DeClan.common.dag.DagNode;
import io.github.H20man13.DeClan.common.dag.DagNode.ScopeType;
import io.github.H20man13.DeClan.common.dag.DagNode.ValueType;

import java.math.BigInteger;
import java.rmi.UnexpectedException;

import io.github.H20man13.DeClan.common.dag.DagOperationNode;
import io.github.H20man13.DeClan.common.exception.ConversionException;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
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
            default: throw new ConversionException("getBinOp", DagOperationNode.Op.class.getName(), BinExp.Operator.class.getName());
        }
    }

    public static UnExp.Operator getUnOp(DagOperationNode.Op op){
        switch(op){
            case INEG: return UnExp.Operator.INEG;
            case RNEG: return UnExp.Operator.RNEG;
            case BNOT: return UnExp.Operator.BNOT;
            case INOT: return UnExp.Operator.INOT;
            default: throw new ConversionException("getUnOp", DagOperationNode.Op.class.getName(), UnExp.Operator.class.getName());
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
            throw new ConversionException("valueToExp", Exp.class.getName(), Object.class.getName());
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
            throw new ConversionException("getValue", Exp.class.getName(), Object.class.getName());
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
            default: throw new ConversionException("binOpToPattern", BinExp.Operator.class.getName(), P.class.getName());
        }
    }

    public static P unOpToPattern(UnExp.Operator op){
        switch(op){
            case INEG: return P.INEG();
            case BNOT: return P.BNOT();
            case RNEG: return P.RNEG();
            case INOT: return P.INOT();
            default: throw new ConversionException("unOpToPattern", UnExp.Operator.class.getName(), P.class.getName());
        }
    }

    public static ICode.Type typeCheckerQualitiesToAssignType(TypeCheckerQualities type){
        if(type.containsQualities(TypeCheckerQualities.BOOLEAN)) return ICode.Type.BOOL;
        else if(type.containsQualities(TypeCheckerQualities.STRING)) return ICode.Type.STRING;
        else if(type.containsQualities(TypeCheckerQualities.REAL)) return ICode.Type.REAL;
        else if(type.containsQualities(TypeCheckerQualities.INTEGER)) return ICode.Type.INT;
        else throw new ConversionException("typeCheckerQualitiesToAssignType", "TypeCheckerQualities", ICode.Type.class.getName());
    }

    public static TypeCheckerQualities assignTypeToTypeCheckerQualities(ICode.Type type){
        if(type == ICode.Type.INT) return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);
        else if(type == ICode.Type.BOOL) return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
        else if(type == ICode.Type.REAL) return new TypeCheckerQualities(TypeCheckerQualities.REAL);
        else if(type == ICode.Type.STRING) return new TypeCheckerQualities(TypeCheckerQualities.STRING);
        throw new ConversionException("assignTypeToTypeCheckerQualities", ICode.Type.class.getName(), "TypeCheckerQualities");
    }

    public static ValueType assignTypeToDagValueType(ICode.Type type){
        if(type == ICode.Type.BOOL) return ValueType.BOOL;
        else if(type == ICode.Type.STRING) return ValueType.STRING;
        else if(type == ICode.Type.REAL) return ValueType.REAL;
        else if(type == ICode.Type.INT) return ValueType.INT;
        else throw new ConversionException("assignTypeToDagValueType", ICode.Type.class.getName(), ValueType.class.getName());
    }

    public static ScopeType assignScopeToDagScopeType(ICode.Scope scope){
        if(scope == ICode.Scope.GLOBAL) return ScopeType.GLOBAL;
        else if(scope == ICode.Scope.PARAM) return ScopeType.PARAM;
        else if(scope == ICode.Scope.RETURN) return ScopeType.RETURN;
        else if(scope == ICode.Scope.LOCAL) return ScopeType.LOCAL;
        else throw new ConversionException("assignScopeToDagScopeType", ICode.Scope.class.getName(), ScopeType.class.getName());
    }

    public static ICode.Scope dagScopeTypeToAssignScope(ScopeType scope){
        if(scope == ScopeType.GLOBAL) return ICode.Scope.GLOBAL;
        else if(scope == ScopeType.PARAM) return ICode.Scope.PARAM;
        else if(scope == ScopeType.RETURN) return ICode.Scope.RETURN;
        else if(scope == ScopeType.LOCAL) return ICode.Scope.LOCAL;
        else throw new ConversionException("dagScopeToAssignScope", ScopeType.class.getName(), ICode.Scope.class.getName());
    }

    public static ICode.Type dagValueTypeToAssignType(ValueType type){
        if(type == ValueType.BOOL) return ICode.Type.BOOL;
        else if(type == ValueType.STRING) return ICode.Type.STRING;
        else if(type == ValueType.REAL) return ICode.Type.REAL;
        else if(type == ValueType.INT) return ICode.Type.INT;
        else throw new ConversionException("dagValueTypeToAssignType", ValueType.class.getName(), ICode.Type.class.getName());
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
            default: throw new ConversionException("toBinOp", IrTokenType.class.getName(), BinExp.Operator.class.getName());
        }
    }

    public static Float toReal(Object input){
        if(input instanceof Integer){
            Integer inti = (Integer)input;
            return inti.floatValue();
        } else if(input instanceof Float){
            Float fValue = (Float)input;
            return fValue.floatValue();
        } else if(input instanceof Boolean){
            Boolean bi = (Boolean)input;
            if(bi){
                return 1.0f;
            } else {
                return 0.0f;
            }
        } else {
            throw new ConversionException("toReal", input.getClass().getName(), Float.class.getName());
        }
    }

    public static Integer toInt(Object input){
        if(input instanceof Integer){
            return (Integer)input;
        } else if(input instanceof BigInteger){
            BigInteger lInput = (BigInteger)input;
            return lInput.intValue();
        } else if(input instanceof Float){
            Float di = (Float)input;
            return di.intValue();
        } else if(input instanceof Boolean){
            Boolean bi = (Boolean)input;
            if(bi){
                return 1;
            } else {
                return 0;
            }
        } else {
            throw new ConversionException("toInt", input.getClass().getName(), Integer.class.getName());
        }
    }

    public static Integer toRawInt(Object input){
        if(input instanceof Integer){
            return (Integer)input;
        } else if(input instanceof Float){
            Float dV = (Float)input;
            Integer result = Float.floatToIntBits(dV);
            return result;
        } else if(input instanceof Boolean){
            Boolean bi = (Boolean)input;
            if(bi) return 1;
            else return 0;
        } else {
            throw new ConversionException("toRawInt", input.getClass().getName(), Integer.class.getName());
        }
    }

    public static Boolean toBool(Object input){
        if(input instanceof Integer){
            Integer inti = (Integer)input;
            return inti != 0;
        } else if(input instanceof Boolean){
            return (Boolean)input;
        } else if(input instanceof Float){
            Float di = (Float)input;
            return di != 0;
        } else {
            throw new ConversionException("toBool", input.getClass().getName(), Boolean.class.getName());
        }
    }
}