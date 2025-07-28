package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.H20man13.DeClan.common.exception.ICodeFormatException;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class Assign implements ICode{
	private static Set<Assign> times = new HashSet<Assign>();
	
    public String place;
    public Exp value;
    private Scope scope;
    private Type type;
    private int time;
    
    public Assign(ICode.Scope scope, String place, Exp value, ICode.Type type){
        this.scope = scope;
        this.place = place;
        this.value = value;
        this.type = type;
        this.time = 0;
        while(times.contains(this)) {
        	this.time++;
        }
        times.add(this);
    }
    
    public Assign(ICode.Scope scope, String place, Exp value, ICode.Type type, int time){
        this.scope = scope;
        this.place = place;
        this.value = value;
        this.type = type;
        this.time = 0;
        this.time = time;
    }

    public Scope getScope(){
        return scope;
    }

    public Type getType(){
        return type;
    }

    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();

        if(this.scope == Scope.GLOBAL){
            sb.append("GLOBAL ");
        } else if(this.scope == Scope.PARAM) {
        	sb.append("PARAM ");
        } else if(this.scope == Scope.RETURN) {
        	sb.append("RETURN ");
        } else if(this.scope != Scope.LOCAL){
            throw new ICodeFormatException(this, "Invalid scope type for assignment " + scope);
        }

        sb.append(place);
        sb.append(" := ");
        sb.append(value.toString());
        sb.append(" <");
        sb.append(this.type);
        sb.append('>');
        return sb.toString();
	}

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Assign){
            Assign assign = (Assign)obj;

            boolean placeEquals = assign.place.equals(place);
            boolean expEquals = assign.value.equals(value);
            boolean timesEquals = assign.time == this.time;

            return placeEquals && expEquals && timesEquals;
        } else {
            return false;
        }
    }

    @Override
    public P asPattern() {
        return P.PAT(P.ID(), P.ASSIGN(), value.asPattern(true), ConversionUtils.typeToPattern(type), P.INT());
    }

    @Override
    public boolean containsPlace(String place) {
        if(this.place.equals(place))
            return true;

        return this.value.containsPlace(place);
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(this.place.equals(from))
            this.place = to;
        value.replacePlace(from, to);
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode() {
    	return Objects.hash(place, value, scope, type, time);
    }

	@Override
	public ICode copy() {
		return new Assign(scope, place, value, type, time);
	}
}