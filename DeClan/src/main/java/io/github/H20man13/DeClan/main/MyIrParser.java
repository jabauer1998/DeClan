package io.github.H20man13.DeClan.main;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.exception.ParseException;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Inline;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.label.Label;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.label.StandardLabel;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.position.Position;
import io.github.H20man13.DeClan.common.token.IrToken;
import io.github.H20man13.DeClan.common.token.IrTokenType;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.Utils;

public class MyIrParser {
    private MyIrLexer lexer;
    private ErrorLog errorLog;
    private IrToken current;
    private Position currentPosition;
    private int errorCount;
    

    public MyIrParser(MyIrLexer lexer, ErrorLog errorLog){
        this.lexer = lexer;
        this.errorLog = errorLog;
        this.current = null;
        this.currentPosition = new Position(0, 0);
        this.errorCount = 0;
        skip();
    }

    public void close(){
        lexer.close();
    }

    public boolean containsErrors(){
        return errorCount > 0;
    }

    boolean willMatch(IrTokenType type){
        return current != null && current.getType() == type;
    }

    IrToken match(IrTokenType type){
        if(willMatch(type)){
            return skip();
        } else if(current == null){
            throw new ParseException("Expected " + type + ", found EOF");
        } else {
            throw new ParseException("Expected " + type + ", found " + current.getType() + " at " + current.getPosition());
        }
    }

    void matchEOF(){
        if(current != null){
            throw new ParseException("Expected end of file, found " + current.getType() + " at " + current.getPosition());
        }
    }

    IrToken skip(){
        IrToken token = current;
        if(lexer.hasNext()){
            current = lexer.next();
            currentPosition = current.getPosition();
        } else {
            current = null;
        }
        return token;
    }

    boolean skipIfYummy(IrTokenType type){
        if(willMatch(type)){
            skip();
            return true;
        }
        return false;
    }

    public Prog parseProgram(){
        List<ICode> toRet = new LinkedList<ICode>();
        ICode icode = parseSymbolSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.ENTRY)){
            icode = parseSymbolEntry();
            toRet.add(icode);
        }
        icode = parseDataSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.CALL) || willMatch(IrTokenType.DEF)){
            icode = parseInstruction();
            toRet.add(icode);
        }
        icode = parseBssSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.DEF)){
            icode = parseDefinition();
            toRet.add(icode);
        }
        icode = parseCodeSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.LABEL) || willMatch(IrTokenType.IF) 
        || willMatch(IrTokenType.ID) || willMatch(IrTokenType.GOTO) 
        || willMatch(IrTokenType.RETURN) || willMatch(IrTokenType.IASM) 
        || willMatch(IrTokenType.IPARAM) || willMatch(IrTokenType.CALL)
        || willMatch(IrTokenType.GLOBAL) || willMatch(IrTokenType.DEF)){
            icode = parseInstruction();
            toRet.add(icode);
        }
        icode = parseEnd();
        toRet.add(icode);
        icode = parseProcedureSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.PROC)){
            icode = parseProcedureLabel();
            toRet.add(icode);
            while(willMatch(IrTokenType.LABEL) || willMatch(IrTokenType.IF) 
            || willMatch(IrTokenType.ID) || willMatch(IrTokenType.GOTO) 
            || willMatch(IrTokenType.IASM) || willMatch(IrTokenType.IPARAM) 
            || willMatch(IrTokenType.CALL) || willMatch(IrTokenType.GLOBAL) 
            || willMatch(IrTokenType.DEF)){
                icode = parseInstruction();
                toRet.add(icode);
            }
            icode = parseReturn();
            toRet.add(icode);
        }
        matchEOF();
        return new Prog(toRet);
    }

    public Lib parseLibrary(){
        List<ICode> toRet = new LinkedList<ICode>();
        ICode icode = parseSymbolSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.ENTRY)){
            icode = parseSymbolEntry();
            toRet.add(icode);
        }
        icode = parseDataSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.CALL) || willMatch(IrTokenType.DEF)){
            icode = parseInstruction();
            toRet.add(icode);
        }
        icode = parseProcedureSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.PROC)){
            icode = parseProcedureLabel();
            toRet.add(icode);
            while(willMatch(IrTokenType.LABEL) || willMatch(IrTokenType.IF) 
            || willMatch(IrTokenType.ID) || willMatch(IrTokenType.GOTO) || 
            willMatch(IrTokenType.IASM) || willMatch(IrTokenType.IPARAM) || 
            willMatch(IrTokenType.CALL) || willMatch(IrTokenType.GLOBAL) || 
            willMatch(IrTokenType.DEF)){
                icode = parseInstruction();
                toRet.add(icode);
            }
            icode = parseReturn();
            toRet.add(icode);
        }
        matchEOF();
        return new Lib(toRet);
    }

    private SymSec parseSymbolSection(){
        match(IrTokenType.SYMBOL);
        match(IrTokenType.SECTION);
        return new SymSec();
    }

    private DataSec parseDataSection(){
        match(IrTokenType.DATA);
        match(IrTokenType.SECTION);
        return new DataSec();
    }

    private BssSec parseBssSection(){
        match(IrTokenType.BSS);
        match(IrTokenType.SECTION);
        return new BssSec();
    }

    private ProcSec parseProcedureSection(){
        match(IrTokenType.PROC);
        match(IrTokenType.SECTION);
        return new ProcSec();
    }

    private End parseEnd(){
        match(IrTokenType.END);
        return new End();
    }

    private SymEntry parseSymbolEntry(){
        match(IrTokenType.ENTRY);
        int resultMask = 0;
        IrToken irPlace = match(IrTokenType.ID);
        if(willMatch(IrTokenType.CONST)){
            skip();
            resultMask |= SymEntry.CONST;
        }

        if(willMatch(IrTokenType.INTERNAL)){
            skip();
            resultMask |= SymEntry.INTERNAL;
        } else {
            match(IrTokenType.EXTERNAL);
            resultMask |= SymEntry.EXTERNAL;
        }

        if(willMatch(IrTokenType.PARAM)){
            skip();
            resultMask |= SymEntry.PARAM;
            IrToken paramName = match(IrTokenType.ID);
            IrToken funcNameTok = match(IrTokenType.ID);
            IrToken funcParamNumberTok = match(IrTokenType.NUMBER);
            Integer funcParamNumber = Integer.parseInt(funcParamNumberTok.getLexeme());
            return new VarSymEntry(irPlace.getLexeme(), resultMask, paramName.getLexeme(), funcNameTok.getLexeme(), funcParamNumber);
        } else if(willMatch(IrTokenType.RETURN)){
            skip();
            resultMask |= SymEntry.RETURN;
            IrToken funcNameTok = match(IrTokenType.ID);
            return new VarSymEntry(irPlace.getLexeme(), resultMask, funcNameTok.getLexeme(), true);
        } else if(willMatch(IrTokenType.GLOBAL)) {
        	skip();
        	IrToken declanIdent = match(IrTokenType.ID);
        	resultMask |= SymEntry.GLOBAL;
            return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme(), false);
        } else if(willMatch(IrTokenType.ID)) {
        	IrToken funcName = skip();
        	resultMask |= SymEntry.LOCAL;
        	IrToken declanIdent = match(IrTokenType.ID);
        	return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme(), funcName.getLexeme());
        } else {
        	resultMask |= SymEntry.LOCAL;
        	IrToken declanIdent = match(IrTokenType.ID);
        	return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme(), false);
        }
    }

    private ProcLabel parseProcedureLabel(){
        match(IrTokenType.PROC);
        match(IrTokenType.LABEL);
        IrToken id = match(IrTokenType.ID);
        return new ProcLabel(id.getLexeme());
    }

    private CodeSec parseCodeSection(){
        match(IrTokenType.CODE);
        match(IrTokenType.SECTION);
        return new CodeSec();
    }

    private ICode parseInstruction(){
        Position start = currentPosition;
        if(willMatch(IrTokenType.IF)){
           return parseIfStatement();
        } else if(willMatch(IrTokenType.IASM) || willMatch(IrTokenType.IPARAM)){
           return parseInlineAssembly();
        } else if(willMatch(IrTokenType.LABEL)){
           return parseLabel();
        } else if(willMatch(IrTokenType.GOTO)){
            return parseGoto();
        } else if(willMatch(IrTokenType.CALL)){
            return parseProcedureCall();  
        } else if(willMatch(IrTokenType.DEF)){
            return parseDefinition();  
        } else {
            return parseAssignment();
        }
    }

    private Inline parseInlineAssembly(){
        List<IdentExp> params = new LinkedList<IdentExp>();
        while(willMatch(IrTokenType.IPARAM)){
            skip();
            IdentExp param = parseIdentifier();
            params.add(param);
        }
        match(IrTokenType.IASM);
        IrToken inlineAssembly = match(IrTokenType.STRING);
        String lexeme = inlineAssembly.getLexeme();
        return new Inline(lexeme, params);
    }

    private IdentExp parseIdentifier(){
        if(willMatch(IrTokenType.LPAR)){
            skip();
            ICode.Scope scope = parseScope();
            IrToken id = match(IrTokenType.ID);
            match(IrTokenType.RPAR);
            return new IdentExp(scope, id.getLexeme());
        } else {
            IrToken id = match(IrTokenType.ID);
            return new IdentExp(ICode.Scope.LOCAL, id.getLexeme());
        }
    }

    private Exp parsePrimaryExpression(){
        if(willMatch(IrTokenType.TRUE)){
            skip();
            return new BoolExp(true);
        } else if(willMatch(IrTokenType.FALSE)){
            skip();
            return new BoolExp(false);
        } else if(willMatch(IrTokenType.NUMBER)){
            IrToken tok = skip();
            if(tok.getLexeme().contains(".")){
                return new RealExp(Float.parseFloat(tok.getLexeme()));
            } else {
                return new IntExp(Integer.parseUnsignedInt(tok.getLexeme()));
            }
        } else if(willMatch(IrTokenType.STRING)){
            IrToken tok = skip();
            return new StrExp(tok.getLexeme());
        } else {
            throw new ParseException("Error when parsing primary expression expected token of type BOOL/REAL/INT/STRING/IDENT but found token of type " + skip());
        }
    }

    private BinExp parseRelationalExpression(){
        IdentExp left = parseIdentifier();

        IrToken op = null;
        if(willMatch(IrTokenType.EQ) 
        || willMatch(IrTokenType.NE)
        || willMatch(IrTokenType.GT)
        || willMatch(IrTokenType.GE)
        || willMatch(IrTokenType.LT)
        || willMatch(IrTokenType.LE)){
            op = skip();
        } else {
            op = match(IrTokenType.EQ);
        }

        IdentExp right = parseIdentifier();

        BinExp expr = new BinExp(left, ConversionUtils.toBinOp(op.getType()), right);
        return expr;
    }

    private ICode parseIfStatement(){
        match(IrTokenType.IF);
        BinExp exp = parseRelationalExpression();
        match(IrTokenType.THEN);
        IrToken labelOne = match(IrTokenType.ID);
        match(IrTokenType.ELSE);
        IrToken labelTwo = match(IrTokenType.ID);

        return new If(exp, labelOne.getLexeme(), labelTwo.getLexeme());
    }

    private Return parseReturn(){
        match(IrTokenType.RETURN);
        return new Return();
    }

    private ICode parseLabel(){
        match(IrTokenType.LABEL);
        IrToken id = match(IrTokenType.ID);
        return new StandardLabel(id.getLexeme());
    }

    private ICode parseGoto(){
        match(IrTokenType.GOTO);
        IrToken id = match(IrTokenType.ID);
        return new Goto(id.getLexeme());
    }

    private ICode parseProcedureCall(){
        match(IrTokenType.CALL);
        IrToken procName = match(IrTokenType.ID);
        match(IrTokenType.LPAR);
        
        List<Def> args = new LinkedList<>();
        
        if(willMatch(IrTokenType.LBRACK)){
            do{
                Def assign = parseArgument();
                args.add(assign);
            } while(skipIfYummy(IrTokenType.COMMA));
        }   

        match(IrTokenType.RPAR);

        return new Call(procName.getLexeme(), args);
    }

    private UnExp parseUnaryExpression(){
        if(willMatch(IrTokenType.BNOT)){
            skip();
            IdentExp right = parseIdentifier();
            return new UnExp(UnExp.Operator.BNOT, right);
        } else if(willMatch(IrTokenType.INOT)){
            skip();
            IdentExp right = parseIdentifier();
            return new UnExp(UnExp.Operator.INOT, right);  
        } else {
            return null;
        }
    }

    private Exp parseExpression(){
        if(willMatch(IrTokenType.INEG) || willMatch(IrTokenType.RNEG) 
        || willMatch(IrTokenType.BNOT) || willMatch(IrTokenType.INOT)) {
            return parseUnaryExpression();
        } else if(willMatch(IrTokenType.ID) || willMatch(IrTokenType.LPAR)) {
            IdentExp exp1 = parseIdentifier();

            if(willMatch(IrTokenType.LT) || willMatch(IrTokenType.IADD) 
            || willMatch(IrTokenType.LE) || willMatch(IrTokenType.GT)
            || willMatch(IrTokenType.NE) || willMatch(IrTokenType.GE)
            || willMatch(IrTokenType.LOR) || willMatch(IrTokenType.ISUB)
            || willMatch(IrTokenType.IMUL) || willMatch(IrTokenType.IDIV)
            || willMatch(IrTokenType.IMOD) || willMatch(IrTokenType.LAND)
            || willMatch(IrTokenType.IAND) || willMatch(IrTokenType.IOR)
            || willMatch(IrTokenType.ILSHIFT) || willMatch(IrTokenType.IRSHIFT)
            || willMatch(IrTokenType.EQ) || willMatch(IrTokenType.RADD)
            || willMatch(IrTokenType.RSUB) || willMatch(IrTokenType.RMUL)
            || willMatch(IrTokenType.RDIV) || willMatch(IrTokenType.RDIVIDE)
            || willMatch(IrTokenType.IDIVIDE) || willMatch(IrTokenType.IXOR)) {
                IrToken op = skip();

                IdentExp exp2 = parseIdentifier();

                return new BinExp(exp1, ConversionUtils.toBinOp(op.getType()), exp2);
            } else {
                return exp1;
            }
        } else {
            return parsePrimaryExpression();
        }
    }

    private Def parseArgument(){
        match(IrTokenType.LBRACK);
        Exp value = parseExpression();
        match(IrTokenType.MAP);
        IrToken place = match(IrTokenType.ID);
        match(IrTokenType.RBRACK);
        ICode.Type type = parseType();
        return new Def(ICode.Scope.PARAM, place.getLexeme(), value, type);
    }

    private ICode.Scope parseScope(){
        if(willMatch(IrTokenType.GLOBAL)){
            skip();
            return ICode.Scope.GLOBAL;
        } else if(willMatch(IrTokenType.PARAM)){
            skip();
            return ICode.Scope.PARAM;
        } else if(willMatch(IrTokenType.RETURN)){
            skip();
            return ICode.Scope.RETURN;
        } else {
            return ICode.Scope.LOCAL;
        }
    }

    private ICode.Type parseType(){
        match(IrTokenType.LANGLE);

        ICode.Type type;
        if(willMatch(IrTokenType.STRING)){
            skip();
            type = ICode.Type.STRING;
        } else if(willMatch(IrTokenType.INT)){
            skip();
            type = ICode.Type.INT;
        } else if(willMatch(IrTokenType.REAL)){
            skip();
            type = ICode.Type.REAL;
        } else if(willMatch(IrTokenType.BOOL)){
            skip();
            type = ICode.Type.BOOL;
        } else {
            throw new ParseException("In function parseType expected token of type INT/STRING/BOOL/REAL but found token of type " + skip());
        }

        match(IrTokenType.RANGLE);
        return type;
    }

    private ICode parseDefinition(){
        match(IrTokenType.DEF);
        ICode.Scope scope = parseScope();
        IrToken id = match(IrTokenType.ID);
        match(IrTokenType.ASSIGN);
        Exp expression = parseExpression();
        ICode.Type type = parseType();
        return new Def(scope, id.getLexeme(), expression, type);
    }

    private ICode parseAssignment(){
        ICode.Scope scope = parseScope();
        IrToken id = match(IrTokenType.ID);
        match(IrTokenType.ASSIGN);
        Exp expression = parseExpression();
        ICode.Type assignType = parseType();
        return new Assign(scope, id.getLexeme(), expression, assignType);
    }
}
