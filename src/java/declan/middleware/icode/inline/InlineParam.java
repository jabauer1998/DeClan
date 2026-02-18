package declan.middleware.icode.inline;

import java.util.Objects;

import declan.utils.Copyable;
import declan.utils.Tuple;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.NullableExp;

public class InlineParam implements Copyable<InlineParam>{
	public static final int IS_ADDRESS = 0b0001;
	public static final int IS_REGISTER = 0b0010;
	public static final int IS_DEFINITION = 0b0100;
	public static final int IS_USE = 0b1000;
	
	public ICode.Type type;
	public IdentExp name;
	public int qual;
	
	public InlineParam(Tuple<NullableExp, ICode.Type> data, int mask) {
		this.name = (IdentExp)data.source;
		this.type = data.dest;
		this.qual = mask;
		if(!containsAnyQual(IS_DEFINITION | IS_USE))
			this.qual |= IS_USE;
	}
	
	public InlineParam(IdentExp name, ICode.Type type, int mask) {
		this.name = name;
		this.type = type;
		this.qual = mask;
		if(!containsAnyQual(IS_DEFINITION | IS_USE))
			this.qual |= IS_USE;
	}
	
	public boolean containsAllQual(int mask) {
		return (qual & mask) == mask;
	}
	
	public boolean containsAnyQual(int mask) {
		return (qual & mask) != 0;
	}
	
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append(this.name);
		toString.append(" <");
		toString.append(this.type);
		toString.append("> %");
		
		if(containsAnyQual(IS_DEFINITION))
			toString.append("d");
		else if(containsAnyQual(IS_USE))
			toString.append("u");
		
		if(containsAnyQual(IS_ADDRESS))
			toString.append('a');
		else if(containsAnyQual(IS_REGISTER))
			toString.append('r');
		
		return toString.toString();
	}
	
	public int hashCode() {
		return Objects.hash(name, type, qual);
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof InlineParam) {
			InlineParam in = (InlineParam)obj;
			
			if(!in.name.equals(this.name))
				return false;
			
			if(this.type != in.type)
				return false;
			
			if(this.qual != in.qual)
				return false;
			
			return true;
		}
		return false;
	}

	@Override
	public InlineParam copy() {
		return new InlineParam(name, type, qual);
	}
}
