package edu.depauw.declan.common.ast;

import edu.depauw.declan.common.Position;

public class Assignment extends AbstractASTNode implements Statement {
    private final Identifier variableName;
    private final Expression variableValue;
    
    public Assignment(Position start, Identifier variableName, Expression variableValue){
	super(start);
	this.variableName = variableName;
	this.variableValue = variableValue;
    }

    public Identifier getVariableName() {
	    return variableName;
    }

    public Expression getVariableValue(){
	    return variableValue;
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
