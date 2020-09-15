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
				} else if (Character.isLetter(c)){
				    state = State.IDENT;
				} else if (Character.isDigit(c)) {
				    state = State.NUM;
				} else if (c == '\"') {
				    state = State.TXT;
				    quotecount++;
				    source.advance();
				    continue;
				} else if (singleOperators.containsKey(c)){
				    state = State.SINGLEOP;
				    lexeme.append(c);
				} else {
				    position = source.getPosition();
				    ERROR("Unrecognized character " + c + " at " + position);
				    continue;
				}
				lexme.append(c);
				position = source.getPosition();
				source.advance();
				continue;
			case KEYWORD:
			    if(Character.isUpperCase(c)){
				lexeme.append(c);
				source.advance();
			    } else if(reserved.containsKey(lexeme.toString())){
				nextToken = tokenFactory.makeToken(keywords.get(lexeme.toString()), position);
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
				}
				nextToken = tokenFactory.makeIdToken(lexeme.toString(), position);
				return;
			case TXT:
			    if(c == '\"'){
				quotecount++;
				if(quotecount == 3){
				    state = State.COMMENT;
				}
			    } else {
				state = State.STRING;
				lexeme.append(c);
			    }
			    source.advance();
			    continue;
			case STRING:
			    if(c == '\"'){
				source.advance();
				nextToken = TokenFactory.makeStringToken(lexeme.toString(), position);
				return;
			    }
			    lexeme.append();
			    source.advance();
			    continue;
			case COMMENT:
			    if (c == '\"') {
				quotecount--;
				if(quotecount == 0){
				    state = State.INIT;
				}
			    } else {
				quotecount = 3;
			    }
			    source.advance();
			    continue;
			case NUM:
			    if(Character.isDigit(c) || (c <= 'F' && c >= 'A')){
				lexeme.append(c);
				source.advance();
				continue;
			    }
			    nextToken = TokenFactory.makeNumToken(lexeme.toString(), position);
			    return;
			case OP:
			    if (c == '<' || c == '>' || c == ':'){
				source.advance();
				c = source.current(); //see if c is =
				if (c == '=') {
				    lexeme.append(c);
				    source.advance();
				    nextToken = tokenFactory.makeToken(dualOperators.get(lexeme.toString()), position);
				} else {
				    nextToken = tokenFactory.makeToken(singleOperators.get(lexeme.toString().charAt(0)), position);
				}
			    } else {
				nextToken = tokenFactory.makeToken(singleOperators.get(lexeme.toString().charAt(0)), position);
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
