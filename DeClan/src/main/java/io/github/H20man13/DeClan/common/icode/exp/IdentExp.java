package io.github.H20man13.DeClan.common.icode.exp;

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
}
