package io.github.H20man13.DeClan.common.icode.exp;

public class RealExp implements Exp {
    public double realValue;

    public RealExp(double realValue){
        this.realValue = realValue;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof RealExp){
            RealExp realExp = (RealExp)exp;
            return this.realValue == realExp.realValue;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        return "" + realValue;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
