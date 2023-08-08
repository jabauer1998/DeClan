package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;

import java.lang.Object;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.Math;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.Scanner;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.Asm;
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
  private Reader standardIn;

  public MyInterpreter(ErrorLog errorLog, Writer standardOutput, Writer standardError, Reader standardIn) {
    this.errorLog = errorLog;
    this.standardOutput = standardOutput;
    this.standardError = standardError;
    this.standardIn = standardIn;
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
      boolean value = Utils.toBool(procedureCall.getArguments().get(0).acceptResult(this));
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
    if(Utils.toBool(toCheck.acceptResult(this))){
      List<Statement> toExec = whilebranch.getExecStatements();
      do {
        for(Integer i = 0; i < toExec.size(); i++){
          toExec.get(i).accept(this);
        }
      } while(Utils.toBool(toCheck.acceptResult(this)));
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
    boolean result = Utils.toBool(toCheck.acceptResult(this));
    while(!result){
      for(Integer i = 0; i < toExec.size(); i++){
	      toExec.get(i).accept(this);
      }
      result = Utils.toBool(toCheck.acceptResult(this));
    }
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
      if(curvalue instanceof Double){
        Double doubleCurValue = Utils.toDouble(curvalue);
        Double doubleTarget = Utils.toDouble(target);
        Double doubleIncriment = Utils.toDouble(incriment);
        if(doubleIncriment < 0 && doubleCurValue > doubleTarget){
            do{
              for(Integer i = 0; i < toExec.size(); i++){
                  toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              entry.setValue(doubleCurValue + doubleIncriment);
              curvalue = entry.getValue();
              doubleCurValue = Utils.toDouble(curvalue);
	          } while(doubleCurValue > doubleTarget);
        } else if(doubleIncriment > 0 && doubleCurValue < doubleTarget){
           do{
              for(Integer i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              entry.setValue(doubleCurValue + doubleIncriment);
              curvalue = entry.getValue();
              doubleCurValue = Utils.toDouble(curvalue);
	          }while(doubleCurValue < doubleTarget);
        }
      } else if(curvalue instanceof Integer) {
        Integer intTarget = Utils.toInt(target);
        Integer intCurVal = Utils.toInt(curvalue);
        Integer intIncriment = Utils.toInt(incriment);
        if(intCurVal > intTarget && intIncriment < 0){
            do{
              for(Integer i = 0; i < toExec.size(); i++){
                  toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              entry.setValue(intCurVal + intIncriment);
              curvalue = entry.getValue();
              intCurVal = Utils.toInt(curvalue);
            } while(intCurVal > intTarget);
        } else if (intCurVal < intTarget && intIncriment > 0){
          do{
            for(Integer i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
            }
            VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
            entry.setValue(intCurVal + intIncriment);
            curvalue = entry.getValue();
            intCurVal = Utils.toInt(curvalue);
          } while(intCurVal < intTarget);
        } else if(intCurVal != intTarget) {
          errorLog.add("Possible infinite forloop in for loop ", forbranch.getStart());
        }
      }
    } else {
        forbranch.getInitAssignment().accept(this);
        Object target = forbranch.getTargetExpression().acceptResult(this);
        Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
        if(curvalue instanceof Double){
          Double doubleTarget = Utils.toDouble(target);
          Double doubleCurVal = Utils.toDouble(curvalue);
          if(doubleTarget < doubleCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              doubleCurVal = Utils.toDouble(curvalue);
            }while(doubleTarget < doubleCurVal);
          } else if(doubleTarget > doubleCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              doubleCurVal = Utils.toDouble(curvalue);
            } while(doubleTarget > doubleCurVal);
          }
        } else if(curvalue instanceof Integer){
          Integer intTarget = Utils.toInt(target);
          Integer intCurVal = Utils.toInt(curvalue);
          if(intTarget < intCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              intCurVal = Utils.toInt(curvalue);
            }while(intTarget < intCurVal);
          } else if(intTarget > intCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              intCurVal = Utils.toInt(curvalue);
            } while(intTarget > intCurVal);
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
        Object value = assignment.getVariableValue().acceptResult(this);
        Integer ivalue = Utils.toInt(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Boolean){
        Object value = assignment.getVariableValue().acceptResult(this);
        Boolean ivalue = Utils.toBool(value);
        entry.setValue(ivalue);
      } else {
        Object value = assignment.getVariableValue().acceptResult(this);
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
        case DIV: return OpUtil.div(leftValue, rightValue);
        case AND: return OpUtil.and(leftValue, rightValue);
        case MOD: return OpUtil.mod(leftValue, rightValue);
        case OR: return OpUtil.or(leftValue, rightValue);
        case LT: return OpUtil.lessThan(leftValue, rightValue);
        case GT: return OpUtil.greaterThan(leftValue, rightValue);
        case EQ: return OpUtil.equal(leftValue, rightValue);
        case GE: return OpUtil.greaterThanOrEqualTo(leftValue, rightValue);
        case LE: return OpUtil.lessThanOrEqualTo(leftValue, rightValue);
        case BAND: return OpUtil.bitwiseAnd(leftValue, rightValue);
        case BOR: return OpUtil.bitwiseOr(leftValue, rightValue);
        case LSHIFT: return OpUtil.leftShift(leftValue, rightValue);
        case RSHIFT: return OpUtil.rightShift(leftValue, rightValue);
        default: return null;
    }
  }

  @Override
  public Object visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    if(funcName.equals("round") || funcName.equals("Round")){
      double argument = Utils.toDouble(funcCall.getArguments().get(0).acceptResult(this));
      return (int)Math.round(argument);
    } else if(funcName.equals("floor") || funcName.equals("Floor")) {
      double argument = Utils.toDouble(funcCall.getArguments().get(0).acceptResult(this));
      return (int)Math.floor(argument);
    } else if(funcName.equals("ceil") || funcName.equals("Ceil")) {
      double argument = Utils.toDouble(funcCall.getArguments().get(0).acceptResult(this));
      return (int)Math.ceil(argument);
    } else if (funcName.equals("readInt") || funcName.equals("ReadInt")) {
      Scanner scanner = new Scanner(standardIn);
      String line = scanner.nextLine();
      scanner.close();
      return Integer.parseInt(line);
    } else {
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

  @Override
  public void visit(Asm asm) {
  }
}
