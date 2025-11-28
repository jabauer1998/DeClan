package io.github.H20man13.DeClan.common;

public class CopyStr implements Copyable<CopyStr>{
	private String data;
	
	public CopyStr(String in) {
		this.data = in;
	}
	
	public String toString() {
		return data;
	}

	@Override
	public CopyStr copy() {
		return new CopyStr(data); 
	}
	
	@Override
	public boolean equals(Object myCpy) {
		if(myCpy instanceof CopyStr) {
			CopyStr str = (CopyStr)myCpy;
			if(str.data.equals(data))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return data.hashCode();
	}
}
