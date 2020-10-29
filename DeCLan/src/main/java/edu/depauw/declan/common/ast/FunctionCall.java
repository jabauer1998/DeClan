package edu.depauw.declan.common.ast;

import edu.depauw.declan.common.Position;
import java.util.List;

/**
 * An ASTNode representing a procedure call statement, which consists of an
 * Identifier naming the procedure and an Expression giving its argument (in the
 * future this will become a list of Expressions).
 * 
 * @author bhoward
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
		this.arguments = null;
	}

	public Identifier getFunctionName() {
		return procedureName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <R> R acceptResult(ExpressionVisitor<R> visitor) {
		return visitor.visitResult(this);
	}
}
