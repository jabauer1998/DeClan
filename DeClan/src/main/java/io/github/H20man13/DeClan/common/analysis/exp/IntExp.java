package io.github.H20man13.DeClan.common.analysis.exp;

public class IntExp implements Exp {
    private int value;

    public IntExp(int value){
        this.value = value;
    }

    @Override
    public boolean equals(Exp exp) {
        if(exp instanceof IntExp){
            IntExp intExp = (IntExp)exp;
            return this.value == intExp.value;
        } else {
            return false;
        }
    } 
}
