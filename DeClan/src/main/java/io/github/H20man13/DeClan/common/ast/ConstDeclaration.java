package io.github.H20man13.DeClan.common.ast;

import io.github.H20man13.DeClan.common.Position;
import java.lang.String;
import java.lang.StringBuilder;

/**
 * An ASTNode representing a CONST declaration. It contains the Identifier being
 * declared plus the constant value (currently just a NumValue) being bound to
 * it.
 * I also added support for Const Strings and Booleans
 * @author bhoward, Jacob
 */
public class ConstDeclaration extends AbstractASTNode implements Declaration {
	private final Identifier identifier;
	private final Expression value;

	/**
	 * Construct a ConstDecl ast node starting at the given source position, with
	 * the specified Identifier and NumValue.
	 * 
	 * @param start
	 * @param identifier
	 * @param number
	 */
	public ConstDeclaration(Position start, Identifier identifier, Expression value) {
		super(start);
		this.identifier = identifier;
		this.value = value;
	}

        public ConstDeclaration(Position start, Identifier identifier, NumValue value) {
	    super(start);
	    this.identifier = identifier;
	    this.value = value;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Expression getValue() {
		return value;
	}

        @Override
	public String toString(){
	  StringBuilder mystring = new StringBuilder();
	  mystring.append("const ");
	  mystring.append(identifier.toString());
	  mystring.append(" = ");
	  mystring.append(value.toString());
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
