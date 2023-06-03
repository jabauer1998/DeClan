package io.github.H20man13.DeClan.common;

public class IrRegisterGenerator {
    private char startLetter = 'a';
    private char endLetter = 'z';
    private char currentLetter = 'a';
    private int startNumber = 0;
    private int endNumber = 9;
    private int currentNumber = 0;

    private static boolean firstRound = true;
    
    public String genNextRegister(){
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
