package edu.depauw.declan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;

import java.util.List;

import edu.depauw.declan.common.Position;

/**
 * This AST class allows for while loops
 * in DeClan you can actually branch with your loops like if statements
 * (Example)While(some expr) - Do this... Elsif - Do this ... etc ...
 * @author Jacob Bauer
 */
public class WhileElifBranch extends Branch implements Statement {
    private final Expression toCheck;
    private final Branch branchTo;
    private static Boolean firstwhile = true;
    
    public WhileElifBranch(Position start, Expression toCheck, List<Statement> toExecute, Branch branchTo){
      super(start, toExecute);
      this.toCheck = toCheck;
      this.branchTo = branchTo;
    }
  
    public List<Statement> getExecStatements(){
      return super.getExecStatements();
    }

    public Expression getExpression(){
      return toCheck;
    }

    public Branch getNextBranch(){
      return branchTo;
    }

    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      if(firstwhile){
        mystring.append("WHILE => ");
	firstwhile = false;
      } else {
	mystring.append("ELIF WHILE => ");
      }
      mystring.append(getExpression().toString() + ":\n");
      List<Statement> stat = getExecStatements();
      for(int i = 0; i < stat.size(); i++){
	mystring.append("\tStatement " + i + " = ");
	mystring.append(stat.get(i).toString());
	mystring.append('\n');
      }
      if(getNextBranch() != null){
	mystring.append(getNextBranch().toString());
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
