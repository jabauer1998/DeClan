package io.github.H20man13.DeClan.common.icode.exp;

public class IntExp implements Exp {
    public int value;

    public IntExp(int value){
        this.value = value;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof IntExp){
            IntExp intExp = (IntExp)exp;
            return this.value == intExp.value;
        } else {
            return false;
        }
    } 

    @Override
    public String toString(){
        return "" + value;
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
