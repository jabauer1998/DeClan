package io.github.H20man13.DeClan.common.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Return;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.label.ProcLabel;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.icode.section.SymSec;

public class BaseBuilder {
    private List<ICode> intermediateCode;

    public BaseBuilder(){
        this.intermediateCode = new ArrayList<ICode>();
    }

    protected List<ICode> getInstructions(){
        return this.intermediateCode;
    }

    protected ICode getInstruction(int index){
        return intermediateCode.get(index);
    }

    protected void addInstruction(int index, ICode instr){
        intermediateCode.add(index, instr);
    }

    protected void addInstruction(ICode instr){
        intermediateCode.add(instr);
    }
    
    public String symbolTableToString() {
    	StringBuilder sb = new StringBuilder();
    	for(int i = beginningOfSymbolTable(); i <= endOfSymbolTable(); i++) {
    		ICode instruction = getInstruction(i);
    		sb.append(instruction.toString());
    		sb.append("\r\n");
    	}
    	return sb.toString();
    }
    
    public String dataSectionToString() {
    	StringBuilder sb = new StringBuilder();
    	for(int i = beginningOfDataSection(); i <= endOfDataSection(); i++) {
    		ICode instruction = getInstruction(i);
    		sb.append(instruction.toString());
    		sb.append("\r\n");
    	}
    	return sb.toString();
    }
    
    public String codeSectionToString() {
    	StringBuilder sb = new StringBuilder();
    	for(int i = beginningOfCodeSection(); i <= endOfCodeSection(); i++) {
    		ICode instruction = getInstruction(i);
    		sb.append(instruction.toString());
    		sb.append("\r\n");
    	}
    	return sb.toString();
    }
    
    public String procedureSectionToString() {
    	StringBuilder sb = new StringBuilder();
    	for(int i = beginningOfProcedureSection(); i < endingOfProcedureSection(); i++){
    		ICode instruction = getInstruction(i);
    		sb.append(instruction.toString());
    		sb.append("\r\n");
    	}
    	return sb.toString();
    }
    

    public int endOfSymbolTable(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof DataSec)
                return index - 1;
            index++;
        }
        return -1;
    }

    public int beginningOfSymbolTable(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof SymSec)
                return index + 1;
            index++;
        }
        return -1;
    }

    public int beginningOfDataSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof DataSec)
                return index + 1;
            index++;
        }
        return -1;
    }

    public int endOfDataSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof BssSec)
                return index - 1;
            index++;
        }
        return -1;
    }

    public int beginningOfBssSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof BssSec)
                return index + 1;
            index++;
        }
        return -1;
    }

    public int endOfBssSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof CodeSec)
                return index - 1;
            else if(icode instanceof ProcSec)
                return index - 1;
            index++;
        }
        return -1;
    }

    public int beginningOfCodeSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof CodeSec)
                return index + 1;
            index++;
        }
        return -1;
    }

    public int endOfCodeSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof ProcSec)
                return index - 1;
            index++;
        }
        return -1;
    }

    public int beginningOfProcedureSection(){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode instanceof ProcSec)
                return index + 1;
            index++;
        }
        return -1;
    }

    public int endingOfProcedureSection(){
        return intermediateCode.size() + 1;
    }

    public int beginningOfFunction(String funcName){
        int index = 0;
        for(ICode icode: intermediateCode){
            if(icode.containsLabel(funcName))
                return index + 1;
            index++;
        }
        return -1;
    }

    public int endOfFunction(String funcName){
        int begin = beginningOfFunction(funcName);
        for(int i = begin; i < intermediateCode.size(); i++){
            ICode getICode = intermediateCode.get(i);
            if(getICode instanceof Return)
                return i - 1;
        }
        return -1;
    }

    public int endOfParamAssign(String funcName){
        int beginningOfFunction = beginningOfFunction(funcName);
        int endOfFunction = endOfFunction(funcName);
        for(int i = beginningOfFunction; i < endOfFunction; i++){
            ICode instr = getInstruction(i);
            if(instr instanceof Def){
                Def defInstr = (Def)instr;
                if(defInstr.scope != Scope.PARAM)
                    return i - 1;
            } else {
                return i - 1;
            }
        }
        return beginningOfFunction + 1;
    }

    public int beginningOfParamAssign(String funcName){
        return beginningOfFunction(funcName) + 1;
    }
}
