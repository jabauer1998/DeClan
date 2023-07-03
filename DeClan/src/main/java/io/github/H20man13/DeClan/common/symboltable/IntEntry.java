package io.github.H20man13.DeClan.common.symboltable;

import io.github.H20man13.DeClan.common.Copyable;

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
