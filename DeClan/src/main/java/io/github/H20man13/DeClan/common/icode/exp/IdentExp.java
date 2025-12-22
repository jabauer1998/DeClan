package io.github.H20man13.DeClan.common.icode.exp;

import java.util.Objects;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.exception.ICodeFormatException;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.pat.P;

public class IdentExp implements Exp{
    public String ident;
    public ICode.Scope scope;

    public IdentExp(Scope scope, String ident){
        this.ident = ident;
        this.scope = scope;
    }

    @Override
    public boolean equals(Object exp) {
        if(exp instanceof IdentExp){
            IdentExp identExp = (IdentExp)exp;
            return this.ident.equals(identExp.ident) && identExp.scope == scope;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        if(this.scope != ICode.Scope.LOCAL){
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            sb.append(this.scope.toString());
            sb.append(' ');
            sb.append(this.ident);
            sb.append(')');
            return sb.toString();
        } else {
            return this.ident;
        }
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public P asPattern(boolean hasContainer) {
    	if(hasContainer) {
    		if(this.scope == ICode.Scope.GLOBAL) return P.PAT(P.PAT(P.GLOBAL(), P.ID()));
            else if(this.scope == ICode.Scope.RETURN) return P.PAT(P.PAT(P.RETURN(), P.ID()));
            else if(this.scope == ICode.Scope.PARAM) return P.PAT(P.PAT(P.PARAM(), P.ID()));
            else if(this.scope == ICode.Scope.LOCAL) return P.PAT(P.ID());
            else throw new ICodeFormatException(this, "Error cant produce pattern for IdentExp because the scope is " + scope);
    	} else {
    		if(this.scope == ICode.Scope.GLOBAL) return P.PAT(P.GLOBAL(), P.ID());
            else if(this.scope == ICode.Scope.RETURN) return P.PAT(P.RETURN(), P.ID());
            else if(this.scope == ICode.Scope.PARAM) return P.PAT(P.PARAM(), P.ID());
            else if(this.scope == ICode.Scope.LOCAL) return P.ID();
            else throw new ICodeFormatException(this, "Error cant produce pattern for IdentExp because the scope is " + scope);
    	}
    }

    @Override
    public boolean containsPlace(String place) {
        if(this.ident.equals(place))
            return true;

        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(this.ident.equals(from))
            this.ident = to;
    }

    @Override
    public NullableExp copy() {
        return new IdentExp(scope, ident);
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(ident, scope);
    }

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}
}
