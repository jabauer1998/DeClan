package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ast.ASTVisitor;
import io.github.H20man13.DeClan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.common.ast.ConstDeclaration;
import io.github.H20man13.DeClan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.ast.ProcedureDeclaration;
import io.github.H20man13.DeClan.common.ast.Declaration;
import io.github.H20man13.DeClan.common.ast.EmptyStatement;
import io.github.H20man13.DeClan.common.ast.IfElifBranch;
import io.github.H20man13.DeClan.common.ast.WhileElifBranch;
import io.github.H20man13.DeClan.common.ast.ForBranch;
import io.github.H20man13.DeClan.common.ast.Expression;
import io.github.H20man13.DeClan.common.ast.ElseBranch;
import io.github.H20man13.DeClan.common.ast.RepeatBranch;
import io.github.H20man13.DeClan.common.ast.Branch;
import io.github.H20man13.DeClan.common.ast.ExpressionVisitor;
import io.github.H20man13.DeClan.common.ast.Identifier;
import io.github.H20man13.DeClan.common.ast.NumValue;
import io.github.H20man13.DeClan.common.ast.StrValue;
import io.github.H20man13.DeClan.common.ast.BoolValue;
import io.github.H20man13.DeClan.common.ast.ProcedureCall;
import io.github.H20man13.DeClan.common.ast.FunctionCall;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.ast.UnaryOperation;
import io.github.H20man13.DeClan.common.ast.Statement;
import io.github.H20man13.DeClan.common.ast.Assignment;

import io.github.H20man13.DeClan.common.symboltable.VariableEntry;
import io.github.H20man13.DeClan.common.symboltable.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.Environment;

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
 *The my Integererpreter class is a visitor object that can Integererpret the entire DeClan Language
 * It also takes in an error log object in order to record errors
 *@author Jacob Bauer
 */

public class MyInterpreter implements ASTVisitor, ExpressionVisitor<Object> {
  private ErrorLog errorLog;
  private Environment <String, VariableEntry> varEnvironment;
  private Environment <String, ProcedureEntry> procEnvironment;
  // TODO declare any data structures needed by the Integererpreter

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
  private static String ifHexToInteger(String lexeme){
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
    varEnvironment.addEntry(id.getLexeme(), new VariableEntry(true, value));
  }

  @Override
  public void visit(VariableDeclaration varDecl) {
    Identifier id = varDecl.getIdentifier();
    String type = varDecl.getType().getLexeme();
    if(type.equals("BOOLEAN")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, false));
    } else if (type.equals("REAL")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, (Double)0.0));
    } else {
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0));
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
      Integer value = (Integer)procedureCall.getArguments().get(0).acceptResult(this);
      System.out.print(value);
    } else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble") || procedureCall.getProcedureName().getLexeme().equals("PrintReal")) {
      Double value = (Double)procedureCall.getArguments().get(0).acceptResult(this);
      System.out.print(value);
    } else if(procedureCall.getProcedureName().getLexeme().equals("PrintString")) {
      String value = (String)procedureCall.getArguments().get(0).acceptResult(this);
      System.out.print(value);
    } else if(procedureCall.getProcedureName().getLexeme().equals("ASSERT")){
      Boolean value = (Boolean)procedureCall.getArguments().get(0).acceptResult(this);
      String toPrint = (String)procedureCall.getArguments().get(1).acceptResult(this);
      if(!value){
	  System.out.print(toPrint);
	  System.exit(1);
      }
    } else if(procedureCall.getProcedureName().getLexeme().equals("PrintLn")){
	System.out.println("");
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
	for(Integer i = 0; i < args.size(); i++){
	    args.get(i).accept(this); //declare parameter variables 
	    VariableEntry toChange = varEnvironment.getEntry(args.get(i).getIdentifier().getLexeme());
	    Object variableValue = valArgResults.get(i);
	    toChange.setValue(variableValue);
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
	for(Integer i = 0; i < toExec.size(); i++){
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
      for(Integer i = 0; i < toExec.size(); i++){
	toExec.get(i).accept(this);
      }
    } else if(ifbranch.getNextBranch() != null) {
      ifbranch.getNextBranch().accept(this);
    }
  }

  @Override
  public void visit(ElseBranch elsebranch){
    List<Statement> toExec = elsebranch.getExecStatements();
    for(Integer i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }
  }

  @Override
  public void visit(RepeatBranch repeatbranch){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    do {
      for(Integer i = 0; i < toExec.size(); i++){
	toExec.get(i).accept(this);
      }
    } while (!(Boolean)toCheck.acceptResult(this)); //keep going until statement is true
  }

  @Override
  public void visit(ForBranch forbranch){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
      forbranch.getInitAssignment().accept(this);
      Object incriment = toMod.acceptResult(this);
      Object target = forbranch.getTargetExpression().acceptResult(this);
      Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
      if(incriment instanceof Double){
	  if((Double)curvalue >= (Double)target && (Double)incriment < 0.0){
	      while((Double)curvalue > (Double)target){
		  for(Integer i = 0; i < toExec.size(); i++){
		      toExec.get(i).accept(this);
		  }
		  VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
		  entry.setValue((Double)curvalue - (Double)incriment);
		  curvalue = entry.getValue();
	      }
	  } else if ((Double)curvalue <= (Double)target && (Double)incriment > 0.0){
	      while((Double)curvalue < (Double)target){
		  for(Integer i = 0; i < toExec.size(); i++){
		      toExec.get(i).accept(this);
		  }
		  VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
		  entry.setValue((Double)curvalue + (Double)incriment);
		  curvalue = entry.getValue();
	      }
	  } else {
	      errorLog.add("Possible infinite forloop in for loop ", forbranch.getStart());
	  }
      } else {
	  if((Integer)curvalue >= (Integer)target && (Integer)incriment < 0){
	      while((Integer)curvalue > (Integer)target){
		  for(Integer i = 0; i < toExec.size(); i++){
		      toExec.get(i).accept(this);
		  }
		  VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
		  entry.setValue((Integer)curvalue - (Integer)curvalue);
		  curvalue = entry.getValue();
	      }
	  } else if ((Integer)curvalue <= (Integer)target && (Integer)incriment > 0){
	      while((Integer)curvalue < (Integer)target){
		  for(Integer i = 0; i < toExec.size(); i++){
		      toExec.get(i).accept(this);
		  }
		  VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
		  entry.setValue((Integer)curvalue + (Integer)incriment);
		  curvalue = entry.getValue();
	      }
	  } else {
	      errorLog.add("Possible infinite forloop in for loop ", forbranch.getStart());
	  }
      }
    } else {
      forbranch.getInitAssignment().accept(this);
      Object target = forbranch.getTargetExpression().acceptResult(this);
      Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
      if(target instanceof Double){
	  while((Double)curvalue > (Double)target){
	      for(Integer i = 0; i < toExec.size(); i++){
		  toExec.get(i).accept(this);
	      }
	  }
      } else {
	  forbranch.getInitAssignment().accept(this);
	  while((Integer)curvalue > (Integer)target){
	      for(Integer i = 0; i < toExec.size(); i++){
		  toExec.get(i).accept(this);
	      }
	  }
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
	  Double value = (Double)assignment.getVariableValue().acceptResult(this);
	  entry.setValue(value);
      } else if(entry.getValue() instanceof Integer){
	  Integer value = (Integer)assignment.getVariableValue().acceptResult(this);
	  entry.setValue(value);
      } else {
	  Boolean value = (Boolean)assignment.getVariableValue().acceptResult(this);
	  entry.setValue(value);
      }
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
    if (leftValue instanceof Boolean || rightValue instanceof Boolean) {
	switch(binaryOperation.getOperator()){
	case NE:
	    return (Boolean)leftValue != (Boolean)rightValue;
        case EQ:
	    return (Boolean)leftValue == (Boolean)rightValue;
        case AND:
	    return (Boolean)leftValue && (Boolean)rightValue;
        default:
	    return (Boolean)leftValue || (Boolean)rightValue;
	}
    } else if (leftValue instanceof Double || rightValue instanceof Double) {
	switch (binaryOperation.getOperator()) {
	case PLUS:
	    return (Double)leftValue + (Double)rightValue;
	case MINUS:
	    return (Double)leftValue - (Double)rightValue;
	case TIMES:
	    return (Double)leftValue * (Double)rightValue;
	case DIVIDE:
	    return (Double)leftValue / (Double)rightValue;
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
	default:
	    return (Boolean)((Double)leftValue <= (Double)rightValue);
	}
    } else {
	switch (binaryOperation.getOperator()) {
	case PLUS:
	    return (Integer)leftValue + (Integer)rightValue;
	case MINUS:
	    return (Integer)leftValue - (Integer)rightValue;
	case TIMES:
	    return (Integer)leftValue * (Integer)rightValue;
	case MOD:
	    return (Integer)leftValue % (Integer)rightValue;
	case DIV:
	    return (Integer)((Integer)leftValue / (Integer)rightValue);
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
	default:
	    return (Boolean)((Integer)leftValue <= (Integer)rightValue);
	}
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
    for(Integer i = 0; i < args.size(); i++){
      args.get(i).accept(this); //declare parameter variable
      VariableEntry toChange = varEnvironment.getEntry(args.get(i).getIdentifier().getLexeme());
      Object variableValue = valArgResults.get(i);
      toChange.setValue(variableValue);
    }
    List<Declaration> LocalDecl = fentry.getLocalVariables();
    for(Declaration decl : LocalDecl){
      decl.accept(this);
    }
    List<Statement> toExec = fentry.getExecList();
    for(Statement toDo : toExec){
      toDo.accept(this);
    }
    Object retValue = fentry.getReturnStatement().acceptResult(this);
    varEnvironment.removeScope(); //clean up local declarations as well as parameters
    return retValue;
  }

  @Override
  public Object visitResult(UnaryOperation unaryOperation) {
    Object value = unaryOperation.getExpression().acceptResult(this);
    if (value instanceof Boolean){
	switch(unaryOperation.getOperator()){
	case NOT:
	    return !(Boolean)value;
	default:
	    return value;
	}
    } else if(value instanceof Double){
      switch (unaryOperation.getOperator()){
      case MINUS:
	return -(Double)value;
      default:
	return value;
      }
    } else {
      switch (unaryOperation.getOperator()){
      case MINUS:
	return -(Integer)value;
      default:
	return value;
      }
    }
  }
    
  @Override
  public Object visitResult(Identifier identifier){
    VariableEntry ident = varEnvironment.getEntry(identifier.getLexeme());
    Object value = ident.getValue();
    return value;
  }

  @Override
  public Object visitResult(NumValue numValue){
    String lexeme = ifHexToInteger(numValue.getLexeme()); //change to hex if you need to otherwise unchanged
    if(lexeme.contains(".")){
      return (Double)Double.parseDouble(lexeme);
    } else {
      return (Integer)Integer.parseInt(lexeme);
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
