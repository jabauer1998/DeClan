package edu.depauw.declan.common.ast;

import java.util.List;
import edu.depauw.declan.common.Position;

public class IfStatement extends AbstractASTNode implements Statement {
    private final List <IfElseBlock> toVerifyAndExecute;
  
    public IfStatement(Position start, List<IfElseBlock> toVerifyAndExecute){
	super(start);
	this.toVerifyAndExecute = toVerifyAndExecute;
    }
  
    public List<IfElseBlock> getIfStatement(){
	return toVerifyAndExecute;
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
