package edu.depauw.declan.main;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.ExpressionVisitor;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.UnaryOperation;
import edu.depauw.declan.common.ast.Statement;

import java.lang.Number;
import java.lang.Math;
import java.util.Map;
import java.util.HashMap;

import static edu.depauw.declan.common.MyIO.*;

public class MyInterpreter implements ASTVisitor, ExpressionVisitor<Number> {
	private ErrorLog errorLog;
        private Map<String, String> environment;
	// TODO declare any data structures needed by the interpreter
	
	public MyInterpreter(ErrorLog errorLog) {
		this.errorLog = errorLog;
		this.environment = new HashMap<>();
	}

	@Override
	public void visit(Program program) {
	        for (ConstDeclaration constDecl : program.getConstDecls()) {
			constDecl.accept(this);
		}
		for (Statement statement : program.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(ConstDeclaration constDecl) {
	        Identifier id = constDecl.getIdentifier();
		NumValue num = constDecl.getNumber();
		environment.put(id.getLexeme(), num.getLexeme());
	}

	@Override
	public void visit(ProcedureCall procedureCall) {
		if (procedureCall.getProcedureName().getLexeme().equals("PrintInt")) {
			Number value = procedureCall.getArgument().acceptResult(this);
			OUT("" + value.intValue());
		} else if (procedureCall.getProcedureName().getLexeme().equals("PrintDouble")) {
			Number value = procedureCall.getArgument().acceptResult(this);
			OUT("" + value.doubleValue());
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
		String lexeme = environment.getOrDefault(identifier.getLexeme(), "0");
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
}
