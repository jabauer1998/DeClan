package declan.utils.matcher;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class NonTerminalAntlrToken<ReturnType> extends AntlrToken<ReturnType>{

    private ParserRuleContext Context;

    public NonTerminalAntlrToken(ParserRuleContext Context){
        this.Context = Context;
    }

    @Override
    public ReturnType accept(ParseTreeVisitor<ReturnType> Visitor) {
        // TODO Auto-generated method stub
       return Context.accept(Visitor);
    }

    @Override
    public int getLineNumber() {
        // TODO Auto-generated method stub
        return Context.getStart().getLine();
    }

    @Override
    public int getLinePosition() {
        // TODO Auto-generated method stub
        return Context.getStart().getStartIndex();
    }

    @Override
    public String getText() {
        return Context.getText();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof NonTerminalAntlrToken){
            NonTerminalAntlrToken<ReturnType> returnOther = (NonTerminalAntlrToken<ReturnType>)other;
            return this.Context.equals(returnOther.Context);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Context.hashCode();
    }
    
}
