package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;

public class SymbolBuilder extends BaseBuilder {
    
    public SymbolBuilder(){
        super();
    }

    public boolean containsExternalArgument(String funcName, int paramNumber){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();

        if(beginningOfSymbolSection != -1 && endOfSymbolTable != -1){
            for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
                ICode instr = getInstruction(i); 
                if(instr instanceof ParamSymEntry){
                    ParamSymEntry param = (ParamSymEntry)instr;
                    if(param.containsQualities(SymEntry.EXTERNAL))
                        if(param.funcName.equals(funcName))
                            if(param.paramNumber == paramNumber)
                                return true;
                }
            }
        }
        return false;
    }

    public boolean containsExternalReturn(String funcName){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();

        if(beginningOfSymbolSection != -1 && endOfSymbolTable != -1){
            for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
                ICode instr = getInstruction(i); 
                if(instr instanceof RetSymEntry){
                    RetSymEntry ret = (RetSymEntry)instr;
                    if(ret.containsQualities(SymEntry.EXTERNAL))
                        if(ret.funcName.equals(funcName))
                            return true;
                }
            }
        }
        return false;
    }

    public boolean containsExternalVariable(String identifier){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsQualities(SymEntry.EXTERNAL))
                    if(entry.declanIdent.equals(identifier))
                        return true;
            }
        }

        return false;
    }

    public String getVariablePlace(String ident){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsQualities(SymEntry.EXTERNAL))
                        return entry.icodePlace;
            }
        }

        throw new RuntimeException("Coulld not find symbol with identifier " + ident);
    }

    public String getArgumentPlace(String funcName, int paramNumber){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();
        
        for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
            ICode instr = getInstruction(i); 
            if(instr instanceof ParamSymEntry){
                ParamSymEntry param = (ParamSymEntry)instr;
                if(param.containsQualities(SymEntry.EXTERNAL))
                    if(param.funcName.equals(funcName))
                        if(param.paramNumber == paramNumber)
                            return param.icodePlace;
            }
        }

        throw new RuntimeException("No paramater found with funcName " + funcName + " and paramNumber=" + paramNumber);
    }

    public String getReturnPlace(String funcName){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();
        
        for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
            ICode instr = getInstruction(i); 
            if(instr instanceof RetSymEntry){
                RetSymEntry ret = (RetSymEntry)instr;
                if(ret.containsQualities(SymEntry.EXTERNAL))
                    if(ret.funcName.equals(funcName))
                        return ret.icodePlace;
            }
        }

        throw new RuntimeException("No return found with funcName " + funcName);
    }

    public void addVariableEntry(String name, int mask, String declanName){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new VarSymEntry(name, mask, declanName));
    }

    public void addParamEntry(String name, int mask, String funcName, int paramNumber){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new ParamSymEntry(name, mask, funcName, paramNumber));
    }

    public void addReturnEntry(String name, int mask, String funcName){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new RetSymEntry(name, mask, funcName));
    }
}
