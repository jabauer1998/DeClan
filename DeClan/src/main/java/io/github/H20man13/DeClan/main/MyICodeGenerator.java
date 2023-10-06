package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.StringEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.StringEntryList;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.Utils;

import static io.github.H20man13.DeClan.common.IrRegisterGenerator.*;
import static io.github.H20man13.DeClan.main.MyIO.*;

import java.io.Writer;
import java.lang.String;

import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.Asm;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.BoolValue;
import edu.depauw.declan.common.ast.Branch;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.DeclarationVisitor;
import edu.depauw.declan.common.ast.ElseBranch;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.ForBranch;
import edu.depauw.declan.common.ast.FunctionCall;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.IfElifBranch;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ParamaterDeclaration;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.ProcedureDeclaration;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.Library;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.StrValue;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.WhileElifBranch;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *The my interpreter class is a visitor object that can interpret the entire DeClan Language
 * It also takes in an error log object in order to record errors
 *@author Jacob Bauer
 */

public class MyICodeGenerator implements ASTVisitor, ExpressionVisitor<String>, DeclarationVisitor<String> {
  private ErrorLog errorLog;
  private Environment<String, StringEntry> varEnvironment;
  private Environment<String, StringEntry> paramEnvironment;
  private Environment<String, StringEntry> procEnvironment;
  private Environment<String, StringEntryList> procArgs;
  private MyIrBuilder builder;
  private MyTypeChecker typeChecker;
  private MyInterpreter interpreter;

  public MyICodeGenerator(ErrorLog errorLog){
    this(errorLog, new IrRegisterGenerator());
  }

  public MyICodeGenerator(ErrorLog errorLog, IrRegisterGenerator Gen) {
    this.errorLog = errorLog;
    this.builder = new MyIrBuilder(errorLog, Gen);
    this.varEnvironment = new Environment<>();
    this.procEnvironment = new Environment<>();
    this.paramEnvironment = new Environment<>();
    this.procArgs = new Environment<>();
    this.typeChecker = new MyTypeChecker(errorLog);
    this.interpreter = new MyInterpreter(errorLog, null, null, null);
  }

  public List<ICode> getICode(){
    return builder.getOutput();
  }

  @Override
  public void visit(Library lib){
    procEnvironment.addScope();
    varEnvironment.addScope();
    procArgs.addScope();
    typeChecker.addScope();

    builder.buildBeginLabel();
    for(Declaration decl : lib.getConstDecls()){
      decl.accept(typeChecker);
      decl.accept(this);
    }

    for(Declaration decl : lib.getVarDecls()){
      decl.accept(typeChecker);
      decl.accept(this);
    }

    builder.buildBeginGoto();

    loadFunctions(lib.getProcDecls());
    typeChecker.loadFunctions(lib.getProcDecls());

    for(Declaration decl : lib.getProcDecls()){
      decl.accept(typeChecker);
      decl.accept(this);
      typeChecker.removeVarScope();
    }
  }

  @Override
  public void visit(Program program) {
    procEnvironment.addScope();
    varEnvironment.addScope();
    procArgs.addScope();
    typeChecker.addScope();
    builder.buildBeginLabel();
    for(Declaration decl : program.getConstDecls()){
      decl.accept(typeChecker);
      decl.accept(this);
    }
    for (Declaration decl : program.getVarDecls()) {
      decl.accept(typeChecker);
      decl.accept(this);
    }

    builder.buildBeginGoto();

    loadFunctions(program.getProcDecls());
    typeChecker.loadFunctions(program.getProcDecls());

    for (Declaration decl : program.getProcDecls()){
      decl.accept(typeChecker);
      decl.accept(this);
      typeChecker.removeVarScope();
    }
    
    builder.buildBeginLabel();
    for (Statement statement : program.getStatements()) {
      statement.accept(this);
    }
    varEnvironment.removeScope();
    procEnvironment.removeScope();
    procArgs.removeScope();
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
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(place));
  }

  @Override
  public void visit(VariableDeclaration varDecl) {
    Identifier id = varDecl.getIdentifier();
    Identifier type = varDecl.getType();
    String argVal = null;
    if(type.getLexeme().equals("INTEGER")){
      argVal = "0";
    } else if(type.getLexeme().equals("STRING")){
      argVal = "\0";
    } else {
      argVal = "0.0";
    }
    String place = builder.buildNumAssignment(argVal);
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(place));
  }

  private void loadFunctions(List<Declaration> decls){
    for(Declaration decl : decls){
      loadFunction((ProcedureDeclaration)decl);
    }
  }

  public void loadFunction(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    List <ParamaterDeclaration> args = procDecl.getArguments();
    
    StringEntryList alias = new StringEntryList();
    for(int i = 0; i < args.size(); i++){
	    String argAlias = builder.buildAlias();
      alias.add(argAlias);
    }

    procArgs.addEntry(procedureName, alias);
  
    String returnPlace = builder.buildAlias();
    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      procEnvironment.addEntry(procedureName, new StringEntry(returnPlace));
    }
  } 

  @Override
  public void visit(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    builder.buildProcedureDeclaration(procedureName);
    paramEnvironment.addScope();
    varEnvironment.addScope();

    StringEntryList list = procArgs.getEntry(procedureName);
    for(int i = 0; i < procDecl.getArguments().size(); i++){
      ParamaterDeclaration decl = procDecl.getArguments().get(i);
      String actual = decl.getIdentifier().getLexeme();
      String alias = list.get(i);
      paramEnvironment.addEntry(actual, new StringEntry(alias));
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
      String returnPlace = procEnvironment.getEntry(procedureName).toString();
      builder.buildVariableAssignment(returnPlace, retPlace);
    }
    
    builder.buildReturnStatement();
    varEnvironment.removeScope();
    paramEnvironment.removeScope();
  }
        
  @Override
  public void visit(ProcedureCall procedureCall) {
    String funcName = procedureCall.getProcedureName().getLexeme();
    List<Expression> valArgs = procedureCall.getArguments();
    List<Tuple<String, String>> valArgResults = new ArrayList<>();

    StringEntryList argsToMap = procArgs.getEntry(funcName);
    for(int i = 0; i < valArgs.size(); i++){
      Expression valArg = valArgs.get(i);
      String result = valArg.acceptResult(this);
      valArgResults.add(new Tuple<String, String>(result, argsToMap.get(i)));
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

    builder.incrimentWhileLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }

    String test2 = toCheck.acceptResult(this);
    builder.buildVariableAssignment(test, test2);

    builder.deIncrimentWhileLoopLevel();

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

    builder.incrimentIfStatementLevel();
    List<Statement> toExec = ifbranch.getExecStatements();
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }
    builder.deIncrimentIfStatementLevel();

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
    builder.incrimentIfStatementLevel();
    for(int i = 0; i < toExec.size(); i++){
      toExec.get(i).accept(this);
    }
    builder.deIncrimentIfStatementLevel();
    builder.buildIfStatementEnd();
  }

  @Override
  public void visit(RepeatBranch repeatbranch){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    String test = toCheck.acceptResult(this);

    builder.buildRepeatLoopBeginning(test);

    builder.incrimentRepeatLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
    }
    builder.deIncrimentRepeatLoopLevel();

    String test2 = toCheck.acceptResult(this);
    builder.buildVariableAssignment(test, test2);

    builder.buildRepeatLoopEnd();
  }

  @Override
  public void visit(ForBranch forbranch){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
        forbranch.getInitAssignment().accept(this);
        String target = forbranch.getTargetExpression().acceptResult(this);
        
        IdentExp targetIdent = new IdentExp(target);
        StringEntry curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
        IdentExp curValueIdent = new IdentExp(curvalue.toString());

        Object actualIncriment = forbranch.getModifyExpression().acceptResult(interpreter);

        if(actualIncriment instanceof Integer){
          Integer intActualIncriment = Utils.toInt(actualIncriment);
          if(intActualIncriment < 0){
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.GT, targetIdent);
          } else if(intActualIncriment > 0){
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.LT, targetIdent);
          } else {
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.NE, targetIdent);
          }
        } else if(actualIncriment instanceof Double){
          Double doubleActualIncriment = Utils.toDouble(actualIncriment);
          if(doubleActualIncriment < 0){
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.GT, targetIdent);
          } else if(doubleActualIncriment > 0){
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.LT, targetIdent);
          } else {
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.NE, targetIdent);
          }
        } else {
          builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.NE, targetIdent);
        }

        
        
        builder.incrimentForLoopLevel();
        for(int i = 0; i < toExec.size(); i++){
            toExec.get(i).accept(this);
        }
        builder.deIncrimentForLoopLevel();

        String incriment = toMod.acceptResult(this);
        TypeCheckerQualities qual = toMod.acceptResult(typeChecker);
        if(qual.containsQualities(TypeCheckerQualities.REAL)){
          String result = builder.buildRealAdditionAssignment(new IdentExp(curvalue.toString()), new IdentExp(incriment));
          builder.buildVariableAssignment(curvalue.toString(), result);
        } else {
          String result = builder.buildIntegerAdditionAssignment(new IdentExp(curvalue.toString()), new IdentExp(incriment));
          builder.buildVariableAssignment(curvalue.toString(), result);
        }

        builder.buildForLoopEnd();
    } else {
      forbranch.getInitAssignment().accept(this);
      String target = forbranch.getTargetExpression().acceptResult(this);
      IdentExp targetIdent = new IdentExp(target);
      StringEntry curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
      IdentExp curvalueIdent = new IdentExp(curvalue.toString());
      builder.buildForLoopBeginning(curvalueIdent, BinExp.Operator.NE, targetIdent);
      builder.incrimentForLoopLevel();
      for(int i = 0; i < toExec.size(); i++){
          toExec.get(i).accept(this);
      }
      builder.deIncrimentForLoopLevel();
      builder.buildForLoopEnd();
    }
  }
        
  @Override
  public void visit(Assignment assignment) {
    StringEntry place = varEnvironment.getEntry(assignment.getVariableName().getLexeme());
    String value = assignment.getVariableValue().acceptResult(this);
    builder.buildVariableAssignment(place.toString(), value);
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
      TypeCheckerQualities leftType = binaryOperation.getLeft().acceptResult(typeChecker);
      
      String rightValue = binaryOperation.getRight().acceptResult(this);
      IdentExp rightIdent = new IdentExp(rightValue);
      TypeCheckerQualities rightType = binaryOperation.getRight().acceptResult(typeChecker);

      if(leftType == null){
        errorLog.add("Error: rightHand side of expression equals null value " + binaryOperation.getLeft(), binaryOperation.getLeft().getStart());
        return "";
      }

      if(rightType == null){
        errorLog.add("Error: leftHand side of expression equals null value " + binaryOperation.getRight(), binaryOperation.getRight().getStart());
        return "";
      }

      if(leftType.containsQualities(TypeCheckerQualities.REAL) || rightType.containsQualities(TypeCheckerQualities.REAL)){
        switch (binaryOperation.getOperator()){
          case PLUS: return builder.buildRealAdditionAssignment(leftIdent, rightIdent);
          case MINUS: return builder.buildRealSubtractionAssignment(leftIdent, rightIdent);
          case TIMES: return builder.buildRealMultiplicationAssignment(leftIdent, rightIdent);
          case DIVIDE: return builder.buildRealDivisionAssignment(leftIdent, rightIdent);
          case DIV: return builder.buildIntegerDivAssignment(leftIdent, rightIdent);
          case MOD: return builder.buildIntegerModuloAssignment(leftIdent, rightIdent);
          case LE: return builder.buildLessThanOrEqualAssignment(leftIdent, rightIdent);
          case LT: return builder.buildLessThanAssignment(leftIdent, rightIdent);
          case GE: return builder.buildGreaterThanOrEqualToAssignment(leftIdent, rightIdent);
          case GT: return builder.buildGreaterThanAssignment(leftIdent, rightIdent);
          case AND: return builder.buildLogicalAndAssignment(leftIdent, rightIdent);
          case OR: return builder.buildLogicalOrAssignment(leftIdent, rightIdent);
          case EQ: return builder.buildEqualityAssignment(leftIdent, rightIdent);
          case NE: return builder.buildInequalityAssignment(leftIdent, rightIdent);
          default: return leftValue;
      }
    } else {
      switch (binaryOperation.getOperator()){
        case PLUS: return builder.buildIntegerAdditionAssignment(leftIdent, rightIdent);
        case MINUS: return builder.buildIntegerSubtractionAssignment(leftIdent, rightIdent);
        case TIMES: return builder.buildIntegerMultiplicationAssignment(leftIdent, rightIdent);
        case DIV: return builder.buildIntegerDivAssignment(leftIdent, rightIdent);
        case DIVIDE: return builder.buildRealDivisionAssignment(leftIdent, rightIdent);
        case MOD: return builder.buildIntegerModuloAssignment(leftIdent, rightIdent);
        case LE: return builder.buildLessThanOrEqualAssignment(leftIdent, rightIdent);
        case LT: return builder.buildLessThanAssignment(leftIdent, rightIdent);
        case GE: return builder.buildGreaterThanOrEqualToAssignment(leftIdent, rightIdent);
        case GT: return builder.buildGreaterThanAssignment(leftIdent, rightIdent);
        case AND: return builder.buildLogicalAndAssignment(leftIdent, rightIdent);
        case BAND: return builder.buildIntegerAndAssignment(leftIdent, rightIdent);
        case BOR: return builder.buildIntegerOrAssignment(leftIdent, rightIdent);
        case LSHIFT: return builder.buildLeftShiftAssignment(leftIdent, rightIdent);
        case RSHIFT: return builder.buildRightShiftAssignment(leftIdent, rightIdent);
        case OR: return builder.buildLogicalOrAssignment(leftIdent, rightIdent);
        case EQ: return builder.buildEqualityAssignment(leftIdent, rightIdent);
        case NE: return builder.buildInequalityAssignment(leftIdent, rightIdent);
        default: return leftValue;
      }
    }
  }

  @Override
  public String visitResult(FunctionCall funcCall) {
    String funcName = funcCall.getFunctionName().getLexeme();
    List<Expression> valArgs = funcCall.getArguments();
    List<Tuple<String, String>> valArgResults = new ArrayList<>();
    StringEntryList argsToMap = procArgs.getEntry(funcName);
    for(int i = 0; i < valArgs.size(); i++){
      Expression valArg = valArgs.get(i);
	    String result = valArg.acceptResult(this);
	    valArgResults.add(new Tuple<String, String>(result, argsToMap.get(i)));
    }
    builder.buildProcedure(funcName, valArgResults);
    StringEntry returnPlace = procEnvironment.getEntry(funcName);
    return builder.buildReturnPlacement(returnPlace.toString());
  }

  @Override
  public String visitResult(UnaryOperation unaryOperation) {
    String value = unaryOperation.getExpression().acceptResult(this);
    IdentExp valueIdent = new IdentExp(value);
    TypeCheckerQualities rightType = unaryOperation.getExpression().acceptResult(typeChecker);

    if(rightType.containsQualities(TypeCheckerQualities.REAL)){
      switch(unaryOperation.getOperator()){
        case MINUS: return builder.buildRealNegationAssignment(valueIdent);
        case NOT: return builder.buildNotAssignment(valueIdent);
        default: return value;
	    }
    } else {
      switch(unaryOperation.getOperator()){
        case MINUS: return builder.buildIntegerNegationAssignment(valueIdent);
        case NOT: return builder.buildNotAssignment(valueIdent);
        default: return value;
	    }
    }
  }
    
  @Override
  public String visitResult(Identifier identifier){
    if(varEnvironment.inScope(identifier.getLexeme())){
      StringEntry place = varEnvironment.getEntry(identifier.getLexeme());
      if(place != null)
        return place.toString();
      else{
        errorLog.add("WHen generating ICode could not find place associated with local identifier " + identifier.getLexeme(), identifier.getStart());
        return "";
      }
    } else if(paramEnvironment.entryExists(identifier.getLexeme())){
        StringEntry place = paramEnvironment.getEntry(identifier.toString());
        String newPlace = builder.buildParamaterAssignment(place.toString());
        varEnvironment.addEntry(identifier.getLexeme(), new StringEntry(newPlace));
        return newPlace;
    } else {
      StringEntry place = varEnvironment.getEntry(identifier.getLexeme());
      if(place != null){
        return place.toString();
      } else {
        errorLog.add("WHen generating ICode could not find place associated with identifier " + identifier.getLexeme(), identifier.getStart());
        return "";
      }
    }
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

  @Override
  public String visitResult(ConstDeclaration constDeclaration) {
    return null;
  }

  @Override
  public String visitResult(VariableDeclaration varDeclaration) {
    return null;
  }

  @Override
  public String visitResult(ProcedureDeclaration varDeclaration) {
    return null;
  }

  @Override
  public String visitResult(ParamaterDeclaration parDeclaration) {
    Identifier id = parDeclaration.getIdentifier();
    String alias = builder.buildAlias();
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(alias));
    return alias;
  }

  @Override
  public void visit(ParamaterDeclaration declaration) {
  }

  @Override
  public void visit(Asm asm) {
     List<String> icodeParams = new LinkedList<String>();
     for(String param : asm.getParamaters()){
       if(varEnvironment.inScope(param)){
          StringEntry icodeParam = varEnvironment.getEntry(param);
          icodeParams.add(icodeParam.toString());
       }
       else if(paramEnvironment.entryExists(param)){
          StringEntry icodeParam = paramEnvironment.getEntry(param);
          String result = builder.buildParamaterAssignment(icodeParam.toString());
          icodeParams.add(result);
          varEnvironment.addEntry(param, new StringEntry(result));
       } else {
         StringEntry icodeParam = varEnvironment.getEntry(param);
         icodeParams.add(icodeParam.toString());
       }
     }
     builder.buildInlineAssembly(asm.getInlineAssembly(), icodeParams);
  }
}
