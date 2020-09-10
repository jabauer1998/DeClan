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
    
    private static  boolean DEBUG_ENABLED = true; //boolean to allow debugging ptrace statements

    public static void Out(String message){ //Prints out a line just like standard output
	System.out.println(message);
    }
    
    public static void DbgOut(String message){ //trace debugging print function
	if(DEBUG_ENABLED){
	    Out(BOLDPURPLE("DBG") + ' ' + BOLDGREY("=>") + ' ' + message);
	}
    }
    
    public static void ErrOut(String message){ //Prints an error and halts program execution
	Out(BOLDRED("ERROR") + ' ' + BOLDGREY("=>") + ' ' + message);
	System.exit(1);
    }
};
