package declan.middleware.analysis.region.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import declan.utils.Tuple;
import declan.utils.ConversionUtils;

public class OpExpr implements Expr{
	public Expr[] arguments;
	public Operator op;
	public enum Operator{
		IPLUS,
		IMINUS,
		ITIMES
	}
	
	public OpExpr(Operator op, Expr... arguments) {
		this.op = op;
		this.arguments = arguments;
	}
	
	public boolean isUnary() {
		return arguments.length == 1;
	}
	
	public boolean isBinary() {
		return arguments.length > 1;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(arguments.length < 1) {
			sb.append((op == Operator.IPLUS) ? '+' : '-');
		} else if(arguments.length == 1) {
			sb.append((op == Operator.IPLUS) ? '+' : '-');
			sb.append(arguments[0].toString());
		} else {
			sb.append(arguments[0].toString());
			for(int i = 1; i < arguments.length; i++) {
				sb.append(' ');
				switch(op) {
				case IPLUS: sb.append('+');
				break;
				case IMINUS: sb.append('-');
				break;
				case ITIMES: sb.append('*');
				}
				sb.append(' ');
				sb.append(arguments[i].toString());
			}
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(op, arguments);
	}

	@Override
	public Expr simplify() {
		Expr[] simplifiedArguments = new Expr[arguments.length]; 
		for(int i = 0; i < arguments.length; i++) {
			simplifiedArguments[i] = arguments[i].simplify();
		}
		if(this.op == Operator.IPLUS) {
			if(this.isBinary()) {
				LinkedList<Expr> finalArguments = new LinkedList<Expr>();
				for(Expr exp: simplifiedArguments) {
					if(exp instanceof OpExpr) {
						OpExpr opExp = (OpExpr)exp;
						if(opExp.isBinary()) {
							for(Expr arg: opExp.arguments) {
								finalArguments.add(arg);
							}
						} else {
							finalArguments.add(opExp);
						}
					} else {
						finalArguments.add(exp);
					}
				}
				
				Expr[] finalArray = ConversionUtils.toExprArrayFromExprList(finalArguments);
				finalArray = combine(finalArray);
				return new OpExpr(Operator.IPLUS, finalArray);
			} else {
				return simplifiedArguments[0];
			}
		} else {
			if(this.isBinary()) {
				LinkedList<Expr> finalArguments = new LinkedList<Expr>();
				
				if(simplifiedArguments[0] instanceof OpExpr) {
					OpExpr arg = (OpExpr)simplifiedArguments[0];
					if(arg.isBinary()) {
						for(Expr expArg: arg.arguments) {
							if(expArg instanceof OpExpr) {
								finalArguments.add(expArg);
							}
						}
					} else {
						finalArguments.add(arg);
					}
				} else {
					finalArguments.add(simplifiedArguments[0]);
				}
				
				for(int i = 1; i < simplifiedArguments.length; i++) {
					Expr exp = simplifiedArguments[i];
					if(exp instanceof OpExpr) {
						OpExpr opExp = (OpExpr)exp;
						if(opExp.isBinary()) {
							for(Expr arg: opExp.arguments) {
								if(arg instanceof OpExpr) {
									OpExpr insideFetcher = (OpExpr)arg;
									finalArguments.add(insideFetcher.arguments[0]);
								} else {
									finalArguments.add(new OpExpr(Operator.IMINUS, arg));
								}
							}
						} else {
							OpExpr insideFetcher = (OpExpr)opExp.arguments[0];
							finalArguments.add(insideFetcher.arguments[0]);
						}
					} else {
						finalArguments.add(new OpExpr(Operator.IMINUS, exp));
					}
				}
				
				Expr[] finalArray = ConversionUtils.toExprArrayFromExprList(finalArguments);
				finalArray = combine(finalArray);
				return new OpExpr(Operator.IPLUS, finalArray);
			} else {
				Expr argAt0 = simplifiedArguments[0];
				if(argAt0 instanceof OpExpr) {
					OpExpr at0Op = (OpExpr)argAt0;
					if(at0Op.isBinary()) {
						LinkedList<Expr> finalArguments = new LinkedList<Expr>();
						for(Expr exp: at0Op.arguments) {
							if(exp instanceof OpExpr) {
								OpExpr insideExp = (OpExpr)exp;
								finalArguments.add(insideExp.arguments[0]);
							} else {
								finalArguments.add(new OpExpr(Operator.IMINUS, exp));
							}
						}
						
						Expr[] finalArray = ConversionUtils.toExprArrayFromExprList(finalArguments);
						finalArray = combine(finalArray);
						
						return new OpExpr(Operator.IPLUS, finalArray);
					} else {
						return at0Op.arguments[0];
					}
				} else {
					return this;
				}
			}
		}
	}
	
	private static Expr[] combine(Expr[] array) {
		int newArrayCount = 0;
		for(Expr exp: array) {
			if(exp instanceof OpExpr) {
				OpExpr expOp = (OpExpr)exp;
				if(expOp.arguments[0] instanceof RefVar) {
					newArrayCount++;
				}
			} else if(exp instanceof RefVar) {
				newArrayCount++;
			}
		}
		
		if(newArrayCount < array.length) {
			newArrayCount++;
		}
		
		Expr[] toRet = new Expr[newArrayCount];
		
		int arrayIndex = 0;
		int combineNumber = 0;
		for(int i = 0; i < array.length; i++) {
			Expr expAtI = array[i];
			if(expAtI instanceof RefVar) {
				toRet[arrayIndex] = expAtI;
				arrayIndex++;
			} else if(expAtI instanceof IntExpr) {
				combineNumber += ((IntExpr)expAtI).value;
			} else if(expAtI instanceof OpExpr) {
				OpExpr expOp = (OpExpr)expAtI;
				if(expOp.arguments[0] instanceof IntExpr) {
					combineNumber -= ((IntExpr)expOp.arguments[0]).value;
				} else if(expAtI instanceof RefVar) {
					toRet[arrayIndex] = expAtI;
					arrayIndex++;
				}
			}
		}
		
		if(arrayIndex < newArrayCount) {
			if(combineNumber >= 0) {
				toRet[arrayIndex] = new IntExpr(combineNumber);
			} else if(combineNumber < 0) {
				toRet[arrayIndex] = new OpExpr(Operator.IMINUS, new IntExpr(-combineNumber));
			}
		}
		
		return toRet;
	}

	@Override
	public Expr copy() {
		return new OpExpr(op, arguments);
	}
}
