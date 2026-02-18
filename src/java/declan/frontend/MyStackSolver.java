package declan.frontend;

import java.util.ArrayList;
import java.util.Stack;
import java.lang.String;

/**
 *The Stack Solver is a Stack Based Language Interpreter that can solve the stack based language created by the PostfixPrintVisitor
 *This was an option for project 2
 *The default input is the string provided but it can also take in a command line argument string and use that as well
 *@author Jacob Bauer
 */
public class MyStackSolver {
    private static String standardInput  = "7\n"
	          + "6\n"
	          + "SUBTRACT\n"
	          + "PRINT\n"
	          + "2\n"
                  + "6\n"
	          + "7\n"
                  + "ADD\n"
                  + "MULTIPLY\n"
                  + "6\n"
	          + "MODULO\n"
                  + "PRINT\n"
                  + "6\n"
                  + "7\n"
                  + "2\n"
                  + "DIVIDE\n"
                  + "SUBTRACT\n"
                  + "PRINT\n"
                  + "6\n"
                  + "7\n"
                  + "MULTIPLY\n"
                  + "PRINT\n";
    private static Stack <Integer> accumulator = new Stack<>();
    public static void stackSolver(ArrayList<String> argv){
	ArrayList <String> parsedInput;
	if(argv.size() > 0){
	    parsedInput = argv;
	} else {
	    parsedInput = parseString(standardInput);
	}
	for(int i = 0; i < parsedInput.size(); i++){
	    if(parsedInput.get(i).equals("MULTIPLY")){
		int right = accumulator.pop();
		int left = accumulator.pop();
		accumulator.push(left * right);
	    } else if (parsedInput.get(i).equals("DIVIDE")){
		int right = accumulator.pop();
		int left = accumulator.pop();
		accumulator.push(left / right);
	    } else if (parsedInput.get(i).equals("PRINT")){
		int pout = accumulator.pop();
		System.out.println(pout);
	    } else if (parsedInput.get(i).equals("MODULO")){
		int right = accumulator.pop();
		int left = accumulator.pop();
		accumulator.push(left % right);
	    } else if (parsedInput.get(i).equals("SUBTRACT")){
		int right = accumulator.pop();
		int left = accumulator.pop();
		accumulator.push(left - right);
	    } else if (parsedInput.get(i).equals("ADD")){
		int right = accumulator.pop();
		int left = accumulator.pop();
		accumulator.push(left + right);
	    } else if (parsedInput.get(i).equals("NEGATE")){
		int value = accumulator.pop();
		value = -value;
		accumulator.push(value);
	    } else {
		accumulator.push(Integer.parseInt(parsedInput.get(i)));
	    }
	}
    }
    public static ArrayList<String> parseString(String input){
	ArrayList<String>local = new ArrayList<>();
	int start = 0;
	for(int i = 0; i < input.length(); i++){
	    if(input.charAt(i) == '\n'){
		local.add(input.substring(start, i));
		start = i + 1;
	    }
	}
	return local;
    }
}

