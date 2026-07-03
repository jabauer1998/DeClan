package declan.middleware.icode;

import java.util.List;
import java.util.Set;

import declan.utils.Copyable;
import declan.utils.pat.P;

public abstract class ICode implements Copyable<ICode>{
    public enum Scope{
        GLOBAL,
        LOCAL,
        PARAM,
        RETURN
     }
 
     public enum Type implements Copyable<ICode.Type>{
         BOOL,
         REAL,
         INT,
         STRING,
	 CHAR,
	 VOID,
	 INT_ARRAY,
	 REAL_ARRAY,
	 BOOL_ARRAY;
		@Override
		public Type copy() {
			return this;
		}
     }

    public abstract String toString();
    public abstract boolean isConstant();
    public abstract boolean isBranch();
    public abstract ICode copy();
    public abstract P asPattern();
    public abstract boolean equals(Object object);
    public abstract boolean containsPlace(String place);
    public abstract boolean containsLabel(String label);
    public abstract void replacePlace(String from, String to);
    public abstract void replaceLabel(String from, String to);
    @Override
    public abstract int hashCode();
}


