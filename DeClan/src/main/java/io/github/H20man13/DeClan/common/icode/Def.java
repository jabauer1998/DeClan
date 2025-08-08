package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.Objects;

import io.github.H20man13.DeClan.common.exception.ICodeFormatException;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class Def extends ICode {
    private static HashSet<Def> allDefs = new HashSet<Def>();
	
	public String label;
    public Type type;
    public Scope scope;
    public Exp val;
    private int seq;

    public Def(ICode.Scope scope, String label, Exp val, ICode.Type type){
        this.scope = scope;
        this.label = label;
        this.type = type;
        this.val = val;
        for(this.seq = 0; allDefs.contains(this); this.seq++);
        allDefs.add(this);
    }
    
    private Def(Def other){
        this.scope = other.scope;
        this.label = other.label;
        this.type = other.type;
        this.val = (Exp)other.val.copy();
        this.seq = other.seq;
    }
    

    @Override
    public boolean isConstant() {
        return val.isConstant();  
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Def){
            Def toCheck = (Def)obj;
            if(toCheck.label.equals(label))
                if(toCheck.scope == scope)
                    if(toCheck.type == type)
                        if(toCheck.val.equals(val))
                        	if(toCheck.seq == seq)
                        		return true;
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        
        if(scope == ICode.Scope.PARAM){
            sb.append('[');
            sb.append(val.toString());
            sb.append(" -> ");
            sb.append(label);
            sb.append("]<");
            sb.append(type);
            sb.append('>');
        } else {
            sb.append("DEF ");
            if(scope != ICode.Scope.LOCAL){
                sb.append(scope.toString());
                sb.append(' ');
            }
            sb.append(label);
            sb.append(" := ");
            sb.append(val.toString());
            sb.append(" <");
            sb.append(type.toString());
            sb.append(">");
        }
        return sb.toString();
    }

    @Override
    public P asPattern() {
        P expPattern = val.asPattern(true);
        P typePattern = ConversionUtils.typeToPattern(type);
        return P.PAT(P.DEF(), P.ID(), P.ASSIGN(), expPattern, typePattern);
    }

    @Override
    public boolean containsPlace(String place) {
        if(label.equals(place))
            return true;
        return this.val.containsPlace(place);
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(this.label.equals(from))
            this.label = to;
        this.val.replacePlace(from, to);
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(label, type, scope, val, seq);
    }

	@Override
	public ICode copy() {
		return new Def(this);
	}
}
