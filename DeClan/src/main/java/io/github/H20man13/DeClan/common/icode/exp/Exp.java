package io.github.H20man13.DeClan.common.icode.exp;

public interface Exp {
    @Override
    public boolean equals(Object exp);
    @Override
    public String toString();

    public boolean isBranch();

    public boolean isConstant();
}
