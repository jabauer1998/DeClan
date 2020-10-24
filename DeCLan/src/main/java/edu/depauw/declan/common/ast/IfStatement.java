package edu.depauw.declan.common.ast;

import java.util.ArrayList;

public class IfStatement extends AbstractASTNode implements Statement {
    private ArrayList <IfBlock> toverifyAndExecute;
    
    public IfBlock(Position start, List<IfBlock> toVerifyAndExecute){
	super(start);
	this.toVerifyAndExecute = toVerifyAndExecute;
    }

    public List<IfBlock> getIfStatement(){
	return doIfTrue;
    }
}
