package io.github.H20man13.DeClan.common.ast;

import io.github.H20man13.DeClan.common.Position;
import java.util.List;
import java.util.ArrayList;
import java.lang.String;
import java.lang.StringBuilder;

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
	    if(arguments == null){
	      arguments = new ArrayList<>();
	    }
	    this.arguments = arguments;
	    this.localVariables = localVariables;
	    if(toExec == null){
	      toExec = new ArrayList<>();
	    }
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

        public String toString(){
	  StringBuilder mystring = new StringBuilder();
	  mystring.append("IDENT: ");
	  mystring.append(getProcedureName().toString());
	  mystring.append(" TYPE: ");
	  mystring.append(getReturnType().toString());
	  mystring.append(" ARGUMENTS: ");
	  mystring.append("( ");
	  List<VariableDeclaration> argu = getArguments();
	  for(int i = 0; i < arguments.size(); i++){
	    mystring.append(argu.get(i).toString());
	    mystring.append(' ');
	  }
	  mystring.append(")\n");
	  List<Declaration> local = getLocalVariables();
	  for(int i = 0; i < local.size(); i++){
	    mystring.append('\t');
	    mystring.append(local.get(i).toString());
	    mystring.append('\n');
	  }
	  List<Statement> exec = getExecutionStatements();
	  for(int i = 0; i < exec.size(); i++){
	    mystring.append("\tStatement " + i + ": ");
	    mystring.append(exec.get(i));
	    mystring.append('\n');
	  }
	  if(getReturnStatement() != null){
	    mystring.append("\treturn ");
	    mystring.append(getReturnStatement().toString());
	    mystring.append(";\n");
	  }
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
