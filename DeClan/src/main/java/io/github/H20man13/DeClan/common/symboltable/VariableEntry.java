package io.github.H20man13.DeClan.common.symboltable;

import static io.github.H20man13.DeClan.main.MyIO.*;

import java.lang.String;
import java.lang.StringBuilder;

import io.github.H20man13.DeClan.common.Position;

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
