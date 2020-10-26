package edu.depauw.declan.main;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.IfElifBranch;
import edu.depauw.declan.common.ast.WhileElifBranch;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.ElseBranch;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Branch;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.VariableEntry;
import edu.depauw.declan.common.ast.Environment;

import java.lang.Number;
import java.lang.Math;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


import static edu.depauw.declan.common.MyIO.*;

public class MyInterpreter implements ASTVisitor, ExpressionVisitor<Number> {
	private ErrorLog errorLog;
        private Environment <VariableEntry> varEnvironment;
	// TODO declare any data structures needed by the interpreter
	
	public MyInterpreter(ErrorLog errorLog) {
		this.errorLog = errorLog;
		this.varEnvironment = new Environment<>();
	}

	@Override
	public void visit(Program program) {
	        varEnvironment.addScope();
	        for (Declaration Decl : program.getDecls()) {
			Decl.accept(this);
		}
		for (Statement statement : program.getStatements()) {
			statement.accept(this);
		}
		varEnvironment.removeScope();
	}

	@Override
	public void visit(ConstDeclaration constDecl) {
	        Identifier id = constDecl.getIdentifier();
		NumValue num = constDecl.getNumber();
		varEnvironment.addEntry(id.getLexeme(), new VariableEntry("CONST", num.getLexeme()));
	}

        @Override
	public void visit(VariableDeclaration varDecl) {
	        Identifier id = varDecl.getIdentifier();
		Identifier type = varDecl.getType();
		varEnvironment.addEntry(id.getLexeme(), new VariableEntry(type.getLexeme()));
	}
        
	@Override
	public void visit(ProcedureCall procedureCall) {
		if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
		        Number value = procedureCall.getArguments().get(0).acceptResult(this);
			OUT("" + value.intValue());
		} else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
		        Number value = procedureCall.getArguments().get(0).acceptResult(this);
			OUT("" + value.doubleValue());
		}
	}


        @Override
	public void visit(WhileElifBranch whilebranch){
	  Expression toCheck = whilebranch.getExpression();
	  if(toCheck.acceptResult(this).intValue() != 0){
	    List<Statement> toExec = whilebranch.getExecStatements();
	    do {
	      varEnvironment.addScope();
	      for(int i = 0; i < toExec.size(); i++){
		toExec.get(i).accept(this);
	      }
	      varEnvironment.removeScope();
	    } while (toCheck.acceptResult(this).intValue() != 0);
	  } else if (whilebranch.getNextBranch() != null){
	    whilebranch.getNextBranch().accept(this);
	  }
	}
    
        @Override
	public void visit(IfElifBranch ifbranch){
	  Expression toCheck = ifbranch.getExpression();
	  if(toCheck.acceptResult(this).intValue() != 0){
	    varEnvironment.addScope();
	    List<Statement> toExec = ifbranch.getExecStatements();
	    for(int i = 0; i < toExec.size(); i++){
	      toExec.get(i).accept(this);
	    }
	    varEnvironment.removeScope();
	  } else if(ifbranch.getNextBranch() != null) {
	    ifbranch.getNextBranch().accept(this);
	  }
	}

        @Override
	public void visit(ElseBranch elsebranch){
	  varEnvironment.addScope();
	  List<Statement> toExec = elsebranch.getExecStatements();
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  varEnvironment.removeScope();
	}

        @Override
	public void visit(RepeatBranch repeatbranch){
	  Expression toCheck = repeatbranch.getExpression();
	  List<Statement> toExec = repeatbranch.getExecStatements();
	  do {
	    varEnvironment.addScope();
	    for(int i = 0; i < toExec.size(); i++){
	      toExec.get(i).accept(this);
	    }
	    varEnvironment.removeScope();
	  } while (toCheck.acceptResult(this).intValue() != 0);
	}
        
        @Override
	public void visit(Assignment assignment) {
	    String name = assignment.getVariableName().getLexeme();
	    if(varEnvironment.entryExists(name)){
		VariableEntry entry = varEnvironment.findEntry(name);
		if(!entry.getType().equals("CONST")){
		    if(entry.getType().equals("REAL")){
			Number value = assignment.getVariableValue().acceptResult(this);
			String newValue = "" + value.doubleValue();
			entry.setValue(newValue);
		    } else if(entry.getType().equals("INTEGER")){
			Number value = assignment.getVariableValue().acceptResult(this);
			String newValue = "" + value.intValue();
			entry.setValue(newValue);
		    }
		} else {
		    FATAL("Variable " + assignment.getVariableName().getLexeme() + " at " + assignment.getVariableName().getStart() + " declared as const");
		}
	    } else {
		FATAL("Undeclared Variable " + assignment.getVariableName().getLexeme() + " at " + assignment.getVariableName().getStart());
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
	public Number visitResult(BinaryOperation binaryOperation) {
		Number leftValue = binaryOperation.getLeft().acceptResult(this);
		Number rightValue = binaryOperation.getRight().acceptResult(this);
		if(leftValue instanceof Double || rightValue instanceof Double){
		    switch (binaryOperation.getOperator()) {
		    case PLUS:
		      return leftValue.doubleValue() + rightValue.doubleValue();
		    case MINUS:
		      return leftValue.doubleValue() - rightValue.doubleValue();
		    case TIMES:
		      return leftValue.doubleValue() * rightValue.doubleValue();
		    case DIVIDE:
		      return leftValue.doubleValue() / rightValue.doubleValue();
		    case LT:
		      return (int)((leftValue.doubleValue() < rightValue.doubleValue()) ? 1 : 0);
		    case GT:
		      return (int)((leftValue.doubleValue() > rightValue.doubleValue()) ? 1 : 0);
		    case NE:
		      return (int)((leftValue.doubleValue() != rightValue.doubleValue()) ? 1 : 0);
		    case EQ:
		      return (int)((leftValue.doubleValue() == rightValue.doubleValue()) ? 1 : 0);
		    case GE:
		      return (int)((leftValue.doubleValue() >= rightValue.doubleValue()) ? 1 : 0);
		    case LE:
		      return (int)((leftValue.doubleValue() <= rightValue.doubleValue()) ? 1 : 0);
		    }
		} else {
		    switch (binaryOperation.getOperator()) {
		    case PLUS:
		      return leftValue.intValue() + rightValue.intValue();
		    case MINUS:
		      return leftValue.intValue() - rightValue.intValue();
		    case TIMES:
		      return leftValue.intValue() * rightValue.intValue();
		    case DIV:
		      return leftValue.intValue() / rightValue.intValue();
		    case MOD:
		      return leftValue.intValue() % rightValue.intValue();
		    case LT:
		      return (int)((leftValue.intValue() < rightValue.intValue()) ? 1 : 0);
		    case GT:
		      return (int)((leftValue.intValue() > rightValue.intValue()) ? 1 : 0);
		    case NE:
		      return (int)((leftValue.intValue() != rightValue.intValue()) ? 1 : 0);
		    case EQ:
		      return (int)((leftValue.intValue() == rightValue.intValue()) ? 1 : 0);
		    case GE:
		      return (int)((leftValue.intValue() >= rightValue.intValue()) ? 1 : 0);
		    case LE:
		      return (int)((leftValue.intValue() <= rightValue.intValue()) ? 1 : 0);
		    case AND:
		      return (int)(((leftValue.intValue() != 0) && (rightValue.intValue() != 0)) ? 1 : 0);
		    case OR:
		      return (int)(((leftValue.intValue() != 0) || (rightValue.intValue() != 0)) ? 1 : 0);
		    }
		}
		return null;
	}

	@Override
	public Number visitResult(UnaryOperation unaryOperation) {
		Number value = unaryOperation.getExpression().acceptResult(this);
		if(value instanceof Double)
		{
		    switch (unaryOperation.getOperator()){
		    case PLUS:
			return value.doubleValue();
		    case MINUS:
			return -value.doubleValue();
		    }
		} else {
		    switch (unaryOperation.getOperator()){
		    case PLUS:
			return value.intValue();
		    case MINUS:
			return -value.intValue();
		    case NOT:
		        return (int)((!(value.intValue() != 0)) ? 1 : 0);
		    }
		}
		return null;
	}
    
	@Override
	public Number visitResult(Identifier identifier){
	        VariableEntry ident = varEnvironment.findEntry(identifier.getLexeme());
	        String lexeme = ident.getValue();
		if(ident.getType().equals("CONST")){
		  if(lexeme.contains(".")){
		    return Double.parseDouble(lexeme);
		  } else {
		    return Integer.parseInt(lexeme);
		  }
		} else if(ident.getType().equals("INTEGER")){
		    return Integer.parseInt(lexeme);
		} else if (ident.getType().equals("REAL")){
		    return Double.parseDouble(lexeme);
		} else if (ident.getType().equals("BOOLEAN")){
		  return (int)((Integer.parseInt(lexeme) != 0)? 1 : 0);
		} else {
		  FATAL(identifier.getLexeme() + " at position " + identifier.getStart() + " is unknown type -> " + ident.getType());
		  return null;
		}
	}

	@Override
	public Number visitResult(NumValue numValue){
		String lexeme = numValue.getLexeme();
		if(lexeme.contains(".")){
		  return Double.parseDouble(lexeme);
		} else {
		  return Integer.parseInt(lexeme);
		}
	}
}
