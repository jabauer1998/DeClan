package io.github.H20man13.DeClan.common.matcher;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TerminalAntlrToken<ReturnType> extends AntlrToken<ReturnType> {
    private TerminalNode Node;
    
    public TerminalAntlrToken(TerminalNode Node){
        this.Node = Node;
    }

    @Override
    public ReturnType accept(ParseTreeVisitor<ReturnType> Visitor) {
        // TODO Auto-generated method stub
        return Node.accept(Visitor);
    }

    @Override
    public int getLineNumber() {
        // TODO Auto-generated method stub
        return Node.getSymbol().getLine();
    }

    @Override
    public int getLinePosition() {
        // TODO Auto-generated method stub
        return Node.getSymbol().getStartIndex();
    }

    @Override
    public String getText() {
        return Node.getText();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof TerminalAntlrToken){
            TerminalAntlrToken<ReturnType> returnOther = (TerminalAntlrToken<ReturnType>)other;
            return returnOther.Node.equals(this.Node);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Node.hashCode();
    }
}
