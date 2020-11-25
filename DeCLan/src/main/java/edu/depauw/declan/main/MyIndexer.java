package edu.depauw.declan.main;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.ProcedureDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.IfElifBranch;
import edu.depauw.declan.common.ast.WhileElifBranch;
import edu.depauw.declan.common.ast.ForBranch;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.ElseBranch;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Branch;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.BoolValue;
import edu.depauw.declan.common.ast.StrValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.FunctionCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.ForAssignment;
import edu.depauw.declan.common.ast.Environment;

import java.lang.Number;
import java.lang.Math;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


import static edu.depauw.declan.common.MyIO.*;

/**
 * This is an implementation of the ASTVisitor that encapsulates the algorithm
 * "print out the abstract syntax tree in postorder, suitable for execution on a
 * simple stack machine." It is used for Project 2 of CSC 426.
 * 
 * @author bhoward
 */
public class MyIndexer implements ASTVisitor {
	/**
	 * The environment is used to record the bindings of numeric values to
	 * constants.
	 */
        private Environment<String, Position> varEnvironment;
        private Environment<String, Position> procEnvironment;
        private boolean ParTrue;

	/**
	 * Construct a default PostfixPrintVisitor that writes to the console.
	 */
	public MyIndexer() {
	    this.varEnvironment = new Environment<>();
	    this.procEnvironment = new Environment<>();
	    this.ParTrue = false;
	}

        private static void printIndexMessage(String Descriptor, Position position, String Rest){
	  OUT(Descriptor + " ( " + position.toString() + " ) " + Rest);
        }

        private static String ifHexToInt(String lexeme){
	  if(lexeme.charAt(0) == '0' && lexeme.length() > 1 && !lexeme.contains(".")){ //is it a hex number
	    int value = (int)Long.parseLong(lexeme.substring(1, lexeme.length() - 1), 16);  
	    return ("" + value);
	  } else {
	    return lexeme; //else returninput it is fine
	  }
	}

	@Override
	public void visit(Program program) {
		// Process all of the constant declarations
	        varEnvironment.addScope();
		procEnvironment.addScope();
		for (Declaration Decl : program.getDecls()) {
			Decl.accept(this);
		}

		// Process all of the statements in the program body
		for (Statement statement : program.getStatements()) {
			statement.accept(this);
		}
		procEnvironment.removeScope();
		varEnvironment.removeScope();
	}

        @Override
	public void visit(VariableDeclaration varDecl){
	  Identifier id = varDecl.getIdentifier();
	  Identifier type = varDecl.getType();
	  varEnvironment.addEntry(id.getLexeme(), id.getStart());
	  if(ParTrue){
	    printIndexMessage("DECL", id.getStart(), "PARAM " + id.getLexeme());
	  } else {
	    printIndexMessage("DECL", id.getStart(), "VAR " + id.getLexeme());
	  }
	}
   
	@Override
	public void visit(ConstDeclaration constDecl) {
	  Identifier id = constDecl.getIdentifier();
	  varEnvironment.addEntry(id.getLexeme(), id.getStart());
	  printIndexMessage("DECL", id.getStart(), "CONST " + id.getLexeme());
	}

        @Override
	public void visit(ProcedureDeclaration procDecl) {
	  Identifier procedName = procDecl.getProcedureName();
	  String procedureName = procedName.getLexeme();
          List <VariableDeclaration> args = procDecl.getArguments();
	  Identifier retType = procDecl.getReturnType();
          String returnType = retType.getLexeme();
          List <Declaration> localVars = procDecl.getLocalVariables();
          List <Statement> Exec = procDecl.getExecutionStatements();
          Expression retExp = procDecl.getReturnStatement();
          procEnvironment.addEntry(procedureName, procedName.getStart());
	  printIndexMessage("DECL", procedName.getStart(), "PROC " + procedureName);
	  ParTrue = true;
	  for(int i = 0; i < args.size(); i++){
	    args.get(i).accept(this);
	  }
	  ParTrue = false;
	  if(!returnType.equals("VOID")){
	    printIndexMessage("DECL", retType.getStart(), "TYPE " + returnType);
	  }
	  for(int i = 0; i < localVars.size(); i++){
	    localVars.get(i).accept(this);
	  }
	  for(int i = 0; i < Exec.size(); i++){
	    Exec.get(i).accept(this);
	  }
	  if(!returnType.equals("VOID")){
	    retExp.accept(this);
	  }
	}
     
        @Override
	public void visit(IfElifBranch ifs){
	  Expression exp = ifs.getExpression();
	  List<Statement> toDo = ifs.getExecStatements();
	  Branch toBranch = ifs.getNextBranch();
	  exp.accept(this);
	  for(int i = 0; i < toDo.size(); i++){
	    toDo.get(i).accept(this);
	  }
	  if(toBranch != null){
	    toBranch.accept(this);
	  }
	}

        @Override
	public void visit(WhileElifBranch wfs){
	  Expression exp = wfs.getExpression();
	  List<Statement> toDo = wfs.getExecStatements();
	  Branch toBranch = wfs.getNextBranch();
	  exp.accept(this);
	  for(int i = 0; i < toDo.size(); i++){
	    toDo.get(i).accept(this);
	  }
	  if(toBranch != null){
	    toBranch.accept(this);
	  }
	}

        @Override
	public void visit(ElseBranch es){
	  List<Statement> toDo = es.getExecStatements();
	  for(int i = 0; i < toDo.size(); i++){
	    toDo.get(i).accept(this);
	  }
	}

        @Override
	public void visit(RepeatBranch rpb){
	  Expression exp = rpb.getExpression();
	  List<Statement> toDo = rpb.getExecStatements();
	  exp.accept(this);
	  for(int i = 0; i < toDo.size(); i++){
	    toDo.get(i).accept(this);
	  }
	}

        @Override
	public void visit(ForBranch branch){
	  ForAssignment fassign = branch.getInitAssignment();
	  Expression texp = branch.getTargetExpression();
	  Expression mexp = branch.getModifyExpression();
          List<Statement> toDo = branch.getExecStatements();
	  fassign.accept(this);
	  texp.accept(this);
	  mexp.accept(this);
	  for(int i = 0; i < toDo.size(); i++){
	    toDo.get(i).accept(this);
	  }
	}

        @Override
	public void visit(ForAssignment assignment) {
	  Identifier id = assignment.getVariableName();
	  Expression exp = assignment.getVariableValue();
	  id.accept(this);
	  exp.accept(this);
	}

        @Override
	public void visit(Assignment assignment){
	  Identifier id = assignment.getVariableName();
	  Expression exp = assignment.getVariableValue();
	  id.accept(this);
	  exp.accept(this);
	}
  
	@Override
	public void visit(ProcedureCall procedureCall){
	  Identifier id = procedureCall.getProcedureName();
	  List<Expression> params = procedureCall.getArguments();
	  if(procEnvironment.getEntry(id.getLexeme()) != null){
	    printIndexMessage("USE", id.getStart(), id.getLexeme() + ", declared at " + procEnvironment.getEntry(id.getLexeme()).toString());
	  }
	  for(int i = 0; i < params.size(); i++){
	    params.get(i).accept(this);
	  }
	}

	@Override
	public void visit(EmptyStatement emptyStatement) {
		// Do nothing
	}
        
	@Override
	public void visit(UnaryOperation unaryOperation) {
		unaryOperation.getExpression().accept(this);
	}

	@Override
	public void visit(BinaryOperation binaryOperation){
		binaryOperation.getLeft().accept(this);
		binaryOperation.getRight().accept(this);
	}

	@Override
	public void visit(NumValue numValue) {
	    //do nothing
	}

        @Override
	public void visit(BoolValue numValue) {
	    //do nothing
	}

        @Override
	public void visit(StrValue numValue) {
	    //do nothing
	}

        @Override
	public void visit(FunctionCall fcall) {
	  Identifier id = fcall.getFunctionName();
	  List<Expression> params = fcall.getArguments();
	  printIndexMessage("USE", id.getStart(), id.getLexeme() + ", declared at " + procEnvironment.getEntry(id.getLexeme()).toString());
	  for(int i = 0; i < params.size(); i++){
	    params.get(i).accept(this);
	  }
	}

	@Override
	public void visit(Identifier id){
	  printIndexMessage("USE", id.getStart(), id.getLexeme() + ", declared at " + varEnvironment.getEntry(id.getLexeme()).toString());
	}
}
