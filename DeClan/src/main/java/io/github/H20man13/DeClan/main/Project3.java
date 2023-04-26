package io.github.H20man13.DeClan.main;

import java.util.Properties;

import io.github.H20man13.DeClan.common.ErrorLog;
import io.github.H20man13.DeClan.common.InterpreterException;
import io.github.H20man13.DeClan.common.ParseException;
import io.github.H20man13.DeClan.common.Parser;
import io.github.H20man13.DeClan.common.ast.ASTVisitor;
import io.github.H20man13.DeClan.common.ast.Program;

/**
 * Main class for Project 3 -- Interpreter for a subset of DeCLan (Fall 2020). Parse
 * a simple program, then execute it directly, statement by statement.
 * 
 * @author bhoward
 */

public class Project3 {
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
