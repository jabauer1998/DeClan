package io.github.H20man13.DeClan.common.icode;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.exception.ICodeFormatException;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;
import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.util.Utils;

public class Prog extends Lib {
    public Prog(boolean insertHeaders){
        super(false);
        if(insertHeaders){
            instructions.add(new SymSec());
            instructions.add(new DataSec());
            instructions.add(new BssSec());
            instructions.add(new CodeSec());
            instructions.add(new ProcSec());
        }
    }

    public Prog(List<ICode> instructions){
        super(instructions);
    }

    @Override
    public boolean isConstant() {
        throw new ICodeFormatException(this, "Cant determine if Prog is a constant");
    }

    @Override
    public boolean isBranch() {
        throw new ICodeFormatException(this, "Cant determine if Prog is a branch");
    }

    @Override
    public P asPattern() {
        throw new ICodeFormatException(this, "Cant generate a pattern of a Prog");
    }

    public void addBssInstruction(ICode instr){
        int endBss = endOfBssSection();
        addInstruction(endBss + 1, instr);
    }

    public void addProgramInstruction(ICode instr){
        int endCode = endOfCodeSection();
        addInstruction(endCode + 1, instr);
    }


    private enum State{
        PROCEDURE_SECTION,
        PROCEDURE,
        DATA_SECTION,
        CODE_SECTION,
        BSS_SECTION,
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
                    if(instruction instanceof BssSec){
                        state = State.BSS_SECTION;
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else {
                        sb.append(' ');
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    }
                    break;
                case BSS_SECTION:
                    if(instruction instanceof CodeSec){
                        state = State.CODE_SECTION;
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else {
                        sb.append(' ');
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    }
                    break;
                case CODE_SECTION:
                    if(instruction instanceof ProcSec){
                        state = State.PROCEDURE_SECTION;
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else if(instruction instanceof End){
                        sb.append(instruction.toString());
                        sb.append("\r\n");
                    } else {
                        sb.append(Utils.formatStringToLeadingWhiteSpace(" " + instruction.toString()));
                        sb.append("\r\n");
                    }
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
    
    public List<ICode> getExecutableCode(){
    	LinkedList<ICode> toRet = new LinkedList<ICode>();
    	
    	int beginningOfData = this.beginningOfDataSection();
    	int endOfData = this.endOfDataSection();
    	for(int i = beginningOfData; i <= endOfData; i++) {
    		toRet.add(getInstruction(i));
    	}
    	int beginningOfBss = this.beginningOfBssSection();
    	int endOfBss = this.endOfBssSection();
    	for(int i = beginningOfBss; i <= endOfBss; i++) {
    		toRet.add(getInstruction(i));
    	}
    	int beginningOfCode = this.beginningOfCodeSection();
    	int endOfCode = this.endOfCodeSection();
    	for(int i = beginningOfCode; i <= endOfCode; i++) {
    		toRet.add(getInstruction(i));
    	}
    	int beginningOfProcedures = this.beginningOfProcedureSection();
    	int endOfProcedures = this.endOfProcedureSection();
    	for(int i = beginningOfProcedures; i <= endOfProcedures; i++) {
    		toRet.add(getInstruction(i));
    	}
    	
    	return toRet;
    }

    @Override
    public int endOfDataSection(){
        int begin = beginningOfDataSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof BssSec){
                return i - 1;
            }
        }
        return -1;
    }

    public int beginningOfBssSection(){
        int begin = endOfDataSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof BssSec){
                return i + 1;
            }
        }
        return -1;
    }

    public int endOfBssSection(){
        int begin = beginningOfBssSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof CodeSec){
                return i - 1;
            }
        }
        return -1;
    }

    public int beginningOfCodeSection(){
        int begin = endOfBssSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof CodeSec){
                return i + 1;
            }
        }
        return -1;
    }

    public int endOfCodeSection(){
        int begin = beginningOfCodeSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof End){
                return i;
            } else if(instruction instanceof ProcSec){
                return i - 1;
            }
        }
        return -1;
    }

    @Override
    public int beginningOfProcedureSection(){
        int begin = endOfCodeSection();
        for(int i = begin; i < instructions.size(); i++){
            ICode instruction = instructions.get(i);
            if(instruction instanceof ProcSec){
                return i + 1;
            }
        }
        return -1;
    }
    
    @Override
    public int hashCode() {
    	return instructions.hashCode();
    }
    
    public ICode copy() {
    	return new Prog(instructions);
    }
}
