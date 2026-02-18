package declan.frontend;

import java.util.Iterator;

/**
 * A Lexer is essentially an iterator over the tokens in an input source. When
 * done, it can be used to close the underlying input.
 * 
 * @author bhoward
 */
public interface Lexer<TokType> extends Iterator<TokType>, AutoCloseable {
	/**
	 * Specialized declaration of close() that guarantees no exceptions are thrown.
	 */
	@Override
	public void close();
}
