package edu.depauw.declan;

import java.util.NoSuchElementException;

import edu.depauw.declan.common.Lexer;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.Token;
import edu.depauw.declan.common.TokenFactory;
import edu.depauw.declan.common.TokenType;
import edu.depauw.declan.common.MyIO;

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
		if (nextToken == null) {
			scanNext();
		}

		return nextToken != null;
	}

	public Token next() {
		if (nextToken == null) {
			scanNext();
		}

		if (nextToken == null) {
			throw new NoSuchElementException("No more tokens");
		}

		Token result = nextToken;
		nextToken = null;
		return result;
	}

	public void close() {
		source.close();
	}

	private static enum State {
	    INIT, IDENT, COLON, KEYWORD, LESS, GREATER, NOTEQUAL, TXT, STRING, COMMENT, NUM
		// TODO add more states here
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
		int quotecount = 0;
		while (!source.atEOF()) {
			char c = source.current();
			switch (state) {
			case INIT:
				// Look for the start of a token
				if (Character.isWhitespace(c)) {
					source.advance();
					continue;
				} else if (Character.isUpperCase(c)) {
				        state = State.KEYWORD;
					lexeme.append(c);
					position = source.getPosition();
					source.advance();
				} else if (Character.isLetter(c)){
					state = State.IDENT;
					lexeme.append(c);
					// Record starting position of identifier or keyword token
					position = source.getPosition();
					source.advance();
					continue;
				} else if (Character.isDigit(c)) {
				    state = State.NUM;
				    lexeme.append(c);
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (c == '\"') {
				    state = State.TXT
				    quotecount++;
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (c == ':') {
					state = State.COLON;
					position = source.getPosition();
					source.advance();
					continue;
				} else if (c == '=') {
					position = source.getPosition();
					source.advance();
					nextToken = tokenFactory.makeToken(TokenType.EQ, position);
					return;
				} else if (c == '<') {
				    state = State.LESS;
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (c == '>') {
				    state = State.GREATER;
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (c == '(') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.LPAR, position);
				    return;
				} else if (c == '(') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.RPAR, position);
				    return;
				} else if (c == '#') {
				    state = State.GREATER;
				    position = source.getPosition();
				    source.advance();
				    continue;
				} else if (c == '+') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.PLUS, position);
				    return;
				} else if (c == '-') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.MINUS, position);
				    return;
				} else if (c == '*') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.TIMES, position);
				    return;
				} else if (c == '/') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.DIVIDE, position);
				    return;
				} else if (c == '&') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.AND, position);
				    return;
				}  else if (c == '~') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.NOT, position);
				    return;
				}  else if (c == ';') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.SEMI, position);
				    return;
				}  else if (c == ',') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.COMMA, position);
				    return;
				}  else if (c == '.') {
				    position = source.getPosition();
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.PERIOD, position);
				    return;
				} else {
					// TODO handle other characters here
					position = source.getPosition();
					ERROR("Unrecognized character " + c + " at " + position);
					source.advance();
					continue;
				}
			case KEYWORD:
			    if(Character.isUpperCase(c)){
				lexeme.append(c);
				source.advance();
			    } else if(reserved.containsKey(lexeme.toString())){
				nextToken = tokenFactory.makeToken(reserved.get(lexeme.toString()), position);
				return;
			    } else {
				state = state.INIT;
			    }
			case IDENT:
				//Handle next character of an identifier or keyword
				if (Character.isLetterOrDigit(c)) {
					lexeme.append(c);
					source.advance();
					continue;
				} else {
				    nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
				    return;
				}
			case COLON:
				// Check for : vs :=
				if (c == '=') {
					source.advance();
					nextToken = tokenFactory.makeToken(TokenType.ASSIGN, position);
					return;
				} else {
					nextToken = tokenFactory.makeToken(TokenType.COLON, position);
					return;
				}
			case LESS:
			    //check for < vs <=
			    if(c == '='){
				source.advance();
				nextToken = tokenFactory.makeToken(TokenType.LE, position);
				return;
			    } else {
				nextToken = tokenFactory.makeToken(TokenType.LT, position);
				return;
			    }
			case GREATER:
			    //check for < vs <=
			    if(c == '='){
				source.advance();
				nextToken = tokenFactory.makeToken(TokenType.GE, position);
				return;
			    } else {
				nextToken = tokenFactory.makeToken(TokenType.GT, position);
				return;
			    }
			case NOTEQUAL:
			    if(c == '='){
				source.advance();
				nextToken = tokenFactory.makeToken(TokenType.NE, position);
				return;
			    } else {
				ERROR("Unexpected # found in code at position " + position);
			    }
			case TXT:
			    if(c == '\"'){
				quotecount++;
				if(quotecount == 3){
				    state = State.COMMENT;
				}
				source.advance();
				continue;
			    } else {
				state = State.STRING;
				lexeme.append(c);
				source.advance();
				continue;
			    }
			case STRING:
			    if(c == '\"'){
				source.advance();
				nextToken = TokenFactory.makeStringToken(lexeme.toString(), position);
				return;
			    } else {
				lexeme.append();
				source.advance();
				continue;
			    }
			case COMMENT:
			    if (c == '\"') {
				quotecount--;
				if(quotecount == 0){
				    state = State.INIT;
				}
				source.advance();
				continue;
			    } else {
				quotecount = 3;
				source.advance();
				continue;
			    }
			case NUM:
			    if(Character.isDigit(c) || (c <= 'F' && c >= 'A')){
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				nextToken = TokenFactory.makeNumToken(lexeme.toString(), position);
				return;
			    }
			// TODO and more state cases here
			}
		}

		// Clean up at end of source
		switch (state) {
		case INIT:
			// No more tokens found
			nextToken = null;
			return;
			
		case IDENT:
			// Successfully ended an identifier or keyword
			nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
			return;
			
		case COLON:
			// Final token was :
			nextToken = tokenFactory.makeToken(TokenType.COLON, position);
			return;
			
		// TODO handle more state cases here as well
		}
	}
}
