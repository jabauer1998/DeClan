package io.github.H20man13.DeClan.main;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ParseException;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.TokenType;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.End;
import io.github.H20man13.DeClan.common.icode.Goto;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Label;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;
import io.github.H20man13.DeClan.common.icode.Proc;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.IntExp;
import io.github.H20man13.DeClan.common.icode.exp.RealExp;
import io.github.H20man13.DeClan.common.icode.exp.StrExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
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
            errorLog.add("Expected " + type + ", found" + current.getType(), currentPosition);
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

    public List<ICode> parseProgram(){
        Position start = currentPosition;
        List<ICode> parseInstructions = parseInstructions();
        match(IrTokenType.END);
        parseInstructions.add(new End());
        matchEOF();
        return parseInstructions;
    }

    public List<ICode> parseInstructions(){
        List<ICode> toRet = new LinkedList<ICode>();
        while(willMatch(IrTokenType.LABEL) || willMatch(IrTokenType.IF) || willMatch(IrTokenType.ID) || willMatch(IrTokenType.GOTO) || willMatch(IrTokenType.PROC) || willMatch(IrTokenType.RETURN)){
            ICode instr = parseInstruction();
            toRet.add(instr);
        }
        return toRet;
    }

    public ICode parseInstruction(){
        Position start = currentPosition;
        if(willMatch(IrTokenType.IF)){
            return parseIfStatement();
        } else if(willMatch(IrTokenType.LABEL)){
            return parseLabel();
        } else if(willMatch(IrTokenType.GOTO)){
            return parseGoto();
        } else if(willMatch(IrTokenType.PROC)){
            return parseProcedure();
        } else if (willMatch(IrTokenType.RETURN)){
            return parseReturn();  
        } else {
            return parseAssignment();
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
                return new RealExp(Double.parseDouble(tok.getLexeme()));
            } else {
                return new IntExp(Integer.parseInt(tok.getLexeme()));
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

    private BinExp parseBinaryExpression(){
        Exp left = parsePrimaryExpression();

        IrToken op = null;
        if(willMatch(IrTokenType.LT) || willMatch(IrTokenType.ADD) 
        || willMatch(IrTokenType.LE) || willMatch(IrTokenType.GT)
        || willMatch(IrTokenType.NE) || willMatch(IrTokenType.GE)
        || willMatch(IrTokenType.BOR) || willMatch(IrTokenType.SUB)
        || willMatch(IrTokenType.MUL) || willMatch(IrTokenType.DIV)
        || willMatch(IrTokenType.MOD) || willMatch(IrTokenType.BAND)
        || willMatch(IrTokenType.EQ)){
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

    private ICode parseReturn(){
        match(IrTokenType.RETURN);
        return new Return();
    }

    private ICode parseLabel(){
        match(IrTokenType.LABEL);
        IrToken id = match(IrTokenType.ID);
        return new Label(id.getLexeme());
    }

    private ICode parseGoto(){
        match(IrTokenType.GOTO);
        IrToken id = match(IrTokenType.ID);
        return new Goto(id.getLexeme());
    }

    private ICode parseProcedure(){
        match(IrTokenType.PROC);
        IrToken procName = match(IrTokenType.ID);
        match(IrTokenType.LPAR);
        
        List<String> args = new LinkedList<>();
        do{
            IrToken type = match(IrTokenType.ID);
            args.add(type.getLexeme());
        } while(skipIfYummy(IrTokenType.COMMA));

        match(IrTokenType.RPAR);

        return new Proc(procName.getLexeme(), args);
    }

    private UnExp parseUnaryExpression(){
        if(willMatch(IrTokenType.NEG)){
            skip();
            Exp right = parsePrimaryExpression();
            return new UnExp(UnExp.Operator.NEG, right);
        } else if(willMatch(IrTokenType.BNOT)){
            skip();
            Exp right = parsePrimaryExpression();
            return new UnExp(UnExp.Operator.BNOT, right);
        } else {
            return null;
        }
    }

    private ICode parseAssignment(){
        IrToken id = match(IrTokenType.ID);
        
        match(IrTokenType.ASSIGN);

        if(willMatch(IrTokenType.NEG) || willMatch(IrTokenType.BNOT)){
            UnExp unExpr = parseUnaryExpression();
            return new LetUn(id.getLexeme(), unExpr);
        } if(willMatch(IrTokenType.CALL)){
            skip();
            IrToken callName = match(IrTokenType.ID);
            match(IrTokenType.LPAR);

            List<String> args = new LinkedList<>();
            do{
                IrToken arg = match(IrTokenType.ID);
                args.add(arg.getLexeme());
            } while(skipIfYummy(IrTokenType.COMMA));

            match(IrTokenType.RPAR);

            return new Call(id.getLexeme(), callName.getLexeme(), args);
        } else if(willMatch(IrTokenType.FALSE)){
            skip();
            return new LetBool(id.getLexeme(), false);
        } else if(willMatch(IrTokenType.TRUE)){
            skip();
            return new LetBool(id.getLexeme(), true);
        } else if(willMatch(IrTokenType.STRING)){
            IrToken str = skip();
            return new LetString(id.getLexeme(), str.getLexeme());
        } else if(willMatch(IrTokenType.NUMBER)){
            IrToken num = skip();
            if(num.getLexeme().contains(".")){
                return new LetReal(id.getLexeme(), Double.parseDouble(num.getLexeme()));
            } else {
                return new LetInt(id.getLexeme(), Integer.parseInt(num.getLexeme()));
            }
        } else {
            BinExp binExp = parseBinaryExpression();
            return new LetBin(id.getLexeme(), binExp);
        }
    }
}
