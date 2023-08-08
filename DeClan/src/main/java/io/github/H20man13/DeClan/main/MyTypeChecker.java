package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.ProcedureTypeEntry;
import io.github.H20man13.DeClan.common.symboltable.entry.TypeCheckerQualities;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;

import java.lang.Number;
import java.lang.Object;

import static io.github.H20man13.DeClan.main.MyIO.*;

import java.lang.Math;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Map;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ErrorLog.LogItem;
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
import edu.depauw.declan.common.ast.BinaryOperation.OpType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the type checker visitor
 * It provides type checking to the entire language
 * Types are represented as Enums
 * @author Jacob Bauer 
 */
public class MyTypeChecker implements ASTVisitor, ExpressionVisitor<TypeCheckerQualities> {
    private ErrorLog errorLog;
    
    private Environment <String, TypeCheckerQualities> varEnvironment;
    private Environment <String, ProcedureTypeEntry> procEnvironment;
    
    // TODO declare any data structures needed by the interpreter

    public MyTypeChecker(ErrorLog errorLog) {
		this.errorLog = errorLog;
		this.varEnvironment = new Environment<>();
		this.procEnvironment = new Environment<>();
    }

	public void addScope(){
		this.procEnvironment.addScope();
		this.varEnvironment.addScope();
	}

	public void removeVarScope(){
		this.varEnvironment.removeScope();
	}

    private TypeCheckerQualities StringToType(String str){
	if(str.equals("STRING")){
	    return new TypeCheckerQualities(TypeCheckerQualities.STRING);
	} else if (str.equals("INTEGER")) {
	    return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);
	} else if (str.equals("REAL")){
	    return new TypeCheckerQualities(TypeCheckerQualities.REAL);
	} else if (str.equals("BOOLEAN")) {
	    return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
	} else if (str.equals("VOID")){
	    return new TypeCheckerQualities(TypeCheckerQualities.VOID);
	} else {
	    return new TypeCheckerQualities(TypeCheckerQualities.NA);
	}
    }
    
    @Override
    public void visit(Program program) {
		procEnvironment.addScope();
		varEnvironment.addScope();
		for (Declaration Decl : program.getConstDecls()) {
			Decl.accept(this);
		}

		for(Declaration Decl : program.getVarDecls()){
			Decl.accept(this);
		}

		for(Declaration Decl : program.getProcDecls()){
			Decl.accept(this);
			varEnvironment.removeScope();
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
		TypeCheckerQualities value = valueExpr.acceptResult(this);
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
			TypeCheckerQualities quals = StringToType(type);
			if(quals.containsQualities(TypeCheckerQualities.STRING)){
				errorLog.add("Variable " + id.getLexeme() + "is of invalid type " + type, id.getStart());
			} else if(quals.containsQualities(TypeCheckerQualities.VOID)){
				errorLog.add("Variable " + id.getLexeme() + "is of invalid type " + type, id.getStart());
			} else {
				varEnvironment.addEntry(id.getLexeme(), quals);
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
	    List <ParamaterDeclaration> args = procDecl.getArguments();
		List <TypeCheckerQualities> argTypes = new LinkedList<TypeCheckerQualities>();
	    for(ParamaterDeclaration decl : args){
			decl.accept(this);
			TypeCheckerQualities type = varEnvironment.getEntry(decl.getIdentifier().getLexeme());
			argTypes.add(type);
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
		TypeCheckerQualities myType = StringToType(returnType);
		if(myType.containsQualities(TypeCheckerQualities.NA) && retExp != null){
			myType = retExp.acceptResult(this);
		} else if(myType.containsQualities(TypeCheckerQualities.VOID) && retExp != null){
			TypeCheckerQualities type = retExp.acceptResult(this);
			if(myType != type){
				errorLog.add("Return Expression is not of type " + returnType, procDecl.getStart());
			}
		} else {
			myType = new TypeCheckerQualities(TypeCheckerQualities.VOID);
		}
	    procEnvironment.addEntry(procedureName, new ProcedureTypeEntry(myType, argTypes));
	} else {
	    errorLog.add("Procedure " + procedureName + " within scope already declared", procDecl.getStart());
	}
    }
        
    @Override
    public void visit(ProcedureCall procedureCall) {
	if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
	    TypeCheckerQualities partype = procedureCall.getArguments().get(0).acceptResult(this);
	    if(!partype.containsQualities(TypeCheckerQualities.INTEGER)){
			errorLog.add("Procedure Call PrintInt Exprected an Integer Argument", procedureCall.getStart());
	    }
	} else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
	    TypeCheckerQualities partype = procedureCall.getArguments().get(0).acceptResult(this);
	    if(!partype.containsQualities(TypeCheckerQualities.REAL)){
			errorLog.add("Procedure Call PrintDouble Exprected an Real Argument", procedureCall.getStart());
	    }
	} else if(procedureCall.getProcedureName().getLexeme().equals("PrintString")) {
	    TypeCheckerQualities partype = procedureCall.getArguments().get(0).acceptResult(this);
	    if(partype.containsQualities(TypeCheckerQualities.STRING)){
		errorLog.add("Procedure Call PrintDouble Exprected an String Argument", procedureCall.getStart());
	    }
	} else if(procedureCall.getProcedureName().getLexeme().equals("PrintLn")){
	    //do nothing
	} else {
	    String funcName = procedureCall.getProcedureName().getLexeme();
	    if(procEnvironment.entryExists(funcName)){
		ProcedureTypeEntry pentry = procEnvironment.getEntry(funcName);
		List<TypeCheckerQualities> args = pentry.getArgumentTypes();
		List<Expression> valArgs = procedureCall.getArguments();
		List<TypeCheckerQualities> exprValues = new ArrayList<TypeCheckerQualities>();
		for(Expression expr : valArgs){
		    exprValues.add(expr.acceptResult(this));
		}
		if(args.size() == valArgs.size()){
		    for(int i = 0; i < args.size(); i++){
				TypeCheckerQualities toChangeType = args.get(i);
				TypeCheckerQualities argumentType = exprValues.get(i);
				if(argumentType.containsQualities(TypeCheckerQualities.VOID) || argumentType.containsQualities(TypeCheckerQualities.NA)
				||(argumentType.containsQualities(TypeCheckerQualities.STRING) && toChangeType.missingQualities(TypeCheckerQualities.STRING))
				||(toChangeType.containsQualities(TypeCheckerQualities.STRING) && argumentType.missingQualities(TypeCheckerQualities.STRING))
				||(toChangeType.containsQualities(TypeCheckerQualities.BOOLEAN) && argumentType.missingQualities(TypeCheckerQualities.BOOLEAN))
				||(argumentType.containsQualities(TypeCheckerQualities.BOOLEAN)&& toChangeType.missingQualities(TypeCheckerQualities.BOOLEAN))){
					errorLog.add("In function " + funcName + " type mismatch at parameter " + (i + 1), procedureCall.getStart());
				}
			}
		} else {
		    errorLog.add("Unexpected amount of arguments provided from Caller to Callie in Function " + procedureCall.getProcedureName().getLexeme(), procedureCall.getStart());
		}
	    }
	}
    }	


    @Override
    public void visit(WhileElifBranch whilebranch){
	Expression toCheck = whilebranch.getExpression();
	if(toCheck.acceptResult(this).missingQualities(TypeCheckerQualities.BOOLEAN)){
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
	if(toCheck.acceptResult(this).missingQualities(TypeCheckerQualities.BOOLEAN)){
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
	if(toCheck.acceptResult(this).missingQualities(TypeCheckerQualities.BOOLEAN)){
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
	    TypeCheckerQualities incriment = toMod.acceptResult(this);
	    TypeCheckerQualities target = forbranch.getTargetExpression().acceptResult(this);
	    TypeCheckerQualities curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
	    if(incriment != target || incriment != curvalue){
		errorLog.add("Type Mismatch in head of For Loop", forbranch.getStart());
	    }
	    for(int i = 0; i < toExec.size(); i++){
	       toExec.get(i).accept(this);
	    }
	} else {
	    forbranch.getInitAssignment().accept(this);
	    TypeCheckerQualities target = forbranch.getTargetExpression().acceptResult(this);
	    TypeCheckerQualities curvalue = varEnvironment.getEntry(forbranch.getInitAssignment().getVariableName().getLexeme());
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
			TypeCheckerQualities entry = varEnvironment.getEntry(name);
			TypeCheckerQualities expression = assignment.getVariableValue().acceptResult(this);
			
			if(expression.containsQualities(TypeCheckerQualities.VOID) || expression.containsQualities(TypeCheckerQualities.NA)
				||(expression.containsQualities(TypeCheckerQualities.STRING) && entry.missingQualities(TypeCheckerQualities.STRING))
				||(entry.containsQualities(TypeCheckerQualities.STRING) && expression.missingQualities(TypeCheckerQualities.STRING))
				||(entry.containsQualities(TypeCheckerQualities.BOOLEAN) && expression.missingQualities(TypeCheckerQualities.BOOLEAN))
				||(expression.containsQualities(TypeCheckerQualities.BOOLEAN)&& entry.missingQualities(TypeCheckerQualities.BOOLEAN))){
					errorLog.add("Variable in Assignment " + name + " is of type " + entry + " but expression is of type " + expression, assignment.getStart());
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
    public TypeCheckerQualities visitResult(BinaryOperation binaryOperation) {
		TypeCheckerQualities leftValue = binaryOperation.getLeft().acceptResult(this);
		TypeCheckerQualities rightValue = binaryOperation.getRight().acceptResult(this);
		BinaryOperation.OpType op = binaryOperation.getOperator();
		switch(op){
		case MOD:
			if(leftValue.missingQualities(TypeCheckerQualities.INTEGER) || rightValue.missingQualities(TypeCheckerQualities.INTEGER)){
				errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
				return null;
			} else {
				return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);
			}
		case AND:
			if(leftValue.missingQualities(TypeCheckerQualities.BOOLEAN) || rightValue.missingQualities(TypeCheckerQualities.BOOLEAN)){
				errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
				return null;
			} else {
				return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
			}
		case OR:
			if(leftValue.missingQualities(TypeCheckerQualities.BOOLEAN) || rightValue.missingQualities(TypeCheckerQualities.BOOLEAN)){
				errorLog.add("Type mismatch in binary opperation: " + leftValue + " " + op + " " + rightValue,  binaryOperation.getStart());
				return null;
			} else {
				return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
			}
		default:
			if(op == BinaryOperation.OpType.EQ || op == BinaryOperation.OpType.NE || op == BinaryOperation.OpType.LT || op == BinaryOperation.OpType.LE || op == BinaryOperation.OpType.GT || op == BinaryOperation.OpType.GE){
				return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
			} else if(op == BinaryOperation.OpType.DIV){
				return new TypeCheckerQualities(TypeCheckerQualities.INTEGER);	
			} else if(op == BinaryOperation.OpType.DIVIDE){
				return new TypeCheckerQualities(TypeCheckerQualities.REAL);	
			} else if(leftValue.containsQualities(TypeCheckerQualities.REAL) || rightValue.containsQualities(TypeCheckerQualities.REAL)){
				return new TypeCheckerQualities(TypeCheckerQualities.REAL);
			} else {
				return leftValue;
			}
		}
    }

    @Override
    public TypeCheckerQualities visitResult(FunctionCall funcCall) {
	String funcName = funcCall.getFunctionName().getLexeme();
	if(procEnvironment.entryExists(funcName)){
	    ProcedureTypeEntry pentry = procEnvironment.getEntry(funcName);
	    List<TypeCheckerQualities> args = pentry.getArgumentTypes();
	    List<Expression> valArgs = funcCall.getArguments();
	    List<TypeCheckerQualities> exprValues = new ArrayList<>();
	    for(Expression expr : valArgs){
			exprValues.add(expr.acceptResult(this));
	    }
	    if(args.size() == valArgs.size()){
		for(int i = 0; i < args.size(); i++){
		    TypeCheckerQualities toChangeType = args.get(i);
		    TypeCheckerQualities argumentType = exprValues.get(i);
			if(argumentType.containsQualities(TypeCheckerQualities.VOID) ||argumentType.containsQualities(TypeCheckerQualities.NA)
			||(argumentType.containsQualities(TypeCheckerQualities.STRING) && toChangeType.missingQualities(TypeCheckerQualities.STRING))
			||(toChangeType.containsQualities(TypeCheckerQualities.STRING) && argumentType.missingQualities(TypeCheckerQualities.STRING))
			||(toChangeType.containsQualities(TypeCheckerQualities.BOOLEAN) && argumentType.missingQualities(TypeCheckerQualities.BOOLEAN))
			||(argumentType.containsQualities(TypeCheckerQualities.BOOLEAN)&& toChangeType.missingQualities(TypeCheckerQualities.BOOLEAN))){
				errorLog.add("In function " + funcName + " type mismatch at parameter " + (i + 1), funcCall.getStart());
		    }
		}
	    } else {
			errorLog.add("Unexpected amount of arguments provided from Caller to Callie in Function " + funcName, funcCall.getStart());
	    }
	    TypeCheckerQualities retValue = pentry.getReturnType();
	    return retValue;
	} else {
	    errorLog.add("Couldnt find entry for " + funcName, funcCall.getStart());
	    return null;
	}
    }

    @Override
    public TypeCheckerQualities visitResult(UnaryOperation unaryOperation) {
		TypeCheckerQualities value = unaryOperation.getExpression().acceptResult(this);
		if (value.containsQualities(TypeCheckerQualities.BOOLEAN) || value.containsQualities(TypeCheckerQualities.INTEGER) || value.containsQualities(TypeCheckerQualities.REAL)){
			return value;
		} else {
			errorLog.add("Invalid Type for Unary Operation " + value , unaryOperation.getStart());
			return null;
		}
    }
    
    @Override
    public TypeCheckerQualities visitResult(Identifier ident){
		if(!varEnvironment.entryExists(ident.getLexeme())){
			errorLog.add("Couldnt find entry for " + ident.getLexeme(), ident.getStart());
		}
		return varEnvironment.getEntry(ident.getLexeme());
    }

    @Override
    public TypeCheckerQualities visitResult(NumValue numValue){
		String lexeme = numValue.getLexeme();
		int result = 0;
		if(lexeme.startsWith("-")){
			result |= TypeCheckerQualities.NEG;
		}
		if(lexeme.contains(".")){
			result |= TypeCheckerQualities.REAL;
		} else {
			result |= TypeCheckerQualities.INTEGER;
		}

		return new TypeCheckerQualities(result);
    }

    @Override
    public TypeCheckerQualities visitResult(BoolValue boolValue){
		return new TypeCheckerQualities(TypeCheckerQualities.BOOLEAN);
    }

    @Override
    public TypeCheckerQualities visitResult(StrValue strValue){
		return new TypeCheckerQualities(TypeCheckerQualities.STRING);
    }

	@Override
	public void visit(ParamaterDeclaration declaration) {
		Identifier id = declaration.getIdentifier();
		String type = declaration.getType().getLexeme();
		if(!varEnvironment.inScope(id.getLexeme())){
			TypeCheckerQualities qual = StringToType(type);
			if(qual.containsQualities(TypeCheckerQualities.STRING)){
				errorLog.add("Variable " + id.getLexeme() + "is of invalid type " + type, id.getStart());
			} else if(qual.containsQualities(TypeCheckerQualities.VOID)){
				errorLog.add("Variable " + id.getLexeme() + "is of invalid type " + type, id.getStart());
			} else {
				varEnvironment.addEntry(id.getLexeme(), qual);
			}
		} else {
			errorLog.add("Multiple Declaration of Variable " + id.getLexeme(), id.getStart());
		}
	}

	@Override
	public void visit(Library library) {
		procEnvironment.addScope();
		varEnvironment.addScope();
		for (Declaration Decl : library.getConstDecls()) {
			Decl.accept(this);
		}

		for(Declaration Decl : library.getVarDecls()){
			Decl.accept(this);
		}

		for(Declaration Decl : library.getProcDecls()){
			Decl.accept(this);
			varEnvironment.removeScope();
		}
	}

	@Override
	public void visit(Asm asm) {
		List<String> actualParam = asm.getParamaters();
		Pattern pat = Pattern.compile("%a|%r", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pat.matcher(asm.getInlineAssembly());
		int found = 0;
		int startIndex = 0;
		while(matcher.find(startIndex)){
			found++;
			startIndex = matcher.start() + 1;
		}
		
		if(actualParam.size() != found){
			errorLog.add("Error: Expected " + found + " Inline Assembly paramaters " + " but found " + actualParam.size(), asm.getStart());
		}
	}
}
