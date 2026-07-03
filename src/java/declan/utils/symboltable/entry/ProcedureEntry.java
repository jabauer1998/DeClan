package declan.utils.symboltable.entry;

import static declan.utils.MyIO.*;

import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;

import declan.utils.Copyable;
import declan.utils.position.Position;
import declan.frontend.ast.Declaration;
import declan.frontend.ast.Expression;
import declan.frontend.ast.ParamaterDeclaration;
import declan.frontend.ast.Statement;
import declan.frontend.ast.VariableDeclaration;

import java.util.ArrayList;

public class ProcedureEntry implements Copyable<ProcedureEntry>{
    private final List<ParamaterDeclaration> arguments;
    private final List<Declaration> local;
    private final List<Statement> ExecutionStats;
    private final Expression ReturnStatement;
  
    public ProcedureEntry(List<ParamaterDeclaration> arguments, List<Declaration> local, List<Statement> ExecutionStats, Expression ReturnStatement){
        if(arguments == null){
	     arguments = new ArrayList<>();
        }
	this.arguments = arguments;
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

    @Override
    public ProcedureEntry copy() {
      return new ProcedureEntry(arguments, local, ExecutionStats, ReturnStatement);
    }
}


