package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.pat.P;

public class Inline implements ICode{
    public String inlineAssembly;
    public List<String> params;

    public Inline(String inlineAssembly, List<String> param){
        this.inlineAssembly = inlineAssembly;
        this.params = param;
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

            if(inLine.params.size() != params.size())
                return false;

            int size = params.size();
            for(int i = 0; i < size; i++){
                if(!params.get(i).equals(inLine.params.get(i)))
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
        for(String str : params){
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

    @Override
    public boolean containsPlace(String place) {
        for(String param: params){
            if(param.equals(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        LinkedList<String> newList = new LinkedList<>();
        for(String param: params){
            if(param.equals(from)){
                newList.add(to);
            } else {
                newList.add(param);
            }
        }
        this.params = newList;
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
}
