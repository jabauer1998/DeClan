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
import io.github.H20man13.DeClan.common.icode.SymEntry;
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
import io.github.H20man13.DeClan.common.icode.procedure.ExternalCall;
import io.github.H20man13.DeClan.common.icode.procedure.ExternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.InternalPlace;
import io.github.H20man13.DeClan.common.icode.procedure.ParamAssign;
import io.github.H20man13.DeClan.common.icode.procedure.Proc;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.token.IrToken;
import io.github.H20man13.DeClan.common.token.IrTokenType;
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

    public SymSec parseSymbolSection(){
        match(IrTokenType.SYMBOL);
        match(IrTokenType.SECTION);

        List<SymEntry> symEntries = new LinkedList<SymEntry>();
        while(willMatch(IrTokenType.ID)){
            SymEntry entry = parseSymbolEntry();
            symEntries.add(entry);
        }
        return new SymSec(symEntries);
    }

    public DataSec parseDataSection(){
        match(IrTokenType.DATA);
        match(IrTokenType.SECTION);
        List<ICode> assignments = new LinkedList<ICode>();
        while(willMatch(IrTokenType.ID)){
            ICode icode = parseDataAssignment();
            assignments.add(icode);
        }
        return new DataSec(assignments);
    }

    public ProcSec parseProcedureSection(){
        match(IrTokenType.PROC);
        match(IrTokenType.SECTION);
        List<Proc> procedures = new LinkedList<Proc>();
        while(willMatch(IrTokenType.PROC)){
            Proc procedure = parseProcedure();
            procedures.add(procedure);
        }
        return new ProcSec(procedures);
    }

    public SymEntry parseSymbolEntry(){
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

        IrToken declanIdent = match(IrTokenType.ID);

        return new SymEntry(resultMask, irPlace.getLexeme(), declanIdent.getLexeme());
    }

    public Proc parseProcedure(){
        match(IrTokenType.PROC);
        match(IrTokenType.LABEL);
        IrToken id = match(IrTokenType.ID);
        ProcLabel label = new ProcLabel(id.getLexeme());
        List<ParamAssign> paramAssignments = new LinkedList<ParamAssign>();
        List<ICode> instructions = new LinkedList<ICode>();
        InternalPlace place = null;
        while(!willMatch(IrTokenType.RETURN)){
            ICode instruction = parseInstruction();
            if(instruction instanceof ParamAssign){
                paramAssignments.add((ParamAssign)instruction);
            } else if(instruction instanceof InternalPlace){
                place = (InternalPlace)instruction;
            } else {
                instructions.add(instruction);
            }
        }

        Return ret = parseReturn();

        return new Proc(label, paramAssignments, instructions, place, ret);
    }

    public CodeSec parseCodeSection(){
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
        || willMatch(IrTokenType.IPARAM) || willMatch(IrTokenType.CALL)){
            ICode instr = parseInstruction();
            toRet.add(instr);
        }
        return toRet;
    }

    public ICode parseInstruction(){
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
            return parseExternalCall();  
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

        BinExp expr = new BinExp(left, Utils.toBinOp(op.getType()), right);
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
        
        List<Tuple<String, String>> args = new LinkedList<>();
        do{
            IrToken from = match(IrTokenType.ID);
            match(IrTokenType.MAP);
            IrToken to = match(IrTokenType.ID);
            args.add(new Tuple<String, String>(from.getLexeme(), to.getLexeme()));
        } while(skipIfYummy(IrTokenType.COMMA));

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
        if(willMatch(IrTokenType.INEG) || willMatch(IrTokenType.RNEG) || willMatch(IrTokenType.BNOT)) {
            return parseUnaryExpression();
        } else if(willMatch(IrTokenType.EXTERNAL)){
            return parseExternalCall();  
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

                return new BinExp(exp1, Utils.toBinOp(op.getType()), exp2);
            }else {
                return exp1;
            }
        }
    }

    private ExternalCall parseExternalCall(){
        match(IrTokenType.EXTERNAL);
        match(IrTokenType.CALL);
        IrToken funcName = match(IrTokenType.ID);
        LinkedList<String> args = new LinkedList<String>();
        match(IrTokenType.LPAR);
        if(willMatch(IrTokenType.ID)){
            do{
                IrToken arg = match(IrTokenType.ID);
                args.add(arg.getLexeme());
            } while(skipIfYummy(IrTokenType.COMMA));
        }
        match(IrTokenType.RPAR);

        return new ExternalCall(funcName.getLexeme(), args);
    }

    private Assign parseDataAssignment(){
        IrToken id = match(IrTokenType.ID);
        match(IrTokenType.ASSIGN);
        Exp expression = parseExpression();
        return new Assign(id.getLexeme(), expression);
    }

    private ICode parseAssignment(){
        IrToken id = match(IrTokenType.ID);

        if(willMatch(IrTokenType.EPLACE)){
            skip();
            IrToken id2 = match(IrTokenType.ID);
            return new ExternalPlace(id.getLexeme(), id2.getLexeme());
        } else if(willMatch(IrTokenType.IPLACE)){
            skip();
            IrToken id2 = match(IrTokenType.ID);
            return new InternalPlace(id.getLexeme(), id2.getLexeme());  
        } else if(willMatch(IrTokenType.PARAM_ASSIGN)) {
            skip();
            IrToken id2 = match(IrTokenType.ID);
            return new ParamAssign(id.getLexeme(), id2.getLexeme());
        } else {
            match(IrTokenType.ASSIGN);
            Exp expression = parseExpression();
            return new Assign(id.getLexeme(), expression);
        }
    }
}
