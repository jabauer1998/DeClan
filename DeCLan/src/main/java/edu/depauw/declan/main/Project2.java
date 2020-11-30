package edu.depauw.declan.main;

import java.util.Properties;
import java.util.ArrayList;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.ParseException;
import edu.depauw.declan.common.Parser;
import edu.depauw.declan.common.ast.Program;

import edu.depauw.declan.main.StackSolver;

/**
 * Main class for Project 2 -- Parser for a subset of DeCLan (Fall 2020). Parse
 * a simple program and print out the corresponding postfix representation.
 * 
 * @author bhoward
 */
public class Project2 {
	public static void main(String[] args) {
		String demoSource =
				  "CONST six = 6; seven = 7;\n"
				+ "BEGIN\n"
				+ "  PrintInt(seven - six);\n"
				+ "  PrintInt(2 * (six + seven) MOD six);\n"
				+ "  PrintInt(six - seven DIV 2);\n"
				+ "  PrintInt(six * seven);\n"
				+ "END.\n";

		Properties props = new Properties();
		props.setProperty("useModelLexer", "false");
		props.setProperty("useModelParser", "false");
		props.setProperty("sourceFile", "");
		props.setProperty("demoSource", demoSource);
		
		Config config = new Config(args, props);

		try (Parser parser = config.getParser()) {
			Program program = parser.parseProgram();
			System.out.println("Proj 1 Interpreter");
			program.accept(new PostfixPrintVisitor());
			System.out.println("DONE");
			System.out.println("Proj 2(op 1)/ proj 3 Interpreter");
			program.accept(new PostfixInterpreterVisitor());
			System.out.println("Proj 2(op 2)/ Stack Language Interpreter");
			StackSolver.stackSolver(new ArrayList<>());
		} catch (ParseException pe) {
			System.err.println(pe.getMessage());
		}

		for (ErrorLog.LogItem item : config.getErrorLog()) {
			System.err.println(item);
		}
	}
}
