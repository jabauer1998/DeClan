package declan.middleware.icode.exp;

import declan.utils.pat.P;
import java.util.Objects;
import declan.middleware.icode.ICode;
import declan.utils.ConversionUtils;

public class ArrayAccess implements Exp{
    public IdentExp index;
    public String name;
    public ICode.Scope scope;
    
    public ArrayAccess(ICode.Scope scope, String name, IdentExp index){
	this.scope = scope;
	this.name = name;
	this.index = index;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof ArrayAccess){
	    ArrayAccess arrAccess = (ArrayAccess)exp;
	    if(arrAccess.index.equals(index))
		    if(arrAccess.name.equals(name))
			return scope == arrAccess.scope;
        }
        return false;
    } 

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
	sb.append("(");
	sb.append(scope);
	sb.append(' ');
	sb.append(name);
	sb.append("@ ");
	sb.append(index.toString());
	sb.append(")");
	return sb.toString();
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public P asPattern(boolean hasContainer) {
        if(hasContainer){
	    return P.PAT(P.PAT(P.ID(), P.AT(), index.asPattern(false)));
        } else {
	    return P.PAT(P.ID(), P.AT(), index.asPattern(false));
	}
    }

    @Override
    public boolean containsPlace(String place) {
        if(place.equals(name))
	    return true;

	return index.containsPlace(place);
    }

    @Override
    public void replacePlace(String from, String to) {
        if(name.equals(from))
	    name = to;
	index.replacePlace(from, to);
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(scope, name, index);
    }

    @Override
    public ArrayAccess copy(){
	return new ArrayAccess(scope, name, index);
    }

    @Override
    public boolean isZero() {
	    return false;
    }
}
