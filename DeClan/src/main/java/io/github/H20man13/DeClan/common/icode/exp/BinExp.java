package io.github.H20man13.DeClan.common.icode.exp;

import java.util.Objects;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class BinExp implements Exp {
    public IdentExp right;
    public IdentExp left;
    public Operator op;

    public BinExp(IdentExp left, Operator op, IdentExp right){
        this.right = right;
        this.left = left;
        this.op = op;
    }
    
    public enum Operator{
        IADD,
        ISUB,
        IAND,
        IOR,
        IXOR,
        IRSHIFT,
        ILSHIFT,
        LAND,
        LOR,
        GE,
        GT,
        LT,
        LE,
        IEQ,
        INE,
        BEQ,
        BNE
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
            return P.PAT(left.asPattern(false), ConversionUtils.binOpToPattern(op), right.asPattern(false));
        else
            return null;
    }

    @Override
    public boolean containsPlace(String place) {
        if(left.containsPlace(place))
            return true;

        if(right.containsPlace(place))
            return true;

        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        left.replacePlace(from, to);
        right.replacePlace(from, to);
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(left, op, right);
    }

	@Override
	public NullableExp copy() {
		return new BinExp((IdentExp)left.copy(), op, (IdentExp)right.copy());
	}

	@Override
	public boolean isZero() {
		return false;
	}
}
