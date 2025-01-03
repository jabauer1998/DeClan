package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.ICode.Type;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.ast.ASTVisitor;
import io.github.H20man13.DeClan.common.ast.Asm;
import io.github.H20man13.DeClan.common.ast.Assignment;
import io.github.H20man13.DeClan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.common.ast.BoolValue;
import io.github.H20man13.DeClan.common.ast.Branch;
import io.github.H20man13.DeClan.common.ast.ConstDeclaration;
import io.github.H20man13.DeClan.common.ast.Declaration;
import io.github.H20man13.DeClan.common.ast.DeclarationVisitor;
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
import io.github.H20man13.DeClan.common.builder.AssignmentBuilder;
import io.github.H20man13.DeClan.common.builder.DefinitionBuilder;
import io.github.H20man13.DeClan.common.builder.IrBuilderContext;
import io.github.H20man13.DeClan.common.builder.LibraryBuilder;
import io.github.H20man13.DeClan.common.builder.ProgramBuilder;
import io.github.H20man13.DeClan.common.builder.StatementBuilder;
import io.github.H20man13.DeClan.common.builder.SymbolBuilder;
import io.github.H20man13.DeClan.common.builder.SymbolBuilder.SymbolBuilderSearchStrategy;
import io.github.H20man13.DeClan.common.exception.ICodeGeneratorException;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.IdentEntryList;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.StringEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.Utils;

import static io.github.H20man13.DeClan.common.gen.IrRegisterGenerator.*;
import static io.github.H20man13.DeClan.main.MyIO.*;

import java.io.Writer;
import java.lang.String;
import java.lang.module.ModuleDescriptor.Builder;
import java.util.List;
import java.util.function.Function;

import javax.management.RuntimeErrorException;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *The my interpreter class is a visitor object that can interpret the entire DeClan Language
 * It also takes in an error log object in order to record errors
 *@author Jacob Bauer
 */

public class MyICodeGenerator{
  private ErrorLog errorLog;
  private MyTypeChecker typeChecker;
  private MyInterpreter interpreter;
  private IrBuilderContext ctx;
  private IrRegisterGenerator gen;

  public MyICodeGenerator(ErrorLog errorLog) {
    this.errorLog = errorLog;
    this.ctx = new IrBuilderContext();
    this.gen = new IrRegisterGenerator();
    this.typeChecker = new MyTypeChecker(errorLog);
    this.interpreter = new MyInterpreter(errorLog, null, null, null);
  }

  public Lib generateLibraryIr(Library lib){
    LibraryBuilder builder = new LibraryBuilder(ctx, gen);
    typeChecker.addScope();

    builder.buildSymbolSectionHeader();

    builder.buildDataSectionHeader();
    for(ConstDeclaration decl : lib.getConstDecls()){
      decl.accept(typeChecker);
      generateConstantIr(Scope.GLOBAL, decl, builder);
    }

    loadFunctions(lib.getProcDecls(), builder);
    typeChecker.loadFunctions(lib.getProcDecls());

    builder.buildProcedureSectionHeader();
    for(ProcedureDeclaration decl : lib.getProcDecls()){
      decl.accept(typeChecker);
      generateProcedureIr(decl, builder);
      typeChecker.removeVarScope();
    }

    return builder.completeBuild();
  }

  public Prog generateProgramIr(Program program) {
    ProgramBuilder builder = new ProgramBuilder(ctx, gen);
    typeChecker.addScope();

    builder.buildSymbolSectionHeader();

    builder.buildDataSectionHeader();
    for(ConstDeclaration decl : program.getConstDecls()){
      decl.accept(typeChecker);
      generateConstantIr(Scope.GLOBAL, decl, builder);      
    }

    builder.buildBssSectionHeader();
    for (VariableDeclaration decl : program.getVarDecls()) {
      decl.accept(typeChecker);
      generateVariableIr(Scope.GLOBAL, decl, builder);
    }

    loadFunctions(program.getProcDecls(), builder);
    typeChecker.loadFunctions(program.getProcDecls());

    builder.buildCodeSectionHeader();
    for (Statement statement : program.getStatements()) {
      statement.accept(typeChecker);
      generateStatementIr(statement, builder);
    }
    builder.buildCodeSectionEnd();

    builder.buildProcedureSectionHeader();
    for (ProcedureDeclaration decl : program.getProcDecls()){
      decl.accept(typeChecker);
      generateProcedureIr(decl, builder);
      typeChecker.removeVarScope();
    }

    return builder.completeBuild();
  }

  public void generateConstantIr(Scope scope, ConstDeclaration constDecl, DefinitionBuilder builder) {
    Identifier id = constDecl.getIdentifier();
    Expression valueExpr = constDecl.getValue();
    Exp value = generateExpressionIr(valueExpr, builder);
    TypeCheckerQualities qual = valueExpr.acceptResult(typeChecker);
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    IdentExp place = builder.buildDefinition(scope, value, type);
    if(scope == ICode.Scope.GLOBAL)
      builder.addVariableEntry(place.ident, SymEntry.CONST | SymEntry.GLOBAL | SymEntry.INTERNAL, id.getLexeme(), false);
  }
  
  public void generateLocalConstantIr(String funcName, ConstDeclaration constDecl, DefinitionBuilder builder) {
	    Identifier id = constDecl.getIdentifier();
	    Expression valueExpr = constDecl.getValue();
	    Exp value = generateExpressionIr(valueExpr, builder);
	    TypeCheckerQualities qual = valueExpr.acceptResult(typeChecker);
	    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	    IdentExp place = builder.buildDefinition(Scope.LOCAL, value, type);
	    builder.addVariableEntry(place.ident, SymEntry.CONST | SymEntry.INTERNAL | SymEntry.LOCAL, id.getLexeme(), funcName);
  }

  public void generateVariableIr(Scope scope, VariableDeclaration varDecl, DefinitionBuilder builder) {
    Identifier id = varDecl.getIdentifier();
    Identifier type = varDecl.getType();
    IdentExp place = null;
    if(type.getLexeme().equals("STRING")){
      place = builder.buildDefinition(scope, new StrExp("\0"), ICode.Type.STRING);
    } else if(type.getLexeme().equals("REAL")) {
      place = builder.buildDefinition(scope, new RealExp(0), ICode.Type.REAL);
    } else if(type.getLexeme().equals("BOOLEAN")){
      place = builder.buildDefinition(scope, new BoolExp(false), ICode.Type.BOOL);
    } else {
      place = builder.buildDefinition(scope, new IntExp(0), ICode.Type.INT);
    }
    if(scope == ICode.Scope.GLOBAL)
      builder.addVariableEntry(place.ident, SymEntry.INTERNAL | SymEntry.GLOBAL, id.getLexeme(), false);
  }
  
  public void generateLocalVariableIr(String funcName, VariableDeclaration varDecl, DefinitionBuilder builder) {
    Identifier id = varDecl.getIdentifier();
    Identifier type = varDecl.getType();
    IdentExp place = null;
    if(type.getLexeme().equals("STRING")){
      place = builder.buildDefinition(Scope.LOCAL, new StrExp("\0"), ICode.Type.STRING);
    } else if(type.getLexeme().equals("REAL")) {
      place = builder.buildDefinition(Scope.LOCAL, new RealExp(0), ICode.Type.REAL);
    } else if(type.getLexeme().equals("BOOLEAN")){
      place = builder.buildDefinition(Scope.LOCAL, new BoolExp(false), ICode.Type.BOOL);
    } else {
      place = builder.buildDefinition(Scope.LOCAL, new IntExp(0), ICode.Type.INT);
    }
    builder.addVariableEntry(place.ident, SymEntry.INTERNAL | SymEntry.LOCAL, id.getLexeme(), funcName);
  }

  private void loadFunctions(List<ProcedureDeclaration> decls, SymbolBuilder builder){
    for(ProcedureDeclaration decl : decls){
      loadFunction(decl, builder);
    }
  }

  public void loadFunction(ProcedureDeclaration procDecl, SymbolBuilder builder){
    String procedureName = procDecl.getProcedureName().getLexeme();
    List <ParamaterDeclaration> args = procDecl.getArguments();
    
    for(int i = 0; i < args.size(); i++){
	    String argAlias = gen.genNext();
        builder.addVariableEntry(argAlias, SymEntry.PARAM | SymEntry.INTERNAL, args.get(i).getIdentifier().getLexeme(), procedureName, i);
    }
  
    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      String returnPlace = gen.genNext();
      builder.addVariableEntry(returnPlace, SymEntry.RETURN | SymEntry.INTERNAL, procedureName, true);
    }
  } 

  public void generateProcedureIr(ProcedureDeclaration procDecl, StatementBuilder builder){
    String procedureName = procDecl.getProcedureName().getLexeme();
    builder.buildProcedureLabel(procedureName);

    List <Declaration> localVars = procDecl.getLocalVariables();
    for(int i = 0; i < localVars.size(); i++){
      Declaration localDecl = localVars.get(i);
      if(localDecl instanceof VariableDeclaration){
        generateLocalVariableIr(procedureName, (VariableDeclaration)localDecl, builder);
      } else if(localDecl instanceof ConstDeclaration){
        generateLocalConstantIr(procedureName, (ConstDeclaration)localDecl, builder);
      }
    }

    List <Statement> exec = procDecl.getExecutionStatements();
    for(int i = 0; i < exec.size(); i++){
      Statement stat = exec.get(i);
	    generateStatementIr(stat, procedureName, builder);
    }

    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      TypeCheckerQualities qual = retExp.acceptResult(typeChecker); 
      IdentExp retPlace = generateExpressionIr(retExp, procedureName, builder);
      IdentExp returnPlace = builder.getVariablePlace(procedureName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
      builder.buildDefinition(returnPlace.scope, returnPlace.ident, retPlace, ConversionUtils.typeCheckerQualitiesToAssignType(qual));
    }
    builder.buildReturnStatement();
  }

  public void generateStatementIr(Statement stat, StatementBuilder builder){
    if(stat instanceof ProcedureCall) generateProcedureCallIr((ProcedureCall)stat, builder);
    else if(stat instanceof Branch) generateBranchIr((Branch)stat, builder);
    else if(stat instanceof Assignment) generateAssignmentIr((Assignment)stat, builder);
    else if(stat instanceof Asm) generateInlineAssemblyIr((Asm)stat, builder);
    else if(stat instanceof EmptyStatement){
         //Do nothing
    }
    else {
      errorLog.add("Error generating invalid statment type " + stat.getClass().getSimpleName(), stat.getStart());
    }
  }
  
  public void generateStatementIr(Statement stat, String callerFuncName, StatementBuilder builder){
	    if(stat instanceof ProcedureCall) generateProcedureCallIr((ProcedureCall)stat, callerFuncName, builder);
	    else if(stat instanceof Branch) generateBranchIr((Branch)stat, callerFuncName, builder);
	    else if(stat instanceof Assignment) generateAssignmentIr((Assignment)stat, callerFuncName, builder);
	    else if(stat instanceof Asm) generateInlineAssemblyIr((Asm)stat, callerFuncName, builder);
	    else if(stat instanceof EmptyStatement){
	         //Do nothing
	    }
	    else {
	      errorLog.add("Error generating invalid statment type " + stat.getClass().getSimpleName(), stat.getStart());
	    }
	  }
        
  public void generateProcedureCallIr(ProcedureCall procedureCall, StatementBuilder builder) {
    String funcName = procedureCall.getProcedureName().getLexeme();
    List<Expression> valArgs = procedureCall.getArguments();

    if(builder.containsEntry(funcName, 0, SymEntry.PARAM | SymEntry.EXTERNAL)){
      //Generate a standard Procedure Call
      List<Def> valArgResults = new ArrayList<Def>();
      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
        TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
        Exp result = generateExpressionIr(valArg, builder);
        IdentExp argToGet = builder.getVariablePlace(funcName, i, SymEntry.PARAM | SymEntry.EXTERNAL);
        valArgResults.add(new Def(argToGet.scope, argToGet.ident, result, type));
      }
      builder.buildProcedureCall(funcName, valArgResults);
    } else {
      //Generate an External Procedure Call
      LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
      for(Expression valArg : valArgs){
         Exp place = generateExpressionIr(valArg, builder);
         TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
         ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
         args.add(new Tuple<Exp, ICode.Type>(place, type));
      }
      builder.buildExternalProcedureCall(funcName, args);
    }
  }
  
  public void generateProcedureCallIr(ProcedureCall procedureCall, String callerFuncName, StatementBuilder builder) {
	    String funcName = procedureCall.getProcedureName().getLexeme();
	    List<Expression> valArgs = procedureCall.getArguments();

	    if(builder.containsEntry(funcName, 0, SymEntry.PARAM | SymEntry.EXTERNAL)){
	      //Generate a standard Procedure Call
	      List<Def> valArgResults = new ArrayList<Def>();
	      for(int i = 0; i < valArgs.size(); i++){
	        Expression valArg = valArgs.get(i);
	        TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
	        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	        Exp result = generateExpressionIr(valArg, callerFuncName, builder);
	        IdentExp argToGet = builder.getVariablePlace(funcName, i, SymEntry.PARAM | SymEntry.EXTERNAL);
	        valArgResults.add(new Def(argToGet.scope, argToGet.ident, result, type));
	      }
	      builder.buildProcedureCall(funcName, valArgResults);
	    } else {
	      //Generate an External Procedure Call
	      LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
	      for(Expression valArg : valArgs){
	         Exp place = generateExpressionIr(valArg, callerFuncName, builder);
	         TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
	         ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	         args.add(new Tuple<Exp, ICode.Type>(place, type));
	      }
	      builder.buildExternalProcedureCall(funcName, args);
	    }
	  }

  public void generateWhileBranchIr(WhileElifBranch whilebranch, StatementBuilder builder){
    Expression toCheck = whilebranch.getExpression();
    List<Statement> toExec = whilebranch.getExecStatements();
    IdentExp test = generateExpressionIr(toCheck, builder);
    
    builder.buildWhileLoopBeginning(test);

    builder.incrimentWhileLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(toExec.get(i), builder);
    }

    Exp test2 = generateExpressionIr(toCheck, builder);
    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    builder.buildAssignment(test.scope, test.ident, test2, type);

    builder.deIncrimentWhileLoopLevel();

    Branch nextBranch = whilebranch.getNextBranch();
    if(nextBranch != null) {
      builder.buildElseWhileLoopBeginning();
      generateBranchIr(nextBranch, builder);
    } else {
      builder.buildWhileLoopEnd();
    }
  }
  
  public void generateWhileBranchIr(WhileElifBranch whilebranch, String callerFuncName, StatementBuilder builder){
	    Expression toCheck = whilebranch.getExpression();
	    List<Statement> toExec = whilebranch.getExecStatements();
	    IdentExp test = generateExpressionIr(toCheck, callerFuncName, builder);
	    
	    builder.buildWhileLoopBeginning(test);

	    builder.incrimentWhileLoopLevel();
	    for(int i = 0; i < toExec.size(); i++){
	      generateStatementIr(toExec.get(i), callerFuncName, builder);
	    }

	    Exp test2 = generateExpressionIr(toCheck, callerFuncName, builder);
	    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
	    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	    builder.buildAssignment(test.scope, test.ident, test2, type);

	    builder.deIncrimentWhileLoopLevel();

	    Branch nextBranch = whilebranch.getNextBranch();
	    if(nextBranch != null) {
	      builder.buildElseWhileLoopBeginning();
	      generateBranchIr(nextBranch, callerFuncName, builder);
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
  
  public void generateBranchIr(Branch branch, String callerFuncName, StatementBuilder builder){
	    if(branch instanceof IfElifBranch) generateIfBranchIr((IfElifBranch)branch, callerFuncName, builder);
	    else if(branch instanceof ElseBranch) generateElseBranchIr((ElseBranch)branch, callerFuncName, builder);
	    else if(branch instanceof WhileElifBranch) generateWhileBranchIr((WhileElifBranch)branch, builder);
	    else if(branch instanceof RepeatBranch) generateRepeatLoopIr((RepeatBranch)branch, callerFuncName, builder);
	    else if(branch instanceof ForBranch) generateForLoopIr((ForBranch)branch, callerFuncName, builder);
	    else {
	      errorLog.add("Error unexpected branch type " + branch.getClass().getSimpleName(), branch.getStart());
	    }
	  }
  
  public void generateIfBranchIr(IfElifBranch ifbranch, StatementBuilder builder){
    Expression toCheck = ifbranch.getExpression();
    IdentExp test = generateExpressionIr(toCheck, builder);
    builder.buildIfStatementBeginning(test);

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
  
  public void generateIfBranchIr(IfElifBranch ifbranch, String callerFuncName, StatementBuilder builder){
	    Expression toCheck = ifbranch.getExpression();
	    IdentExp test = generateExpressionIr(toCheck, callerFuncName, builder);
	    builder.buildIfStatementBeginning(test);

	    builder.incrimentIfStatementLevel();
	    List<Statement> toExec = ifbranch.getExecStatements();
	    for(int i = 0; i < toExec.size(); i++){
	      generateStatementIr(toExec.get(i), callerFuncName, builder);
	    }
	    builder.deIncrimentIfStatementLevel();

	    if(ifbranch.getNextBranch() != null) {
	      builder.buildElseIfStatementBeginning();
	      generateBranchIr(ifbranch.getNextBranch(), callerFuncName, builder);
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
  
  public void generateElseBranchIr(ElseBranch elsebranch, String callerFuncName, StatementBuilder builder){
	    List<Statement> toExec = elsebranch.getExecStatements();
	    builder.incrimentIfStatementLevel();
	    for(int i = 0; i < toExec.size(); i++){
	      generateStatementIr(toExec.get(i), callerFuncName, builder);
	    }
	    builder.deIncrimentIfStatementLevel();
	    builder.buildIfStatementEnd();
	  }

  public void generateRepeatLoopIr(RepeatBranch repeatbranch, StatementBuilder builder){
    Expression toCheck = repeatbranch.getExpression();
    List<Statement> toExec = repeatbranch.getExecStatements();
    IdentExp test = generateExpressionIr(toCheck, builder);

    builder.buildRepeatLoopBeginning(test);

    builder.incrimentRepeatLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
	    generateStatementIr(toExec.get(i), builder);
    }
    builder.deIncrimentRepeatLoopLevel();

    Exp test2 = generateExpressionIr(toCheck, builder);
    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    builder.buildAssignment(test.scope, test.toString(), test2, type);
    builder.buildRepeatLoopEnd();
  }
  
  public void generateRepeatLoopIr(RepeatBranch repeatbranch, String callerFuncName, StatementBuilder builder){
	    Expression toCheck = repeatbranch.getExpression();
	    List<Statement> toExec = repeatbranch.getExecStatements();
	    IdentExp test = generateExpressionIr(toCheck, callerFuncName, builder);

	    builder.buildRepeatLoopBeginning(test);

	    builder.incrimentRepeatLoopLevel();
	    for(int i = 0; i < toExec.size(); i++){
		    generateStatementIr(toExec.get(i), callerFuncName, builder);
	    }
	    builder.deIncrimentRepeatLoopLevel();

	    Exp test2 = generateExpressionIr(toCheck, callerFuncName, builder);
	    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
	    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	    builder.buildAssignment(test.scope, test.toString(), test2, type);
	    builder.buildRepeatLoopEnd();
	  }
  
  public IdentExp generateInductionVariable(StatementBuilder builder) {
	  return builder.buildDefinition(Scope.LOCAL, new IntExp(0), ICode.Type.INT);
  }

  public void generateForLoopIr(ForBranch forbranch, StatementBuilder builder){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    Assignment initAssign = forbranch.getInitAssignment();
    if(toMod != null){
    	IdentExp curValueInduction = null;
    	if(forbranch.isSimplifiable()) {
    		int numTimes = forbranch.getLoopIterations();
    		curValueInduction = generateInductionVariable(builder);
    		IdentExp target = builder.buildDefinition(Scope.LOCAL, new IntExp(numTimes), ICode.Type.INT);
    		builder.buildInductionBasedForLoopBeginning(curValueInduction, target, builder.getVariablePlace(initAssign.getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilder.SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME), initAssign.getVariableValue().acceptResult(interpreter), toMod.acceptResult(interpreter));
    	} else {
    		generateAssignmentIr(forbranch.getInitAssignment(), builder);
            IdentExp target = generateExpressionIr(forbranch.getTargetExpression(), builder);
            IdentExp curValue = builder.getVariablePlace(forbranch.getInitAssignment().getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
            Object actualIncriment = forbranch.getModifyExpression().acceptResult(interpreter);

            if(actualIncriment instanceof Integer){
              Integer intActualIncriment = ConversionUtils.toInt(actualIncriment);
              if(intActualIncriment < 0){
                builder.buildForLoopBeginning(curValue, BinExp.Operator.GT, target);
              } else if(intActualIncriment > 0){
                builder.buildForLoopBeginning(curValue, BinExp.Operator.LT, target);
              } else {
                builder.buildForLoopBeginning(curValue, BinExp.Operator.NE, target);
              }
            } else if(actualIncriment instanceof Float){
              Float floatActualIncriment = ConversionUtils.toReal(actualIncriment);
              if(floatActualIncriment < 0){
                builder.buildForLoopBeginning(curValue, BinExp.Operator.GT, target);
              } else if(floatActualIncriment > 0){
                builder.buildForLoopBeginning(curValue, BinExp.Operator.LT, target);
              } else {
                builder.buildForLoopBeginning(curValue, BinExp.Operator.NE, target);
              }
            } else {
              builder.buildForLoopBeginning(curValue, BinExp.Operator.NE, target);
            }
    	}
        
        builder.incrimentForLoopLevel();
        for(int i = 0; i < toExec.size(); i++){
            generateStatementIr(toExec.get(i), builder);
        }
        builder.deIncrimentForLoopLevel();

        if(forbranch.isSimplifiable()){
        	IdentExp incr = builder.buildDefinition(ICode.Scope.LOCAL, new IntExp(1), ICode.Type.INT);
        	builder.buildAssignment(curValueInduction.scope, curValueInduction.ident, new BinExp(curValueInduction, BinExp.Operator.IADD, incr), ICode.Type.INT);
        }
    	IdentExp curValue = builder.getVariablePlace(forbranch.getInitAssignment().getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
    	IdentExp incriment = generateExpressionIr(toMod, builder);
        TypeCheckerQualities qual = toMod.acceptResult(typeChecker);
        if(qual.containsQualities(TypeCheckerQualities.REAL)){
        	IdentExp result = builder.buildRealAddition(ICode.Scope.LOCAL, curValue, incriment);
            builder.buildAssignment(curValue.scope, curValue.ident, result, ICode.Type.REAL);
        } else if(qual.containsQualities(TypeCheckerQualities.INTEGER)) {
        	BinExp exp = new BinExp(curValue, BinExp.Operator.IADD, incriment);
        	builder.buildAssignment(curValue.scope, curValue.ident, exp, ICode.Type.INT);
        }
        builder.buildForLoopEnd();
    } else {
      generateAssignmentIr(forbranch.getInitAssignment(), builder);
      IdentExp target = generateExpressionIr(forbranch.getTargetExpression(), builder);
      IdentExp curvalue = builder.getVariablePlace(forbranch.getInitAssignment().getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
      builder.buildForLoopBeginning(curvalue, BinExp.Operator.NE, target);
      builder.incrimentForLoopLevel();
      for(int i = 0; i < toExec.size(); i++){
          generateStatementIr(toExec.get(i), builder);
      }
      builder.deIncrimentForLoopLevel();
      builder.buildForLoopEnd();
    }
  }
  
  public void generateForLoopIr(ForBranch forbranch, String callerFuncName, StatementBuilder builder){
	    Expression toMod = forbranch.getModifyExpression();
	    List<Statement> toExec = forbranch.getExecStatements();
	    Assignment initAssign = forbranch.getInitAssignment();
	    if(toMod != null){
	    	IdentExp curValueInduction = null;
	    	if(forbranch.isSimplifiable()) {
	    		int numTimes = forbranch.getLoopIterations();
	    		curValueInduction = generateInductionVariable(builder);
	    		IdentExp target = builder.buildDefinition(Scope.LOCAL, new IntExp(numTimes), ICode.Type.INT);
	    		builder.buildInductionBasedForLoopBeginning(curValueInduction, target, builder.getVariablePlace(initAssign.getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilder.SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME), initAssign.getVariableValue().acceptResult(interpreter), toMod.acceptResult(interpreter));
	    	} else {
	    		generateAssignmentIr(forbranch.getInitAssignment(), callerFuncName, builder);
	            IdentExp target = generateExpressionIr(forbranch.getTargetExpression(), callerFuncName, builder);
	            IdentExp curValue = builder.getVariablePlace(forbranch.getInitAssignment().getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
	            Object actualIncriment = forbranch.getModifyExpression().acceptResult(interpreter);

	            if(actualIncriment instanceof Integer){
	              Integer intActualIncriment = ConversionUtils.toInt(actualIncriment);
	              if(intActualIncriment < 0){
	                builder.buildForLoopBeginning(curValue, BinExp.Operator.GT, target);
	              } else if(intActualIncriment > 0){
	                builder.buildForLoopBeginning(curValue, BinExp.Operator.LT, target);
	              } else {
	                builder.buildForLoopBeginning(curValue, BinExp.Operator.NE, target);
	              }
	            } else if(actualIncriment instanceof Float){
	              Float floatActualIncriment = ConversionUtils.toReal(actualIncriment);
	              if(floatActualIncriment < 0){
	                builder.buildForLoopBeginning(curValue, BinExp.Operator.GT, target);
	              } else if(floatActualIncriment > 0){
	                builder.buildForLoopBeginning(curValue, BinExp.Operator.LT, target);
	              } else {
	                builder.buildForLoopBeginning(curValue, BinExp.Operator.NE, target);
	              }
	            } else {
	              builder.buildForLoopBeginning(curValue, BinExp.Operator.NE, target);
	            }
	    	}
	        
	        builder.incrimentForLoopLevel();
	        for(int i = 0; i < toExec.size(); i++){
	            generateStatementIr(toExec.get(i), callerFuncName, builder);
	        }
	        builder.deIncrimentForLoopLevel();

	        if(forbranch.isSimplifiable()){
	        	IdentExp incr = builder.buildDefinition(ICode.Scope.LOCAL, new IntExp(1), ICode.Type.INT);
	        	builder.buildAssignment(curValueInduction.scope, curValueInduction.ident, new BinExp(curValueInduction, BinExp.Operator.IADD, incr), ICode.Type.INT);
	        }
	    	IdentExp curValue = builder.getVariablePlace(forbranch.getInitAssignment().getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
	    	IdentExp incriment = generateExpressionIr(toMod, builder);
	        TypeCheckerQualities qual = toMod.acceptResult(typeChecker);
	        if(qual.containsQualities(TypeCheckerQualities.REAL)){
	        	IdentExp result = builder.buildRealAddition(ICode.Scope.LOCAL, curValue, incriment);
	            builder.buildAssignment(curValue.scope, curValue.ident, result, ICode.Type.REAL);
	        } else if(qual.containsQualities(TypeCheckerQualities.INTEGER)) {
	        	BinExp exp = new BinExp(curValue, BinExp.Operator.IADD, incriment);
	        	builder.buildAssignment(curValue.scope, curValue.ident, exp, ICode.Type.INT);
	        }
	        builder.buildForLoopEnd();
	    } else {
	      generateAssignmentIr(forbranch.getInitAssignment(), callerFuncName, builder);
	      IdentExp target = generateExpressionIr(forbranch.getTargetExpression(), callerFuncName, builder);
	      IdentExp curvalue = builder.getVariablePlace(forbranch.getInitAssignment().getVariableName().getLexeme(), SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
	      builder.buildForLoopBeginning(curvalue, BinExp.Operator.NE, target);
	      builder.incrimentForLoopLevel();
	      for(int i = 0; i < toExec.size(); i++){
	          generateStatementIr(toExec.get(i), builder);
	      }
	      builder.deIncrimentForLoopLevel();
	      builder.buildForLoopEnd();
	    }
	  }
        
  public void generateAssignmentIr(Assignment assignment, String callerFuncName, AssignmentBuilder builder) {
	IdentExp place = null;
	String myStr = assignment.getVariableName().getLexeme();
	if(builder.containsEntry(myStr, callerFuncName, SymEntry.INTERNAL)) {
		place = builder.getVariablePlace(myStr, callerFuncName, SymEntry.INTERNAL);
	} else if(builder.containsEntry(myStr, SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)) {
		place = builder.getVariablePlace(myStr, SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
	} else {
		throw new ICodeGeneratorException(assignment, "Error no variable was found with attributes selected");
	}
    Expression exp = assignment.getVariableValue();
    IdentExp value = generateExpressionIr(exp, callerFuncName, builder);
    TypeCheckerQualities qual = exp.acceptResult(typeChecker);
    TypeCheckerQualities convType = assignment.getVariableName().acceptResult(typeChecker);
    
    if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.REAL)){
      value = builder.buildRealToIntConversion(ICode.Scope.LOCAL, value);
    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
      value = builder.buildIntToRealConversion(ICode.Scope.LOCAL, value);
    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
      value = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, value);
    } else if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
      value = builder.buildBoolToIntConversion(ICode.Scope.LOCAL, value);
    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.REAL)){
      value = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, value);
    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
      value = builder.buildBoolToRealConversion(ICode.Scope.LOCAL, value);
    }
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(convType);
    builder.buildAssignment(place.scope, place.ident, value, type);
  }

  public void generateAssignmentIr(Assignment assignment, AssignmentBuilder builder) {
		IdentExp place = null;
		String myStr = assignment.getVariableName().getLexeme();
		if(builder.containsEntry(myStr, SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)) {
			place = builder.getVariablePlace(myStr, SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
		} else {
			throw new ICodeGeneratorException(assignment, "Error no variable was found with attributes selected");
		}
	    Expression exp = assignment.getVariableValue();
	    IdentExp value = generateExpressionIr(exp, builder);
	    TypeCheckerQualities qual = exp.acceptResult(typeChecker);
	    TypeCheckerQualities convType = assignment.getVariableName().acceptResult(typeChecker);
	    
	    if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.REAL)){
	      value = builder.buildRealToIntConversion(ICode.Scope.LOCAL, value);
	    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
	      value = builder.buildIntToRealConversion(ICode.Scope.LOCAL, value);
	    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
	      value = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, value);
	    } else if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
	      value = builder.buildBoolToIntConversion(ICode.Scope.LOCAL, value);
	    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.REAL)){
	      value = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, value);
	    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
	      value = builder.buildBoolToRealConversion(ICode.Scope.LOCAL, value);
	    }
	    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(convType);
	    builder.buildAssignment(place.scope, place.ident, value, type);
	  }
  
  public void generateInlineAssemblyIr(Asm asm, StatementBuilder builder) {
     List<IdentExp> icodeParams = new LinkedList<IdentExp>();
     for(String param : asm.getParamaters()){
       if(builder.containsEntry(param, SymEntry.ANY, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)){
          IdentExp icodeParam = builder.getVariablePlace(param, SymEntry.INTERNAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
          icodeParams.add(icodeParam);
       } else {
          throw new ICodeGeneratorException(asm, "Input paramater " + param + " does not exist"); 
       }
     }
     builder.buildInlineAssembly(asm.getInlineAssembly(), icodeParams);
  }
  
  public void generateInlineAssemblyIr(Asm asm, String callerFuncName, StatementBuilder builder) {
	     List<IdentExp> icodeParams = new LinkedList<IdentExp>();
	     for(String param : asm.getParamaters()){
	       if(builder.containsEntry(param, callerFuncName, SymEntry.INTERNAL)) {
	          IdentExp icodeParam = builder.getVariablePlace(param, callerFuncName, SymEntry.INTERNAL);
	          icodeParams.add(icodeParam);
	       } else if(builder.containsEntry(param, SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)){
	    	   IdentExp icodeParam = builder.getVariablePlace(param, SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
		       icodeParams.add(icodeParam);
	       } else {
	          throw new ICodeGeneratorException(asm, "Input paramater " + param + " does not exist"); 
	       }
	     }
	     builder.buildInlineAssembly(asm.getInlineAssembly(), icodeParams);
	  }

  
  public IdentExp generateExpressionIr(Expression exp, DefinitionBuilder builder){
    if(exp instanceof BinaryOperation) return generateBinaryOperationIr((BinaryOperation)exp, builder);
    else if(exp instanceof FunctionCall) return generateFunctionCallIr((FunctionCall)exp, builder);
    else if(exp instanceof UnaryOperation) return generateUnaryOperationIr((UnaryOperation)exp, builder);
    else if(exp instanceof Identifier) return generateIdentifierIr((Identifier)exp, builder);
    else if(exp instanceof NumValue) return generateNumberIr((NumValue)exp, builder);
    else if(exp instanceof BoolValue) return generateBooleanIr((BoolValue)exp, builder);
    else if(exp instanceof StrValue) return generateStringIr((StrValue)exp, builder);
    else {
      throw new RuntimeException("Error Invalid Expression Type found when generating Ir at" + exp.getStart().toString());
    }
  }
  
  public IdentExp generateExpressionIr(Expression exp, String callerFuncName, DefinitionBuilder builder){
	    if(exp instanceof BinaryOperation) return generateBinaryOperationIr((BinaryOperation)exp, callerFuncName, builder);
	    else if(exp instanceof FunctionCall) return generateFunctionCallIr((FunctionCall)exp, callerFuncName, builder);
	    else if(exp instanceof UnaryOperation) return generateUnaryOperationIr((UnaryOperation)exp, callerFuncName, builder);
	    else if(exp instanceof Identifier) return generateIdentifierIr((Identifier)exp, callerFuncName, builder);
	    else if(exp instanceof NumValue) return generateNumberIr((NumValue)exp, builder);
	    else if(exp instanceof BoolValue) return generateBooleanIr((BoolValue)exp, builder);
	    else if(exp instanceof StrValue) return generateStringIr((StrValue)exp, builder);
	    else {
	      throw new RuntimeException("Error Invalid Expression Type found when generating Ir at" + exp.getStart().toString());
	    }
	  }
  
  
  public IdentExp generateBinaryOperationIr(BinaryOperation binaryOperation, DefinitionBuilder builder) {
      IdentExp leftValue = generateExpressionIr(binaryOperation.getLeft(), builder);
      TypeCheckerQualities leftType = binaryOperation.getLeft().acceptResult(typeChecker);
      
      IdentExp rightValue = generateExpressionIr(binaryOperation.getRight(), builder);
      TypeCheckerQualities rightType = binaryOperation.getRight().acceptResult(typeChecker);

      if(binaryOperation.getOperator() == BinaryOperation.OpType.AND || binaryOperation.getOperator() == BinaryOperation.OpType.OR){
        if(leftType.containsQualities(TypeCheckerQualities.REAL)){
           leftValue = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, leftValue);
        } else if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
           leftValue = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, leftValue);
        }
        
        if(rightType.containsQualities(TypeCheckerQualities.REAL)){
            rightValue = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, rightValue);
        } else if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
            rightValue = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, rightValue);
        }

        switch(binaryOperation.getOperator()){
          case AND: return builder.buildBooleanAndDefinition(Scope.LOCAL, leftValue, rightValue);
          case OR: return builder.buildBooleanOrDefinition(Scope.LOCAL, leftValue, rightValue);
          default: return leftValue;
        }
      } else if(leftType.containsQualities(TypeCheckerQualities.REAL) || rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
        	  leftValue = builder.buildIntToRealConversion(ICode.Scope.LOCAL, leftValue);
          }

          if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
        	  rightValue = builder.buildIntToRealConversion(ICode.Scope.LOCAL, rightValue);
          }
      
      switch (binaryOperation.getOperator()){
          case PLUS: return builder.buildRealAddition(ICode.Scope.LOCAL, leftValue, rightValue);
          case MINUS: return builder.buildRealSubtraction(ICode.Scope.LOCAL, leftValue, rightValue);
          case TIMES: return builder.buildRealMultiplication(ICode.Scope.LOCAL, leftValue, rightValue);
          case DIVIDE: return builder.buildRealDivision(ICode.Scope.LOCAL, leftValue, rightValue);
          case DIV: return builder.buildRealDiv(ICode.Scope.LOCAL, leftValue, rightValue);
          case LE: return builder.buildRealLessThanOrEqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          case LT: return builder.buildRealLessThan(ICode.Scope.LOCAL, leftValue, rightValue);
          case GE: return builder.buildRealGreaterThenOrEqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          case GT: return builder.buildRealGreaterThan(ICode.Scope.LOCAL, leftValue, rightValue);
          case EQ: return builder.buildREqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          case NE: return builder.buildRNotEqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          default: return leftValue;
      }
    } else {
      switch (binaryOperation.getOperator()){
        case PLUS: return builder.buildIntegerAdditionDefinition(Scope.LOCAL, leftValue, rightValue);
        case MINUS: return builder.buildIntegerSubtractionDefinition(Scope.LOCAL, leftValue, rightValue);
        case TIMES: return builder.buildIntegerMultiplicationDefinition(Scope.LOCAL, leftValue, rightValue);
        case DIV: return builder.buildIntegerDiv(ICode.Scope.LOCAL, leftValue, rightValue);
        case DIVIDE: return builder.buildIntegerDivide(ICode.Scope.LOCAL, leftValue, rightValue);
        case MOD: return builder.buildIntegerModulo(Scope.LOCAL, leftValue, rightValue);
        case LE: return builder.buildIntegerLessThenOrEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        case LT: return builder.buildIntegerLessThenDefinition(Scope.LOCAL, leftValue, rightValue);
        case GE: return builder.buildIntegerGreaterThenOrEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        case GT: return builder.buildIntegerGreaterThenDefinition(Scope.LOCAL, leftValue, rightValue);
        case BAND: return builder.buildIntegerBitwiseAndDefinition(Scope.LOCAL, leftValue, rightValue);
        case BOR: return builder.buildIntegerBitwiseOrDefinition(Scope.LOCAL, leftValue, rightValue);
        case BXOR: return builder.buildIntegerBitwiseXorDefinition(Scope.LOCAL, leftValue, rightValue);
        case LSHIFT: return builder.buildIntegerLeftShiftDefinition(Scope.LOCAL, leftValue, rightValue);
        case RSHIFT: return builder.buildIntegerRightShiftDefinition(Scope.LOCAL, leftValue, rightValue);
        case EQ: return builder.buildIntegerEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        case NE: return builder.buildIntegerNotEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        default: return leftValue;
      }
    }
  }
  
  public IdentExp generateBinaryOperationIr(BinaryOperation binaryOperation, String callerFuncName, DefinitionBuilder builder) {
      IdentExp leftValue = generateExpressionIr(binaryOperation.getLeft(), callerFuncName, builder);
      TypeCheckerQualities leftType = binaryOperation.getLeft().acceptResult(typeChecker);
      
      IdentExp rightValue = generateExpressionIr(binaryOperation.getRight(), callerFuncName, builder);
      TypeCheckerQualities rightType = binaryOperation.getRight().acceptResult(typeChecker);

      if(binaryOperation.getOperator() == BinaryOperation.OpType.AND || binaryOperation.getOperator() == BinaryOperation.OpType.OR){
        if(leftType.containsQualities(TypeCheckerQualities.REAL)){
           leftValue = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, leftValue);
        } else if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
           leftValue = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, leftValue);
        }
        
        if(rightType.containsQualities(TypeCheckerQualities.REAL)){
            rightValue = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, rightValue);
        } else if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
            rightValue = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, rightValue);
        }

        switch(binaryOperation.getOperator()){
          case AND: return builder.buildBooleanAndDefinition(Scope.LOCAL, leftValue, rightValue);
          case OR: return builder.buildBooleanOrDefinition(Scope.LOCAL, leftValue, rightValue);
          default: return leftValue;
        }
      } else if(leftType.containsQualities(TypeCheckerQualities.REAL) || rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
        	  leftValue = builder.buildIntToRealConversion(ICode.Scope.LOCAL, leftValue);
          }

          if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
        	  rightValue = builder.buildIntToRealConversion(ICode.Scope.LOCAL, rightValue);
          }
      
      switch (binaryOperation.getOperator()){
          case PLUS: return builder.buildRealAddition(ICode.Scope.LOCAL, leftValue, rightValue);
          case MINUS: return builder.buildRealSubtraction(ICode.Scope.LOCAL, leftValue, rightValue);
          case TIMES: return builder.buildRealMultiplication(ICode.Scope.LOCAL, leftValue, rightValue);
          case DIVIDE: return builder.buildRealDivision(ICode.Scope.LOCAL, leftValue, rightValue);
          case DIV: return builder.buildRealDiv(ICode.Scope.LOCAL, leftValue, rightValue);
          case LE: return builder.buildRealLessThanOrEqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          case LT: return builder.buildRealLessThan(ICode.Scope.LOCAL, leftValue, rightValue);
          case GE: return builder.buildRealGreaterThenOrEqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          case GT: return builder.buildRealGreaterThan(ICode.Scope.LOCAL, leftValue, rightValue);
          case EQ: return builder.buildREqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          case NE: return builder.buildRNotEqualTo(ICode.Scope.LOCAL, leftValue, rightValue);
          default: return leftValue;
      }
    } else {
      switch (binaryOperation.getOperator()){
        case PLUS: return builder.buildIntegerAdditionDefinition(Scope.LOCAL, leftValue, rightValue);
        case MINUS: return builder.buildIntegerSubtractionDefinition(Scope.LOCAL, leftValue, rightValue);
        case TIMES: return builder.buildIntegerMultiplicationDefinition(Scope.LOCAL, leftValue, rightValue);
        case DIV: return builder.buildIntegerDiv(ICode.Scope.LOCAL, leftValue, rightValue);
        case DIVIDE: return builder.buildIntegerDivide(ICode.Scope.LOCAL, leftValue, rightValue);
        case MOD: return builder.buildIntegerModulo(Scope.LOCAL, leftValue, rightValue);
        case LE: return builder.buildIntegerLessThenOrEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        case LT: return builder.buildIntegerLessThenDefinition(Scope.LOCAL, leftValue, rightValue);
        case GE: return builder.buildIntegerGreaterThenOrEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        case GT: return builder.buildIntegerGreaterThenDefinition(Scope.LOCAL, leftValue, rightValue);
        case BAND: return builder.buildIntegerBitwiseAndDefinition(Scope.LOCAL, leftValue, rightValue);
        case BOR: return builder.buildIntegerBitwiseOrDefinition(Scope.LOCAL, leftValue, rightValue);
        case BXOR: return builder.buildIntegerBitwiseXorDefinition(Scope.LOCAL, leftValue, rightValue);
        case LSHIFT: return builder.buildIntegerLeftShiftDefinition(Scope.LOCAL, leftValue, rightValue);
        case RSHIFT: return builder.buildIntegerRightShiftDefinition(Scope.LOCAL, leftValue, rightValue);
        case EQ: return builder.buildIntegerEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        case NE: return builder.buildIntegerNotEqualToDefinition(Scope.LOCAL, leftValue, rightValue);
        default: return leftValue;
      }
    }
  }

  public IdentExp generateFunctionCallIr(FunctionCall funcCall, DefinitionBuilder builder) {
	String funcName = funcCall.getFunctionName().getLexeme();
	List<Expression> valArgs = funcCall.getArguments();
	TypeCheckerQualities returnType = funcCall.acceptResult(typeChecker);
	ICode.Type retType = ConversionUtils.typeCheckerQualitiesToAssignType(returnType);
      //Build Internal Function Call Sequence

	if(builder.containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
		LinkedList<Def> definitions = new LinkedList<Def>();
		for(int i = 0; i < valArgs.size(); i++){
		    Expression valArg = valArgs.get(i);
		    TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
		    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
		    IdentExp result = generateExpressionIr(valArg, builder);
		    IdentExp arg = builder.getVariablePlace(funcName, i, SymEntry.INTERNAL | SymEntry.PARAM);
		    definitions.add(new Def(arg.scope, arg.ident, result, type));
		}
		
		IdentExp returnPlace = builder.getVariablePlace(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
		
		return builder.buildFunctionCall(funcName, definitions, returnPlace, retType);
	} else if(builder.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
		LinkedList<Def> definitions = new LinkedList<Def>();
		for(int i = 0; i < valArgs.size(); i++){
		    Expression valArg = valArgs.get(i);
		    TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
		    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
		    IdentExp result = generateExpressionIr(valArg, builder);
		    IdentExp arg = builder.getVariablePlace(funcName, i, SymEntry.EXTERNAL | SymEntry.PARAM);
		    definitions.add(new Def(arg.scope, arg.ident, result, type));
		}
		
		IdentExp returnPlace = builder.getVariablePlace(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
		
		return builder.buildFunctionCall(funcName, definitions, returnPlace, retType);
	} else {
	 //Build external function call
      LinkedList<Tuple<Exp, ICode.Type>> procedureArgs = new LinkedList<Tuple<Exp, ICode.Type>>();
      for(Expression arg : valArgs){
        Exp place = generateExpressionIr(arg, builder);
        TypeCheckerQualities qual = arg.acceptResult(typeChecker);
        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
        procedureArgs.add(new Tuple<Exp, ICode.Type>(place, type));
      }

      return builder.buildExternalFunctionCall(Scope.LOCAL, funcName, procedureArgs, retType);
	}
	
  }
  
  public IdentExp generateFunctionCallIr(FunctionCall funcCall, String callerFuncName, DefinitionBuilder builder) {
		String funcName = funcCall.getFunctionName().getLexeme();
		List<Expression> valArgs = funcCall.getArguments();
		TypeCheckerQualities returnType = funcCall.acceptResult(typeChecker);
		ICode.Type retType = ConversionUtils.typeCheckerQualitiesToAssignType(returnType);
	      //Build Internal Function Call Sequence

		if(builder.containsEntry(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
			LinkedList<Def> definitions = new LinkedList<Def>();
			for(int i = 0; i < valArgs.size(); i++){
			    Expression valArg = valArgs.get(i);
			    TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
			    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
			    IdentExp result = generateExpressionIr(valArg, callerFuncName, builder);
			    IdentExp arg = builder.getVariablePlace(funcName, i, SymEntry.INTERNAL | SymEntry.PARAM);
			    definitions.add(new Def(arg.scope, arg.ident, result, type));
			}
			
			IdentExp returnPlace = builder.getVariablePlace(funcName, SymEntry.INTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
			
			return builder.buildFunctionCall(funcName, definitions, returnPlace, retType);
		} else if(builder.containsEntry(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME)) {
			LinkedList<Def> definitions = new LinkedList<Def>();
			for(int i = 0; i < valArgs.size(); i++){
			    Expression valArg = valArgs.get(i);
			    TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
			    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
			    IdentExp result = generateExpressionIr(valArg, builder);
			    IdentExp arg = builder.getVariablePlace(funcName, i, SymEntry.EXTERNAL | SymEntry.PARAM);
			    definitions.add(new Def(arg.scope, arg.ident, result, type));
			}
			
			IdentExp returnPlace = builder.getVariablePlace(funcName, SymEntry.EXTERNAL | SymEntry.RETURN, SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME);
			
			return builder.buildFunctionCall(funcName, definitions, returnPlace, retType);
		} else {
		 //Build external function call
	      LinkedList<Tuple<Exp, ICode.Type>> procedureArgs = new LinkedList<Tuple<Exp, ICode.Type>>();
	      for(Expression arg : valArgs){
	        Exp place = generateExpressionIr(arg, builder);
	        TypeCheckerQualities qual = arg.acceptResult(typeChecker);
	        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	        procedureArgs.add(new Tuple<Exp, ICode.Type>(place, type));
	      }

	      return builder.buildExternalFunctionCall(Scope.LOCAL, funcName, procedureArgs, retType);
		}
		
	  }

  public IdentExp generateUnaryOperationIr(UnaryOperation unaryOperation, DefinitionBuilder builder) {
    IdentExp value = generateExpressionIr(unaryOperation.getExpression(), builder);
    TypeCheckerQualities rightType = unaryOperation.getExpression().acceptResult(typeChecker);

    if(unaryOperation.getOperator() == UnaryOperation.OpType.NOT){
      if(rightType.containsQualities(TypeCheckerQualities.REAL)){
          value = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, value);
        } else if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          value = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, value);
        }
        return builder.buildBooleanNotDefinition(ICode.Scope.LOCAL, value);
    } else if(rightType.containsQualities(TypeCheckerQualities.REAL)){
      switch(unaryOperation.getOperator()){
        case MINUS: return builder.buildRealNegationDefinition(ICode.Scope.LOCAL, value);
        default:
          errorLog.add("Error unexpected Operation for Real Value " + unaryOperation.getOperator(), unaryOperation.getStart());
          return value;
	    }
    } else {
      switch(unaryOperation.getOperator()){
        case MINUS: return builder.buildIntegerNegationDefinition(ICode.Scope.LOCAL, value);
        case BNOT: return builder.buildIntegerBitwiseNegationDefinition(Scope.LOCAL, value);
        default: return value;
	    }
    }
  }
  
  public IdentExp generateUnaryOperationIr(UnaryOperation unaryOperation, String funcName, DefinitionBuilder builder) {
	    IdentExp value = generateExpressionIr(unaryOperation.getExpression(), funcName, builder);
	    TypeCheckerQualities rightType = unaryOperation.getExpression().acceptResult(typeChecker);

	    if(unaryOperation.getOperator() == UnaryOperation.OpType.NOT){
	      if(rightType.containsQualities(TypeCheckerQualities.REAL)){
	          value = builder.buildRealToBoolConversion(ICode.Scope.LOCAL, value);
	        } else if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
	          value = builder.buildIntToBoolConversion(ICode.Scope.LOCAL, value);
	        }
	        return builder.buildBooleanNotDefinition(ICode.Scope.LOCAL, value);
	    } else if(rightType.containsQualities(TypeCheckerQualities.REAL)){
	      switch(unaryOperation.getOperator()){
	        case MINUS: return builder.buildRealNegationDefinition(ICode.Scope.LOCAL, value);
	        default:
	          errorLog.add("Error unexpected Operation for Real Value " + unaryOperation.getOperator(), unaryOperation.getStart());
	          return value;
		    }
	    } else {
	      switch(unaryOperation.getOperator()){
	        case MINUS: return builder.buildIntegerNegationDefinition(ICode.Scope.LOCAL, value);
	        case BNOT: return builder.buildIntegerBitwiseNegationDefinition(Scope.LOCAL, value);
	        default: return value;
		    }
	    }
	  }

  public IdentExp generateIdentifierIr(Identifier identifier, String funcName, DefinitionBuilder builder){
    if(builder.containsEntry(identifier.getLexeme(), funcName, SymEntry.INTERNAL)){
      return builder.getVariablePlace(identifier.getLexeme(), funcName, SymEntry.INTERNAL);
    } else if(builder.containsEntry(identifier.getLexeme(), SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)){
      return builder.getVariablePlace(identifier.getLexeme(), SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
    } else {
        String place = gen.genNext();
        builder.addVariableEntry(place, SymEntry.GLOBAL | SymEntry.EXTERNAL, identifier.getLexeme(), false);
        IdentExp ident = new IdentExp(Scope.GLOBAL, place);
        return ident;
    }
  }
  
  public IdentExp generateIdentifierIr(Identifier identifier, DefinitionBuilder builder){
    if(builder.containsEntry(identifier.getLexeme(), SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)){
      return builder.getVariablePlace(identifier.getLexeme(), SymEntry.INTERNAL | SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
    } else if(builder.containsEntry(identifier.getLexeme(), SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME)){
      return builder.getVariablePlace(identifier.getLexeme(), SymEntry.EXTERNAL | SymEntry.GLOBAL, SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME);
    } else {
        String place = gen.genNext();
        builder.addVariableEntry(place, SymEntry.GLOBAL | SymEntry.EXTERNAL, identifier.getLexeme(), false);
        IdentExp ident = new IdentExp(Scope.GLOBAL, place);
        return ident;
    }
  }

  public IdentExp generateNumberIr(NumValue numValue, DefinitionBuilder builder){
      String rawnum = Utils.ifHexToInt(numValue.getLexeme());
      if(rawnum.contains(".")){
         return builder.buildDefinition(Scope.LOCAL, new RealExp(Float.parseFloat(rawnum)), ICode.Type.REAL);
      } else {
        try{
          return builder.buildDefinition(Scope.LOCAL, new IntExp(Integer.parseInt(rawnum)), ICode.Type.INT);
        } catch(NumberFormatException e){
          return builder.buildDefinition(Scope.LOCAL, new IntExp(Integer.parseUnsignedInt(rawnum)), ICode.Type.INT);
        }
      }
  }

  public IdentExp generateBooleanIr(BoolValue boolValue, DefinitionBuilder builder){
      String lexeme = boolValue.getLexeme(); //change to hex if you need to otherwise unchanged
      return builder.buildDefinition(Scope.LOCAL, new BoolExp(Boolean.parseBoolean(lexeme)), ICode.Type.BOOL);
  }

  public IdentExp generateStringIr(StrValue strValue, DefinitionBuilder builder){
      return builder.buildDefinition(Scope.LOCAL, new StrExp(strValue.getLexeme()), ICode.Type.STRING);
  }
}
