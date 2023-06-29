package io.github.H20man13.DeClan.common.arm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ArmCodeGenerator {
    private List<String> dataSection;
    private List<String> instructions;
    private String label;

    public ArmCodeGenerator(){
        this.dataSection = new LinkedList<String>();
        this.instructions = new LinkedList<String>();
        this.label = null;
    }

    public void addVariable(String varName, String variableValue){
        this.dataSection.add(varName + ": " + variableValue);
    }

    public void addVariable(String variableValue){
        this.dataSection.add(variableValue);
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

    public void writeToFile(FileWriter writer) throws IOException{
        writer.append("B begin\n");
        
        for(String dataValue : dataSection){
            writer.append((dataValue + '\n'));
        }

        writer.append("begin: ");

        for(String instruction : instructions){
            writer.append((instruction) + '\n');
        }
    }
}
