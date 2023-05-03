package edu.depauw.declan.common;

import java.util.Iterator;

import io.github.H20man13.DeClan.common.token.DeClanToken;

/**
 * A Lexer is essentially an iterator over the tokens in an input source. When
 * done, it can be used to close the underlying input.
 * 
 * @author bhoward
 */
public interface Lexer extends Iterator<DeClanToken>, AutoCloseable {
	/**
	 * Specialized declaration of close() that guarantees no exceptions are thrown.
	 */
	@Override
	public void close();
}
