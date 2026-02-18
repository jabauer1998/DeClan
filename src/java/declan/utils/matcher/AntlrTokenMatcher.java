package io.github.h20man13.DeClan.common.matcher;

public class AntlrTokenMatcher<ReturnType> {
    public AntlrTokenMatcher(){

    }

    public boolean match(AntlrToken<ReturnType>... tokens){
        AntlrToken<ReturnType> pastToken = tokens[0];
        if(pastToken != null){
            for(int i = 1; i < tokens.length; i++){
                AntlrToken<ReturnType> currentToken = tokens[i];
                if(currentToken == null || 
                  (currentToken.getLineNumber() < pastToken.getLineNumber() 
                  || (currentToken.getLineNumber() == pastToken.getLineNumber() 
                  && pastToken.getLinePosition() > currentToken.getLinePosition()))){
                    return false;
                }
                pastToken = currentToken;
            }
            return true;
        } else {
            return false;
        }
    }
}
