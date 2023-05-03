package io.github.H20man13.DeClan.main;

import static io.github.H20man13.DeClan.main.MyIO.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Lexer;
import edu.depauw.declan.common.Parser;
import io.github.H20man13.DeClan.common.ParseException;
import io.github.H20man13.DeClan.common.Position;
import io.github.H20man13.DeClan.common.ast.ConstDeclaration;
import io.github.H20man13.DeClan.common.ast.VariableDeclaration;
import io.github.H20man13.DeClan.common.ast.ProcedureDeclaration;
import io.github.H20man13.DeClan.common.ast.Declaration;
import io.github.H20man13.DeClan.common.ast.Identifier;
import io.github.H20man13.DeClan.common.ast.NumValue;
import io.github.H20man13.DeClan.common.ast.BoolValue;
import io.github.H20man13.DeClan.common.ast.StrValue;
import io.github.H20man13.DeClan.common.ast.Program;
import io.github.H20man13.DeClan.common.ast.Statement;
import io.github.H20man13.DeClan.common.ast.Assignment;
import io.github.H20man13.DeClan.common.ast.EmptyStatement;
import io.github.H20man13.DeClan.common.ast.ElseBranch;
import io.github.H20man13.DeClan.common.ast.IfElifBranch;
import io.github.H20man13.DeClan.common.ast.WhileElifBranch;
import io.github.H20man13.DeClan.common.token.DeClanToken;
import io.github.H20man13.DeClan.common.token.DeClanTokenType;
import io.github.H20man13.DeClan.common.ast.RepeatBranch;
import io.github.H20man13.DeClan.common.ast.Branch;
import io.github.H20man13.DeClan.common.ast.ForBranch;
import io.github.H20man13.DeClan.common.ast.ProcedureCall;
import io.github.H20man13.DeClan.common.ast.FunctionCall;
import io.github.H20man13.DeClan.common.ast.Expression;
import io.github.H20man13.DeClan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.common.ast.UnaryOperation;

/**
 * A parser for a subset of DeCLan consisting only of integer constant
 * declarations and calls to PrintInt with integer expression arguments. This is
 * starter code for CSC426 Project 2.
 * 
 * @author bhoward, Jacob Bauer
 */
public class MyDeClanParser implements Parser {
  private Lexer lexer;
  private ErrorLog errorLog;
  /**
   * Holds the current Token from the Lexer, or null if at end of file
   */
  private DeClanToken current;

  /**
   * Holds the Position of the current Token, or the most recent one if at end of
   * file (or position 0:0 if source file is empty)
   */
  private Position currentPosition;

  public MyDeClanParser(Lexer lexer, ErrorLog errorLog) {
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
  boolean willMatch(DeClanTokenType type) {
    return current != null && current.getType() == type;
  }

  /**
   * If the current token has the given type, skip to the next token and return
   * the matched token. Otherwise, abort and generate an error message.
   * 
   * @param type
   * @return the matched token if successful
   */
  DeClanToken match(DeClanTokenType type) {
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
  DeClanToken skip() {
    DeClanToken token = current;
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
    match(DeClanTokenType.BEGIN);
    List<Statement> statements = parseStatementSequence();
    match(DeClanTokenType.END);
    match(DeClanTokenType.PERIOD);
    matchEOF();
    return new Program(start, Decls, statements);
  }
  // DeclSequence -> CONST ConstDeclSequence VAR VariableDeclSequence ProcedureDeclSequence
  // DeclSequence -> CONST ConstDeclSequence ProcedureDeclSequence
  // DeclSequence -> VAR VariableDeclSequence ProcedureDeclSequence
  // DeclSequence -> ProcedureDeclSequence
  public List<Declaration> parseDeclarationSequence(){
    List<Declaration> Decls = new ArrayList<>();
    if(willMatch(DeClanTokenType.CONST)){
      Decls.addAll(parseConstDeclSequence());
    }
    if(willMatch(DeClanTokenType.VAR)){
      Decls.addAll(parseVariableDeclSequence());
    }
    Decls.addAll(parseProcedureDeclSequence());
    return Decls;
  }

	
  // ConstDeclSequence -> ConstDecl ; ConstDeclSequence
  // ConstDeclSequence ->
  private List<ConstDeclaration> parseConstDeclSequence(){
    List<ConstDeclaration> constDecls = new ArrayList<>();
    if (willMatch(DeClanTokenType.CONST)){
      skip();
      while(willMatch(DeClanTokenType.ID)){
        ConstDeclaration constDecl = parseConstDecl();
        constDecls.add(constDecl);
        match(DeClanTokenType.SEMI);
      }
    }
    return Collections.unmodifiableList(constDecls);
  }
        
  //VariableDeclSequence -> VariableDecl ; VariableDeclSequence
  //VariableDeclSequence ->
  private List<VariableDeclaration> parseVariableDeclSequence(){
    List<VariableDeclaration> varDecls = new ArrayList<>();
    while (willMatch(DeClanTokenType.VAR)) {
      skip();
      List<VariableDeclaration> varDecl = parseVariableDecl();
      varDecls.addAll(varDecl);
      match(DeClanTokenType.SEMI);
    }
    return Collections.unmodifiableList(varDecls);
  }
  
  //ProcedureDeclSequence -> ProcedureDecl ; ProcedureDeclSequence
  //ProcedureDeclSequence ->
  private List<ProcedureDeclaration> parseProcedureDeclSequence(){
    List<ProcedureDeclaration> procDecls = new ArrayList<>();
    while (willMatch(DeClanTokenType.PROCEDURE)) {
      ProcedureDeclaration proc = parseProcedureDecl();
      procDecls.add(proc);
      match(DeClanTokenType.SEMI);
    }
    return Collections.unmodifiableList(procDecls);
  }


  //ProcedureDecl -> ProcedureHead ; ProcedureBody ident
  private ProcedureDeclaration parseProcedureDecl(){
    Position start = currentPosition;
    // ProcedureHead -> PROCEDURE ident FormalParameters
    // ProcedureHead -> PROCEDURE ident
    match(DeClanTokenType.PROCEDURE);
    Identifier procName = parseIdentifier();
    List<VariableDeclaration> fpSequence = new ArrayList<>();
    Identifier returnType = new Identifier(start, "VOID"); //defualt is no return Type
    // FormalParameters -> ( FPSection FPSectionSequence ) : Type
    // FormalParameters -> ( FPSection FPSectionSequence )
    // FormalParameters -> ( ) : Type
    // FormalParameters -> ( )
    if(willMatch(DeClanTokenType.LPAR)){
      skip();
      // FPSectionSequence -> ; FPSection FPSectionSequence
      // FPSectionSequence ->
      // FPSection -> VAR IdentList : Type
      // FPSection -> IdentList : Type
      if(willMatch(DeClanTokenType.ID) || willMatch(DeClanTokenType.VAR)){
	if(willMatch(DeClanTokenType.VAR)){
	  skip();
	}
	List<VariableDeclaration> aSequence = parseVariableDecl();
	fpSequence.addAll(aSequence);
	while(willMatch(DeClanTokenType.SEMI)){
	  skip();
	  if(willMatch(DeClanTokenType.VAR)){
	    skip();
	  }
	  aSequence = parseVariableDecl();
	  fpSequence.addAll(aSequence);
	}
      }
      match(DeClanTokenType.RPAR);
      if(willMatch(DeClanTokenType.COLON)){
        skip();
        returnType = parseIdentifier();
      }
    }
    match(DeClanTokenType.SEMI);
    // ProcedureBody -> DeclSequence BEGIN StatementSequence RETURN Expression END
    // ProcedureBody -> DeclSequence BEGIN StatementSequence END
    // ProcedureBody -> DeclSequence RETURN Expression END
    // ProcedureBody -> DeclSequence END
    List<Declaration> procDeclSequence = parseDeclarationSequence();
    List<Statement> toExecute = new ArrayList<>();
    if(willMatch(DeClanTokenType.BEGIN)){
      skip();
      toExecute = parseStatementSequence();
    }
    Expression retExpression = null;
    if(willMatch(DeClanTokenType.RETURN)){
      skip();
      retExpression = parseExpression();
    }
    match(DeClanTokenType.END);
    Identifier nameCheck = parseIdentifier();
    if(!nameCheck.getLexeme().equals(procName.getLexeme())){
	errorLog.add("Expected -> Identity Given at the end of Procedure Declaration ( " + nameCheck.getLexeme() + " ) is not equal to the Expected Procedure Declaration Name ( " + procName.getLexeme() + " )", start);
    }
    return new ProcedureDeclaration(start, procName, fpSequence, returnType, procDeclSequence, toExecute, retExpression);
  }
        
        
  
  // ConstDecl -> ident = number
  private ConstDeclaration parseConstDecl() {
    Identifier id = parseIdentifier();
    match(DeClanTokenType.EQ);
    Expression num = parseNumValue();
    return new ConstDeclaration(id.getStart(), id, num);
  }

  //VariableDecl -> IdentList : Type
  private List <VariableDeclaration> parseVariableDecl() {
    List<Identifier> identList = new ArrayList<>();
    //IdentList -> ident IdentListRest
    //IdentListRest -> , ident IdentListRest
    //IdentListRest ->
    DeClanToken varname = match(DeClanTokenType.ID);
    identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
    while(willMatch(DeClanTokenType.COMMA)){
      skip();
      varname = match(DeClanTokenType.ID);
      identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
    }
    match(DeClanTokenType.COLON);
    DeClanToken type = match(DeClanTokenType.ID);
    Identifier typeid = new Identifier(type.getPosition(), type.getLexeme());
    List <VariableDeclaration> varDecl = new ArrayList<>();
    for(int i = 0; i < identList.size(); i++){
      Identifier elem = identList.get(i);
      varDecl.add(new VariableDeclaration(elem.getStart(), elem, typeid));
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
    while(willMatch(DeClanTokenType.SEMI)){
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
    if(willMatch(DeClanTokenType.ID)) {
      Identifier ident = parseIdentifier();
      if(willMatch(DeClanTokenType.ASSIGN)) {
	statement = parseAssignment(ident);
      } else {
	statement = parseProcedureCall(ident);
      }
    } else if(willMatch(DeClanTokenType.IF)){
      statement = parseIfStatement();
    } else if(willMatch(DeClanTokenType.WHILE)){
      statement = parseWhileStatement();
    } else if(willMatch(DeClanTokenType.REPEAT)){
      statement = parseRepeatStatement();
    } else if (willMatch(DeClanTokenType.FOR)){
      statement = parseForStatement();
    }
    return statement;
  }
    
  //ProcedureCall -> ident ActualParameters
  private ProcedureCall parseProcedureCall(Identifier nameOfProcedure){
    Position start = currentPosition;
    if(willMatch(DeClanTokenType.LPAR)){
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
    if(willMatch(DeClanTokenType.ELSIF)){
      skip();
      Expression exp = parseExpression();
      match(DeClanTokenType.THEN);
      List<Statement> stats = parseStatementSequence();
      result = new IfElifBranch(start, exp, stats, parseIfBranch());
    } else if(willMatch(DeClanTokenType.ELSE)) {
      skip();
      List<Statement> stats = parseStatementSequence();
      result = new ElseBranch(start, stats);
    } else if (willMatch(DeClanTokenType.END)) {
      result = null;
    } else {
      errorLog.add("Expected ELSIF, END or ELSE token toward the end of if statement", start);
      result = null;
    }
    return result;
  }
  //IfStatement -> IF Expression THEN StatementSequence ElsifSequence ELSE StatementSequence END
  //IfStatement -> IF Expression THEN StatementSequence ElsifSequence END
  private IfElifBranch parseIfStatement(){
    Position start = currentPosition; 
    match(DeClanTokenType.IF);
    Expression ifExpr = parseExpression();
    match(DeClanTokenType.THEN);
    List<Statement> topStatements = parseStatementSequence();
    IfElifBranch topBranch = new IfElifBranch(start, ifExpr, topStatements, parseIfBranch());
    match(DeClanTokenType.END);
    return topBranch;
  }

  //ElsifDoSequence -> ELSIF Expression DO StatementSequence ElsifDoSequence
  //ElsifDoSequence ->
  private WhileElifBranch parseWhileBranch(){
    Position start = currentPosition;
    WhileElifBranch result;
    if(willMatch(DeClanTokenType.ELSIF)){
      skip();
      Expression exp = parseExpression();
      match(DeClanTokenType.DO);
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
    match(DeClanTokenType.WHILE);
    Expression whileExpr = parseExpression();
    match(DeClanTokenType.DO);
    List<Statement> topStatements = parseStatementSequence();
    WhileElifBranch topBranch = new WhileElifBranch(start, whileExpr, topStatements, parseWhileBranch());
    match(DeClanTokenType.END);
    return topBranch;
  }

  //RepeatStatement -> REPEAT StatementSequence UNTIL Expression
  private RepeatBranch parseRepeatStatement(){
    Position start = currentPosition;
    match(DeClanTokenType.REPEAT);
    List<Statement> topStatements = parseStatementSequence();
    match(DeClanTokenType.UNTIL);
    Expression endExpr = parseExpression();
    return new RepeatBranch(start, topStatements, endExpr);
  }

  //ForStatement -> FOR ident := Expression TO Expression BY ConstExpr DO StatementSequence END
  //ForStatement -> FOR ident := Expression TO Expression DO StatementSequence END
  private ForBranch parseForStatement(){
    Position start = currentPosition;
    match(DeClanTokenType.FOR);
    Identifier parseIdent = parseIdentifier();
    Assignment assign = parseAssignment(parseIdent);
    match(DeClanTokenType.TO);
    Expression toCheck = parseExpression();
    Expression toChange = null;
    if(willMatch(DeClanTokenType.BY)){
      skip();
      toChange = parseExpression();
    }
    match(DeClanTokenType.DO);
    List<Statement> toDo = parseStatementSequence();
    match(DeClanTokenType.END);
    return new ForBranch(start, assign, toCheck, toChange, toDo);
  }

  //Assignment -> ident := Expression
  private Assignment parseAssignment(Identifier toBeAssigned){
    Position start = currentPosition;
    match(DeClanTokenType.ASSIGN);
    Expression exp = parseExpression();
    return new Assignment(start, toBeAssigned, exp);
  }

  //ExpList -> Expression ExpListRest
  //ExpListRest -> , Expression
  //ExpListRest ->
  private List<Expression> parseExpressionList(){
    List<Expression> expList = new ArrayList<Expression>();
    Expression exp = parseExpression();
    expList.add(exp);
    while(willMatch(DeClanTokenType.COMMA)){
      skip();
      exp = parseExpression();
      expList.add(exp);
    }
    return Collections.unmodifiableList(expList);
  }
  //ActualParameters -> ( ExpList )
  //ActualParameters -> ( )
  private List<Expression> parseActualParameters(){
    match(DeClanTokenType.LPAR);
    List<Expression> elist = new ArrayList<>();
    if(!willMatch(DeClanTokenType.RPAR)){
      elist = parseExpressionList();
    }
    match(DeClanTokenType.RPAR);
    return Collections.unmodifiableList(elist);
  }
  //Expression -> SimpleExpr
  //Expression -> SimpleExpr Relation SimpleExpr
  private Expression parseExpression(){
    Position start = currentPosition;
    Expression left = parseSimpleExpression();
    if(willMatch(DeClanTokenType.NE) || willMatch(DeClanTokenType.LE) || willMatch(DeClanTokenType.LT) || willMatch(DeClanTokenType.EQ) || willMatch(DeClanTokenType.GT) || willMatch(DeClanTokenType.GE)){
      BinaryOperation.OpType op = parseBoolOp();
      Expression right = parseSimpleExpression();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
        
  private BinaryOperation.OpType parseBoolOp() {
    if(willMatch(DeClanTokenType.NE)){
      skip();
      return BinaryOperation.OpType.NE;
    } else if(willMatch(DeClanTokenType.EQ)){
      skip();
      return BinaryOperation.OpType.EQ;
    } else if(willMatch(DeClanTokenType.LT)){
      skip();
      return BinaryOperation.OpType.LT;
    } else if(willMatch(DeClanTokenType.GT)){
      skip();
      return BinaryOperation.OpType.GT;
    } else if(willMatch(DeClanTokenType.GE)){
      skip();
      return BinaryOperation.OpType.GE;
    } else {
      match(DeClanTokenType.LE);
      return BinaryOperation.OpType.LE;
    }
  }
  //SimpleExpr -> + Term SimpleExprRest
  //SimpleExpr -> - Term SimpleExprRest
  //SimpleExpr -> Term SimpleExprRest
  private Expression parseSimpleExpression(){
    Position start = currentPosition; 
    Expression left;
    if(willMatch(DeClanTokenType.MINUS) || willMatch(DeClanTokenType.PLUS)){
      UnaryOperation.OpType pm = parseUnaryOp();
      left = parseTerm();
      left = new UnaryOperation(start, pm, left);
    } else {
      left = parseTerm();
    }
    while(willMatch(DeClanTokenType.PLUS) || willMatch(DeClanTokenType.MINUS)){
      BinaryOperation.OpType op = parseAddOp();
      Expression right = parseTerm();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
    
  private UnaryOperation.OpType parseUnaryOp() {
    if(willMatch(DeClanTokenType.PLUS)){
      skip();
      return UnaryOperation.OpType.PLUS;
    } else if (willMatch(DeClanTokenType.MINUS)){
      skip();
      return UnaryOperation.OpType.MINUS;
    } else {
      match(DeClanTokenType.NOT);
      return UnaryOperation.OpType.NOT;
    }
  }
  // AddOperator -> + | -
  private BinaryOperation.OpType parseAddOp(){
    if(willMatch(DeClanTokenType.PLUS)){
      skip();
      return BinaryOperation.OpType.PLUS;
    } else if(willMatch(DeClanTokenType.OR)){
      skip();
      return BinaryOperation.OpType.OR;
    } else {
      match(DeClanTokenType.MINUS);
      return BinaryOperation.OpType.MINUS;
    }
  }
  // Term -> Factor TermRest
  // TermRest -> MulOperator Factor TermRest
  // TermRest ->
  private Expression parseTerm(){
    Position start = currentPosition;
    Expression left = parseFactor();
    while (willMatch(DeClanTokenType.DIV) || willMatch(DeClanTokenType.MOD) || willMatch(DeClanTokenType.TIMES) || willMatch(DeClanTokenType.DIVIDE)){
      BinaryOperation.OpType op = parseMultOp();
      Expression right = parseFactor();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
  // MulOperator -> * | DIV | MOD
  private BinaryOperation.OpType parseMultOp() {
    if(willMatch(DeClanTokenType.TIMES)){
      skip();
      return BinaryOperation.OpType.TIMES;
    } else if(willMatch(DeClanTokenType.DIV)) {
      skip();
      return BinaryOperation.OpType.DIV;
    } else if(willMatch(DeClanTokenType.DIVIDE)){
      skip();
      return BinaryOperation.OpType.DIVIDE;
    } else if(willMatch(DeClanTokenType.AND)){
      skip();
      return BinaryOperation.OpType.AND;
    } else {
      match(DeClanTokenType.MOD);
      return BinaryOperation.OpType.MOD;
    }
  }
  // Factor -> number | ident | string | functioncall | TRUE | FALSE | ~FACTOR
  // Factor -> ( Expression )
  private Expression parseFactor(){
    if(willMatch(DeClanTokenType.NUM)){
      return parseNumValue(); 
    } else if (willMatch(DeClanTokenType.TRUE) || willMatch(DeClanTokenType.FALSE)){
      return parseBoolValue();
    } else if(willMatch(DeClanTokenType.ID)){
      DeClanToken id = skip();
      Position start = currentPosition;
      if(willMatch(DeClanTokenType.LPAR)){
	List<Expression> expList = parseActualParameters();
	return new FunctionCall(start, new Identifier(start, id.getLexeme()), expList);
      } else {
	return parseIdentifier(id);
      }
    } else if (willMatch(DeClanTokenType.STRING)){
      return parseStrValue();
    } else if (willMatch(DeClanTokenType.NOT)){
      Position start = currentPosition;
      UnaryOperation.OpType not = parseUnaryOp();
      Expression exp = parseFactor();
      return new UnaryOperation(start, not, exp);
    } else {
      match(DeClanTokenType.LPAR);
      Expression expr = parseExpression();
      match(DeClanTokenType.RPAR);
      return expr;
    }
  }
  //ident -> IDENT 
  private Identifier parseIdentifier() {
    DeClanToken id = match(DeClanTokenType.ID);
    Position start = currentPosition;
    return new Identifier(start, id.getLexeme());
  }
  //ident -> IDENT 
  private Identifier parseIdentifier(DeClanToken id) {
    Position start = currentPosition;
    return new Identifier(start, id.getLexeme());
  }
  
  //number -> NUM
  private NumValue parseNumValue() {
    DeClanToken num = match(DeClanTokenType.NUM);
    Position start = currentPosition;
    return new NumValue(start, num.getLexeme());
  }

  //string -> STRING
  private StrValue parseStrValue() {
    DeClanToken str = match(DeClanTokenType.STRING);
    Position start = currentPosition;
    return new StrValue(start, str.getLexeme());
  }
  //Number == true of false
  private BoolValue parseBoolValue() {
    if(willMatch(DeClanTokenType.TRUE)){
      skip();
      Position start = currentPosition;
      return new BoolValue(start, "TRUE");
    } else if (willMatch(DeClanTokenType.FALSE)){
      skip();
      Position start = currentPosition;
      return new BoolValue(start, "FALSE");
    } else {
      Position start = currentPosition;
      errorLog.add("Expected True or False value to enter the function", start);
      return null;
    }
  }
}
