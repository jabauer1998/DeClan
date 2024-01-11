package io.github.H20man13.DeClan.common;

import edu.depauw.declan.common.Position;

public class FilePosition extends Position {
    private String fileName;

    public FilePosition(String fileName, int line, int column) {
        super(line, column);
        this.fileName = fileName;
    }

    public FilePosition(String fileName, Position pos){
        super(pos);
        this.fileName = fileName;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append("File: ");
        res.append(fileName);
        res.append(" ");
        res.append(super.toString());
        return res.toString();
    }
}
