package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.symboltable.VariableEntry;
import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.common.symboltable.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.Environment;

import java.lang.Number;
import java.lang.Object;
import java.io.IOException;
import java.io.Writer;
import java.lang.Math;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Map;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.BoolValue;
import edu.depauw.declan.common.ast.Branch;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.ElseBranch;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.ForBranch;
import edu.depauw.declan.common.ast.FunctionCall;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.IfElifBranch;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ParamaterDeclaration;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.ProcedureDeclaration;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.StrValue;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.WhileElifBranch;

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
  private Writer standardOutput;
  private Writer standardError;

  public MyInterpreter(ErrorLog errorLog, Writer standardOutput, Writer standardError) {
    this.errorLog = errorLog;
    this.standardOutput = standardOutput;
    this.standardError = standardError;
    this.varEnvironment = new Environment<>();
    this.procEnvironment = new Environment<>();
  }

  @Override
  public void visit(Library library){
    procEnvironment.addScope();
    varEnvironment.addScope();
    for(Declaration decl : library.getDecls()){
      decl.accept(this);
    }
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
    List <ParamaterDeclaration> args = procDecl.getArguments();
    String returnType = procDecl.getReturnType().getLexeme();
    List <Declaration> localVars = procDecl.getLocalVariables();
    List <Statement> Exec = procDecl.getExecutionStatements();
    Expression retExp = procDecl.getReturnStatement();
    procEnvironment.addEntry(procedureName, new ProcedureEntry(args, returnType, localVars, Exec, retExp));
  }
        
  @Override
  public void visit(ProcedureCall procedureCall) {
    String procName = procedureCall.getProcedureName().getLexeme();
    if (procName.equals("WriteInt") || procName.equals("PrintInt")) {
      Object value = procedureCall.getArguments().get(0).acceptResult(this);
      Integer intVal = Utils.toInt(value);
      try{
        standardOutput.append("" + intVal);
      } catch(IOException exp){}
    } else if (procName.equals("WriteReal") || procName.equals("PrintReal")) {
      Object value = procedureCall.getArguments().get(0).acceptResult(this);
      Double doubleVal = Utils.toDouble(value);
      try{
        standardOutput.append("" + doubleVal);
      } catch(IOException exp){}
    } else if(procName.equals("WriteLn") || procName.equals("PrintLn")) {
       try{
        standardOutput.append("\n");
       } catch(IOException exp){}
    } else if(procName.equals("WriteString") || procName.equals("PrintString")) {
      Object value = (Object)procedureCall.getArguments().get(0).acceptResult(this);
      try{
        standardOutput.append("" + value);
       } catch(IOException exp){}
    } else if(procName.equals("ASSERT")){
      boolean value = (boolean)procedureCall.getArguments().get(0).acceptResult(this);
      Object toPrint = (Object)procedureCall.getArguments().get(1).acceptResult(this);
        if(!value){
        try{
          standardError.append("" + toPrint);
        } catch(IOException exp){}
	      System.exit(1);
      }
    } else {
        String funcName = procedureCall.getProcedureName().getLexeme();
        ProcedureEntry pentry = procEnvironment.getEntry(funcName);
        List<ParamaterDeclaration> args = pentry.getArguments();
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
    Object res = toCheck.acceptResult(this);
    Boolean boolRes = Utils.toBool(res);
    if(boolRes){
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
	      errorLog.add("Variable " + assignment.getVariableName().getLexeme() + " declared as const ", assignment.getVariableName().getStart());
      } else if(entry.getValue() instanceof Double){
        Object value = assignment.getVariableValue().acceptResult(this);
        Double ivalue = Utils.toDouble(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Integer){
        Object value = (Object)assignment.getVariableValue().acceptResult(this);
        Integer ivalue = Utils.toInt(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Boolean){
        Object value = (Object)assignment.getVariableValue().acceptResult(this);
        Boolean ivalue = Utils.toBool(value);
        entry.setValue(ivalue);
      } else {
        Object value = (Object)assignment.getVariableValue().acceptResult(this);
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
    switch(binaryOperation.getOperator()){
        case NE: return OpUtil.notEqual(leftValue, rightValue);
        case PLUS: return OpUtil.plus(leftValue, rightValue);
        case MINUS: return OpUtil.minus(leftValue, rightValue);
        case TIMES: return OpUtil.times(leftValue, rightValue);
        case DIVIDE: return OpUtil.divide(leftValue, rightValue);
        case DIV: return OpUtil.divide(leftValue, rightValue);
        case LT: return OpUtil.lessThan(leftValue, rightValue);
        case GT: return OpUtil.greaterThan(leftValue, rightValue);
        case EQ: return OpUtil.equal(leftValue, rightValue);
        case GE: return OpUtil.greaterThanOrEqualTo(leftValue, rightValue);
        case LE: return OpUtil.lessThanOrEqualTo(leftValue, rightValue);
        default: return null;
    }
  }

  @Override
  public Object visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    ProcedureEntry fentry = procEnvironment.getEntry(funcName);
    List<ParamaterDeclaration> args = fentry.getArguments();
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
      if(toChange.getValue() instanceof Integer){
         Integer varVal = Utils.toInt(variableValue);
         toChange.setValue(varVal);
      } else if(toChange.getValue() instanceof Double){
        Double varVal = Utils.toDouble(variableValue);
        toChange.setValue(varVal);
      } else if(toChange.getValue() instanceof Boolean){
        Boolean varVal = Utils.toBool(variableValue);
        toChange.setValue(varVal);
      } else {
        toChange.setValue(variableValue);
      }
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
	  switch(unaryOperation.getOperator()){
	    case NOT: return OpUtil.not(value);
      case PLUS: return value;
      case MINUS: return OpUtil.negate(value);
      default: return null;
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


  @Override
  public void visit(ParamaterDeclaration declaration) {
    Identifier id = declaration.getIdentifier();
    String type = declaration.getType().getLexeme();
    if(type.equals("BOOLEAN")){
	      varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, false));
    } else if (type.equals("REAL")){
	      varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, (Double)0.0));
    } else {
	      varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0));
    }
  }
}
