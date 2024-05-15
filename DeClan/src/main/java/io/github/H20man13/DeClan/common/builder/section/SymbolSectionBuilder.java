package io.github.H20man13.DeClan.common.builder.section;

import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import io.github.H20man13.DeClan.common.builder.template.CompletableBuilder;
import io.github.H20man13.DeClan.common.builder.template.ResetableBuilder;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;

public class SymbolSectionBuilder implements CompletableBuilder<SymSec>, ResetableBuilder {
    private ErrorLog errorLog;
    private List<SymEntry> symbols;
    
    public SymbolSectionBuilder(ErrorLog errLog){
        this.errorLog = errLog;
        resetBuilder();
    }

    public void addVarSymEntry(int qualityMask, String irPlace, String declanIdent){
        addSymEntry(new VarSymEntry(irPlace, qualityMask, declanIdent));
    }

    public void addReturnSymEntry(int qualityMask, String irPlace, String funcName){
        addSymEntry(new RetSymEntry(irPlace, qualityMask, funcName));
    }

    public void addParamSymEntry(int qualityMask, String irPlace, String funcName, int paramNumber){
        addSymEntry(new ParamSymEntry(irPlace, qualityMask, funcName, paramNumber));
    }

    private void addSymEntry(SymEntry entry){
        this.symbols.add(entry);
    }

    @Override
    public void resetBuilder() {
        this.symbols = new LinkedList<SymEntry>();
    }

    public boolean containsExternalVariable(String name){
        for(SymEntry symbol: symbols){
            if(symbol instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)symbol;
                if(entry.containsQualities(SymEntry.EXTERNAL))
                    if(entry.declanIdent.equals(name))
                        return true;
            }
        }
        return false;
    }

    public String getExternalName(String name){
        for(SymEntry symbol: symbols){
            if(symbol instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)symbol;
                if(entry.containsQualities(SymEntry.EXTERNAL))
                    if(entry.declanIdent.equals(name))
                        return entry.icodePlace;
            }
        }
        return null;
    }

    public String getArgumentName(String funcName, int number){
        for(SymEntry symbol: symbols){
            if(symbol instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)symbol;
                if(entry.containsQualities(SymEntry.EXTERNAL)){
                    if(entry.funcName.equals(funcName)){
                        if(entry.paramNumber == number){
                            return entry.icodePlace;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean containsExternalArgument(String funcName, int argNum){
        for(SymEntry symbol: symbols){
            if(symbol instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)symbol;
                if(entry.containsQualities(SymEntry.EXTERNAL))
                    if(entry.funcName.equals(funcName))
                        if(entry.paramNumber == argNum)
                            return true;
            }
        }
        return false;
    }

    public String getReturnName(String funcName){
        for(SymEntry entry: symbols){
            if(entry instanceof RetSymEntry){
                RetSymEntry symbol = (RetSymEntry)entry;
                if(symbol.containsQualities(SymEntry.EXTERNAL))
                    if(symbol.funcName.equals(funcName))
                        return symbol.icodePlace;
            }
        }
        return null;
    }

    public boolean containsExternalReturn(String funcName){
        for(SymEntry entry: symbols){
            if(entry instanceof RetSymEntry){
                RetSymEntry symbol = (RetSymEntry)entry;
                if(symbol.containsQualities(SymEntry.EXTERNAL))
                    if(symbol.funcName.equals(funcName))
                        return true;
            }
        }
        return false;
    }

    @Override
    public SymSec completeBuild() {
        return new SymSec(symbols);
    }
}
