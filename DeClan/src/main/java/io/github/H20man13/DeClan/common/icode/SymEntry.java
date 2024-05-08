package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.pat.P;

public class SymEntry implements ICode, Copyable<SymEntry> {
    private int symType;
    public String icodePlace;
    public String declanIdent;

    public SymEntry(int symType, String icodePlace, String declanIdent){
        this.symType = symType;
        this.icodePlace = icodePlace;
        this.declanIdent = declanIdent;
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
    public P asPattern() {
        if(containsQualities(CONST | INTERNAL)) return P.PAT(P.ID(), P.CONST(), P.INTERNAL(), P.ID());
        else if(containsQualities(CONST | EXTERNAL)) return P.PAT(P.ID(), P.CONST(), P.EXTERNAL(), P.ID());
        else if(containsQualities(INTERNAL)) return P.PAT(P.ID(), P.INTERNAL(), P.ID());
        else if(containsQualities(EXTERNAL)) return P.PAT(P.ID(), P.EXTERNAL(), P.ID());
        else {
            throw new RuntimeException("Error Unknown pattern type found in P.asPattern");
        }
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
            
            boolean identEquals = entry.declanIdent.equals(declanIdent);
            boolean maskEquals = entry.symType == symType;
            boolean placeEquals = entry.icodePlace.equals(icodePlace);

            return identEquals && maskEquals && placeEquals;
        } else {
            return false;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(icodePlace);
        sb.append(' ');
        
        if(containsQualities(CONST | INTERNAL)) sb.append("CONST INTERNAL");
        else if(containsQualities(CONST | EXTERNAL)) sb.append("CONST EXTERNAL");
        else if(containsQualities(INTERNAL)) sb.append("INTERNAL");
        else if(containsQualities(EXTERNAL)) sb.append("EXTERNAL");

        sb.append(' ');
        sb.append(declanIdent);
        return sb.toString();
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
    public SymEntry copy(){
        return new SymEntry(symType, icodePlace, declanIdent);
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
}
