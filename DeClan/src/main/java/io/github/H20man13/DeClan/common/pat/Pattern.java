package io.github.H20man13.DeClan.common.pat;

public class Pattern {
    //The Next pattern we will initialize is a Function call followed by a return Placement
    //There will be a pattern for just standard function Calls without a return as well
    public static P callWithReturn0 = P.PAT(P.PAT(P.CALL(), P.ID()),
                                            P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.BOOL()));
    public static P callWithReturn1 = P.PAT(P.PAT(P.CALL(), P.ID()),
            								P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.BOOL()));
    public static P callWithReturn2 = P.PAT(P.PAT(P.CALL(), P.ID()),
            								P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.INT()));
    public static P callWithReturn3 = P.PAT(P.PAT(P.CALL(), P.ID()),
											P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.INT()));
    public static P callWithReturn4 = P.PAT(P.PAT(P.CALL(), P.ID()),
            								P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.REAL()));
    public static P callWithReturn5 = P.PAT(P.PAT(P.CALL(), P.ID()),
											P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.REAL()));
    public static P callWithReturn6 = P.PAT(P.PAT(P.CALL(), P.ID()),
									  P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.STR()));
    public static P callWithReturn7 = P.PAT(P.PAT(P.CALL(), P.ID()),
											P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.PAT(P.RETURN(), P.ID())), P.STR()));
    

    //Addition Patterns
    public static P add0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID()), P.INT());
    public static P add1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID()), P.INT());
    
    //Subtraction Patterns
    public static P sub0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.ID()), P.INT());
    public static P sub1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.ID()), P.INT());
    
    //Bitwise And Patterns
    public static P bitwiseAnd0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IAND(), P.ID()), P.INT());
    public static P bitwiseAnd1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IAND(), P.ID()), P.INT());

    //Bitwise Or Pattern
    public static P bitwiseOr0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IOR(), P.ID()), P.INT());
    public static P bitwiseOr1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IOR(), P.ID()), P.INT());

    //Bitwise Xor Patterns
    public static P bitwiseExclusiveOr0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IXOR(), P.ID()), P.INT());
    public static P bitwiseExclusiveOr1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IXOR(), P.ID()), P.INT());

    //Left Shift Patterns
    public static P leftShift0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LSHIFT(), P.ID()), P.INT());
    public static P leftShift1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LSHIFT(), P.ID()), P.INT());

    //Right Shift Patterns
    public static P rightShift0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RSHIFT(), P.ID()), P.INT());
    public static P rightShift1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RSHIFT(), P.ID()), P.INT());

    //Greater or Equal To Pattern
    public static P ge0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.ID()), P.BOOL());
    public static P ge1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.ID()), P.BOOL());

    //Greater then pattern
    public static P gt0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.ID()), P.BOOL());
    public static P gt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.ID()), P.BOOL());

    //Less then Pattern
    public static P lt0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.ID()), P.BOOL());
    public static P lt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.ID()), P.BOOL());

    //Less then or equal Pattern
    public static P le0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.ID()), P.BOOL());
    public static P le1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.ID()), P.BOOL());

    //Boolean Equal to Pattern
    public static P bEq0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BEQ(), P.ID()), P.BOOL());
    public static P bEq1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BEQ(), P.ID()), P.BOOL());
    
    //Integer Equal to Pattern
    public static P iEq0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IEQ(), P.ID()), P.BOOL());
    public static P iEq1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IEQ(), P.ID()), P.BOOL());

    //Boolean Not equal to patterns
    public static P bNe0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BNE(), P.ID()), P.BOOL());
    public static P bNe1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BNE(), P.ID()), P.BOOL());
    
    //Integer Not Equal to patterns
    public static P iNe0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.INE(), P.ID()), P.BOOL());
    public static P iNe1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.INE(), P.ID()), P.BOOL());

    //Boolean And patterns
    public static P and0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BAND(), P.ID()), P.BOOL());
    public static P and1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BAND(), P.ID()), P.BOOL());

    //Boolean Or Patterns
    public static P or0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BOR(), P.ID()), P.BOOL());
    public static P or1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BOR(), P.ID()), P.BOOL());

    //Boolean Negation Patterns
    public static P bnot0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.ID()), P.BOOL());
    public static P bnot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.ID()), P.BOOL());

    //Bitwise Not Patterns
    public static P bitwiseNot0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.INOT(), P.ID()), P.INT());
    public static P bitwiseNot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INOT(), P.ID()), P.INT());
    
    //Boolean Init/Assign Patterns
    public static P bool0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.BOOL()), P.BOOL());
    public static P bool1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL()), P.BOOL());
    
    //Real Init/Assign Patterns
    public static P real0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.REAL()), P.REAL());
    public static P real1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL()), P.REAL());
    
    //Int Init/Assign Patterns
    public static P int0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.INT()), P.INT());
    public static P int1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT()), P.INT());
    
    //String Init/Assign Patterns
    public static P str0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.STR()), P.STR());
    public static P str1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.STR()), P.STR());
    
    //Identifier Assignments Patterns
    public static P id0 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.BOOL());
    public static P id1 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.REAL());
    public static P id2 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.INT());
    public static P id3 = P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.STR());
    public static P id4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.BOOL());
    public static P id5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.REAL());
    public static P id6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.INT());
    public static P id7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()), P.STR());
    

    public static P if0 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if1 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if2 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if3 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if4 = P.PAT(P.IF(), P.PAT(P.ID(), P.IEQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if5 = P.PAT(P.IF(), P.PAT(P.ID(), P.INE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if6 = P.PAT(P.IF(), P.PAT(P.ID(), P.BEQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if7 = P.PAT(P.IF(), P.PAT(P.ID(), P.BNE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    //Declare all usefull headers inside the code generator
    public static P dataSectionHeader = P.PAT(P.DATA(), P.SECTION());
    public static P bssSectionHeader = P.PAT(P.BSS(), P.SECTION());
    public static P codeSectionHeader = P.PAT(P.CODE(), P.SECTION());
    public static P procSectionHeader = P.PAT(P.PROC(), P.SECTION());
    
    //Declare all spill patterns
    public static P spill0 = P.PAT(P.SPILL(), P.ID(), P.INT());
    public static P spill1 = P.PAT(P.SPILL(), P.ID(), P.REAL());
    public static P spill2 = P.PAT(P.SPILL(), P.ID(), P.STR());
    public static P spill3 = P.PAT(P.SPILL(), P.ID(), P.BOOL());
    
    public static P goto0 = P.PAT(P.GOTO(), P.ID());
    public static P label0 = P.PAT(P.LABEL(), P.ID());
    public static P procLabel0 = P.PAT(P.PROC(), P.LABEL(), P.ID());
    public static P end0 = P.END();
    public static P return0 = P.PAT(P.RETURN(), P.FROM(), P.ID());
    public static P call0 = P.PAT(P.CALL(), P.ID());
    public static P inline0 = P.INLINE();
}
