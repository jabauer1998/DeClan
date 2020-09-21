package edu.depauw.declan;

import java.util.NoSuchElementException;

import edu.depauw.declan.common.Lexer;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.Token;
import edu.depauw.declan.common.TokenFactory;
import edu.depauw.declan.common.TokenType;
import static edu.depauw.declan.common.MyIO.*;

public class MyLexer implements Lexer {
	private Source source;
	private TokenFactory tokenFactory;
	private Token nextToken;

	public MyLexer(Source source, TokenFactory tokenFactory) {
		this.source = source;
		this.tokenFactory = tokenFactory;
		this.nextToken = null;
	}

	public boolean hasNext() {
		if(nextToken == null) {
			scanNext();
		}
		return nextToken != null;
	}

	public Token next(){
		if (nextToken == null) {
			scanNext();
		}
		if (nextToken == null) {
		    ERROR("No more tokens");
		}
		Token result = nextToken;
		nextToken = null;
		return result;
	}

	public void close() {
		source.close();
	}

	private static enum State {
	    INIT, IDENT, KEYWORD, OP, TXT, STRING, COMMENT, NUM
	}

	/**
	 * Scan through characters from source, starting with the current one, to find
	 * the next token. If found, store it in nextToken and leave the source on the
	 * next character after the token. If no token found, set nextToken to null.
	 */
	private void scanNext() {
		State state = State.INIT;
		StringBuilder lexeme = new StringBuilder();
		Position position = null;
		int Comment = 0;
		while (!source.atEOF()) {
			char c = source.current();
			switch (state) {
			case INIT:
				// Look for the start of a token
				if (Character.isWhitespace(c)) {
				    source.advance();
				    continue;
				} else if (c == '\"') {
				    state = State.STRING;
				} else if (Character.isUpperCase(c)) {
				    state = State.KEYWORD;
				    lexeme.append(c);
				} else if (Character.isLetter(c)){
				    state = State.IDENT;
				    lexeme.append(c);
				} else if (Character.isDigit(c)){
				    state = State.NUM;
				    lexeme.append(c);
				} else if (TokenType.singleOperators.containsKey(c)){
				    state = State.OP;
				    lexeme.append(c);
				    position = source.getPosition();
				    continue;
				} else {
				    position = source.getPosition();
				    ERROR("Unrecognized character " + c + " at " + position);
				    source.advance();
				    continue;
				}
				position = source.getPosition();
				source.advance();
				continue;
			case KEYWORD:
			    if(Character.isUpperCase(c)){
				lexeme.append(c);
				source.advance();
			    } else if(Character.isLetterOrDigit(c)) {
				state = State.IDENT;
			    } else {
				nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
				return;
			    }
			    continue;
			case IDENT:
			    //Handle next character of an identifier or keyword
				if (Character.isLetterOrDigit(c)) {
				    lexeme.append(c);
				    source.advance();
				    continue;
				}
				nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
				return;
			case STRING:
			    if(c != '\"'){
				source.advance();
				lexeme.append(c);
				continue;
			    }
			    nextToken = tokenFactory.makeStringToken(lexeme.toString(), position);
			    source.advance();
			    return;
			case COMMENT:
			    if(c == '(') {
				  source.advance();
				  c = source.current();
				  if(c == '*') {
				      state = State.COMMENT;
				      source.advance();
				      Comment++;
				  }
			    } else if(c == '*') {
				  source.advance();
				  c = source.current();
				  if(c == ')') {
				      state = State.COMMENT;
				      source.advance();
				      Comment--;
				  }
				  if(Comment <= 0){
				      state = state.INIT;
				  }
			    } else {
				source.advance();
			    }
			    continue;
			case NUM:
			    if(Character.isLetterOrDigit(c)){
				lexeme.append(c);
				source.advance();
				continue;
			    }
			    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
			    return;
			case OP:
			    if (c == '<' || c == '>' || c == ':'){
				source.advance();
				c = source.current(); //see if c is =
				if(c == '=') {
				    lexeme.append(c);
				    nextToken = tokenFactory.makeToken(TokenType.dualOperators.get(lexeme.toString()), position);
				    source.advance();
				} else {
				    nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
				}
                            } else if(c == '(') {
				  source.advance();
				  c = source.current();
				  if(c == '*') {
				      state = State.COMMENT;
				      source.advance();
				      Comment++;
				      continue;
				  }
				  nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
			    } else {
				nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
				source.advance();
			    }
			    return;
			// TODO and more state cases here
			}
		}
		// Clean up at end of source
		switch (state) {
		case INIT:
		    // No more tokens found
		    nextToken = null;
		    return;
		case KEYWORD:
		    //Not entire keyword was found so it is likely an identity
		    nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
		    return;
		case IDENT:
		    // Successfully ended an identifier
		    nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
		    return;
		case TXT:
		    ERROR("Extra quotation mark at the end");
		    nextToken = null;
		    return;
	        case STRING:
		    ERROR("Unterminated String");
		    nextToken = null;
		    return;
		case COMMENT:
		    ERROR("Unterminated Comment");
		    nextToken = null;
		    return;
		case NUM:
		    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
		    return;
		// The operator doesnt need any clean up it will be impossible to get down here from that state
		}
	}
}
