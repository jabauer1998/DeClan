package edu.depauw.declan.main;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;

import java.util.Map;
import java.util.HashMap;

import static edu.depauw.declan.common.MyIO.*;

public class MyInterpreter implements ASTVisitor, ExpressionVisitor<Integer> {
	private ErrorLog errorLog;
        private Map<String, String> environment;
	// TODO declare any data structures needed by the interpreter
	
	public MyInterpreter(ErrorLog errorLog) {
		this.errorLog = errorLog;
		this.environment = new HashMap<>();
	}

	@Override
	public void visit(Program program) {
	        for (ConstDeclaration constDecl : program.getConstDecls()) {
			constDecl.accept(this);
		}
		for (Statement statement : program.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(ConstDeclaration constDecl) {
	        Identifier id = constDecl.getIdentifier();
		NumValue num = constDecl.getNumber();
		environment.put(id.getLexeme(), num.getLexeme());
	}

	@Override
	public void visit(ProcedureCall procedureCall) {
		if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
			int value = procedureCall.getArgument().acceptResult(this);
			OUT("" + value);
		} else {
			// Ignore all other procedure calls
		}
	}
        @Override
	public void visit(EmptyStatement emptyStatement) {
		// TODO Auto-generated method stub

	}
        @Override
	public void visit(UnaryOperation unaryOperation) {
		// Not used
	}

	@Override
	public void visit(BinaryOperation binaryOperation) {
		// Not used
	}

	@Override
	public void visit(NumValue numValue) {
		// Not used
	}

	@Override
	public void visit(Identifier identifier) {
		// Not used
	}
    
	@Override
	public Integer visitResult(BinaryOperation binaryOperation) {
		Integer leftvalue = binaryOperation.getLeft().acceptResult(this);
		Integer rightvalue = binaryOperation.getRight().acceptResult(this);
		switch (binaryOperation.getOperator()) {
		case PLUS:
		    return leftvalue + rightvalue;
		case MINUS:
		    return leftvalue - rightvalue;
		case TIMES:
		    return leftvalue * rightvalue;
		case DIV:
		    return leftvalue / rightvalue;
		case MOD:
		    return leftvalue % rightvalue;
		}
		return null;
	}

	@Override
	public Integer visitResult(UnaryOperation unaryOperation) {
		Integer value = unaryOperation.getExpression().acceptResult(this);
		switch (unaryOperation.getOperator()) {
		case PLUS:
		    return value;
		case MINUS:
		    return -value;
		}
		return null;
	}

	@Override
	public Integer visitResult(Identifier identifier) {
		return Integer.parseInt(environment.getOrDefault(identifier.getLexeme(), "0"));
	}

	@Override
	public Integer visitResult(NumValue numValue) {
		return Integer.parseInt(numValue.getLexeme());
	}

}
