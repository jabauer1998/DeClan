package io.github.h20man13.DeClan.common.symboltable.entry;

import io.github.h20man13.DeClan.common.Copyable;

public class StringEntry implements Copyable<StringEntry> {
    private String string;

    public StringEntry(String string){
        this.string = string;
    }

    @Override
    public StringEntry copy() {
        return new StringEntry(string);
    }

    @Override
    public String toString(){
        return this.string;
    }
    
}
