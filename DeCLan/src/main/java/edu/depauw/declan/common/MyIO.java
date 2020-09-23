package edu.depauw.declan.common;

import java.lang.*;

public class MyIO{
    
    private static String BOLDPURPLE(String message){
	return "\u001b[35;1m" + message + "\u001b[0m"; //Changes text color to bright magenta
    }
    private static String BOLDRED(String message){
	return "\u001b[31;1m" + message + "\u001b[0m";//Changes text color to bright red
    }
    private static String BOLDGREY(String message){
	return "\u001b[30;1m" + message + "\u001b[0m"; //Changes color to bright black (or grey)
    }
    
    private static  boolean DEBUG_ENABLED = false; //boolean to allow debugging ptrace statements

    public static void Start_DBG(){ //Allow Debugging
	DEBUG_ENABLED = true;
    }

    public static void End_DBG(){
	DEBUG_ENABLED = false; //Stop Debugging
    }

    public static void OUT(String message){ //Prints out a line just like standard output
	System.out.println(message);
    }
    
    public static void DBG(String message){ //trace debugging print function
	if(DEBUG_ENABLED){
	    OUT(BOLDPURPLE("DBG") + ' ' + BOLDGREY("=>") + ' ' + message);
	}
    }
    
    public static void ERROR(String message){ //Prints an error message
	OUT(BOLDRED("ERROR") + ' ' + BOLDGREY("=>") + ' ' + message);
    }

    public static void FATAL(String message){ //Prints an error and halts program execution
	ERROR(message);
	System.exit(1);
    }
};
