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
import edu.depauw.declan.common.ast.ConstDeclaration;
import edu.depauw.declan.common.ast.VariableDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.Expression;
import edu.depauw.declan.common.ast.BinaryOperation;
import edu.depauw.declan.common.ast.UnaryOperation;

import static edu.depauw.declan.common.MyIO.*;

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
	// Program -> DeclSequence BEGIN StatementSequence END
	@Override
	public Program parseProgram() {
		Position start = currentPosition;
		List<Declaration> Decls = parseDeclarationSequence();
		match(TokenType.BEGIN);
		List<Statement> statements = parseStatementSequence();
		match(TokenType.END);
		match(TokenType.PERIOD);
		matchEOF();
		return new Program(start, Decls, statements);
	}

        public List<Declaration> parseDeclarationSequence(){
	    List<Declaration> Decls = new ArrayList<>();
	    if(willMatch(TokenType.CONST)){
		Decls.addAll(parseConstDeclSequence());
	    }
	    if(willMatch(TokenType.VAR)){
		Decls.addAll(parseVariableDeclSequence());
	    }
	    return Decls;
        }

	// DeclSequence -> CONST ConstDeclSequence
	// DeclSequence ->
	//
	// ConstDeclSequence -> ConstDecl ; ConstDeclSequence
	// ConstDeclSequence ->
	private List<ConstDeclaration> parseConstDeclSequence(){
		List<ConstDeclaration> constDecls = new ArrayList<>();
		if (willMatch(TokenType.CONST)) {
			skip();
			// FIRST(ConstDecl) = ID
			while (willMatch(TokenType.ID)) {
				ConstDeclaration constDecl = parseConstDecl();
				constDecls.add(constDecl);
				match(TokenType.SEMI);
			}
		}
		return Collections.unmodifiableList(constDecls);
	}
        
        
        private List<VariableDeclaration> parseVariableDeclSequence(){
	    List<VariableDeclaration> varDecls = new ArrayList<>();
	    while (willMatch(TokenType.VAR)) {
		skip();
		List<VariableDeclaration> varDecl = parseVariableDecl();
		varDecls.addAll(varDecl);
		match(TokenType.SEMI);
	    }
	    return Collections.unmodifiableList(varDecls);
	}

        

	// ConstDecl -> ident =umber
	private ConstDeclaration parseConstDecl() {
		Position start = currentPosition;
		Identifier id = ParseIdentifier();
		match(TokenType.EQ);
		NumValue num = ParseNumValue();
		return new ConstDeclaration(start, id, num);
	}

        private List <VariableDeclaration> parseVariableDecl() {
	    Position start = currentPosition;
	    List<Identifier> identList = new ArrayList<>();
	    while(willMatch(TokenType.ID)){
		Token varname = skip();
		identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
		match(TokenType.COMMA);
	    }
	    match(TokenType.COLON);
	    Token type = match(TokenType.ID);
	    Identifier typeid = new Identifier(type.getPosition(), type.getLexeme());
	    List <VariableDeclaration> varDecl = new ArrayList<>();
	    for(int i = 0; i < identList.size(); i++){
		varDecl.add(new VariableDeclaration(start, identList.get(i), typeid));
	    }
	    return Collections.unmodifiableList(varDecl);
        }

	// StatementSequence -> Statement StatementSequenceRest
	//
	// StatementSequenceRest -> ; Statement StatementSequenceRest
	// StatementSequenceRest ->
	private List<Statement> parseStatementSequence() {
		// TODO Auto-generated method stub
	    List<Statement> statements = new ArrayList<>();
	    while(!willMatch(TokenType.END)){
		Statement s = ParseStatement();
		statements.add(s);
		if(!willMatch(TokenType.END)){
		    match(TokenType.SEMI);
		}
	    }
	    return Collections.unmodifiableList(statements);
	}

	// TODO handle the rest of the grammar:
	//
	// Statement -> ProcedureCall
	// Statement ->
	private Statement ParseStatement(){
	    Position start = currentPosition;
	    Statement pcall = new EmptyStatement(start);
	    if(willMatch(TokenType.ID)) {
		pcall = ParseProcedureCall();
	    }
	    return pcall;
        }
	// ProcedureCall -> ident ( Expression )
        private ProcedureCall ParseProcedureCall(){
	    Identifier ident = ParseIdentifier();
	    Position start = currentPosition;
	    match(TokenType.LPAR);
	    Expression exp = ParseExpression();
	    match(TokenType.RPAR);
	    return new ProcedureCall(start, ident, exp);
        }
	//
	// Expression -> + Term ExprRest
	// Expression -> - Term ExprRest
        private UnaryOperation.OpType ParseUnaryOp() {
	    if(willMatch(TokenType.PLUS)){
		skip();
		return UnaryOperation.OpType.PLUS;
	    } else {
		match(TokenType.MINUS);
		return UnaryOperation.OpType.MINUS;
	    }
        }
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
        private BinaryOperation.OpType ParseAddOp(){
	    if(willMatch(TokenType.PLUS)){
		skip();
		return BinaryOperation.OpType.PLUS;
	    } else {
		match(TokenType.MINUS);
		return BinaryOperation.OpType.MINUS;
	    }
        }
	// Term -> Factor TermRest
        // TermRest -> MulOperator Factor TermRest
	// TermRest ->
        private Expression ParseTerm(){
	    Position start = currentPosition;
	    Expression left = ParseFactor();
	    while (willMatch(TokenType.DIV) || willMatch(TokenType.MOD) || willMatch(TokenType.TIMES)){
		BinaryOperation.OpType op = ParseMultOp();
		Expression right = ParseFactor();
		left = new BinaryOperation(start, left, op, right);
	    }
	    return left;
        }
    
	// MulOperator -> * | DIV | MOD
        private BinaryOperation.OpType ParseMultOp() {
	    if(willMatch(TokenType.TIMES)){
		skip();
		return BinaryOperation.OpType.TIMES;
	    } else if(willMatch(TokenType.DIV)) {
		skip();
		return BinaryOperation.OpType.DIV;
	    } else if(willMatch(TokenType.DIVIDE)){
		skip();
		return BinaryOperation.OpType.DIVIDE;
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
	    } else {
		match(TokenType.LPAR);
		Expression expr = ParseExpression();
		match(TokenType.RPAR);
	        return expr;
	    }
	}
        //ident -> IDENT 
        private Identifier ParseIdentifier() {
	    Token id = match(TokenType.ID);
	    Position start = currentPosition;
	    return new Identifier(start, id.getLexeme());
        }
        //number -> NUM
        private NumValue ParseNumValue() {
	    Token num = match(TokenType.NUM);
	    Position start = currentPosition;
	    return new NumValue(start, num.getLexeme());
        }
}
