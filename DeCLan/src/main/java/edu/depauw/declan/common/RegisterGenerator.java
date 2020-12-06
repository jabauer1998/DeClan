package edu.depauw.declan.common;

public class RegisterGenerator {
    private static char startLetter = 'a';
    private static char endLetter = 'z';
    private static char currentLetter = 'a';
    private static int startNumber = 0;
    private static int endNumber = 9;
    private static int currentNumber = 0;

    private static boolean firstRound = true;
    
    public static void main(String [] args){
	for(int i = 0 ; i < 10000; i++){
	    System.out.println(genNextRegister());
	}
    }
    public static String genNextRegister(){
	if(firstRound){
	    String awnser = "" + currentLetter;
	    if(currentLetter != endLetter){
		currentLetter++;
	    } else if(currentLetter == 'z'){
		startLetter = Character.toUpperCase(startLetter);
		endLetter = Character.toUpperCase(endLetter);
		currentLetter = startLetter;
	    } else if (currentLetter == 'Z'){
		startLetter = Character.toLowerCase(startLetter);
		endLetter = Character.toLowerCase(endLetter);
		currentLetter = startLetter;
		firstRound = false;
	    }
	    return awnser;
	} else {
	    String awnser = "" + currentLetter + currentNumber;
	    if(currentNumber != endNumber){
		currentNumber++;
	    } else {
		if(currentLetter != endLetter){
		    currentLetter++;
		    currentNumber = startNumber;
		} else if (endLetter == 'z') {
		    startLetter = Character.toUpperCase(startLetter);
		    endLetter = Character.toUpperCase(endLetter);
		    currentLetter = startLetter;
		    currentNumber = startNumber;
		} else if (endLetter == 'Z') {
		    startLetter = Character.toLowerCase(startLetter);
		    endLetter = Character.toLowerCase(endLetter);
		    currentLetter = startLetter;
		    if(startNumber == 0){
			startNumber = 10;
		    } else {
			startNumber *= 10;
		    }
		    endNumber = endNumber * 10 + 9;
		}
	    }
	    return awnser;
	}
    }
}
