package io.github.h20man13.DeClan.common.gen;

public class IrRegisterGenerator implements Generator {
    private char startLetter;
    private char endLetter;
    private char currentLetter;
    private int startNumber;
    private int endNumber;
    private int currentNumber;
    private boolean firstRound;

	public IrRegisterGenerator(){
		firstRound = true;
		startLetter = 'a';
		endLetter = 'z';
		currentLetter = 'a';
		startNumber = 0;
		endNumber = 9;
		currentNumber = 0;
	}
	
	public void synch(char currentLetter, int currentNumber) {
		this.currentLetter = currentLetter;
		this.currentNumber = currentNumber;
	}
	
	public char getCurrentLetter() {
		return currentLetter;
	}
	
	public int getCurrentNumber() {
		return currentNumber;
	}
    
    public String genNext(){
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
					currentNumber = startNumber;
					endNumber = endNumber * 10 + 9;
				}
			}
			return awnser;
		}
    }
}
