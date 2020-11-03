package edu.depauw.declan.common.ast;

import java.lang.String;
import java.lang.StringBuilder;
import java.lang.Number;

import static edu.depauw.declan.common.MyIO.*;

public class VariableEntry{
    
    private VarType typedef; //variable to store the type or CONST
    private Number value; //variable to store the current value of the variable
    
    public VariableEntry(String type, String value){
	if(type.equals("CONST")){
	    this.typedef = VarType.CONST;
	} else if(type.equals("REAL")){
	    this.typedef = VarType.REAL;
	} else if(type.equals("INTEGER")){
	    this.typedef = VarType.INTEGER;
	} else if(type.equals("BOOLEAN")){
	    this.typedef = VarType.BOOLEAN;
	} else {
	    FATAL("Unknown type: " + type);
	}
	if(!value.equals("")){
	    if(typedef == VarType.CONST){
		if(value.contains(".")){
		    this.value = Double.parseDouble(value);
		} else {
		    this.value = Integer.parseInt(value);
		}
	    } else if(this.typedef == VarType.REAL) {
		this.value = Double.parseDouble(value);
	    } else {
		this.value = Integer.parseInt(value);
	    }
	} else {
	    this.value = null;
	}
    }

    public VariableEntry(String type){
	this(type, "");
    }

    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      mystring.append("Var TYPE: ");
      mystring.append(typeToString(getType()));
      mystring.append("Cur VALUE: ");
      mystring.append("" + getValue());
      return mystring.toString();
    }
    
    public Number getValue(){
	if(value == null){
	    FATAL("Value hasnt been initialized to a value yet");
	}
	return value;
    }
    
    public VarType getType(){
	return typedef;
    }
    
    public void setValue(Number value){
	this.value = value;
    }

    private String typeToString(VarType type){
      if(type == VarType.INTEGER){
	return "int";
      } else if (type == VarType.BOOLEAN){
	return "bool";
      } else if (type == VarType.REAL){
	return "real";
      } else if (type == VarType.CONST){
	return "const";
      } else {
	FATAL("Unknown vartype value: " + type);
	return "";
      }
    }
  
    public static enum VarType{
	INTEGER, BOOLEAN, REAL, CONST
    }
    
}
