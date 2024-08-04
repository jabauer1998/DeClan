package io.github.H20man13.DeClan.main;

import io.github.H20man13.DeClan.common.ast.Program;

/**
 * A Parser is able to parse a DeCLan program into an abstract syntax tree. It
 * builds upon an underlying Lexer to extract Tokens from a Source.
 * 
 * @author bhoward
 */
public interface DeclanParser extends AutoCloseable {
	/**
	 * @return an abstract syntax tree of class Program, or throw a ParseException
	 */
	Program parseProgram();

	/**
	 * Specialized declaration of close() that guarantees no exceptions are thrown.
	 */
	@Override
	public void close();
}
