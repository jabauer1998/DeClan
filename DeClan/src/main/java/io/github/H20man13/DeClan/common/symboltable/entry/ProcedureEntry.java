package io.github.H20man13.DeClan.common.symboltable.entry;

import static io.github.H20man13.DeClan.main.MyIO.*;

import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;

import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.ParamaterDeclaration;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.Copyable;

import java.util.ArrayList;

public class ProcedureEntry implements Copyable<ProcedureEntry>{

    private final ProcType Type;
    private final String typeStr;
    private final List<ParamaterDeclaration> arguments;
    private final List<Declaration> local;
    private final List<Statement> ExecutionStats;
    private final Expression ReturnStatement;
  
    public ProcedureEntry(List<ParamaterDeclaration> arguments, String type, List<Declaration> local, List<Statement> ExecutionStats, Expression ReturnStatement){
        if(arguments == null){
	        arguments = new ArrayList<>();
        }
        this.typeStr = type;
	      this.arguments = arguments;
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
          ExecutionStats = new ArrayList<>();
        }
        this.ExecutionStats = ExecutionStats;
        this.ReturnStatement = ReturnStatement;
    }
  
    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("TYPE: ");
      mystring.append(typeToString(getType()));
      mystring.append(" ARGUMENTS: ");
      mystring.append("( ");
      List<ParamaterDeclaration> argments = getArguments();
      for(int i = 0; i < argments.size(); i++){
        mystring.append(argments.get(i).toString());
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
        mystring.append(exec.get(i).toString());
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

    public List<ParamaterDeclaration> getArguments(){
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
      if(proc == ProcType.INTEGER){
	return "int";
      } else if (proc == ProcType.BOOLEAN){
	return "bool";
      } else if (proc == ProcType.REAL){
	return "double";
      } else {
	return "void";
      }
    }

    @Override
    public ProcedureEntry copy() {
      return new ProcedureEntry(arguments, typeStr, local, ExecutionStats, ReturnStatement);
    }
}
