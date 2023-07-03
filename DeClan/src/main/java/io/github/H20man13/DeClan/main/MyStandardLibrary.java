package io.github.H20man13.DeClan.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Position;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.common.ast.Library;
import io.github.H20man13.DeClan.common.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;

public class MyStandardLibrary {
    private ErrorLog errLog;

    public MyStandardLibrary(ErrorLog errLog){
        this.errLog = errLog;
    }

    public Library mathLibrary(){
        return parseLibrarySource("declan_libraries/Math.declib");
    }

    public Library ioLibrary(){
        return parseLibrarySource("declan_libraries/Io.declib");
    }

    private Library parseLibrarySource(String sourceName){
        try{
            File file = new File(sourceName);
            if(file.exists()){
                FileReader reader = new FileReader(file);
                Source readerSource = new ReaderSource(reader);
                MyDeClanLexer lexer = new MyDeClanLexer(readerSource, errLog);
                MyDeClanParser parser = new MyDeClanParser(lexer, errLog);
                return parser.parseLibrary();
            } else {
                errLog.add("Could not create file reader with source name " + sourceName, new Position(0, 0));
                return null;
            }
        } catch(FileNotFoundException exp) {
            errLog.add("Could not create file reader with source name " + sourceName, new Position(0, 0));
            return null;
        }
    }

}


