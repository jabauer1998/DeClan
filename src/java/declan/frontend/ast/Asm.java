package io.github.h20man13.DeClan.common.ast;

import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.position.Position;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("asm(");
        sb.append(inlineAssembly);
        sb.append(':');
        int size = paramaters.size();
        for(int i = 0; i < size; i++){
            String par = paramaters.get(i);
            sb.append(par);
            if(i < size - 1){
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }
    
}
