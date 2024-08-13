package io.github.H20man13.DeClan.main;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import io.github.H20man13.DeClan.common.ast.ASTVisitor;
import io.github.H20man13.DeClan.common.ast.Asm;
import io.github.H20man13.DeClan.common.ast.Assignment;
import io.github.H20man13.DeClan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.common.ast.BoolValue;
import io.github.H20man13.DeClan.common.ast.Branch;
import io.github.H20man13.DeClan.common.ast.ConstDeclaration;
import io.github.H20man13.DeClan.common.ast.Declaration;
import io.github.H20man13.DeClan.common.ast.ElseBranch;
import io.github.H20man13.DeClan.common.ast.EmptyStatement;
import io.github.H20man13.DeClan.common.ast.Expression;
import io.github.H20man13.DeClan.common.ast.ForBranch;
import io.github.H20man13.DeClan.common.ast.FunctionCall;
import io.github.H20man13.DeClan.common.ast.Identifier;
import io.github.H20man13.DeClan.common.ast.IfElifBranch;
import io.github.H20man13.DeClan.common.ast.Library;
import io.github.H20man13.DeClan.common.ast.NumValue;
import io.github.H20man13.DeClan.common.ast.ParamaterDeclaration;
import io.github.H20man13.DeClan.common.ast.ProcedureCall;
import io.github.H20man13.DeClan.common.ast.ProcedureDeclaration;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.ast.RepeatBranch;
import io.github.H20man13.DeClan.common.ast.Statement;
import io.github.H20man13.DeClan.common.ast.StrValue;
import io.github.H20man13.DeClan.common.ast.UnaryOperation;
import io.github.H20man13.DeClan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.ast.WhileElifBranch;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;

/**
 * This is an implementation of the ASTVisitor that encapsulates the algorithm
 * "interpret project 2 It is used for Project 2-3 of CSC 426.
 * 
 * @author jacobbauer
 */
public class MyPostfixInterpreterVisitor implements ASTVisitor {
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
	public MyPostfixInterpreterVisitor() {
		this(new PrintWriter(System.out, true));
	}

	/**
	 * Construct a PostfixPrintVisitor that sends output to the given PrintWriter.
	 * 
	 * @param out
	 */
	public MyPostfixInterpreterVisitor(PrintWriter out) {
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
		NumValue num = (NumValue)constDecl.getValue();
		environment.put(id.getLexeme(), new VariableEntry(true, Integer.parseInt(num.getLexeme())));
	}

        @Override
	public void visit(ProcedureDeclaration constDecl) {
	    //not supported yet
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
	  //do nothing this is not needed yet
	}

        @Override
	public void visit(IfElifBranch ifs){
	  //do nothing this is not needed yet
	}
        
        @Override
	public void visit(ElseBranch ifs){
	  //do nothing this is not needed yet
	}
        
        @Override
	public void visit(WhileElifBranch ifs){
	  //do nothing this is not needed yet
	}

        @Override
	public void visit(RepeatBranch repeatStatement){
		//do nothing this is not needed yet
	}
        
        @Override
	public void visit(Assignment assignment) {
		//do nothing this is not needed yet
	}

        @Override
	public void visit(ForBranch assignment) {
	    //do nothing this is not needed yet
	}

	@Override
	public void visit(EmptyStatement emptyStatement){
		//do nothing this is not needed yet
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
	public void visit(StrValue strValue) {
	   
	}

        @Override
	public void visit(BoolValue boolValue) {
	   
	}

	@Override
	public void visit(Identifier identifier) {
	    int value = (int)environment.get(identifier.getLexeme()).getValue();
	    accumulator.push(value);
	}

	@Override
	public void visit(ParamaterDeclaration declaration) {
		
	}

	@Override
	public void visit(Library library) {
	}

	@Override
	public void visit(Asm asm) {
	}
}
