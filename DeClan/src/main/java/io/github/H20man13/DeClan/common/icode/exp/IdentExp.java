package io.github.H20man13.DeClan.common.icode.exp;

import io.github.H20man13.DeClan.common.pat.P;

public class IdentExp implements Exp{
    public String ident;

    public IdentExp(String ident){
        this.ident = ident;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof IdentExp){
            IdentExp identExp = (IdentExp)exp;
            return this.ident.equals(identExp.ident);
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        return ident;
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
        if(hasContainer){
            return P.PAT(P.ID());
        } else {
            return P.ID();
        }
    }

    @Override
    public boolean containsPlace(String place) {
        if(this.ident.equals(place))
            return true;

        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(this.ident.equals(from))
            this.ident = to;
    }
}
