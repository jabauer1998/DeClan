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
    IADD,
    ISUB,
    IMUL,
    IDIV,
    IDIVIDE,
    IMOD,
    RADD,
    RSUB,
    RMUL,
    RDIV,
    RDIVIDE,
    LOR,
    LAND,
    IAND,
    IOR,
    IXOR,
    INOT,
    IRSHIFT,
    ILSHIFT,
    INEG,
    RNEG,
    BNOT,
    LT,
    GT,
    GE,
    LE,
    NE,
    EQ,
    END,
    RETURN,
    IASM,
    IPARAM,
    SECTION,
    CODE,
    DATA,
    EXTERNAL,
    SYMBOL,
    INTERNAL,
    CONST,
    
    //Operators
    ASSIGN,
    IPLACE,
    EPLACE,
    MAP,
    PARAM_ASSIGN,

    //Other Misc Symbols
    COMMA,
    LPAR,
    RPAR;

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
        addKeyword(INEG);
        addKeyword(RNEG);
        addKeyword(BNOT);
        addKeyword(ISUB);
        addKeyword(IMUL);
        addKeyword(IDIV);
        addKeyword(IDIVIDE);
        addKeyword(IMOD);
        addKeyword(IADD);
        addKeyword(RSUB);
        addKeyword(RMUL);
        addKeyword(RDIV);
        addKeyword(RDIVIDE);
        addKeyword(RADD);
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
        addKeyword(NE);
        addKeyword(EQ);
        addKeyword(END);
        addKeyword(RETURN);
        addKeyword(IASM);
        addKeyword(IPARAM);
        addKeyword(SECTION);
        addKeyword(CODE);
        addKeyword(DATA);
        addKeyword(EXTERNAL);
        addKeyword(INTERNAL);
        addKeyword(SYMBOL);
        addKeyword(CONST);

        dualOperators = new HashMap<>();
        addDualOp(":=", ASSIGN);
        addDualOp("->", MAP);
        addDualOp("<|", EPLACE);
        addDualOp("|<", IPLACE);
        addDualOp("<-", PARAM_ASSIGN);

        singleOperators = new HashMap<>();
        addSingleOp(',', COMMA);
        addSingleOp('(', LPAR);
        addSingleOp(')', RPAR);
    }
}
