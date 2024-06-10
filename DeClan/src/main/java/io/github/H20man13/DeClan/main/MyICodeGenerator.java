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
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.builder.AssignmentBuilder;
import io.github.H20man13.DeClan.common.builder.DefinitionBuilder;
import io.github.H20man13.DeClan.common.builder.IrBuilderContext;
import io.github.H20man13.DeClan.common.builder.LibraryBuilder;
import io.github.H20man13.DeClan.common.builder.ProgramBuilder;
import io.github.H20man13.DeClan.common.builder.StatementBuilder;
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

import java.util.List;
import java.util.function.Function;

import javax.management.RuntimeErrorException;

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
  private Environment<String, IdentExp> varEnvironment;
  private Environment<String, IdentExp> paramEnvironment;
  private Environment<String, IdentExp> procEnvironment;
  private Environment<String, IdentEntryList> procArgs;
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
    LibraryBuilder builder = new LibraryBuilder(ctx, gen);
    procEnvironment.addScope();
    varEnvironment.addScope();
    procArgs.addScope();
    typeChecker.addScope();

    builder.buildSymbolSectionHeader();

    builder.buildDataSectionHeader();
    for(ConstDeclaration decl : lib.getConstDecls()){
      decl.accept(typeChecker);
      generateConstantIr(Scope.GLOBAL, decl, builder);
    }

    builder.buildBssSectionHeader();
    for(VariableDeclaration decl : lib.getVarDecls()){
      decl.accept(typeChecker);
      generateVariableIr(Scope.GLOBAL, decl, builder);
    }

    loadFunctions(lib.getProcDecls());
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
    procEnvironment.addScope();
    varEnvironment.addScope();
    procArgs.addScope();
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

    loadFunctions(program.getProcDecls());
    typeChecker.loadFunctions(program.getProcDecls());

    builder.buildCodeSectionHeader();
    for (Statement statement : program.getStatements()) {
      statement.accept(typeChecker);
      generateStatementIr(Scope.LOCAL, statement, builder);
    }
    builder.buildCodeSectionEnd();

    builder.buildProcedureSectionHeader();
    for (ProcedureDeclaration decl : program.getProcDecls()){
      decl.accept(typeChecker);
      generateProcedureIr(decl, builder);
      typeChecker.removeVarScope();
    }

    varEnvironment.removeScope();
    procEnvironment.removeScope();
    procArgs.removeScope();

    return builder.completeBuild();
  }

  public void generateConstantIr(Scope scope, ConstDeclaration constDecl, DefinitionBuilder builder) {
    Identifier id = constDecl.getIdentifier();
    Expression valueExpr = constDecl.getValue();
    Exp value = generateExpressionIr(Scope.LOCAL, valueExpr, builder);
    TypeCheckerQualities qual = valueExpr.acceptResult(typeChecker);
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    IdentExp place = builder.buildDefinition(scope, value, type);
    varEnvironment.addEntry(id.getLexeme(), place);
    if(scope == ICode.Scope.GLOBAL)
      builder.addVariableEntry(place.ident, SymEntry.CONST | SymEntry.INTERNAL, id.getLexeme());
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
    varEnvironment.addEntry(id.getLexeme(), place);
    if(scope == ICode.Scope.GLOBAL)
      builder.addVariableEntry(place.ident, SymEntry.INTERNAL, id.getLexeme());
  }

  private void loadFunctions(List<ProcedureDeclaration> decls){
    for(ProcedureDeclaration decl : decls){
      loadFunction(decl);
    }
  }

  public void loadFunction(ProcedureDeclaration procDecl){
    String procedureName = procDecl.getProcedureName().getLexeme();
    List <ParamaterDeclaration> args = procDecl.getArguments();
    
    IdentEntryList alias = new IdentEntryList();
    for(int i = 0; i < args.size(); i++){
	    String argAlias = gen.genNext();
      alias.add(new IdentExp(Scope.PARAM, argAlias));
    }

    procArgs.addEntry(procedureName, alias);
  
    String returnPlace = gen.genNext();
    Expression retExp = procDecl.getReturnStatement();
    if(retExp != null){
      procEnvironment.addEntry(procedureName, new IdentExp(Scope.RETURN, returnPlace));
    }
  } 

  public void generateProcedureIr(ProcedureDeclaration procDecl, StatementBuilder builder){
    String procedureName = procDecl.getProcedureName().getLexeme();
    builder.buildProcedureLabel(procedureName);
    paramEnvironment.addScope();
    varEnvironment.addScope();

    IdentEntryList list = procArgs.getEntry(procedureName);
    for(int i = 0; i < procDecl.getArguments().size(); i++){
      ParamaterDeclaration decl = procDecl.getArguments().get(i);
      String actual = decl.getIdentifier().getLexeme();
      IdentExp alias = list.get(i);
      paramEnvironment.addEntry(actual, alias);
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
      Exp retPlace = generateExpressionIr(Scope.LOCAL, retExp, builder);
      IdentExp returnPlace = procEnvironment.getEntry(procedureName);
      builder.buildDefinition(Scope.RETURN, returnPlace.ident, retPlace, ConversionUtils.typeCheckerQualitiesToAssignType(qual));
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
      IdentEntryList argsToMap = procArgs.getEntry(funcName);
      List<Def> valArgResults = new ArrayList<Def>();
      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
        TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
        Exp result = generateExpressionIr(Scope.LOCAL, valArg, builder);
        valArgResults.add(new Def(Scope.PARAM, argsToMap.get(i).ident, result, type));
      }
      builder.buildProcedureCall(funcName, valArgResults);
    } else {
      //Generate an External Procedure Call
      LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
      for(Expression valArg : valArgs){
         Exp place = generateExpressionIr(scope, valArg, builder);
         TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
         ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
         args.add(new Tuple<Exp, ICode.Type>(place, type));
      }
      builder.buildExternalProcedureCall(funcName, args);
    }
  }

  public void generateWhileBranchIr(Scope scope, WhileElifBranch whilebranch, StatementBuilder builder){
    Expression toCheck = whilebranch.getExpression();
    List<Statement> toExec = whilebranch.getExecStatements();
    Exp test = generateExpressionIr(scope, toCheck, builder);
    
    builder.buildWhileLoopBeginning(test);

    builder.incrimentWhileLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
      generateStatementIr(scope, toExec.get(i), builder);
    }

    Exp test2 = generateExpressionIr(scope, toCheck, builder);
    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    builder.buildAssignment(scope, test.toString(), test2, type);

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
    Exp test = generateExpressionIr(scope, toCheck, builder);
    builder.buildIfStatementBeginning(test);

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
    Exp test = generateExpressionIr(scope, toCheck, builder);

    builder.buildRepeatLoopBeginning(test);

    builder.incrimentRepeatLoopLevel();
    for(int i = 0; i < toExec.size(); i++){
	    generateStatementIr(scope, toExec.get(i), builder);
    }
    builder.deIncrimentRepeatLoopLevel();

    Exp test2 = generateExpressionIr(scope, toCheck, builder);
    TypeCheckerQualities qual = toCheck.acceptResult(typeChecker);
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
    builder.buildAssignment(scope, test.toString(), test2, type);

    builder.buildRepeatLoopEnd();
  }

  public void generateForLoopIr(Scope scope, ForBranch forbranch, StatementBuilder builder){
    Expression toMod = forbranch.getModifyExpression();
    List<Statement> toExec = forbranch.getExecStatements();
    if(toMod != null){
        generateAssignmentIr(scope, forbranch.getInitAssignment(), builder);
        Exp target = generateExpressionIr(scope, forbranch.getTargetExpression(), builder);
        
        IdentExp curValue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());

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

        
        
        builder.incrimentForLoopLevel();
        for(int i = 0; i < toExec.size(); i++){
            generateStatementIr(scope, toExec.get(i), builder);
        }
        builder.deIncrimentForLoopLevel();

        Exp incriment = generateExpressionIr(scope, toMod, builder);
        TypeCheckerQualities qual = toMod.acceptResult(typeChecker);
        if(qual.containsQualities(TypeCheckerQualities.REAL)){
          IdentExp result = null;
          if(procArgs.entryExists("RAdd") && procEnvironment.entryExists("RAdd")){
              IdentEntryList params = procArgs.getEntry("RAdd");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, curValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, incriment, ICode.Type.REAL));

                IdentExp returnPlace = procEnvironment.getEntry("RAdd");
                result = builder.buildFunctionCall("RAdd", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function RAdd that contains two arguments", forbranch.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(curValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(incriment, ICode.Type.REAL));
                 result = builder.buildExternalFunctionCall(scope, "RAdd", args, ICode.Type.REAL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(curValue, ICode.Type.REAL));
               args.add(new Tuple<Exp, ICode.Type>(incriment, ICode.Type.REAL));
               result = builder.buildExternalFunctionCall(scope, "RAdd", args, ICode.Type.REAL);
            }
            builder.buildAssignment(curValue.scope, curValue.ident, result, ICode.Type.REAL);
        } else {
          IdentExp result = builder.buildDefinition(scope, new BinExp(curValue, BinExp.Operator.RADD, incriment), ICode.Type.REAL);
          builder.buildDefinition(scope, curValue.ident, result, ICode.Type.INT);
        }

        builder.buildForLoopEnd();
    } else {
      generateAssignmentIr(scope, forbranch.getInitAssignment(), builder);
      Exp target = generateExpressionIr(scope, forbranch.getTargetExpression(), builder);
      Exp curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
      builder.buildForLoopBeginning(curvalue, BinExp.Operator.NE, target);
      builder.incrimentForLoopLevel();
      for(int i = 0; i < toExec.size(); i++){
          generateStatementIr(scope, toExec.get(i), builder);
      }
      builder.deIncrimentForLoopLevel();
      builder.buildForLoopEnd();
    }
  }
        
  public void generateAssignmentIr(Scope scope, Assignment assignment, AssignmentBuilder builder) {
    IdentExp place = varEnvironment.getEntry(assignment.getVariableName().getLexeme());
    Expression exp = assignment.getVariableValue();
    Exp value = generateExpressionIr(scope, exp, builder);
    TypeCheckerQualities qual = exp.acceptResult(typeChecker);
    TypeCheckerQualities convType = assignment.getVariableName().acceptResult(typeChecker);
    if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.REAL)){
      if(procEnvironment.entryExists("RealToInt")){
        IdentExp argDest = procArgs.getEntry("RealToInt").getFirst();
        List<Def> args = new LinkedList<Def>();
        args.add(new Def(Scope.PARAM, argDest.ident, value, ICode.Type.REAL));
        value = builder.buildFunctionCall("RealToInt", args, procEnvironment.getEntry("RealToInt"), ICode.Type.REAL);
      } else {
        List<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
        args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.REAL));
        value = builder.buildExternalFunctionCall(scope, "RealToInt", args, ICode.Type.INT);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
      if(procEnvironment.entryExists("IntToReal")){
        IdentExp argDest = procArgs.getEntry("IntToReal").getFirst();
        List<Def> args = new LinkedList<Def>();
        args.add(new Def(Scope.PARAM, argDest.ident, value, ICode.Type.INT));
        IdentExp retPlace = procEnvironment.getEntry("IntToReal");

        value = builder.buildFunctionCall("IntToReal", args, retPlace, ICode.Type.REAL);
      } else {
        List<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
        args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.INT));
        value = builder.buildExternalFunctionCall(scope, "IntToReal", args, ICode.Type.REAL);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.INTEGER)){
      if(procEnvironment.entryExists("IntToBool")){
        IdentExp argDest = procArgs.getEntry("IntToBool").getFirst();
        List<Def> args = new LinkedList<Def>();
        args.add(new Def(Scope.PARAM, argDest.ident, value, ICode.Type.INT));
        IdentExp retPlace = procEnvironment.getEntry("IntToBool");

        value = builder.buildFunctionCall("IntToBool", args, retPlace, ICode.Type.BOOL);
      } else {
        List<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
        args.add(new Tuple<Exp,ICode.Type>(value, ICode.Type.INT));
        value = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.INTEGER) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
      if(procEnvironment.entryExists("BoolToInt")){
        IdentExp argDest = procArgs.getEntry("BoolToInt").getFirst();
        List<Def> args = new LinkedList<Def>();
        args.add(new Def(Scope.PARAM, argDest.ident, value, ICode.Type.BOOL));
        IdentExp retPlace = procEnvironment.getEntry("BoolToInt");
        value = builder.buildFunctionCall("BoolToInt", args, retPlace, ICode.Type.INT);
      } else {
        List<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
        args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.BOOL));
        value = builder.buildExternalFunctionCall(scope, "BoolToInt", args, ICode.Type.INT);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.BOOLEAN) && qual.containsQualities(TypeCheckerQualities.REAL)){
      if(procEnvironment.entryExists("RealToBool")){
        IdentExp argDest = procArgs.getEntry("RealToBool").getFirst();
        List<Def> args = new LinkedList<Def>();
        args.add(new Def(Scope.PARAM, argDest.ident, value, ICode.Type.REAL));
        IdentExp retPlace = procEnvironment.getEntry("RealToBool");

        value = builder.buildFunctionCall("RealToBool", args, retPlace, ICode.Type.BOOL);
      } else {
        List<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
        args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.REAL));
        value = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
      }
    } else if(convType.containsQualities(TypeCheckerQualities.REAL) && qual.containsQualities(TypeCheckerQualities.BOOLEAN)){
      if(procEnvironment.entryExists("BoolToReal")){
        IdentExp argDest = procArgs.getEntry("BoolToReal").getFirst();
        List<Def> args = new LinkedList<Def>();
        args.add(new Def(Scope.PARAM, argDest.ident, value, ICode.Type.BOOL));
        IdentExp retPlace = procEnvironment.getEntry("BoolToReal");
        value = builder.buildFunctionCall("BoolToReal", args, retPlace, ICode.Type.REAL);
      } else {
        List<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
        args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.BOOL));
        value = builder.buildExternalFunctionCall(scope, "BoolToReal", args, ICode.Type.REAL);
      }
    }
    ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(convType);
    builder.buildAssignment(scope, place.ident, value, type);
  }

  public void generateInlineAssemblyIr(Asm asm, StatementBuilder builder) {
     List<IdentExp> icodeParams = new LinkedList<IdentExp>();
     for(String param : asm.getParamaters()){
       if(varEnvironment.entryExists(param)){
          IdentExp icodeParam = varEnvironment.getEntry(param);
          icodeParams.add(icodeParam);
       } else {
          throw new ICodeGeneratorException(asm, "Input paramater " + param + " does not exist"); 
       }
     }
     builder.buildInlineAssembly(asm.getInlineAssembly(), icodeParams);
  }

  
  public Exp generateExpressionIr(Scope scope, Expression exp, DefinitionBuilder builder){
    if(exp instanceof BinaryOperation) return generateBinaryOperationIr(scope, (BinaryOperation)exp, builder);
    else if(exp instanceof FunctionCall) return generateFunctionCallIr(scope, (FunctionCall)exp, builder);
    else if(exp instanceof UnaryOperation) return generateUnaryOperationIr(scope, (UnaryOperation)exp, builder);
    else if(exp instanceof Identifier) return generateIdentifierIr((Identifier)exp, builder);
    else if(exp instanceof NumValue) return generateNumberIr(scope, (NumValue)exp, builder);
    else if(exp instanceof BoolValue) return generateBooleanIr(scope, (BoolValue)exp, builder);
    else if(exp instanceof StrValue) return generateStringIr(scope, (StrValue)exp, builder);
    else {
      throw new RuntimeException("Error Invalid Expression Type found when generating Ir at" + exp.getStart().toString());
    }
  }
  
  
  public Exp generateBinaryOperationIr(Scope scope, BinaryOperation binaryOperation, DefinitionBuilder builder) {
      Exp leftValue = generateExpressionIr(scope, binaryOperation.getLeft(), builder);
      TypeCheckerQualities leftType = binaryOperation.getLeft().acceptResult(typeChecker);
      
      Exp rightValue = generateExpressionIr(scope, binaryOperation.getRight(), builder);
      TypeCheckerQualities rightType = binaryOperation.getRight().acceptResult(typeChecker);

      if(binaryOperation.getOperator() == BinaryOperation.OpType.AND || binaryOperation.getOperator() == BinaryOperation.OpType.OR){
        if(leftType.containsQualities(TypeCheckerQualities.REAL)){
          if(procArgs.entryExists("RealToBool") && procEnvironment.entryExists("RealToBool")){
             IdentEntryList params = procArgs.getEntry("RealToBool");
             if(params.size() >= 1){
               LinkedList<Def> args = new LinkedList<Def>();
               args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
               IdentExp entry = procEnvironment.getEntry("RealToBool");
               leftValue = builder.buildFunctionCall("RealToBool", args, entry, ICode.Type.BOOL);
             } else {
                LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                leftValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
             }
           } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
              leftValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
           }
        } else if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToBool") && procEnvironment.entryExists("IntToBool")){
            IdentEntryList params = procArgs.getEntry("IntToBool");
            if(params.size() >= 1){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.INT));
                IdentExp entry = procEnvironment.getEntry("IntToBool");
                leftValue = builder.buildFunctionCall("IntToBool", args, entry, ICode.Type.BOOL);
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
              leftValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
            }
          } else {
            LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
            args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
            leftValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
          }
        }

        if(rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(procArgs.entryExists("RealToBool") && procEnvironment.entryExists("RealToBool")){
             IdentEntryList params = procArgs.getEntry("RealToBool");
             if(params.size() >= 1){
               LinkedList<Def> args = new LinkedList<Def>();
               args.add(new Def(Scope.PARAM, params.get(0).ident, rightValue, ICode.Type.REAL));
               IdentExp entry = procEnvironment.getEntry("RealToBool");
               rightValue = builder.buildFunctionCall("RealToBool", args, entry, ICode.Type.BOOL);
             } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              rightValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
             }
           } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              rightValue = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
           }
        } else if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToBool") && procEnvironment.entryExists("IntToBool")){
             IdentEntryList params = procArgs.getEntry("IntToBool");
             if(params.size() >= 1){
               LinkedList<Def> args = new LinkedList<Def>();
               args.add(new Def(Scope.PARAM, params.get(0).ident, rightValue, ICode.Type.INT));
               IdentExp entry = procEnvironment.getEntry("IntToBool");
               rightValue = builder.buildFunctionCall("IntToBool", args, entry, ICode.Type.BOOL);
             } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
              rightValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
             }
           } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
              rightValue = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
           }
        }

        switch(binaryOperation.getOperator()){
          case AND: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.LAND, rightValue, ICode.Type.BOOL);
          case OR: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.LOR, rightValue, ICode.Type.BOOL);
          default: return leftValue;
        }
      } else if(leftType.containsQualities(TypeCheckerQualities.REAL) || rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(leftType.containsQualities(TypeCheckerQualities.INTEGER)){
           if(procArgs.entryExists("IntToReal") && procEnvironment.entryExists("IntToReal")){
             IdentEntryList params = procArgs.getEntry("IntToReal");
             if(params.size() >= 1){
               LinkedList<Def> args = new LinkedList<Def>();
               args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.INT));
               IdentExp entry = procEnvironment.getEntry("IntToReal");
               leftValue = builder.buildFunctionCall("IntToReal", args, entry, ICode.Type.REAL);
             } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
              leftValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, ICode.Type.REAL);
             }
           } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
              leftValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, ICode.Type.REAL);
           }
        }

        if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToReal") && procEnvironment.entryExists("IntToReal")){
             IdentEntryList params = procArgs.getEntry("IntToReal");
             if(params.size() >= 1){
               LinkedList<Def> args = new LinkedList<Def>();
               args.add(new Def(Scope.PARAM, params.get(0).ident, rightValue, ICode.Type.INT));
               IdentExp entry = procEnvironment.getEntry("IntToReal");
               rightValue = builder.buildFunctionCall("IntToReal", args, entry, ICode.Type.REAL);
             } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
              rightValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, ICode.Type.REAL);
             }
           } else {
            LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
            args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
            rightValue = builder.buildExternalFunctionCall(scope, "IntToReal", args, ICode.Type.REAL);
           }
        }
      
      switch (binaryOperation.getOperator()){
          case PLUS:
            if(procArgs.entryExists("RAdd") && procEnvironment.entryExists("RAdd")){
              IdentEntryList params = procArgs.getEntry("RAdd");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RAdd");

                return builder.buildFunctionCall("RAdd", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function RAdd that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RAdd", args, ICode.Type.REAL);
              }
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RAdd", args, ICode.Type.REAL);
            }
          case MINUS: 
            if(procArgs.entryExists("RSub") && procEnvironment.entryExists("RSub")){
              IdentEntryList params = procArgs.getEntry("RSub");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RSub");
                return builder.buildFunctionCall("RSub", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Subtract that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RSub", args, ICode.Type.REAL);
              }
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RSub", args, ICode.Type.REAL);
            }
          case TIMES: 
            if(procArgs.entryExists("RMul") && procEnvironment.entryExists("RMul")){
              IdentEntryList params = procArgs.getEntry("RMul");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RMul");

                return builder.buildFunctionCall("RMul", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Divide that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RMul", args, ICode.Type.REAL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
               args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RMul", args, ICode.Type.REAL);
            }
          case DIVIDE:
            if(procArgs.entryExists("RDivide") && procEnvironment.entryExists("RDivide")){
              IdentEntryList params = procArgs.getEntry("RDivide");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RDivide");

                return builder.buildFunctionCall("RDivide", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Divide that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RDivide", args, ICode.Type.REAL);
              }
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RDivide", args, ICode.Type.REAL);
            }
          case DIV:
            if(procArgs.entryExists("RDiv") && procEnvironment.entryExists("RDiv")){
              IdentEntryList params = procArgs.getEntry("RDiv");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RDiv");

                return builder.buildFunctionCall("RDiv", args, returnPlace, ICode.Type.INT);
              } else {
                 errorLog.add("Cant find the function Div that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RDiv", args, ICode.Type.INT);
              }
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RDiv", args, ICode.Type.INT);
            }
          case LE:
            if(procArgs.entryExists("RLessThanOrEqualTo") && procEnvironment.entryExists("RLessThanOrEqualTo")){
              IdentEntryList params = procArgs.getEntry("RLessThanOrEqualTo");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RLessThanOrEqualTo");

                return builder.buildFunctionCall("RLessThanOrEqualTo", args, returnPlace, ICode.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RLessThanOrEqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RLessThanOrEqualTo", args, ICode.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
               args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RLessThanOrEqualTo", args, ICode.Type.BOOL);
            }
          case LT:
            if(procArgs.entryExists("RLessThan") && procEnvironment.entryExists("RLessThan")){
              IdentEntryList params = procArgs.getEntry("RLessThan");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RLessThan");

                return builder.buildFunctionCall("RLessThan", args, returnPlace, ICode.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RLessThan that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RLessThan", args, ICode.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                return builder.buildExternalFunctionCall(scope, "RLessThan", args, ICode.Type.BOOL);
            }
          case GE:
            if(procArgs.entryExists("RGreaterThanOrEqualTo") && procEnvironment.entryExists("RGreaterThanOrEqualTo")){
              IdentEntryList params = procArgs.getEntry("RGreaterThanOrEqualTo");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RGreaterThanOrEqualTo");

                return builder.buildFunctionCall("RGreaterThanOrEqualTo", args, returnPlace, ICode.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RGreaterThanOrEqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RGreaterThanOrEqualTo", args, ICode.Type.BOOL);
              }
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
              args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
              return builder.buildExternalFunctionCall(scope, "RGreaterThanOrEqualTo", args, ICode.Type.BOOL);
            }
          case GT:
            if(procArgs.entryExists("RGreaterThan") && procEnvironment.entryExists("RGreaterThan")){
              IdentEntryList params = procArgs.getEntry("RGreaterThan");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RGreaterThan");

                return builder.buildFunctionCall("RGreaterThan", args, returnPlace, ICode.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RGreaterThan that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RGreaterThan", args, ICode.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
               args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RGreaterThan", args, ICode.Type.BOOL);
            }
          case EQ:
            if(procArgs.entryExists("REqualTo") && procEnvironment.entryExists("REqualTo")){
              IdentEntryList params = procArgs.getEntry("REqualTo");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));

                IdentExp returnPlace = procEnvironment.getEntry("REqualTo");

                return builder.buildFunctionCall("REqualTo", args, returnPlace, ICode.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function REqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "REqualTo", args, ICode.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
               args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "REqualTo", args, ICode.Type.BOOL);
            }
          case NE: 
            if(procArgs.entryExists("RNotEqualTo") && procEnvironment.entryExists("RNotEqualTo")){
              IdentEntryList params = procArgs.getEntry("RNotEqualTo");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.REAL));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RNotEqualTo");

                return builder.buildFunctionCall("RNotEqualTo", args, returnPlace, ICode.Type.BOOL);
              } else {
                 errorLog.add("Cant find the function RNotEqualTo that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RNotEqualTo", args, ICode.Type.BOOL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.REAL));
                args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.REAL));
                return builder.buildExternalFunctionCall(scope, "RNotEqualTo", args, ICode.Type.BOOL);
            }
          default: return leftValue;
      }
    } else {
      switch (binaryOperation.getOperator()){
        case PLUS: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IADD, rightValue, ICode.Type.INT);
        case MINUS: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.ISUB, rightValue, ICode.Type.INT);
        case TIMES: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IMUL, rightValue, ICode.Type.INT);
        case DIV: 
          if(procArgs.entryExists("Div") && procEnvironment.entryExists("Div")){
              IdentEntryList params = procArgs.getEntry("Div");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.INT));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.INT));
                IdentExp returnPlace = procEnvironment.getEntry("Div");

                return builder.buildFunctionCall("Div", args, returnPlace, ICode.Type.INT);
              } else {
                 errorLog.add("Cant find the function Div that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
                 return builder.buildExternalFunctionCall(scope, "Div", args, ICode.Type.INT);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
               args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
               return builder.buildExternalFunctionCall(scope, "Div", args, ICode.Type.INT);
            }
        case DIVIDE: 
            if(procArgs.entryExists("Divide") && procEnvironment.entryExists("Divide")){
              IdentEntryList params = procArgs.getEntry("Divide");
              if(params.size() >= 2){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, leftValue, ICode.Type.INT));
                args.add(new Def(Scope.PARAM, params.get(1).ident, rightValue, ICode.Type.INT));
                IdentExp returnPlace = procEnvironment.getEntry("Divide");

                return builder.buildFunctionCall("Divide", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function Divide that contains two arguments", binaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
                 args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
                 return builder.buildExternalFunctionCall(scope, "Divide", args, ICode.Type.REAL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(leftValue, ICode.Type.INT));
               args.add(new Tuple<Exp, ICode.Type>(rightValue, ICode.Type.INT));
               return builder.buildExternalFunctionCall(scope, "Divide", args, ICode.Type.REAL);
            }
        case MOD: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IMOD, rightValue, ICode.Type.INT);
        case LE: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.LE, rightValue, ICode.Type.BOOL);
        case LT: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.LT, rightValue, ICode.Type.BOOL);
        case GE: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.GE, rightValue, ICode.Type.BOOL);
        case GT: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.GT, rightValue, ICode.Type.BOOL);
        case BAND: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IAND, rightValue, ICode.Type.INT);
        case BOR: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IOR, rightValue, ICode.Type.INT);
        case BXOR: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IXOR, rightValue, ICode.Type.INT);
        case LSHIFT: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.ILSHIFT, rightValue, ICode.Type.INT);
        case RSHIFT: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.IRSHIFT, rightValue, ICode.Type.INT);
        case EQ: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.EQ, rightValue, ICode.Type.BOOL);
        case NE: return builder.buildBinaryDefinition(scope, leftValue, BinExp.Operator.NE, rightValue, ICode.Type.BOOL);
        default: return leftValue;
      }
    }
  }

  public Exp generateFunctionCallIr(Scope scope, FunctionCall funcCall, DefinitionBuilder builder) {
    String funcName = funcCall.getFunctionName().getLexeme();
    List<Expression> valArgs = funcCall.getArguments();
    TypeCheckerQualities returnType = funcCall.acceptResult(typeChecker);
    ICode.Type retType = ConversionUtils.typeCheckerQualitiesToAssignType(returnType);
    if(procArgs.entryExists(funcName)){
      //Build Internal Function Call Sequence
      IdentEntryList argsToMap = procArgs.getEntry(funcName);
      List<Def> valArgResults = new ArrayList<Def>();

      for(int i = 0; i < valArgs.size(); i++){
        Expression valArg = valArgs.get(i);
        TypeCheckerQualities qual = valArg.acceptResult(typeChecker);
        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
	      Exp result = generateExpressionIr(scope, valArg, builder);
	      valArgResults.add(new Def(Scope.PARAM, argsToMap.get(i).ident, result, type));
      }
      IdentExp returnPlace = procEnvironment.getEntry(funcName);

      return builder.buildFunctionCall(funcName, valArgResults, returnPlace, retType);
    } else {
      //Build external function call
      LinkedList<Tuple<Exp, ICode.Type>> procedureArgs = new LinkedList<Tuple<Exp, ICode.Type>>();
      for(Expression arg : valArgs){
        Exp place = generateExpressionIr(scope, arg, builder);
        TypeCheckerQualities qual = arg.acceptResult(typeChecker);
        ICode.Type type = ConversionUtils.typeCheckerQualitiesToAssignType(qual);
        procedureArgs.add(new Tuple<Exp, ICode.Type>(place, type));
      }

      return builder.buildExternalFunctionCall(scope, funcName, procedureArgs, retType);
    }
  }

  public Exp generateUnaryOperationIr(Scope scope, UnaryOperation unaryOperation, DefinitionBuilder builder) {
    Exp value = generateExpressionIr(scope, unaryOperation.getExpression(), builder);
    TypeCheckerQualities rightType = unaryOperation.getExpression().acceptResult(typeChecker);

    if(unaryOperation.getOperator() == UnaryOperation.OpType.NOT){
      if(rightType.containsQualities(TypeCheckerQualities.REAL)){
          if(procArgs.entryExists("RealToBool") && procEnvironment.entryExists("RealToBool")){
             IdentEntryList params = procArgs.getEntry("RealToBool");
             if(params.size() >= 1){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, value, ICode.Type.REAL));
                IdentExp entry = procEnvironment.getEntry("RealToBool");
                value = builder.buildFunctionCall("RealToBool", args, entry, ICode.Type.BOOL);
             } else {
                LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.REAL));
                value = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
             }
           } else {
             LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
             args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.REAL));
             value = builder.buildExternalFunctionCall(scope, "RealToBool", args, ICode.Type.BOOL);
           }
        } else if(rightType.containsQualities(TypeCheckerQualities.INTEGER)){
          if(procArgs.entryExists("IntToBool") && procEnvironment.entryExists("IntToBool")){
            IdentEntryList params = procArgs.getEntry("IntToBool");
            if(params.size() >= 1){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, value, ICode.Type.INT));
                IdentExp entry = procEnvironment.getEntry("IntToBool");
                value = builder.buildFunctionCall("IntToBool", args, entry, ICode.Type.BOOL);
            } else {
              LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
              args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.INT));
              value = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
            }
          } else {
            LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
            args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.INT));
            value = builder.buildExternalFunctionCall(scope, "IntToBool", args, ICode.Type.BOOL);
          }
        }
        Exp expVal = value;
        return builder.buildUnaryDefinition(scope, UnExp.Operator.BNOT, expVal, ICode.Type.BOOL);
    } else if(rightType.containsQualities(TypeCheckerQualities.REAL)){
      switch(unaryOperation.getOperator()){
        case MINUS: 
          if(procArgs.entryExists("RNeg") && procEnvironment.entryExists("RNeg")){
              IdentEntryList params = procArgs.getEntry("RNeg");
              if(params.size() >= 1){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, value, ICode.Type.REAL));
                IdentExp returnPlace = procEnvironment.getEntry("RNeg");

                return builder.buildFunctionCall("RNeg", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function RNeg that contains one argument", unaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.REAL));
                 return builder.buildExternalFunctionCall(scope, "RNeg", args, ICode.Type.REAL);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.REAL));
               return builder.buildExternalFunctionCall(scope, "RNeg", args, ICode.Type.REAL);
            }
        default:
          errorLog.add("Error unexpected Operation for Real Value " + unaryOperation.getOperator(), unaryOperation.getStart());
          return value;
	    }
    } else {
      switch(unaryOperation.getOperator()){
        case MINUS:
          if(procArgs.entryExists("INeg") && procEnvironment.entryExists("INeg")){
              IdentEntryList params = procArgs.getEntry("INeg");
              if(params.size() >= 1){
                LinkedList<Def> args = new LinkedList<Def>();
                args.add(new Def(Scope.PARAM, params.get(0).ident, value, ICode.Type.INT));
                IdentExp returnPlace = procEnvironment.getEntry("INeg");

                return builder.buildFunctionCall("INeg", args, returnPlace, ICode.Type.REAL);
              } else {
                 errorLog.add("Cant find the function INeg that contains one argument", unaryOperation.getStart());
                 LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
                 args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.INT));
                 return builder.buildExternalFunctionCall(scope, "INeg", args, ICode.Type.INT);
              }
            } else {
               LinkedList<Tuple<Exp, ICode.Type>> args = new LinkedList<Tuple<Exp, ICode.Type>>();
               args.add(new Tuple<Exp, ICode.Type>(value, ICode.Type.INT));
               return builder.buildExternalFunctionCall(scope, "INeg", args, ICode.Type.INT);
            }
        case BNOT: return builder.buildUnaryDefinition(scope, UnExp.Operator.INOT, value, ICode.Type.INT);
        default: return value;
	    }
    }
  }

  public Exp generateIdentifierIr(Identifier identifier, DefinitionBuilder builder){
    if(varEnvironment.entryExists(identifier.getLexeme())){
      IdentExp place = varEnvironment.getEntry(identifier.getLexeme());
      return place;
    } else {
        if(builder.containsExternalVariable(identifier.getLexeme())){
          return new IdentExp(Scope.GLOBAL, builder.getVariablePlace(identifier.getLexeme()));
        } else {
            String place = gen.genNext();
            builder.addVariableEntry(place, SymEntry.EXTERNAL, identifier.getLexeme());
            IdentExp ident = new IdentExp(Scope.LOCAL, place);
            varEnvironment.addEntry(identifier.getLexeme(), ident);
            return ident;
        }
    }
  }

  public IdentExp generateNumberIr(Scope scope, NumValue numValue, DefinitionBuilder builder){
      String rawnum = Utils.ifHexToInt(numValue.getLexeme());
      if(rawnum.contains(".")){
         return builder.buildDefinition(scope, new RealExp(Float.parseFloat(rawnum)), ICode.Type.REAL);
      } else {
        return builder.buildDefinition(scope, new IntExp(Integer.parseInt(rawnum)), ICode.Type.INT);
      }
  }

  public IdentExp generateBooleanIr(Scope scope, BoolValue boolValue, DefinitionBuilder builder){
      String lexeme = boolValue.getLexeme(); //change to hex if you need to otherwise unchanged
      return builder.buildDefinition(scope, new BoolExp(Boolean.parseBoolean(lexeme)), ICode.Type.BOOL);
  }

  public IdentExp generateStringIr(Scope scope, StrValue strValue, DefinitionBuilder builder){
      return builder.buildDefinition(scope, new StrExp(strValue.getLexeme()), ICode.Type.STRING);
  }


  public String generateParamaterDeclarationIr(ParamaterDeclaration parDeclaration, DefinitionBuilder builder) {
    Identifier id = parDeclaration.getIdentifier();
    String alias = gen.genNext();
    varEnvironment.addEntry(id.getLexeme(), new IdentExp(Scope.PARAM, alias));
    return alias;
  }
}
