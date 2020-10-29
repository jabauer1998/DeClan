package edu.depauw.declan.common.ast;

import java.lang.String;
import java.util.List;

import static edu.depauw.declan.common.MyIO.*;

public class ProcedureEntry{
    
    private ProcedureDeclaration PD;
    
    public ProcedureEntry(ProcedureDeclaration declaration){
      this.PD = declaration;
    }
    
    public List<Statement> getExecList(){
      return PD.getExecutionStatements();
    }

    public List<VariableDeclaration> getArguments(){
      return PD.getArguments();
    }

    public List<Declaration> getLocalVariables(){
      return PD.getLocalVariables();
    }

    public Expression getReturnStatement(){
      return PD.getReturnStatement();
    }

    public ProcType getType(){
        if(PD.getReturnType() == null){
	    return ProcType.VOID;
        } else if(PD.getReturnType().getLexeme().equals("REAL")){
	    return ProcType.REAL;
	} else if(PD.getReturnType().getLexeme().equals("INTEGER")){
	    return ProcType.INTEGER;
	} else if(PD.getReturnType().getLexeme().equals("BOOLEAN")){
	    return ProcType.BOOLEAN;
	} else {
	    FATAL("Unknown Type " + PD.getReturnType().getLexeme());
	    return null;
	}
    }

    public static enum ProcType{
      INTEGER, BOOLEAN, REAL, VOID
    }
    
}
