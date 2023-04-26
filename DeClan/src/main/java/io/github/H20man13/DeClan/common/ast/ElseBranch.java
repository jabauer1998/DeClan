package io.github.H20man13.DeClan.common.ast;

import java.util.List;
import java.lang.StringBuilder;
import java.lang.String;
import io.github.H20man13.DeClan.common.Position;

/**
 * This is the Else AST visitor class that represents the else branch in the if statement
 * @author Jacob Bauer
*/
public class ElseBranch extends Branch implements Statement {
  
    public ElseBranch(Position start, List<Statement> toExecute){
        super(start, toExecute);
    }
  
    public List<Statement> getExecStatements(){
       return super.getExecStatements();
    }
  
    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("ELSE:\n");
      List<Statement> stat = getExecStatements();
      for(int i = 0; i < stat.size(); i++){
	mystring.append("\tStatement " + i + " = ");
	mystring.append(stat.get(i).toString());
	mystring.append('\n');
      }
      return mystring.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public <R> R acceptResult(StatementVisitor<R> visitor) {
      return visitor.visitResult(this);
    }
    
}
