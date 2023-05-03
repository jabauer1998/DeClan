package io.github.H20man13.DeClan;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ErrorLog.LogItem;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.token.IrToken;
import io.github.H20man13.DeClan.common.token.IrTokenType;
import io.github.H20man13.DeClan.main.MyIrLexer;

public class IrLexerTest {

    public void testLexer(MyIrLexer lex, List<IrTokenType> tokTypes){
        List<IrToken> toks = new LinkedList<>();
        while(lex.hasNext()){
            IrToken tok = lex.next();
            toks.add(tok);
        }
        compareTokensToTypes(toks, tokTypes);
    }

    public void compareTokensToTypes(List<IrToken> tokens, List<IrTokenType> tokTypes){
        assertTrue(tokens.size() == tokTypes.size());

        for(int i = 0; i < tokens.size(); i++){
            IrToken tok = tokens.get(i);
            IrTokenType type = tokTypes.get(i);
            assertTrue("Expected token of type " + tok.getType() + " but got token of type " + type, tok.getType() == type);
        }
    }


    @Test
    public void testKeywords(){
        String keywords = "LABEL IF TRUE FALSE THEN ELSE GOTO CALL PROC PLUS MINUS NOT TIMES DIVIDE MODULO LT GT GE LE NE EQ";
        Source source = new ReaderSource(new StringReader(keywords));
        ErrorLog errLog = new ErrorLog();
        MyIrLexer lex = new MyIrLexer(source, errLog);
        
        List<IrTokenType> tokTypes = new LinkedList<IrTokenType>();
        tokTypes.add(IrTokenType.LABEL);
        tokTypes.add(IrTokenType.IF);
        tokTypes.add(IrTokenType.TRUE);
        tokTypes.add(IrTokenType.FALSE);
        tokTypes.add(IrTokenType.THEN);
        tokTypes.add(IrTokenType.ELSE);
        tokTypes.add(IrTokenType.GOTO);
        tokTypes.add(IrTokenType.CALL); 
        tokTypes.add(IrTokenType.PROC);
        tokTypes.add(IrTokenType.PLUS);
        tokTypes.add(IrTokenType.MINUS);
        tokTypes.add(IrTokenType.NOT); 
        tokTypes.add(IrTokenType.TIMES);
        tokTypes.add(IrTokenType.DIVIDE);
        tokTypes.add(IrTokenType.MODULO);
        tokTypes.add(IrTokenType.LT); 
        tokTypes.add(IrTokenType.GT);
        tokTypes.add(IrTokenType.GE);
        tokTypes.add(IrTokenType.LE);
        tokTypes.add(IrTokenType.NE);
        tokTypes.add(IrTokenType.EQ);

        for(LogItem item : errLog){
            assertTrue(item.toString(), false);
        }

        testLexer(lex, tokTypes);
    }

    @Test
    public void testSymbols(){
        String symbols = "( , ) :=";
        Source source = new ReaderSource(new StringReader(symbols));
        ErrorLog errLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(source, errLog);

        List<IrTokenType> tokTypes = new LinkedList<>();
        tokTypes.add(IrTokenType.LPAR);
        tokTypes.add(IrTokenType.COMMA);
        tokTypes.add(IrTokenType.RPAR);
        tokTypes.add(IrTokenType.ASSIGN);

        for(LogItem item : errLog){
            assertTrue(item.toString(), false);
        }

        testLexer(lexer, tokTypes);
    }

    @Test
    public void testNumber(){
        String numbers = "1090 244.45";
        Source source = new ReaderSource(new StringReader(numbers));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(source, errorLog);

        List<IrTokenType> tokTypes = new LinkedList<>();
        tokTypes.add(IrTokenType.NUMBER);
        tokTypes.add(IrTokenType.NUMBER);

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        testLexer(lexer, tokTypes);
    }

    @Test
    public void testIdentifier(){
        String identifiers = "fjlkadjfd LABEL_TEXT";
        Source source = new ReaderSource(new StringReader(identifiers));
        ErrorLog errorLog = new ErrorLog();
        MyIrLexer lexer = new MyIrLexer(source, errorLog);

        List<IrTokenType> tokTypes = new LinkedList<>();
        tokTypes.add(IrTokenType.ID);
        tokTypes.add(IrTokenType.ID);

        for(LogItem item : errorLog){
            assertTrue(item.toString(), false);
        }

        testLexer(lexer, tokTypes);
    }
}
