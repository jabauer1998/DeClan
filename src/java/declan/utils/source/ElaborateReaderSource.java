package declan.utils.source;

import java.io.Reader;

import declan.utils.position.Position;
import declan.utils.position.FilePosition;

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
