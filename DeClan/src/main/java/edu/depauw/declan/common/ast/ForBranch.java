package edu.depauw.declan.common.ast;

import java.util.List;

import edu.depauw.declan.common.Position;

import java.lang.String;
import java.lang.StringBuilder;

/**
 * This is the ast class that allows for for Loops in the Declan Language
 * @author Jacob Bauer
 */
public class ForBranch extends Branch implements Statement {
    private final Assignment initAssign;
    private final Expression toCheck;
    private final Expression toMod;
    
    public ForBranch(Position start, Assignment initAssign, Expression toCheck, Expression toMod, List<Statement> toExecute){
      super(start, toExecute);
      this.toCheck = toCheck;
      this.initAssign = initAssign;
      this.toMod = toMod;
    }
  
    public List<Statement> getExecStatements(){
      return super.getExecStatements();
    }

    public Expression getTargetExpression(){
      return toCheck;
    }

    public Expression getModifyExpression(){
      return toMod;
    }

    public Assignment getInitAssignment(){
      return initAssign;
    }
  
    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("FOR ");
      mystring.append(initAssign.toString());
      mystring.append(", ");
      mystring.append(getTargetExpression());
      if(toMod != null){
	mystring.append(", ");
	mystring.append(getModifyExpression().toString());
      }
      mystring.append(":\n");
      List<Statement> toExecc = getExecStatements();
      for(int i = 0; i < toExecc.size(); i++){
	mystring.append("\t Statement " + (i + 1));
	mystring.append(": ");
	mystring.append(toExecc.get(i).toString());
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
