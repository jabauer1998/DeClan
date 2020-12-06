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
import edu.depauw.declan.common.ast.StrValue;
import edu.depauw.declan.common.ast.BoolValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.FunctionCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.Assignment;

import edu.depauw.declan.common.symboltable.VariableEntry;
import edu.depauw.declan.common.symboltable.ProcedureEntry;
import edu.depauw.declan.common.symboltable.Environment;

import static edu.depauw.declan.common.MyIO.*;

import java.lang.Number;
import java.lang.Object;
import java.lang.Math;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *The my interpreter class is a visitor object that can interpret the entire DeClan Language
 * It also takes in an error log object in order to record errors
 *@author Jacob Bauer
 */

public class MyInterpreter implements ASTVisitor, ExpressionVisitor<Object> {
  private ErrorLog errorLog;
  private Environment <String, VariableEntry> varEnvironment;
  private Environment <String, ProcedureEntry> procEnvironment;
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

    //this function is needed to change a Hex String from the Declan Format to the expected Java format
  private static String ifHexToInt(String lexeme){
    if(lexeme.charAt(0) == '0' && lexeme.length() > 1 && !lexeme.contains(".")){ //is it a hex number
      Long value = Long.parseLong(lexeme.substring(1, lexeme.length() - 1), 16);  
      return ("" + value);
    } else {
      return lexeme; //else return input it is fine
    }
  }
  
  @Override
  public void visit(ConstDeclaration constDecl) {
    Identifier id = constDecl.getIdentifier();
    Expression valueExpr = constDecl.getValue();
    Object value = valueExpr.acceptResult(this);
    if(value instanceof Integer){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(true, (Integer)value));
    } else if (value instanceof Boolean) {
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(true, (Boolean)value));
    } else if (value instanceof String) {
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(true, (String)value));
    } else if(value instanceof Double) {
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(true, (Double)value));
    } else {
	errorLog.add("Error invalid constant expression for constant " + id.getLexeme(), id.getStart());
    }
  }

  @Override
  public void visit(VariableDeclaration varDecl) {
    Identifier id = varDecl.getIdentifier();
    String type = varDecl.getType().getLexeme();
    if(type.equals("BOOLEAN")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, new Boolean(false)));
    } else if (type.equals("REAL")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, new Double(0)));
    } else if (type.equals("STRING")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, new String("")));
    } else if (type.equals("INTEGER")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, new Integer(0)));
    } else {
	errorLog.add("Variable " + id.getLexeme() + " is of unknown type " + type, varDecl.getStart());
    }
  }

  @Override
  public void visit(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    List <VariableDeclaration> args = procDecl.getArguments();
    String returnType = procDecl.getReturnType().getLexeme();
    List <Declaration> localVars = procDecl.getLocalVariables();
    List <Statement> Exec = procDecl.getExecutionStatements();
    Expression retExp = procDecl.getReturnStatement();
    procEnvironment.addEntry(procedureName, new ProcedureEntry(args, returnType, localVars, Exec, retExp));
  }
        
  @Override
  public void visit(ProcedureCall procedureCall) {
    if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
      Object value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT("" + (Integer)value);
    } else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble") || procedureCall.getProcedureName().getLexeme().equals("PrintReal")) {
      Object value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT("" + (Double)value);
    } else if(procedureCall.getProcedureName().getLexeme().equals("PrintString")) {
      Object value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT((String)value);
    } else if(procedureCall.getProcedureName().getLexeme().equals("ASSERT")){
      Boolean value = (Boolean)procedureCall.getArguments().get(0).acceptResult(this);
      String toPrint = (String)procedureCall.getArguments().get(1).acceptResult(this);
      if(!value){
	  OUT(toPrint);
	  System.exit(1);
      }
    } else if(procedureCall.getProcedureName().getLexeme().equals("PrintLn")){
	
    } else {
	String funcName = procedureCall.getProcedureName().getLexeme();
	ProcedureEntry pentry = procEnvironment.getEntry(funcName);
	List<VariableDeclaration> args = pentry.getArguments();
	List<Expression> valArgs = procedureCall.getArguments();
	List<Object> valArgResults = new ArrayList<>();
	for(Expression valArg : valArgs){
	    Object result = valArg.acceptResult(this);
	    valArgResults.add(result);
	}
	varEnvironment.addScope();
	if(args.size() == valArgs.size()){
	    for(int i = 0; i < args.size(); i++){
		args.get(i).accept(this); //declare parameter variables 
		VariableEntry toChange = varEnvironment.getEntry(args.get(i).getIdentifier().getLexeme());
		Object variableValue = valArgResults.get(i);
		toChange.setValue(variableValue);
	    }
	} else {
	    errorLog.add("Unexpected amount of arguments provided from Caller to Callie in Function " + funcName, procedureCall.getStart());
	}
	List<Declaration> LocalDecl = pentry.getLocalVariables();
	for(Declaration decl : LocalDecl){
	    decl.accept(this);
	}
	List<Statement> toExec = pentry.getExecList();
	for(Statement toDo : toExec){
	    toDo.accept(this);
	}
	varEnvironment.removeScope(); //clean up local declarations as well as parameters	
    }
  }


  @Override
  public void visit(WhileElifBranch whilebranch){
    Expression toCheck = whilebranch.getExpression();
    if((Boolean)toCheck.acceptResult(this)){
      List<Statement> toExec = whilebranch.getExecStatements();
      do {
	for(int i = 0; i < toExec.size(); i++){
	  toExec.get(i).accept(this);
	}
      } while((Boolean)toCheck.acceptResult(this));
    } else if (whilebranch.getNextBranch() != null){
      whilebranch.getNextBranch().accept(this);
    }
  }
    
  @Override
  public void visit(IfElifBranch ifbranch){
    Expression toCheck = ifbranch.getExpression();
    if((Boolean)toCheck.acceptResult(this)){
      List<Statement> toExec = ifbranch.getExecStatements();
      for(int i = 0; i < toExec.size(); i++){
	toExec.get(i).accept(this);
      }
    } else if(ifbranch.getNextBranch() != null) {
      ifbranch.getNextBranch().accept(this);
    }
  }

  @Override
  public void visit(ElseBranch elsebranch){
    List<Statement> toExec = elsebranch.getExecStatements();
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }
  }

  @Override
  public void visit(RepeatBranch repeatbranch){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    do {
      for(int i = 0; i < toExec.size(); i++){
	toExec.get(i).accept(this);
      }
    } while (!(Boolean)toCheck.acceptResult(this)); //keep going until statement is true
  }

  @Override
  public void visit(ForBranch forbranch){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
      Object incriment = toMod.acceptResult(this);
      if(incriment instanceof Double){
	forbranch.getInitAssignment().accept(this);
	Object target = forbranch.getTargetExpression().acceptResult(this);
	Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
	while((Double)target != (Double)curvalue){
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	  entry.setValue((Double)entry.getValue() + (Double)incriment);
	  curvalue = entry.getValue();
	}
      } else {
	forbranch.getInitAssignment().accept(this);
	Object target = forbranch.getTargetExpression().acceptResult(this);
	Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
	while((Integer)target != (Integer)curvalue){
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	  entry.setValue((Integer)entry.getValue() + (Integer)incriment);
	  curvalue = entry.getValue();
	}
      }
    } else {
      forbranch.getInitAssignment().accept(this);
      Object target = forbranch.getTargetExpression().acceptResult(this);
      Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
      while((Integer)target != (Integer)curvalue){
	for(int i = 0; i < toExec.size(); i++){
	  toExec.get(i).accept(this);
	}
	curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
      }
    }
  }
        
  @Override
  public void visit(Assignment assignment) {
    String name = assignment.getVariableName().getLexeme();
    if(varEnvironment.entryExists(name)){
      VariableEntry entry = varEnvironment.getEntry(name);
      if(entry.isConst()){
	  errorLog.add("Variable " + assignment.getVariableName().getLexeme() + " declared as const", assignment.getVariableName().getStart());
      } else if(entry.getValue() instanceof Double){
	  Object value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue((Double)value);
      } else if(entry.getValue() instanceof Integer){
	  Object value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue((Integer)value);
      } else if(entry.getValue() instanceof String){
	  Object value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue((String)value);
      } else if(entry.getValue() instanceof Boolean){
	  Object value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue((Boolean)value);
      } else {
	  errorLog.add("Variable in Assignment " + name + " is of unknown DeClan type?", assignment.getStart());
      }
    } else {
	errorLog.add("Undeclared Variable " + assignment.getVariableName().getLexeme(), assignment.getVariableName().getStart());
    }
  }
  
  @Override
  public void visit(EmptyStatement emptyStatement) {
    // Not used
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
  public void visit(StrValue numValue) {
    // Not used
  }

  @Override
  public void visit(BoolValue boolValue) {
    // Not used
  }

  @Override
  public void visit(Identifier identifier) {
    // Not used
  }

  @Override
  public void visit(FunctionCall funcCall){
    // Not used
  }
  
  @Override
  public Object visitResult(BinaryOperation binaryOperation) {
    Object leftValue = binaryOperation.getLeft().acceptResult(this);
    Object rightValue = binaryOperation.getRight().acceptResult(this);
    if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
	switch(binaryOperation.getOperator()){
	case NE:
	    return (Boolean)leftValue != (Boolean)rightValue;
        case EQ:
	    return (Boolean)leftValue == (Boolean)rightValue;
        case AND:
	    return (Boolean)leftValue && (Boolean)rightValue;
        case OR:
	    return (Boolean)leftValue || (Boolean)rightValue;
	default:
	    errorLog.add("Invalid operation between " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), binaryOperation.getStart());
	    return null;
	}
    } else if((leftValue instanceof Double && rightValue instanceof Integer)){
	switch (binaryOperation.getOperator()) {
	case PLUS:
	    return (Double)leftValue + (Integer)rightValue;
	case MINUS:
	    return (Double)leftValue - (Integer)rightValue;
	case TIMES:
	    return (Double)leftValue * (Integer)rightValue;
	case DIVIDE:
	    return (Double)leftValue / (Integer)rightValue;
	case DIV:
	    return (int)((Double)leftValue/(Integer)rightValue);
	case LT:
	    return (Boolean)((Double)leftValue < (Integer)rightValue);
	case GT:
	    return (Boolean)((Double)leftValue > (Integer)rightValue);
	case GE:
	    return (Boolean)((Double)leftValue >= (Integer)rightValue);
	case LE:
	    return (Boolean)((Double)leftValue <= (Integer)rightValue);
	default:
	    errorLog.add("Invalid operation between " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), binaryOperation.getStart());
	    return null;
	}
    } else if (rightValue instanceof Double && leftValue instanceof Integer) {
	switch (binaryOperation.getOperator()) {
	case PLUS:
	    return (Integer)leftValue + (Double)rightValue;
	case MINUS:
	    return (Integer)leftValue - (Double)rightValue;
	case TIMES:
	    return (Integer)leftValue * (Double)rightValue;
	case DIV:
	    return (int)((Integer)leftValue/(Double)rightValue);
	case DIVIDE:
	    return (Integer)leftValue / (Double)rightValue;
	case LT:
	    return (Boolean)((Integer)leftValue < (Double)rightValue);
	case GT:
	    return (Boolean)((Integer)leftValue > (Double)rightValue);
	case GE:
	    return (Boolean)((Integer)leftValue >= (Double)rightValue);
	case LE:
	    return (Boolean)((Integer)leftValue <= (Double)rightValue);
	default:
	    errorLog.add("Invalid operation between " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), binaryOperation.getStart());
	    return null;
	}
    } else if (leftValue instanceof Double && rightValue instanceof Double) {
	switch (binaryOperation.getOperator()) {
	case PLUS:
	    return (Double)leftValue + (Double)rightValue;
	case MINUS:
	    return (Double)leftValue - (Double)rightValue;
	case TIMES:
	    return (Double)leftValue * (Double)rightValue;
	case DIV:
	    return (int)((Double)leftValue / (Double)rightValue);
	case DIVIDE:
	    return (Double)leftValue / (Double)rightValue;
	case MOD:
	    return (Double)leftValue % (Double)rightValue;
	case LT:
	    return (Boolean)((Double)leftValue < (Double)rightValue);
	case GT:
	    return (Boolean)((Double)leftValue > (Double)rightValue);
	case NE:
	    return (Boolean)((Double)leftValue != (Double)rightValue);
	case EQ:
	    return (Boolean)((Double)leftValue == (Double)rightValue);
	case GE:
	    return (Boolean)((Double)leftValue >= (Double)rightValue);
	case LE:
	    return (Boolean)((Double)leftValue <= (Double)rightValue);
	default:
	    errorLog.add("Invalid operation between " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), binaryOperation.getStart());
	    return null;
	}
    } else if (rightValue instanceof Integer && leftValue instanceof Integer) {
	switch (binaryOperation.getOperator()) {
	case PLUS:
	    return (Integer)leftValue + (Integer)rightValue;
	case MINUS:
	    return (Integer)leftValue - (Integer)rightValue;
	case TIMES:
	    return (Integer)leftValue * (Integer)rightValue;
	case DIV:
	    return (int)((Integer)leftValue / (Integer)rightValue);
	case DIVIDE:
	    return (Integer)leftValue / (Integer)rightValue;
	case LT:
	    return (Boolean)((Integer)leftValue < (Integer)rightValue);
	case GT:
	    return (Boolean)((Integer)leftValue > (Integer)rightValue);
	case NE:
	    return (Boolean)((Integer)leftValue != (Integer)rightValue);
	case EQ:
	    return (Boolean)((Integer)leftValue == (Integer)rightValue);
	case GE:
	    return (Boolean)((Integer)leftValue >= (Integer)rightValue);
	case LE:
	    return (Boolean)((Integer)leftValue <= (Integer)rightValue);
	default:
	    errorLog.add("Invalid operation between " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), binaryOperation.getStart());
	    return null;
	}
    } else {
      errorLog.add("Unknown Operation with " + leftValue.getClass().getSimpleName() + " and " + rightValue.getClass().getSimpleName(), binaryOperation.getStart());
      return null;
    }
  }

  @Override
  public Object visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    ProcedureEntry fentry = procEnvironment.getEntry(funcName);
    List<VariableDeclaration> args = fentry.getArguments();
    List<Expression> valArgs = funcCall.getArguments();
    List<Object> valArgResults = new ArrayList<>();
    for(Expression valArg : valArgs){
	Object result = valArg.acceptResult(this);
	valArgResults.add(result);
    }
    varEnvironment.addScope();
    if(args.size() == valArgs.size()){
      for(int i = 0; i < args.size(); i++){
	args.get(i).accept(this); //declare parameter variable
	VariableEntry toChange = varEnvironment.getEntry(args.get(i).getIdentifier().getLexeme());
	Object variableValue = valArgResults.get(i);
	toChange.setValue(variableValue);
      }
    } else {
	errorLog.add("Unexpected amount of arguments provided from Caller to Callie in Function " + funcCall.getFunctionName().getLexeme(), funcCall.getStart());
    }
    List<Declaration> LocalDecl = fentry.getLocalVariables();
    for(Declaration decl : LocalDecl){
      decl.accept(this);
    }
    List<Statement> toExec = fentry.getExecList();
    for(Statement toDo : toExec){
      toDo.accept(this);
    }
    if(fentry.getType() == ProcedureEntry.ProcType.VOID){
	errorLog.add("Return Type is Void it should be either INTEGER, REAL, or BOOLEAN", funcCall.getStart());
    }
    Object retValue = fentry.getReturnStatement().acceptResult(this);
    varEnvironment.removeScope(); //clean up local declarations as well as parameters
    return retValue;
  }

  @Override
  public Object visitResult(UnaryOperation unaryOperation) {
    Object value = unaryOperation.getExpression().acceptResult(this);
    if (value instanceof Boolean && (unaryOperation.getOperator() == UnaryOperation.OpType.NOT)){
      return !(Boolean)value;
    } else if(value instanceof Double){
      switch (unaryOperation.getOperator()){
      case PLUS:
	return value;
      case MINUS:
	return -(Double)value;
      default:
	return null;
      }
    } else if (value instanceof Integer) {
      switch (unaryOperation.getOperator()){
      case PLUS:
	return value;
      case MINUS:
	return -(Integer)value;
      default:
	errorLog.add("Unexpected operator in unary operation of type " + value.getClass().getSimpleName(), unaryOperation.getStart());
	return null;
      }
    } else {
      errorLog.add("Unexpected type in unary operation",  unaryOperation.getStart());
      return null;
    }
  }
    
  @Override
  public Object visitResult(Identifier identifier){
    VariableEntry ident = varEnvironment.getEntry(identifier.getLexeme());
    Object value = ident.getValue();
    if(value != null){
      return value;
    } else {
      errorLog.add("Identifier " + identifier.getLexeme() + " wasnt found in the table", identifier.getStart());
      return null;
    }
  }

  @Override
  public Object visitResult(NumValue numValue){
    String lexeme = ifHexToInt(numValue.getLexeme()); //change to hex if you need to otherwise unchanged
    if(lexeme.contains(".")){
      return Double.parseDouble(lexeme);
    } else {
      return Integer.parseInt(lexeme);
    }
  }

  @Override
  public Object visitResult(BoolValue boolValue){
    String lexeme = boolValue.getLexeme(); //change to hex if you need to otherwise unchanged
    if(lexeme.equals("TRUE")){
	return true;
    } else {
	return false;
    }
  }

  @Override
  public Object visitResult(StrValue strValue){
    return strValue.getLexeme();
  }
}
