package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.main.MyTypeChecker;

public class LetUn implements ICode {
	private String place;
	private Op op;
	private String value;

	public LetUn(String place, Op op, String value) {
		this.place = place;
		this.op = op;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return place + " := " + op + " " + value;
	}
    
    public static Op getOp(MyTypeChecker.TypeCheckerTypes type){
	switch(type){
	case BOOLEAN:
	    return Op.BNOT;
	case REAL:
	    return Op.RNEG;
	case INTEGER:
	    return Op.INEG; 
	}
	return null;
    }

    public enum Op {
	INEG, RNEG, BNOT
    }
}
