package io.github.H20man13.DeClan.common.icode.exp;

import java.util.Objects;

import io.github.H20man13.DeClan.common.pat.P;

public class RealExp implements Exp {
    public float realValue;

    public RealExp(float realValue){
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

    @Override
    public P asPattern(boolean hasContainer) {
        if(hasContainer){
            return P.PAT(P.REAL());
        } else {
            return P.REAL();
        }
    }

    @Override
    public boolean containsPlace(String place) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode() {
    	return Objects.hashCode(realValue);
    }
}
