package declan.middleware.icode;

import java.util.HashSet;
import java.util.Set;
import declan.utils.pat.P;
import declan.middleware.icode.exp.Exp;
import declan.utils.exception.ICodeFormatException;
import declan.utils.ConversionUtils;
import java.util.Objects;

public class ArrayAssign extends ICode{
    private static Set<ArrayAssign> times = new HashSet<ArrayAssign>();
    
    public ICode.Type type;
    public ICode.Scope scope;
    public Exp index;
    public String ident;
    public Exp expression;
    private int time;
    
    public ArrayAssign(ICode.Scope scope, String ident, Exp index, Exp expression, ICode.Type type){
	this.scope = scope;
	this.ident = ident;
	this.index = index;
	this.expression = expression;
	this.type = type;
    }

    public ArrayAssign(ArrayAssign assign){
	this.scope = assign.scope;
	this.ident = assign.ident;
	this.index = (Exp)assign.index.copy();
	this.expression = (Exp)assign.expression.copy();
	recalculateIdentNumber();
    }

    public static void resetAssigns() {
    	times = new HashSet<ArrayAssign>();
    }

    @Override
	public String toString() {
        StringBuilder sb = new StringBuilder();

        if(this.scope == ICode.Scope.GLOBAL){
            sb.append("GLOBAL ");
        } else if(this.scope == ICode.Scope.PARAM) {
	    sb.append("PARAM ");
        } else if(this.scope == ICode.Scope.RETURN) {
	    sb.append("RETURN ");
        } else if(this.scope != ICode.Scope.LOCAL){
            throw new RuntimeException("Invalid scope type for assignment " + scope);
        }

        sb.append(ident);
	sb.append("@ ");
	sb.append(index.toString());
        sb.append(" := ");
        sb.append(expression.toString());
        sb.append(" <");
        sb.append(this.type);
        sb.append('>');
        return sb.toString();
	}
    
	public String toAccurateString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(this.toString());
            sb.append("# ");
	    sb.append(time);
	    return sb.toString();
	}

    @Override
    public boolean isConstant() {
        return expression.isConstant();
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ArrayAssign){
            ArrayAssign assign = (ArrayAssign)obj;

            boolean placeEquals = assign.ident.equals(ident);
            boolean expEquals = assign.expression.equals(expression);
            boolean timesEquals = assign.time == this.time;
	    boolean indexEquals = assign.index.equals(this.index);

            return placeEquals && expEquals && timesEquals && indexEquals;
        } else {
            return false;
        }
    }

    @Override
    public P asPattern() {
        return P.PAT(P.ID(), P.AT(), index.asPattern(false),  P.ASSIGN(), expression.asPattern(true), ConversionUtils.typeToPattern(type));
    }

    @Override
    public boolean containsPlace(String place) {
        if(this.ident.equals(place))
            return true;

        return (this.index.containsPlace(place) || this.expression.containsPlace(place));
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(this.ident.equals(from))
            this.ident = to;
        expression.replacePlace(from, to);
	index.replacePlace(from, to);
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
    
    @Override
    public int hashCode(){
    	return Objects.hash(ident, index, expression, scope, type, time);
    }
    
    public void recalculateIdentNumber() {
    	for(this.time = 0; times.contains(this); ++this.time);
    	times.add(this);
    }

    @Override
    public ICode copy() {
	return new ArrayAssign(this);
    }
}
