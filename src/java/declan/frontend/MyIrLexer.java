package declan.frontend;

import declan.utils.ErrorLog;
import declan.frontend.Lexer;
import declan.utils.position.Position;
import declan.utils.source.Source;
import declan.frontend.token.IrToken;
import declan.frontend.token.IrTokenType;

public class MyIrLexer implements Lexer<IrToken> {
    private Source source;
    private IrToken nextToken;
    private ErrorLog errorLog;

    public MyIrLexer(Source source, ErrorLog errorLog){
        this.source = source;
        this.errorLog = errorLog;
        this.nextToken = null;
    }

    public boolean hasNext(){
        if(nextToken == null){
            scanNext();
        }
        return nextToken != null;
    }

    public IrToken next(){
        if(nextToken == null){
            scanNext();
        }
        if(nextToken == null){
            System.err.println("No more tokens");
        }
        IrToken result = nextToken;
        nextToken = null;
        return result;
    }

    public void close(){
        source.close();
    }

    private static enum State{
        INIT, IDENT, STRING, COMMENT, NUM, REAL, OP, SPEC
    }

    private void scanNext(){
        State state = State.INIT;
        StringBuilder lexeme = new StringBuilder();
        Position position = null;
        while(!source.atEOF()){
            char c = source.current();
            switch(state){
                case INIT:
                    if(Character.isWhitespace(c)){
                        source.advance();
                        continue;
                    } else if(c == '\"'){
                        state = state.STRING;
                        position = source.getPosition();
                        source.advance();
                        continue;
                    } else if(Character.isDigit(c)){
                        state = state.NUM;
                        lexeme.append(c);
                        position = source.getPosition();
                        source.advance();
                        continue;
                    } else if(Character.isLetter(c)){
                        state = state.IDENT;
                        lexeme.append(c);
                        position = source.getPosition();
                        source.advance();
                        continue;
                    } else if (c == '#'){
                        state = state.COMMENT;
                        source.advance();
                        continue;
                    } else if(IrTokenType.contSingleOpToken(c) || c == ':' || c == '-'){
                        state = state.OP;
                        lexeme.append(c);
                        source.advance();
                        position = source.getPosition();
                        continue;
                    } else if(c == '%') {
                    	state = state.SPEC;
                    	lexeme.append(c);
                    	source.advance();
                    	position = source.getPosition();
                    	continue;
                    } else {
                        position = source.getPosition();
                        errorLog.add("Unrecognized character " + c, position);
                        source.advance();
                        continue;
                    }
                case IDENT:
                    if(Character.isLetterOrDigit(c) || c == '_'){
                        lexeme.append(c);
                        source.advance();
                        continue;
                    } else {
                        nextToken = IrToken.createId(lexeme.toString(), position);
                        return;
                    }
                case STRING:
                    if(c != '\"'){
                        source.advance();
                        lexeme.append(c);
                        continue;
                    } else {
                        nextToken = IrToken.createString(lexeme.toString(), position);
                        source.advance();
                        return;
                    }
                case COMMENT:
                    if(c != '\n'){
                        source.advance();
                        continue;
                    } else {
                        state = state.INIT;
                        source.advance();
                        continue;
                    }
                case NUM:
                    if(Character.isDigit(c)){
                        lexeme.append(c);
                        source.advance();
                        continue;
                    } else if(c == '.') {
                        state = state.REAL;
                        lexeme.append(c);
                        source.advance();
                        continue;
                    } else {
                        nextToken = IrToken.createNumber(lexeme.toString(), position);
                        return;
                    }
                case REAL:
                    if(Character.isDigit(c)){
                        lexeme.append(c);
                        source.advance();
                        continue;
                    } else {
                        nextToken = IrToken.createNumber(lexeme.toString(), position);
                        return;
                    }
                case OP:
                    if(IrTokenType.contDualOpToken(lexeme.toString())) {
                        nextToken = IrToken.create(IrTokenType.getDualOpToken(lexeme.toString()), lexeme.toString(), position);
                        return;
                    } else if(IrTokenType.contSingleOpToken(lexeme.toString().charAt(0))){
                        nextToken = IrToken.create(IrTokenType.getSingleOpToken(lexeme.toString()), lexeme.toString(), position);
                        return;
                    } else if(!Character.isWhitespace(c)){
                        lexeme.append(c);
                        source.advance();
                        continue;
                    } else {
                        nextToken = null;
                        errorLog.add("Error: Invalid operator with lexeme " + lexeme.toString(), position);
                        return;
                    }
                case SPEC:
                	if(c == 'u' || c == 'd' || c == 'a' || c == 'r') {
                		lexeme.append(c);
                		source.advance();
                		continue;
                	} else {
                		nextToken = IrToken.create(IrTokenType.SPECIFIER, lexeme.toString(), position);
                		return;
                	}
            }
        }

        switch(state){
            case INIT:
                nextToken = null;
                return;
            case IDENT:
                nextToken = IrToken.createId(lexeme.toString(), position);
                return;
            case STRING:
                errorLog.add("Unterminated String Literal at the end of the file", position);
                nextToken = null;
                return;
            case COMMENT:
                nextToken = null;
                return;
            case NUM:
                nextToken = IrToken.createNumber(lexeme.toString(), position);
                return;
            case REAL:
                nextToken = IrToken.createNumber(lexeme.toString(), position);
                return;
            case OP:
                if(IrTokenType.contDualOpToken(lexeme.toString())){
                    nextToken = IrToken.create(IrTokenType.getDualOpToken(lexeme.toString()), lexeme.toString(), position);
                    return;
                } else if(IrTokenType.contSingleOpToken(lexeme.charAt(0))){
                    nextToken = IrToken.create(IrTokenType.getSingleOpToken(lexeme.toString()), lexeme.toString(), position);
                    return;
                } else {
                    errorLog.add("Error no valid operator found at the end of the file", position);
                    nextToken = null;
                    return;
                }
        }
    }
}
