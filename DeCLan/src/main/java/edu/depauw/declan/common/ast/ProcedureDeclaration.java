package edu.depauw.declan.common.ast;

import edu.depauw.declan.common.Position;
import java.util.List;

/**
 * An ASTNode representing a Procedure declaration. It contains the Identifier being
 * declared plus the variable value (currently just a NumValue) being bound to
 * it.
 * 
 * @author Jacob Bauer
 */
public class ProcedureDeclaration extends AbstractASTNode implements Declaration {
	private final Identifier procedureName;
        private final List<VariableDeclaration> arguments;
        private final Identifier returnType;
        private final List<Declaration> localVariables;
        private final List<Statement> toExec;
        private final Expression returnStatement;

	/**
	 * Construct a VarDecl ast node starting at the given source position, with
	 * the specified Identifier and NumValue.
	 * 
	 * @param start
	 * @param identifier
	 * @param number
	 */
  public ProcedureDeclaration(Position start, Identifier procedureName, List<VariableDeclaration> arguments, Identifier returnType, List<Declaration> localVariables, List<Statement> toExec, Expression returnStatement){
	    super(start);
	    this.procedureName = procedureName;
	    this.arguments = arguments;
	    this.localVariables = localVariables;
	    this.toExec = toExec;
	    this.returnStatement = returnStatement;
	    this.returnType = returnType;
	}

	public Identifier getProcedureName() {
		return procedureName;
	}

        public Identifier getReturnType() {
		return returnType;
	}

        public List<VariableDeclaration> getArguments(){
	  return arguments;
        }

        public List<Declaration> getLocalVariables(){
	  return localVariables;
        }

        public List<Statement> getExecutionStatements(){
	  return toExec;
        }

        public Expression getReturnStatement(){
	  return returnStatement;
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
