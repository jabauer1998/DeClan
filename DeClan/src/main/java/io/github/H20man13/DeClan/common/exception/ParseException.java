package io.github.H20man13.DeClan.common.exception;

/**
 * An unchecked exception class that may be thrown during parsing.
 * 
 * @author bhoward
 */
@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
	public ParseException(String message) {
		super(message);
	}
}
