package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.pat.P;

public class Inline implements ICode{
    public String inlineAssembly;
    public List<IdentExp> params;

    public Inline(String inlineAssembly, List<IdentExp> param){
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
        for(IdentExp str : params){
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
    public boolean containsPlace(String place) {
        for(IdentExp param: params){
            if(param.containsPlace(place))
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
        for(IdentExp param: params){
            param.replacePlace(from, to);
        }
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(inlineAssembly, params);
    }

	@Override
	public ICode copy() {
		return new Inline(inlineAssembly, params);
	}
}
