package edu.depauw.declan.main;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.IfStatement;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.VariableEntry;
import edu.depauw.declan.common.ast.Environment;
import edu.depauw.declan.common.ast.BooleanOperation;
import java.lang.Number;
import java.lang.Math;
import java.util.Map;
import java.util.HashMap;

import static edu.depauw.declan.common.MyIO.*;

public class MyInterpreter implements ASTVisitor, ExpressionVisitor<Number> {
	private ErrorLog errorLog;
        private Environment <VariableEntry> varEnvironment;
	// TODO declare any data structures needed by the interpreter
	
	public MyInterpreter(ErrorLog errorLog) {
		this.errorLog = errorLog;
		this.varEnvironment = new Environment<>();
	}

	@Override
	public void visit(Program program) {
	        varEnvironment.addScope();
	        for (Declaration Decl : program.getDecls()) {
			Decl.accept(this);
		}
		for (Statement statement : program.getStatements()) {
			statement.accept(this);
		}
		varEnvironment.removeScope();
	}

	@Override
	public void visit(ConstDeclaration constDecl) {
	        Identifier id = constDecl.getIdentifier();
		NumValue num = constDecl.getNumber();
		varEnvironment.addEntry(id.getLexeme(), new VariableEntry("CONST", num.getLexeme()));
	}

        @Override
	public void visit(VariableDeclaration varDecl) {
	        Identifier id = varDecl.getIdentifier();
		Identifier type = varDecl.getType();
		varEnvironment.addEntry(id.getLexeme(), new VariableEntry(type.getLexeme()));
	}

        @Override
	public void visit(IfStatement ifs){
	  
	}
        
	@Override
	public void visit(ProcedureCall procedureCall) {
		if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
		        Number value = procedureCall.getArguments().get(0).acceptResult(this);
			OUT("" + value.intValue());
		} else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
		        Number value = procedureCall.getArguments().get(0).acceptResult(this);
			OUT("" + value.doubleValue());
		}
	}
        
        @Override
	public void visit(Assignment assignment) {
	    String name = assignment.getVariableName().getLexeme();
	    if(varEnvironment.entryExists(name)){
		VariableEntry entry = varEnvironment.findEntry(name);
		if(!entry.getType().equals("CONST")){
		    if(entry.getType().equals("REAL")){
			Number value = assignment.getVariableValue().acceptResult(this);
			String newValue = "" + value.doubleValue();
			entry.setValue(newValue);
		    } else if(entry.getType().equals("INTEGER")){
			Number value = assignment.getVariableValue().acceptResult(this);
			String newValue = "" + value.intValue();
			entry.setValue(newValue);
		    }
		} else {
		    FATAL("Variable " + assignment.getVariableName().getLexeme() + " at " + assignment.getVariableName().getStart() + " declared as const");
		}
	    } else {
		FATAL("Undeclared Variable " + assignment.getVariableName().getLexeme() + " at " + assignment.getVariableName().getStart());
	    }
	}
        @Override
	public void visit(EmptyStatement emptyStatement) {
		// TODO Auto-generated method stub

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
	public void visit(Identifier identifier) {
		// Not used
	}
        
	@Override
	public Number visitResult(BinaryOperation binaryOperation) {
		Number leftvalue = binaryOperation.getLeft().acceptResult(this);
		Number rightvalue = binaryOperation.getRight().acceptResult(this);
		if(leftvalue instanceof Double || rightvalue instanceof Double)
		{
		    switch (binaryOperation.getOperator()) {
			case PLUS:
			    return leftvalue.doubleValue() + rightvalue.doubleValue();
			case MINUS:
			    return leftvalue.doubleValue() - rightvalue.doubleValue();
			case TIMES:
			    return leftvalue.doubleValue() * rightvalue.doubleValue();
			case DIVIDE:
			    return leftvalue.doubleValue() / rightvalue.doubleValue();
		    }
		} else {
		    switch (binaryOperation.getOperator()) {
			case PLUS:
			    return leftvalue.intValue() + rightvalue.intValue();
			case MINUS:
			    return leftvalue.intValue() - rightvalue.intValue();
			case TIMES:
			    return leftvalue.intValue() * rightvalue.intValue();
			case DIV:
			    return leftvalue.intValue() / rightvalue.intValue();
		        case MOD:
			    return leftvalue.intValue() % rightvalue.intValue();
		    }
		}
		return null;
	}

	@Override
	public Number visitResult(UnaryOperation unaryOperation) {
		Number value = unaryOperation.getExpression().acceptResult(this);
		if(value instanceof Double)
		{
		    switch (unaryOperation.getOperator()){
		    case PLUS:
			return value.doubleValue();
		    case MINUS:
			return -value.doubleValue();
		    }
		} else {
		    switch (unaryOperation.getOperator()){
		    case PLUS:
			return value.intValue();
		    case MINUS:
			return -value.intValue();
		    }
		}
		return null;
	}

        //Checks to see if number has exponent E and is not Hex
        private static int checkE(String s){
	    boolean notHex = true;
	    int idex = -1;
	    for(int i = 0; i < s.length(); i++){
		if(s.charAt(i) == 'E'){
		    idex = i;
		}
	    }
	    if(s.charAt(s.length() - 1) == 'H'){
		notHex = false;
	    }
	    return (notHex) ? idex : -1;
        }

        private static String ConvertEstring(String s, int Eindex){
	    double beforeE = Double.parseDouble(s.substring(0, Eindex));
	    int Exponent = Integer.parseInt(s.substring(Eindex + 1, s.length()));
	    return ("" + (beforeE * Math.pow(10, Exponent)));
        }
    
	@Override
	public Number visitResult(Identifier identifier){
	        String lexeme = varEnvironment.findEntry(identifier.getLexeme()).getValue();
		int Eindex = checkE(lexeme);
		if(Eindex > 0){
		    if(lexeme.contains(".")){
			return Double.parseDouble(ConvertEstring(lexeme, Eindex));
		    } else {
			return Integer.parseInt(ConvertEstring(lexeme, Eindex));
		    }
		} else {
		    if(lexeme.contains(".")){
			return Double.parseDouble(lexeme);
		    } else {
			return Integer.parseInt(lexeme);
		    }
		}
	}

	@Override
	public Number visitResult(NumValue numValue){
		String lexeme = numValue.getLexeme();
		int Eindex = checkE(lexeme);
		if(Eindex > 0){
		    if(lexeme.contains(".")){
			return Double.parseDouble(ConvertEstring(lexeme, Eindex));
		    } else {
			return Integer.parseInt(ConvertEstring(lexeme, Eindex));
		    }
		} else {
		    if(lexeme.contains(".")){
			return Double.parseDouble(lexeme);
		    } else {
			return Integer.parseInt(lexeme);
		    }
		}
	}

        public Number visitResult(BooleanOperation op){
	    return null;
        }

        @Override
	public void visit(BooleanOperation bool){

	}
}
