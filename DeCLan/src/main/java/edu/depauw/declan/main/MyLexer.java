package edu.depauw.declan.main;

import java.util.NoSuchElementException;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Lexer;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.Token;
import edu.depauw.declan.common.TokenType;

import static edu.depauw.declan.common.Token.*;
import static edu.depauw.declan.common.TokenType.*;
import static edu.depauw.declan.common.MyIO.*;

public class MyLexer implements Lexer {
	private Source source;
	private Token nextToken;
        private ErrorLog errorLog;

        public MyLexer(Source source, ErrorLog errorLog) {
		this.source = source;
		this.errorLog = errorLog;
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
	    INIT, IDENT, OP, STRING, COMMENT, NUM, HEX, EXP, REAL
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
				} else if (contSingleOpToken(c)){
				    state = State.OP;
				    lexeme.append(c);
				    position = source.getPosition();
				    continue;
				} else {
				    position = source.getPosition();
				    ERROR("Unrecognized character " + c + " at " + position);
				    errorLog.add("Unrecognized character " + c, position);
				    source.advance();
				    continue;
				}
			case IDENT:
			    //Handle next character of an identifier or keyword
				if (Character.isLetterOrDigit(c)) {
				    lexeme.append(c);
				    source.advance();
				    continue;
				} else {
				    nextToken = createId(lexeme.toString(), position);
				    return;
				}
			case STRING:
			    if(c != '\"'){
				source.advance();
				lexeme.append(c);
				continue;
			    } else {
				nextToken = createString(lexeme.toString(), position);
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
				  continue;
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
				  continue;
			    } else {
				source.advance();
				continue;
			    }
			case EXP:
			    if(Character.isDigit(c)){
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				nextToken = createNum(lexeme.toString(), position);
				return;
			    }
			case HEX:
			    if(c == 'H'){
			        lexeme.append(c);
				nextToken = createNum(lexeme.toString(), position);
				source.advance();
				return;
			    } else if (Character.toLowerCase(c) >= 'a' && Character.toLowerCase(c) <= 'f' || Character.isDigit(c)) {
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				ERROR("Unterminated hex literal " + lexeme.toString() + " at " + position); //print error
				errorLog.add("Unterminated hex literal " + lexeme.toString(), position);
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
					state = State.EXP; //go to exp state
					continue;
				    } else {
					ERROR("Missing exponent in real literal " + lexeme.toString() + " at " + position); //print error and go to next token
					errorLog.add("Missing exponent in real literal " + lexeme.toString(), position);
					lexeme.setLength(0);
					state = state.INIT;
					continue;
				    }
				} else if (Character.isDigit(c)) {
				    state = State.EXP;
				    continue;
				} else {
				    ERROR("Missing exponent in real literal " + lexeme.toString() + " at " + position); //print error and go to next token
				    errorLog.add("Missing exponent in real literal " + lexeme.toString(), position);
				    lexeme.setLength(0);
				    state = State.INIT;
				    continue;
				}
			    } else if(Character.isDigit(c)) {
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				nextToken = createNum(lexeme.toString(), position); //created a Real Number Token
				return;
			    }
			case NUM:
			    if (c == '.') {
				state = State.REAL;
				lexeme.append(c);
				source.advance();
				continue;
			    } else if(c <= 'F' && c >= 'A' || c == 'H'){
				state = State.HEX;
				continue;
			    } else if (Character.isDigit(c)){
				lexeme.append(c);
				source.advance();
				continue;
			    } else {
				nextToken = createNum(lexeme.toString(), position);
				return;
			    }
			case OP:
			    if (c == '<' || c == '>' || c == ':'){
				source.advance();
				c = source.current(); //see if c is =
				if(c == '=') {
				    lexeme.append(c);
				    nextToken = create(getDualOpToken(lexeme.toString()), position);
				    source.advance();
				    return;
				} else {
				    nextToken = create(getSingleOpToken(lexeme.toString()), position);
				    return;
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
				  } else {
				      nextToken = create(getSingleOpToken(lexeme.toString()), position);
				      return;
				  }
			    } else {
				nextToken = create(getSingleOpToken(lexeme.toString()), position);
				source.advance();
				return;
			    }
			}
		}
		// Clean up at end of source
		switch (state) {
		case INIT:
		    // No more tokens found
		    nextToken = null;
		    return;
		case IDENT:
		    // Successfully ended an identifier
		    nextToken = createId(lexeme.toString(), position);
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
		    nextToken = createNum(lexeme.toString(), position);
		    return;
		case EXP:
		    nextToken = createNum(lexeme.toString(), position);
		    return;
		case HEX:
		    nextToken = createNum(lexeme.toString(), position);
		    return;
		case REAL:
		    nextToken = createNum(lexeme.toString(), position);
		    return;
		// The operator doesnt need any clean up it will be impossible to get down here from that state
		}
	}
}
