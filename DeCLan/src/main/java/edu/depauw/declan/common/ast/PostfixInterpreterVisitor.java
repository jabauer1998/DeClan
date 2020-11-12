package edu.depauw.declan.common.ast;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This is an implementation of the ASTVisitor that encapsulates the algorithm
 * "interpret project 2 It is used for Project 2-3 of CSC 426.
 * 
 * @author bhoward
 */
public class PostfixInterpreterVisitor implements ASTVisitor {
	/**
	 * The environment is used to record the bindings of numeric values to
	 * constants.
	 */
        private Map<String, VariableEntry> environment;
	private PrintWriter out;
        private Stack<Integer> accumulator;

	/**
	 * Construct a default PostfixPrintVisitor that writes to the console.
	 */
	public PostfixInterpreterVisitor() {
		this(new PrintWriter(System.out, true));
	}

	/**
	 * Construct a PostfixPrintVisitor that sends output to the given PrintWriter.
	 * 
	 * @param out
	 */
	public PostfixInterpreterVisitor(PrintWriter out) {
		this.environment = new HashMap<>();
		this.out = out;
		this.accumulator = new Stack<>();
	}

	@Override
	public void visit(Program program) {
		// Process all of the constant declarations
		for (Declaration Decl : program.getDecls()) {
			Decl.accept(this);
		}
		// Process all of the statements in the program body
		for (Statement statement : program.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(ConstDeclaration constDecl) {
		// Bind a numeric value to a constant identifier
		Identifier id = constDecl.getIdentifier();
		NumValue num = constDecl.getNumber();
		environment.put(id.getLexeme(), new VariableEntry("CONST", num.getLexeme()));
	}

        @Override
	public void visit(ProcedureDeclaration constDecl) {
	  
	}

        @Override
	public void visit(VariableDeclaration varDecl) {
	    //do nothing not supported yet
	}

	@Override
	public void visit(ProcedureCall procedureCall) {
		if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
		        procedureCall.getArguments().get(0).accept(this);
			out.println(accumulator.pop());
		} else {
			// Ignore all other procedure calls
		}
	}

        @Override
	public void visit(FunctionCall call) {
	  
	}

        @Override
	public void visit(IfElifBranch ifs){
	  
	}
        
        @Override
	public void visit(ElseBranch ifs){
	  
	}
        
        @Override
	public void visit(WhileElifBranch ifs){
	  
	}

        @Override
	public void visit(RepeatBranch repeatStatement){
	  
	}
        
        @Override
	public void visit(Assignment assignment) {
	    //do nothing this is not needed yet
	}

        @Override
	public void visit(ForAssignment assignment) {
	    
	}

        @Override
	public void visit(ForBranch assignment) {
	    
	}

	@Override
	public void visit(EmptyStatement emptyStatement){
		// Do nothing
	}

	@Override
	public void visit(UnaryOperation unaryOperation){
		unaryOperation.getExpression().accept(this);
		int value = accumulator.pop();
		switch (unaryOperation.getOperator()) {
		case PLUS:
		    accumulator.push(value);
		    break;
		case MINUS:
		    accumulator.push(-value);
		    break;
		}
	}

	@Override
	public void visit(BinaryOperation binaryOperation) {
		binaryOperation.getLeft().accept(this);
		int leftvalue = accumulator.pop();
		binaryOperation.getRight().accept(this);
		int rightvalue = accumulator.pop();
		switch (binaryOperation.getOperator()) {
		case PLUS:
		    accumulator.push(leftvalue + rightvalue);
		    break;
		case MINUS:
		    accumulator.push(leftvalue - rightvalue);
		    break;
		case TIMES:
		    accumulator.push(leftvalue * rightvalue);
		    break;
		case DIV:
		    accumulator.push(leftvalue / rightvalue);
		    break;
		case MOD:
		    accumulator.push(leftvalue % rightvalue);
		    break;
		}
	}

	@Override
	public void visit(NumValue numValue) {
	    int value = Integer.parseInt(numValue.getLexeme());
	    accumulator.push(value);
	}

        @Override
	public void visit(StrValue numValue) {
	   
	}

	@Override
	public void visit(Identifier identifier) {
	    int value = environment.get(identifier.getLexeme()).getValue().intValue();
	    accumulator.push(value);
	}
}
