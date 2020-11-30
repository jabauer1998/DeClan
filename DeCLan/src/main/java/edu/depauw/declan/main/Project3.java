package edu.depauw.declan.main;

import java.util.Properties;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.InterpreterException;
import edu.depauw.declan.common.ParseException;
import edu.depauw.declan.common.Parser;
import edu.depauw.declan.common.ast.ASTVisitor;
import edu.depauw.declan.common.ast.Program;

/**
 * Main class for Project 3 -- Interpreter for a subset of DeCLan (Fall 2020). Parse
 * a simple program, then execute it directly, statement by statement.
 * 
 * @author bhoward
 */

public class Project3 {
	public static void main(String[] args) {
		String demoSource =
		                  "(* Declare some constants and a global variable *)\n"
				+ "CONST six = 8; seven = 90;\n"
				+ "VAR answer : INTEGER;\n"
				+ "(* Define a function *)\n"
				+ "PROCEDURE gcd(a: INTEGER; b: INTEGER): INTEGER;\n"
				+ "  VAR c : INTEGER;\n"
				+ "  BEGIN\n"
				+ "    IF b = 0 THEN c := a\n"
				+ "    ELSE c := gcd(b, a DIV b)\n"
				+ "    END;\n"
				+ "    RETURN c\n"
				+ "  END gcd;\n"
				+ "(* Define a proper procedure *)\n"
				+ "PROCEDURE Display(VAR answer: INTEGER; a, b: INTEGER);\n"
				+ "  VAR i : INTEGER;\n"
				+ "  BEGIN\n"
				+ "    FOR i := a TO b BY -1 DO\n"
				+ "      PrintInt(answer); PrintLn;\n"
				+ "      WHILE answer > i DO answer := answer - 1\n"
				+ "      ELSIF answer < i DO answer := answer + 1\n"
				+ "      END\n"
				+ "    END\n"
				+ "  END Display;\n"
				+ "(*********** Main Program ***********)\n"
				+ "BEGIN\n"
				+ "  answer := six * seven * gcd(six, seven);\n"
				+ "  PrintString(\"The answer is \");\n"
				+ "  Display(answer, seven, six);\n"
				+ "  PrintInt(answer); PrintLn;\n"
				+ "END.\n";

		Properties props = new Properties();
		props.setProperty("useModelLexer", "false");
		props.setProperty("useModelParser", "false");
		props.setProperty("useModelInterpreter", "false");
		props.setProperty("sourceFile", "");
		props.setProperty("demoSource", demoSource);
		
		Config config = new Config(args, props);

		try (Parser parser = config.getParser()) {
			Program program = parser.parseProgram();
			ASTVisitor interpreter = config.getInterpreter();
			program.accept(interpreter);
		} catch (ParseException pe) {
			System.err.println(pe.getMessage());
		} catch (InterpreterException ie) {
			System.err.println(ie.getMessage());
		}

		for (ErrorLog.LogItem item : config.getErrorLog()) {
			System.err.println(item);
		}

		System.out.println("DONE");
	}
}
