package edu.depauw.declan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;

import static edu.depauw.declan.common.MyIO.*;
import edu.depauw.declan.common.Position;

public class VariableEntry{
    
    private Boolean CONST; //variable to store the type or CONST
    private Object value; //variable to store the current value of the variable
    private Position declPosition; //where variable was declared
    
    public VariableEntry(Boolean CONST, String value){
        this.CONST = CONST;
	if(value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"'){
	  this.value = value.substring(1, value.length() - 1);
	} else if (value.equals("TRUE")){
	  this.value = true;
	} else if (value.equals("FALSE")){
	  this.value = false;
	} else if(value.contains(".")) {
	  this.value = Double.parseDouble(value);
	} else if (!value.equals("")) {
	  this.value = Integer.parseInt(value);
	} else {
	  this.value = null;
	}
    }

    public VariableEntry(){
      this(false, "");
    }
    
    public Object getValue(){
	if(value == null){
	    FATAL("Variable hast been initialized to any value yet");
	}
	return value;
    }
  
    
    public void setValue(Object value){
	this.value = value;
    }
  
    public static enum VarType{
	INTEGER, BOOLEAN, REAL, CONST
    }
    
}
