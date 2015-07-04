/*******************************************************************************
 * // Copyright ==========
 * Copyright (c) 2008-2014 G Rosenberg.
 * // Copyright ==========
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 * // Contributor ==========
 *		G Rosenberg - initial API and implementation
 * // Contributor ==========
 *
 * Versions:
 * // Version ==========
 * 		1.0 - 2014.03.26: First release level code
 * 		1.1 - 2014.08.26: Updates, add Tests support
 * // Version ==========
 *******************************************************************************/
// ErrorListenerClass ==========
package net.certiv.json.parser;

import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import net.certiv.json.parser.gen.JsonLexer;
import net.certiv.json.util.Log;

public class JsonErrorListener extends BaseErrorListener {

	public int lastError = -1;

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {

		Parser parser = (Parser) recognizer;
		String name = parser.getSourceName();
		CommonTokenStream tokens = (CommonTokenStream) parser.getInputStream();
		tokens.fill();

		Token offSymbol = (Token) offendingSymbol;
		int thisError = offSymbol.getTokenIndex();
		if (offSymbol.getType() == -1 && thisError == tokens.size() - 1) {
			Log.debug(this, name + ": Incorrect error: " + msg);
			return;
		}

		String offSymName = JsonLexer.VOCABULARY.getSymbolicName(offSymbol.getType());
		List<String> stack = parser.getRuleInvocationStack();
		// Collections.reverse(stack);

		Log.error(this, name);
		Log.error(this, "Rule stack: " + stack);
		Log.error(this, "At line " + line + ":" + charPositionInLine + " at " + offSymName + ": " + msg);
		Log.error(this, "Expecting: " + parser.getExpectedTokens().toString(JsonLexer.VOCABULARY));

		if (thisError > lastError + 10) {
			lastError = thisError - 10;
		}
		int leadError = thisError + 10 < tokens.size() ? thisError + 10 : tokens.size() - thisError;

		for (int idx = lastError + 1; idx < leadError; idx++) {
			Token token = tokens.get(idx);
			if (token.getChannel() != Token.HIDDEN_CHANNEL) Log.error(this, token.toString());
		}
		lastError = thisError;
	}
}
// ErrorListenerClass ==========
