package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.RegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Call;

import io.github.H20man13.DeClan.common.symboltable.VariableEntry;
import io.github.H20man13.DeClan.common.symboltable.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.Environment;

import static io.github.H20man13.DeClan.common.RegisterGenerator.*;
import static io.github.H20man13.DeClan.main.MyIO.*;

import java.lang.String;

import java.util.List;

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
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.ProcedureDeclaration;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.StrValue;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.WhileElifBranch;

import java.util.ArrayList;

/**
 *The my interpreter class is a visitor object that can interpret the entire DeClan Language
 * It also takes in an error log object in order to record errors
 *@author Jacob Bauer
 */

public class MyICodeGenerator implements ASTVisitor, ExpressionVisitor<String> {
  private ErrorLog errorLog;
  private Environment<String, String> varEnvironment;
  private Environment<String, String> procEnvironment;
  private MyIrBuilder builder;

  public MyICodeGenerator(ErrorLog errorLog){
    this(errorLog, new RegisterGenerator());
  }

  public MyICodeGenerator(ErrorLog errorLog, RegisterGenerator Gen) {
    this.errorLog = errorLog;
    this.builder = new MyIrBuilder(errorLog, Gen);
    this.varEnvironment = new Environment<>();
    this.procEnvironment = new Environment<>();
  }

  public List<ICode> getICode(){
    return builder.getOutput();
  }

  @Override
  public void visit(Program program) {
    procEnvironment.addScope();
    varEnvironment.addScope();
    for (Declaration decl : program.getDecls()) {
      decl.accept(this);
    }
    for (Statement statement : program.getStatements()) {
      statement.accept(this);
    }
    varEnvironment.removeScope();
    procEnvironment.removeScope();
    builder.buildEnd();
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
    String value = valueExpr.acceptResult(this);
    String place = builder.buildVariableAssignment(value);
    varEnvironment.addEntry(id.getLexeme(), place);
  }

  @Override
  public void visit(VariableDeclaration varDecl) {
    Identifier id = varDecl.getIdentifier();
    String place = builder.buildNumAssignment("0");
    varEnvironment.addEntry(id.getLexeme(), place);
  }

  @Override
  public void visit(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    builder.buildProcedureDeclaration(procedureName);
    varEnvironment.addScope();
    List <VariableDeclaration> args = procDecl.getArguments();
    for(int i = 0; i < args.size(); i++){
	    args.get(i).accept(this);
    }
    List <Declaration> localVars = procDecl.getLocalVariables();
    for(int i = 0; i < localVars.size(); i++){
	    localVars.get(i).accept(this);
    }
    List <Statement> exec = procDecl.getExecutionStatements();
    for(int i = 0; i < exec.size(); i++){
	    exec.get(i).accept(this);
    }
    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      String retPlace = retExp.acceptResult(this);
      procEnvironment.addEntry(procedureName, retPlace);
      builder.buildReturnStatement();
    }
    varEnvironment.removeScope();
  }
        
  @Override
  public void visit(ProcedureCall procedureCall) {
    String funcName = procedureCall.getProcedureName().getLexeme();
    List<Expression> valArgs = procedureCall.getArguments();
    List<String> valArgResults = new ArrayList<>();
    for(Expression valArg : valArgs){
      String result = valArg.acceptResult(this);
      valArgResults.add(result);
    }
    builder.buildProcedure(funcName, valArgResults);
  }

  @Override
  public void visit(WhileElifBranch whilebranch){
    Expression toCheck = whilebranch.getExpression();
    List<Statement> toExec = whilebranch.getExecStatements();
    String test = toCheck.acceptResult(this);
    
    IdentExp ident = new IdentExp(test);
    builder.buildWhileLoopBeginning(ident);
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }

    if(whilebranch.getNextBranch() != null) {
      builder.buildElseWhileLoopBeginning();
      whilebranch.getNextBranch().accept(this);
    } else {
      builder.buildWhileLoopEnd();
    }
  }
    
  @Override
  public void visit(IfElifBranch ifbranch){
    Expression toCheck = ifbranch.getExpression();
    String test = toCheck.acceptResult(this);
    IdentExp ident = new IdentExp(test);
    builder.buildIfStatementBeginning(ident);

    List<Statement> toExec = ifbranch.getExecStatements();
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }

    if(ifbranch.getNextBranch() != null) {
      builder.buildElseIfStatementBeginning();
      ifbranch.getNextBranch().accept(this);
    } else {
      builder.buildIfStatementEnd();
    }
  }

  @Override
  public void visit(ElseBranch elsebranch){
    List<Statement> toExec = elsebranch.getExecStatements();
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }
    builder.buildIfStatementEnd();
  }

  @Override
  public void visit(RepeatBranch repeatbranch){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    String test = toCheck.acceptResult(this);

    builder.buildRepeatLoopBeginning(test);
    for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
    }
    builder.buildRepeatLoopEnd();
  }

  @Override
  public void visit(ForBranch forbranch){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
        String incriment = toMod.acceptResult(this);
        forbranch.getInitAssignment().accept(this);
        String target = forbranch.getTargetExpression().acceptResult(this);
        IdentExp targetIdent = new IdentExp(target);
        String curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
        IdentExp curValueIdent = new IdentExp(curvalue);
        builder.buildForLoopBeginning(curValueIdent, targetIdent);
        for(int i = 0; i < toExec.size(); i++){
            toExec.get(i).accept(this);
        }
        builder.buildForLoopEnd();
    } else {
      forbranch.getInitAssignment().accept(this);
      String target = forbranch.getTargetExpression().acceptResult(this);
      IdentExp targetIdent = new IdentExp(target);
      String curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
      IdentExp curvalueIdent = new IdentExp(curvalue);
      builder.buildForLoopBeginning(curvalueIdent, targetIdent);
      for(int i = 0; i < toExec.size(); i++){
          toExec.get(i).accept(this);
      }
      builder.buildForLoopEnd();
    }
  }
        
  @Override
  public void visit(Assignment assignment) {
    String place = varEnvironment.getEntry(assignment.getVariableName().getLexeme());
    String value = assignment.getVariableValue().acceptResult(this);
    builder.buildVariableAssignment(place, value);
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
  public String visitResult(BinaryOperation binaryOperation) {
      String leftValue = binaryOperation.getLeft().acceptResult(this);
      IdentExp leftIdent = new IdentExp(leftValue);
      String rightValue = binaryOperation.getRight().acceptResult(this);
      IdentExp rightIdent = new IdentExp(rightValue);
      switch (binaryOperation.getOperator()){
        case PLUS: return builder.buildAdditionAssignment(leftIdent, rightIdent);
        case MINUS: return builder.buildSubtractionAssignment(leftIdent, rightIdent);
        case TIMES: return builder.buildMultiplicationAssignment(leftIdent, rightIdent);
        case DIV: return builder.buildDivisionAssignment(leftIdent, rightIdent);
        case DIVIDE: return builder.buildDivisionAssignment(leftIdent, rightIdent);
        case MOD: return builder.buildModuloAssignment(leftIdent, rightIdent);
        case LE: return builder.buildLessThanOrEqualAssignment(leftIdent, rightIdent);
        case LT: return builder.buildLessThanAssignment(leftIdent, rightIdent);
        case GE: return builder.buildGreaterThanOrEqualToAssignment(leftIdent, rightIdent);
        case GT: return builder.buildGreaterThanAssignment(leftIdent, rightIdent);
        case AND: return builder.buildAndAssignment(leftIdent, rightIdent);
        case OR: return builder.buildOrAssignment(leftIdent, rightIdent);
        case EQ: return builder.buildEqualityAssignment(leftIdent, rightIdent);
        case NE: return builder.buildInequalityAssignment(leftIdent, rightIdent);
        default: return leftValue;
      }
  }

  @Override
  public String visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    List<Expression> valArgs = funcCall.getArguments();
    List<String> valArgResults = new ArrayList<>();
    for(Expression valArg : valArgs){
	    String result = valArg.acceptResult(this);
	    valArgResults.add(result);
    }
    return builder.buildProcedureCall(funcName, valArgResults);
  }

  @Override
  public String visitResult(UnaryOperation unaryOperation) {
    String value = unaryOperation.getExpression().acceptResult(this);

    IdentExp valueIdent = new IdentExp(value);
    
	  switch(unaryOperation.getOperator()){
	    case MINUS: return builder.buildNegationAssignment(valueIdent);
	    case NOT: return builder.buildNotAssignment(valueIdent);
	    default: return value;
	  }
  }
    
  @Override
  public String visitResult(Identifier identifier){
    String place = varEnvironment.getEntry(identifier.getLexeme());
    return place;
  }

  @Override
  public String visitResult(NumValue numValue){
      String rawnum = ifHexToInt(numValue.getLexeme());
      return builder.buildNumAssignment(rawnum);
  }

  @Override
  public String visitResult(BoolValue boolValue){
      String lexeme = boolValue.getLexeme(); //change to hex if you need to otherwise unchanged
      return builder.buildBoolAssignment(lexeme);
  }

  @Override
  public String visitResult(StrValue strValue){
      return builder.buildStringAssignment(strValue.getLexeme());
  }
}
