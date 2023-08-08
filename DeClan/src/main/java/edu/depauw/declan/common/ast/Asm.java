package edu.depauw.declan.common.ast;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.Position;

public class Asm extends AbstractASTNode implements Statement {
    private String inlineAssembly;
    private List<String> paramaters;

    public Asm(Position pos, String inlineAssembly, List<String> paramaters){
        super(pos);
        this.inlineAssembly = inlineAssembly;
        this.paramaters = paramaters;
    }

    public Asm(Position pos, String inlineAssembly){
        super(pos);
        this.paramaters = new LinkedList<String>();
        this.inlineAssembly = inlineAssembly;
    }

    public String getInlineAssembly(){
        return inlineAssembly;
    }

    public List<String> getParamaters(){
        return paramaters;
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
