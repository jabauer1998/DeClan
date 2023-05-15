package io.github.H20man13.DeClan.common.analysis.exp;

public class IdentExp implements Exp{
    private String ident;

    public IdentExp(String ident){
        this.ident = ident;
    }

    @Override
    public boolean equals(Exp exp) {
        if(exp instanceof IdentExp){
            IdentExp identExp = (IdentExp)exp;
            return this.ident.equals(identExp.ident);
        } else {
            return false;
        }
    }
}
