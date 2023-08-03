package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.pat.P;

public class Inline implements ICode{
    public String inlineAssembly;

    public Inline(String inlineAssembly){
        this.inlineAssembly = inlineAssembly;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public P asPattern() {
        return P.INLINE();
    }

    @Override
    public String toString(){
        return "INLINE \""+inlineAssembly+'\"';
    }
}
