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
		                + "VAR result : INTEGER;\n"
		                + "PROCEDURE ADD(VAR X : INTEGER; VAR Y : INTEGER) : INTEGER;\n"
		                + "    RETURN X + Y\n"
		                + "END ADD;"
		                + "PROCEDURE MULT(VAR X : INTEGER; VAR Y : INTEGER) : INTEGER;\n"
		                + "    RETURN 70 * 80\n"
		                + "END MULT;"
				+ "BEGIN\n"
		                + "FOR I := 0 TO I < 20 BY 1 DO\n"
		                + "  result := ADD(MULT(7, 10), seven);\n"
		                + "  PrintInt(result)\n"
		                + "END\n"
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
