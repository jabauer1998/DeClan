package io.github.H20man13.DeClan.common.symboltable.entry;

import io.github.H20man13.DeClan.common.Copyable;

public class TypeCheckerQualities implements Copyable<TypeCheckerQualities> {
    private int val;
    
    public TypeCheckerQualities(int value) {
        this.val = value;
    }
    
    public static final int INTEGER = 0b0000001;
    public static final int BOOLEAN = 0b0000010;
    public static final int STRING = 0b00000100;
    public static final int REAL = 0b0000001000;
    public static final int VOID = 0b0000010000;
    public static final int NA = 0b000000100000;
    public static final int NEG = 0b00001000000;
    public static final int NULL = 0b0010000000;
    public static final int CONST = 0b100000000;

    public boolean containsQualities(int quality){
        return (quality & this.val) == quality;
    }

    public boolean missingQualities(int quality){
        return (quality & this.val) == 0;
    }

    @Override
    public TypeCheckerQualities copy() {
        return new TypeCheckerQualities(val);
    }
}
