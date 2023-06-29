package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.pat.P;

public interface ICode {
    public String toString();
    public boolean isConstant();
    public boolean isBranch();
    public P asPattern();
}
