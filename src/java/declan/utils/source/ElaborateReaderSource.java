package io.github.h20man13.DeClan.common.source;

import java.io.Reader;

import io.github.h20man13.DeClan.common.position.Position;
import io.github.h20man13.DeClan.common.position.FilePosition;

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
