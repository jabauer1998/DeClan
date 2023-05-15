package io.github.H20man13.DeClan.common.analysis.exp;

public class StrExp implements Exp{
    private String value;

    public StrExp(String value){
        this.value = value;
    }

    @Override
    public boolean equals(Exp exp) {
        if(exp instanceof StrExp){
            StrExp strExp = (StrExp)exp;
            return this.value.equals(strExp.value);
        } else {
            return false;
        }
    }
    
}
