package declan.utils.matcher;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AntlrTokenFactory<ReturnType> {
    public AntlrTokenFactory(){}

    public AntlrToken<ReturnType> decorateToken(ParserRuleContext Context){
        if(Context == null)
            return null;
        else 
            return new NonTerminalAntlrToken<ReturnType>(Context);
    }

    public AntlrToken<ReturnType> decorateToken(TerminalNode Node){
        if(Node == null)
            return null;
        else 
            return new TerminalAntlrToken<ReturnType>(Node);
    }

    public List<AntlrToken<ReturnType>> decorateContexts(List<ParserRuleContext> Contexts){
        List<AntlrToken<ReturnType>> returnList = new LinkedList<>();
        for(ParserRuleContext Context : Contexts){
            returnList.add(this.decorateToken(Context));
        }
        return returnList;
    }

    public List<AntlrToken<ReturnType>> decorateNodes(List<TerminalNode> Nodes){
        List<AntlrToken<ReturnType>> returnList = new LinkedList<>();
        for(TerminalNode Node : Nodes){
            returnList.add(this.decorateToken(Node));
        }
        return returnList;
    }
}
