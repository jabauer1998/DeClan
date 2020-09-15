package edu.depauw.declan.common;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
	ID, // identifier, such as a variable name
	NUM, // numeric literal
	STRING, // string literal
	LT, // less than "<"
	LE, // less than or equal "<="
	GT, // greater than ">"
	GE, // greater than or equal ">="
	ASSIGN, // assignment operator ":="
	COLON, // colon ":"
	LPAR, // left parenthesis "("
	RPAR, // right parenthesis ")"
	EQ, // equals sign "="
	NE, // not equal "#"
	PLUS, // plus operator "+"
	MINUS, // minus operator "-"
	TIMES, // times operator "*"
	DIVIDE, // divide operator "/"
	AND, // and operator "&"
	NOT, // not operator "~"
	SEMI, // semicolon ";"
	COMMA, // comma ","
	PERIOD, // period "."
	// the rest are reserved words whose lexeme matches their name
	BEGIN, BY, CONST, DIV, DO, ELSE, ELSIF, END, FALSE, FOR, IF, MOD, OR, PROCEDURE, REPEAT, RETURN, THEN, TO, TRUE, UNTIL, VAR, WHILE;

	public static final Map<String, TokenType> keywords;
	public static final Map<Character, TokenType> singleOperators;
	public static final Map<String, TokenType> dualOperators;
	
	private static void addKeyword(TokenType type) {
	    keywords.put(type.toString(), type);
	}
	private static void addSingleOp(Character key, TokenType type){
	    singleOperators.put(key, type);
	}
	private static void addDualOp(String key, TokenType type){
	    dualOperators.put(key, type);
	}
	static {
		keywords = new HashMap<>();
		addKeyword(BEGIN);
		addKeyword(BY);
		addKeyword(CONST);
		addKeyword(DIV);
		addKeyword(DO);
		addKeyword(ELSE);
		addKeyword(ELSIF);
		addKeyword(END);
		addKeyword(FALSE);
		addKeyword(FOR);
		addKeyword(IF);
		addKeyword(MOD);
		addKeyword(OR);
		addKeyword(PROCEDURE);
		addKeyword(REPEAT);
		addKeyword(RETURN);
		addKeyword(THEN);
		addKeyword(TO);
		addKeyword(TRUE);
		addKeyword(UNTIL);
		addKeyword(VAR);
		addKeyword(WHILE);

		singleOperators = new HashMap<>();
		addSingleOp('<', LT);
		addSingleOp('>', GT);
		addSingleOp(':', COLON);
		addSingleOp('+', PLUS);
		addSingleOp('-', MINUS);
		addSingleOp('&', AND);
		addSingleOp('#', NE);
		addSingleOp('=', EQ);
		addSingleOp('(', LPAR);
		addSingleOp(')', RPAR);
		addSingleOp('~', NOT);
		addSingleOp('-', MINUS);
		addSingleOp('/', DIVIDE);
		addSingleOp('*', TIMES);
		addSingleOp(';', SEMI);
		addSingleOp(',', COMMA);
		addSingleOp('.', PERIOD);
		
		dualOperators = new HashMap<>();
		addDualOp(">=", GE);
		addDualOp("<=", LE);
		addDualOp(":=", ASSIGN);
	}
}
