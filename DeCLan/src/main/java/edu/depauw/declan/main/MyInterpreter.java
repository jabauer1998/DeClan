
package edu.depauw.declan.main;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.ProcedureDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.IfElifBranch;
import edu.depauw.declan.common.ast.WhileElifBranch;
import edu.depauw.declan.common.ast.ForBranch;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.ElseBranch;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Branch;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.FunctionCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.ForAssignment;
import edu.depauw.declan.common.ast.VariableEntry;
import edu.depauw.declan.common.ast.ProcedureEntry;
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
  private Environment <ProcedureEntry> procEnvironment;
  // TODO declare any data structures needed by the interpreter
	
  public MyInterpreter(ErrorLog errorLog) {
    this.errorLog = errorLog;
    this.varEnvironment = new Environment<>();
    this.procEnvironment = new Environment<>();
  }

  @Override
  public void visit(Program program) {
    procEnvironment.addScope();
    varEnvironment.addScope();
    for (Declaration Decl : program.getDecls()) {
      Decl.accept(this);
    }
    for (Statement statement : program.getStatements()) {
      statement.accept(this);
    }
    varEnvironment.removeScope();
    procEnvironment.removeScope();
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
  public void visit(ProcedureDeclaration procDecl) {
    procEnvironment.addEntry(procDecl.getProcedureName().getLexeme(), new ProcedureEntry(procDecl));
  }
        
  @Override
  public void visit(ProcedureCall procedureCall) {
    if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
      Number value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT("" + value.intValue());
    } else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
      Number value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT("" + value.doubleValue());
    } else {
      String funcName = procedureCall.getProcedureName().getLexeme();
      ProcedureEntry pentry = procEnvironment.findEntry(funcName);
      List<VariableDeclaration> args = pentry.getArguments();
      varEnvironment.addScope();
      if(args != null && args.size() > 0){
	List<Expression> valArgs = procedureCall.getArguments();
	for(int i = 0; i < args.size(); i++){
	  args.get(i).accept(this); //declare parameter variables 
	  VariableEntry toChange = varEnvironment.findEntry(args.get(i).getIdentifier().getLexeme());
	  Number variableValue = valArgs.get(i).acceptResult(this);
	  toChange.setValue(variableValue);
	}
      }
      List<Declaration> LocalDecl = pentry.getLocalVariables();
      for(Declaration decl : LocalDecl){
	decl.accept(this);
      }
      List<Statement> toExec = pentry.getExecList();
      if(toExec != null && toExec.size() > 0){
	for(Statement toDo : toExec){
	  toDo.accept(this);
	}
      }
      varEnvironment.removeScope(); //clean up local declarations as well as parameters	
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
  public void visit(ForBranch forbranch){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
      Number incriment = toMod.acceptResult(this);
      if(incriment instanceof Double){
	varEnvironment.addScope();
	forbranch.getInitAssignment().accept(this);
	while(forbranch.getTargetExpression().acceptResult(this).intValue() != 0){
	  varEnvironment.addScope();
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  varEnvironment.removeScope();
	  VariableEntry entry = varEnvironment.findEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	  entry.setValue(entry.getValue().doubleValue() + incriment.doubleValue());
	}
	varEnvironment.removeScope();
      } else {
	varEnvironment.addScope();
	forbranch.getInitAssignment().accept(this);
	while(forbranch.getTargetExpression().acceptResult(this).intValue() != 0){
	  varEnvironment.addScope();
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  varEnvironment.removeScope();
	  VariableEntry entry = varEnvironment.findEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	  entry.setValue(entry.getValue().intValue() + incriment.intValue());
	}
	varEnvironment.removeScope();
      }
    } else {
      varEnvironment.addScope();
      forbranch.getInitAssignment().accept(this);
      while(forbranch.getTargetExpression().acceptResult(this).intValue() != 0){
	varEnvironment.addScope();
	for(int i = 0; i < toExec.size(); i++){
	  toExec.get(i).accept(this);
	}
	varEnvironment.removeScope();
      }
      varEnvironment.removeScope();
    }
  }
        
  @Override
  public void visit(Assignment assignment) {
    String name = assignment.getVariableName().getLexeme();
    if(varEnvironment.entryExists(name)){
      VariableEntry entry = varEnvironment.findEntry(name);
      if(entry.getType() != VariableEntry.VarType.CONST){
	if(entry.getType() == VariableEntry.VarType.REAL){
	  Number value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue(value.doubleValue());
	} else if(entry.getType() == VariableEntry.VarType.INTEGER){
	  Number value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue(value.intValue());
	}
      } else {
	FATAL("Variable " + assignment.getVariableName().getLexeme() + " at " + assignment.getVariableName().getStart() + " declared as const");
      }
    } else {
      FATAL("Undeclared Variable " + assignment.getVariableName().getLexeme() + " at " + assignment.getVariableName().getStart());
    }
  }
  @Override
  public void visit(ForAssignment assignment) {
    String name = assignment.getVariableName().getLexeme();
    Number value = assignment.getVariableValue().acceptResult(this);
    if(value instanceof Double){
      String valueStr = "" + value.doubleValue();
      varEnvironment.addEntry(name, new VariableEntry("DOUBLE", valueStr));
    } else {
      String valueStr = "" + value.intValue();
      varEnvironment.addEntry(name, new VariableEntry("INTEGER", valueStr));
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
  public void visit(FunctionCall funcCall){
    //donotuse
  }
  @Override
  public Number visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    ProcedureEntry fentry = procEnvironment.findEntry(funcName);
    List<VariableDeclaration> args = fentry.getArguments();
    varEnvironment.addScope();
    if(args != null && args.size() > 0){
      List<Expression> valArgs = funcCall.getArguments();
      for(int i = 0; i < args.size(); i++){
	args.get(i).accept(this); //declare parameter variables 
	VariableEntry toChange = varEnvironment.findEntry(args.get(i).getIdentifier().getLexeme());
	Number variableValue = valArgs.get(i).acceptResult(this);
	toChange.setValue(variableValue);
      }
    }
    List<Declaration> LocalDecl = fentry.getLocalVariables();
    for(Declaration decl : LocalDecl){
      decl.accept(this);
    }
    List<Statement> toExec = fentry.getExecList();
    if(toExec != null && toExec.size() > 0){
      for(Statement toDo : toExec){
	toDo.accept(this);
      }
    }
    if(fentry.getType() == ProcedureEntry.ProcType.VOID){
      FATAL("Return Type is Void it should be either Integer, Real, or Boolean");
    }
    Number retValue = fentry.getReturnStatement().acceptResult(this);
    varEnvironment.removeScope(); //clean up local declarations as well as parameters
    return retValue;
  }

  @Override
  public Number visitResult(UnaryOperation unaryOperation) {
    Number value = unaryOperation.getExpression().acceptResult(this);
    if(value instanceof Double){
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
    Number lexeme = ident.getValue();
    if(ident.getType() == VariableEntry.VarType.CONST){
      if(lexeme instanceof Double){
	return lexeme.doubleValue();
      } else {
	return lexeme.intValue();
      }
    } else if(ident.getType() == VariableEntry.VarType.INTEGER){
      return lexeme.intValue();
    } else if (ident.getType() == VariableEntry.VarType.REAL){
      return lexeme.doubleValue();
    } else if (ident.getType() == VariableEntry.VarType.BOOLEAN){
      return (int)((lexeme.intValue() != 0) ? 1 : 0);
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
