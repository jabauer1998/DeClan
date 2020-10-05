package edu.depauw.declan.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Lexer;
import edu.depauw.declan.common.ParseException;
import edu.depauw.declan.common.Parser;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Token;
import edu.depauw.declan.common.TokenType;
import edu.depauw.declan.common.ast.ConstDecl;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.UnaryOperation;

/**
 * A parser for a subset of DeCLan consisting only of integer constant
 * declarations and calls to PrintInt with integer expression arguments. This is
 * starter code for CSC426 Project 2.
 * 
 * @author bhoward
 */
public class MyParser implements Parser {
	private Lexer lexer;
	private ErrorLog errorLog;

	/**
	 * Holds the current Token from the Lexer, or null if at end of file
	 */
	private Token current;

	/**
	 * Holds the Position of the current Token, or the most recent one if at end of
	 * file (or position 0:0 if source file is empty)
	 */
	private Position currentPosition;

	public MyParser(Lexer lexer, ErrorLog errorLog) {
		this.lexer = lexer;
		this.errorLog = errorLog;
		this.current = null;
		this.currentPosition = new Position(0, 0);
		skip();
	}

	@Override
	public void close() {
		lexer.close();
	}

	/**
	 * Check whether the current token will match the given type.
	 * 
	 * @param type
	 * @return true if the TokenType matches the current token
	 */
	boolean willMatch(TokenType type) {
		return current != null && current.getType() == type;
	}

	/**
	 * If the current token has the given type, skip to the next token and return
	 * the matched token. Otherwise, abort and generate an error message.
	 * 
	 * @param type
	 * @return the matched token if successful
	 */
	Token match(TokenType type) {
		if (willMatch(type)) {
			return skip();
		} else if (current == null) {
			errorLog.add("Expected " + type + ", found end of file", currentPosition);
		} else {
			errorLog.add("Expected " + type + ", found " + current.getType(), currentPosition);
		}
		throw new ParseException("Parsing aborted");
	}

	/**
	 * If the current token is null (signifying that there are no more tokens),
	 * succeed. Otherwise, abort and generate an error message.
	 */
	void matchEOF() {
		if (current != null) {
			errorLog.add("Expected end of file, found " + current.getType(), currentPosition);
			throw new ParseException("Parsing aborted");
		}
	}

	/**
	 * Skip to the next token and return the skipped token.
	 * 
	 * @return the skipped token
	 */
	Token skip() {
		Token token = current;
		if (lexer.hasNext()) {
			current = lexer.next();
			currentPosition = current.getPosition();
		} else {
			current = null;
			// keep previous value of currentPosition
		}
		return token;
	}

	// Program -> DeclSequence BEGIN StatementSequence END .
	@Override
	public Program parseProgram() {
		Position start = currentPosition;

		Collection<ConstDecl> constDecls = parseDeclSequence();
		match(TokenType.BEGIN);
		Collection<Statement> statements = parseStatementSequence();
		match(TokenType.END);
		match(TokenType.PERIOD);
		matchEOF();
		return new Program(start, constDecls, statements);
	}

	// DeclSequence -> CONST ConstDeclSequence
	// DeclSequence ->
	//
	// ConstDeclSequence -> ConstDecl ; ConstDeclSequence
	// ConstDeclSequence ->
	private Collection<ConstDecl> parseDeclSequence() {
		List<ConstDecl> constDecls = new ArrayList<>();

		if (willMatch(TokenType.CONST)) {
			skip();

			// FIRST(ConstDecl) = ID
			while (willMatch(TokenType.ID)) {
				ConstDecl constDecl = parseConstDecl();
				constDecls.add(constDecl);

				match(TokenType.SEMI);
			}
		}

		// Return a read-only view of the list of ConstDecl objects
		return Collections.unmodifiableCollection(constDecls);
	}

	// ConstDecl -> ident = number
	private ConstDecl parseConstDecl() {
		Position start = currentPosition;

		Token idTok = match(TokenType.ID);
		Identifier id = new Identifier(idTok.getPosition(), idTok.getLexeme());

		match(TokenType.EQ);

		Token numTok = match(TokenType.NUM);
		NumValue num = new NumValue(numTok.getPosition(), numTok.getLexeme());

		return new ConstDecl(start, id, num);
	}

	// StatementSequence -> Statement StatementSequenceRest
	//
	// StatementSequenceRest -> ; Statement StatementSequenceRest
	// StatementSequenceRest ->
	private Collection<Statement> parseStatementSequence() {
		// TODO Auto-generated method stub
	    List<Statement> statements = new ArrayList<>();
	    while(willMatch(TokenType.ID)){
		Statement s = ParseStatement();
		statements.add(s);
		match(TokenType.SEMI);
	    }
	    return Collections.unmodifiableCollection(statements);
	}

        private UnaryOperation.OpType UnOp() {
	    if(willMatch(TokenType.PLUS)){
		skip();
		return UnaryOperation.OpType.PLUS;
	    } else {
		match(TokenType.MINUS);
		return UnaryOperation.OpType.MINUS;
	    }
        }

        

	// TODO handle the rest of the grammar:
	//
	// Statement -> ProcedureCall
	// Statement ->
	private Statement ParseStatement(){
	    Statement pcall;
	    if(willMatch(TokenType.ID)) {
		pcall = ParseProcedureCall();
	    } else {
		
	    }
	    return pcall;
        }
	// ProcedureCall -> ident ( Expression )
        private ProcedureCall ParseProcedureCall(){
	    Position start = currentPosition;
	    Identifier ident = ParseIdentifier();
	    match(TokenType.LPAR);
	    exp = ParseExpression();
	    match(TokenType.LPAR);
	    return new ProcedureCall(start, ident, exp);
        }
	//
	// Expression -> + Term ExprRest
	// Expression -> - Term ExprRest
	// Expression -> Term ExprRest
        // ExprRest -> AddOperator Term ExprRest
	// ExprRest ->
         private Expression ParseExpression(){
	    Position start = currentPosition; 
	    Expression left;
	    if(willMatch(TokenType.MINUS) || willMatch(TokenType.PLUS)){
		UnaryOperation.OpType pm = ParseUnaryOp();
		left = ParseTerm();
		left = new UnaryOperation(start, pm, left);
	    } else {
		left = ParseTerm();
	    }
	    while(willMatch(TokenType.PLUS) || willMatch(TokenType.MINUS)){
		BinaryOperation.OpType op = ParseAddOp();
		Expression right = ParseTerm();
		left = new BinaryOperation(start, left, op, right);
	    }
	    return left;
        }
	// AddOperator -> + | -
	 private BinaryOperation.OpType ParseAddOp() {
	    if(willMatch(TokenType.PLUS)){
		skip();
		return BinaryOperation.OpType.PLUS;
	    } else {
		match(TokenType.MINUS);
		return BinaryOperation.OpType.MINUS;
	    }
        }
        private BinaryOperation.OpType ParseUnOp() {
	    if(willMatch(TokenType.PLUS)){
		skip();
		return UnaryOperation.OpType.PLUS;
	    } else {
		match(TokenType.MINUS);
		return UnaryOperation.OpType.MINUS;
	    }
        }
	// Term -> Factor TermRest
        // TermRest -> MulOperator Factor TermRest
	// TermRest ->
        private Expression ParseTerm(){
	    Position start = currentPosition();
	    Expression left = ParseFactor();
	    while (willMatch(TokenType.DIV) || willMatch(TokenType.MOD) || willMatch(TokenType.TIMES)){
		BinaryOperation OpType = ParseMultOp();
		Expression right = ParseFactor();
		left = new BinaryOperation(start, left, OpType, right);
	    }
	    return left;
        }
    
	// MulOperator -> * | DIV | MOD
        private BinaryOperation.OpType ParseMultOp() {
	    if(willMatch(TokenType.TIMES)){
		skip();
		return BinaryOperation.OpType.TIMES;
	    } else if(willMatch(TokenType.DIVIDE)) {
		skip();
		return BinaryOperation.OpType.DIV;
	    } else {
		match(TokenType.MOD);
		return BinaryOperation.OpType.MOD;
	    }
        }
	// Factor -> number | ident
	// Factor -> ( Expression )
        private Expression ParseFactor(){
	    if(willMatch(TokenType.NUM)){
		return ParseNumValue(); 
	    } else if(willMatch(TokenType.ID)){
		return ParseIdentifier();
	    } else if(willMatch(TokenType.LPAR)) {
		skip();
		Expression expr = ParseExpression();
		match(TokenType.RPAR);
	        return expr;
	    } else {
		FATAL("Error Factor must be an identifier or an id");
		return null;
	    }
	}
    
        private Identifier ParseIdentifier() {
	    Position start = currentPosition;
	    Token id = match(TokenType.ID);
	    return new Identifier(start, id);
        }
    
        private NumValue ParseNumValue() {
	    Position start = currentPosition;
	    Token num = match(TokenType.NUM);
	    return new NumValue(start, num);
        }
}
