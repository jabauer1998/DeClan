package io.github.H20man13.DeClan.common.analysis.exp;

public class BoolExp implements Exp {
    private boolean trueFalse;

    public BoolExp(boolean trueFalse){
        this.trueFalse = trueFalse;
    }

    @Override
    public boolean equals(Exp exp) {
        if(exp instanceof BoolExp){
            BoolExp boolExp = (BoolExp)exp;
            return this.trueFalse = boolExp.trueFalse;
        } else {
            return false;
        }
    }
    
}
