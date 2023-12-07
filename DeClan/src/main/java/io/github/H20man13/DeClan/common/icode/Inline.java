package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
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
    public boolean equals(Object obj){
        if(obj instanceof Inline){
            Inline inLine = (Inline)obj;

            if(!inLine.inlineAssembly.equals(inlineAssembly))
                return false;

            if(inLine.param.size() != param.size())
                return false;

            int size = param.size();
            for(int i = 0; i < size; i++){
                if(!param.get(i).equals(inLine.param.get(i)))
                    return false;
            }

            return true;
        } else {
            return false;
        }
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

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> list = new LinkedList<ICode>();
        list.add(this);
        return list;
    }
}
