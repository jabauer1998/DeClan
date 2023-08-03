package edu.depauw.declan.common.ast;

import edu.depauw.declan.common.Position;

public class Asm extends AbstractASTNode implements Statement {
    private String inlineAssembly;

    public Asm(Position pos, String inlineAssembly){
        super(pos);
        this.inlineAssembly = inlineAssembly;
    }

    public String getInlineAssembly(){
        return inlineAssembly;
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
