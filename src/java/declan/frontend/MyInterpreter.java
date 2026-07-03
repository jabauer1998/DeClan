package declan.frontend;

import declan.utils.ConversionUtils;
import declan.utils.OpUtil;
import declan.utils.Utils;
import declan.utils.ErrorLog;
import declan.utils.MyStandardLibrary;
import declan.frontend.ast.ASTVisitor;
import declan.frontend.ast.Asm;
import declan.frontend.ast.Assignment;
import declan.frontend.ast.BinaryOperation;
import declan.frontend.ast.BoolValue;
import declan.frontend.ast.Branch;
import declan.frontend.ast.ConstDeclaration;
import declan.frontend.ast.Declaration;
import declan.frontend.ast.ElseBranch;
import declan.frontend.ast.EmptyStatement;
import declan.frontend.ast.Expression;
import declan.frontend.ast.ExpressionVisitor;
import declan.frontend.ast.ForBranch;
import declan.frontend.ast.FunctionCall;
import declan.frontend.ast.Identifier;
import declan.frontend.ast.IfElifBranch;
import declan.frontend.ast.Library;
import declan.frontend.ast.NumValue;
import declan.frontend.ast.ParamaterDeclaration;
import declan.frontend.ast.ProcedureCall;
import declan.frontend.ast.ProcedureDeclaration;
import declan.frontend.ast.Program;
import declan.frontend.ast.RepeatBranch;
import declan.frontend.ast.Statement;
import declan.frontend.ast.StrValue;
import declan.frontend.ast.CharValue;
import declan.frontend.ast.UnaryOperation;
import declan.frontend.ast.VariableDeclaration;
import declan.frontend.ast.WhileElifBranch;
import declan.utils.symboltable.Environment;
import declan.utils.symboltable.entry.ProcedureEntry;
import declan.utils.symboltable.entry.VariableEntry;
import declan.frontend.ast.ElementAssignment;
import declan.frontend.ast.ElementAccess;
import declan.frontend.ast.ArrayDeclaration;

import java.lang.Object;
import java.io.IOException;
import java.io.Reader;
import java.io.PushbackReader;
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
  private PushbackReader standardIn;
  private MyStandardLibrary libs;

  public MyInterpreter(ErrorLog errorLog, Writer standardOutput, Writer standardError, Reader standardIn) {
    this.errorLog = errorLog;
    this.standardOutput = standardOutput;
    this.standardError = standardError;
    if(standardIn != null)
	this.standardIn = new PushbackReader(standardIn);
    else
	this.standardIn = null;
    libs = new MyStandardLibrary(errorLog);
    this.varEnvironment = new Environment<>();
    this.procEnvironment = new Environment<>();
  }

  @Override
  public void visit(Library library){
    for(Declaration decl : library.getDecls()){
        decl.accept(this);
    }
  }

  private void visitStandardLibrary(){
      libs.declanMathLibrary().accept(this);
      libs.declanIoLibrary().accept(this);
      libs.declanRealLibrary().accept(this);
      libs.declanIntLibrary().accept(this);
      libs.declanConversionsLibrary().accept(this);
      libs.declanUtilsLibrary().accept(this);
  }
    
  @Override
  public void visit(Program program) {
    procEnvironment.addScope();
    varEnvironment.addScope();

    visitStandardLibrary();

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
    } else if (type.equals("INTEGER")){
	    varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0));
    } else if(type.equals("CHAR")){
	    varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, '\0'));
    }
  }

  @Override
  public void visit(ArrayDeclaration decl){
      String name = decl.getName();
      ArrayDeclaration.Type type = decl.getType();
      Expression size = decl.getSize();
      int mySize = (int)size.acceptResult(this);

      switch(type){
      case CHAR: varEnvironment.addEntry(name, new VariableEntry(false, new char[mySize])); break;
      case REAL: varEnvironment.addEntry(name, new VariableEntry(false, new float[mySize])); break;
      case INT: varEnvironment.addEntry(name, new VariableEntry(false, new int[mySize])); break;
      case BOOLEAN: varEnvironment.addEntry(name, new VariableEntry(false, new boolean[mySize])); break;
      default: throw new RuntimeException("Unknown array type found for " + decl.toString() + " at position " + decl.getStart()); 
      }
  }

  @Override
  public void visit(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    List <ParamaterDeclaration> args = procDecl.getArguments();
    List <Declaration> localVars = procDecl.getLocalVariables();
    List <Statement> Exec = procDecl.getExecutionStatements();
    Expression retExp = procDecl.getReturnStatement();
    procEnvironment.addEntry(procedureName, new ProcedureEntry(args, localVars, Exec, retExp));
  }
        
  @Override
  public void visit(ProcedureCall procedureCall) {
    String procName = procedureCall.getProcedureName().getLexeme();
    if(procName.equals("WriteChar")) {
      char value = (char)procedureCall.getArguments().get(0).acceptResult(this);
      try{
	  standardOutput.write((int)value);
	  standardOutput.flush();
      }catch(IOException exp){}
    } else if(procName.equals("SkipChar")) {
	try{
	  standardIn.skip(1);
	} catch(Exception exp){}
    } else if(procName.equals("ASSERT")){
      boolean value = ConversionUtils.toBool(procedureCall.getArguments().get(0).acceptResult(this));
      Object toPrint = (Object)procedureCall.getArguments().get(1).acceptResult(this);
        if(!value){
        try{
          standardError.append("" + toPrint);
        } catch(IOException exp){}
      }
    } else {
        String funcName = procedureCall.getProcedureName().getLexeme();
        ProcedureEntry pentry = procEnvironment.getEntry(funcName);

	if(pentry == null){
	    throw new RuntimeException("Cant find entry for " + funcName + " in symbol table for procedure call " + procedureCall + "\n at " + procedureCall.getStart());
	}
	
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
	 throw new RuntimeException("Variable " + assignment.getVariableName().getLexeme() + " declared as const at" + assignment.getVariableName().getStart());
      } else if(entry.getValue() instanceof Float){
        Object value = assignment.getVariableValue().acceptResult(this);
        Float ivalue = ConversionUtils.toReal(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Integer){
        Object value = assignment.getVariableValue().acceptResult(this);
	if(value == null)
	    throw new RuntimeException("Error cant assign null value to int in instuction " + assignment + " at " + assignment.getStart());
        Integer ivalue = ConversionUtils.toInt(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Boolean){
        Object value = assignment.getVariableValue().acceptResult(this);
        Boolean ivalue = ConversionUtils.toBool(value);
        entry.setValue(ivalue);
      } else if(entry.getValue() instanceof Character) {
	Object value = assignment.getVariableValue().acceptResult(this);
	Character cValue = ConversionUtils.toChar(value);
	entry.setValue(cValue);
      } else if(entry.getValue() instanceof String) {
        Object value = assignment.getVariableValue().acceptResult(this);
        entry.setValue(value);
      } else {
	  throw new RuntimeException("Unexpected type on left hand of assignment " + assignment.toString() + " at " + assignment.getStart());
      }
    }
  }

  @Override
  public void visit(ElementAssignment assignment) {
    String name = assignment.getVariableName().getLexeme();
    if(varEnvironment.entryExists(name)){
      VariableEntry entry = varEnvironment.getEntry(name);
      if(entry.isConst()){
	      errorLog.add("Variable " + assignment.getVariableName().getLexeme() + " declared as const ", assignment.getVariableName().getStart());
      } else if(entry.getValue() instanceof Character[]){
        Object value = assignment.getVariableValue().acceptResult(this);
	Object index = assignment.getVariableIndex().acceptResult(this);
        if(value instanceof Character && index instanceof Integer){
	    char valChar = (char)value;
	    char[] original = (char[])entry.getValue();
	    original[(int)index] = valChar;
	}
      } else if(entry.getValue() instanceof Integer[]){
	 Object value = assignment.getVariableValue().acceptResult(this);
	 Object index = assignment.getVariableIndex().acceptResult(this);
         if(index instanceof Integer){
	     int valInt = ConversionUtils.toInt(value);
	     int[] original = (int[])entry.getValue();
	     original[(int)index] = valInt;
	 }
      } else if(entry.getValue() instanceof Float[]){
	 Object value = assignment.getVariableValue().acceptResult(this);
	 Object index = assignment.getVariableIndex().acceptResult(this);
         if(index instanceof Integer){
	     float valReal = ConversionUtils.toReal(value);
	     float[] original = (float[])entry.getValue();
	     original[(int)index] = valReal;
	 }
      } else if(entry.getValue() instanceof Boolean[]){
	 Object value = assignment.getVariableValue().acceptResult(this);
	 Object index = assignment.getVariableIndex().acceptResult(this);
         if(index instanceof Integer){
	     boolean valBool = ConversionUtils.toBool(value);
	     boolean[] original = (boolean[])entry.getValue();
	     original[(int)index] = valBool;
	 }
      } else {
	  throw new RuntimeException("Cant assign to element of array of type " + entry.getValue().getClass().getSimpleName() + " at " + assignment.getStart());
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
  public void visit(CharValue cVal) {
      //not used
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

    if(leftValue instanceof Character){
	leftValue = (int)(char)leftValue;
    }
    if(rightValue instanceof Character){
	rightValue = (int)(char)rightValue;
    }
    
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
        default: throw new RuntimeException("Error Invalid operation in binary operation " + binaryOperation.toString() + " at " + binaryOperation.getStart());
    }
  }

  @Override
  public Object visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    if(funcName.equals("readChar") || funcName.equals("ReadChar")) {
      try{
	  int res = standardIn.read();
	  if(res == -1)
	      return '\0';
	  return (char)res;
	} catch(Exception exp){
	  throw new RuntimeException("In function " +  funcCall.toString() + "exp.toString()");
        }
    } else if(funcName.equals("peekChar") || funcName.equals("PeekChar")) {
	try {
	    int res = standardIn.read();
	    if(res <= -1)
		return '\0';
	    char line = (char)res;
	    standardIn.unread(res);
	    return line;
	} catch(Exception exp){
	    throw new RuntimeException("Function call " + funcCall.toString() + "\n exception occured " + exp.toString());
	}
    } else if(funcName.equals("realBinaryAsInt") || funcName.equals("RealBinaryAsInt")){
      float argument = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
      return Float.floatToRawIntBits(argument);
    } else if(funcName.equals("intBinaryAsReal") || funcName.equals("IntBinaryAsReal")){
      int argument = ConversionUtils.toInt(funcCall.getArguments().get(0).acceptResult(this));
      return Float.intBitsToFloat(argument);
    } else if(funcName.equals("BoolBinaryAsReal")) {
	boolean argument = ConversionUtils.toBool(funcCall.getArguments().get(0).acceptResult(this));
	return (float)(argument ? 1.0 : 0.0);
    } else if(funcName.equals("RealBinaryAsBool")) {
	float argument = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
	return argument != 0;
    } else if(funcName.equals("BoolBinaryAsInt")) {
	boolean argument = ConversionUtils.toBool(funcCall.getArguments().get(0).acceptResult(this));
	return argument ? 1 : 0;
    } else if(funcName.equals("IntBinaryAsBool")) {
        int argument = ConversionUtils.toInt(funcCall.getArguments().get(0).acceptResult(this));
        return argument > 0;
    } else if(funcName.equals("CharBinaryAsInt")) {
	char arg = ConversionUtils.toChar(funcCall.getArguments().get(0).acceptResult(this));
	return (int)arg;
    } else if(funcName.equals("IntBinaryAsChar")) {
	int argument = ConversionUtils.toInt(funcCall.getArguments().get(0).acceptResult(this));
        return (char)argument;
    } else if(funcName.equals("Multiply")) {
	int argument1 = ConversionUtils.toInt(funcCall.getArguments().get(0).acceptResult(this));
	int argument2 = ConversionUtils.toInt(funcCall.getArguments().get(1).acceptResult(this));
	return (int)(argument1 * argument2);
    } else if(funcName.equals("RMul")) {
	float argument1 = ConversionUtils.toReal(funcCall.getArguments().get(0).acceptResult(this));
	float argument2 = ConversionUtils.toReal(funcCall.getArguments().get(1).acceptResult(this));
	return (float)(argument1 * argument2);
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
        } else if(toChange.getValue() instanceof Character) {
	    char val = ConversionUtils.toChar(variableValue);
	    toChange.setValue(val);
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
      case BNOT: return OpUtil.bitwiseNot(value);
      default: return null;
    }
  }

  @Override
  public Object visitResult(ElementAccess access){
    VariableEntry ident = varEnvironment.getEntry(access.getLexeme());
    Object value = ident.getValue();
    Object index = access.getExpression().acceptResult(this);

    if(value instanceof char[] && index instanceof Integer){
	char[] valText = (char[])value;
	int index2 = (int)index;

	return valText[index2];
    } else if(value instanceof int[] && index instanceof Integer){
	int[] val = (int[])value;
	int index2 = (int)index;
	return val[index2];
    } else if(value instanceof float[] && index instanceof Integer){
	float[] val = (float[])value;
	int index2 = (int)index;
	return val[index2];
    } else if(value instanceof boolean[] && index instanceof Integer){
	boolean[] val = (boolean[])value;
	int index2 = (int)index;
	return val[index2];
    } else {
	throw new RuntimeException("Error unexpected type for array " + value.getClass().getSimpleName());
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
      return (float)Float.parseFloat(lexeme);
    } else {
      return (int)Integer.parseInt(lexeme);
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
  public Object visitResult(CharValue chrValue){
      String lexeme = chrValue.getLexeme();
      if(!lexeme.isEmpty()){
	  return lexeme.translateEscapes().charAt(0);
      } else {
	  throw new RuntimeException("Error lexeme is empty expected \\0");
      }
  }

  @Override
  public Object visitResult(StrValue strValue){
      char[] toRet = new char[strValue.getLexeme().length() + 1];
      int i;
      for(i = 0; i < strValue.getLexeme().length(); i++){
	  toRet[i] = strValue.getLexeme().charAt(i);
      }
      toRet[i] = '\0';
      return toRet;
  }


  @Override
  public void visit(ParamaterDeclaration declaration) {
    Identifier id = declaration.getIdentifier();
    String type = declaration.getType().getLexeme();
    if(type.equals("BOOLEAN")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, false));
    } else if (type.equals("REAL")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0.0f));
    } else if(type.equals("STRING")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, new char[]{}));
    } else if(type.equals("INTEGER")){
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, 0));
    } else if(type.equals("CHAR")) {
	varEnvironment.addEntry(id.getLexeme(), new VariableEntry(false, '\0'));
    } else {
	throw new RuntimeException("Error unexpected type for paramater " + type + " in " + declaration.toString());
    }
  }

  @Override
  public void visit(Asm asm) {
  }

  @Override
  public void visit(ElementAccess access){
  }
}


