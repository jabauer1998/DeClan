package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.AssignmentBuilder;
import io.github.H20man13.DeClan.common.builder.IrBuilderContext;
import io.github.H20man13.DeClan.common.builder.LibraryBuilder;
import io.github.H20man13.DeClan.common.builder.ProcedureBuilder;
import io.github.H20man13.DeClan.common.builder.ProgramBuilder;
import io.github.H20man13.DeClan.common.builder.StatementBuilder;
import io.github.H20man13.DeClan.common.builder.section.CodeSectionBuilder;
import io.github.H20man13.DeClan.common.builder.section.DataSectionBuilder;
import io.github.H20man13.DeClan.common.builder.section.ProcedureSectionBuilder;
import io.github.H20man13.DeClan.common.builder.section.SymbolSectionBuilder;
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
import java.util.function.Function;

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

public class MyICodeGenerator{
  private ErrorLog errorLog;
  private Environment<String, StringEntry> varEnvironment;
  private Environment<String, StringEntry> paramEnvironment;
  private Environment<String, StringEntry> procEnvironment;
  private Environment<String, StringEntryList> procArgs;
  private MyTypeChecker typeChecker;
  private MyInterpreter interpreter;
  private IrBuilderContext ctx;
  private IrRegisterGenerator gen;

  public MyICodeGenerator(ErrorLog errorLog, IrRegisterGenerator Gen) {
    this.errorLog = errorLog;
    this.ctx = ctx;
    this.gen = new IrRegisterGenerator();
    this.varEnvironment = new Environment<>();
    this.procEnvironment = new Environment<>();
    this.paramEnvironment = new Environment<>();
    this.procArgs = new Environment<>();
    this.typeChecker = new MyTypeChecker(errorLog);
    this.interpreter = new MyInterpreter(errorLog, null, null, null);
  }

  public Lib generateLibraryIr(Library lib){
    LibraryBuilder libBuilder = new LibraryBuilder(ctx, gen, errorLog);
    procEnvironment.addScope();
    varEnvironment.addScope();
    procArgs.addScope();
    typeChecker.addScope();

    DataSectionBuilder dataSecBuilder = libBuilder.getDataSectionBuilder();
    for(Declaration decl : lib.getConstDecls()){
      decl.accept(typeChecker);
      if(decl instanceof ConstDeclaration){
        generateConstantIr((ConstDeclaration)decl, dataSecBuilder);
      }
    }

    for(Declaration decl : lib.getVarDecls()){
      decl.accept(typeChecker);
      if(decl instanceof VariableDeclaration){
        generateVariableIr((VariableDeclaration)decl, dataSecBuilder);
      }
    }

    loadFunctions(lib.getProcDecls());
    typeChecker.loadFunctions(lib.getProcDecls());

    ProcedureSectionBuilder secBuilder = libBuilder.getProcedureSectionBuilder();
    ProcedureBuilder procBuilder = secBuilder.getProcedureBuilder();
    for(Declaration decl : lib.getProcDecls()){
      decl.accept(typeChecker);
      if(decl instanceof ProcedureDeclaration){
        generateProcedureIr((ProcedureDeclaration)decl, procBuilder);
      }
      typeChecker.removeVarScope();
      procBuilder.resetBuilder();
    }

    return libBuilder.completeBuild();
  }

  public Prog generateProgramIr(Program program) {
    ProgramBuilder builder = new ProgramBuilder(ctx, gen, errorLog);
    procEnvironment.addScope();
    varEnvironment.addScope();
    procArgs.addScope();
    typeChecker.addScope();

    DataSectionBuilder variableBuilder = builder.getDataSectionBuilder(); 
    for(ConstDeclaration decl : program.getConstDecls()){
      decl.accept(typeChecker);
      generateConstantIr(decl, variableBuilder);      
    }

    for (VariableDeclaration decl : program.getVarDecls()) {
      decl.accept(typeChecker);
      generateVariableIr(decl, variableBuilder);
    }

    loadFunctions(program.getProcDecls());
    typeChecker.loadFunctions(program.getProcDecls());

    CodeSectionBuilder codeBuilder = builder.getCodeSectionBuilder();
    for (Statement statement : program.getStatements()) {
      generateStatementIr(statement, codeBuilder);
    }

    ProcedureSectionBuilder procSectionBuilder = builder.getProcedureSectionBuilder();
    ProcedureBuilder procBuilder = procSectionBuilder.getProcedureBuilder();
    for (ProcedureDeclaration decl : program.getProcDecls()){
      generateProcedureIr(decl, procBuilder);
      procSectionBuilder.addProcedure(procBuilder.completeBuild());
      procBuilder.resetBuilder();
    }

    varEnvironment.removeScope();
    procEnvironment.removeScope();
    procArgs.removeScope();

    return builder.completeBuild();
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

  public void generateConstantIr(ConstDeclaration constDecl, AssignmentBuilder builder) {
    Identifier id = constDecl.getIdentifier();
    Expression valueExpr = constDecl.getValue();
    String value = generateExpressionIr(valueExpr, builder);
    String place = builder.buildVariableAssignment(value);
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(place));
    SymbolSectionBuilder symbolBuilder = builder.getSymbolSectionBuilder();
    symbolBuilder.addSymEntry(SymEntry.CONST | SymEntry.INTERNAL, place, id.getLexeme());
  }

  public void generateVariableIr(VariableDeclaration varDecl, AssignmentBuilder builder) {
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
    SymbolSectionBuilder symBuilder = builder.getSymbolSectionBuilder();
    symBuilder.addSymEntry(SymEntry.INTERNAL, place, id.getLexeme());
  }

  private void loadFunctions(List<ProcedureDeclaration> decls){
    for(ProcedureDeclaration decl : decls){
      loadFunction(decl);
    }
  }

  public void loadFunction(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    List <ParamaterDeclaration> args = procDecl.getArguments();
    
    StringEntryList alias = new StringEntryList();
    for(int i = 0; i < args.size(); i++){
	    String argAlias = gen.genNextRegister();
      alias.add(argAlias);
    }

    procArgs.addEntry(procedureName, alias);
  
    String returnPlace = gen.genNextRegister();
    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      procEnvironment.addEntry(procedureName, new StringEntry(returnPlace));
    }
  } 

  public void generateProcedureIr(ProcedureDeclaration procDecl, ProcedureBuilder builder){
    String procedureName = procDecl.getProcedureName().getLexeme();
    builder.buildProcedureLabel(procedureName);
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
      Declaration localDecl = localVars.get(i);
      if(localDecl instanceof VariableDeclaration){
        generateVariableIr((VariableDeclaration)localVars.get(i), builder);
      }
    }

    List <Statement> exec = procDecl.getExecutionStatements();
    for(int i = 0; i < exec.size(); i++){
	    generateStatementIr(exec.get(i), builder);
    }

    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      String retPlace = generateExpressionIr(retExp, builder);
      String returnPlace = procEnvironment.getEntry(procedureName).toString();
      builder.buildInternalReturnPlacement(returnPlace, retPlace);
    }
    builder.buildReturnStatement();
    varEnvironment.removeScope();
    paramEnvironment.removeScope();
  }

  public void generateStatementIr(Statement stat, StatementBuilder builder){
    if(stat instanceof ProcedureCall) generateProcedureCallIr((ProcedureCall)stat, builder);
    else if(stat instanceof Branch) generateBranchIr((Branch)stat, builder);
    else if(stat instanceof Assignment) generateAssignmentIr((Assignment)stat, builder);
    else if(stat instanceof Asm) generateInlineAssemblyIr((Asm)stat, builder);
    else {
      errorLog.add("Error generating invalid statment type " + stat.getClass().getSimpleName(), stat.getStart());
    }
  }
        
  public void generateProcedureCallIr(ProcedureCall procedureCall, StatementBuilder builder) {
    String funcName = procedureCall.getProcedureName().getLexeme();
    List<Expression> valArgs = procedureCall.getArguments();

    if(procArgs.entryExists(funcName)){
      //Generate a standard Procedure Call
      StringEntryList argsToMap = procArgs.getEntry(funcName);
      List<Tuple<String, String>> valArgResults = new ArrayList<Tuple<String, String>>();
      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
        String result = generateExpressionIr(valArg, builder);
        valArgResults.add(new Tuple<String, String>(result, argsToMap.get(i)));
      }
      builder.buildProcedureCall(funcName, valArgResults);
    } else {
      //Generate an External Procedure Call
      LinkedList<String> args = new LinkedList<String>();
      for(Expression valArg : valArgs){
         String place = generateExpressionIr(valArg, builder);
         args.add(place);
      }
      builder.buildExternalProcedureCall(funcName, args);
    }
  }

  public void generateWhileBranchIr(WhileElifBranch whilebranch, StatementBuilder builder){
    Expression toCheck = whilebranch.getExpression();
    List<Statement> toExec = whilebranch.getExecStatements();
    String test = generateExpressionIr(toCheck, builder);
    
    IdentExp ident = new IdentExp(test);
    builder.buildWhileLoopBeginning(ident);

    builder.incrimentWhileLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(toExec.get(i), builder);
    }

    String test2 = generateExpressionIr(toCheck, builder);
    builder.buildVariableAssignment(test, test2);

    builder.deIncrimentWhileLoopLevel();

    if(whilebranch.getNextBranch() != null) {
      builder.buildElseWhileLoopBeginning();
      generateBranchIr(whilebranch, builder);
    } else {
      builder.buildWhileLoopEnd();
    }
  }
    
  public void generateBranchIr(Branch branch, StatementBuilder builder){
    if(branch instanceof IfElifBranch) generateIfBranchIr((IfElifBranch)branch, builder);
    else if(branch instanceof ElseBranch) generateElseBranchIr((ElseBranch)branch, builder);
    else if(branch instanceof WhileElifBranch) generateWhileBranchIr((WhileElifBranch)branch, builder);
    else if(branch instanceof RepeatBranch) generateRepeatLoopIr((RepeatBranch)branch, builder);
    else if(branch instanceof ForBranch) generateForLoopIr((ForBranch)branch, builder);
    else {
      errorLog.add("Error unexpected branch type " + branch.getClass().getSimpleName(), branch.getStart());
    }
  }
  public void generateIfBranchIr(IfElifBranch ifbranch, StatementBuilder builder){
    Expression toCheck = ifbranch.getExpression();
    String test = generateExpressionIr(toCheck, builder);
    IdentExp ident = new IdentExp(test);
    builder.buildIfStatementBeginning(ident);

    builder.incrimentIfStatementLevel();
    List<Statement> toExec = ifbranch.getExecStatements();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(toExec.get(i), builder);
    }
    builder.deIncrimentIfStatementLevel();

    if(ifbranch.getNextBranch() != null) {
      builder.buildElseIfStatementBeginning();
      generateBranchIr(ifbranch.getNextBranch(), builder);
    } else {
      builder.buildIfStatementEnd();
    }
  }

  public void generateElseBranchIr(ElseBranch elsebranch, StatementBuilder builder){
    List<Statement> toExec = elsebranch.getExecStatements();
    builder.incrimentIfStatementLevel();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(toExec.get(i), builder);
    }
    builder.deIncrimentIfStatementLevel();
    builder.buildIfStatementEnd();
  }

  public void generateRepeatLoopIr(RepeatBranch repeatbranch, StatementBuilder builder){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    String test = generateExpressionIr(toCheck, builder);

    builder.buildRepeatLoopBeginning(test);

    builder.incrimentRepeatLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
	    generateStatementIr(toExec.get(i), builder);
    }
    builder.deIncrimentRepeatLoopLevel();

    String test2 = generateExpressionIr(toCheck, builder);
    builder.buildVariableAssignment(test, test2);

    builder.buildRepeatLoopEnd();
  }

  public void generateForLoopIr(ForBranch forbranch, StatementBuilder builder){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
        generateAssignmentIr(forbranch.getInitAssignment(), builder);
        String target = generateExpressionIr(forbranch.getTargetExpression(), builder);
        
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
            generateStatementIr(toExec.get(i), builder);
        }
        builder.deIncrimentForLoopLevel();

        String incriment = generateExpressionIr(toMod, builder);
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
      generateAssignmentIr(forbranch.getInitAssignment(), builder);
      String target = generateExpressionIr(forbranch.getTargetExpression(), builder);
      IdentExp targetIdent = new IdentExp(target);
      StringEntry curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
      IdentExp curvalueIdent = new IdentExp(curvalue.toString());
      builder.buildForLoopBeginning(curvalueIdent, BinExp.Operator.NE, targetIdent);
      builder.incrimentForLoopLevel();
      for(int i = 0; i < toExec.size(); i++){
          generateStatementIr(toExec.get(i), builder);
      }
      builder.deIncrimentForLoopLevel();
      builder.buildForLoopEnd();
    }
  }
        
  public void generateAssignmentIr(Assignment assignment, AssignmentBuilder builder) {
    StringEntry place = varEnvironment.getEntry(assignment.getVariableName().getLexeme());
    String value = generateExpressionIr(assignment.getVariableValue(), builder);
    builder.buildVariableAssignment(place.toString(), value);
  }

  public void generateInlineAssemblyIr(Asm asm, StatementBuilder builder) {
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

  
  public String generateExpressionIr(Expression exp, AssignmentBuilder builder){
    if(exp instanceof BinaryOperation) return generateBinaryOperationIr((BinaryOperation)exp, builder);
    else if(exp instanceof FunctionCall) return generateFunctionCallIr((FunctionCall)exp, builder);
    else if(exp instanceof UnaryOperation) return generateUnaryOperationIr((UnaryOperation)exp, builder);
    else if(exp instanceof Identifier) return generateIdentifierIr((Identifier)exp, builder);
    else if(exp instanceof NumValue) return generateNumberIr((NumValue)exp, builder);
    else if(exp instanceof BoolValue) return generateBooleanIr((BoolValue)exp, builder);
    else if(exp instanceof StrValue) return generateStringIr((StrValue)exp, builder);
    else {
      errorLog.add("Error Invalid Expression Type found when generating Ir", exp.getStart());
      return "NOTFOUND";
    }
  }
  
  
  public String generateBinaryOperationIr(BinaryOperation binaryOperation, AssignmentBuilder builder) {
      String leftValue = generateExpressionIr(binaryOperation.getLeft(), builder);
      IdentExp leftIdent = new IdentExp(leftValue);
      TypeCheckerQualities leftType = binaryOperation.getLeft().acceptResult(typeChecker);
      
      String rightValue = generateExpressionIr(binaryOperation.getRight(), builder);
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
        case BXOR: return builder.buildIntegerExclusiveOrAssignment(leftIdent, rightIdent);
        case LSHIFT: return builder.buildLeftShiftAssignment(leftIdent, rightIdent);
        case RSHIFT: return builder.buildRightShiftAssignment(leftIdent, rightIdent);
        case OR: return builder.buildLogicalOrAssignment(leftIdent, rightIdent);
        case EQ: return builder.buildEqualityAssignment(leftIdent, rightIdent);
        case NE: return builder.buildInequalityAssignment(leftIdent, rightIdent);
        default: return leftValue;
      }
    }
  }

  public String generateFunctionCallIr(FunctionCall funcCall, AssignmentBuilder builder) {
    String funcName = funcCall.getFunctionName().getLexeme();
    List<Expression> valArgs = funcCall.getArguments();
    if(procArgs.entryExists(funcName)){
      //Build Internal Function Call Sequence
      StringEntryList argsToMap = procArgs.getEntry(funcName);
      List<Tuple<String, String>> valArgResults = new ArrayList<>();

      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
	      String result = generateExpressionIr(valArg, builder);
	      valArgResults.add(new Tuple<String, String>(result, argsToMap.get(i)));
      }
      builder.buildProcedureCall(funcName, valArgResults);
      StringEntry returnPlace = procEnvironment.getEntry(funcName);
      return builder.buildExternalReturnPlacement(returnPlace.toString());
    } else {
      //Build external function call
      LinkedList<String> procedureArgs = new LinkedList<String>();
      for(Expression arg : valArgs){
        String place = generateExpressionIr(arg, builder);
        procedureArgs.add(place);
      }

      String retPlace = builder.buildExternalFunctionCall(funcName, procedureArgs);
      return retPlace;
    }
  }

  public String generateUnaryOperationIr(UnaryOperation unaryOperation, AssignmentBuilder builder) {
    String value = generateExpressionIr(unaryOperation.getExpression(), builder);
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
        case BNOT: return builder.buildIntegerNotAssignment(valueIdent);
        default: return value;
	    }
    }
  }

  public String generateIdentifierIr(Identifier identifier, AssignmentBuilder builder){
    SymbolSectionBuilder symBuilder = builder.getSymbolSectionBuilder();
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
      if(varEnvironment.entryExists(identifier.getLexeme())){
        StringEntry place = varEnvironment.getEntry(identifier.getLexeme());
        if(place != null){
          return place.toString();
        } else {
          errorLog.add("WHen generating ICode could not find place associated with identifier " + identifier.getLexeme(), identifier.getStart());
          return "";
        }
      } else {
        String place = gen.genNextRegister();
        symBuilder.addSymEntry(SymEntry.EXTERNAL, place, identifier.getLexeme());
        varEnvironment.addEntry(identifier.getLexeme(), new StringEntry(place));
        return place;
      }
    }
  }

  public String generateNumberIr(NumValue numValue, AssignmentBuilder builder){
      String rawnum = ifHexToInt(numValue.getLexeme());
      return builder.buildNumAssignment(rawnum);
  }

  public String generateBooleanIr(BoolValue boolValue, AssignmentBuilder builder){
      String lexeme = boolValue.getLexeme(); //change to hex if you need to otherwise unchanged
      return builder.buildBoolAssignment(lexeme);
  }

  public String generateStringIr(StrValue strValue, AssignmentBuilder builder){
      return builder.buildStringAssignment(strValue.getLexeme());
  }


  public String generateParamaterDeclarationIr(ParamaterDeclaration parDeclaration, AssignmentBuilder builder) {
    Identifier id = parDeclaration.getIdentifier();
    String alias = gen.genNextRegister();
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(alias));
    return alias;
  }
}
