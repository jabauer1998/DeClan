package edu.depauw.declan.common.ast;

import edu.depauw.declan.common.Position;

public class ParamaterDeclaration extends VariableDeclaration {

    public ParamaterDeclaration(Position start, Identifier identifier, Identifier type) {
        super(start, identifier, type);
        //TODO Auto-generated constructor stub
    }

    @Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <R> R acceptResult(DeclarationVisitor<R> visitor) {
		return visitor.visitResult(this);
	}
}
