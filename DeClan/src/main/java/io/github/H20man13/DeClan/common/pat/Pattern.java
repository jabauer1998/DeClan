package io.github.H20man13.DeClan.common.pat;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.ASSIGN;
import io.github.H20man13.DeClan.common.pat.P.ID;
import io.github.H20man13.DeClan.common.pat.P.PAT;

public class Pattern {
    //The First patterns we will initialize here are Two Step Patterns

    //The First pattern is to identify possible use cases of the Multiply and Accumulate Function
    public static P multiplyAndAccumulate0 = P.PAT(P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID())),
                                                   P.PAT(P.DEF(), P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    //The Next pattern we will initialize is a Function call followed by a return Placement
    //There will be a pattern for just standard function Calls without a return as well
    public static P callWithReturn0 = P.PAT(P.PAT(P.CALL(), P.ID()),
                                            P.PAT(P.ID(), P.EXTERNAL(), P.PLACE(), P.ID()));

    public static P add0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID()));

    public static P sub0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.ID()));

    public static P mul0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID()));

    public static P div0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.ID()));

    public static P divide0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RDIVIDE(), P.ID()));

    public static P mod0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMOD(), P.ID()));

    public static P bitwiseAnd0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IAND(), P.ID()));

    public static P bitwiseOr0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IOR(), P.ID()));

    public static P bitwiseExclusiveOr0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IXOR(), P.ID()));

    public static P leftShift0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LSHIFT(), P.ID()));

    public static P rightShift0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RSHIFT(), P.ID()));

    public static P ge0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.ID()));

    public static P gt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.ID()));

    public static P lt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.ID()));

    public static P le0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.ID()));

    public static P eq0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.ID()));

    public static P ne0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.ID()));

    public static P and0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BAND(), P.ID()));

    public static P or0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BOR(), P.ID()));

    public static P neg0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INEG(), P.ID()));
    public static P neg2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.RNEG(), P.ID()));

    public static P bnot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.ID()));

    public static P bitwiseNot0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INOT(), P.ID()));

    public static P bool0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL()));
    public static P real0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL()));
    public static P int0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT()));
    public static P id0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()));

    public static P if0 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if4 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if8 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if12 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if16 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if23 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P paramAssign0 = P.PAT(P.ID(), P.ASSIGN(), P.PARAM());
    public static P internalReturnPlacement0 = P.PAT(P.ID(), P.INTERNAL(), P.PLACE(), P.ID());
    public static P goto0 = P.PAT(P.GOTO(), P.ID());
    public static P label0 = P.PAT(P.LABEL(), P.ID());
    public static P procLabel0 = P.PAT(P.PROC(), P.LABEL(), P.ID());
    public static P end0 = P.END();
    public static P return0 = P.RETURN();
    public static P call0 = P.PAT(P.CALL(), P.ID());
    public static P inline0 = P.INLINE();
}
