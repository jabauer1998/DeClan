package io.github.H20man13.DeClan.common.icode.section;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.SymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;

public class SymSec implements ICode {
    public List<SymEntry> entries;

    public SymSec(List<SymEntry> entries){
        this.entries = entries;
    }

    public SymSec(){
        this(new LinkedList<SymEntry>());
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isBranch() {
        return false;
    }

    public void addEntry(SymEntry entry){
        entries.add(entry);
    }

    public SymEntry getEntryByICodePlace(String place, int mask){
        for(SymEntry entry : entries){
            if(entry.icodePlace.equals(place) && entry.containsQualities(mask)){
                return entry;
            }
        }
        return null;
    }

    public SymEntry getEntryByIndex(int index){
        return entries.get(index);
    }

    public int getLength(){
        return entries.size();
    }

    public List<SymEntry> getEntriesByIdentifier(String ident, int mask){
        LinkedList<SymEntry> entries = new LinkedList<SymEntry>();
        for(SymEntry entry: this.entries){
            if(entry.declanIdent.equals(ident) && entry.containsQualities(mask)){
                entries.add(entry);
            }
        }
        return entries;
    }

    public SymEntry removeEntryWithICodePlace(String place, int mask){
        int toRemove = -1;
        for(int i = 0; i < entries.size(); i++){
            SymEntry entry = entries.get(i);
            if(entry.icodePlace.equals(place) && entry.containsQualities(mask)){
                toRemove = i;
                break;
            }
        }

        if(toRemove != -1){
            return entries.remove(toRemove);
        } else {
            return null;
        }
    }

    public void removeEntriesWithIdentifier(final String ident, final int mask){
        entries.removeIf(new Predicate<SymEntry>() {
            @Override
            public boolean test(SymEntry entry) {
                return entry.declanIdent.equals(ident) && entry.containsQualities(mask);
            }
        });
    }

    public boolean containsEntryWithICodePlace(String place, int mask){
        for(SymEntry entry : entries){
            if(entry.icodePlace.equals(place) && entry.containsQualities(mask)){
                return true;
            }
        }
        return false;
    }

    public boolean containsEntryWithIdentifier(String ident, int mask){
        for(SymEntry entry : entries){
            if(entry.declanIdent.equals(ident) && entry.containsQualities(mask)){
                return true;
            }
        } 
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("SYMBOL SECTION\r\n");
        for(SymEntry entry : entries){
            sb.append(' ');
            sb.append(entry.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public P asPattern() {
        int size = entries.size();
        P[] patterns = new P[size];
        for(int i = 0; i < size; i++){
            patterns[i] = entries.get(i).asPattern();
        }
        return P.PAT(patterns);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof SymSec){
            SymSec symbols = (SymSec)obj;

            if(getLength() != symbols.getLength())
                return false;

            for(int i = 0; i < getLength(); i++){
                SymEntry expected = symbols.getEntryByIndex(i);
                SymEntry actual = getEntryByIndex(i);
                if(!expected.equals(actual))
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<ICode> genFlatCode() {
        LinkedList<ICode> resultList = new LinkedList<ICode>();
        resultList.add(this);
        return resultList;
    }

    @Override
    public boolean containsPlace(String place) {
        for(SymEntry entry: entries){
            if(entry.containsPlace(place))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsLabel(String label) {
        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        for(SymEntry entry: entries){
            entry.replacePlace(from, to);
        }
    }

    @Override
    public void replaceLabel(String from, String to) {
        //Do nothing
    }
}
