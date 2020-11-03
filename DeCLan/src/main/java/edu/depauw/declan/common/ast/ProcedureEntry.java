package edu.depauw.declan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;

import static edu.depauw.declan.common.MyIO.*;

public class ProcedureEntry{

    private final ProcType Type;
    private final List<VariableDeclaration> arguments;
    private final List<Declaration> local;
    private final List<Statement> ExecutionStats;
    private final Expression ReturnStatement;
  
    public ProcedureEntry(List<VariableDeclaration> arguments, String type, List<Declaration> local, List<Statement> ExecutionStats, Expression ReturnStatement){
        if(arguments == null){
	  this.arguments = new ArrayList<>();
        } else {
	  this.arguments = arguments;
        }
        if(type.equals("REAL")){
	    Type = ProcType.REAL;
	} else if(type.equals("INTEGER")){
	    Type = ProcType.INTEGER;
	} else if(type.equals("BOOLEAN")){
	    Type = ProcType.BOOLEAN;
	} else {
	    Type = ProcType.VOID;
	}
	this.local = local;
	if(ExecutionStats == null){
	  this.ExecutonStats = new ArrayList<>();
	} else {
	  this.ExecutionStats = ExecutionStats;
	}
	this.ReturnStatement = ReturnStatement;
    }
  
    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("TYPE: ");
      mystring.append(typeToString(getType));
      mystring.append(" ARGUMENTS: ");
      mystring.append("( ");
      List<VariableDeclaration> arguments = getArguments();
      for(int i = 0; i < arguments.size(); i++){
	mystring.append(argumets.get(i).toString());
	mystring.append(' ');
      }
      mystring.append(")\n");
      List<Declaration> local = getLocalVariables();
      for(int i = 0; i < local.size(); i++){
	mystring.append('\t');
	mystring.append(local.get(i).toString());
	mystring.append('\n');
      }
      List<Statement> exec = getExecList();
      for(int i = 0; i < exec.size(); i++){
	mystring.append("\tStatement " + i + ": ");
	mystring.append(exec.get(i));
	mystring.append('\n');
      }
      if(getReturnStatement() != null){
	mystring.append("\treturn ");
	mystring.append(getReturnStatement().toString());
	mystring.append(";\n");
      }
      return mystring.toString();
    }
    
    public List<Statement> getExecList(){
      return ExecutionStats;
    }

    public List<VariableDeclaration> getArguments(){
      return arguments;
    }

    public List<Declaration> getLocalVariables(){
      return local;
    }

    public Expression getReturnStatement(){
      return ReturnStatement;
    }

    public ProcType getType(){
      return Type;
    }

    public static enum ProcType{
      INTEGER, BOOLEAN, REAL, VOID
    }

    private static String typeToString(ProcType proc){
      if(proc == INTEGER){
	return "int";
      } else if (proc == BOOLEAN){
	return "bool";
      } else if (proc == REAL){
	return "double";
      } else if (proc == VOID){
	return "void";
      } else {
	FATAL("Unknown return type value: " + (int)proc);
	return "";
      }
    }
}
