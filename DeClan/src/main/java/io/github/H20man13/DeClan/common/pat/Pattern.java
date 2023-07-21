package io.github.H20man13.DeClan.common.pat;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.PAT;

public class Pattern {
    public static P multiplyAndAccumulate0 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate1 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate2 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate3 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate4 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate5 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate6 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate7 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate8 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.INT())));
    public static P multiplyAndAccumulate9 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.INT())));
    public static P multiplyAndAccumulate10 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.INT())),
                                                      P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.INT())));
    public static P multiplyAndAccumulate11 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.INT())),
                                                      P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.INT())));

    public static P add0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID()));
    public static P add1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.ID()));
    public static P add2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.INT()));
    public static P add3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.INT()));

    public static P sub0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.ID()));
    public static P sub1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ISUB(), P.ID()));
    public static P sub2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.INT()));
    public static P sub3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ISUB(), P.INT()));

    public static P mul0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID()));
    public static P mul1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.ID()));
    public static P mul2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.INT()));
    public static P mul3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.INT()));

    public static P div0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.ID()));
    public static P div1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIV(), P.ID()));
    public static P div2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.INT()));
    public static P div3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIV(), P.INT()));

    public static P divide0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIVIDE(), P.ID()));
    public static P divide1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIVIDE(), P.ID()));
    public static P divide2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIVIDE(), P.INT()));
    public static P divide3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIVIDE(), P.INT()));

    public static P mod0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMOD(), P.ID()));
    public static P mod1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMOD(), P.ID()));
    public static P mod2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMOD(), P.INT()));
    public static P mod3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMOD(), P.INT()));

    public static P ge0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.ID()));
    public static P ge1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.ID()));
    public static P ge2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.INT()));
    public static P ge3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.INT()));

    public static P gt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.ID()));
    public static P gt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.ID()));
    public static P gt2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.INT()));
    public static P gt3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.INT()));

    public static P lt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.ID()));
    public static P lt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.ID()));
    public static P lt2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.INT()));
    public static P lt3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.INT()));

    public static P le0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.ID()));
    public static P le1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.ID()));
    public static P le2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.INT()));
    public static P le3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.INT()));

    public static P eq0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.ID()));
    public static P eq1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.ID()));
    public static P eq2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.INT()));
    public static P eq3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.INT()));
    public static P eq4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.EQ(), P.ID()));
    public static P eq5= P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.BOOL()));
    public static P eq6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.EQ(), P.BOOL()));

    public static P ne0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.ID()));
    public static P ne1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.ID()));
    public static P ne2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.INT()));
    public static P ne3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.INT()));
    public static P ne4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.NE(), P.ID()));
    public static P ne5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.BOOL()));
    public static P ne6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.NE(), P.BOOL()));

    public static P neg0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INEG(), P.ID()));
    public static P neg1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INEG(), P.INT()));

    public static P bnot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.ID()));
    public static P bnot0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.BOOL()));

    public static P bool0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL()));
    public static P int0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT()));
    public static P id0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()));

    public static P if0 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if1 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if2 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if3 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if4 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if5 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if6 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if7 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if8 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if9 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if10 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if11 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if12 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if13 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if14 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if15 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if16 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if17 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if18 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if19 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if20 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if21 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if22 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.EQ(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if23 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if24 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if25 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if26 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if27 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if28 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if29 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.NE(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P goto0 = P.PAT(P.GOTO(), P.ID());
    public static P label0 = P.PAT(P.LABEL(), P.ID());
    public static P end0 = P.END();
    public static P return0 = P.RETURN();
    public static P proc0 = P.PAT(P.PROC(), P.ID());
}
