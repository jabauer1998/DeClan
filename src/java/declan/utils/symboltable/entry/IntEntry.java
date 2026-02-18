package io.github.h20man13.DeClan.common.symboltable.entry;

import io.github.h20man13.DeClan.common.Copyable;

public class IntEntry implements Copyable<IntEntry> {
    private int value;

    public IntEntry(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    @Override
    public IntEntry copy() {
        return new IntEntry(value);
    }
}
