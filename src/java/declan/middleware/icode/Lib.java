package declan.middleware.icode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import declan.utils.exception.ICodeFormatException;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.label.ProcLabel;
import declan.middleware.icode.section.BssSec;
import declan.middleware.icode.section.CodeSec;
import declan.middleware.icode.section.DataSec;
import declan.middleware.icode.section.ProcSec;
import declan.middleware.icode.section.SymSec;
import declan.middleware.icode.symbols.SymEntry;
import declan.middleware.icode.symbols.VarSymEntry;
import declan.utils.pat.P;
import declan.utils.symboltable.entry.VariableEntry;
import declan.utils.Utils;

public class Lib extends ICode implements Iterable<ICode> {
    protected List<ICode> instructions;

    public Lib(boolean insertHeaders){
        this.instructions = new LinkedList<ICode>();
        if(insertHeaders){
            instructions.add(new SymSec());
            instructions.add(new DataSec());
            instructions.add(new ProcSec());
        }
    }
    
    public enum SymbolSearchStrategy{
    	FIND_VIA_ICODE_LOCATION,
    	FIND_VIA_FUNCTION_NAME,
    	FIND_VIA_IDENTIFIER_NAME
    }
    
    public boolean containsEntry(String identifierOrFunctionOrLocation, int externalOrInternal, SymbolSearchStrategy strategy){
        int symbolStart = beginningOfSymbolSection();
        int symbolEnd = endOfSymbolSection();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsAllQualities(externalOrInternal))
                	if(strategy == SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) {
            				if(entry.declanIdent != null)
            					if(entry.declanIdent.equals(identifierOrFunctionOrLocation))
            						return true;
                	} else if(strategy == SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME) {
            				if(entry.funcName != null)
            					if(entry.funcName.equals(identifierOrFunctionOrLocation))
            						return true;
                	} else {
            			if(entry.icodePlace != null)
            				if(entry.icodePlace.equals(identifierOrFunctionOrLocation))
            					return true;
                	}
             }
        }

        return false;
    }
        
    public boolean containsEntry(String icodePlace, String funcName, int externalOrInternal, SymbolSearchStrategy strat){
        int symbolStart = beginningOfSymbolSection();
        int symbolEnd = endOfSymbolSection();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = this.getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.containsAllQualities(externalOrInternal))
                	if(strat == SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) {
                		if(entry.icodePlace != null)
                    		if(entry.icodePlace.equals(icodePlace))
                    			if(entry.funcName != null)
    	                			if(entry.funcName.equals(funcName))
    	                				return true;
                	} else if(strat == SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) {
                		if(entry.declanIdent != null)
                    		if(entry.declanIdent.equals(icodePlace))
                    			if(entry.funcName != null)
    	                			if(entry.funcName.equals(funcName))
    	                				return true;
                	}
            }
        }
        return false;
    }
    
    public boolean containsEntry(String funcName, int paramNumber, int filter){
        int symbolStart = beginningOfSymbolSection();
        int symbolEnd = endOfSymbolSection();
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
        
        

    public VarSymEntry getVariableData(String identifierOrFuncnameOrLocation, int internalOrExternal, SymbolSearchStrategy strategy){
        int symbolStart = beginningOfSymbolSection();
        int symbolEnd = endOfSymbolSection();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(strategy == SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME) {
                	if(entry.funcName != null)
                		if(entry.funcName.equals(identifierOrFuncnameOrLocation))
                			if(entry.containsAllQualities(internalOrExternal))
                				return entry;
                } else if(strategy == SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) {
                	if(entry.declanIdent != null)
                		if(entry.declanIdent.equals(identifierOrFuncnameOrLocation))
                			if(entry.containsAllQualities(internalOrExternal))
                				return entry;
                } else {
                	if(entry.icodePlace != null)
                		if(entry.icodePlace.equals(identifierOrFuncnameOrLocation))
                			if(entry.containsAllQualities(internalOrExternal))
                				return entry;
                }
            }
        }

        throw new RuntimeException("Coulld not find symbol with identifier ");
    }
    
    public VarSymEntry getVariableData(String funcName, int paramNumber, int internalOrExternal){
        int symbolStart = beginningOfSymbolSection();
        int symbolEnd = endOfSymbolSection();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.funcName != null)
                	if(entry.funcName.equals(funcName))
                		if(entry.paramNumber == paramNumber)
                			if(entry.containsAllQualities(internalOrExternal))
                				return entry;
            }
        }

        throw new RuntimeException("Coulld not find symbol with identifier " + funcName);
    }
    
    public VarSymEntry getVariableData(String identifierName, String funcName, int internalOrExternal, SymbolSearchStrategy strat){
        int symbolStart = beginningOfSymbolSection();
        int symbolEnd = endOfSymbolSection();
        for(int i = symbolStart; i <= symbolEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(strat == SymbolSearchStrategy.FIND_VIA_IDENTIFIER_NAME) {
                	if(entry.declanIdent != null)
                		if(entry.declanIdent.equals(identifierName))
                        	if(entry.funcName != null)
                        		if(entry.funcName.equals(funcName))
                        			if(entry.containsAllQualities(internalOrExternal))
                        				return entry;
                } else if(strat == SymbolSearchStrategy.FIND_VIA_ICODE_LOCATION) {
                	if(entry.icodePlace.equals(identifierName))
                    	if(entry.funcName != null)
                    		if(entry.funcName.equals(funcName))
                    			if(entry.containsAllQualities(internalOrExternal))
                    				return entry;
                }
            }
        }

        throw new RuntimeException("Coulld not find symbol with identifier ");
    }

    public void addProcedureHeader(String procName){
        int end = endOfProcedureSection();
        addInstruction(end + 1, new ProcLabel(procName));
        addInstruction(end + 2, new Return(procName));
    }

    public Lib(List<ICode> instructions){
        this.instructions = instructions;
    }

    public int getSize(){
        return instructions.size();
    }

    public List<ICode> getICode(){
        return instructions;
    }

    public ICode getInstruction(int index){
        return instructions.get(index);
    }

    protected void addInstruction(int index, ICode instruction){
        this.instructions.add(index, instruction);
    }

    public void addInstruction(ICode instruction){
        this.instructions.add(instruction);
    }

    public void addProcedureInstruction(String procName, ICode instruction){
        int endProcedure = endOfProcedure(procName);
        addInstruction(endProcedure, instruction);
    }

    public void addDataInstruction(ICode instruction){
        int dataEnd = endOfDataSection();
        addInstruction(dataEnd + 1, instruction);
    }

    public void addSymEntry(SymEntry entry){
        int entryEnd = this.endOfSymbolSection();
        addInstruction(entryEnd + 1, entry);
    }

    public boolean dataSectionContainsInstruction(ICode paramInstr){
        int begin = beginningOfDataSection();
        int end = endOfDataSection();
        for(int i = begin; i <= end; i++){
            ICode localInstr = getInstruction(i);
            if(paramInstr.equals(localInstr))
                return true;
        }
        return false;
    }

    @Override
    public boolean isConstant() {
        throw new ICodeFormatException(this, "Cant determine if lib is a constant");
    }

    @Override
    public boolean isBranch() {
        throw new ICodeFormatException(this, "Cant check if Lib is a branch");
    }

    @Override
    public P asPattern() {
        throw new ICodeFormatException(this, "Error cant convert Lib to Patterns");
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Lib){
            Lib lib = (Lib)obj;
            List<ICode> instructions = this.instructions;
            if(!(instructions instanceof ArrayList)){
                instructions = new ArrayList<ICode>();
                instructions.addAll(this.instructions);
            }
            List<ICode> otherInstructions = lib.instructions;
            if(!(otherInstructions instanceof ArrayList)){
                otherInstructions = new ArrayList<ICode>();
                otherInstructions.addAll(lib.instructions);    
            }

            for(int i = 0; i < instructions.size(); i++){
                ICode instruction = instructions.get(i);
                ICode otherInstruction = otherInstructions.get(i);

                if(!instruction.equals(otherInstruction))
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }

    private enum State{
        PROCEDURE_SECTION,
        PROCEDURE,
        DATA_SECTION,
        SYMBOL_SECTION,
        INIT
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        State state = State.INIT;
        for(ICode instruction : instructions){
            switch(state){
                case INIT:
                    if(instruction instanceof SymSec){
                        state = State.SYMBOL_SECTION;
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    }
                    break;
                case SYMBOL_SECTION:
                    if(instruction instanceof DataSec){
                        state = State.DATA_SECTION;
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else {
                        sb.append(' ');
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    }
                    break;
                case DATA_SECTION: 
                    if(instruction instanceof ProcSec){
                        state = State.PROCEDURE_SECTION;
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else {
                        sb.append(' ');
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    }
                    break;
                case PROCEDURE_SECTION:
                    if(instruction instanceof ProcLabel){
                        state = State.PROCEDURE;
                        sb.append(' ');
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    }
                    break;
                case PROCEDURE:
                    if(instruction instanceof Return){
                        state = State.PROCEDURE_SECTION;
                        sb.append(' ');
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else {
                        sb.append(Utils.formatStringToLeadingWhiteSpace("  " + instruction.toString()));
                        sb.append("\r\n");
                    }
            }
        }
        return sb.toString();
    }

    public boolean containsPlace(String place){
        for(ICode instruction: instructions){
            if(instruction instanceof BssSec)
                continue;
            if(instruction instanceof DataSec)
                continue;
            if(instruction instanceof SymSec)
                continue;
            if(instruction instanceof CodeSec)
                continue;
            if(instruction instanceof ProcSec)
                continue;               
            if(instruction.containsPlace(place))
                return true;
        }

        return false;
    }

    public boolean containsLabel(String label){
        for(ICode instruction: instructions){
            if(instruction instanceof BssSec)
                continue;
            if(instruction instanceof DataSec)
                continue;
            if(instruction instanceof SymSec)
                continue;
            if(instruction instanceof CodeSec)
                continue;
            if(instruction instanceof ProcSec)
                continue;
            if(instruction.containsLabel(label))
                return true;
        }

        return false;
    }

    @Override
    public void replacePlace(String from, String to) {
        for(ICode instruction: instructions){
            if(instruction instanceof BssSec)
                continue;
            if(instruction instanceof DataSec)
                continue;
            if(instruction instanceof SymSec)
                continue;
            if(instruction instanceof CodeSec)
                continue;
            if(instruction instanceof ProcSec)
                continue;
            instruction.replacePlace(from, to);
        }
    }

    @Override
    public void replaceLabel(String from, String to) {
        for(ICode instruction: instructions){
            if(instruction instanceof BssSec)
                continue;
            if(instruction instanceof DataSec)
                continue;
            if(instruction instanceof SymSec)
                continue;
            if(instruction instanceof CodeSec)
                continue;
            if(instruction instanceof ProcSec)
                continue;
            instruction.replaceLabel(from, to);
        }
    }

    public int beginningOfSymbolSection(){
        for(int i = 0; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof SymSec){
                return i + 1;
            }
        }
        return -1;
    }

    public int endOfSymbolSection(){
        int begin = beginningOfSymbolSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof DataSec){
                return i - 1;
            }
        }

        return -1;
    }

    public int beginningOfDataSection(){
        int begin = endOfSymbolSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof DataSec){
                return i + 1;
            }
        }
        return -1;
    }

    public int endOfDataSection(){
        int begin = beginningOfDataSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof ProcSec){
                return i - 1;
            }
        }

        return -1;
    }

    public int beginningOfProcedureSection(){
        int begin = endOfDataSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof ProcSec){
                return i + 1;
            }
        }
        return -1;
    }

    public boolean containsProcedure(String procName){
        int begin = beginningOfProcedureSection();
        int end = endOfProcedureSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof ProcLabel){
                ProcLabel label = (ProcLabel)instruction;
                if(label.label.equals(procName)){
                    return true;
                }
            }
        }
        return false;
    }


    public int beginningOfProcedure(String procedureName){
        int begin = beginningOfProcedureSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof ProcLabel){
                ProcLabel label = (ProcLabel)instruction;
                if(label.label.equals(procedureName))
                    return i;
            }
        }
        return -1;
    }

    public int endOfProcedure(String procedureName){
        int begin = beginningOfProcedure(procedureName);
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof Return){
                return i;
            }
        }
        return -1;
    }

    public int endOfProcedureSection(){
        return instructions.size() - 1;
    }

    @Override
    public Iterator<ICode> iterator() {
        return this.instructions.iterator();
    }
    
    @Override
    public int hashCode() {
    	return instructions.hashCode();
    }

	@Override
	public ICode copy() {
		return new Lib(instructions);
	}
}
