package edu.depauw.declan.common.ast;

import java.util.Stack;
import java.util.HashMap;

import java.lang.String;
import java.lang.StringBuilder;


/**
 *The environment class is used to create symbol tables for the Declan Compiler
 *The symbol tables are implemented using hashmaps and the scopes are implemented using stacks
 *The keys must allways be strings however the entries are an object of your choice
 *@author Jacob Bauer
 */
public class Environment <TableType> {
    /**
     *The environment stack is how scopes are implemented
     *@author Jacob Bauer
     */
    private Stack <HashMap<String, TableType>> environment;

    /**
     *The costructor dynamicaly initailizes the stack
     *@author Jacob Bauer
     */ 
    public Environment(){
	environment = new Stack<>();
    }

    /**
     * This is the method that removes the top scope or hashmap in the stack
     * @author Jacob Bauer
     */
    public void removeScope(){
	environment.pop();
    }
    /** 
     *This is the method that adds a scope or hashmap to the stack
     * @author Jacob Bauer
     */
    public void addScope(){
	environment.push(new HashMap<>());
    }
    /**
     * This method is used to check if a variable exists within the entire stack
     * @param <code> symbolName </code> => String => the name of the symbol you want to find
     * @author Jacob Bauer
     */
    public boolean entryExists(String symbolName){
	for(HashMap<String, TableType> current : environment){
	    if(current.containsKey(symbolName)){
		return true;
	    }
	}
	return false;
    }
  
    /**
     * The find Entry method tries to find the symbolname passed and it returns the data corresponding to the symbol
     * @param <code> symbolName </code> => String => the symbol name passed
     * @author Jacob Bauer
     */
    
    public TableType findEntry(String symbolName){
	for(HashMap<String, TableType> current : environment){
	    if(current.containsKey(symbolName)){
		return current.get(symbolName);
	    }
	}
	return null;
    }

    /** 
     * To add the entry to the symbol table
     * 
     */
    public void addEntry(String name, TableType description){
	HashMap<String, TableType> saved = environment.pop();
	saved.put(name, description);
	environment.push(saved);
    }

    /**
    * To String
    */

    @Override
    public String toString(){
      StringBuilder mystring = new StringBuilder();
      for(int i = environment.size() - 1; i >= 0; i--){
	mystring.append("STACK LEVEL -> " + i + '\n');
	HashMap<String, TableType> list = environment.get(i);
	for(String key : list.keySet()){
	  mystring.append("KEY: " + key + " VALUE: ");
	  mystring.append(list.get(key).toString());
	  mystring.append('\n');
	}
      }
      return mystring.toString();
    }
}
