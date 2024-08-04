package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;
import io.github.H20man13.DeClan.common.ErrorLog;
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
import io.github.H20man13.DeClan.common.ast.ExpressionVisitor;
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
	    varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0.0f));
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
      Integer intVal = ConversionUtils.toInt(value);
      try{
        standardOutput.append("" + intVal);
      } catch(IOException exp){}
    } else if (procName.equals("WriteReal") || procName.equals("PrintReal")) {
      Object value = procedureCall.getArguments().get(0).acceptResult(this);
      Float floatVal = ConversionUtils.toReal(value);
      try{
        standardOutput.append(floatVal.toString());
      } catch(IOException exp){}
    } else if(procName.equals("WriteLn") || procName.equals("PrintLn")) {
       try{
        standardOutput.append("\n");
       } catch(IOException exp){}
    } else if(procName.equals("WriteBool") || procName.equals("PrintBool")){
        Object value = procedureCall.getArguments().get(0).acceptResult(this);
        Boolean val = ConversionUtils.toBool(value);
        try{
         standardOutput.append(val.toString());
        }catch(IOException exp){}
    } else if(procName.equals("WriteString") || procName.equals("PrintString")) {
      Object value = (Object)procedureCall.getArguments().get(0).acceptResult(this);
      try{
        standardOutput.append(value.toString());
      }catch(IOException exp){}
    } else if(procName.equals("ASSERT")){
      boolean value = ConversionUtils.toBool(procedureCall.getArguments().get(0).acceptResult(this));
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
    if(ConversionUtils.toBool(toCheck.acceptResult(this))){
      List<Statement> toExec = whilebranch.getExecStatements();
      do {
        for(Integer i = 0; i < toExec.size(); i++){
          toExec.get(i).accept(this);
        }
      } while(ConversionUtils.toBool(toCheck.acceptResult(this)));
    } else if (whilebranch.getNextBranch() != null){
      whilebranch.getNextBranch().accept(this);
    }
  }
    
  @Override
  public void visit(IfElifBranch ifbranch){
    Expression toCheck = ifbranch.getExpression();
    Object res = toCheck.acceptResult(this);
    Boolean boolRes = ConversionUtils.toBool(res);
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
    boolean result = ConversionUtils.toBool(toCheck.acceptResult(this));
    while(!result){
      for(Integer i = 0; i < toExec.size(); i++){
	      toExec.get(i).accept(this);
      }
      result = ConversionUtils.toBool(toCheck.acceptResult(this));
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
      if(curvalue instanceof Float){
        Float floatCurValue = ConversionUtils.toReal(curvalue);
        Float floatTarget = ConversionUtils.toReal(target);
        Float floatIncriment = ConversionUtils.toReal(incriment);
        if(floatIncriment < 0 && floatCurValue > floatTarget){
            do{
              for(Integer i = 0; i < toExec.size(); i++){
                  toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              entry.setValue(floatCurValue + floatIncriment);
              curvalue = entry.getValue();
              floatCurValue = ConversionUtils.toReal(curvalue);
	          } while(floatCurValue > floatTarget);
        } else if(floatIncriment > 0 && floatCurValue < floatTarget){
           do{
              for(Integer i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              entry.setValue(floatCurValue + floatIncriment);
              curvalue = entry.getValue();
              floatCurValue = ConversionUtils.toReal(curvalue);
	          }while(floatCurValue < floatTarget);
        }
      } else if(curvalue instanceof Integer) {
        Integer intTarget = ConversionUtils.toInt(target);
        Integer intCurVal = ConversionUtils.toInt(curvalue);
        Integer intIncriment = ConversionUtils.toInt(incriment);
        if(intCurVal > intTarget && intIncriment < 0){
            do{
              for(Integer i = 0; i < toExec.size(); i++){
                  toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              entry.setValue(intCurVal + intIncriment);
              curvalue = entry.getValue();
              intCurVal = ConversionUtils.toInt(curvalue);
            } while(intCurVal > intTarget);
        } else if (intCurVal < intTarget && intIncriment > 0){
          do{
            for(Integer i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
            }
            VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
            entry.setValue(intCurVal + intIncriment);
            curvalue = entry.getValue();
            intCurVal = ConversionUtils.toInt(curvalue);
          } while(intCurVal < intTarget);
        } else if(intCurVal != intTarget) {
          errorLog.add("Possible infinite forloop in for loop ", forbranch.getStart());
        }
      }
    } else {
        forbranch.getInitAssignment().accept(this);
        Object target = forbranch.getTargetExpression().acceptResult(this);
        Object curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme()).getValue();
        if(curvalue instanceof Float){
          Float floatTarget = ConversionUtils.toReal(target);
          Float floatCurVal = ConversionUtils.toReal(curvalue);
          if(floatTarget < floatCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              floatCurVal = ConversionUtils.toReal(curvalue);
            }while(floatTarget < floatCurVal);
          } else if(floatTarget > floatCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              floatCurVal = ConversionUtils.toReal(curvalue);
            } while(floatTarget > floatCurVal);
          }
        } else if(curvalue instanceof Integer){
          Integer intTarget = ConversionUtils.toInt(target);
          Integer intCurVal = ConversionUtils.toInt(curvalue);
          if(intTarget < intCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              intCurVal = ConversionUtils.toInt(curvalue);
            }while(intTarget < intCurVal);
          } else if(intTarget > intCurVal){
            do{
              for(int i = 0; i < toExec.size(); i++){
                toExec.get(i).accept(this);
              }
              VariableEntry entry = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
              curvalue = entry.getValue();
              intCurVal = ConversionUtils.toInt(curvalue);
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
      } else if(entry.getValue() instanceof Float){
        Object value = assignment.getVariableValue().acceptResult(this);
        Float ivalue = ConversionUtils.toReal(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Integer){
        Object value = assignment.getVariableValue().acceptResult(this);
        Integer ivalue = ConversionUtils.toInt(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Boolean){
        Object value = assignment.getVariableValue().acceptResult(this);
        Boolean ivalue = ConversionUtils.toBool(value);
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
      float argument = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
      return (int)Math.round(argument);
    } else if(funcName.equals("floor") || funcName.equals("Floor")) {
      float argument = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
      return (int)Math.floor(argument);
    } else if(funcName.equals("ceil") || funcName.equals("Ceil")) {
      float argument = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
      return (int)Math.ceil(argument);
    } else if (funcName.equals("readInt") || funcName.equals("ReadInt")) {
      Scanner scanner = new Scanner(standardIn);
      String line = scanner.nextLine();
      scanner.close();
      return Integer.parseInt(line);
    } else if(funcName.equals("readReal") || funcName.equals("ReadReal")){
      Scanner scanner = new Scanner(standardIn);
      String line = scanner.nextLine();
      scanner.close();
      return Float.parseFloat(line);
    } else if(funcName.equals("readBool") || funcName.equals("ReadBool")){
      Scanner scanner = new Scanner(standardIn);
      String line = scanner.nextLine();
      scanner.close();
      return Boolean.parseBoolean(line);
    } else if(funcName.equals("realBinaryAsInt") || funcName.equals("RealBinaryAsInt")){
      float argument = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
      return Float.floatToRawIntBits(argument);
    } else if(funcName.equals("intBinaryAsReal") || funcName.equals("IntBinaryAsReal")){
      int argument = ConversionUtils.toInt(funcCall.getArguments().get(0).acceptResult(this));
      return Float.intBitsToFloat(argument);
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
          Integer varVal = ConversionUtils.toInt(variableValue);
          toChange.setValue(varVal);
        } else if(toChange.getValue() instanceof Float){
          Float varVal = ConversionUtils.toReal(variableValue);
          toChange.setValue(varVal);
        } else if(toChange.getValue() instanceof Boolean){
          Boolean varVal = ConversionUtils.toBool(variableValue);
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
      return (Float)Float.parseFloat(lexeme);
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
	      varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0.0f));
    } else {
	      varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0));
    }
  }

  @Override
  public void visit(Asm asm) {
  }
}
