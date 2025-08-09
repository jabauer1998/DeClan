package io.github.H20man13.DeClan.common.arm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.iterative.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator.VariableLength;
import io.github.H20man13.DeClan.common.exception.RegisterAllocatorException;
import io.github.H20man13.DeClan.common.icode.ICode;

public class ArmRegisterGenerator {
    private Set<String> registers;
    private Set<String> availableRegisters;
    private Set<Tuple<String, String>> variablesToRegister;
    private Set<Tuple<String, String>> variablesToTempRegisters;
    private LiveVariableAnalysis liveAnal;
    private int spillOffset;

    public ArmRegisterGenerator(LiveVariableAnalysis liveAnal){
        this.registers = new HashSet<String>();
        this.registers.add("R0");
        this.registers.add("R1");
        this.registers.add("R2");
        this.registers.add("R3");
        this.registers.add("R4");
        this.registers.add("R5");
        this.registers.add("R6");
        this.registers.add("R7");
        this.registers.add("R8");
        this.registers.add("R9");
        this.registers.add("R10");
        this.registers.add("R11");
        this.registers.add("R12");
        this.registers.add("R15");
        this.availableRegisters = new HashSet<String>();
        this.availableRegisters.addAll(this.registers);
        this.liveAnal = liveAnal;
        this.variablesToRegister = new HashSet<Tuple<String, String>>();
        this.variablesToTempRegisters = new HashSet<Tuple<String, String>>();
        this.spillOffset = 0;
    }

    private Set<String> getRegs(String var){
        Set<String> vars = new HashSet<String>();
        for(Tuple<String, String> tuple : variablesToRegister){
            if(tuple.source.equals(var)){
                vars.add(tuple.dest);
            }
        }

        for(Tuple<String, String> tuple : variablesToTempRegisters){
            if(tuple.source.equals(var)){
                vars.add(tuple.dest);
            }
        }
        
        return vars;
    }

    private Set<String> removeRegs(String var){
        Set<String> regs = new HashSet<String>();
        Set<Tuple<String, String>> resultSet = new HashSet<Tuple<String, String>>();
        for(Tuple<String, String> sourceDest : variablesToRegister){
            if(sourceDest.source.equals(var)){
                regs.add(sourceDest.dest);
            } else {
                resultSet.add(sourceDest);
            }
        }
        this.variablesToRegister = resultSet;
        return regs;
    }

    private boolean containsReg(String var){
        Set<Tuple<String, String>> allRegs = new HashSet<Tuple<String, String>>();
        allRegs.addAll(variablesToRegister);
        allRegs.addAll(variablesToTempRegisters);

        for(Tuple<String, String> tuple : allRegs){
            if(tuple.source.equals(var)){
                return true;
            }
        }

        return false;
    }

    private void addReg(String var, String reg){
        Tuple<String, String> tup = new Tuple<String, String>(var, reg);
        this.variablesToRegister.add(tup);
    }

    private void addTempReg(String var, String reg){
        Tuple<String, String> tup = new Tuple<String, String>(var, reg);
        this.variablesToTempRegisters.add(tup);
    }

    private Set<String> getVars(){
        Set<String> var = new HashSet<String>();
        for(Tuple<String, String> varsToReg : this.variablesToRegister){
            var.add(varsToReg.source);
        }
        return var;
    }
    
    public void freeNonInputRegs(ICode icode) {
    	Set<String> vars = liveAnal.getInputSet(icode);
    	Set<String> myVars = getVars();
        for(String key : myVars){
            if(!vars.contains(key)){
                Set<String> value = removeRegs(key);
                for(String regVal : value){
                    availableRegisters.add(regVal);
                }
            }
        }
    }

    public String getTempReg(String place, ICode icode) throws Exception{
        if(containsReg(place)){
            Set<String> regs = getRegs(place);
            for(String reg : regs){
                return reg;
            }
            return null;
        } else if(!this.availableRegisters.isEmpty()){
            String firstAvailable = (String)this.availableRegisters.toArray()[0];
            this.availableRegisters.remove(firstAvailable);
            addTempReg(place, firstAvailable);
            return firstAvailable;
        } else {
        	throw new RegisterAllocatorException("getReg", icode, this.variablesToRegister, this.variablesToTempRegisters, "Cant generate temp register for "+ place + " because there are no registers available to choose from");
        }
    }

    public String getReg(String place, ICode icode) throws Exception{
        if(containsReg(place)){
            Set<String> regs = getRegs(place);
            for(String reg : regs){
                return reg;
            }
            return null;
        } else if(!this.availableRegisters.isEmpty()){
            String firstAvailable = (String)this.availableRegisters.toArray()[0];
            this.availableRegisters.remove(firstAvailable);
            addReg(place, firstAvailable);
            return firstAvailable;
        } else {
            throw new RegisterAllocatorException("getReg", icode, this.variablesToRegister, this.variablesToTempRegisters, "Cant generate register for "+ place + " because there are no registers available to choose from");
        }
    }

    public void freeTempRegs(){
        Set<String> regs = new HashSet<String>();
        for(Tuple<String, String> varToReg: variablesToTempRegisters){
            String reg = varToReg.dest;
            regs.add(reg);
        }
        variablesToTempRegisters = new HashSet<Tuple<String, String>>();
        this.availableRegisters.addAll(regs);
    }
}
