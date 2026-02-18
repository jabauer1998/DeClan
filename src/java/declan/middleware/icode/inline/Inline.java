package io.github.h20man13.DeClan.common.icode.inline;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.ICode.Type;
import io.github.h20man13.DeClan.common.icode.exp.IdentExp;
import io.github.h20man13.DeClan.common.pat.P;

public class Inline extends ICode{
    public String inlineAssembly;
    public List<InlineParam> params;

    public Inline(String inlineAssembly, List<InlineParam> param){
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
        for(InlineParam str : params){
            inlineAssemblyBuilder.append("IPARAM ");
            inlineAssemblyBuilder.append(str);
            inlineAssemblyBuilder.append("\r\n");
        }
        inlineAssemblyBuilder.append("IASM \"");
        inlineAssemblyBuilder.append(inlineAssembly);
        inlineAssemblyBuilder.append('\"');

        return inlineAssemblyBuilder.toString();
    }

    @Override
    public boolean containsPlace(String place) {
        for(InlineParam param: params){
            if(param.name.containsPlace(place))
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
        for(InlineParam param: params){
            param.name.replacePlace(from, to);
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
		LinkedList<InlineParam> newCopy = new LinkedList<InlineParam>();
		for(InlineParam param: params) {
			newCopy.add(param.copy());
		}
		
		return new Inline(inlineAssembly, newCopy);
	}
}
