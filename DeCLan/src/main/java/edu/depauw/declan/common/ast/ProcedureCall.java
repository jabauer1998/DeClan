package edu.depauw.declan.common.ast;

import edu.depauw.declan.common.Position;
import java.util.List;
import java.util.ArrayList;

/**
 * An ASTNode representing a procedure call statement, which consists of an
 * Identifier naming the procedure and an Expression giving its argument (in the
 * future this will become a list of Expressions).
 * 
 * @author bhoward
 */
public class ProcedureCall extends AbstractASTNode implements Statement {
	private final Identifier procedureName;
	private List<Expression> arguments;

	/**
	 * Construct a ProcedureCall ast node starting at the given source Position,
	 * with the specified procedure name and argument Expression.
	 * 
	 * @param start
	 * @param procedureName
	 * @param argument
	 */
	public ProcedureCall(Position start, Identifier procedureName, List<Expression> arguments) {
		super(start);
		this.procedureName = procedureName;
		this.arguments = arguments;
	}

        public ProcedureCall(Position start, Identifier procedureName, Expression argument) { //for testing compilation
		super(start);
		this.procedureName = procedureName;
		this.arguments = new ArrayList<>();
		this.arguments.add(argument);
	}

        public ProcedureCall(Position start, Identifier procedureName) {
		super(start);
		this.procedureName = procedureName;
		this.arguments = null;
	}

	public Identifier getProcedureName() {
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
	public <R> R acceptResult(StatementVisitor<R> visitor) {
		return visitor.visitResult(this);
	}
}
