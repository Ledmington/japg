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
package com.ledmington.automata;

import static com.ledmington.automata.CorrectGrammars.TEST_CASES;
import static com.ledmington.automata.CorrectGrammars.one_or_more;
import static com.ledmington.automata.CorrectGrammars.or;
import static com.ledmington.automata.CorrectGrammars.p;
import static com.ledmington.automata.CorrectGrammars.t;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Utils;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TestAutomata {

	private TestAutomata() {}

	public static Stream<Arguments> onlyGrammars() {
		return TEST_CASES.stream().map(tc -> Arguments.of(tc.get()[0]));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void checkValidEpsilonNFA(final Grammar g) {
		final NFA epsilonNFA = AutomataConversions.convertGrammarToEpsilonNFA(g);
		assertDoesNotThrow(
				() -> AutomataUtils.assertEpsilonNFAValid(epsilonNFA),
				() -> String.format(
						"Expected this automaton to be valid but it wasn't:\n%s\n", epsilonNFA.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void checkValidNFA(final Grammar g) {
		final NFA nfa = AutomataConversions.convertGrammarToNFA(g);
		assertDoesNotThrow(
				() -> AutomataUtils.assertNFAValid(nfa),
				() -> String.format("Expected this automaton to be valid but it wasn't:\n%s\n", nfa.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void checkValidDFA(final Grammar g) {
		final DFA dfa = AutomataConversions.convertGrammarToDFA(g);
		assertDoesNotThrow(
				() -> AutomataUtils.assertDFAValid(dfa),
				() -> String.format("Expected this automaton to be valid but it wasn't:\n%s\n", dfa.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void checkValidMinimizedDFA(final Grammar g) {
		final DFA minimizedDFA = AutomataConversions.convertGrammarToMinimizedDFA(g);
		assertDoesNotThrow(
				() -> AutomataUtils.assertDFAValid(minimizedDFA),
				() -> String.format(
						"Expected this automaton to be valid but it wasn't:\n%s\n", minimizedDFA.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void epsilonNFADeterministicGeneration(final Grammar g) {
		final NFA epsilonNFA1 = AutomataConversions.convertGrammarToEpsilonNFA(g);
		final NFA epsilonNFA2 = AutomataConversions.convertGrammarToEpsilonNFA(g);
		assertEquals(
				epsilonNFA1,
				epsilonNFA2,
				() -> String.format(
						"Expected epsilon-NFA generation to be deterministic, but the following grammar generated two different automata.%n%s%n%s%n%s%n",
						Utils.prettyPrint(g), epsilonNFA1.toGraphviz(), epsilonNFA2.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void nfaDeterministicGeneration(final Grammar g) {
		final NFA nfa1 = AutomataConversions.convertGrammarToNFA(g);
		final NFA nfa2 = AutomataConversions.convertGrammarToNFA(g);
		assertEquals(
				nfa1,
				nfa2,
				() -> String.format(
						"Expected NFA generation to be deterministic, but the following grammar generated two different automata.%n%s%n%s%n%s%n",
						Utils.prettyPrint(g), nfa1.toGraphviz(), nfa2.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void dfaDeterministicGeneration(final Grammar g) {
		final DFA dfa1 = AutomataConversions.convertGrammarToDFA(g);
		final DFA dfa2 = AutomataConversions.convertGrammarToDFA(g);
		assertEquals(
				dfa1,
				dfa2,
				() -> String.format(
						"Expected DFA generation to be deterministic, but the following grammar generated two different automata.%n%s%n%s%n%s%n",
						Utils.prettyPrint(g), dfa1.toGraphviz(), dfa2.toGraphviz()));
	}

	@ParameterizedTest
	@MethodSource("onlyGrammars")
	void minimizedDFADeterministicGeneration(final Grammar g) {
		final DFA minimizedDFA1 = AutomataConversions.convertGrammarToMinimizedDFA(g);
		final DFA minimizedDFA2 = AutomataConversions.convertGrammarToMinimizedDFA(g);
		assertEquals(
				minimizedDFA1,
				minimizedDFA2,
				() -> String.format(
						"Expected min-DFA generation to be deterministic, but the following grammar generated two different automata.%n%s%n%s%n%s%n",
						Utils.prettyPrint(g), minimizedDFA1.toGraphviz(), minimizedDFA2.toGraphviz()));
	}

	private record Match(String name, String content) {}

	List<Match> tryMatch(final DFA dfa, final String input) {
		final char[] v = input.toCharArray();
		int pos = 0;
		State currentState = dfa.startingState();
		final List<Match> matches = new ArrayList<>();
		int lastMatchStart = 0;
		int lastMatchEnd = 0;
		while (pos < v.length) {
			if (currentState.isAccepting()) {
				lastMatchEnd = pos;
			}
			final char ch = v[pos];
			if (dfa.neighbors(currentState).containsKey(ch)) {
				currentState = dfa.neighbors(currentState).get(ch);
				pos++;
			} else {
				// no more transitions, emit last match if present
				if (!currentState.isAccepting()) {
					throw new IllegalArgumentException("No match.");
				} else {
					final int length = lastMatchEnd - lastMatchStart;
					if (length == 0) {
						throw new IllegalArgumentException("Empty match.");
					}
					final AcceptingState as = (AcceptingState) currentState;
					matches.add(new Match(as.tokenName(), String.copyValueOf(v, lastMatchStart, length)));
					lastMatchStart = pos;
					lastMatchEnd = -1;
					currentState = dfa.startingState();
				}
			}
		}
		if (currentState.isAccepting()) {
			lastMatchEnd = pos;
		}
		final int length = lastMatchEnd - lastMatchStart;
		if (currentState.isAccepting() && length > 0) {
			final AcceptingState as = (AcceptingState) currentState;
			matches.add(new Match(as.tokenName(), String.copyValueOf(v, lastMatchStart, length)));
		}
		return matches;
	}

	@Test
	void checkDFAMatchesSubTokens() {
		final List<Production> productions =
				List.of(p("AN", t("an")), p("ID", one_or_more(or(t("a"), t("b"), t("n")))));
		final DFA dfa = AutomataConversions.convertGrammarToDFA(productions);
		final List<Match> tokens = tryMatch(dfa, "banana");
		assertEquals(List.of(new Match("ID", "banana")), tokens);
	}

	@Test
	void checkMinimizedDFAMatchesSubTokens() {
		final List<Production> productions =
				List.of(p("AN", t("an")), p("ID", one_or_more(or(t("a"), t("b"), t("n")))));
		final DFA dfa = AutomataConversions.convertGrammarToMinimizedDFA(productions);
		final List<Match> tokens = tryMatch(dfa, "banana");
		assertEquals(List.of(new Match("ID", "banana")), tokens);
	}
}
