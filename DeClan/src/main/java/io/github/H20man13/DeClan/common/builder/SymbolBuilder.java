package io.github.H20man13.DeClan.common.builder;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;

public class SymbolBuilder extends BaseBuilder {
    public SymbolBuilder(){
        super();
    }
    
    public enum SymbolBuilderSearchStrategy{
    	SEARCH_VIA_FUNC_NAME,
    	SEARCH_VIA_IDENT_NAME
    }

    public boolean containsEntry(String identifierOrFunction, int externalOrInternal, SymbolBuilderSearchStrategy strat){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsAllQualities(externalOrInternal))
                	if(strat == SymbolBuilderSearchStrategy.SEARCH_VIA_IDENT_NAME) {
                		if(entry.declanIdent != null)
                			if(entry.declanIdent.equals(identifierOrFunction))
                				return true;
                	} else {
                		if(entry.funcName != null)
                			if(entry.funcName.equals(identifierOrFunction))
                				return true;
                	}
             }
        }

        return false;
    }
        
    public boolean containsEntry(String identifier, String funcName, int filter){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsAllQualities(filter))
                	if(entry.declanIdent != null)
                		if(entry.declanIdent != null)
                			if(entry.declanIdent.equals(identifier))
                				if(entry.funcName != null)
                					if(entry.funcName.equals(funcName))
                						return true;
            }
         }
         return false;
    }
    
    public boolean containsEntry(String funcName, int paramNumber, int filter){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsAllQualities(filter))
                	if(entry.funcName != null)
                		if(entry.funcName.equals(funcName))
                			if(entry.paramNumber == paramNumber)
                				return true;
            }
         }
        return false;
    }
        
        

    public IdentExp getVariablePlace(String identifierOrFuncName, int internalOrExternal, SymbolBuilderSearchStrategy strat){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(strat == SymbolBuilderSearchStrategy.SEARCH_VIA_FUNC_NAME) {
                	if(entry.funcName != null)
                		if(entry.funcName.equals(identifierOrFuncName))
                			if(entry.containsAllQualities(internalOrExternal))
                				if(entry.containsAllQualities(SymEntry.RETURN))
                					return new IdentExp(ICode.Scope.RETURN, entry.icodePlace);
                	
                } else {
                	if(entry.declanIdent != null)
                		if(entry.declanIdent.equals(identifierOrFuncName))
                			if(entry.containsAllQualities(internalOrExternal))
                				if(entry.containsAllQualities(SymEntry.LOCAL)) {
                					return new IdentExp(ICode.Scope.LOCAL, entry.icodePlace);
                				} else if(entry.containsAllQualities(SymEntry.GLOBAL)) {
                					return new IdentExp(ICode.Scope.GLOBAL, entry.icodePlace);
                				} else if(entry.containsAllQualities(SymEntry.PARAM)) {
                					return new IdentExp(ICode.Scope.PARAM, entry.icodePlace);
                				}
                }
            }
        }

        throw new RuntimeException("Coulld not find symbol with identifier ");
    }
    
    public IdentExp getVariablePlace(String funcName, int paramNumber, int internalOrExternal){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.funcName != null)
                	if(entry.funcName.equals(funcName))
                		if(entry.paramNumber == paramNumber)
                			if(entry.containsAllQualities(internalOrExternal))
                				if(entry.containsAllQualities(SymEntry.LOCAL)) {
                					return new IdentExp(ICode.Scope.LOCAL, entry.icodePlace);
                				} else if(entry.containsAllQualities(SymEntry.GLOBAL)) {
                					return new IdentExp(ICode.Scope.GLOBAL, entry.icodePlace);
                				} else if(entry.containsAllQualities(SymEntry.PARAM)) {
                					return new IdentExp(ICode.Scope.PARAM, entry.icodePlace);
                				}
            }
        }

        throw new RuntimeException("Could not find symbol with identifier " + funcName);
    }
    
    public IdentExp getVariablePlace(String identifierName, String funcName, int internalOrExternal){
        int symbolStart = beginningOfSymbolTable();
        int symbolEnd = endOfSymbolTable();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent != null)
                	if(entry.declanIdent.equals(identifierName))
                		if(entry.funcName != null)
                			if(entry.funcName.equals(funcName))
                				if(entry.containsAllQualities(internalOrExternal))
                					if(entry.containsAllQualities(SymEntry.LOCAL))
                						return new IdentExp(ICode.Scope.LOCAL, entry.icodePlace);
                					else if(entry.containsAllQualities(SymEntry.GLOBAL))
                						return new IdentExp(ICode.Scope.GLOBAL, entry.icodePlace);
                					else if(entry.containsAllQualities(SymEntry.PARAM))
                						return new IdentExp(ICode.Scope.PARAM, entry.icodePlace);
            }
        }

        throw new RuntimeException("Could not find symbol with identifier ");
    }
    
    public void addVariableEntry(String name, int mask, String declanName, String functionName, int paramNumber){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new VarSymEntry(name, mask, declanName, functionName, paramNumber));
    }
    
    public void addVariableEntry(String name, int mask, String functionName, int paramNumber){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new VarSymEntry(name, mask, functionName, paramNumber));
    }

    public void addVariableEntry(String name, int mask, String declanName, String funcName){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new VarSymEntry(name, mask, declanName, funcName));
    }
    
    public void addVariableEntry(String name, int mask, String declanNameOrFunctionName, boolean isReturn){
        int end = this.endOfSymbolTable() + 1;
        addInstruction(end, new VarSymEntry(name, mask, declanNameOrFunctionName, isReturn));
    }
}
