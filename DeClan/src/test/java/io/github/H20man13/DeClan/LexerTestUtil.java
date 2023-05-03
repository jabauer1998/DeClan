package io.github.H20man13.DeClan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Iterator;

import edu.depauw.declan.common.ErrorLog;
import edu.depauw.declan.common.Lexer;
import edu.depauw.declan.common.Source;
import edu.depauw.declan.model.ReferenceLexer;
import io.github.H20man13.DeClan.common.ReaderSource;
import io.github.H20man13.DeClan.main.MyDeClanLexer;

public class LexerTestUtil {

	/**
	 * Run the given input through both MyLexer and the ReferenceLexer (provided in
	 * the .jar file in the libs folder). Assertions check that they both produce
	 * the same sequence of Tokens, as well as the same set of error messages (if
	 * any).
	 * 
	 * @param input
	 */
	static void compareToModel(String input) {
		Source mySource = new ReaderSource(new StringReader(input));
		Source modelSource = new ReaderSource(new StringReader(input));

		ErrorLog myErrorLog = new ErrorLog();
		ErrorLog modelErrorLog = new ErrorLog();

		try (Lexer myLexer = new MyDeClanLexer(mySource, myErrorLog);
			 Lexer modelLexer = new ReferenceLexer(modelSource, modelErrorLog)) {
			while (modelLexer.hasNext()) {
				assertTrue("Not enough tokens", myLexer.hasNext());
				assertEquals(modelLexer.next(), myLexer.next());
			}
			assertFalse("Too many tokens", myLexer.hasNext());
		}

		Iterator<edu.depauw.declan.common.ErrorLog.LogItem> myItems = myErrorLog.iterator();
		for (edu.depauw.declan.common.ErrorLog.LogItem item : modelErrorLog) {
			assertTrue("Not enough error items", myItems.hasNext());
			assertEquals(item, myItems.next());
		}
		assertFalse("Too many error items", myItems.hasNext());
	}

}
