package edu.depauw.declan.common.ast;

import java.util.List;

import edu.depauw.declan.common.Position;

import java.lang.String;
import java.lang.StringBuilder;

/**
 * This class allows for repeat statements which act like the inverse of a do while loop
 * They run UNTIL the condition is true
 * @author Jacob Bauer
 */
public class RepeatBranch extends Branch implements Statement {
    private final Expression toCheck;
    
    public RepeatBranch(Position start, List<Statement> toExecute, Expression toCheck){
      super(start, toExecute);
      this.toCheck = toCheck;
    }
  
    public List<Statement> getExecStatements(){
      return super.getExecStatements();
    }

    public Expression getExpression(){
      return toCheck;
    }

    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("REPEAT:\n");
      List<Statement> stats = getExecStatements();
      for(int i = 0; i < stats.size(); i++){
	mystring.append("\tStatement " + i + ": ");
	mystring.append(stats.get(i).toString());
	mystring.append('\n');
      }
      mystring.append("UNTIL ");
      mystring.append(getExpression().toString());
      mystring.append('\n');
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
