package declan.frontend.ast;

import declan.utils.position.Position;

public class CharValue extends AbstractASTNode implements Expression{
    public String lexeme;

    public CharValue(Position pos, String lexeme){
	super(pos);
	this.lexeme = lexeme;
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
    public String toString(){
	return lexeme;
    }

    public String getLexeme(){
	return lexeme;
    }

    @Override
    public boolean containsIdentifier(String ident){
	return false;
    }

    @Override
    public boolean isConstant(){
	return true;
    }
}
