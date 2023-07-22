package io.github.H20man13.DeClan.common.arm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

public class ArmCodeGenerator {
    private List<String> dataSection;
    private List<String> instructions;
    private String label;

    public enum VariableLength{
        BYTE,
        WORD
    }

    public ArmCodeGenerator(){
        this.dataSection = new LinkedList<String>();
        this.instructions = new LinkedList<String>();
        this.label = null;
    }

    public void addVariable(String varName, VariableLength length, int variableValue){
        if(length == VariableLength.BYTE){
            this.dataSection.add(varName + ": .BYTE " + variableValue);
        } else {
            this.dataSection.add(varName + ": .WORD " + variableValue);
        }
    }

    public void addVariable(VariableLength length, int variableValue){
        if(length == VariableLength.BYTE){
            this.dataSection.add(".BYTE " + variableValue);
        } else {
            this.dataSection.add(".WORD " + variableValue);
        }
    }

    public void addVariable(String variableName, VariableLength length){
        if(length == VariableLength.BYTE){
            this.dataSection.add(variableName + ": .BYTE 0");
        } else {
            this.dataSection.add(variableName + ": .WORD 0");
        }
    }

    public void addInstruction(String instr){
        if(this.label == null){
            this.instructions.add(instr);
        } else {
            this.instructions.add(this.label + ": " + instr);
        }
        this.label = null;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void writeToStream(Writer writer) throws IOException{
        for(String dataValue : dataSection){
            writer.append(dataValue + '\n');
        }

        for(String instruction : instructions){
            writer.append(instruction + '\n');
        }
    }
}
