package io.github.H20man13.DeClan.common.icode.exp;

public class StrExp implements Exp{
    public String value;

    public StrExp(String value){
        this.value = value;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof StrExp){
            StrExp strExp = (StrExp)exp;
            return this.value.equals(strExp.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        return '\"' + value + '\"';
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
