package io.github.h20man13.DeClan.common.icode.symbols;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.h20man13.DeClan.common.Copyable;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.exp.IdentExp;
import io.github.h20man13.DeClan.common.pat.P;

public abstract class SymEntry extends ICode{
    private int symType;
    public String icodePlace;

    protected SymEntry(int symType, String icodePlace){
        this.symType = symType;
        this.icodePlace = icodePlace;
    }
    
    protected SymEntry(SymEntry toCopy) {
    	this(toCopy.symType, toCopy.icodePlace);
    }

    public static final int ANY = 0b0;
    public static final int CONST = 0b1;
    public static final int EXTERNAL = 0b1000;
    public static final int INTERNAL = 0b10000;
    public static final int LOCAL = 0b100000;
    public static final int GLOBAL = 0b1000000;
    public static final int PARAM =  0b10000000;
    public static final int RETURN = 0b100000000;
    
    public boolean containsAllQualities(int qualityMask){
        return (this.symType & qualityMask) == qualityMask;
    }
    
    public boolean containsAnyQualities(int qualityMask) {
    	return (this.symType & qualityMask) != 0;
    }

    public boolean missingAllQualities(int qualityMask){
        return (this.symType ^ qualityMask) == qualityMask;
    }
    
    public boolean missingAnyQualities(int qualityMask) {
    	return (this.symType ^ qualityMask) != 0;
    }

    @Override
    public boolean isConstant() {
        return containsAnyQualities(SymEntry.CONST);
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    @Override
    public boolean containsPlace(String place){
        if(icodePlace.equals(place))
            return true;
        return false;
    }

    @Override
    public boolean containsLabel(String label){
        return false;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof SymEntry){
            SymEntry otherEntry = (SymEntry)obj;
            if(otherEntry.icodePlace.equals(icodePlace))
                return otherEntry.symType == symType; 
        }
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        if(icodePlace.equals(from))
            this.icodePlace = to;    
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing this is just a place holder
    }

    public abstract String toString();
}
