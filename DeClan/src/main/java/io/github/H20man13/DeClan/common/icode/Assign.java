package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.pat.P;

public class Assign implements ICode{
    public String place;
    public Exp value;
    private Scope scope;
    private Type type;

    public enum Scope{
        GLOBAL,
        LOCAL,
        PARAM,
        EXTERNAL_RETURN,
        INTERNAL_RETURN,
        ARGUMENT,
     }
 
     public enum Type{
         BOOL,
         REAL,
         INT,
         STRING
     }

    public Assign(Scope scope, String place, Exp value, Type type){
        this.scope = scope;
        this.place = place;
        this.value = value;
        this.type = type;
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

        if(this.scope == Scope.ARGUMENT){
            sb.append('(');
            sb.append(this.value.toString());
            sb.append(" -> ");
            sb.append(this.place);
            sb.append(')');
            sb.append('[');
            sb.append(this.type);
            sb.append(']');
            return sb.toString();
        } else {
            if(this.scope == Scope.EXTERNAL_RETURN){
                sb.append("EXTERNAL RETURN ");
            } else if(this.scope == Scope.INTERNAL_RETURN){
                sb.append("INTERNAL RETURN ");
            } else if(this.scope == Scope.PARAM){
                sb.append("PARAM ");
            } else if(this.scope == Scope.GLOBAL){
                sb.append("GLOBAL ");
            }

            sb.append(place);
            sb.append(" := ");
            sb.append(value.toString());
            sb.append(" [");
            sb.append(this.type);
            sb.append(']');
            return sb.toString();
        }
	}

    @Override
    public boolean isConstant() {
        return value.isConstant();
    }

    @Override
    public boolean isBranch() {
        return value.isBranch();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Assign){
            Assign assign = (Assign)obj;

            boolean placeEquals = assign.place.equals(place);
            boolean expEquals = assign.value.equals(value);

            return placeEquals && expEquals;
        } else {
            return false;
        }
    }

    @Override
    public P asPattern() {
        return P.PAT(P.ID(), P.ASSIGN(), value.asPattern(true));
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> linkedList = new LinkedList<ICode>();
        linkedList.add(this);
        return linkedList;
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
}
