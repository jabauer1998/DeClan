package io.github.H20man13.DeClan.common;
import java.lang.*;

/**
This class contains methods to make the error statements for the compiler nicer and more readable. This class has a few types of print statements aswell as private methods to change the color of the text using escape sequences.
@author Jacob Bauer
 */
public class MyIO{

    /** Changes the color of the text to purple
	@param <code> message </code> the string to change to purple
	@return the purple text
	@author Jacob Bauer
     */
    private static String BOLDPURPLE(String message){
	return "\u001b[35;1m" + message + "\u001b[0m"; //Changes text color to bright magenta
    }
    /** Changes the color of the text to red
	@param <code> message </code> the string to change to red
	@return the red text
	@author Jacob Bauer
     */
    public static String BOLDRED(String message){
	return "\u001b[31;1m" + message + "\u001b[0m";
    }
    /** Changes the color of the text to Grey
	@param <code>message</code> the string to change to Grey
	@return the grey text
	@author Jacob Bauer
     */
    public static String BOLDGREY(String message){
	return "\u001b[30;1m" + message + "\u001b[0m";
    }
    
    public static  boolean DEBUG_ENABLED = false;

    /**
       This function enables debuuging in a section of code by setting <code> DEBUG_ENABLED </code> to true
       @author Jacob Bauer
     */
    public static void Start_DBG(){
	DEBUG_ENABLED = true;
    }
    /**
       This function disables debuuging in a section of code by setting <code> DEBUG_ENABLED </code> to false
       @author Jacob Bauer
     */
    public static void End_DBG(){
	DEBUG_ENABLED = false;
    }
    /** 
	A shorthand method for <code> System.out.println() </code>
	@param <code> message </code> message to print out
	@author Jacob Bauer
     */
    public static void OUT(String message){
	System.out.println(message);
    }

    /**
       Prints a Debugging Statement if debugging is enabled
       @param <code> message </code> debugging message to print out
       @author Jacob Bauer
     */
    
    public static void DBG(String message){
	if(DEBUG_ENABLED){
	    OUT(BOLDPURPLE("DBG") + ' ' + BOLDGREY("=>") + ' ' + message);
	}
    }
};
