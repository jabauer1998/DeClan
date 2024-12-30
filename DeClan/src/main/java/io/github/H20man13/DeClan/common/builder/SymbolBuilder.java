package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;

public class SymbolBuilder extends BaseBuilder {
    
    public SymbolBuilder(){
        super();
    }

    public boolean containsArgument(String funcName, int paramNumber, int externalOrInternal){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();

        if(beginningOfSymbolSection != -1 && endOfSymbolTable != -1){
            for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
                ICode instr = getInstruction(i); 
                if(instr instanceof ParamSymEntry){
                    ParamSymEntry param = (ParamSymEntry)instr;
                    if(param.containsAllQualities(externalOrInternal))
                        if(param.funcName.equals(funcName))
                            if(param.paramNumber == paramNumber)
                                return true;
                }
            }
        }
        return false;
    }
    
    public boolean containsArgument(String funcName, int externalOrInternal){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();

        if(beginningOfSymbolSection != -1 && endOfSymbolTable != -1){
            for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
                ICode instr = getInstruction(i); 
                if(instr instanceof ParamSymEntry){
                    ParamSymEntry param = (ParamSymEntry)instr;
                    if(param.containsAllQualities(externalOrInternal))
                        if(param.funcName.equals(funcName))
                        	return true;
                }
            }
        }
        return false;
    }

    public boolean containsReturn(String funcName, int externalOrInternal){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();

        if(beginningOfSymbolSection != -1 && endOfSymbolTable != -1){
            for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
                ICode instr = getInstruction(i); 
                if(instr instanceof RetSymEntry){
                    RetSymEntry ret = (RetSymEntry)instr;
                    if(ret.containsAllQualities(externalOrInternal))
                        if(ret.funcName.equals(funcName))
                            return true;
                }
            }
        }
        return false;
    }

    public boolean containsVariable(String identifier, int externalOrInternal){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsAllQualities(externalOrInternal))
                    if(entry.declanIdent.equals(identifier))
                        return true;
            }
        }

        return false;
    }

    public IdentExp getVariablePlace(String ident, int internalOrExternal){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsAllQualities(internalOrExternal))
                    	if(entry.containsAllQualities(SymEntry.LOCAL)) {
                    		return new IdentExp(ICode.Scope.LOCAL, entry.icodePlace);
                    	} else if(entry.containsAllQualities(SymEntry.GLOBAL)) {
                    		return new IdentExp(ICode.Scope.GLOBAL, entry.icodePlace);
                    	} else if(entry.containsAllQualities(SymEntry.PARAM)) {
                    		return new IdentExp(ICode.Scope.PARAM, entry.icodePlace);
                    	} else {
                    		throw new RuntimeException("Error unexpected quality type for paramater");
                    	}
            }
        }

        throw new RuntimeException("Coulld not find symbol with identifier " + ident);
    }

    public IdentExp getArgumentPlace(String funcName, int paramNumber, int internalExternal){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();
        
        for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
            ICode instr = getInstruction(i); 
            if(instr instanceof ParamSymEntry){
                ParamSymEntry param = (ParamSymEntry)instr;
                if(param.containsAllQualities(internalExternal))
                    if(param.funcName.equals(funcName))
                        if(param.paramNumber == paramNumber)
                            return new IdentExp(ICode.Scope.PARAM, param.icodePlace);
            }
        }

        throw new RuntimeException("No paramater found with funcName " + funcName + " and paramNumber=" + paramNumber);
    }

    public IdentExp getReturnPlace(String funcName, int internalExternal){
        int beginningOfSymbolSection = this.beginningOfSymbolTable();
        int endOfSymbolTable = this.endOfSymbolTable();
        
        for(int i = beginningOfSymbolSection; i <= endOfSymbolTable; i++){
            ICode instr = getInstruction(i); 
            if(instr instanceof RetSymEntry){
                RetSymEntry ret = (RetSymEntry)instr;
                if(ret.containsAllQualities(internalExternal))
                    if(ret.funcName.equals(funcName))
                        return new IdentExp(ICode.Scope.RETURN, ret.icodePlace);
            }
        }

        throw new RuntimeException("No return found with funcName " + funcName);
    }

    public void addVariableEntry(String name, int mask, String declanName, String funcName){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new VarSymEntry(name, mask, declanName, funcName));
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
