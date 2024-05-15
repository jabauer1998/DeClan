package io.github.H20man13.DeClan.common.icode.symbols;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public abstract class SymEntry implements ICode, Copyable<SymEntry> {
    protected int symType;
    public String icodePlace;

    protected SymEntry(int symType, String icodePlace){
        this.symType = symType;
        this.icodePlace = icodePlace;
    }

    public static final int CONST = 0b1;
    public static final int EXTERNAL = 0b1000;
    public static final int INTERNAL = 0b10000;
    
    public boolean containsQualities(int qualityMask){
        return (this.symType & qualityMask) == qualityMask;
    }

    public boolean missingQualities(int qualityMask){
        return (this.symType ^ qualityMask) == qualityMask;
    }

    @Override
    public boolean isConstant() {
        return containsQualities(SymEntry.CONST);
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
            SymEntry entry = (SymEntry)obj;
            
            boolean maskEquals = entry.symType == symType;
            boolean placeEquals = entry.icodePlace.equals(icodePlace);

            return maskEquals && placeEquals;
        } else {
            return false;
        }
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> list = new LinkedList<ICode>();
        list.add(this);
        return list;
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

    @Override
    public boolean containsParamater(String place) {
        return false;
    }

    @Override
    public boolean containsArgument(String place) {
        return false;
    }

    @Override
    public Set<String> paramaterForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public Set<String> argumentInFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public Set<String> internalReturnForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public Set<String> externalReturnForFunctions(String place) {
        return new HashSet<String>();
    }

    @Override
    public boolean containsInternalReturn(String place) {
        return false;
    }

    @Override
    public boolean containsExternalReturn(String place) {
        return false;
    }

    @Override
    public abstract String toString();
}
