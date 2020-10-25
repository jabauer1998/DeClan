package edu.depauw.declan.common.ast;

import java.util.List;

public class IfBlock extends IfElseBlock{
    private final Expression Check;
    
    public IfBlock(Expression Check, List<Statement> doIfTrue){
        super(doIfTrue);
	this.Check = Check;
    }

    public Expression getExpression(){
	return Check;
    }
}
