package edu.depauw.declan.common.ast;

import java.util.List;
import edu.depauw.declan.common.Position;

public class ElseBranch extends Branch implements Statement {
  
    public ElseBranch(Position start, List<Statement> toExecute){
        super(start, toExecute);
    }
  
    public List<Statement> getExecStatements(){
       return super.getExecStatements();
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
