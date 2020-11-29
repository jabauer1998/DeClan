package edu.depauw.declan.common.symboltable;

import java.lang.String;
import java.lang.StringBuilder;

import static edu.depauw.declan.common.MyIO.*;
import edu.depauw.declan.common.Position;

public class VariableEntry{
    
    private Boolean isConst; //variable to store the type or CONST
    private Object value; //variable to store the current value of the variable
    private Position declPosition; //where variable was declared

    public VariableEntry(Boolean isConst, Object value){
        this.isConst = isConst;
	this.value = value;
    }
    
    public Object getValue(){
	return value;
    }

    public boolean isConst(){
	return isConst;
    }
    
    public void setValue(Object value){
	this.value = value;
    }
}
