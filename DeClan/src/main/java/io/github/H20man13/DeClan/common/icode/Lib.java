package io.github.H20man13.DeClan.common.icode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.H20man13.DeClan.common.exception.ICodeFormatException;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.icode.symbols.ParamSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.RetSymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.icode.symbols.VarSymEntry;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.symboltable.entry.VariableEntry;
import io.github.H20man13.DeClan.common.util.Utils;

public class Lib implements ICode, Iterable<ICode> {
    protected List<ICode> instructions;

    public Lib(boolean insertHeaders){
        this.instructions = new LinkedList<ICode>();
        if(insertHeaders){
            instructions.add(new SymSec());
            instructions.add(new DataSec());
            instructions.add(new ProcSec());
        }
    }

    public void addProcedureHeader(String procName){
        int end = endOfProcedureSection();
        addInstruction(end + 1, new ProcLabel(procName));
        addInstruction(end + 2, new Return());
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

    public boolean containsVariableEntryWithICodePlace(String ident, int mask){
        int symbolsBegin = this.beginningOfSymbolSection();
        int symbolsEnd = this.endOfSymbolSection();
        for(int i = symbolsBegin; i <= symbolsEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.icodePlace.equals(ident))
                    if(entry.containsAllQualities(mask))
                        return true;
            }
        }
        return false;
    }

    public boolean containsVariableEntryWithIdentifier(String ident, int mask){
        int symbolsBegin = this.beginningOfSymbolSection();
        int symbolsEnd = this.endOfSymbolSection();
        for(int i = symbolsBegin; i <= symbolsEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsAllQualities(mask))
                        return true;
            }
        }
        return false;
    }

    public VarSymEntry getVariableEntryByICodePlace(String ident, int mask){
        int symbolsBegin = this.beginningOfSymbolSection();
        int symbolsEnd = this.endOfSymbolSection();
        for(int i = symbolsBegin; i <= symbolsEnd; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.icodePlace.equals(ident))
                    if(entry.containsAllQualities(mask))
                        return entry;
            }
        }
        throw new ICodeFormatException(this, "Cant find a Variable entry in the symbol table");
    }

    public boolean containsExternalReturnByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return true;
            }
        }

        return false;
    }

    public RetSymEntry getExternalReturnByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No External Return Entry found in symbol table");
    }

    public boolean containsInternalReturnByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return true;
            }
        }

        return false;
    }

    public RetSymEntry getInternalReturnByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No External Return Entry found in symbol table");
    }

    public boolean containsExternalVariableByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return true;
            }
        }

        return false;
    }

    public VarSymEntry getExternalVariableByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No External Variable Entry found in symbol table");
    }

    public boolean containsInternalVariableByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return true;
            }
        }

        return false;
    }

    public VarSymEntry getInternalVariableByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No External Variable Entry found in symbol table");
    }

    public boolean containsExternalParamaterByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return true;
            }
        }

        return false;
    }

    public ParamSymEntry getExternalParamaterByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No External Paramater Entry found in symbol table");
    }

    public boolean containsInternalParamaterByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return true;
            }
        }

        return false;
    }

    public ParamSymEntry getInternalParamaterByPlace(String place){
        int beginningOfSymbolSection = beginningOfSymbolSection();
        int endingOfSymbolSection = endOfSymbolSection();

        for(int i = beginningOfSymbolSection; i <= endingOfSymbolSection; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.icodePlace.equals(place))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No External Paramater Entry found in symbol table");
    }

    public boolean containsExternalReturnByFunctionName(String funcName){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.funcName.equals(funcName))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return true;
            }
        }
        return false;
    }

    public RetSymEntry getExternalReturnByFunctionName(String funcName){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.funcName.equals(funcName))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No return entry with funcName " + funcName + " found");
    }

    public boolean containsInternalReturnByFunctionName(String funcName){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.funcName.equals(funcName))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return true;
            }
        }
        return false;
    }

    public RetSymEntry getInternalReturnByFunctionName(String funcName){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof RetSymEntry){
                RetSymEntry entry = (RetSymEntry)instruction;
                if(entry.funcName.equals(funcName))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "No return entry with funcName " + funcName + " found");
    }

    public boolean containsExternalParamaterByFunctionNameAndNumber(String funcName, int number){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.paramNumber == number)
                    if(entry.funcName.equals(funcName))
                        if(entry.containsAllQualities(SymEntry.EXTERNAL))
                            return true;
            }
        }
        return false;
    }

    public ParamSymEntry getExternalParamaterByFunctionNameAndNumber(String funcName, int number){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.paramNumber == number)
                    if(entry.funcName.equals(funcName))
                        if(entry.containsAllQualities(SymEntry.EXTERNAL))
                            return entry;
            }
        }

        throw new ICodeFormatException(this, "No param entry with func" + funcName + "(#" + number + ')');
    }

    public boolean containsInternalParamaterByFunctionNameAndNumber(String funcName, int number){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.paramNumber == number)
                    if(entry.funcName.equals(funcName))
                        if(entry.containsAllQualities(SymEntry.INTERNAL))
                            return true;
            }
        }
        return false;
    }

    public ParamSymEntry getInternalParamaterByFunctionNameAndNumber(String funcName, int number){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();
        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof ParamSymEntry){
                ParamSymEntry entry = (ParamSymEntry)instruction;
                if(entry.paramNumber == number)
                    if(entry.funcName.equals(funcName))
                        if(entry.containsAllQualities(SymEntry.INTERNAL))
                            return entry;
            }
        }

        throw new ICodeFormatException(this, "No param entry with funcName " + funcName + "(#" + number + ')');
    }

    public boolean containsExternalVariableByIdent(String ident){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();

        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return true;
            }
        }

        return false;
    }

    public VarSymEntry getExternalVariableByIdent(String ident){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();

        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsAllQualities(SymEntry.EXTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "External Variable entry does not exist with name " + ident);
    }

    public boolean containsInternalVariableByIdent(String ident){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();

        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return true;
            }
        }

        return false;
    }

    public VarSymEntry getInternalVariableByIdent(String ident){
        int begin = beginningOfSymbolSection();
        int end = endOfSymbolSection();

        for(int i = begin; i <= end; i++){
            ICode instruction = getInstruction(i);
            if(instruction instanceof VarSymEntry){
                VarSymEntry entry = (VarSymEntry)instruction;
                if(entry.declanIdent.equals(ident))
                    if(entry.containsAllQualities(SymEntry.INTERNAL))
                        return entry;
            }
        }

        throw new ICodeFormatException(this, "External Variable entry does not exist with name " + ident);
    }

    public boolean placeDefinedInProcedure(String procedure, String place){
        if(containsProcedure(procedure)){
            int begin = beginningOfProcedure(procedure);
            int end = endOfProcedure(procedure);
            for(int i = begin; i <= end; i++){
                ICode instruction = getInstruction(i);
                if(instruction instanceof Def){
                    Def def = (Def)instruction;
                    if(def.label.equals(place)){
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
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
}
