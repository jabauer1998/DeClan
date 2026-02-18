package declan.frontend;

import java.util.LinkedList;
import java.util.List;

import declan.utils.ErrorLog;
import declan.utils.Tuple;
import declan.utils.exception.ParseException;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Def;
import declan.middleware.icode.End;
import declan.middleware.icode.Goto;
import declan.middleware.icode.ICode;
import declan.middleware.icode.If;
import declan.middleware.icode.Lib;
import declan.middleware.icode.Spill;
import declan.middleware.icode.Prog;
import declan.middleware.icode.Return;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.BoolExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.IntExp;
import declan.middleware.icode.exp.RealExp;
import declan.middleware.icode.exp.StrExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.inline.Inline;
import declan.middleware.icode.inline.InlineParam;
import declan.middleware.icode.label.Label;
import declan.middleware.icode.label.ProcLabel;
import declan.middleware.icode.label.StandardLabel;
import declan.middleware.icode.section.BssSec;
import declan.middleware.icode.section.CodeSec;
import declan.middleware.icode.section.DataSec;
import declan.middleware.icode.section.ProcSec;
import declan.middleware.icode.section.SymSec;
import declan.middleware.icode.symbols.SymEntry;
import declan.middleware.icode.symbols.VarSymEntry;
import declan.utils.pat.P;
import declan.utils.position.Position;
import declan.frontend.token.IrToken;
import declan.frontend.token.IrTokenType;
import declan.utils.ConversionUtils;
import declan.utils.Utils;

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
        icode = parseBssSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.DEF)){
            icode = parseDefinition();
            toRet.add(icode);
        }
        icode = parseDataSection();
        toRet.add(icode);
        while(willMatch(IrTokenType.CALL) || willMatch(IrTokenType.DEF)){
            icode = parseInstruction();
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
            || willMatch(IrTokenType.DEF) || willMatch(IrTokenType.PARAM)){
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
            IrToken paramNameOrFunctionName = match(IrTokenType.ID);
            if(willMatch(IrTokenType.NUMBER)) {
            	IrToken funcParamNumberTok = skip();
                Integer funcParamNumber = Integer.parseInt(funcParamNumberTok.getLexeme());
                ICode.Type type = parseType();
                return new VarSymEntry(irPlace.getLexeme(), resultMask, null, paramNameOrFunctionName.getLexeme(), funcParamNumber, type);
            } else {
            	 IrToken funcNameTok = match(IrTokenType.ID);
            	 IrToken funcParamNumberTok = skip();
                 Integer funcParamNumber = Integer.parseInt(funcParamNumberTok.getLexeme());
                 ICode.Type type = parseType();
                 return new VarSymEntry(irPlace.getLexeme(), resultMask, paramNameOrFunctionName.getLexeme(), funcNameTok.getLexeme(), funcParamNumber, type);
            }
        } else if(willMatch(IrTokenType.RETURN)){
            skip();
            resultMask |= SymEntry.RETURN;
            IrToken funcNameTok = match(IrTokenType.ID);
            ICode.Type type = parseType();
            return new VarSymEntry(irPlace.getLexeme(), resultMask, funcNameTok.getLexeme(), type, true);
        } else if(willMatch(IrTokenType.GLOBAL)) {
        	skip();
        	IrToken declanIdent = match(IrTokenType.ID);
        	resultMask |= SymEntry.GLOBAL;
        	ICode.Type type = parseType();
            return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme(), type, false);
        } else {
        	IrToken declanIdent = skip();
        	resultMask |= SymEntry.LOCAL;
        	if(willMatch(IrTokenType.ID)) {
        		IrToken funcName = skip();
        		ICode.Type type = parseType();
            	return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme(), funcName.getLexeme(), type);
        	} else {
        		ICode.Type type = parseType();
            	return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme(), type, false);
        	}
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
        } else if(willMatch(IrTokenType.SPILL)){
        	return parseSpill();
        } else {
            return parseAssignment();
        }
    }
    
    private Spill parseSpill() {
    	match(IrTokenType.SPILL);
    	IrToken id = match(IrTokenType.ID);
    	ICode.Type type = parseType();
    	return new Spill(id.toString(), type);
    }
    
    private InlineParam parseInlineParam() {
    	match(IrTokenType.IPARAM);
    	IdentExp ident = parseIdentifier();
    	ICode.Type type = parseType();
    	IrToken tok = match(IrTokenType.SPECIFIER);
    	String tokLexeme = tok.getLexeme();
    	int mask = 0;
    	for(int i = 0; i < tokLexeme.length(); i++){
			char c = tokLexeme.charAt(i);
			if(c == 'a')
				mask |= InlineParam.IS_ADDRESS;
			else if(c == 'r')
				mask |= InlineParam.IS_REGISTER;
			else if(c == 'd')
				mask |= InlineParam.IS_DEFINITION;
			else if(c == 'u')
				mask |= InlineParam.IS_USE;
		}
    	return new InlineParam(ident, type, mask);
    }

    private Inline parseInlineAssembly(){
        List<InlineParam> params = new LinkedList<InlineParam>();
        while(willMatch(IrTokenType.IPARAM)){
            InlineParam param = parseInlineParam();
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
        if(willMatch(IrTokenType.BEQ) 
        || willMatch(IrTokenType.BNE)
        || willMatch(IrTokenType.INE)
        || willMatch(IrTokenType.IEQ)
        || willMatch(IrTokenType.GT)
        || willMatch(IrTokenType.GE)
        || willMatch(IrTokenType.LT)
        || willMatch(IrTokenType.LE)){
            op = skip();
        } else {
            op = match(IrTokenType.IEQ);
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
        match(IrTokenType.FROM);
        IrToken id = match(IrTokenType.ID);
        return new Return(id.getLexeme());
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
        if(willMatch(IrTokenType.BNOT) || willMatch(IrTokenType.INOT)) {
            return parseUnaryExpression();
        } else if(willMatch(IrTokenType.ID) || willMatch(IrTokenType.LPAR)) {
            IdentExp exp1 = parseIdentifier();

            if(willMatch(IrTokenType.LT) || willMatch(IrTokenType.IADD) 
            || willMatch(IrTokenType.LE) || willMatch(IrTokenType.GT)
            || willMatch(IrTokenType.INE) || willMatch(IrTokenType.BNE)
            || willMatch(IrTokenType.GE) || willMatch(IrTokenType.LOR) 
            || willMatch(IrTokenType.ISUB) || willMatch(IrTokenType.LAND) 
            || willMatch(IrTokenType.IAND) || willMatch(IrTokenType.IOR) 
            || willMatch(IrTokenType.ILSHIFT) || willMatch(IrTokenType.IRSHIFT) 
            || willMatch(IrTokenType.BEQ) || willMatch(IrTokenType.IEQ) 
            || willMatch(IrTokenType.IXOR)) {
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
