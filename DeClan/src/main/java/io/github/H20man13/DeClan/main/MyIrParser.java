package io.github.H20man13.DeClan.main;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ParseException;
import edu.depauw.declan.common.Position;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
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
import io.github.H20man13.DeClan.common.icode.procedure.Call;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.pat.P;
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
            errorLog.add("Expected " + type + ", found EOF", current.getPosition());
            errorCount++;
        } else {
            errorLog.add("Expected " + type + ", found " + current.getType(), currentPosition);
            errorCount++;
        }
        throw new ParseException("Parsing aborted");
    }

    void matchEOF(){
        if(current != null){
            errorLog.add("Expected end of file, found " + current.getType(), current.getPosition());
            errorCount++;
            throw new ParseException("Parsing aborted");
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
        SymSec sym = parseSymbolSection();
        DataSec data = parseDataSection();
        CodeSec code = parseCodeSection();
        ProcSec proc = parseProcedureSection();
        matchEOF();
        return new Prog(sym, data, code, proc);
    }

    public Lib parseLibrary(){
        SymSec sym = parseSymbolSection();
        DataSec data = parseDataSection();
        ProcSec proc = parseProcedureSection();
        matchEOF();
        return new Lib(sym, data, proc);
    }

    private SymSec parseSymbolSection(){
        match(IrTokenType.SYMBOL);
        match(IrTokenType.SECTION);

        List<SymEntry> symEntries = new LinkedList<SymEntry>();
        while(willMatch(IrTokenType.ID)){
            SymEntry entry = parseSymbolEntry();
            symEntries.add(entry);
        }
        return new SymSec(symEntries);
    }

    private DataSec parseDataSection(){
        match(IrTokenType.DATA);
        match(IrTokenType.SECTION);
        List<ICode> assignments = new LinkedList<ICode>();
        while(willMatch(IrTokenType.ID) || willMatch(IrTokenType.GLOBAL) 
        || willMatch(IrTokenType.CALL) || willMatch(IrTokenType.EXTERNAL)){
            ICode icode = parseInstruction();
            assignments.add(icode);
        }
        return new DataSec(assignments);
    }

    private ProcSec parseProcedureSection(){
        match(IrTokenType.PROC);
        match(IrTokenType.SECTION);
        List<Proc> procedures = new LinkedList<Proc>();
        while(willMatch(IrTokenType.PROC)){
            Proc procedure = parseProcedure();
            procedures.add(procedure);
        }
        return new ProcSec(procedures);
    }

    private SymEntry parseSymbolEntry(){
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
            IrToken funcNameTok = match(IrTokenType.ID);
            IrToken funcParamNumberTok = match(IrTokenType.NUMBER);
            Integer funcParamNumber = Integer.parseInt(funcParamNumberTok.getLexeme());
            return new ParamSymEntry(irPlace.getLexeme(), resultMask, funcNameTok.getLexeme(), funcParamNumber);
        } else if(willMatch(IrTokenType.RETURN)){
            skip();
            IrToken funcNameTok = match(IrTokenType.ID);
            return new RetSymEntry(irPlace.getLexeme(), resultMask, funcNameTok.getLexeme());
        } else {
            IrToken declanIdent = match(IrTokenType.ID);
            return new VarSymEntry(irPlace.getLexeme(), resultMask, declanIdent.getLexeme());
        }
    }

    private Proc parseProcedure(){
        match(IrTokenType.PROC);
        match(IrTokenType.LABEL);
        IrToken id = match(IrTokenType.ID);
        ProcLabel label = new ProcLabel(id.getLexeme());
        List<Assign> paramAssignments = new LinkedList<Assign>();
        List<ICode> instructions = new LinkedList<ICode>();
        Assign place = null;
        while(!willMatch(IrTokenType.RETURN)){
            ICode instruction = parseInstruction();
            if(instruction instanceof Assign){
                Assign assign = (Assign)instruction;
                if(assign.getScope() == ICode.Scope.PARAM){
                    paramAssignments.add(assign);
                } else if(assign.getScope() == ICode.Scope.INTERNAL_RETURN){
                    place = assign;  
                } else {
                    instructions.add(instruction);
                }
            } else {
                instructions.add(instruction);
            }
        }

        Return ret = parseReturn();

        return new Proc(label, paramAssignments, instructions, place, ret);
    }

    private CodeSec parseCodeSection(){
        match(IrTokenType.CODE);
        match(IrTokenType.SECTION);
        List<ICode> instructions = parseInstructions();
        match(IrTokenType.END);
        return new CodeSec(instructions);
    }

    public List<ICode> parseInstructions(){
        List<ICode> toRet = new LinkedList<ICode>();
        while(willMatch(IrTokenType.LABEL) || willMatch(IrTokenType.IF) 
        || willMatch(IrTokenType.ID) || willMatch(IrTokenType.GOTO) 
        || willMatch(IrTokenType.RETURN) || willMatch(IrTokenType.IASM) 
        || willMatch(IrTokenType.IPARAM) || willMatch(IrTokenType.CALL) 
        || willMatch(IrTokenType.EXTERNAL) || willMatch(IrTokenType.INTERNAL)
        || willMatch(IrTokenType.PARAM) || willMatch(IrTokenType.GLOBAL)){
            ICode instr = parseInstruction();
            toRet.add(instr);
        }
        return toRet;
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
        } else if(willMatch(IrTokenType.EXTERNAL)){
            skip();
            return parseExternalReturn();  
        } else {
            return parseAssignment();
        }
    }

    private Inline parseInlineAssembly(){
        List<String> params = new LinkedList<String>();
        while(willMatch(IrTokenType.IPARAM)){
            skip();
            IrToken param = match(IrTokenType.ID);
            params.add(param.getLexeme());
        }
        match(IrTokenType.IASM);
        IrToken inlineAssembly = match(IrTokenType.STRING);
        String lexeme = inlineAssembly.getLexeme();
        return new Inline(lexeme, params);
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
        } else if(willMatch(IrTokenType.ID)){
            IrToken tok = skip();
            return new IdentExp(tok.getLexeme());
        } else {
            return null;
        }
    }

    private BinExp parseRelationalExpression(){
        Exp left = parsePrimaryExpression();

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

        Exp right = parsePrimaryExpression();

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
        
        List<Assign> args = new LinkedList<>();
        
        if(willMatch(IrTokenType.LPAR)){
            do{
                Assign assign = parseArgument();
                args.add(assign);
            } while(skipIfYummy(IrTokenType.COMMA));
        }   

        match(IrTokenType.RPAR);

        return new Call(procName.getLexeme(), args);
    }

    private UnExp parseUnaryExpression(){
        if(willMatch(IrTokenType.INEG)){
            skip();
            Exp right = parsePrimaryExpression();
            return new UnExp(UnExp.Operator.INEG, right);
        } else if(willMatch(IrTokenType.RNEG)){
            skip();
            Exp right = parsePrimaryExpression();
            return new UnExp(UnExp.Operator.RNEG, right);
        } else if(willMatch(IrTokenType.BNOT)){
            skip();
            Exp right = parsePrimaryExpression();
            return new UnExp(UnExp.Operator.BNOT, right);
        } else if(willMatch(IrTokenType.INOT)){
            skip();
            Exp right = parsePrimaryExpression();
            return new UnExp(UnExp.Operator.INOT, right);  
        } else {
            return null;
        }
    }

    private Exp parseExpression(){
        if(willMatch(IrTokenType.INEG) || willMatch(IrTokenType.RNEG) 
        || willMatch(IrTokenType.BNOT) || willMatch(IrTokenType.INOT)) {
            return parseUnaryExpression();
        } else {
            Exp exp1 = parsePrimaryExpression();

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

                Exp exp2 = parsePrimaryExpression();

                return new BinExp(exp1, ConversionUtils.toBinOp(op.getType()), exp2);
            } else {
                return exp1;
            }
        }
    }

    private Assign parseArgument(){
        match(IrTokenType.LPAR);
        IrToken value = match(IrTokenType.ID);
        match(IrTokenType.MAP);
        IrToken place = match(IrTokenType.ID);
        match(IrTokenType.RPAR);

        match(IrTokenType.LBRACK);
        Assign.Type type;
        if(willMatch(IrTokenType.BOOL)){
            skip();
            type = Assign.Type.BOOL;
        } else if(willMatch(IrTokenType.STRING)){
            skip();
            type = Assign.Type.STRING;
        } else if(willMatch(IrTokenType.REAL)){
            skip();
            type = Assign.Type.REAL;
        } else {
            match(IrTokenType.INT);
            type = Assign.Type.INT;
        }
        match(IrTokenType.RBRACK);

        return new Assign(ICode.Scope.ARGUMENT, place.getLexeme(), new IdentExp(value.getLexeme()), type);
    }

    private ICode parseExternalReturn(){
        match(IrTokenType.RETURN);
        IrToken place = match(IrTokenType.ID);
        match(IrTokenType.ASSIGN);
        Exp exp = parseExpression();
        Assign.Type type;
        match(IrTokenType.LBRACK);
        if(willMatch(IrTokenType.STRING)){
            skip();
            type = Assign.Type.STRING;
        } else if(willMatch(IrTokenType.BOOL)){
            skip();
            type = Assign.Type.BOOL;
        } else if(willMatch(IrTokenType.REAL)){
            skip();
            type = Assign.Type.REAL;
        } else {
            match(IrTokenType.INT);
            type = Assign.Type.INT;
        }
        match(IrTokenType.RBRACK);

        return new Assign(ICode.Scope.EXTERNAL_RETURN, place.getLexeme(), exp, type);
    }

    private ICode parseAssignment(){
        Assign.Scope scope;
        if(willMatch(IrTokenType.EXTERNAL)){
            skip();
            match(IrTokenType.RETURN);
            scope = ICode.Scope.EXTERNAL_RETURN;
        } else if(willMatch(IrTokenType.INTERNAL)){
            skip();
            match(IrTokenType.RETURN);
            scope = ICode.Scope.INTERNAL_RETURN;
        } else if(willMatch(IrTokenType.PARAM)){
            skip();
            scope = ICode.Scope.PARAM;
        } else if(willMatch(IrTokenType.GLOBAL)) {
            skip();
            scope = ICode.Scope.GLOBAL;
        } else {
            scope = ICode.Scope.LOCAL;
        }

        IrToken id = match(IrTokenType.ID);
        match(IrTokenType.ASSIGN);
        Exp expression = parseExpression();

        match(IrTokenType.LBRACK);
        ICode.Type assignType;
        if(willMatch(IrTokenType.REAL)){
            skip();
            assignType = ICode.Type.REAL; 
        } else if(willMatch(IrTokenType.BOOL)){
            skip();
            assignType = ICode.Type.BOOL;
        } else if(willMatch(IrTokenType.STRING)){
            skip();
            assignType = ICode.Type.STRING;  
        } else {
            match(IrTokenType.INT);
            assignType = ICode.Type.INT;
        }
        match(IrTokenType.RBRACK);
        return new Assign(scope, id.getLexeme(), expression, assignType);
    }
}
