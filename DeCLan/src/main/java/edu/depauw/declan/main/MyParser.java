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
import edu.depauw.declan.common.ast.ProcedureDeclaration;
import edu.depauw.declan.common.ast.Declaration;
import edu.depauw.declan.common.ast.Identifier;
import edu.depauw.declan.common.ast.NumValue;
import edu.depauw.declan.common.ast.StrValue;
import edu.depauw.declan.common.ast.Program;
import edu.depauw.declan.common.ast.Statement;
import edu.depauw.declan.common.ast.Assignment;
import edu.depauw.declan.common.ast.ForAssignment;
import edu.depauw.declan.common.ast.EmptyStatement;
import edu.depauw.declan.common.ast.ElseBranch;
import edu.depauw.declan.common.ast.IfElifBranch;
import edu.depauw.declan.common.ast.WhileElifBranch;
import edu.depauw.declan.common.ast.RepeatBranch;
import edu.depauw.declan.common.ast.Branch;
import edu.depauw.declan.common.ast.ForBranch;
import edu.depauw.declan.common.ast.ProcedureCall;
import edu.depauw.declan.common.ast.FunctionCall;
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
  // DeclSequence -> CONST ConstDeclSequence VAR VariableDeclSequence ProcedureDeclSequence
  // DeclSequence -> CONST ConstDeclSequence ProcedureDeclSequence
  // DeclSequence -> VAR VariableDeclSequence ProcedureDeclSequence
  // DeclSequence -> ProcedureDeclSequence
  public List<Declaration> parseDeclarationSequence(){
    List<Declaration> Decls = new ArrayList<>();
    if(willMatch(TokenType.CONST)){
      Decls.addAll(parseConstDeclSequence());
    }
    if(willMatch(TokenType.VAR)){
      Decls.addAll(parseVariableDeclSequence());
    }
    Decls.addAll(parseProcedureDeclSequence());
    return Decls;
  }

	
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
        
  //VariableDecl -> IdentList : Type
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
  
  //ProcedureDeclSequence -> ProcedureDecl ; ProcedureDeclSequence
  //ProcedureDeclSequence ->
  private List<ProcedureDeclaration> parseProcedureDeclSequence(){
    List<ProcedureDeclaration> procDecls = new ArrayList<>();
    while (willMatch(TokenType.PROCEDURE)) {
      ProcedureDeclaration proc = parseProcedureDecl();
      procDecls.add(proc);
      match(TokenType.SEMI);
    }
    return Collections.unmodifiableList(procDecls);
  }


  //ProcedureDecl -> ProcedureHead ; ProcedureBody ident
  private ProcedureDeclaration parseProcedureDecl(){
    Position start = currentPosition;
    // ProcedureHead -> PROCEDURE ident FormalParameters
    // ProcedureHead -> PROCEDURE ident
    match(TokenType.PROCEDURE);
    Identifier procName = parseIdentifier();
    List<VariableDeclaration> fpSequence = new ArrayList<>();
    Identifier returnType = new Identifier(start, "VOID"); //defualt is no return Type
    // FormalParameters -> ( FPSection FPSectionSequence ) : Type
    // FormalParameters -> ( FPSection FPSectionSequence )
    // FormalParameters -> ( ) : Type
    // FormalParameters -> ( )
    if(willMatch(TokenType.LPAR)){
      skip();
      // FPSectionSequence -> ; FPSection FPSectionSequence
      // FPSectionSequence ->
      while(!willMatch(TokenType.RPAR)){
	//FPSection -> VAR IdentList : Type
	//FPSection -> IdentList : Type
	if(willMatch(TokenType.VAR)){
	  skip();
	}
	List<VariableDeclaration> aSequence = parseVariableDecl();
	fpSequence.addAll(aSequence);
	if(willMatch(TokenType.SEMI)){
	  skip();
	} else {
	  break;
	}
      }
      match(TokenType.RPAR);
      if(willMatch(TokenType.COLON)){
	skip();
	returnType = parseIdentifier();
      }
    }
    match(TokenType.SEMI);
    // ProcedureBody -> DeclSequence BEGIN StatementSequence RETURN Expression END
    // ProcedureBody -> DeclSequence BEGIN StatementSequence END
    // ProcedureBody -> DeclSequence RETURN Expression END
    // ProcedureBody -> DeclSequence END
    List<Declaration> procDeclSequence = parseDeclarationSequence();
    List<Statement> toExecute = null;
    if(willMatch(TokenType.BEGIN)){
      skip();
      toExecute = parseStatementSequence();
    }
    Expression retExpression = null;
    if(willMatch(TokenType.RETURN)){
      skip();
      retExpression = parseExpression();
    }
    match(TokenType.END);
    Identifier nameCheck = parseIdentifier();
    if(!nameCheck.getLexeme().equals(procName.getLexeme())){
      FATAL("Expected -> identity at the end of the function declaration must be the same as the function name");
    }
    return new ProcedureDeclaration(start, procName, fpSequence, returnType, procDeclSequence, toExecute, retExpression);
  }
        
        
  
  // ConstDecl -> ident = number
  private ConstDeclaration parseConstDecl() {
    Position start = currentPosition;
    Identifier id = parseIdentifier();
    match(TokenType.EQ);
    NumValue num = parseNumValue();
    return new ConstDeclaration(start, id, num);
  }
  //IdentList -> ident IdentListRest
  //IdentListRest -> , ident IdentListRest
  //IdentListRest ->
  private List <VariableDeclaration> parseVariableDecl() {
    Position start = currentPosition;
    List<Identifier> identList = new ArrayList<>();
    while(!willMatch(TokenType.COLON)){
      Token varname = match(TokenType.ID);
      identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
      if(!willMatch(TokenType.COMMA)){
	match(TokenType.COLON);
	break;
      } else {
	skip();
      }
    }
    Token type = match(TokenType.ID);
    Identifier typeid = new Identifier(type.getPosition(), type.getLexeme());
    List <VariableDeclaration> varDecl = new ArrayList<>();
    for(int i = 0; i < identList.size(); i++){
      varDecl.add(new VariableDeclaration(start, identList.get(i), typeid));
    }
    return Collections.unmodifiableList(varDecl);
  }

  // StatementSequence -> Statement StatementSequenceRest
  // StatementSequenceRest -> ; Statement StatementSequenceRest
  // StatementSequenceRest ->
  private List<Statement> parseStatementSequence() {
    // TODO Auto-generated method stub
    List<Statement> statements = new ArrayList<>();
    Statement s = parseStatement();
    statements.add(s);
    while(willMatch(TokenType.SEMI)){
      skip();
      s = parseStatement();
      statements.add(s);
    }
    return Collections.unmodifiableList(statements);
  }
  //Statement -> Assignment | ProcedureCall | IfStatement | WhileStatement | RepeatStatement | ForStatement
  //Statement ->
  private Statement parseStatement(){
    Position start = currentPosition;
    Statement statement = new EmptyStatement(start);
    if(willMatch(TokenType.ID)) {
      Identifier ident = parseIdentifier();
      if(willMatch(TokenType.ASSIGN)) {
	statement = parseAssignment(ident);
      } else {
	statement = parseProcedureCall(ident);
      }
    } else if(willMatch(TokenType.IF)){
      statement = parseIfStatement();
    } else if(willMatch(TokenType.WHILE)){
      statement = parseWhileStatement();
    } else if(willMatch(TokenType.REPEAT)){
      statement = parseRepeatStatement();
    } else if (willMatch(TokenType.FOR)){
      statement = parseForStatement();
    }
    return statement;
  }
    
  //ProcedureCall -> ident ActualParameters
  private ProcedureCall parseProcedureCall(Identifier nameOfProcedure){
    Position start = currentPosition;
    if(willMatch(TokenType.LPAR)){
      List<Expression> expList = parseActualParameters();
      return new ProcedureCall(start, nameOfProcedure, expList);
    }
    return new ProcedureCall(start, nameOfProcedure);
  }
   
  //ElsifThenSequence -> ELSIF Expression THEN StatementSequence ElsifThenSequence
  //ElsifThenSequence ->
  private Branch parseIfBranch(){
    Position start = currentPosition;
    Branch result;
    if(willMatch(TokenType.ELSIF)){
      skip();
      Expression exp = parseExpression();
      match(TokenType.THEN);
      List<Statement> stats = parseStatementSequence();
      result = new IfElifBranch(start, exp, stats, parseIfBranch());
    } else if(willMatch(TokenType.ELSE)) {
      skip();
      List<Statement> stats = parseStatementSequence();
      result = new ElseBranch(start, stats);
    } else if (willMatch(TokenType.END)) {
      result = null;
    } else {
      FATAL("Expected ELSIF, END or ELSE token toward the end of if statement");
      result = null;
    }
    return result;
  }
  //IfStatement -> IF Expression THEN StatementSequence ElsifSequence ELSE StatementSequence END
  //IfStatement -> IF Expression THEN StatementSequence ElsifSequence END
  private IfElifBranch parseIfStatement(){
    Position start = currentPosition; 
    match(TokenType.IF);
    Expression ifExpr = parseExpression();
    match(TokenType.THEN);
    List<Statement> topStatements = parseStatementSequence();
    IfElifBranch topBranch = new IfElifBranch(start, ifExpr, topStatements, parseIfBranch());
    match(TokenType.END);
    return topBranch;
  }

  //ElsifDoSequence -> ELSIF Expression DO StatementSequence ElsifDoSequence
  //ElsifDoSequence ->
  private WhileElifBranch parseWhileBranch(){
    Position start = currentPosition;
    WhileElifBranch result;
    if(willMatch(TokenType.ELSIF)){
      skip();
      Expression exp = parseExpression();
      match(TokenType.DO);
      List<Statement> stats = parseStatementSequence();
      result  = new WhileElifBranch(start, exp, stats, parseIfBranch());
    } else {
      result = null;
    }
    return result;
  }
        
  //WhileStatement -> WHILE Expression DO StatementSequence ElsifDoSequence END
  private WhileElifBranch parseWhileStatement(){
    Position start = currentPosition; 
    match(TokenType.WHILE);
    Expression whileExpr = parseExpression();
    match(TokenType.DO);
    List<Statement> topStatements = parseStatementSequence();
    WhileElifBranch topBranch = new WhileElifBranch(start, whileExpr, topStatements, parseWhileBranch());
    match(TokenType.END);
    return topBranch;
  }

  //RepeatStatement -> REPEAT StatementSequence UNTIL Expression
  private RepeatBranch parseRepeatStatement(){
    Position start = currentPosition;
    match(TokenType.REPEAT);
    List<Statement> topStatements = parseStatementSequence();
    match(TokenType.UNTIL);
    Expression endExpr = parseExpression();
    return new RepeatBranch(start, topStatements, endExpr);
  }

  //ForStatement -> FOR ident := Expression TO Expression BY ConstExpr DO StatementSequence END
  //ForStatement -> FOR ident := Expression TO Expression DO StatementSequence END
  private ForBranch parseForStatement(){
    Position start = currentPosition;
    match(TokenType.FOR);
    Identifier parseIdent = parseIdentifier();
    ForAssignment assign = new ForAssignment(parseAssignment(parseIdent));
    match(TokenType.TO);
    Expression toCheck = parseExpression();
    Expression toChange = null;
    if(willMatch(TokenType.BY)){
      skip();
      toChange = parseExpression();
    }
    match(TokenType.DO);
    List<Statement> toDo = parseStatementSequence();
    match(TokenType.END);
    return new ForBranch(start, assign, toCheck, toChange, toDo);
  }

  //Assignment -> ident := Expression
  private Assignment parseAssignment(Identifier toBeAssigned){
    Position start = currentPosition;
    match(TokenType.ASSIGN);
    Expression exp = parseExpression();
    return new Assignment(start, toBeAssigned, exp);
  }

  //ExpList -> Expression ExpListRest
  //ExpListRest -> , Expression
  //ExpListRest ->
  private List<Expression> parseExpressionList(){
    List<Expression> expList = new ArrayList<Expression>();
    while(!willMatch(TokenType.RPAR)){ //check if it is a factor or a term
      Expression exp = parseExpression();
      expList.add(exp);
      if(willMatch(TokenType.COMMA)){
	skip();
	continue;
      } else {
	break;
      }
    }
    return Collections.unmodifiableList(expList);
  }
  //ActualParameters -> ( ExpList )
  //ActualParameters -> ( )
  private List<Expression> parseActualParameters(){
    match(TokenType.LPAR);
    List<Expression> elist = parseExpressionList();
    match(TokenType.RPAR);
    return Collections.unmodifiableList(elist);
  }
  //Expression -> SimpleExpr
  //Expression -> SimpleExpr Relation SimpleExpr
  private Expression parseExpression(){
    Position start = currentPosition;
    Expression left = parseSimpleExpression();
    if(willMatch(TokenType.NE) || willMatch(TokenType.LE) || willMatch(TokenType.LT) || willMatch(TokenType.EQ) || willMatch(TokenType.GT) || willMatch(TokenType.GE)){
      BinaryOperation.OpType op = parseBoolOp();
      Expression right = parseSimpleExpression();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
        
  private BinaryOperation.OpType parseBoolOp() {
    if(willMatch(TokenType.NE)){
      skip();
      return BinaryOperation.OpType.NE;
    } else if(willMatch(TokenType.EQ)){
      skip();
      return BinaryOperation.OpType.EQ;
    } else if(willMatch(TokenType.LT)){
      skip();
      return BinaryOperation.OpType.LT;
    } else if(willMatch(TokenType.GT)){
      skip();
      return BinaryOperation.OpType.GT;
    } else if(willMatch(TokenType.GE)){
      skip();
      return BinaryOperation.OpType.GE;
    } else {
      match(TokenType.LE);
      return BinaryOperation.OpType.LE;
    }
  }
  //SimpleExpr -> + Term SimpleExprRest
  //SimpleExpr -> - Term SimpleExprRest
  //SimpleExpr -> Term SimpleExprRest
  private Expression parseSimpleExpression(){
    Position start = currentPosition; 
    Expression left;
    if(willMatch(TokenType.MINUS) || willMatch(TokenType.PLUS)){
      UnaryOperation.OpType pm = parseUnaryOp();
      left = parseTerm();
      left = new UnaryOperation(start, pm, left);
    } else {
      left = parseTerm();
    }
    while(willMatch(TokenType.PLUS) || willMatch(TokenType.MINUS)){
      BinaryOperation.OpType op = parseAddOp();
      Expression right = parseTerm();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
  //
  // Expression -> + Term ExprRest
  // Expression -> - Term ExprRest
  private UnaryOperation.OpType parseUnaryOp() {
    if(willMatch(TokenType.PLUS)){
      skip();
      return UnaryOperation.OpType.PLUS;
    } else {
      match(TokenType.MINUS);
      return UnaryOperation.OpType.MINUS;
    }
  }
  // AddOperator -> + | -
  private BinaryOperation.OpType parseAddOp(){
    if(willMatch(TokenType.PLUS)){
      skip();
      return BinaryOperation.OpType.PLUS;
    } else if(willMatch(TokenType.OR)){
      skip();
      return BinaryOperation.OpType.OR;
    } else {
      match(TokenType.MINUS);
      return BinaryOperation.OpType.MINUS;
    }
  }
  // Term -> Factor TermRest
  // TermRest -> MulOperator Factor TermRest
  // TermRest ->
  private Expression parseTerm(){
    Position start = currentPosition;
    Expression left = parseFactor();
    while (willMatch(TokenType.DIV) || willMatch(TokenType.MOD) || willMatch(TokenType.TIMES)){
      BinaryOperation.OpType op = parseMultOp();
      Expression right = parseFactor();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
  // MulOperator -> * | DIV | MOD
  private BinaryOperation.OpType parseMultOp() {
    if(willMatch(TokenType.TIMES)){
      skip();
      return BinaryOperation.OpType.TIMES;
    } else if(willMatch(TokenType.DIV)) {
      skip();
      return BinaryOperation.OpType.DIV;
    } else if(willMatch(TokenType.DIVIDE)){
      skip();
      return BinaryOperation.OpType.DIVIDE;
    } else if(willMatch(TokenType.AND)){
      skip();
      return BinaryOperation.OpType.AND;
    } else {
      match(TokenType.MOD);
      return BinaryOperation.OpType.MOD;
    }
  }
  // Factor -> number | ident | string
  // Factor -> ( Expression )
  private Expression parseFactor(){
    if(willMatch(TokenType.NUM)){
      return parseNumValue(); 
    } else if (willMatch(TokenType.TRUE) || willMatch(TokenType.FALSE)){
      return parseBoolValue();
    } else if(willMatch(TokenType.ID)){
      Token id = skip();
      Position start = currentPosition;
      if(willMatch(TokenType.LPAR)){
	List<Expression> expList = parseActualParameters();
	return new FunctionCall(start, new Identifier(start, id.getLexeme()), expList);
      } else {
	return parseIdentifier(id);
      }
    } else if (willMatch(TokenType.STRING)){
      return parseStrValue();
    } else {
      match(TokenType.LPAR);
      Expression expr = parseExpression();
      match(TokenType.RPAR);
      return expr;
    }
  }
  //ident -> IDENT 
  private Identifier parseIdentifier() {
    Token id = match(TokenType.ID);
    Position start = currentPosition;
    return new Identifier(start, id.getLexeme());
  }
  //ident -> IDENT 
  private Identifier parseIdentifier(Token id) {
    Position start = currentPosition;
    return new Identifier(start, id.getLexeme());
  }
  
  //number -> NUM
  private NumValue parseNumValue() {
    Token num = match(TokenType.NUM);
    Position start = currentPosition;
    return new NumValue(start, num.getLexeme());
  }

  //number -> NUM
  private StrValue parseStrValue() {
    Token str = match(TokenType.STRING);
    Position start = currentPosition;
    return new StrValue(start, str.getLexeme());
  }
  //Number == true of false
  private NumValue parseBoolValue() {
    if(willMatch(TokenType.TRUE)){
      skip();
      Position start = currentPosition;
      return new NumValue(start, "1");
    } else if (willMatch(TokenType.FALSE)){
      skip();
      Position start = currentPosition;
      return new NumValue(start, "0");
    } else {
      FATAL("Expected True or False value to enter the function");
      return null;
    }
  }
}
