package io.github.H20man13.DeClan.common.analysis.exp;

public class RealExp implements Exp {
    private double realValue;

    public RealExp(double realValue){
        this.realValue = realValue;
    }

    @Override
    public boolean equals(Exp exp) {
        if(exp instanceof RealExp){
            RealExp realExp = (RealExp)exp;
            return this.realValue == realExp.realValue;
        } else {
            return false;
        }
    }
}
