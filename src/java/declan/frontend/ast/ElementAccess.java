package declan.frontend.ast;

import declan.utils.position.Position;

public class ElementAccess extends AbstractASTNode implements Expression{
    private final String lexeme;
    private final Expression index;

	/**
	 * Construct an Identifier ast node starting at the given source Position, with
	 * the specified lexeme giving the name of the identifier.
	 * 
	 * @param start
	 * @param lexeme
	 */
        public ElementAccess(Position start, String lexeme, Expression index) {
		super(start);
		this.lexeme = lexeme;
		this.index = index;
	}

	public String getLexeme() {
		return lexeme;
	}

        public Expression getExpression(){
	    return index;
        }

        @Override
        public String toString(){
	  return getLexeme() + '[' + index + ']';
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

	@Override
	public boolean containsIdentifier(String ident) {
		return lexeme.equals(ident);
	}
}


