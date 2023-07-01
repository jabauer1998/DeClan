package io.github.H20man13.DeClan.common.pat;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.PAT;

public class Pattern {
    public static PAT multiplyAndAccumulate0 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate1 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate2 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate3 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate4 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate5 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate6 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate7 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.INT())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ADD(), P.ID())));
    public static PAT multiplyAndAccumulate8 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.INT())));
    public static PAT multiplyAndAccumulate9 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.ID())),
                                                     P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.INT())));
    public static PAT multiplyAndAccumulate10 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.INT())),
                                                      P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.INT())));
    public static PAT multiplyAndAccumulate11 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.INT())),
                                                      P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.INT())));

    public static PAT add0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.ID()));
    public static PAT add1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ADD(), P.ID()));
    public static PAT add2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ADD(), P.INT()));
    public static PAT add3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ADD(), P.INT()));

    public static PAT sub0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.SUB(), P.ID()));
    public static PAT sub1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.SUB(), P.ID()));
    public static PAT sub2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.SUB(), P.INT()));
    public static PAT sub3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.SUB(), P.INT()));

    public static PAT mul0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.ID()));
    public static PAT mul1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.ID()));
    public static PAT mul2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MUL(), P.INT()));
    public static PAT mul3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MUL(), P.INT()));

    public static PAT div0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.DIV(), P.ID()));
    public static PAT div1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.DIV(), P.ID()));
    public static PAT div2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.DIV(), P.INT()));
    public static PAT div3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.DIV(), P.INT()));

    public static PAT mod0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MOD(), P.ID()));
    public static PAT mod1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MOD(), P.ID()));
    public static PAT mod2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.MOD(), P.INT()));
    public static PAT mod3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.MOD(), P.INT()));

    public static PAT ge0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.ID()));
    public static PAT ge1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.ID()));
    public static PAT ge2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.INT()));
    public static PAT ge3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.INT()));

    public static PAT gt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.ID()));
    public static PAT gt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.ID()));
    public static PAT gt2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.INT()));
    public static PAT gt3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.INT()));

    public static PAT lt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.ID()));
    public static PAT lt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.ID()));
    public static PAT lt2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.INT()));
    public static PAT lt3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.INT()));

    public static PAT le0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.ID()));
    public static PAT le1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.ID()));
    public static PAT le2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.INT()));
    public static PAT le3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.INT()));

    public static PAT eq0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.ID()));
    public static PAT eq1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.ID()));
    public static PAT eq2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.INT()));
    public static PAT eq3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.INT()));
    public static PAT eq4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.EQ(), P.ID()));
    public static PAT eq5= P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.BOOL()));
    public static PAT eq6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.EQ(), P.BOOL()));

    public static PAT ne0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.ID()));
    public static PAT ne1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.ID()));
    public static PAT ne2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.INT()));
    public static PAT ne3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.INT()));
    public static PAT ne4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.NE(), P.ID()));
    public static PAT ne5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.BOOL()));
    public static PAT ne6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.NE(), P.BOOL()));

    public static PAT neg0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.NEG(), P.ID()));
    public static PAT neg1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.NEG(), P.INT()));

    public static PAT bnot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.ID()));
    public static PAT bnot0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.BOOL()));

    public static PAT bool0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL()));
    public static PAT int0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT()));

    public static PAT if0 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if1 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if2 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if3 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static PAT if4 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if5 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if6 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if7 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static PAT if8 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if9 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if10 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if11 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static PAT if12 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if13 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if14 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if15 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static PAT if16 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if17 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if18 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if19 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if20 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if21 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if22 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.EQ(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static PAT if23 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if24 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if25 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if26 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if27 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if28 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static PAT if29 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.NE(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static PAT goto0 = P.PAT(P.GOTO(), P.ID());
    public static PAT label = P.PAT(P.LABEL(), P.ID());
}
