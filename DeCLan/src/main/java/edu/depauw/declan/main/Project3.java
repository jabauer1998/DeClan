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
		                  "CONST six = 6; seven = 7; bignine = 9.E-3;\n"
		                + "PROCEDURE ADD(VAR X : INTEGER; VAR Y : INTEGER) : INTEGER;\n"
		                + "BEGIN\n"
		                + "    RETURN X + Y\n"
		                + "END ADD;"
				+ "BEGIN\n"
		                + "  REPEAT\n"
		                + "  FOR I := 0 UNTIL I := 20 BY 1\n"
		                + "  DO\n"
		                + "  PrintInt(ADD(I * 2 + 3, 1000 / 2));\n"
		                + "  END;\n"
		                + "  i = i + 1"
		                + "  UNTIL i > 3\n"
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
