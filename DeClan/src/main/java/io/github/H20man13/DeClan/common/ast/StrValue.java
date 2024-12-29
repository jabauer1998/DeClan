package io.github.H20man13.DeClan.common.ast;

import java.lang.String;

import io.github.H20man13.DeClan.common.position.Position;

/**
 * An ASTNode representing a String literal. It is stored here as a lexeme
 * (that is, a String), to avoid the question of what Java numeric type to use
 * to represent it as an actual number.
 * 
 * @author Jacob Bauer
 */
public class StrValue extends AbstractASTNode implements Expression {
	private final String lexeme;

	/**
	 * Construct a NumValue ast node starting at the given source Position, with the
	 * specified lexeme for its textual representation.
	 * 
	 * @param start
	 * @param lexeme
	 */
	public StrValue(Position start, String lexeme) {
		super(start);
		this.lexeme = lexeme;
	}

	public String getLexeme() {
		return lexeme;
	}

	@Override
	public String toString(){
		return '\"' + getLexeme() + '\"';
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
		return true;
	}

	@Override
	public boolean containsIdentifier(String ident) {
		return false;
	}
}
