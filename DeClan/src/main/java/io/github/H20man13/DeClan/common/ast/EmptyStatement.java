package io.github.H20man13.DeClan.common.ast;

import io.github.H20man13.DeClan.common.Position;

/**
 * An ASTNode representing an empty Statement. An empty statement doesn't serve
 * much purpose, but it allows us to avoid the question of whether the semicolon
 * is a separator or a terminator (that is, the statement sequences "S; T" and
 * "S; T;" are both grammatically correct and essentially equivalent, although
 * the latter ends with an empty statement).
 * 
 * @author bhoward
 */
public class EmptyStatement extends AbstractASTNode implements Statement {
	/**
	 * Construct an EmptyStatement ast node starting at the given source Position.
	 * 
	 * @param start
	 */
	public EmptyStatement(Position start) {
		super(start);
	}

        @Override
	public String toString(){
	  return "";
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
