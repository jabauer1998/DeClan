package edu.depauw.declan.common.ast;

import java.util.List;
import edu.depauw.declan.common.Position;

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
    public void accept(ASTVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public <R> R acceptResult(StatementVisitor<R> visitor) {
      return visitor.visitResult(this);
    }
}
