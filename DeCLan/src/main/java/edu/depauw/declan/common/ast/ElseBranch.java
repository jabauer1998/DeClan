package edu.depauw.declan.common.ast;

import java.util.List;
import java.lang.StringBuilder;
import java.lang.String;
import edu.depauw.declan.common.Position;


public class ElseBranch extends Branch implements Statement {
  
    public ElseBranch(Position start, List<Statement> toExecute){
        super(start, toExecute);
    }
  
    public List<Statement> getExecStatements(){
       return super.getExecStatements();
    }
  
    @Override
    public String toString(){
      StringBuilder mystring;
      mystring.append("ELSE:\n");
      List<Statement> stat = getExecStatements();
      for(int i = 0; i < stat.size(); i++){
	mystring.append("\tStatement " + i + " = ");
	mystring.append(stat.get(i).toString());
	mystring.append('\n');
      }
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
