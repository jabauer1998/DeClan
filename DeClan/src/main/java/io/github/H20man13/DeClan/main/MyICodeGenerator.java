package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.icode.Assign.Scope;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.procedure.Call;
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
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.StringEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.StringEntryList;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.Utils;

import static io.github.H20man13.DeClan.common.gen.IrRegisterGenerator.*;
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

  public MyICodeGenerator(ErrorLog errorLog) {
    this.errorLog = errorLog;
    this.ctx = new IrBuilderContext();
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
    for(ConstDeclaration decl : lib.getConstDecls()){
      decl.accept(typeChecker);
      generateConstantIr(Scope.GLOBAL, decl, dataSecBuilder);
    }

    for(VariableDeclaration decl : lib.getVarDecls()){
      decl.accept(typeChecker);
      generateVariableIr(Scope.GLOBAL, decl, dataSecBuilder);
    }

    loadFunctions(lib.getProcDecls());
    typeChecker.loadFunctions(lib.getProcDecls());

    ProcedureSectionBuilder secBuilder = libBuilder.getProcedureSectionBuilder();
    ProcedureBuilder procBuilder = secBuilder.getProcedureBuilder();
    for(ProcedureDeclaration decl : lib.getProcDecls()){
      decl.accept(typeChecker);
      generateProcedureIr(decl, procBuilder);
      secBuilder.addProcedure(procBuilder.completeBuild());
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
      generateConstantIr(Scope.GLOBAL, decl, variableBuilder);      
    }

    for (VariableDeclaration decl : program.getVarDecls()) {
      decl.accept(typeChecker);
      generateVariableIr(Scope.GLOBAL, decl, variableBuilder);
    }

    loadFunctions(program.getProcDecls());
    typeChecker.loadFunctions(program.getProcDecls());

    CodeSectionBuilder codeBuilder = builder.getCodeSectionBuilder();
    for (Statement statement : program.getStatements()) {
      statement.accept(typeChecker);
      generateStatementIr(Scope.LOCAL, statement, codeBuilder);
    }

    ProcedureSectionBuilder procSectionBuilder = builder.getProcedureSectionBuilder();
    ProcedureBuilder procBuilder = procSectionBuilder.getProcedureBuilder();
    for (ProcedureDeclaration decl : program.getProcDecls()){
      decl.accept(typeChecker);
      generateProcedureIr(decl, procBuilder);
      procSectionBuilder.addProcedure(procBuilder.completeBuild());
      typeChecker.removeVarScope();
      procBuilder.resetBuilder();
    }

    varEnvironment.removeScope();
    procEnvironment.removeScope();
    procArgs.removeScope();

    return builder.completeBuild();
  }

  public void generateConstantIr(Scope scope, ConstDeclaration constDecl, AssignmentBuilder builder) {
    Identifier id = constDecl.getIdentifier();
    Expression valueExpr = constDecl.getValue();
    String value = generateExpressionIr(Scope.LOCAL, valueExpr, builder);
    TypeCheckerQualities qual = valueExpr.acceptResult(typeChecker);
    Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    String place = builder.buildVariableAssignment(scope, value, type);
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(place));
    SymbolSectionBuilder symbolBuilder = builder.getSymbolSectionBuilder();
    symbolBuilder.addSymEntry(SymEntry.CONST | SymEntry.INTERNAL, place, id.getLexeme());
  }

  public void generateVariableIr(Scope scope, VariableDeclaration varDecl, AssignmentBuilder builder) {
    Identifier id = varDecl.getIdentifier();
    Identifier type = varDecl.getType();
    String place = null;
    if(type.getLexeme().equals("STRING")){
      place = builder.buildStringAssignment(scope, "\0");
    } else if(type.getLexeme().equals("REAL")) {
      place = builder.buildNumAssignment(scope, "0.0");
    } else if(type.getLexeme().equals("BOOLEAN")){
      place = builder.buildBoolAssignment(scope, "FALSE");
    } else {
      place = builder.buildNumAssignment(scope, "0");
    }
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
	    String argAlias = gen.genNext();
      alias.add(argAlias);
    }

    procArgs.addEntry(procedureName, alias);
  
    String returnPlace = gen.genNext();
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
        generateVariableIr(Scope.LOCAL, (VariableDeclaration)localDecl, builder);
      } else if(localDecl instanceof ConstDeclaration){
        generateConstantIr(Scope.LOCAL, (ConstDeclaration)localDecl, builder);
      }
    }

    List <Statement> exec = procDecl.getExecutionStatements();
    for(int i = 0; i < exec.size(); i++){
      Statement stat = exec.get(i);
	    generateStatementIr(Scope.LOCAL, stat, builder);
    }

    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      TypeCheckerQualities qual = retExp.acceptResult(typeChecker); 
      String retPlace = generateExpressionIr(Scope.LOCAL, retExp, builder);
      String returnPlace = procEnvironment.getEntry(procedureName).toString();
      builder.buildInternalReturnPlacement(returnPlace, retPlace, ConversionUtils.typeCheckerQualitiesToAssignType(qual));
    }
    builder.buildReturnStatement();
    varEnvironment.removeScope();
    paramEnvironment.removeScope();
  }

  public void generateStatementIr(Scope scope, Statement stat, StatementBuilder builder){
    if(stat instanceof ProcedureCall) generateProcedureCallIr(scope, (ProcedureCall)stat, builder);
    else if(stat instanceof Branch) generateBranchIr(scope, (Branch)stat, builder);
    else if(stat instanceof Assignment) generateAssignmentIr(scope, (Assignment)stat, builder);
    else if(stat instanceof Asm) generateInlineAssemblyIr((Asm)stat, builder);
    else if(stat instanceof EmptyStatement){
         //Do nothing
    }
    else {
      errorLog.add("Error generating invalid statment type " + stat.getClass().getSimpleName(), stat.getStart());
    }
  }
        
  public void generateProcedureCallIr(Scope scope, ProcedureCall procedureCall, StatementBuilder builder) {
    String funcName = procedureCall.getProcedureName().getLexeme();
    List<Expression> valArgs = procedureCall.getArguments();

    if(procArgs.entryExists(funcName)){
      //Generate a standard Procedure Call
      StringEntryList argsToMap = procArgs.getEntry(funcName);
      List<Assign> valArgResults = new ArrayList<Assign>();
      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
        TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
        Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
        String result = generateExpressionIr(Scope.LOCAL, valArg, builder);
        valArgResults.add(new Assign(Scope.ARGUMENT, argsToMap.get(i), new IdentExp(result), type));
      }
      builder.buildProcedureCall(funcName, valArgResults);
    } else {
      //Generate an External Procedure Call
      LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
      for(Expression valArg : valArgs){
         String place = generateExpressionIr(scope, valArg, builder);
         TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
         Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
         args.add(new Tuple<String, Assign.Type>(place, type));
      }
      builder.buildExternalProcedureCall(funcName, args);
    }
  }

  public void generateWhileBranchIr(Scope scope, WhileElifBranch whilebranch, StatementBuilder builder){
    Expression toCheck = whilebranch.getExpression();
    List<Statement> toExec = whilebranch.getExecStatements();
    String test = generateExpressionIr(scope, toCheck, builder);
    
    IdentExp ident = new IdentExp(test);
    builder.buildWhileLoopBeginning(ident);

    builder.incrimentWhileLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(scope, toExec.get(i), builder);
    }

    String test2 = generateExpressionIr(scope, toCheck, builder);
    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
    Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    builder.buildVariableAssignment(scope, test, test2, type);

    builder.deIncrimentWhileLoopLevel();

    Branch nextBranch = whilebranch.getNextBranch();
    if(nextBranch != null) {
      builder.buildElseWhileLoopBeginning();
      generateBranchIr(scope, nextBranch, builder);
    } else {
      builder.buildWhileLoopEnd();
    }
  }
    
  public void generateBranchIr(Scope scope, Branch branch, StatementBuilder builder){
    if(branch instanceof IfElifBranch) generateIfBranchIr(scope, (IfElifBranch)branch, builder);
    else if(branch instanceof ElseBranch) generateElseBranchIr(scope, (ElseBranch)branch, builder);
    else if(branch instanceof WhileElifBranch) generateWhileBranchIr(scope, (WhileElifBranch)branch, builder);
    else if(branch instanceof RepeatBranch) generateRepeatLoopIr(scope, (RepeatBranch)branch, builder);
    else if(branch instanceof ForBranch) generateForLoopIr(scope, (ForBranch)branch, builder);
    else {
      errorLog.add("Error unexpected branch type " + branch.getClass().getSimpleName(), branch.getStart());
    }
  }
  public void generateIfBranchIr(Scope scope, IfElifBranch ifbranch, StatementBuilder builder){
    Expression toCheck = ifbranch.getExpression();
    String test = generateExpressionIr(scope, toCheck, builder);
    IdentExp ident = new IdentExp(test);
    builder.buildIfStatementBeginning(ident);

    builder.incrimentIfStatementLevel();
    List<Statement> toExec = ifbranch.getExecStatements();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(scope, toExec.get(i), builder);
    }
    builder.deIncrimentIfStatementLevel();

    if(ifbranch.getNextBranch() != null) {
      builder.buildElseIfStatementBeginning();
      generateBranchIr(scope, ifbranch.getNextBranch(), builder);
    } else {
      builder.buildIfStatementEnd();
    }
  }

  public void generateElseBranchIr(Scope scope, ElseBranch elsebranch, StatementBuilder builder){
    List<Statement> toExec = elsebranch.getExecStatements();
    builder.incrimentIfStatementLevel();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(scope, toExec.get(i), builder);
    }
    builder.deIncrimentIfStatementLevel();
    builder.buildIfStatementEnd();
  }

  public void generateRepeatLoopIr(Scope scope, RepeatBranch repeatbranch, StatementBuilder builder){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    String test = generateExpressionIr(scope, toCheck, builder);

    builder.buildRepeatLoopBeginning(test);

    builder.incrimentRepeatLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
	    generateStatementIr(scope, toExec.get(i), builder);
    }
    builder.deIncrimentRepeatLoopLevel();

    String test2 = generateExpressionIr(scope, toCheck, builder);
    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
    Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    builder.buildVariableAssignment(scope, test, test2, type);

    builder.buildRepeatLoopEnd();
  }

  public void generateForLoopIr(Scope scope, ForBranch forbranch, StatementBuilder builder){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
        generateAssignmentIr(scope, forbranch.getInitAssignment(), builder);
        String target = generateExpressionIr(scope, forbranch.getTargetExpression(), builder);
        
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
        } else if(actualIncriment instanceof Float){
          Float floatActualIncriment = Utils.toReal(actualIncriment);
          if(floatActualIncriment < 0){
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.GT, targetIdent);
          } else if(floatActualIncriment > 0){
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.LT, targetIdent);
          } else {
            builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.NE, targetIdent);
          }
        } else {
          builder.buildForLoopBeginning(curValueIdent, BinExp.Operator.NE, targetIdent);
        }

        
        
        builder.incrimentForLoopLevel();
        for(int i = 0; i < toExec.size(); i++){
            generateStatementIr(scope, toExec.get(i), builder);
        }
        builder.deIncrimentForLoopLevel();

        String incriment = generateExpressionIr(scope, toMod, builder);
        TypeCheckerQualities qual = toMod.acceptResult(typeChecker);
        if(qual.containsQualities(TypeCheckerQualities.REAL)){
          String result = null;
          if(procArgs.entryExists("RAdd") && procEnvironment.entryExists("RAdd")){
              StringEntryList params = procArgs.getEntry("RAdd");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(curvalue.toString()), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(incriment), Assign.Type.REAL));

                builder.buildProcedureCall("RAdd", args);

                StringEntry returnPlace = procEnvironment.getEntry("RAdd");
                result = builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function RAdd that contains two arguments", forbranch.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(curvalue.toString(), Assign.Type.REAL));
                 args.add(new Tuple<String, Assign.Type>(incriment, Assign.Type.REAL));
                 result = builder.buildExternalFunctionCall(scope, "RAdd", args, Assign.Type.REAL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                args.add(new Tuple<String, Assign.Type>(curvalue.toString(), Assign.Type.REAL));
                args.add(new Tuple<String, Assign.Type>(incriment, Assign.Type.REAL));
                result = builder.buildExternalFunctionCall(scope, "RAdd", args, Assign.Type.REAL);
            }
            builder.buildVariableAssignment(scope, curvalue.toString(), result, Assign.Type.REAL);
        } else {
          String result = builder.buildIntegerAdditionAssignment(scope, new IdentExp(curvalue.toString()), new IdentExp(incriment));
          builder.buildVariableAssignment(scope, curvalue.toString(), result, Assign.Type.INT);
        }

        builder.buildForLoopEnd();
    } else {
      generateAssignmentIr(scope, forbranch.getInitAssignment(), builder);
      String target = generateExpressionIr(scope, forbranch.getTargetExpression(), builder);
      IdentExp targetIdent = new IdentExp(target);
      StringEntry curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
      IdentExp curvalueIdent = new IdentExp(curvalue.toString());
      builder.buildForLoopBeginning(curvalueIdent, BinExp.Operator.NE, targetIdent);
      builder.incrimentForLoopLevel();
      for(int i = 0; i < toExec.size(); i++){
          generateStatementIr(scope, toExec.get(i), builder);
      }
      builder.deIncrimentForLoopLevel();
      builder.buildForLoopEnd();
    }
  }
        
  public void generateAssignmentIr(Scope scope, Assignment assignment, AssignmentBuilder builder) {
    StringEntry place = varEnvironment.getEntry(assignment.getVariableName().getLexeme());
    Expression exp = assignment.getVariableValue();
    String value = generateExpressionIr(scope, exp, builder);
    TypeCheckerQualities qual = exp.acceptResult(typeChecker);
    TypeCheckerQualities convType = assignment.getVariableName().acceptResult(typeChecker);
    if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.REAL)){
      if(procEnvironment.entryExists("RealToInt")){
        String argDest = procArgs.getEntry("RealToInt").getFirst();
        List<Assign> args = new LinkedList<Assign>();
        args.add(new Assign(Scope.ARGUMENT, argDest, new IdentExp(value), Assign.Type.REAL));
        builder.buildProcedureCall("RealToInt", args);

        String retPlace = procEnvironment.getEntry("RealToInt").toString();
        value = builder.buildExternalReturnPlacement(retPlace.toString(), Assign.Type.INT);
      } else {
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(value, Assign.Type.REAL));
        value = builder.buildExternalFunctionCall(scope, "RealToInt", args, Assign.Type.INT);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
      if(procEnvironment.entryExists("IntToReal")){
        String argDest = procArgs.getEntry("IntToReal").getFirst();
        List<Assign> args = new LinkedList<Assign>();
        args.add(new Assign(Scope.ARGUMENT, argDest, new IdentExp(value), Assign.Type.INT));
        builder.buildProcedureCall("IntToReal", args);

        String retPlace = procEnvironment.getEntry("IntToReal").toString();
        value = builder.buildExternalReturnPlacement(retPlace.toString(), Assign.Type.REAL);
      } else {
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(value, Assign.Type.INT));
        value = builder.buildExternalFunctionCall(scope, "IntToReal", args, Assign.Type.REAL);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
      if(procEnvironment.entryExists("IntToBool")){
        String argDest = procArgs.getEntry("IntToBool").getFirst();
        List<Assign> args = new LinkedList<Assign>();
        args.add(new Assign(Scope.ARGUMENT, argDest, new IdentExp(value), Assign.Type.INT));
        builder.buildProcedureCall("IntToBool", args);

        String retPlace = procEnvironment.getEntry("IntToBool").toString();
        value = builder.buildExternalReturnPlacement(retPlace.toString(), Assign.Type.BOOL);
      } else {
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(value, Assign.Type.INT));
        value = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
      if(procEnvironment.entryExists("BoolToInt")){
        String argDest = procArgs.getEntry("BoolToInt").getFirst();
        List<Assign> args = new LinkedList<Assign>();
        args.add(new Assign(Scope.ARGUMENT, argDest, new IdentExp(value), Assign.Type.BOOL));
        builder.buildProcedureCall("BoolToInt", args);

        String retPlace = procEnvironment.getEntry("BoolToInt").toString();
        value = builder.buildExternalReturnPlacement(retPlace.toString(), Assign.Type.INT);
      } else {
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(value, Assign.Type.BOOL));
        value = builder.buildExternalFunctionCall(scope, "BoolToInt", args, Assign.Type.INT);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.REAL)){
      if(procEnvironment.entryExists("RealToBool")){
        String argDest = procArgs.getEntry("RealToBool").getFirst();
        List<Assign> args = new LinkedList<Assign>();
        args.add(new Assign(Scope.ARGUMENT, argDest, new IdentExp(value), Assign.Type.REAL));
        builder.buildProcedureCall("RealToBool", args);

        String retPlace = procEnvironment.getEntry("RealToBool").toString();
        value = builder.buildExternalReturnPlacement(retPlace.toString(), Assign.Type.BOOL);
      } else {
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(value, Assign.Type.REAL));
        value = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
      if(procEnvironment.entryExists("BoolToReal")){
        String argDest = procArgs.getEntry("BoolToReal").getFirst();
        List<Assign> args = new LinkedList<Assign>();
        args.add(new Assign(Scope.ARGUMENT, argDest, new IdentExp(value), Assign.Type.BOOL));
        builder.buildProcedureCall("BoolToReal", args);

        String retPlace = procEnvironment.getEntry("BoolToReal").toString();
        value = builder.buildExternalReturnPlacement(retPlace.toString(), Assign.Type.REAL);
      } else {
        List<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
        args.add(new Tuple<String,Assign.Type>(value, Assign.Type.BOOL));
        value = builder.buildExternalFunctionCall(scope, "BoolToReal", args, Assign.Type.REAL);
      }
    }
    Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(convType);
    builder.buildVariableAssignment(scope, place.toString(), value, type);
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
          TypeCheckerQualities qual = typeChecker.getVarType(param);
          Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
          String result = builder.buildParamaterAssignment(icodeParam.toString(), type);
          icodeParams.add(result);
          varEnvironment.addEntry(param, new StringEntry(result));
       } else {
         StringEntry icodeParam = varEnvironment.getEntry(param);
         icodeParams.add(icodeParam.toString());
       }
     }
     builder.buildInlineAssembly(asm.getInlineAssembly(), icodeParams);
  }

  
  public String generateExpressionIr(Scope scope, Expression exp, AssignmentBuilder builder){
    if(exp instanceof BinaryOperation) return generateBinaryOperationIr(scope, (BinaryOperation)exp, builder);
    else if(exp instanceof FunctionCall) return generateFunctionCallIr(scope, (FunctionCall)exp, builder);
    else if(exp instanceof UnaryOperation) return generateUnaryOperationIr(scope, (UnaryOperation)exp, builder);
    else if(exp instanceof Identifier) return generateIdentifierIr((Identifier)exp, builder);
    else if(exp instanceof NumValue) return generateNumberIr(scope, (NumValue)exp, builder);
    else if(exp instanceof BoolValue) return generateBooleanIr(scope, (BoolValue)exp, builder);
    else if(exp instanceof StrValue) return generateStringIr(scope, (StrValue)exp, builder);
    else {
      errorLog.add("Error Invalid Expression Type found when generating Ir", exp.getStart());
      return "NOTFOUND";
    }
  }
  
  
  public String generateBinaryOperationIr(Scope scope, BinaryOperation binaryOperation, AssignmentBuilder builder) {
      String leftValue = generateExpressionIr(scope, binaryOperation.getLeft(), builder);
      IdentExp leftIdent = new IdentExp(leftValue);
      TypeCheckerQualities leftType = binaryOperation.getLeft().acceptResult(typeChecker);
      
      String rightValue = generateExpressionIr(scope, binaryOperation.getRight(), builder);
      IdentExp rightIdent = new IdentExp(rightValue);
      TypeCheckerQualities rightType = binaryOperation.getRight().acceptResult(typeChecker);

      if(binaryOperation.getOperator() == BinaryOperation.OpType.AND || binaryOperation.getOperator() == BinaryOperation.OpType.OR){
        if(leftType.containsQualities(TypeCheckerQualities.REAL)){
          if(procArgs.entryExists("RealToBool") && procEnvironment.entryExists("RealToBool")){
             StringEntryList params = procArgs.getEntry("RealToBool");
             if(params.size() >= 1){
               LinkedList<Assign> args = new LinkedList<Assign>();
               args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
               builder.buildProcedureCall("RealToBool", args);
               StringEntry entry = procEnvironment.getEntry("RealToBool");
               leftValue = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.BOOL);
             } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
              leftValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
             }
           } else {
             LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
              leftValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
           }
        } else if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToBool") && procEnvironment.entryExists("IntToBool")){
            StringEntryList params = procArgs.getEntry("IntToBool");
            if(params.size() >= 1){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.INT));
                builder.buildProcedureCall("IntToBool", args);
                StringEntry entry = procEnvironment.getEntry("IntToBool");
                leftValue = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.BOOL);
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.INT));
              leftValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
            }
          } else {
            LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
            args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.INT));
            leftValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
          }
        }

        if(rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(procArgs.entryExists("RealToBool") && procEnvironment.entryExists("RealToBool")){
             StringEntryList params = procArgs.getEntry("RealToBool");
             if(params.size() >= 1){
               LinkedList<Assign> args = new LinkedList<Assign>();
               args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(rightValue), Assign.Type.REAL));
               builder.buildProcedureCall("RealToBool", args);
               StringEntry entry = procEnvironment.getEntry("RealToBool");
               rightValue = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.BOOL);
             } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
              rightValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
             }
           } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
              rightValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
           }
        } else if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToBool") && procEnvironment.entryExists("IntToBool")){
             StringEntryList params = procArgs.getEntry("IntToBool");
             if(params.size() >= 1){
               LinkedList<Assign> args = new LinkedList<Assign>();
               args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(rightValue), Assign.Type.INT));
               builder.buildProcedureCall("IntToBool", args);
               StringEntry entry = procEnvironment.getEntry("IntToBool");
               rightValue = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.BOOL);
             } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.INT));
              rightValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
             }
           } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.INT));
              rightValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
           }
        }

        switch(binaryOperation.getOperator()){
          case AND: return builder.buildLogicalAndAssignment(scope, new IdentExp(leftValue), new IdentExp(rightValue));
          case OR: return builder.buildLogicalOrAssignment(scope, new IdentExp(leftValue), new IdentExp(rightValue));
          default: return leftValue;
        }
      } else if(leftType.containsQualities(TypeCheckerQualities.REAL) || rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
           if(procArgs.entryExists("IntToReal") && procEnvironment.entryExists("IntToReal")){
             StringEntryList params = procArgs.getEntry("IntToReal");
             if(params.size() >= 1){
               LinkedList<Assign> args = new LinkedList<Assign>();
               args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.INT));
               builder.buildProcedureCall("IntToReal", args);
               StringEntry entry = procEnvironment.getEntry("IntToReal");
               leftValue = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.REAL);
             } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.INT));
              leftValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, Assign.Type.REAL);
             }
           } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.INT));
              leftValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, Assign.Type.REAL);
           }
        }

        if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToReal") && procEnvironment.entryExists("IntToReal")){
             StringEntryList params = procArgs.getEntry("IntToReal");
             if(params.size() >= 1){
               LinkedList<Assign> args = new LinkedList<Assign>();
               args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(rightValue), Assign.Type.INT));
               builder.buildProcedureCall("IntToReal", args);
               StringEntry entry = procEnvironment.getEntry("IntToReal");
               rightValue = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.REAL);
             } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.INT));
              rightValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, Assign.Type.REAL);
             }
           } else {
            LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
            args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.INT));
            rightValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, Assign.Type.REAL);
           }
        }
      
      switch (binaryOperation.getOperator()){
          case PLUS:
            if(procArgs.entryExists("RAdd") && procEnvironment.entryExists("RAdd")){
              StringEntryList params = procArgs.getEntry("RAdd");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RAdd", args);

                StringEntry returnPlace = procEnvironment.getEntry("RAdd");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function RAdd that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RAdd", args, Assign.Type.REAL);
              }
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RAdd", args, Assign.Type.REAL);
            }
          case MINUS: 
            if(procArgs.entryExists("RSub") && procEnvironment.entryExists("RSub")){
              StringEntryList params = procArgs.getEntry("RSub");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RSub", args);

                StringEntry returnPlace = procEnvironment.getEntry("RSub");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Subtract that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RSub", args, Assign.Type.REAL);
              }
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
              args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RSub", args, Assign.Type.REAL);
            }
          case TIMES: 
            if(procArgs.entryExists("RMul") && procEnvironment.entryExists("RMul")){
              StringEntryList params = procArgs.getEntry("RMul");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RMul", args);

                StringEntry returnPlace = procEnvironment.getEntry("RMul");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Divide that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RMul", args, Assign.Type.REAL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
               args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RMul", args, Assign.Type.REAL);
            }
          case DIVIDE:
            if(procArgs.entryExists("RDivide") && procEnvironment.entryExists("RDivide")){
              StringEntryList params = procArgs.getEntry("RDivide");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RDivide", args);

                StringEntry returnPlace = procEnvironment.getEntry("RDivide");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Divide that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RDivide", args, Assign.Type.REAL);
              }
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
              args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RDivide", args, Assign.Type.REAL);
            }
          case DIV:
            if(procArgs.entryExists("RDiv") && procEnvironment.entryExists("RDiv")){
              StringEntryList params = procArgs.getEntry("RDiv");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RDiv", args);

                StringEntry returnPlace = procEnvironment.getEntry("RDiv");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.INT);
              } else {
                 errorLog.add("Cant find the function Div that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RDiv", args, Assign.Type.INT);
              }
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
              args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RDiv", args, Assign.Type.INT);
            }
          case LE:
            if(procArgs.entryExists("RLessThanOrEqualTo") && procEnvironment.entryExists("RLessThanOrEqualTo")){
              StringEntryList params = procArgs.getEntry("RLessThanOrEqualTo");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RLessThanOrEqualTo", args);

                StringEntry returnPlace = procEnvironment.getEntry("RLessThanOrEqualTo");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RLessThanOrEqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RLessThanOrEqualTo", args, Assign.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
               args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RLessThanOrEqualTo", args, Assign.Type.BOOL);
            }
          case LT:
            if(procArgs.entryExists("RLessThan") && procEnvironment.entryExists("RLessThan")){
              StringEntryList params = procArgs.getEntry("RLessThan");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RLessThan", args);

                StringEntry returnPlace = procEnvironment.getEntry("RLessThan");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RLessThan that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RLessThan", args, Assign.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                return builder.buildExternalFunctionCall(scope, "RLessThan", args, Assign.Type.BOOL);
            }
          case GE:
            if(procArgs.entryExists("RGreaterThanOrEqualTo") && procEnvironment.entryExists("RGreaterThanOrEqualTo")){
              StringEntryList params = procArgs.getEntry("RGreaterThanOrEqualTo");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RGreaterThanOrEqualTo", args);

                StringEntry returnPlace = procEnvironment.getEntry("RGreaterThanOrEqualTo");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RGreaterThanOrEqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RGreaterThanOrEqualTo", args, Assign.Type.BOOL);
              }
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
              args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RGreaterThanOrEqualTo", args, Assign.Type.BOOL);
            }
          case GT:
            if(procArgs.entryExists("RGreaterThan") && procEnvironment.entryExists("RGreaterThan")){
              StringEntryList params = procArgs.getEntry("RGreaterThan");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RGreaterThan", args);

                StringEntry returnPlace = procEnvironment.getEntry("RGreaterThan");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RGreaterThan that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RGreaterThan", args, Assign.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.REAL));
               args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RGreaterThan", args, Assign.Type.BOOL);
            }
          case EQ:
            if(procArgs.entryExists("REqualTo") && procEnvironment.entryExists("REqualTo")){
              StringEntryList params = procArgs.getEntry("REqualTo");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("REqualTo", args);

                StringEntry returnPlace = procEnvironment.getEntry("REqualTo");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function REqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "REqualTo", args, Assign.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
               args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "REqualTo", args, Assign.Type.BOOL);
            }
          case NE: 
            if(procArgs.entryExists("RNotEqualTo") && procEnvironment.entryExists("RNotEqualTo")){
              StringEntryList params = procArgs.getEntry("RNotEqualTo");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.REAL));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.REAL));

                builder.buildProcedureCall("RNotEqualTo", args);

                StringEntry returnPlace = procEnvironment.getEntry("RNotEqualTo");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RNotEqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RNotEqualTo", args, Assign.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.REAL));
                args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.REAL));
                return builder.buildExternalFunctionCall(scope, "RNotEqualTo", args, Assign.Type.BOOL);
            }
          default: return leftValue;
      }
    } else {
      switch (binaryOperation.getOperator()){
        case PLUS: return builder.buildIntegerAdditionAssignment(scope, leftIdent, rightIdent);
        case MINUS: return builder.buildIntegerSubtractionAssignment(scope, leftIdent, rightIdent);
        case TIMES: return builder.buildIntegerMultiplicationAssignment(scope, leftIdent, rightIdent);
        case DIV: 
          if(procArgs.entryExists("Div") && procEnvironment.entryExists("Div")){
              StringEntryList params = procArgs.getEntry("Div");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.INT));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.INT));

                builder.buildProcedureCall("Div", args);

                StringEntry returnPlace = procEnvironment.getEntry("Div");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.INT);
              } else {
                 errorLog.add("Cant find the function Div that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.INT));
                 args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.INT));
                 return builder.buildExternalFunctionCall(scope, "Div", args, Assign.Type.INT);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String, Assign.Type>(leftValue, Assign.Type.INT));
               args.add(new Tuple<String, Assign.Type>(rightValue, Assign.Type.INT));
               return builder.buildExternalFunctionCall(scope, "Div", args, Assign.Type.INT);
            }
        case DIVIDE: 
            if(procArgs.entryExists("Divide") && procEnvironment.entryExists("Divide")){
              StringEntryList params = procArgs.getEntry("Divide");
              if(params.size() >= 2){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(leftValue), Assign.Type.INT));
                args.add(new Assign(Scope.ARGUMENT, params.get(1), new IdentExp(rightValue), Assign.Type.INT));

                builder.buildProcedureCall("Divide", args);

                StringEntry returnPlace = procEnvironment.getEntry("Divide");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Divide that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.INT));
                 args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.INT));
                 return builder.buildExternalFunctionCall(scope, "Divide", args, Assign.Type.REAL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String,Assign.Type>(leftValue, Assign.Type.INT));
               args.add(new Tuple<String,Assign.Type>(rightValue, Assign.Type.INT));
               return builder.buildExternalFunctionCall(scope, "Divide", args, Assign.Type.REAL);
            }
        case MOD: return builder.buildIntegerModuloAssignment(scope, leftValue, rightValue);
        case LE: return builder.buildLessThanOrEqualAssignment(scope, leftIdent, rightIdent);
        case LT: return builder.buildLessThanAssignment(scope, leftIdent, rightIdent);
        case GE: return builder.buildGreaterThanOrEqualToAssignment(scope, leftIdent, rightIdent);
        case GT: return builder.buildGreaterThanAssignment(scope, leftIdent, rightIdent);
        case BAND: return builder.buildIntegerAndAssignment(scope, leftIdent, rightIdent);
        case BOR: return builder.buildIntegerOrAssignment(scope, leftIdent, rightIdent);
        case BXOR: return builder.buildIntegerExclusiveOrAssignment(scope, leftIdent, rightIdent);
        case LSHIFT: return builder.buildLeftShiftAssignment(scope, leftIdent, rightIdent);
        case RSHIFT: return builder.buildRightShiftAssignment(scope, leftIdent, rightIdent);
        case EQ: return builder.buildEqualityAssignment(scope, leftIdent, rightIdent);
        case NE: return builder.buildInequalityAssignment(scope, leftIdent, rightIdent);
        default: return leftValue;
      }
    }
  }

  public String generateFunctionCallIr(Scope scope, FunctionCall funcCall, AssignmentBuilder builder) {
    String funcName = funcCall.getFunctionName().getLexeme();
    List<Expression> valArgs = funcCall.getArguments();
    TypeCheckerQualities returnType = funcCall.acceptResult(typeChecker);
    Assign.Type retType = ConversionUtils.typeCheckerQualitiesToAssignType(returnType);
    if(procArgs.entryExists(funcName)){
      //Build Internal Function Call Sequence
      StringEntryList argsToMap = procArgs.getEntry(funcName);
      List<Assign> valArgResults = new ArrayList<Assign>();

      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
        TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
        Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	      String result = generateExpressionIr(scope, valArg, builder);
	      valArgResults.add(new Assign(Scope.ARGUMENT, argsToMap.get(i), new IdentExp(result), type));
      }
      builder.buildProcedureCall(funcName, valArgResults);
      StringEntry returnPlace = procEnvironment.getEntry(funcName);
      return builder.buildExternalReturnPlacement(returnPlace.toString(), retType);
    } else {
      //Build external function call
      LinkedList<Tuple<String, Assign.Type>> procedureArgs = new LinkedList<Tuple<String, Assign.Type>>();
      for(Expression arg : valArgs){
        String place = generateExpressionIr(scope, arg, builder);
        TypeCheckerQualities qual = arg.acceptResult(typeChecker);
        Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
        procedureArgs.add(new Tuple<String, Assign.Type>(place, type));
      }

      String retPlace = builder.buildExternalFunctionCall(scope, funcName, procedureArgs, retType);
      return retPlace;
    }
  }

  public String generateUnaryOperationIr(Scope scope, UnaryOperation unaryOperation, AssignmentBuilder builder) {
    String value = generateExpressionIr(scope, unaryOperation.getExpression(), builder);
    IdentExp valueIdent = new IdentExp(value);
    TypeCheckerQualities rightType = unaryOperation.getExpression().acceptResult(typeChecker);

    if(unaryOperation.getOperator() == UnaryOperation.OpType.NOT){
      if(rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(procArgs.entryExists("RealToBool") && procEnvironment.entryExists("RealToBool")){
             StringEntryList params = procArgs.getEntry("RealToBool");
             if(params.size() >= 1){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(value), Assign.Type.REAL));
                builder.buildProcedureCall("RealToBool", args);
                StringEntry entry = procEnvironment.getEntry("RealToBool");
                value = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.BOOL);
             } else {
                LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                args.add(new Tuple<String, Assign.Type>(value, Assign.Type.REAL));
                value = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
             }
           } else {
             LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
             args.add(new Tuple<String, Assign.Type>(value, Assign.Type.REAL));
             value = builder.buildExternalFunctionCall(scope, "RealToBool", args, Assign.Type.BOOL);
           }
        } else if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToBool") && procEnvironment.entryExists("IntToBool")){
            StringEntryList params = procArgs.getEntry("IntToBool");
            if(params.size() >= 1){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(value), Assign.Type.INT));
                builder.buildProcedureCall("IntToBool", args);
                StringEntry entry = procEnvironment.getEntry("IntToBool");
                value = builder.buildExternalReturnPlacement(entry.toString(), Assign.Type.BOOL);
            } else {
              LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
              args.add(new Tuple<String,Assign.Type>(value, Assign.Type.INT));
              value = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
            }
          } else {
            LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
            args.add(new Tuple<String,Assign.Type>(value, Assign.Type.INT));
            value = builder.buildExternalFunctionCall(scope, "IntToBool", args, Assign.Type.BOOL);
          }
        }
        Exp expVal = new IdentExp(value);
        return builder.buildNotAssignment(scope, expVal);
    } else if(rightType.containsQualities(TypeCheckerQualities.REAL)){
      switch(unaryOperation.getOperator()){
        case MINUS: 
          if(procArgs.entryExists("RNeg") && procEnvironment.entryExists("RNeg")){
              StringEntryList params = procArgs.getEntry("RNeg");
              if(params.size() >= 1){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(value), Assign.Type.REAL));
                builder.buildProcedureCall("RNeg", args);
                StringEntry returnPlace = procEnvironment.getEntry("RNeg");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.REAL);
              } else {
                 errorLog.add("Cant find the function RNeg that contains one argument", unaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(value, Assign.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RNeg", args, Assign.Type.REAL);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String,Assign.Type>(value, Assign.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RNeg", args, Assign.Type.REAL);
            }
        default:
          errorLog.add("Error unexpected Operation for Real Value " + unaryOperation.getOperator(), unaryOperation.getStart());
          return value;
	    }
    } else {
      switch(unaryOperation.getOperator()){
        case MINUS:
          if(procArgs.entryExists("INeg") && procEnvironment.entryExists("INeg")){
              StringEntryList params = procArgs.getEntry("INeg");
              if(params.size() >= 1){
                LinkedList<Assign> args = new LinkedList<Assign>();
                args.add(new Assign(Scope.ARGUMENT, params.get(0), new IdentExp(value), Assign.Type.INT));
                builder.buildProcedureCall("INeg", args);

                StringEntry returnPlace = procEnvironment.getEntry("INeg");
                return builder.buildExternalReturnPlacement(returnPlace.toString(), Assign.Type.INT);
              } else {
                 errorLog.add("Cant find the function INeg that contains one argument", unaryOperation.getStart());
                 LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
                 args.add(new Tuple<String,Assign.Type>(value, Assign.Type.INT));
                 return builder.buildExternalFunctionCall(scope, "INeg", args, Assign.Type.INT);
              }
            } else {
               LinkedList<Tuple<String, Assign.Type>> args = new LinkedList<Tuple<String, Assign.Type>>();
               args.add(new Tuple<String,Assign.Type>(value, Assign.Type.INT));
               return builder.buildExternalFunctionCall(scope, "INeg", args, Assign.Type.INT);
            }
        case BNOT: return builder.buildIntegerNotAssignment(scope, valueIdent);
        default: return value;
	    }
    }
  }

  public String generateIdentifierIr(Identifier identifier, AssignmentBuilder builder){
    SymbolSectionBuilder symBuilder = builder.getSymbolSectionBuilder();
    TypeCheckerQualities qual = identifier.acceptResult(typeChecker);
    Assign.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    if(varEnvironment.inScope(identifier.getLexeme())){
      StringEntry place = varEnvironment.getEntry(identifier.getLexeme());
      if(place != null)
        return place.toString();
      else{
        errorLog.add("When generating ICode could not find place associated with local identifier " + identifier.getLexeme(), identifier.getStart());
        return "";
      }
    } else if(paramEnvironment.entryExists(identifier.getLexeme())){
        StringEntry place = paramEnvironment.getEntry(identifier.toString());
        String newPlace = builder.buildParamaterAssignment(place.toString(), type);
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
        String place = gen.genNext();
        symBuilder.addSymEntry(SymEntry.EXTERNAL, place, identifier.getLexeme());
        varEnvironment.addEntry(identifier.getLexeme(), new StringEntry(place));
        return place;
      }
    }
  }

  public String generateNumberIr(Scope scope, NumValue numValue, AssignmentBuilder builder){
      String rawnum = Utils.ifHexToInt(numValue.getLexeme());
      return builder.buildNumAssignment(scope, rawnum);
  }

  public String generateBooleanIr(Scope scope, BoolValue boolValue, AssignmentBuilder builder){
      String lexeme = boolValue.getLexeme(); //change to hex if you need to otherwise unchanged
      return builder.buildBoolAssignment(scope, lexeme);
  }

  public String generateStringIr(Scope scope, StrValue strValue, AssignmentBuilder builder){
      return builder.buildStringAssignment(scope, strValue.getLexeme());
  }


  public String generateParamaterDeclarationIr(ParamaterDeclaration parDeclaration, AssignmentBuilder builder) {
    Identifier id = parDeclaration.getIdentifier();
    String alias = gen.genNext();
    varEnvironment.addEntry(id.getLexeme(), new StringEntry(alias));
    return alias;
  }
}
