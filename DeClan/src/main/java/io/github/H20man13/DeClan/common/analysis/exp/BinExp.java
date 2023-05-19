package io.github.H20man13.DeClan.common.analysis.exp;

public class BinExp implements Exp {
    public IdentExp right;
    public IdentExp left;
    public Operator op;

    public BinExp(IdentExp right, Operator op, IdentExp left){
        this.right = right;
        this.left = left;
        this.op = op;
    }
    
    public enum Operator{
        ADD,
        SUB,
        MUL,
        MOD,
        DIV,
        BAND,
        BOR,
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
}
