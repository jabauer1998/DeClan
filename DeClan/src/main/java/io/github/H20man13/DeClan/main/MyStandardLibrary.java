package io.github.H20man13.DeClan.main;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Source;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.common.icode.ICode;

public class MyStandardLibrary {
    private List<ICode> myStandardLibrary;
    private ErrorLog errorLog;

    public MyStandardLibrary(ErrorLog errLog){
        this.errorLog = errLog;
        this.myStandardLibrary = new LinkedList<ICode>();
        initLibraryRoutines();
    }

    private void initLibraryRoutines(){
        initLibraryRoutine("total := 0\n"
                          +"dividend := 0\n"
                          +"result := 0\n"
                          +"LABEL div\n"
                          +"total := total SUB dividend\n"
                          +"result := result ADD 1\n"
                          +"IF total GT 0 THEN div ELSE endDiv\n"
                          +"LABEL endDiv\n"
                          +"RETURN\n");
                          
        initLibraryRoutine("modTotal := 0\n"
                          +"ModDividend := 0\n"
                          +"modResult := 0\n"
                          +"LABEL mod\n" 
                          +"modTotal := modTotal SUB modDividend\n"
                          +"IF modTotal GE modDividend THEN mod ELSE endMod\n"
                          +"LABEL endMod\n"
                          +"modResult := modTotal\n"
                          +"RETURN\n");
    }

    private void initLibraryRoutine(String routine){
        StringReader reader = new StringReader(routine);
        Source source = new ReaderSource(reader);
        MyIrLexer lexer = new MyIrLexer(source, errorLog);
        MyIrParser parser = new MyIrParser(lexer, errorLog);
        List<ICode> icode = parser.parseProgram();
        this.myStandardLibrary.addAll(icode);

    }

}


