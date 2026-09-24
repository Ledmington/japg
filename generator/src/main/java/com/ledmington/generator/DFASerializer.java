/*
 * japg - Just a Parser Generator
 * Copyright (C) 2025-2026 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.generator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.automata.AcceptingState;
import com.ledmington.automata.AutomataUtils;
import com.ledmington.automata.DFA;
import com.ledmington.automata.DFAMinimizer;
import com.ledmington.automata.EpsilonNFAToNFA;
import com.ledmington.automata.GrammarToEpsilonNFA;
import com.ledmington.automata.NFA;
import com.ledmington.automata.NFAToDFA;
import com.ledmington.automata.State;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Utils;

/** Helper class to generate java code for a DFA parsing a given list of productions. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DFASerializer {

	private DFASerializer() {}

	/**
	 * Generates code of a DFA to match a list of tokens.
	 *
	 * @param sb THe StringBuilder to place the generated code into.
	 * @param lexerName The name of the resulting class.
	 * @param lexerProductions The list of token productions, sorted by priority.
	 */
	public static void generateLexer(
			final IndentedStringBuilder sb, final String lexerName, final List<Production> lexerProductions) {
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA();
		final NFA epsilonNFA = g2enfa.convert(lexerProductions);
		AutomataUtils.assertEpsilonNFAValid(epsilonNFA);
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA();
		final NFA nfa = enfa2nfa.convert(epsilonNFA);
		AutomataUtils.assertNFAValid(nfa);
		final NFAToDFA nfa2dfa = new NFAToDFA();
		final DFA dfa = nfa2dfa.convert(nfa);
		AutomataUtils.assertDFAValid(dfa);
		final DFAMinimizer DFAmin = new DFAMinimizer();
		final DFA minimizedDFA = DFAmin.minimize(dfa);
		AutomataUtils.assertDFAValid(minimizedDFA);

		// re-index DFA states
		final Map<State, Integer> stateIndex = new HashMap<>();
		{
			stateIndex.put(minimizedDFA.startingState(), 0);
			int idx = 1;
			for (final State s : minimizedDFA.states()) {
				if (s.equals(minimizedDFA.startingState())) {
					continue;
				}
				stateIndex.put(s, idx);
				idx++;
			}
		}

		final List<State> allStates = stateIndex.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.toList();

		final List<String> sortedTokenTypes =
				lexerProductions.stream().map(p -> p.start().name()).sorted().toList();
		final Map<String, Integer> tokenTypeToIndex = IntStream.range(0, sortedTokenTypes.size())
				.boxed()
				.collect(Collectors.toMap(sortedTokenTypes::get, i -> i));
		sb.append("public enum TokenType {\n")
				.indent()
				.append(String.join(",\n", sortedTokenTypes))
				.append('\n')
				.deindent()
				.append("}\n")
				.append("public record Token(TokenType type, String content) {\n")
				.indent()
				.append("public Token {\n")
				.indent()
				.append("Objects.requireNonNull(type);\n")
				.append("Objects.requireNonNull(content);\n")
				.deindent()
				.append("}\n")
				.deindent()
				.append("}\n")
				.append("public static final class ")
				.append(lexerName)
				.append(" {\n")
				.indent();

		final boolean[] isAccepting = new boolean[allStates.size()];
		for (int i = 0; i < allStates.size(); i++) {
			final State s = allStates.get(i);
			isAccepting[i] = s.isAccepting();
		}

		final boolean[] isSkippable = new boolean[allStates.size()];
		for (int i = 0; i < allStates.size(); i++) {
			final State s = allStates.get(i);
			isSkippable[i] = (s.isAccepting() && Production.isSkippable(((AcceptingState) s).tokenName()));
		}

		final int[] tokensToMatch = new int[allStates.size()];
		for (int i = 0; i < allStates.size(); i++) {
			final State s = allStates.get(i);
			tokensToMatch[i] = s.isAccepting() ? tokenTypeToIndex.get(((AcceptingState) s).tokenName()) : -1;
		}

		final int allTransitions = minimizedDFA.states().stream()
				.mapToInt(s -> minimizedDFA.neighbors(s).size())
				.sum();
		final int[] offsets = new int[allStates.size() + 1];
		offsets[0] = 0;
		final char[] symbols = new char[allTransitions];
		final int[] destinations = new int[allTransitions];
		int idx = 0;
		for (int i = 1; i < allStates.size() + 1; i++) {
			final State s = allStates.get(i - 1);
			offsets[i] = offsets[i - 1] + minimizedDFA.neighbors(s).size();
			for (final Map.Entry<Character, State> e : minimizedDFA.neighbors(s).entrySet().stream()
					.sorted(Map.Entry.comparingByKey())
					.toList()) {
				symbols[idx] = e.getKey();
				destinations[idx] = stateIndex.get(e.getValue());
				idx++;
			}
		}

		sb.append("private final boolean[] isAccepting;\n")
				.append("private final boolean[] isSkippable;\n")
				.append("private final TokenType[] tokensToMatch;\n")
				.append("private final int[] offsets;\n")
				.append("private final char[] symbols;\n")
				.append("private final int[] destinations;\n");

		final int totalBytes = 4
				+ 4
				+ isAccepting.length
				+ isSkippable.length
				+ Integer.BYTES * tokensToMatch.length
				+ Integer.BYTES * offsets.length
				+ Character.BYTES * symbols.length
				+ Integer.BYTES * destinations.length;
		final ByteBuffer bb = ByteBuffer.allocate(totalBytes).order(ByteOrder.BIG_ENDIAN);

		final int numStates = tokensToMatch.length;
		bb.putInt(numStates);
		bb.putInt(symbols.length);
		for (final boolean acc : isAccepting) {
			bb.put(acc ? (byte) 0xff : (byte) 0x00);
		}
		for (final boolean skip : isSkippable) {
			bb.put(skip ? (byte) 0xff : (byte) 0x00);
		}
		for (final int tokenIndex : tokensToMatch) {
			bb.putInt(tokenIndex);
		}
		for (final int off : offsets) {
			bb.putInt(off);
		}
		for (final char sym : symbols) {
			bb.putChar(sym);
		}
		for (final int dest : destinations) {
			bb.putInt(dest);
		}

		final String encoded = Utils.getEscapedString(Base64.getEncoder().encodeToString(bb.array()));

		/*
		java has an hard-coded limit of 65'534 characters for "constant strings" (string literals), so if the string
		we need to write is longer, we split it into chunks and concatenate them in such a way that the (dumb)
		compiler cannot inline (declaring separate non-final variables and concatenate them into another non-final
		variable)
		 */
		final int maxChunkLength = 65_534;

		sb.append("public ").append(lexerName).append("() {\n").indent();

		if (encoded.length() < maxChunkLength) {
			sb.append("final String encoded = \"").append(encoded).append("\";\n");
		} else {
			// Split encoded string into chunks
			sb.append("String encoded;\n").append("{\n").indent();
			final int n = encoded.length();
			int chunkIndex = 0;
			int start = 0;
			for (; start + maxChunkLength < n; start += maxChunkLength, chunkIndex++) {
				final String chunkName = "chunk_" + chunkIndex;
				sb.append("String ")
						.append(chunkName)
						.append(" = \"")
						.append(encoded.substring(start, start + maxChunkLength))
						.append("\";\n");
			}
			// encode last chunk "manually"
			final String lastChunkName = "chunk_" + chunkIndex;
			chunkIndex++;
			sb.append("String ")
					.append(lastChunkName)
					.append(" = \"")
					.append(encoded.substring(start))
					.append("\";\n")
					.append("encoded = ");
			for (int i = 0; i < chunkIndex; i++) {
				final String chunkName = "chunk_" + i;
				sb.append(chunkName);
				if (i < chunkIndex - 1) {
					sb.append(" + ");
				}
			}
			sb.append(";\n").deindent().append("}\n");
		}

		sb.append(
						"final ByteBuffer bb = ByteBuffer.wrap(Base64.getDecoder().decode(encoded)).order(ByteOrder.BIG_ENDIAN);\n")
				.append("final int num_states = bb.getInt();\n")
				.append("final int num_destinations = bb.getInt();\n")
				.append("this.isAccepting = new boolean[num_states];\n")
				.append("this.isSkippable = new boolean[num_states];\n")
				.append("this.tokensToMatch = new TokenType[num_states];\n")
				.append("this.offsets = new int[num_states + 1];\n")
				.append("this.symbols = new char[num_destinations];\n")
				.append("this.destinations = new int[num_destinations];\n")
				.append("for (int i = 0; i < num_states; i++) {\n")
				.indent()
				.append("this.isAccepting[i] = bb.get() == (byte) 0xff;\n")
				.deindent()
				.append("}\n")
				.append("for (int i = 0; i < num_states; i++) {\n")
				.indent()
				.append("this.isSkippable[i] = bb.get() == (byte) 0xff;\n")
				.deindent()
				.append("}\n")
				.append("final TokenType[] tokenTypes = TokenType.values();\n")
				.append("for (int i = 0; i < num_states; i++) {\n")
				.indent()
				.append("final int x = bb.getInt();\n")
				.append("this.tokensToMatch[i] = x == -1 ? null : tokenTypes[x];\n")
				.deindent()
				.append("}\n")
				.append("for (int i = 0; i < num_states + 1; i++) {\n")
				.indent()
				.append("this.offsets[i] = bb.getInt();\n")
				.deindent()
				.append("}\n")
				.append("for (int i = 0; i < num_destinations; i++) {\n")
				.indent()
				.append("this.symbols[i] = bb.getChar();\n")
				.deindent()
				.append("}\n")
				.append("for (int i = 0; i < num_destinations; i++) {\n")
				.indent()
				.append("this.destinations[i] = bb.getInt();\n")
				.deindent()
				.append("}\n")
				.deindent()
				.append("}\n")
				// TODO: this can be optimized to a binary search
				.append("private int transition(final int currentState, final char symbol) {\n")
				.indent()
				.append("final int start = this.offsets[currentState];\n")
				.append("final int end = this.offsets[currentState + 1];\n")
				.append("for (int i = start; i < end; i++) {\n")
				.indent()
				.append("if (this.symbols[i] == symbol) {\n")
				.indent()
				.append("return this.destinations[i];\n")
				.deindent()
				.append("}\n")
				.deindent()
				.append("}\n")
				.append("return -1;\n")
				.deindent()
				.append("}\n")
				.append("public List<Token> tokenize(final String input) {\n")
				.indent()
				.append("final char[] v = input.toCharArray();\n")
				.append("int pos = 0;\n")
				.append("int lastTokenMatchStart = 0;\n")
				.append("int lastTokenMatchEnd = 0;\n")
				.append("final List<Token> tokens = new ArrayList<>();\n")
				.append("int currentState = 0;\n")
				.append("while (pos < v.length) {\n")
				.indent()
				.append("if (isAccepting[currentState]) {\n")
				.indent()
				.append("lastTokenMatchEnd = pos;\n")
				.deindent()
				.append("}\n")
				.append("final char ch = v[pos];\n")
				.append("final int nextState = transition(currentState, ch);\n")
				.append("if (nextState != -1) {\n")
				.indent()
				.append("currentState = nextState;\n")
				.append("pos++;\n")
				.deindent()
				.append("} else {\n")
				.indent()
				.append("if (isAccepting[currentState]) {\n")
				.indent()
				.append("final int length = lastTokenMatchEnd - lastTokenMatchStart;\n")
				.append("if (length == 0) {\n")
				.indent()
				.append(
						"throw new IllegalArgumentException(String.format(\"No token emitted for empty match at index %,d.\", pos));\n")
				.deindent()
				.append("}\n")
				.append("if (!isSkippable[currentState]) {\n")
				.indent()
				.append("final String match = String.copyValueOf(v, lastTokenMatchStart, length);\n")
				.append("tokens.add(new Token(tokensToMatch[currentState], match));\n")
				.deindent()
				.append("}\n")
				.append("lastTokenMatchStart = pos;\n")
				.append("lastTokenMatchEnd = -1;\n")
				.append("currentState = 0;\n")
				.deindent()
				.append("} else {\n")
				.indent()
				.append("throw new IllegalArgumentException(String.format(\"Lexical error at index %,d.\", pos));\n")
				.deindent()
				.append("}\n")
				.deindent()
				.append("}\n")
				.deindent()
				.append("}\n")
				.append("if (isAccepting[currentState]) {\n")
				.indent()
				.append("lastTokenMatchEnd = pos;\n")
				.deindent()
				.append("}\n")
				.append("final int length = lastTokenMatchEnd - lastTokenMatchStart;\n")
				.append("if (isAccepting[currentState] && length > 0 && !isSkippable[currentState]) {\n")
				.indent()
				.append("final String match = String.copyValueOf(v, lastTokenMatchStart, length);\n")
				.append("tokens.add(new Token(tokensToMatch[currentState], match));\n")
				.deindent()
				.append("}\n")
				.append("return tokens;\n")
				.deindent()
				.append("}\n")
				.deindent()
				.append("}\n");
	}
}
