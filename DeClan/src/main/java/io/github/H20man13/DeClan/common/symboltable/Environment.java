package io.github.H20man13.DeClan.common.symboltable;

import java.util.Stack;
import java.util.HashMap;

import static io.github.H20man13.DeClan.main.MyIO.*;

import java.lang.String;
import java.lang.StringBuilder;


/**
 *The environment class is used to create symbol tables for the Declan Compiler
 *The symbol tables are implemented using hashmaps and the scopes are implemented using stacks
 *The keys must allways be strings however the entries are an object of your choice
 *@author Jacob Bauer
 */
public class Environment <KeyType, TableType> {
    /**
     *The environment stack is how scopes are implemented
     *@author Jacob Bauer
     */
    private Stack <HashMap<KeyType, TableType>> environment;

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
	    environment.push(new HashMap<KeyType, TableType>());
    }
    /**
     * This method is used to check if a variable exists within the entire stack
     * @param <code> symbolName </code> => String => the name of the symbol you want to find
     * @author Jacob Bauer
     */
    public boolean entryExists(KeyType symbolName){
	for(HashMap<KeyType, TableType> current : environment){
	    if(current.containsKey(symbolName)){
		return true;
	    }
	}
	return false;
    }
    /**
     * This method is used to check if a variable exists within the current scope
     * @param <code> symbolName </code> => String => the name of the symbol you want to find
     * @author Jacob Bauer
     */
    public boolean inScope(KeyType symbolName){
	HashMap<KeyType, TableType> list = environment.pop();
	boolean tf = list.containsKey(symbolName);
	environment.push(list);
	return tf;
    }
  
    /**
     * The get Entry method tries to find the symbolname passed and it returns the data corresponding to the symbol
     * @param <code> symbolName </code> => String => the symbol name passed
     * @author Jacob Bauer
     */
    
    public TableType getEntry(KeyType symbolName){
	for(HashMap<KeyType, TableType> current : environment){
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
    public void addEntry(KeyType name, TableType description){
      HashMap<KeyType, TableType> saved = environment.pop();
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
	HashMap<KeyType, TableType> list = environment.get(i);
	for(KeyType key : list.keySet()){
	  mystring.append("KEY: " + key + " VALUE: ");
	  mystring.append(list.get(key).toString());
	  mystring.append('\n');
	}
      }
      return mystring.toString();
    }
}
