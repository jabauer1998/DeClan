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
    private int totalBytes;
    private boolean first;

    public enum VariableLength{
        BYTE,
        WORD
    }

    public ArmCodeGenerator(){
        this.dataSection = new LinkedList<String>();
        this.instructions = new LinkedList<String>();
        this.label = null;
        this.totalBytes = 0;
        this.first = true;
    }

    public void addVariable(String varName, VariableLength length, int variableValue){
        if(length == VariableLength.BYTE){
            this.dataSection.add(varName + ": .BYTE " + variableValue);
            totalBytes += 1;
        } else {
            this.dataSection.add(varName + ": .WORD " + variableValue);
            totalBytes += 4;
        }
    }

    public void addVariable(String varName, double variableValue){
        this.dataSection.add(varName + ": .WORD " + variableValue);
        totalBytes += 4;
    }

    public void addVariable(VariableLength length, int variableValue){
        if(length == VariableLength.BYTE){
            this.dataSection.add(".BYTE " + variableValue);
            totalBytes += 1;
        } else {
            this.dataSection.add(".WORD " + variableValue);
            totalBytes += 4;
        }
    }

    public void addVariable(String variableName, VariableLength length){
        if(length == VariableLength.BYTE){
            this.dataSection.add(variableName + ": .BYTE 0");
            totalBytes += 1;
        } else {
            this.dataSection.add(variableName + ": .WORD 0");
            totalBytes += 4;
        }
    }

    public void addInstruction(String instr){
        if(this.label == null){
            this.instructions.add(instr);
        } else {
            this.instructions.add(this.label + ": " + instr);
        }
        this.totalBytes += 4;
        this.label = null;
    }

    public void setLabel(String label){
        if(this.label != null){
            addInstruction("ADD R0, R0, #0");
            this.label = null;
        }
        this.label = label;
    }

    public void writeToStream(Writer writer) throws IOException{
        if(first){
            addVariable("totalBytes", VariableLength.WORD, totalBytes+12);
        }

        writer.append("LDR R13, totalBytes\r\n");
        writer.append("B begin_0\r\n");
        for(String dataValue : dataSection){
            writer.append(dataValue + "\r\n");
        }
        for(String instruction : instructions){
            writer.append(instruction + "\r\n");
        }
    }
}
