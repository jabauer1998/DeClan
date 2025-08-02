package io.github.H20man13.DeClan.common.icode.inline;

import java.util.Objects;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;

public class InlineParam implements Copyable<InlineParam>{
	public static final int IS_ADDRESS = 0b0001;
	public static final int IS_REGISTER = 0b0010;
	public static final int IS_DEFINITION = 0b0100;
	public static final int IS_USE = 0b1000;
	
	public ICode.Type type;
	public IdentExp name;
	public int qual;
	
	public InlineParam(Tuple<IdentExp, ICode.Type> data, int mask) {
		this.name = data.source;
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
