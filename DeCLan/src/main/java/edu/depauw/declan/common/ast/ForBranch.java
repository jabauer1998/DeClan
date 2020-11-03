package edu.depauw.declan.common.ast;

import java.util.List;
import java.lang.String;
import java.lang.StringBuilder;
import edu.depauw.declan.common.Position;

public class ForBranch extends Branch implements Statement {
    private final ForAssignment initAssign;
    private final Expression toCheck;
    private final Expression toMod;
    
    public ForBranch(Position start, ForAssignment initAssign, Expression toCheck, Expression toMod, List<Statement> toExecute){
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

    public ForAssignment getInitAssignment(){
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
