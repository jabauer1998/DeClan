package io.github.H20man13.DeClan.common.pat;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.ASSIGN;
import io.github.H20man13.DeClan.common.pat.P.ID;
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

                                                    
    public static P multiplyAndAccumulate12 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IMUL(), P.ID())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate13 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.REAL())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate14 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IMUL(), P.REAL())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate15 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate16 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IMUL(), P.ID())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate17 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.REAL())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate18 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IMUL(), P.REAL())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IADD(), P.ID())));
    public static P multiplyAndAccumulate19 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.REAL())));
    public static P multiplyAndAccumulate20 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IMUL(), P.ID())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.REAL())));
    public static P multiplyAndAccumulate21 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.REAL())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.REAL())));
    public static P multiplyAndAccumulate22 = P.PAT(P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IMUL(), P.REAL())),
                                                    P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.REAL())));

    public static P add0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.ID()));
    public static P add1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.ID()));
    public static P add2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IADD(), P.INT()));
    public static P add3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IADD(), P.INT()));
    public static P add4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RADD(), P.ID()));
    public static P add5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RADD(), P.REAL()));
    public static P add6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RADD(), P.REAL()));
    public static P add7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RADD(), P.INT()));
    public static P add8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.RADD(), P.REAL()));
    public static P add9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RADD(), P.ID()));

    public static P sub0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.ID()));
    public static P sub1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ISUB(), P.ID()));
    public static P sub2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.ISUB(), P.INT()));
    public static P sub3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.ISUB(), P.INT()));
    public static P sub4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RSUB(), P.ID()));
    public static P sub5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RSUB(), P.REAL()));
    public static P sub6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RSUB(), P.REAL()));
    public static P sub7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RSUB(), P.INT()));
    public static P sub8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.RSUB(), P.REAL()));
    public static P sub9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RSUB(), P.ID()));

    public static P mul0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.ID()));
    public static P mul1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.ID()));
    public static P mul2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMUL(), P.INT()));
    public static P mul3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMUL(), P.INT()));
    public static P mul4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RMUL(), P.ID()));
    public static P mul5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RMUL(), P.REAL()));
    public static P mul6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RMUL(), P.REAL()));
    public static P mul7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RMUL(), P.INT()));
    public static P mul8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.RMUL(), P.REAL()));
    public static P mul9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RMUL(), P.ID()));

    public static P div0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.ID()));
    public static P div1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIV(), P.ID()));
    public static P div2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.INT()));
    public static P div3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIV(), P.INT()));
    public static P div4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IDIV(), P.ID()));
    public static P div5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.REAL()));
    public static P div6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IDIV(), P.REAL()));
    public static P div7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.IDIV(), P.INT()));
    public static P div8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IDIV(), P.REAL()));
    public static P div9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IDIV(), P.ID()));

    public static P divide0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RDIVIDE(), P.ID()));
    public static P divide1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.RDIVIDE(), P.ID()));
    public static P divide2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RDIVIDE(), P.INT()));
    public static P divide3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.RDIVIDE(), P.INT()));
    public static P divide4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RDIVIDE(), P.ID()));
    public static P divide5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RDIVIDE(), P.REAL()));
    public static P divide6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RDIVIDE(), P.REAL()));
    public static P divide7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.RDIVIDE(), P.INT()));
    public static P divide8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.RDIVIDE(), P.REAL()));
    public static P divide9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.RDIVIDE(), P.ID()));

    public static P mod0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMOD(), P.ID()));
    public static P mod1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMOD(), P.ID()));
    public static P mod2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IMOD(), P.INT()));
    public static P mod3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IMOD(), P.INT()));

    public static P bitwiseAnd0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IAND(), P.ID()));
    public static P bitwiseAnd1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IAND(), P.ID()));
    public static P bitiwseAnd2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IAND(), P.INT()));
    public static P bitwiseAnd3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IAND(), P.INT()));

    public static P bitwiseOr0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IOR(), P.ID()));
    public static P bitwiseOr1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IOR(), P.ID()));
    public static P bitiwseOr2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IOR(), P.INT()));
    public static P bitwiseOr3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IOR(), P.INT()));

    public static P bitwiseExclusiveOr0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IXOR(), P.ID()));
    public static P bitwiseExclusiveOr1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IXOR(), P.ID()));
    public static P bitiwseExclusiveOr2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.IXOR(), P.INT()));
    public static P bitwiseExclusiveOr3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.IXOR(), P.INT()));

    public static P ge0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.ID()));
    public static P ge1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.ID()));
    public static P ge2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.INT()));
    public static P ge3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.INT()));
    public static P ge4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.GE(), P.ID()));
    public static P ge5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GE(), P.REAL()));
    public static P ge6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.GE(), P.REAL()));
    public static P ge7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.GE(), P.INT()));
    public static P ge8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GE(), P.REAL()));

    public static P gt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.ID()));
    public static P gt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.ID()));
    public static P gt2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.INT()));
    public static P gt3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.INT()));
    public static P gt4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.GT(), P.ID()));
    public static P gt5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.GT(), P.REAL()));
    public static P gt6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.GT(), P.REAL()));
    public static P gt7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.GT(), P.INT()));
    public static P gt8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.GT(), P.REAL()));

    public static P lt0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.ID()));
    public static P lt1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.ID()));
    public static P lt2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.INT()));
    public static P lt3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.INT()));
    public static P lt4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.LT(), P.ID()));
    public static P lt5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LT(), P.REAL()));
    public static P lt6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.LT(), P.REAL()));
    public static P lt7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.LT(), P.INT()));
    public static P lt8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LT(), P.REAL()));

    public static P le0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.ID()));
    public static P le1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.ID()));
    public static P le2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.INT()));
    public static P le3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.INT()));
    public static P le4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.LE(), P.ID()));
    public static P le5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.LE(), P.REAL()));
    public static P le6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.LE(), P.REAL()));
    public static P le7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.LE(), P.INT()));
    public static P le8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.LE(), P.REAL()));

    public static P eq0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.ID()));
    public static P eq1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.ID()));
    public static P eq2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.INT()));
    public static P eq3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.INT()));
    public static P eq4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.EQ(), P.ID()));
    public static P eq5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.BOOL()));
    public static P eq6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.EQ(), P.BOOL()));
    public static P eq7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.EQ(), P.ID()));
    public static P eq8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.EQ(), P.REAL()));
    public static P eq9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.EQ(), P.REAL()));
    public static P eq10 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.EQ(), P.INT()));
    public static P eq11 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.EQ(), P.REAL()));

    public static P ne0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.ID()));
    public static P ne1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.ID()));
    public static P ne2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.INT()));
    public static P ne3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.INT()));
    public static P ne4 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.NE(), P.ID()));
    public static P ne5 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.BOOL()));
    public static P ne6 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.NE(), P.BOOL()));
    public static P ne7 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.NE(), P.ID()));
    public static P ne8 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.NE(), P.REAL()));
    public static P ne9 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.NE(), P.REAL()));
    public static P ne10 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.REAL(), P.NE(), P.INT()));
    public static P ne11 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT(), P.NE(), P.REAL()));

    public static P and0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BAND(), P.ID()));
    public static P and1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.BAND(), P.ID()));
    public static P and2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BAND(), P.BOOL()));
    public static P and3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.BAND(), P.BOOL()));

    public static P or0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BOR(), P.ID()));
    public static P or1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.BOR(), P.ID()));
    public static P or2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID(), P.BOR(), P.BOOL()));
    public static P or3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL(), P.BOR(), P.BOOL()));

    public static P neg0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INEG(), P.ID()));
    public static P neg1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INEG(), P.INT()));
    public static P neg2 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.RNEG(), P.ID()));
    public static P neg3 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.RNEG(), P.REAL()));

    public static P bnot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.ID()));
    public static P bnot0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BNOT(), P.BOOL()));

    public static P bitwiseNot0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INOT(), P.ID()));
    public static P bitwiseNot1 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INOT(), P.INT()));

    public static P bool0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.BOOL()));
    public static P int0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.INT()));
    public static P id0 = P.PAT(P.ID(), P.ASSIGN(), P.PAT(P.ID()));

    public static P if0 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if1 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if2 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if3 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if30 = P.PAT(P.IF(), P.PAT(P.REAL(), P.LT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if31 = P.PAT(P.IF(), P.PAT(P.ID(), P.LT(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if32 = P.PAT(P.IF(), P.PAT(P.REAL(), P.LT(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if33 = P.PAT(P.IF(), P.PAT(P.REAL(), P.LT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if34 = P.PAT(P.IF(), P.PAT(P.INT(), P.LT(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if4 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if5 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if6 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if7 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if35 = P.PAT(P.IF(), P.PAT(P.REAL(), P.GT(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if36 = P.PAT(P.IF(), P.PAT(P.ID(), P.GT(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if37 = P.PAT(P.IF(), P.PAT(P.REAL(), P.GT(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if38 = P.PAT(P.IF(), P.PAT(P.REAL(), P.GT(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if39 = P.PAT(P.IF(), P.PAT(P.INT(), P.GT(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if8 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if9 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if10 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if11 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if40 = P.PAT(P.IF(), P.PAT(P.REAL(), P.LE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if41 = P.PAT(P.IF(), P.PAT(P.ID(), P.LE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if42 = P.PAT(P.IF(), P.PAT(P.REAL(), P.LE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if43 = P.PAT(P.IF(), P.PAT(P.REAL(), P.LE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if44 = P.PAT(P.IF(), P.PAT(P.INT(), P.LE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if12 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if13 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if14 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if15 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if45 = P.PAT(P.IF(), P.PAT(P.REAL(), P.GE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if46 = P.PAT(P.IF(), P.PAT(P.ID(), P.GE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if47 = P.PAT(P.IF(), P.PAT(P.REAL(), P.GE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if48 = P.PAT(P.IF(), P.PAT(P.REAL(), P.GE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if49 = P.PAT(P.IF(), P.PAT(P.INT(), P.GE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if16 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if17 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if18 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if19 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if20 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if21 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if22 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.EQ(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if50 = P.PAT(P.IF(), P.PAT(P.REAL(), P.EQ(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if51 = P.PAT(P.IF(), P.PAT(P.ID(), P.EQ(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if52 = P.PAT(P.IF(), P.PAT(P.REAL(), P.EQ(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if53 = P.PAT(P.IF(), P.PAT(P.REAL(), P.EQ(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if54 = P.PAT(P.IF(), P.PAT(P.INT(), P.EQ(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if23 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if24 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if25 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if26 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if27 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if28 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if29 = P.PAT(P.IF(), P.PAT(P.BOOL(), P.NE(), P.BOOL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P if55 = P.PAT(P.IF(), P.PAT(P.REAL(), P.NE(), P.ID()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if56 = P.PAT(P.IF(), P.PAT(P.ID(), P.NE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if57 = P.PAT(P.IF(), P.PAT(P.REAL(), P.NE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if58 = P.PAT(P.IF(), P.PAT(P.REAL(), P.NE(), P.INT()), P.THEN(), P.ID(), P.ELSE(), P.ID());
    public static P if59 = P.PAT(P.IF(), P.PAT(P.INT(), P.NE(), P.REAL()), P.THEN(), P.ID(), P.ELSE(), P.ID());

    public static P paramAssign0 = P.PAT(P.ID(), P.ASSIGN(), P.PARAM());
    public static P goto0 = P.PAT(P.GOTO(), P.ID());
    public static P label0 = P.PAT(P.LABEL(), P.ID());
    public static P end0 = P.END();
    public static P return0 = P.RETURN();
    public static P proc0 = P.PAT(P.PROC(), P.ID());
    public static P inline0 = P.INLINE();
}
