package declan.frontend.ast;

import declan.utils.position.Position;

public class ElementAssignment extends AbstractASTNode implements Statement{
    private final Identifier variableName;
    private final Expression variableValue;
    private final Expression variableIndex;
    
    public ElementAssignment(Position start, Identifier variableName, Expression variableIndex, Expression variableValue){
	super(start);
	this.variableName = variableName;
	this.variableValue = variableValue;
	this.variableIndex = variableIndex;
    }

    public Identifier getVariableName() {
	    return variableName;
    }

    public Expression getVariableValue(){
	    return variableValue;
    }

    public Expression getVariableIndex(){
	return variableIndex;
    }

    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append(getVariableName().toString());
      mystring.append('[');
      mystring.append(variableIndex.toString());
      mystring.append(']');
      mystring.append(" = ");
      mystring.append(getVariableValue().toString());
      mystring.append('\n');
      return mystring.toString();
    }
    
    public boolean isIncriment() {
    	if(!(variableValue instanceof Identifier))
    		return variableValue.containsIdentifier(variableName.getLexeme());
    	return false;
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
