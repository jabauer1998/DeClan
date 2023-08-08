package edu.depauw.declan.common;

import java.util.TreeMap;
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
	BEGIN, BY, CONST, DIV, DO, ELSE, ELSIF, END, FALSE, FOR, IF, MOD, OR, PROCEDURE, REPEAT, RETURN, THEN, TO, TRUE, UNTIL, VAR, WHILE, BAND, BOR, LSHIFT, RSHIFT;

	public static final Map<String, TokenType> reserved; 
	private static final Map<Character, TokenType> singleOperators; //all 1 character long operators
	private static final Map<String, TokenType> dualOperators; //all two character long operators
	
	private static void addKeyword(TokenType type) {
	    reserved.put(type.toString(), type);
	}

	/**
	   Adds single element operator to the hash map
	   @param <code> key </code> character representing operator
	   @param <code> type </code> token the character is mapped to 
	   @author Jacob Bauer
	*/
	private static void addSingleOp(Character key, TokenType type){
	    singleOperators.put(key, type);
	}
	/**
	   Adds double element operator to the hash map
	   @param <code> key </code> character representing operator
	   @param <code> type </code> token the string is mapped to 
	   @author Jacob Bauer
	*/
	private static void addDualOp(String key, TokenType type){
	    dualOperators.put(key, type);
	}
	/**
	   Const double element operator to the hash map
	   @param <code> key </code> string representing operator
	   @param <code> type </code> token the string is mapped to 
	   @author Jacob Bauer
	*/
	public static boolean contDualOpToken(String key){
	    return dualOperators.containsKey(key);
	}
	
	/**
	   Checks if tree map contains element with the specified key
	   @param <code> key </code> character representing operator
	   @param <code> type </code> token the string is mapped to 
	   @author Jacob Bauer
	*/
	public static boolean contSingleOpToken(char key){
	    return singleOperators.containsKey(key);
	}

	/**
	   Returns the TokenType of the specified string
	   @param <code> key </code> string representing operator
	   @author Jacob Bauer
	*/
	
	public static TokenType getDualOpToken(String key){
	    return dualOperators.get(key);
	}

	/**
	   Returns the tokentype of the specified string
	   @param <code> key </code> string representing operator
	   @author Jacob Bauer
	*/
	
	public static TokenType getSingleOpToken(String key){
	    return singleOperators.get(key.charAt(0));
	}
	
	static {
		reserved = new HashMap<>();
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
		addKeyword(BAND);
		addKeyword(BOR);

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
		
		dualOperators = new TreeMap<>();
		addDualOp(">=", GE);
		addDualOp("<=", LE);
		addDualOp(":=", ASSIGN);
		addDualOp(">>", RSHIFT);
		addDualOp("<<", LSHIFT);
	}
}
