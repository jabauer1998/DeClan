package io.github.H20man13.DeClan.common.icode.exp;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.Utils;

public class BinExp implements Exp {
    public Exp right;
    public Exp left;
    public Operator op;

    public BinExp(Exp left, Operator op, Exp right){
        this.right = right;
        this.left = left;
        this.op = op;
    }
    
    public enum Operator{
        IADD,
        ISUB,
        IMUL,
        IMOD,
        IAND,
        IOR,
        IDIV,
        IRSHIFT,
        ILSHIFT,
        RADD,
        RSUB,
        RMUL,
        RDIVIDE,
        LAND,
        LOR,
        GE,
        GT,
        LT,
        LE,
        EQ,
        NE
    }
    
    @Override
    public boolean equals(Object exp) {
        if(exp instanceof BinExp){
            BinExp binExp = (BinExp)exp;
            boolean operatorEquals = this.op == binExp.op;
            boolean leftEquals = this.left.equals(binExp.left);
            boolean rightEquals = this.right.equals(binExp.right);
            return leftEquals && operatorEquals && rightEquals;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        return left.toString() + " " + op + " " + right.toString();
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public P asPattern(boolean hasContainer) {
        if(hasContainer)
            return P.PAT(left.asPattern(false), Utils.binOpToPattern(op), right.asPattern(false));
        else
            return null;
    }
}
