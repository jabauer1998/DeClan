package io.github.H20man13.DeClan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;

import io.github.H20man13.DeClan.common.position.Position;

/**
 * An ASTNode representing a binary operation (+, -, *, DIV, or MOD currently),
 * with left and right subexpressions and an operator.
 * 
 * @author bhoward, Jacob Bauer
 */
public class BinaryOperation extends AbstractASTNode implements Expression {
	private final OpType operator;
	private final Expression left, right;

	/**
	 * Construct a BinaryOperation ast node starting at the given source Position,
	 * with the specified left and right subexpressions and operator type.
	 * 
	 * @param start
	 * @param left
	 * @param operator
	 * @param right
	 */
	public BinaryOperation(Position start, Expression left, OpType operator, Expression right) {
		super(start);
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public Expression getLeft() {
		return left;
	}

	public OpType getOperator() {
		return operator;
	}

	public Expression getRight() {
		return right;
	}
        @Override
    public String toString(){
	  StringBuilder mystring = new StringBuilder();
	  mystring.append("(( ");
	  mystring.append(getLeft().toString());
	  mystring.append(" )");
	  mystring.append(' ');
	  mystring.append(opToString(getOperator()));
	  mystring.append(' ');
	  mystring.append("( ");
	  mystring.append(getRight().toString());
	  mystring.append(" ))");
	  return mystring.toString();
	}
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <R> R acceptResult(ExpressionVisitor<R> visitor) {
		return visitor.visitResult(this);
	}

	/**
	 * Define the allowable binary operators. Not to be confused with the
	 * similarly-named TokenTypes.
	 * 
	 * @author bhoward
	 */
	public enum OpType {
	  PLUS, MINUS, TIMES, DIV, MOD, DIVIDE, OR, AND, LT, LE, GT, GE, NE, EQ, LSHIFT, RSHIFT, BAND, BOR, BXOR
	}

    private static String opToString(OpType op){
	  if(op == OpType.PLUS){
	    return "+";
	  } else if (op == OpType.MINUS){
	    return "-";
	  } else if (op == OpType.TIMES){
	    return "*";
	  } else if(op == OpType.DIV || op == OpType.DIVIDE){
	    return "/";
	  } else if (op == OpType.MOD){
	    return "%";
	  } else if (op == OpType.OR){
	    return "||";
	  } else if (op == OpType.AND){
	    return "&&";
	  } else if (op == OpType.LT){
	    return "<";
	  } else if (op == OpType.LE){
	    return "<=";
	  } else if (op == OpType.GT){
	    return ">";
	  } else if (op == OpType.GE){
	    return ">=";
	  } else if (op == OpType.NE){
	    return "!=";
	  } else if (op == OpType.BAND){
		return "BAND";
	  } else if (op == OpType.BOR){
		return "BOR";
	  } else if (op == OpType.LSHIFT){
		return "<<";
	  } else if (op == OpType.RSHIFT){
		return ">>";
	  } else if(op == OpType.BXOR){
		return "BXOR";
	  } else {
	    return "==";
	  }
    }

	@Override
	public boolean isConstant() {
		return left.isConstant() && right.isConstant();
	}

	@Override
	public boolean containsIdentifier(String ident) {
		return left.containsIdentifier(ident) || right.containsIdentifier(ident);
	}
}
