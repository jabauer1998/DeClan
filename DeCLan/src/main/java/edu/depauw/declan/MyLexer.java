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
	    INIT, IDENT, KEYWORD, OP, STRING, COMMENT, NUM, HEX, EXP, REAL
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
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (Character.isUpperCase(c)) {
				    state = State.KEYWORD;
				    lexeme.append(c);
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (Character.isLetter(c)){
				    state = State.IDENT;
				    lexeme.append(c);
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (Character.isDigit(c)){
				    state = State.NUM;
				    lexeme.append(c);
				    position = source.getPosition();
				    source.advance();
				    continue;
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
			case KEYWORD:
			    if(Character.isUpperCase(c)){
				lexeme.append(c);
				source.advance();
				continue;
			    } else if(Character.isLetterOrDigit(c)) {
				state = State.IDENT;
				continue;
			    } else {
				DBG("Token is: " + lexeme.toString());
				nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
				return;
			    }
			case IDENT:
			    //Handle next character of an identifier or keyword
				if (Character.isLetterOrDigit(c)) {
				    lexeme.append(c);
				    source.advance();
				    continue;
				} else {
				    DBG("Token is: " + lexeme.toString());
				    nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
				    return;
				}
			case STRING:
			    if(c != '\"'){
				source.advance();
				lexeme.append(c);
				continue;
			    } else {
				DBG("Token is: " + lexeme.toString());
				nextToken = tokenFactory.makeStringToken(lexeme.toString(), position);
				source.advance();
				return;
			    }
			case COMMENT:
			    if(c == '(') {
				  source.advance();
				  c = source.current();
				  if(c == '*') {
				      source.advance();
				      Comment++;
				  }
			    } else if(c == '*') {
				  source.advance();
				  c = source.current();
				  if(c == ')') {
				      source.advance();
				      Comment--;
				      if(Comment <= 0){
					  state = state.INIT;
				      }
				  }
			    } else {
				source.advance();
			    }
			    continue;
			case EXP:
			    if(Character.isDigit(c)){
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				DBG("Token is: " + lexeme.toString());
				nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
				return;
			    }
			case HEX:
			    if(c == 'H'){
			        lexeme.append(c);
				nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
				source.advance();
				DBG("My token is: " + lexeme.toString());
				return;
			    } else if (Character.toLowerCase(c) >= 'a' && Character.toLowerCase(c) <= 'f' || Character.isDigit(c)) {
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				ERROR("Unterminated hex literal " + lexeme.toString() + " at " + position); //print error 
				lexeme.setLength(0); //clear string builder buffer
				state = State.INIT;
				continue;
			    }
			case REAL:
			    if(c == 'E') {
				lexeme.append(c);
				source.advance();
				c = source.current();
				if(c == '+' || c == '-'){
				    lexeme.append(c);
				    source.advance();
				    c = source.current();
				    if(Character.isDigit(c)){
					state = State.EXP;
					continue;
				    } else {
					ERROR("Missing exponent in real literal " + lexeme.toString() + " at " + position);
					lexeme.setLength(0);
					state = state.INIT;
					continue;
				    }
				} else if (Character.isDigit(c)) {
				    state = State.EXP;
				    continue;
				} else {
				    ERROR("Missing exponent in real literal " + lexeme.toString() + " at " + position);
				    lexeme.setLength(0);
				    state = State.INIT;
				    continue;
				}
			    } else if(Character.isDigit(c)) {
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
				return;
			    }
			case NUM:
			    if (c == '.') {
				state = State.REAL;
				lexeme.append(c);
				source.advance();
				continue;
			    } else if(Character.toLowerCase(c) <= 'f' && Character.toLowerCase(c) >= 'a' || c == 'H'){
				state = State.HEX;
				continue;
			    } else if (Character.isDigit(c)){
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				DBG("Token is: " + lexeme.toString());
				nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
				return;
			    }
			case OP:
			    if (c == '<' || c == '>' || c == ':'){
				source.advance();
				c = source.current(); //see if c is =
				if(c == '=') {
				    lexeme.append(c);
				    DBG("Token is: " + lexeme.toString());
				    nextToken = tokenFactory.makeToken(TokenType.dualOperators.get(lexeme.toString()), position);
				    source.advance();
				} else {
				    DBG("Token is: " + lexeme.toString());
				    nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
				}
                            } else if(c == '(') {
				  source.advance();
				  c = source.current();
				  if(c == '*') {
				      state = State.COMMENT;
				      lexeme.setLength(0); //remove ( from lexeme it is only a comment not a token
				      source.advance();
				      Comment++;
				      continue;
				  }
				  nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
			    } else {
				nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
				source.advance();
			    }
			    DBG("Token is: " + lexeme.toString());
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
	        case STRING:
		    ERROR("Unterminated string literal at end of file");
		    nextToken = null;
		    return;
		case COMMENT:
		    ERROR("Unterminated comment at end of file");
		    nextToken = null;
		    return;
		case NUM:
		    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
		    return;
		case EXP:
		    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
		    return;
		case HEX:
		    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
		    return;
		case REAL:
		    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
		    return;
		// The operator doesnt need any clean up it will be impossible to get down here from that state
		}
	}
}
