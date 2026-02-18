package io.github.h20man13.DeClan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;

import io.github.h20man13.DeClan.common.position.Position;

/**
 * An ASTNode representing a Variable declaration. It contains the Identifier being
 * declared plus the variable value (currently just a NumValue) being bound to
 * it.
 * 
 * @author Jacob Bauer
 */
public class VariableDeclaration extends AbstractASTNode implements Declaration {
	private final Identifier identifier;
        private final Identifier type;

	/**
	 * Construct a VarDecl ast node starting at the given source position, with
	 * the specified Identifier and NumValue.
	 * 
	 * @param start
	 * @param identifier
	 * @param number
	 */
        public VariableDeclaration(Position start, Identifier identifier, Identifier type) {
	    super(start);
	    this.identifier = identifier;
	    this.type = type;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Identifier getType() {
		return type;
	}
        @Override
        public String toString(){
	  StringBuilder mystring = new StringBuilder();
	  mystring.append(getIdentifier().toString());
	  mystring.append(' ');
	  mystring.append(getType().toString());
	  mystring.append(';');
	  return mystring.toString();
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
