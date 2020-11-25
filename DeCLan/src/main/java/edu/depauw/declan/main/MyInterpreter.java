
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
import edu.depauw.declan.common.ast.ForAssignment;
import edu.depauw.declan.common.ast.VariableEntry;
import edu.depauw.declan.common.ast.ProcedureEntry;
import edu.depauw.declan.common.ast.Environment;


import java.lang.Number;
import java.lang.Object;
import java.lang.Math;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


import static edu.depauw.declan.common.MyIO.*;

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
  
  private static String ifHexToInt(String lexeme){
    if(lexeme.charAt(0) == '0' && lexeme.length() > 1 && !lexeme.contains(".")){ //is it a hex number
      int value = (int)Long.parseLong(lexeme.substring(1, lexeme.length() - 1), 16);  
      return ("" + value);
    } else {
      return lexeme; //else returninput it is fine
    }
  }
  
  @Override
  public void visit(ConstDeclaration constDecl) {
    Identifier id = constDecl.getIdentifier();
    NumValue num = constDecl.getNumber();
    varEnvironment.addEntry(id.getLexeme(), new VariableEntry("CONST", ifHexToInt(num.getLexeme())));
  }

  @Override
  public void visit(VariableDeclaration varDecl) {
    Identifier id = varDecl.getIdentifier();
    Identifier type = varDecl.getType();
    varEnvironment.addEntry(id.getLexeme(), new VariableEntry(type.getLexeme()));
  }

  @Override
  public void visit(ProcedureDeclaration procDecl) {
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
      Serializable value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT("" + value.intValue());
    } else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
      Serializable value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT("" + value.doubleValue());
    } else if(procedureCall.getProcedureName().getLexeme().equals("PrintString")) {
      Serializable value = procedureCall.getArguments().get(0).acceptResult(this);
      OUT(value);
    } else if(procedureCall.getProcedureName().getLexeme().equals("PrintLn")){
      OUT("\n");
    } else {
      String funcName = procedureCall.getProcedureName().getLexeme();
      ProcedureEntry pentry = procEnvironment.findEntry(funcName);
      List<VariableDeclaration> args = pentry.getArguments();
      varEnvironment.addScope();
      if(args.size() > 0){
	List<Expression> valArgs = procedureCall.getArguments();
	if(args.size() == valArgs.size()){
	  for(int i = 0; i < args.size(); i++){
	    args.get(i).accept(this); //declare parameter variables 
	    VariableEntry toChange = varEnvironment.findEntry(args.get(i).getIdentifier().getLexeme());
	    Serializable variableValue = valArgs.get(i).acceptResult(this);
	    toChange.setValue(variableValue);
	  }
	} else {
	  FATAL("Unexpected amount of arguments provided from Caller to Callie in Function " + procedureCall.getProcedureName().getLexeme());
	}
      }
      List<Declaration> LocalDecl = pentry.getLocalVariables();
      for(Declaration decl : LocalDecl){
        decl.accept(this);
      }
      List<Statement> toExec = pentry.getExecList();
      if(toExec.size() > 0){
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
	for(int i = 0; i < toExec.size(); i++){
	  toExec.get(i).accept(this);
	}
      } while (((Number)toCheck.acceptResult(this)).intValue() != 0);
    } else if (whilebranch.getNextBranch() != null){
      whilebranch.getNextBranch().accept(this);
    }
  }
    
  @Override
  public void visit(IfElifBranch ifbranch){
    Expression toCheck = ifbranch.getExpression();
    if(toCheck.acceptResult(this).intValue() != 0){
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
    } while (toCheck.acceptResult(this).intValue() == 0); //keep going until statement is true
  }

  @Override
  public void visit(ForBranch forbranch){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
      Serializable incriment = toMod.acceptResult(this);
      if(incriment instanceof Double){
	varEnvironment.addScope();
	forbranch.getInitAssignment().accept(this);
	while(forbranch.getTargetExpression().acceptResult(this).intValue() != 0){
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  VariableEntry entry = varEnvironment.findEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	  entry.setValue(entry.getValue().doubleValue() + incriment.doubleValue());
	}
	varEnvironment.removeScope();
      } else {
	varEnvironment.addScope();
	forbranch.getInitAssignment().accept(this);
	while(forbranch.getTargetExpression().acceptResult(this).intValue() != 0){
	  for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	  }
	  VariableEntry entry = varEnvironment.findEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	  entry.setValue(entry.getValue().intValue() + incriment.intValue());
	}
	varEnvironment.removeScope();
      }
    } else {
      varEnvironment.addScope();
      forbranch.getInitAssignment().accept(this);
      while(forbranch.getTargetExpression().acceptResult(this).intValue() != 0){
	for(int i = 0; i < toExec.size(); i++){
	  toExec.get(i).accept(this);
	}
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
	  Serializable value = assignment.getVariableValue().acceptResult(this);
	  entry.setValue(value.doubleValue());
	} else if(entry.getType() == VariableEntry.VarType.INTEGER){
	  Serializable value = (Number)assignment.getVariableValue().acceptResult(this);
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
    Serializable value = assignment.getVariableValue().acceptResult(this);
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
  public void visit(StrValue numValue) {
    // Not used
  }

  @Override
  public void visit(BoolValue numValue) {
    // Not used
  }

  @Override
  public void visit(Identifier identifier) {
    // Not used
  }
  
  @Override
  public Object visitResult(BinaryOperation binaryOperation) {
    Object leftValue = binaryOperation.getLeft().acceptResult(this);
    Object rightValue = binaryOperation.getRight().acceptResult(this);
    if (leftValue instanceof Boolean && rightValue instanceof Boolean) {
      case NE:
	return (Boolean)leftValue != (Boolean)rightValue;
      case EQ:
	return (Boolean)leftValue == (Boolean)rightValue;
      case AND:
	return (Boolean)leftValue && (Boolean)rightValue;
      case OR:
	return (Boolean)leftValue || (Boolean)rightValue;
    } else if((leftValue instanceof Double && rightValue instanceof Integer) || (rightValue instanceof Double && leftValue instanceof Integer)){
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
	return (Double)leftValue < (Double)rightValue;
      case GT:
	return (Double)leftValue > (Double)rightValue;
      case NE:
	return (Double)leftValue != (Double)rightValue;
      case EQ:
	return (Double)leftValue == (Double)rightValue;
      case GE:
	return (Double)leftValue >= (Double)rightValue;
      case LE:
	return (Double)leftValue <= (Double)rightValue;
      }
    } else if (leftValue instanceof Integer && rightValue instanceof Integer) {
      switch (binaryOperation.getOperator()) {
      case PLUS:
	return (Integer)leftValue + (Integer)rightValue.intValue();
      case MINUS:
	return (Integer)leftValue - (Integer)rightValue.intValue();
      case TIMES:
	return (Integer)leftValue * (Integer)rightValue.intValue();
      case DIV:
	return (Integer)((Integer)leftValue / (Integer)rightValue);
      case DIVIDE:
	return (Integer)leftValue / (Integer)rightValue;
      case MOD:
	return (Integer)leftValue % (Integer)rightValue;
      case LT:
	return (Integer)leftValue < (Integer)rightValue;
      case GT:
	return (Integer)leftValue > (Integer)rightValue;
      case NE:
	return (Integer)leftValue != (Integer)rightValue;
      case EQ:
	return (Integer)leftValue == (Integer)rightValue;
      case GE:
	return (Integer)leftValue >= (Integer)rightValue;
      case LE:
	return (Integer)leftValue <= (Integer)rightValue;
      }
    } else {
      FATAL("Unknown Operation Performed");
    }
    return null;
  }

  @Override
  public void visit(FunctionCall funcCall){
    //donotuse
  }
  @Override
  public Object visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    ProcedureEntry fentry = procEnvironment.findEntry(funcName);
    List<VariableDeclaration> args = fentry.getArguments();
    varEnvironment.addScope();
    if(args.size() > 0){
      List<Expression> valArgs = funcCall.getArguments();
      if(args.size() == valArgs.size()){
	for(int i = 0; i < args.size(); i++){
	  args.get(i).accept(this); //declare parameter variable
	  VariableEntry toChange = varEnvironment.findEntry(args.get(i).getIdentifier().getLexeme());
	  Number variableValue = (Number)valArgs.get(i).acceptResult(this);
	  toChange.setValue(variableValue);
	}
      } else {
	FATAL("Unexpected amount of arguments provided from Caller to Callie in Function " + funcCall.getFunctionName().getLexeme());
      }
    }
    List<Declaration> LocalDecl = fentry.getLocalVariables();
    for(Declaration decl : LocalDecl){
      decl.accept(this);
    }
    List<Statement> toExec = fentry.getExecList();
    if(toExec.size() > 0){
      for(Statement toDo : toExec){
	toDo.accept(this);
      }
    }
    if(fentry.getType() == ProcedureEntry.ProcType.VOID){
      FATAL("Return Type is Void it should be either INTEGER, REAL, or BOOLEAN");
    }
    Serializable retValue = fentry.getReturnStatement().acceptResult(this);
    varEnvironment.removeScope(); //clean up local declarations as well as parameters
    return retValue;
  }

  @Override
  public Object visitResult(UnaryOperation unaryOperation) {
    Object value = unaryOperation.getExpression().acceptResult(this);
    if (value instanceof Boolean && (unaryOperation.getOperator() == unaryOperation.OpType.NOT)){
      return !(Boolean)value;
    } else if(value instanceof Double){
      switch (unaryOperation.getOperator()){
      case PLUS:
	return value;
      case MINUS:
	return -(Double)value;
      }
    } else if (value instanceof Integer) {
      switch (unaryOperation.getOperator()){
      case PLUS:
	return value;
      case MINUS:
	return -(Integer)value;
      }
    } else {
      FATAL("Unexpected type in unary operation");
      return null;
    }
  }
    
  @Override
  public Object visitResult(Identifier identifier){
    VariableEntry ident = varEnvironment.findEntry(identifier.getLexeme());
    Object value = ident.getValue();
    if(value != null){
      return value;
    } else {
      FATAL("Identifier wasnt found in the table");
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
  public Object visitResult(StrValue numValue){
    return numValue.getLexeme();
  }
}
