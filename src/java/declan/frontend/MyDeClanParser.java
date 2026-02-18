package io.github.h20man13.DeClan.main;

import java.io.Closeable;

import static io.github.h20man13.DeClan.main.MyIO.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.github.h20man13.DeClan.common.Parser;
import io.github.h20man13.DeClan.common.ErrorLog;
import io.github.h20man13.DeClan.common.ast.*;
import io.github.h20man13.DeClan.common.exception.ParseException;
import io.github.h20man13.DeClan.common.position.Position;
import io.github.h20man13.DeClan.common.token.DeclanToken;
import io.github.h20man13.DeClan.common.token.DeclanTokenType;
import io.github.h20man13.DeClan.main.MyDeClanLexer;

/**
 * A parser for a subset of DeCLan consisting only of integer constant
 * declarations and calls to PrintInt with integer expression arguments. This is
 * starter code for CSC426 Project 2.
 * 
 * @author bhoward, Jacob Bauer
 */
public class MyDeClanParser implements Parser{
  private MyDeClanLexer lexer;
  private ErrorLog errorLog;
  /**
   * Holds the current DeclanToken from the Lexer, or null if at end of file
   */
  private DeclanToken current;

  /**
   * Holds the Position of the current DeclanToken, or the most recent one if at end of
   * file (or position 0:0 if source file is empty)
   */
  private Position currentPosition;

  public MyDeClanParser(MyDeClanLexer lexer, ErrorLog errorLog) {
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
   * @return true if the DeclanTokenType matches the current token
   */
  boolean willMatch(DeclanTokenType type) {
    return current != null && current.getType() == type;
  }

  /**
   * If the current token has the given type, skip to the next token and return
   * the matched token. Otherwise, abort and generate an error message.
   * 
   * @param type
   * @return the matched token if successful
   */
  DeclanToken match(DeclanTokenType type) {
    if (willMatch(type)) {
      return skip();
    } else if (current == null) {
      errorLog.add("Expected " + type + ", found end of file", currentPosition);
      throw new ParseException("Expected " + type + ", found end of file at " + currentPosition);
    } else {
      errorLog.add("Expected " + type + ", found " + current.getType(), currentPosition);
      throw new ParseException("Expected " + type + ", found " + current.getType() + " at " + currentPosition);
    }
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
  DeclanToken skip() {
    DeclanToken token = current;
    if (lexer.hasNext()) {
      current = lexer.next();
      currentPosition = current.getPosition();
    } else {
      current = null;
      // keep previous value of currentPosition
    }
    return token;
  }

  private boolean skipIfYummy(DeclanTokenType type){
    if(willMatch(type)){
      skip();
      return true;
    } else {
      return false;
    }
  }
  // Library -> DeclDequence
  public Library parseLibrary(){
    Position start = currentPosition;
    List<ConstDeclaration> constDeclarations = new LinkedList<ConstDeclaration>();
    if(willMatch(DeclanTokenType.CONST)){
      skip();
      constDeclarations.addAll(parseConstDeclSequence());
    }
    List<VariableDeclaration> varDeclarations = new LinkedList<VariableDeclaration>();
    if(willMatch(DeclanTokenType.VAR)){
      skip();
      varDeclarations.addAll(parseVariableDeclSequence());
    }
    List<ProcedureDeclaration> procDeclarations = new LinkedList<ProcedureDeclaration>();
    procDeclarations.addAll(parseProcedureDeclSequence());

    return new Library(start, constDeclarations, varDeclarations, procDeclarations);
  }
  
  // Program -> DeclSequence BEGIN StatementSequence END
  @Override
  public Program parseProgram() {
    Position start = currentPosition;
    List<ConstDeclaration> constDeclarations = new LinkedList<ConstDeclaration>();
    if(willMatch(DeclanTokenType.CONST)){
      skip();
      constDeclarations.addAll(parseConstDeclSequence());
    }
    List<VariableDeclaration> varDeclarations = new LinkedList<VariableDeclaration>();
    if(willMatch(DeclanTokenType.VAR)){
      skip();
      varDeclarations.addAll(parseVariableDeclSequence());
    }
    List<ProcedureDeclaration> procDeclarations = new LinkedList<ProcedureDeclaration>();
    procDeclarations.addAll(parseProcedureDeclSequence());
    match(DeclanTokenType.BEGIN);
    List<Statement> statements = parseStatementSequence();
    match(DeclanTokenType.END);
    match(DeclanTokenType.PERIOD);
    matchEOF();
    return new Program(start, constDeclarations, varDeclarations, procDeclarations, statements);
  }
  // DeclSequence -> CONST ConstDeclSequence VAR VariableDeclSequence ProcedureDeclSequence
  // DeclSequence -> CONST ConstDeclSequence ProcedureDeclSequence
  // DeclSequence -> VAR VariableDeclSequence ProcedureDeclSequence
  // DeclSequence -> ProcedureDeclSequence
  public List<Declaration> parseDeclarationSequence(){
    List<Declaration> Decls = new ArrayList<>();
    if(willMatch(DeclanTokenType.CONST)){
      skip();
      Decls.addAll(parseConstDeclSequence());
    }
    if(willMatch(DeclanTokenType.VAR)){
      skip();
      Decls.addAll(parseVariableDeclSequence());
    }
    Decls.addAll(parseProcedureDeclSequence());
    return Decls;
  }

	
  // ConstDeclSequence -> ConstDecl ; ConstDeclSequence
  // ConstDeclSequence ->
  private List<ConstDeclaration> parseConstDeclSequence(){
    List<ConstDeclaration> constDecls = new ArrayList<>();
    while(willMatch(DeclanTokenType.ID)){
      ConstDeclaration constDecl = parseConstDecl();
      constDecls.add(constDecl);
      match(DeclanTokenType.SEMI);
    }
    return Collections.unmodifiableList(constDecls);
  }
        
  //VariableDeclSequence -> VariableDecl ; VariableDeclSequence
  //VariableDeclSequence ->
  private List<VariableDeclaration> parseVariableDeclSequence(){
    List<VariableDeclaration> varDecls = new ArrayList<>();
    while (willMatch(DeclanTokenType.ID)) {
      List<VariableDeclaration> varDecl = parseVariableDecl();
      varDecls.addAll(varDecl);
      match(DeclanTokenType.SEMI);
    }
    return Collections.unmodifiableList(varDecls);
  }
  
  //ProcedureDeclSequence -> ProcedureDecl ; ProcedureDeclSequence
  //ProcedureDeclSequence ->
  private List<ProcedureDeclaration> parseProcedureDeclSequence(){
    List<ProcedureDeclaration> procDecls = new ArrayList<>();
    while (willMatch(DeclanTokenType.PROCEDURE)) {
      ProcedureDeclaration proc = parseProcedureDecl();
      procDecls.add(proc);
      match(DeclanTokenType.SEMI);
    }
    return Collections.unmodifiableList(procDecls);
  }


  //ProcedureDecl -> ProcedureHead ; ProcedureBody ident
  private ProcedureDeclaration parseProcedureDecl(){
    Position start = currentPosition;
    // ProcedureHead -> PROCEDURE ident FormalParameters
    // ProcedureHead -> PROCEDURE ident
    match(DeclanTokenType.PROCEDURE);
    Identifier procName = parseIdentifier();
    List<ParamaterDeclaration> fpSequence = new ArrayList<>();
    Identifier returnType = new Identifier(start, "NA"); //defualt is no return Type
    // FormalParameters -> ( FPSection FPSectionSequence ) : Type
    // FormalParameters -> ( FPSection FPSectionSequence )
    // FormalParameters -> ( ) : Type
    // FormalParameters -> ( )
    if(willMatch(DeclanTokenType.LPAR)){
      skip();
      // FPSectionSequence -> ; FPSection FPSectionSequence
      // FPSectionSequence ->
      // FPSection -> VAR IdentList : Type
      // FPSection -> IdentList : Type
      if(willMatch(DeclanTokenType.ID) || willMatch(DeclanTokenType.VAR)){
	      if(willMatch(DeclanTokenType.VAR)){
	        skip();
	      }
	      List<ParamaterDeclaration> aSequence = parseParamDecl();
	      fpSequence.addAll(aSequence);
        while(willMatch(DeclanTokenType.SEMI)){
          skip();
          if(willMatch(DeclanTokenType.VAR)){
            skip();
          }
          aSequence = parseParamDecl();
          fpSequence.addAll(aSequence);
        }
      }
      match(DeclanTokenType.RPAR);
      if(willMatch(DeclanTokenType.COLON)){
        skip();
        returnType = parseIdentifier();
      }
    }
    match(DeclanTokenType.SEMI);
    // ProcedureBody -> DeclSequence BEGIN StatementSequence RETURN Expression END
    // ProcedureBody -> DeclSequence BEGIN StatementSequence END
    // ProcedureBody -> DeclSequence RETURN Expression END
    // ProcedureBody -> DeclSequence END
    List<Declaration> procDeclSequence = parseDeclarationSequence();
    List<Statement> toExecute = new ArrayList<>();
    if(willMatch(DeclanTokenType.BEGIN)){
      skip();
      toExecute = parseStatementSequence();
    }
    Expression retExpression = null;
    if(willMatch(DeclanTokenType.RETURN)){
      skip();
      retExpression = parseExpression();
    }
    match(DeclanTokenType.END);
    Identifier nameCheck = parseIdentifier();
    if(!nameCheck.getLexeme().equals(procName.getLexeme())){
	    errorLog.add("Expected -> Identity Given at the end of Procedure Declaration ( " + nameCheck.getLexeme() + " ) is not equal to the Expected Procedure Declaration Name ( " + procName.getLexeme() + " )", start);
    }
    return new ProcedureDeclaration(start, procName, fpSequence, returnType, procDeclSequence, toExecute, retExpression);
  }

 // ConstDecl -> ident = number
  private ConstDeclaration parseConstDecl() {
    Identifier id = parseIdentifier();
    match(DeclanTokenType.EQ);
    Expression exp = parseExpression();
    return new ConstDeclaration(id.getStart(), id, exp);
  }

  private List<ParamaterDeclaration> parseParamDecl(){
    List<Identifier> identList = new ArrayList<>();
    //IdentList -> ident IdentListRest
    //IdentListRest -> , ident IdentListRest
    //IdentListRest ->
    DeclanToken varname = match(DeclanTokenType.ID);
    identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
    while(willMatch(DeclanTokenType.COMMA)){
      skip();
      varname = match(DeclanTokenType.ID);
      identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
    }
    match(DeclanTokenType.COLON);
    DeclanToken type = match(DeclanTokenType.ID);
    Identifier typeid = new Identifier(type.getPosition(), type.getLexeme());
    List <ParamaterDeclaration> varDecl = new ArrayList<>();
    for(int i = 0; i < identList.size(); i++){
      Identifier elem = identList.get(i);
      varDecl.add(new ParamaterDeclaration(elem.getStart(), elem, typeid));
    }
    return Collections.unmodifiableList(varDecl);
  }

  //VariableDecl -> IdentList : Type
  private List <VariableDeclaration> parseVariableDecl() {
    List<Identifier> identList = new ArrayList<>();
    //IdentList -> ident IdentListRest
    //IdentListRest -> , ident IdentListRest
    //IdentListRest ->
    DeclanToken varname = match(DeclanTokenType.ID);
    identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
    while(willMatch(DeclanTokenType.COMMA)){
      skip();
      varname = match(DeclanTokenType.ID);
      identList.add(new Identifier(varname.getPosition(), varname.getLexeme()));
    }
    match(DeclanTokenType.COLON);
    DeclanToken type = match(DeclanTokenType.ID);
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
    
    do{
      Statement s = parseStatement();
      statements.add(s);
    } while(skipIfYummy(DeclanTokenType.SEMI));
    
    return Collections.unmodifiableList(statements);
  }
  //Statement -> Assignment | ProcedureCall | IfStatement | WhileStatement | RepeatStatement | ForStatement
  //Statement -> 
  private Statement parseStatement(){
    Position start = currentPosition;
    Statement statement = new EmptyStatement(start);
    if(willMatch(DeclanTokenType.ID)) {
      Identifier ident = parseIdentifier();
      if(willMatch(DeclanTokenType.ASSIGN)) {
	      statement = parseAssignment(ident);
      } else {
	      statement = parseProcedureCall(ident);
      }
    } else if(willMatch(DeclanTokenType.IF)){
      statement = parseIfStatement();
    } else if(willMatch(DeclanTokenType.WHILE)){
      statement = parseWhileStatement();
    } else if(willMatch(DeclanTokenType.REPEAT)){
      statement = parseRepeatStatement();
    } else if (willMatch(DeclanTokenType.FOR)){
      statement = parseForStatement();
    }
    return statement;
  }
    
  //ProcedureCall -> ident ActualParameters
  private Statement parseProcedureCall(Identifier nameOfProcedure){
    Position start = currentPosition;
    if(nameOfProcedure.getLexeme().equals("asm")){
      return parseInlineAssembly();
    } else if(willMatch(DeclanTokenType.LPAR)){
      List<Expression> expList = parseActualParameters();
      return new ProcedureCall(start, nameOfProcedure, expList);
    } else {
      return new ProcedureCall(start, nameOfProcedure);
    }
  }

  private Asm parseInlineAssembly(){
    Position start = currentPosition;
    match(DeclanTokenType.LPAR);
    DeclanToken inlineAssembly = match(DeclanTokenType.STRING);
    String inlineLexeme = inlineAssembly.getLexeme();
    if(willMatch(DeclanTokenType.COLON)){
       skip();
       List<String> inlineArgs = parseInlineAssemblyArguments();
       match(DeclanTokenType.RPAR);
       return new Asm(start, inlineLexeme, inlineArgs);
    } else {
      match(DeclanTokenType.RPAR);
      return new Asm(start, inlineLexeme);
    }
  }

  private List<String> parseInlineAssemblyArguments(){
    List<String> arguments = new LinkedList<String>();
    do{
        String arg = parseInlineAssemblyArgument();
        arguments.add(arg);
    }while(skipIfYummy(DeclanTokenType.COMMA));
    return arguments;
  }

  private String parseInlineAssemblyArgument(){
     DeclanToken ident = match(DeclanTokenType.ID);
     return ident.getLexeme();
  }
   
  //ElsifThenSequence -> ELSIF Expression THEN StatementSequence ElsifThenSequence
  //ElsifThenSequence ->
  private Branch parseIfBranch(){
    Position start = currentPosition;
    Branch result;
    if(willMatch(DeclanTokenType.ELSIF)){
      skip();
      Expression exp = parseExpression();
      match(DeclanTokenType.THEN);
      List<Statement> stats = parseStatementSequence();
      result = new IfElifBranch(start, exp, stats, parseIfBranch());
    } else if(willMatch(DeclanTokenType.ELSE)) {
      skip();
      List<Statement> stats = parseStatementSequence();
      result = new ElseBranch(start, stats);
    } else if (willMatch(DeclanTokenType.END)) {
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
    match(DeclanTokenType.IF);
    Expression ifExpr = parseExpression();
    match(DeclanTokenType.THEN);
    List<Statement> topStatements = parseStatementSequence();
    IfElifBranch topBranch = new IfElifBranch(start, ifExpr, topStatements, parseIfBranch());
    match(DeclanTokenType.END);
    return topBranch;
  }

  //ElsifDoSequence -> ELSIF Expression DO StatementSequence ElsifDoSequence
  //ElsifDoSequence ->
  private WhileElifBranch parseWhileBranch(){
    Position start = currentPosition;
    WhileElifBranch result;
    if(willMatch(DeclanTokenType.ELSIF)){
      skip();
      Expression exp = parseExpression();
      match(DeclanTokenType.DO);
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
    match(DeclanTokenType.WHILE);
    Expression whileExpr = parseExpression();
    match(DeclanTokenType.DO);
    List<Statement> topStatements = parseStatementSequence();
    WhileElifBranch topBranch = new WhileElifBranch(start, whileExpr, topStatements, parseWhileBranch());
    match(DeclanTokenType.END);
    return topBranch;
  }

  //RepeatStatement -> REPEAT StatementSequence UNTIL Expression
  private RepeatBranch parseRepeatStatement(){
    Position start = currentPosition;
    match(DeclanTokenType.REPEAT);
    List<Statement> topStatements = parseStatementSequence();
    match(DeclanTokenType.UNTIL);
    Expression endExpr = parseExpression();
    return new RepeatBranch(start, topStatements, endExpr);
  }

  //ForStatement -> FOR ident := Expression TO Expression BY ConstExpr DO StatementSequence END
  //ForStatement -> FOR ident := Expression TO Expression DO StatementSequence END
  private ForBranch parseForStatement(){
    Position start = currentPosition;
    match(DeclanTokenType.FOR);
    Identifier parseIdent = parseIdentifier();
    Assignment assign = parseAssignment(parseIdent);
    match(DeclanTokenType.TO);
    Expression toCheck = parseExpression();
    Expression toChange = null;
    if(willMatch(DeclanTokenType.BY)){
      skip();
      toChange = parseExpression();
    }
    match(DeclanTokenType.DO);
    List<Statement> toDo = parseStatementSequence();
    match(DeclanTokenType.END);
    return new ForBranch(start, assign, toCheck, toChange, toDo);
  }

  //Assignment -> ident := Expression
  private Assignment parseAssignment(Identifier toBeAssigned){
    Position start = currentPosition;
    match(DeclanTokenType.ASSIGN);
    Expression exp = parseExpression();
    return new Assignment(start, toBeAssigned, exp);
  }

  //ExpList -> Expression ExpListRest
  //ExpListRest -> , Expression
  //ExpListRest ->
  private List<Expression> parseExpressionList(){
    List<Expression> expList = new ArrayList<Expression>();
    
    do{
      Expression exp = parseExpression();
      expList.add(exp);
    } while(skipIfYummy(DeclanTokenType.COMMA));

    return Collections.unmodifiableList(expList);
  }
  //ActualParameters -> ( ExpList )
  //ActualParameters -> ( )
  private List<Expression> parseActualParameters(){
    match(DeclanTokenType.LPAR);
    List<Expression> elist = new ArrayList<>();
    if(!willMatch(DeclanTokenType.RPAR)){
      elist = parseExpressionList();
    }
    match(DeclanTokenType.RPAR);
    return Collections.unmodifiableList(elist);
  }
  //Expression -> SimpleExpr
  //Expression -> SimpleExpr Relation SimpleExpr
  private Expression parseExpression(){
    Position start = currentPosition;
    Expression left = parseSimpleExpression();
    if(willMatch(DeclanTokenType.NE) || willMatch(DeclanTokenType.LE) || willMatch(DeclanTokenType.LT) || willMatch(DeclanTokenType.EQ) || willMatch(DeclanTokenType.GT) || willMatch(DeclanTokenType.GE)){
      BinaryOperation.OpType op = parseBoolOp();
      Expression right = parseSimpleExpression();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
        
  private BinaryOperation.OpType parseBoolOp() {
    if(willMatch(DeclanTokenType.NE)){
      skip();
      return BinaryOperation.OpType.NE;
    } else if(willMatch(DeclanTokenType.EQ)){
      skip();
      return BinaryOperation.OpType.EQ;
    } else if(willMatch(DeclanTokenType.LT)){
      skip();
      return BinaryOperation.OpType.LT;
    } else if(willMatch(DeclanTokenType.GT)){
      skip();
      return BinaryOperation.OpType.GT;
    } else if(willMatch(DeclanTokenType.GE)){
      skip();
      return BinaryOperation.OpType.GE;
    } else {
      match(DeclanTokenType.LE);
      return BinaryOperation.OpType.LE;
    }
  }
  //SimpleExpr -> + Term SimpleExprRest
  //SimpleExpr -> - Term SimpleExprRest
  //SimpleExpr -> Term SimpleExprRest
  private Expression parseSimpleExpression(){
    Position start = currentPosition; 
    Expression left;
    if(willMatch(DeclanTokenType.MINUS) || willMatch(DeclanTokenType.PLUS)){
      UnaryOperation.OpType pm = parseUnaryOp();
      left = parseTerm();
      left = new UnaryOperation(start, pm, left);
    } else {
      left = parseTerm();
    }
    while(willMatch(DeclanTokenType.PLUS) || willMatch(DeclanTokenType.MINUS) || willMatch(DeclanTokenType.OR) 
    || willMatch(DeclanTokenType.BAND) || willMatch(DeclanTokenType.BOR) || willMatch(DeclanTokenType.BXOR)
    || willMatch(DeclanTokenType.LSHIFT) || willMatch(DeclanTokenType.RSHIFT)){
      BinaryOperation.OpType op = parseAddOp();
      Expression right = parseTerm();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
    
  private UnaryOperation.OpType parseUnaryOp() {
    if(willMatch(DeclanTokenType.PLUS)){
      skip();
      return UnaryOperation.OpType.PLUS;
    } else if (willMatch(DeclanTokenType.MINUS)){
      skip();
      return UnaryOperation.OpType.MINUS;
    } else if (willMatch(DeclanTokenType.BNOT)){
      skip();
      return UnaryOperation.OpType.BNOT;
    } else {
      match(DeclanTokenType.NOT);
      return UnaryOperation.OpType.NOT;
    }
  }
  // AddOperator -> + | -
  private BinaryOperation.OpType parseAddOp(){
    if(willMatch(DeclanTokenType.PLUS)){
      skip();
      return BinaryOperation.OpType.PLUS;
    } else if(willMatch(DeclanTokenType.BOR)){
      skip();
      return BinaryOperation.OpType.BOR;
    } else if(willMatch(DeclanTokenType.BAND)){
      skip();
      return BinaryOperation.OpType.BAND; 
    } else if(willMatch(DeclanTokenType.LSHIFT)){
      skip();
      return BinaryOperation.OpType.LSHIFT;
    } else if (willMatch(DeclanTokenType.RSHIFT)){
      skip();
      return BinaryOperation.OpType.RSHIFT;
    } else if(willMatch(DeclanTokenType.OR)){
      skip();
      return BinaryOperation.OpType.OR;
    } else if(willMatch(DeclanTokenType.BXOR)){
      skip();
      return BinaryOperation.OpType.BXOR;
    } else {
      match(DeclanTokenType.MINUS);
      return BinaryOperation.OpType.MINUS;
    }
  }
  // Term -> Factor TermRest
  // TermRest -> MulOperator Factor TermRest
  // TermRest ->
  private Expression parseTerm(){
    Position start = currentPosition;
    Expression left = parseFactor();
    while (willMatch(DeclanTokenType.DIV) || willMatch(DeclanTokenType.MOD) || willMatch(DeclanTokenType.TIMES) || willMatch(DeclanTokenType.DIVIDE) || willMatch(DeclanTokenType.AND)){
      BinaryOperation.OpType op = parseMultOp();
      Expression right = parseFactor();
      left = new BinaryOperation(start, left, op, right);
    }
    return left;
  }
  // MulOperator -> * | DIV | MOD
  private BinaryOperation.OpType parseMultOp() {
    if(willMatch(DeclanTokenType.TIMES)){
      skip();
      return BinaryOperation.OpType.TIMES;
    } else if(willMatch(DeclanTokenType.DIV)) {
      skip();
      return BinaryOperation.OpType.DIV;
    } else if(willMatch(DeclanTokenType.DIVIDE)){
      skip();
      return BinaryOperation.OpType.DIVIDE;
    } else if(willMatch(DeclanTokenType.AND)){
      skip();
      return BinaryOperation.OpType.AND;
    } else {
      match(DeclanTokenType.MOD);
      return BinaryOperation.OpType.MOD;
    }
  }
  // Factor -> number | ident | string | functioncall | TRUE | FALSE | ~FACTOR
  // Factor -> ( Expression )
  private Expression parseFactor(){
    if(willMatch(DeclanTokenType.NUM)){
      return parseNumValue(); 
    } else if (willMatch(DeclanTokenType.TRUE) || willMatch(DeclanTokenType.FALSE)){
      return parseBoolValue();
    } else if(willMatch(DeclanTokenType.ID)){
      DeclanToken id = skip();
      Position start = currentPosition;
      if(willMatch(DeclanTokenType.LPAR)){
	      List<Expression> expList = parseActualParameters();
	      return new FunctionCall(start, new Identifier(start, id.getLexeme()), expList);
      } else {
	      return parseIdentifier(id);
      }
    } else if (willMatch(DeclanTokenType.STRING)){
      return parseStrValue();
    } else if (willMatch(DeclanTokenType.NOT) || willMatch(DeclanTokenType.BNOT)){
      Position start = currentPosition;
      UnaryOperation.OpType not = parseUnaryOp();
      Expression exp = parseFactor();
      return new UnaryOperation(start, not, exp);
    } else {
      match(DeclanTokenType.LPAR);
      Expression expr = parseExpression();
      match(DeclanTokenType.RPAR);
      return expr;
    }
  }
  //ident -> IDENT 
  private Identifier parseIdentifier() {
    DeclanToken id = match(DeclanTokenType.ID);
    Position start = currentPosition;
    return new Identifier(start, id.getLexeme());
  }
  //ident -> IDENT 
  private Identifier parseIdentifier(DeclanToken id) {
    Position start = currentPosition;
    return new Identifier(start, id.getLexeme());
  }
  
  //number -> NUM
  private NumValue parseNumValue() {
    DeclanToken num = match(DeclanTokenType.NUM);
    Position start = currentPosition;
    return new NumValue(start, num.getLexeme());
  }

  //string -> STRING
  private StrValue parseStrValue() {
    DeclanToken str = match(DeclanTokenType.STRING);
    Position start = currentPosition;
    return new StrValue(start, str.getLexeme());
  }
  //Number == true of false
  private BoolValue parseBoolValue() {
    if(willMatch(DeclanTokenType.TRUE)){
      skip();
      Position start = currentPosition;
      return new BoolValue(start, "TRUE");
    } else if (willMatch(DeclanTokenType.FALSE)){
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
