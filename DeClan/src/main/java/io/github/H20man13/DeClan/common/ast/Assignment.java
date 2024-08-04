package io.github.H20man13.DeClan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;

import io.github.H20man13.DeClan.common.position.Position;

/**
 * The assignment class allows the programmer to execute Assignments in Declan
 * The Syntax is as followed: ident := Expression
 * @author Jacob Bauer
 */
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
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append(getVariableName().toString());
      mystring.append(" = ");
      mystring.append(getVariableValue().toString());
      mystring.append('\n');
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
