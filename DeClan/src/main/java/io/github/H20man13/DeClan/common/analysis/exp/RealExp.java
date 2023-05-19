package io.github.H20man13.DeClan.common.analysis.exp;

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
}
