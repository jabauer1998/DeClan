package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.main.MyTypeChecker;
import io.github.H20man13.DeClan.common.ast.BinaryOperation;

public class LetBin implements ICode {
	public String place;
	public String left;
	public Op op;
	public String right;

	public LetBin(String place, String left, Op op, String right) {
		this.place = place;
		this.left = left;
		this.op = op;
		this.right = right;
	}
	
	@Override
	public String toString() {
		return place + " := " + left + " " + op + " " + right;
	}

        public static Op getOp(MyTypeChecker.TypeCheckerTypes leftValue, BinaryOperation.OpType op, MyTypeChecker.TypeCheckerTypes rightValue){
	    if((leftValue == MyTypeChecker.TypeCheckerTypes.BOOLEAN && rightValue == MyTypeChecker.TypeCheckerTypes.BOOLEAN)){
		switch(op){
		case AND:
		    return Op.BAND;
		case OR:
		    return Op.BOR;
		}
	    } else if((leftValue == MyTypeChecker.TypeCheckerTypes.INTEGER || rightValue == MyTypeChecker.TypeCheckerTypes.INTEGER)){
		switch(op){
		case DIV:
		    return Op.IDIV;
		case MOD:
		    return Op.IMOD;
		case PLUS:
		    return Op.IADD;
		case MINUS:
		    return Op.ISUB;
		case TIMES:
		    return Op.IMUL;
		case GE:
		    return Op.IGE;
		case GT:
		    return Op.IGT;
		case NE:
		    return Op.INE;
		case LT:
		    return Op.ILT;
		case LE:
		    return Op.INE;
		}
	    } else if ((leftValue == MyTypeChecker.TypeCheckerTypes.REAL || rightValue == MyTypeChecker.TypeCheckerTypes.REAL)) {
		switch(op){
		case DIVIDE:
		    return Op.RDIV;
		case PLUS:
		    return Op.RADD;
		case MINUS:
		    return Op.RSUB;
		case TIMES:
		    return Op.RMUL;
		case GE:
		    return Op.RGE;
		case GT:
		    return Op.RGT;
		case NE:
		    return Op.RNE;
		case LT:
		    return Op.RLT;
		case LE:
		    return Op.RNE;
		}
	    }
	    return null;
        }

	public enum Op {
	    IADD, ISUB, IMUL, IDIV, IMOD, RADD, RSUB, RMUL, RDIV, BAND, BOR, RLT, RLE, RGT, RGE, RNE, REQ, ILT, ILE, IGT, IGE, INE, IEQ
	}
}
