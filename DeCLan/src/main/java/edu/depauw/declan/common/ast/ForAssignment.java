package edu.depauw.declan.common.ast;

import java.lang.String;
import edu.depauw.declan.common.Position;

public class ForAssignment extends AbstractASTNode implements Statement {
    private final Assignment assignment;
    
    public ForAssignment(Assignment assignment){
        super(assignment.getStart());
	this.assignment = assignment;
    }

    public Identifier getVariableName() {
      return assignment.getVariableName();
    }

    public Expression getVariableValue(){
      return assignment.getVariableValue();
    }

    public String toString(){
      return assignment.toString();
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
