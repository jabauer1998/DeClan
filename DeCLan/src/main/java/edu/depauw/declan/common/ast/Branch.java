package edu.depauw.declan.common.ast;

import java.util.List;
import edu.depauw.declan.common.Position;

public abstract class Branch extends AbstractASTNode {
    private List<Statement> toExecute;
  
    public Branch(Position start, List<Statement> toExecute){
        super(start);
	this.toExecute = toExecute;
    }
  
    public List<Statement> getExecStatements(){
	return toExecute;
    }
  
}
