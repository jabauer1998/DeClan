package io.github.H20man13.DeClan.common.arm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import io.github.H20man13.DeClan.common.util.Utils;

public class ArmCodeGenerator {
    private String label;
    private int totalBytes;
    private boolean first;
    private String instructionFile;
    private String variableFile;
    private String finalFile;
    private FileWriter variableWriter;
    private FileWriter instructionWriter;
    private FileWriter finalOutputWriter;
    private boolean codeWritten;

    public enum VariableLength{
        BYTE,
        WORD
    }

    public ArmCodeGenerator(String fileName) throws IOException{
        this.label = null;
        this.totalBytes = 0;
        this.first = true;
        this.variableFile = fileName + ".var";
        this.variableWriter = new FileWriter(this.variableFile);
        this.instructionFile = fileName + ".instr";
        this.instructionWriter = new FileWriter(this.instructionFile);
        this.finalFile = fileName;
        this.finalOutputWriter = new FileWriter(this.finalFile);
        this.codeWritten = false;
    }

    public void addVariable(String varName, VariableLength length, int variableValue) throws IOException{
        if(!codeWritten){
            if(length == VariableLength.BYTE){
                this.variableWriter.append(varName + ": .BYTE " + variableValue + "\r\n");
                totalBytes += 1;
            } else {
                this.variableWriter.append(varName + ": .WORD " + variableValue + "\r\n");
                totalBytes += 4;
            }
        }
    }

    public void addVariable(String varName, float variableValue) throws IOException{
        if(!codeWritten){
            this.variableWriter.append(varName + ": .WORD " + variableValue + "\r\n");
            totalBytes += 4;
        }
    }

    public void addVariable(VariableLength length, int variableValue) throws IOException{
        if(!codeWritten){
            if(length == VariableLength.BYTE){
                this.variableWriter.append(".BYTE " + variableValue + "\r\n");
                totalBytes += 1;
            } else {
                this.variableWriter.append(".WORD " + variableValue + "\r\n");
                totalBytes += 4;
            }
        }
    }

    public void addVariable(String variableName, VariableLength length) throws IOException{
        if(!codeWritten){
            if(length == VariableLength.BYTE){
                this.variableWriter.append(variableName + ": .BYTE 0\r\n");
                totalBytes += 1;
            } else {
                this.variableWriter.append(variableName + ": .WORD 0\r\n");
                totalBytes += 4;
            }
        }
    }

    public void addInstruction(String instr) throws IOException{
        if(!codeWritten){
            if(this.label == null){
                this.instructionWriter.append(instr + "\r\n");
            } else {
                this.instructionWriter.append(this.label + ": " + instr + "\r\n");
            }
            this.totalBytes += 4;
            this.label = null;
        }
    }

    public void setLabel(String label) throws IOException{
        if(!codeWritten){
            if(this.label != null){
                addInstruction("MOV R0, R0\r\n");
                this.label = null;
            }
            this.label = label;
        }
    }

    public void writeToStream() throws IOException{
        if(first){
            addVariable("totalBytes", VariableLength.WORD, totalBytes+12);
        }
        variableWriter.close();
        instructionWriter.close();

        finalOutputWriter.append("LDR R13, totalBytes\r\n");

        FileReader instructionReader = new FileReader(this.instructionFile);
        Scanner instructionScanner = new Scanner(instructionReader);
        while(instructionScanner.hasNext()){
            String line = instructionScanner.nextLine();
            finalOutputWriter.append(line);
            finalOutputWriter.append("\r\n");
        }
        instructionScanner.close();

        FileReader variableFileReader = new FileReader(this.variableFile);
        Scanner variableScanner = new Scanner(variableFileReader);
        while(variableScanner.hasNext()){
            String line = variableScanner.nextLine();
            finalOutputWriter.append(line);
            finalOutputWriter.append("\r\n");
        }
        variableScanner.close();

        this.finalOutputWriter.close();

        Utils.deleteFile(this.variableFile);
        Utils.deleteFile(this.instructionFile);

        this.codeWritten = true;
    }
}
