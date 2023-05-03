package io.github.H20man13.DeClan.common.token;

import java.util.HashMap;
import java.util.Map;

public enum IrTokenType {
    ID, //Identifiers
    NUMBER,
    STRING,
    //Reserved Keywords
    LABEL,
    IF,
    TRUE,
    FALSE,
    THEN,
    ELSE,
    GOTO,
    CALL,
    PROC,
    PLUS,
    MINUS,
    NOT,
    TIMES,
    DIVIDE,
    MODULO,
    LT,
    GT,
    GE,
    LE,
    NE,
    EQ,
    
    //Operators
    ASSIGN,

    //Other Misc Symbols
    COMMA,
    LPAR,
    RPAR;

    public static final Map<String, IrTokenType> reserved;
    private static final Map<Character, IrTokenType> singleOperators;
    private static final Map<String, IrTokenType> dualOperators;

    private static void addKeyword(IrTokenType type){
        reserved.put(type.toString(), type);
    }

    private static void addSingleOp(Character key, IrTokenType type){
        singleOperators.put(key, type);
    }

    private static void addDualOp(String key, IrTokenType type){
        dualOperators.put(key, type);
    }

    public static boolean contDualOpToken(String key){
        return dualOperators.containsKey(key);
    }

    public static boolean contSingleOpToken(char key){
        return singleOperators.containsKey(key);
    }

    public static IrTokenType getDualOpToken(String key){
        return dualOperators.get(key);
    }

    public static IrTokenType getSingleOpToken(String key){
        return singleOperators.get(key.charAt(0));
    }

    static {
        reserved = new HashMap<>();
        addKeyword(LABEL);
        addKeyword(IF);
        addKeyword(THEN);
        addKeyword(ELSE);
        addKeyword(TRUE);
        addKeyword(FALSE);
        addKeyword(GOTO);
        addKeyword(CALL);
        addKeyword(PROC);
        addKeyword(PLUS);
        addKeyword(MINUS);
        addKeyword(TIMES);
        addKeyword(DIVIDE);
        addKeyword(MODULO);
        addKeyword(LT);
        addKeyword(GT);
        addKeyword(NE);
        addKeyword(EQ);

        dualOperators = new HashMap<>();
        addDualOp(":=", ASSIGN);

        singleOperators = new HashMap<>();
        addSingleOp(',', COMMA);
        addSingleOp('(', LPAR);
        addSingleOp(')', RPAR);
    }
}
