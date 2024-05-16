package io.github.H20man13.DeClan.common.icode.section;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import io.github.H20man13.DeClan.common.exception.NoSymbolFoundException;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.Environment;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;

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

    public VarSymEntry getVariableEntryByIdentifier(String ident, int mask){
        for(SymEntry entry: this.entries){
            if(entry instanceof VarSymEntry){
                VarSymEntry newEntry = (VarSymEntry)entry;
                if(newEntry.declanIdent.equals(ident) && newEntry.containsQualities(mask))
                    return  newEntry;
            }
        }
        throw new NoSymbolFoundException();
    }

    public VarSymEntry getVariableEntryByICodePlace(String place, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof VarSymEntry){
                VarSymEntry varEntry = (VarSymEntry)entry;
                if(entry.icodePlace.equals(place))
                    if(entry.containsQualities(mask))
                        return varEntry;
            }
        }
        throw new NoSymbolFoundException();
    }

    public ParamSymEntry getParamaterByFunctionNameAndNumber(String functionName, int paramNumber, int mask){
        for(SymEntry entry: entries){
            if(entry instanceof ParamSymEntry){
                ParamSymEntry paramEntry = (ParamSymEntry)entry;
                if(paramEntry.funcName.equals(functionName))
                    if(paramEntry.paramNumber == paramNumber)
                        if(entry.containsQualities(mask))
                            return paramEntry;
            }
        }
        throw new NoSymbolFoundException();
    }

    public ParamSymEntry getParamaterByICodePlace(String place, int mask){
        for(SymEntry entry: entries){
            if(entry instanceof ParamSymEntry){
                ParamSymEntry symEntry = (ParamSymEntry)entry;
                if(symEntry.icodePlace.equals(place))
                    if(entry.containsQualities(mask))
                        return symEntry;
            }
        }
        throw new NoSymbolFoundException();
    }

    public RetSymEntry getReturnByICodePlace(String place, int mask){
        for(SymEntry entry: entries){
            if(entry instanceof RetSymEntry){
                RetSymEntry retEntry = (RetSymEntry)entry;
                if(retEntry.icodePlace.equals(place))
                    if(retEntry.containsQualities(mask))
                        return retEntry;
            }
        }
        throw new NoSymbolFoundException();
    }

    public RetSymEntry getReturnByFunctionName(String funcName, int mask){
        for(SymEntry entry: entries){
            RetSymEntry myEntry = (RetSymEntry)entry;
            if(myEntry.funcName.equals(funcName))
                if(myEntry.containsQualities(mask))
                    return myEntry;
        }
        throw new NoSymbolFoundException();
    }



    public SymEntry getEntryByIndex(int index){
        return entries.get(index);
    }

    public int getLength(){
        return entries.size();
    }

    public boolean containsParamaterEntryWithICodePlace(String place, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof ParamSymEntry){
                if(entry.icodePlace.equals(place))
                    if(entry.containsQualities(mask))
                        return true;
            }
        }
        return false;
    }

    public boolean containsParamaterEntryWithFunctionNameAndParamaterNumber(String funcName, int paramNumber, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof ParamSymEntry){
                ParamSymEntry myEntry = (ParamSymEntry)entry;
                if(myEntry.funcName.equals(funcName))
                    if(myEntry.paramNumber == paramNumber)
                        if(myEntry.containsQualities(mask))
                            return true;
            }
        }
        return false;
    }

    public boolean containsReturnEntryWithICodePlace(String place, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof RetSymEntry){
                if(entry.icodePlace.equals(place))
                    if(entry.containsQualities(mask))
                        return true;
            }
        }
        return false;
    }

    public boolean containsReturnEntryWithFunctionName(String functionName, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof RetSymEntry){
                RetSymEntry myEntry = (RetSymEntry)entry;
                if(myEntry.funcName.equals(functionName))
                    if(myEntry.containsQualities(mask))
                        return true;
            }
        }
        return false;
    }

    public boolean containsVariableEntryWithICodePlace(String place, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof VarSymEntry){
                if(entry.icodePlace.equals(place))
                    if(entry.containsQualities(mask))
                        return true;
            }
        }
        return false;
    }

    public boolean containsVariableEntryWithIdentifier(String ident, int mask){
        for(SymEntry entry : entries){
            if(entry instanceof VarSymEntry){
                VarSymEntry myEntry = (VarSymEntry)entry;
                if(myEntry.declanIdent.equals(ident))
                    if(myEntry.containsQualities(mask))
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

    @Override
    public boolean containsParamater(String place) {
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
    public boolean containsArgument(String place) {
        return false;
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
