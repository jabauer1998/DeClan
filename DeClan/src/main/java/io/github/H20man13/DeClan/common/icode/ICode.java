package io.github.H20man13.DeClan.common.icode;

import java.util.List;

import io.github.H20man13.DeClan.common.pat.P;

public interface ICode {
    public String toString();
    public boolean isConstant();
    public boolean isBranch();
    public P asPattern();
    public boolean equals(Object object);
    public List<ICode> genFlatCode();
}
