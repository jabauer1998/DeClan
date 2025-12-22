package io.github.H20man13.DeClan.common.token;

import java.util.HashMap;
import java.util.Map;

public enum IrTokenType {
    ID, //Identifiers
    NUMBER,
    STRING,
    SPECIFIER,
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
    IADD,
    ISUB,
    LOR,
    LAND,
    IAND,
    IOR,
    IXOR,
    INOT,
    IRSHIFT,
    ILSHIFT,
    IPARAM,
    BNOT,
    LT,
    GT,
    GE,
    LE,
    BNE,
    INE,
    BEQ,
    IEQ,
    END,
    RETURN,
    IASM,
    SECTION,
    CODE,
    DATA,
    EXTERNAL,
    SYMBOL,
    INTERNAL,
    CONST,
    GLOBAL,
    DEF,
    ENTRY,
    BSS,
    FROM,
    SPILL,
    
    //Operators
    ASSIGN,
    MAP,
    PARAM,

    //Assign Types
    INT,
    REAL,
    BOOL,

    //Other Misc Symbols
    COMMA,
    LPAR,
    RPAR,
    LBRACK,
    RBRACK,
    LANGLE,
    RANGLE;

    public static final Map<String, IrTokenType> reservedIr;
    private static final Map<Character, IrTokenType> singleOperators;
    private static final Map<String, IrTokenType> dualOperators;

    private static void addKeyword(IrTokenType type){
        reservedIr.put(type.toString(), type);
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
        reservedIr = new HashMap<>();
        addKeyword(LABEL);
        addKeyword(IF);
        addKeyword(THEN);
        addKeyword(ELSE);
        addKeyword(TRUE);
        addKeyword(FALSE);
        addKeyword(GOTO);
        addKeyword(CALL);
        addKeyword(PROC);
        addKeyword(BNOT);
        addKeyword(ISUB);
        addKeyword(IADD);
        addKeyword(LAND);
        addKeyword(LOR);
        addKeyword(IAND);
        addKeyword(IOR);
        addKeyword(IXOR);
        addKeyword(INOT);
        addKeyword(ILSHIFT);
        addKeyword(IRSHIFT);
        addKeyword(LT);
        addKeyword(GT);
        addKeyword(GE);
        addKeyword(LE);
        addKeyword(INE);
        addKeyword(BNE);
        addKeyword(BEQ);
        addKeyword(IEQ);
        addKeyword(END);
        addKeyword(RETURN);
        addKeyword(IASM);
        addKeyword(SECTION);
        addKeyword(CODE);
        addKeyword(DATA);
        addKeyword(EXTERNAL);
        addKeyword(INTERNAL);
        addKeyword(SYMBOL);
        addKeyword(CONST);
        addKeyword(PARAM);
        addKeyword(INT);
        addKeyword(REAL);
        addKeyword(BOOL);
        addKeyword(STRING);
        addKeyword(IPARAM);
        addKeyword(GLOBAL);
        addKeyword(DEF);
        addKeyword(ENTRY);
        addKeyword(BSS);
        addKeyword(FROM);
        addKeyword(SPILL);

        dualOperators = new HashMap<>();
        addDualOp(":=", ASSIGN);
        addDualOp("->", MAP);

        singleOperators = new HashMap<>();
        addSingleOp(',', COMMA);
        addSingleOp('(', LPAR);
        addSingleOp(')', RPAR);
        addSingleOp('[', LBRACK);
        addSingleOp(']', RBRACK);
        addSingleOp('<', LANGLE);
        addSingleOp('>', RANGLE);
    }
}
