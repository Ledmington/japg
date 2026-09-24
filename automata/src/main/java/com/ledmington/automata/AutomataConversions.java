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

import java.util.List;

import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Production;

/** A collection of conversions from an EBNF grammar into various types of automata. */
public final class AutomataConversions {

	private AutomataConversions() {}

	/**
	 * Converts a given grammar to an epsilon NFA.
	 *
	 * @param g the grammar to be converted
	 * @return the corresponding epsilon NFA
	 */
	public static NFA convertGrammarToEpsilonNFA(final Grammar g) {
		final StateFactory stateFactory = new StateFactory();
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA(stateFactory);
		return g2enfa.convert(g);
	}

	/**
	 * Converts a given grammar to a standard NFA by first converting it to an epsilon NFA and then removing epsilon
	 * transitions.
	 *
	 * @param g the grammar to be converted
	 * @return the corresponding NFA
	 */
	public static NFA convertGrammarToNFA(final Grammar g) {
		final StateFactory stateFactory = new StateFactory();
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA(stateFactory);
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA(stateFactory);
		return enfa2nfa.convert(g2enfa.convert(g));
	}

	/**
	 * Converts a given grammar to a DFA by first converting it to an epsilon NFA, then to a standard NFA, and finally
	 * to a DFA.
	 *
	 * @param g the grammar to be converted
	 * @return the corresponding DFA
	 */
	public static DFA convertGrammarToDFA(final Grammar g) {
		final StateFactory stateFactory = new StateFactory();
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA(stateFactory);
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA(stateFactory);
		final NFAToDFA nfa2dfa = new NFAToDFA(stateFactory);
		return nfa2dfa.convert(enfa2nfa.convert(g2enfa.convert(g)));
	}

	/**
	 * Converts a list of productions to a DFA by first converting it to an epsilon NFA, then to a standard NFA, and
	 * finally to a DFA.
	 *
	 * @param productions the list of productions to be converted
	 * @return the corresponding DFA
	 */
	public static DFA convertGrammarToDFA(final List<Production> productions) {
		final StateFactory stateFactory = new StateFactory();
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA(stateFactory);
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA(stateFactory);
		final NFAToDFA nfa2dfa = new NFAToDFA(stateFactory);
		return nfa2dfa.convert(enfa2nfa.convert(g2enfa.convert(productions)));
	}

	/**
	 * Converts a given grammar to a minimized DFA by first converting it to an epsilon NFA, then to a standard NFA,
	 * followed by a DFA, and finally minimizing the DFA.
	 *
	 * @param g the grammar to be converted
	 * @return the minimized DFA
	 */
	public static DFA convertGrammarToMinimizedDFA(final Grammar g) {
		final StateFactory stateFactory = new StateFactory();
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA(stateFactory);
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA(stateFactory);
		final NFAToDFA nfa2dfa = new NFAToDFA(stateFactory);
		final DFAMinimizer DFAmin = new DFAMinimizer(stateFactory);
		return DFAmin.minimize(nfa2dfa.convert(enfa2nfa.convert(g2enfa.convert(g))));
	}

	/**
	 * Converts a list of productions to a minimized DFA by first converting it to an epsilon NFA, then to a standard
	 * NFA, followed by a DFA, and finally minimizing the DFA.
	 *
	 * @param productions the list of productions to be converted
	 * @return the minimized DFA
	 */
	public static DFA convertGrammarToMinimizedDFA(final List<Production> productions) {
		final StateFactory stateFactory = new StateFactory();
		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA(stateFactory);
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA(stateFactory);
		final NFAToDFA nfa2dfa = new NFAToDFA(stateFactory);
		final DFAMinimizer DFAmin = new DFAMinimizer(stateFactory);
		return DFAmin.minimize(nfa2dfa.convert(enfa2nfa.convert(g2enfa.convert(productions))));
	}
}
