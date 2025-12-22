package io.github.H20man13.DeClan.common.icode.exp;

public class NaaExp implements NullableExp{
	public NaaExp(){}
	@Override
	public NullableExp copy() {
		return new NaaExp();
	}
	@Override
	public boolean isConstant() {
		return false;
	}
	
	@Override
	public String toString() {
		return "NaaExp";
	}
	
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof NaaExp;
	}
	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}
}
