package edu.depauw.declan.common.ast;

import java.lang.String;

public class TableEntry{
    
    private String type; //variable to store the type or CONST 
    private String value; //variable to store the current value of the variable

    public TableEntry(String type, String value){
	this.type = type;
	this.value = value;
    }

    public TableEntry(String type){
	this.type = type;
	this.value = "";
    }
    
    public String getValue(){
	return value;
    }
    public String getType(){
	return type;
    }
    public void setValue(String value){
	this.value = value;
    }
}
