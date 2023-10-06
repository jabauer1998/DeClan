package io.github.H20man13.DeClan.common.symboltable.entry;

import io.github.H20man13.DeClan.common.Copyable;

public class NullEntry implements  Copyable<NullEntry>{
    public NullEntry(){}

    @Override
    public NullEntry copy() {
        return this;
    }
}
