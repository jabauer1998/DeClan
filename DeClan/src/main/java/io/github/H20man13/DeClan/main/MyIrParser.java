package io.github.H20man13.DeClan.main;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.ParseException;
import io.github.H20man13.DeClan.common.Position;
import io.github.H20man13.DeClan.common.icode.Call;
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
import io.github.H20man13.DeClan.common.icode.LetUn.Op;
import io.github.H20man13.DeClan.common.token.IrToken;
import io.github.H20man13.DeClan.common.token.IrTokenType;

public class MyIrParser {
    private MyIrLexer lexer;
    private ErrorLog errorLog;
    private IrToken current;
    private Position currentPosition;
    

    public MyIrParser(MyIrLexer lexer, ErrorLog errorLog){
        this.lexer = lexer;
        this.errorLog = errorLog;
        this.current = null;
        this.currentPosition = new Position(0, 0);
        skip();
    }

    public void close(){
        lexer.close();
    }

    boolean willMatch(IrTokenType type){
        return current != null && current.getType() == type;
    }

    IrToken match(IrTokenType type){
        if(willMatch(type)){
            return skip();
        } else if(current == null){
            errorLog.add("Expected " + type + ", found EOF", current.getPosition());
        } else {
            errorLog.add("Expected " + type + ", found" + current.getType(), currentPosition);
        }
        throw new ParseException("Parsing aborted");
    }

    void matchEOF(){
        if(current != null){
            errorLog.add("Expected end of file, found " + current.getType(), current.getPosition());
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
        matchEOF();
        return parseInstructions;
    }

    public List<ICode> parseInstructions(){
        List<ICode> toRet = new LinkedList<ICode>();
        while(willMatch(IrTokenType.LABEL) || willMatch(IrTokenType.IF) || willMatch(IrTokenType.ID) || willMatch(IrTokenType.GOTO) || willMatch(IrTokenType.PROC)){
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
        } else {
            return parseAssignment();
        }
    }

    private static If.Op toIfOp(IrTokenType type){
        switch(type){
            case NE: return If.Op.NE;
            case EQ: return If.Op.EQ;
            case GE: return If.Op.GE;
            case GT: return If.Op.GT;
            case LE: return If.Op.LE;
            case LT: return If.Op.LT;
            default: return null;
        }
    } 

    private ICode parseIfStatement(){
        match(IrTokenType.IF);
        IrToken id = match(IrTokenType.ID);
        IrToken op = null;
        if(willMatch(IrTokenType.LT)){
            op = skip();
        } else if(willMatch(IrTokenType.GT)){
            op = skip();
        } else if(willMatch(IrTokenType.NE)){
            op = skip();
        } else if(willMatch(IrTokenType.GE)){
            op = skip();
        } else if(willMatch(IrTokenType.LE)){
            op = skip();
        } else {
            op = match(IrTokenType.EQ);
        }

        IrToken atom = null;
        if(willMatch(IrTokenType.NUMBER)){
            atom = skip();
        } else if(willMatch(IrTokenType.TRUE)){
            atom = skip();
        } else if(willMatch(IrTokenType.FALSE)){
            atom = skip();
        } else {
            atom = match(IrTokenType.ID);
        }

        match(IrTokenType.THEN);

        IrToken labelOne = match(IrTokenType.ID);

        match(IrTokenType.ELSE);

        IrToken labelTwo = match(IrTokenType.ID);

        return new If(id.getLexeme(), toIfOp(op.getType()), atom.getLexeme(), labelOne.getLexeme(), labelTwo.getLexeme());
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

    private ICode parseAssignment(){
        IrToken id = match(IrTokenType.ID);
        
        match(IrTokenType.ASSIGN);

        if(willMatch(IrTokenType.MINUS)){
            skip();
            IrToken right = match(IrTokenType.ID);
            return new LetUn(id.getLexeme(), Op.NEG, right.getLexeme());
        } else if(willMatch(IrTokenType.NOT)){
            skip();
            IrToken right = match(IrTokenType.ID);
            return new LetUn(id.getLexeme(), Op.BNOT, right.getLexeme());
        } else if(willMatch(IrTokenType.CALL)){
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
            IrToken id2 = match(IrTokenType.ID);

            if(willMatch(IrTokenType.EQ)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.EQ, id3.getLexeme());
            } else if (willMatch(IrTokenType.NE)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.NE, id3.getLexeme());
            } else if (willMatch(IrTokenType.GT)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.GT, id3.getLexeme());
            } else if (willMatch(IrTokenType.GE)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.GE, id3.getLexeme());
            } else if (willMatch(IrTokenType.LT)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.LT, id3.getLexeme());
            } else if (willMatch(IrTokenType.LE)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.LE, id3.getLexeme());
            } else if (willMatch(IrTokenType.PLUS)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.ADD, id3.getLexeme());
            } else if (willMatch(IrTokenType.MINUS)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.SUB, id3.getLexeme());
            } else if (willMatch(IrTokenType.TIMES)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.MUL, id3.getLexeme());
            } else if (willMatch(IrTokenType.DIVIDE)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.DIV, id3.getLexeme());
            } else if (willMatch(IrTokenType.MODULO)){
                skip();
                IrToken id3 = match(IrTokenType.ID);
                return new LetBin(id.getLexeme(), id2.getLexeme(), LetBin.Op.MOD, id3.getLexeme());
            } else {
                return new LetVar(id.getLexeme(), id2.getLexeme());
            }
        }
    }
}
