package io.github.H20man13.DeClan.common;

import java.io.Reader;

import edu.depauw.declan.common.Position;

public class ElaborateReaderSource extends ReaderSource{
    private String fileName;
    
    public ElaborateReaderSource(String fileName, Reader in) {
        super(in);
        this.fileName = fileName;
    }

    @Override
    public Position getPosition(){
        return new FilePosition(fileName, super.getPosition());
    }
}
