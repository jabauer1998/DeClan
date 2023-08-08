package io.github.H20man13.DeClan.common.icode;

import java.util.List;

import io.github.H20man13.DeClan.common.pat.P;

public class Inline implements ICode{
    public String inlineAssembly;
    public List<String> param;

    public Inline(String inlineAssembly, List<String> param){
        this.inlineAssembly = inlineAssembly;
        this.param = param;
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
        StringBuilder inlineAssemblyBuilder = new StringBuilder();
        for(String str : param){
            inlineAssemblyBuilder.append("IPARAM ");
            inlineAssemblyBuilder.append(str);
            inlineAssemblyBuilder.append('\n');
        }
        inlineAssemblyBuilder.append("IASM \"");
        inlineAssemblyBuilder.append(inlineAssembly);
        inlineAssemblyBuilder.append('\"');

        return inlineAssemblyBuilder.toString();
    }
}
