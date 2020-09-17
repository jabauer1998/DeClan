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
				} else if (c == '\"') {
				    state = State.TXT;
				    quotecount++;
				} else if (Character.isUpperCase(c)) {
				    state = State.KEYWORD;
				} else if (Character.isLetter(c)){
				    state = State.IDENT;
				} else if (Character.isDigit(c)) {
				    state = State.NUM;
				} else if (TokenType.singleOperators.containsKey(c)){
				    state = State.OP;
				} else {
				    position = source.getPosition();
				    MyIO.ERROR("Unrecognized character " + c + " at " + position);
				    continue;
				}
				lexeme.append(c);
				position = source.getPosition();
				source.advance();
				continue;
			case KEYWORD:
			    if(Character.isUpperCase(c)){
				lexeme.append(c);
				source.advance();
			    } else if(TokenType.keywords.containsKey(lexeme.toString())){
				nextToken = tokenFactory.makeToken(TokenType.keywords.get(lexeme.toString()), position);
				return;
			    } else {
				state = state.INIT;
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
			case TXT:
			    if(c == '\"'){
				quotecount++;
				if(quotecount == 3){
				    state = State.COMMENT;
				}
			    } else {
				state = State.STRING;
				String s = "";
				s += c;
				lexeme.replace(0, 1, s); //replace " with the new character
			    }
			    source.advance();
			    continue;
			case STRING:
			    if(c == '\"'){
				source.advance();
				nextToken = tokenFactory.makeStringToken(lexeme.toString(), position);
				return;
			    }
			    lexeme.append(c);
			    source.advance();
			    continue;
			case COMMENT:
			    if(c == '\"'){
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
				    source.advance();
				    nextToken = tokenFactory.makeToken(TokenType.dualOperators.get(lexeme.toString()), position);
				} else {
				    nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
				}
			    } else {
				nextToken = tokenFactory.makeToken(TokenType.singleOperators.get(lexeme.toString().charAt(0)), position);
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
		case TXT:
		        nextToken = 
		        nextToken = null;
			return;
	        case STRING:
		    MyIO.ERROR("Unterminated String");
		    nextToken = null;
		    return;
		case COMMENT:
		    MyIO.ERROR("Unterminated Comment");
		    nextToken = null;
		    return;
		case NUM:
		    nextToken = tokenFactory.makeNumToken(lexeme.toString(), position);
		    return;
		// TODO handle more state cases here as well
		}
	}
}
