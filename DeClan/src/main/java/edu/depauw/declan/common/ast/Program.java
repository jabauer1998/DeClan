package edu.depauw.declan.common.ast;


import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.Position;

import java.lang.String;
import java.lang.StringBuilder;

/**
 * An ASTNode representing the top-level DeCLan program. It consists of a
 * sequence of declarations followed by a sequence of statements (the "body").
 * The declarations set up the bindings for names (constants, variables, types,
 * and procedures) to be used in the body of the program.
 * 
 * @author bhoward
 */
public class Program extends AbstractASTNode {
	private final List<Declaration> constDecls;
	private final List<Declaration> varDecls;
	private final List<Declaration> procDecls;
	private final List<Statement> statements;

	/**
	 * Construct a Program ast node starting at the given source Position, with the
	 * specified Collections (which are expected to be read-only, such as produced
	 * by {@link java.util.Collections#unmodifiableCollection
	 * Collections.unmodifiableCollection} method) of declarations and statements.
	 * 
	 * @param start
	 * @param constDecls
	 * @param statements
	 */
	public Program(Position start, List<Declaration> constDecls, List<Declaration> varDecls, List<Declaration> procDecls, List<Statement> statements) {
		super(start);
		this.constDecls = constDecls;
		this.procDecls = procDecls;
		this.varDecls = varDecls;
		this.statements = statements;
	}
        @Override
    public String toString(){
	  StringBuilder mystring = new StringBuilder();
	  mystring.append("Program Decls:\n");
	  List<Declaration> decl = getDecls();
	  for(int i = 0; i < decl.size(); i++){
	    mystring.append(decl.get(i).toString());
	    mystring.append('\n');
	  }
	  mystring.append("Program Statements:\n");
	  List<Statement> stats = getStatements();
	  for(int i = 0; i < stats.size(); i++){
	    mystring.append(stats.get(i).toString());
	    mystring.append('\n');
	  }
	  mystring.append("DONE!!!");
	  return mystring.toString();
	}
  
	public List<Declaration> getConstDecls() {
		return constDecls;
	}

	public List<Declaration> getVarDecls(){
		return varDecls;
	}

	public List<Declaration> getProcDecls(){
		return procDecls;
	}

	public List<Statement> getStatements() {
		return statements;
	}

	public List<Declaration> getDecls(){
		List<Declaration> result = new LinkedList<Declaration>();
		result.addAll(constDecls);
		result.addAll(varDecls);
		result.addAll(procDecls);
		return result;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
