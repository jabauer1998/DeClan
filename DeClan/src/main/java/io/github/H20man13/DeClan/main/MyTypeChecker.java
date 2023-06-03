package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.symboltable.VariableEntry;
import io.github.H20man13.DeClan.common.symboltable.ProcedureEntry;
import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.symboltable.Environment;


import java.lang.Number;
import java.lang.Object;

import static io.github.H20man13.DeClan.main.MyIO.*;

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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the type checker visitor
 * It provides type checking to the entire language
 * Types are represented as Enums
 * @author Jacob Bauer 
 */
public class MyTypeChecker implements ASTVisitor, ExpressionVisitor<MyTypeChecker.TypeCheckerTypes> {
    private ErrorLog errorLog;
    
    private Environment <String, TypeCheckerTypes> varEnvironment;
    private Environment <String, ProcedureEntry> procEnvironment;

    public static enum TypeCheckerTypes implements Copyable<TypeCheckerTypes> {
		VOID, INTEGER, BOOLEAN, STRING, REAL;

		@Override
		public TypeCheckerTypes copy() {
			return this;
		}
    }
    
    // TODO declare any data structures needed by the interpreter

    public MyTypeChecker(ErrorLog errorLog) {
	this.errorLog = errorLog;
	this.varEnvironment = new Environment<>();
	this.procEnvironment = new Environment<>();
    }

    private static TypeCheckerTypes StringToType(String str){
	if(str.equals("STRING")){
	    return TypeCheckerTypes.STRING;
	} else if (str.equals("INTEGER")) {
	    return TypeCheckerTypes.INTEGER;
	} else if (str.equals("REAL")){
	    return TypeCheckerTypes.REAL;
	} else if (str.equals("BOOLEAN")) {
	    return TypeCheckerTypes.BOOLEAN;
	} else if (str.equals("VOID")){
	    return TypeCheckerTypes.VOID;
	} else {
	    System.err.println("Unknown String Type" + str);
	    return null;
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
  
    @Override
    public void visit(ConstDeclaration constDecl) {
	Identifier id = constDecl.getIdentifier();
	Expression valueExpr = constDecl.getValue();
	TypeCheckerTypes value = valueExpr.acceptResult(this);
	if(!varEnvironment.inScope(id.getLexeme())){
	    varEnvironment.addEntry(id.getLexeme(), value);
	} else {
	    errorLog.add("Constant " + id.getLexeme() + " within scope already declared", id.getStart());
	}
    }

    @Override
    public void visit(VariableDeclaration varDecl) {
	Identifier id = varDecl.getIdentifier();
	String type = varDecl.getType().getLexeme();
	if(!varEnvironment.inScope(id.getLexeme())){
	    switch(StringToType(type)){
	    case STRING:
		errorLog.add("Variable " + id.getLexeme() + "is of invalid type " + type, id.getStart());
	    case VOID:
		errorLog.add("Variable " + id.getLexeme() + "is of invalid type " + type, id.getStart());
	    default:
		varEnvironment.addEntry(id.getLexeme(), StringToType(type));
	    }
	} else {
	    errorLog.add("Multiple Declaration of Variable " + id.getLexeme(), id.getStart());
	}
    }

    @Override
    public void visit(ProcedureDeclaration procDecl){
	String procedureName = procDecl.getProcedureName().getLexeme();
	if(!procEnvironment.entryExists(procedureName)){
	    varEnvironment.addScope();
	    List <VariableDeclaration> args = procDecl.getArguments();
	    for(VariableDeclaration decl : args){
		decl.accept(this);
	    }
	    List <Declaration> localVars = procDecl.getLocalVariables();
	    for(Declaration decl : localVars){
		decl.accept(this);
	    }
	    List <Statement> Exec = procDecl.getExecutionStatements();
	    for(Statement exe : Exec){
		exe.accept(this);
	    }
	    Expression retExp = procDecl.getReturnStatement();
	    String returnType = procDecl.getReturnType().getLexeme();
	    if(!returnType.equals("VOID")){
		TypeCheckerTypes type = retExp.acceptResult(this);
		if(StringToType(returnType) != type){
		    errorLog.add("Return Expression is not of type " + returnType, procDecl.getStart());
		}
	    }
	    varEnvironment.removeScope();
	    procEnvironment.addEntry(procedureName, new ProcedureEntry(args, returnType, localVars, Exec, retExp));
	} else {
	    errorLog.add("Procedure " + procedureName + " within scope already declared", procDecl.getStart());
	}
    }
        
    @Override
    public void visit(ProcedureCall procedureCall) {
	if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
	    TypeCheckerTypes partype = procedureCall.getArguments().get(0).acceptResult(this);
	    if(partype != TypeCheckerTypes.INTEGER){
		errorLog.add("Procedure Call PrintInt Exprected an Integer Argument", procedureCall.getStart());
	    }
	} else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
	    TypeCheckerTypes partype = procedureCall.getArguments().get(0).acceptResult(this);
	    if(partype != TypeCheckerTypes.REAL){
		errorLog.add("Procedure Call PrintDouble Exprected an Real Argument", procedureCall.getStart());
	    }
	} else if(procedureCall.getProcedureName().getLexeme().equals("PrintString")) {
	    TypeCheckerTypes partype = procedureCall.getArguments().get(0).acceptResult(this);
	    if(partype != TypeCheckerTypes.STRING){
		errorLog.add("Procedure Call PrintDouble Exprected an String Argument", procedureCall.getStart());
	    }
	} else if(procedureCall.getProcedureName().getLexeme().equals("PrintLn")){
	    //do nothing
	} else {
	    String funcName = procedureCall.getProcedureName().getLexeme();
	    if(procEnvironment.entryExists(funcName)){
		ProcedureEntry pentry = procEnvironment.getEntry(funcName);
		List<VariableDeclaration> args = pentry.getArguments();
		List<Expression> valArgs = procedureCall.getArguments();
		List<TypeCheckerTypes> exprValues = new ArrayList<>();
		for(Expression expr : valArgs){
		    exprValues.add(expr.acceptResult(this));
		}
		varEnvironment.addScope();
		if(args.size() == valArgs.size()){
		    for(int i = 0; i < args.size(); i++){
			args.get(i).accept(this); //declare parameter variables 
			TypeCheckerTypes toChangeType = varEnvironment.getEntry(args.get(i).getIdentifier().getLexeme());
			TypeCheckerTypes argumentType = exprValues.get(i);
			if(toChangeType != argumentType){
			    errorLog.add("In function " + funcName + " type mismatch at parameter " + (i + 1), procedureCall.getStart());
			}
		    }
		} else {
		    errorLog.add("Unexpected amount of arguments provided from Caller to Callie in Function " + procedureCall.getProcedureName().getLexeme(), procedureCall.getStart());
		}
		varEnvironment.removeScope(); //clean up local declarations as well as parameters
	    }
	}
    }	


    @Override
    public void visit(WhileElifBranch whilebranch){
	Expression toCheck = whilebranch.getExpression();
	if(toCheck.acceptResult(this) != TypeCheckerTypes.BOOLEAN){
	    errorLog.add("Invalid Boolean Expression in While Loop", toCheck.getStart());
	}
	List<Statement> toExec = whilebranch.getExecStatements();
	for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	}
	if (whilebranch.getNextBranch() != null){
	    whilebranch.getNextBranch().accept(this);
	}
    }
    
    @Override
    public void visit(IfElifBranch ifbranch){
	Expression toCheck = ifbranch.getExpression();
	if(toCheck.acceptResult(this) != TypeCheckerTypes.BOOLEAN){
	    errorLog.add("Invalid Boolean Expression in While Loop", ifbranch.getStart());
	}
	List<Statement> toExec = ifbranch.getExecStatements();
	for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	}
	if(ifbranch.getNextBranch() != null) {
	    ifbranch.getNextBranch().accept(this);
	}
    }

    @Override
    public void visit(ElseBranch elsebranch){
	List<Statement> toExec = elsebranch.getExecStatements();
	for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	}
    }

    @Override
    public void visit(RepeatBranch repeatbranch){
	Expression toCheck = repeatbranch.getExpression();
	if(toCheck.acceptResult(this) != TypeCheckerTypes.BOOLEAN){
	    errorLog.add("Invalid Boolean Expression in while loop", repeatbranch.getStart());
	}
	List<Statement> toExec = repeatbranch.getExecStatements();
	for(int i = 0; i < toExec.size(); i++){
	    toExec.get(i).accept(this);
	}
    }

    @Override
    public void visit(ForBranch forbranch){
	Expression toMod = forbranch.getModifyExpression();
	List<Statement> toExec = forbranch.getExecStatements();
	if(toMod != null){
	    forbranch.getInitAssignment().accept(this);
	    TypeCheckerTypes incriment = toMod.acceptResult(this);
	    TypeCheckerTypes target = forbranch.getTargetExpression().acceptResult(this);
	    TypeCheckerTypes curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	    if(incriment != target || incriment != curvalue){
		errorLog.add("Type Mismatch in head of For Loop", forbranch.getStart());
	    }
	    for(int i = 0; i < toExec.size(); i++){
	       toExec.get(i).accept(this);
	    }
	} else {
	    forbranch.getInitAssignment().accept(this);
	    TypeCheckerTypes target = forbranch.getTargetExpression().acceptResult(this);
	    TypeCheckerTypes curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	    if(curvalue != target){
	        errorLog.add("Type Mismatch in head of For Loop", forbranch.getStart());
	    }
	    for(int i = 0; i < toExec.size(); i++){
		toExec.get(i).accept(this);
	    }
	}
    }
        
    @Override
    public void visit(Assignment assignment) {
	String name = assignment.getVariableName().getLexeme();
	if(varEnvironment.entryExists(name)){
	    TypeCheckerTypes entry = varEnvironment.getEntry(name);
	    TypeCheckerTypes expression  = assignment.getVariableValue().acceptResult(this);
	    if(entry != expression){
		errorLog.add("Variable in Assignment " + name + " is of unknown DeClan type?", assignment.getStart());
	    }
	} else {
	    errorLog.add("Undeclared Variable " , assignment.getVariableName().getStart());
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
    public TypeCheckerTypes visitResult(BinaryOperation binaryOperation) {
	TypeCheckerTypes leftValue = binaryOperation.getLeft().acceptResult(this);
	TypeCheckerTypes rightValue = binaryOperation.getRight().acceptResult(this);
	BinaryOperation.OpType op = binaryOperation.getOperator();
	switch(op){
	case MOD:
	    if(leftValue != TypeCheckerTypes.INTEGER || rightValue != TypeCheckerTypes.INTEGER){
		errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
	    }
	    return null;
	case DIVIDE:
	    if(leftValue != TypeCheckerTypes.REAL || rightValue != TypeCheckerTypes.REAL){
		errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
	    }
	    return null;
	case DIV:
	    if(leftValue != TypeCheckerTypes.INTEGER || rightValue != TypeCheckerTypes.INTEGER){
		errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
	    }
	    return null;
	case AND:
	    if(leftValue != TypeCheckerTypes.BOOLEAN || rightValue != TypeCheckerTypes.BOOLEAN){
		errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
	    }
	    return null;
	case OR:
	    if(leftValue != TypeCheckerTypes.BOOLEAN || rightValue != TypeCheckerTypes.BOOLEAN){
		errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
	    }
	    return null;
	default:
	    if(leftValue != rightValue){
		errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
		return null;
	    } else {
		if(op == BinaryOperation.OpType.EQ || op == BinaryOperation.OpType.NE || op == BinaryOperation.OpType.LT || op == BinaryOperation.OpType.LE || op == BinaryOperation.OpType.GT || op == BinaryOperation.OpType.GE){
		    return TypeCheckerTypes.BOOLEAN;
		} else {
		    return leftValue;
		}
	    }
	}
    }

    @Override
    public TypeCheckerTypes visitResult(FunctionCall funcCall) {
	String funcName = funcCall.getFunctionName().getLexeme();
	if(procEnvironment.entryExists(funcName)){
	    ProcedureEntry pentry = procEnvironment.getEntry(funcName);
	    List<VariableDeclaration> args = pentry.getArguments();
	    List<Expression> valArgs = funcCall.getArguments();
	    List<TypeCheckerTypes> exprValues = new ArrayList<>();
	    for(Expression expr : valArgs){
		exprValues.add(expr.acceptResult(this));
	    }
	    varEnvironment.addScope();
	    if(args.size() == valArgs.size()){
		for(int i = 0; i < args.size(); i++){
		    args.get(i).accept(this); //declare parameter variables 
		    TypeCheckerTypes toChangeType = varEnvironment.getEntry(args.get(i).getIdentifier().getLexeme());
		    TypeCheckerTypes argumentType = exprValues.get(i);
		    if(toChangeType != argumentType){
			errorLog.add("In function " + funcName + " type mismatch at parameter " + (i + 1), funcCall.getStart());
		    }
		}
	    } else {
		errorLog.add("Unexpected amount of arguments provided from Caller to Callie in Function " + funcName, funcCall.getStart());
	    }
	    TypeCheckerTypes retValue = pentry.getReturnStatement().acceptResult(this);
	    varEnvironment.removeScope(); //clean up local declarations as well as parameters
	    return retValue;
	} else {
	    errorLog.add("Couldnt find entry for " + funcName, funcCall.getStart());
	    return null;
	}
    }

    @Override
    public TypeCheckerTypes visitResult(UnaryOperation unaryOperation) {
	TypeCheckerTypes value = unaryOperation.getExpression().acceptResult(this);
	if (value == TypeCheckerTypes.BOOLEAN || value == TypeCheckerTypes.INTEGER || value == TypeCheckerTypes.REAL){
	    return value;
	} else {
	    errorLog.add("Invalid Type for Unary Operation " + value , unaryOperation.getStart());
	    return null;
	}
    }
    
    @Override
    public TypeCheckerTypes visitResult(Identifier ident){
	if(!varEnvironment.entryExists(ident.getLexeme())){
	    errorLog.add("Couldnt find entry for " + ident.getLexeme(), ident.getStart());
	}
	return varEnvironment.getEntry(ident.getLexeme());
    }

    @Override
    public TypeCheckerTypes visitResult(NumValue numValue){
	String lexeme = numValue.getLexeme(); //change to hex if you need to otherwise unchanged
	if(lexeme.contains(".")){
	    return TypeCheckerTypes.REAL;
	} else {
	    return TypeCheckerTypes.INTEGER;
	}
    }

    @Override
    public TypeCheckerTypes visitResult(BoolValue boolValue){
	return TypeCheckerTypes.BOOLEAN;
    }

    @Override
    public TypeCheckerTypes visitResult(StrValue strValue){
	return TypeCheckerTypes.STRING;
    }
}
