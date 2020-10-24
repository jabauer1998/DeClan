package edu.depauw.declan.common.ast;

import java.util.ArrayList;

public class IfBlock {
    private Expression Check;
    private List <Statement> doIfTrue;
    
    public IfBlock(Expression Check, List<Statement> doIfTrue){
	this.Check = Check;
	this.doIfTrue = doIfTrue;
    }

    public Expression getExpression(){
	return Check;
    }

    public List<Statement> getStatements(){
	return doIfTrue;
    }
}
