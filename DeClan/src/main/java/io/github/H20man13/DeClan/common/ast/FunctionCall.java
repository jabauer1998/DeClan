package io.github.H20man13.DeClan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;

import java.util.List;

import io.github.H20man13.DeClan.common.position.Position;

import java.util.ArrayList;

/**
 * An ASTNode representing a function call statement, which consists of an
 * Identifier naming the procedure and an Expression giving its argument (in the
 * future this will become a list of Expressions).
 * 
 * @author Jacob Bauer
 */
public class FunctionCall extends AbstractASTNode implements Expression {
	private final Identifier procedureName;
	private final List<Expression> arguments;

	/**
	 * Construct a ProcedureCall ast node starting at the given source Position,
	 * with the specified procedure name and argument Expression.
	 * 
	 * @param start
	 * @param functionName
	 * @param argument
	 */
        
        public FunctionCall(Position start, Identifier procedureName, List<Expression> arguments) {
		super(start);
		this.procedureName = procedureName;
		this.arguments = arguments;
	}

        public FunctionCall(Position start, Identifier procedureName) {
		super(start);
		this.procedureName = procedureName;
		this.arguments = new ArrayList<>();
	}

	public Identifier getFunctionName() {
		return procedureName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

        @Override
        public String toString(){
	  StringBuilder mystring = new StringBuilder();
	  mystring.append(getFunctionName().toString());
	  mystring.append("( ");
	  List <Expression> args = getArguments();
	  for(int i = 0; i < args.size(); i++){
	    mystring.append(args.get(i).toString());
	    mystring.append(' ');
	  }
	  mystring.append(");");
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

	@Override
	public boolean isConstant() {
		return false;
	}
	
	public boolean containsIdentifier(String ident) {
		for(Expression arg: arguments) {
			if(arg.containsIdentifier(ident))
				return true;
		}
		return false;
	}
}
