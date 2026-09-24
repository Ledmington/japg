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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Node;
import com.ledmington.ebnf.NonTerminal;
import com.ledmington.ebnf.OneOrMore;
import com.ledmington.ebnf.Or;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Sequence;
import com.ledmington.ebnf.Terminal;
import com.ledmington.ebnf.ZeroOrMore;
import com.ledmington.ebnf.ZeroOrOne;

/** A class to convert an EBNF grammar into an epsilon NFA. */
public final class GrammarToEpsilonNFA {

	private final StateFactory stateFactory;
	private final List<Production> lexerProductions = new ArrayList<>();
	private final Map<String, Integer> priorities = new HashMap<>();
	private NFABuilder builder = null;

	/**
	 * Constructs a GrammarToEpsilonNFA with the specified StateFactory.
	 *
	 * @param factory the StateFactory to be used for creating states
	 */
	public GrammarToEpsilonNFA(final StateFactory factory) {
		this.stateFactory = Objects.requireNonNull(factory);
	}

	/** Constructs a GrammarToEpsilonNFA with a default StateFactory. */
	public GrammarToEpsilonNFA() {
		this(new StateFactory());
	}

	private void reset() {
		this.lexerProductions.clear();
		this.builder = NFA.builder();
	}

	/**
	 * Converts the given grammar into an epsilon-NFA by first extracting the lexer productions.
	 *
	 * @param g The grammar to be converted.
	 * @return A new epsilon-NFA representing all the lexer productions of the grammar combined.
	 */
	public NFA convert(final Grammar g) {
		reset();

		Grammar.splitProductions(g.getProductions(), this.lexerProductions, new ArrayList<>());

		for (int i = 0; i < g.getProductions().size(); i++) {
			this.priorities.put(g.getProductions().get(i).start().name(), i + 1);
		}

		return convertGrammarToEpsilonNFA();
	}

	/**
	 * Converts the given list of token productions into an epsilon-NFA by deducing the priorities from the order.
	 *
	 * @param lexerProductions The list of token productions ordered by priority.
	 * @return A new epsilon-NFA representing all the lexer productions of the grammar combined.
	 */
	public NFA convert(final List<Production> lexerProductions) {
		reset();

		this.lexerProductions.addAll(lexerProductions);

		for (int i = 0; i < lexerProductions.size(); i++) {
			this.priorities.put(lexerProductions.get(i).start().name(), i + 1);
		}
		return convertGrammarToEpsilonNFA();
	}

	private NFA convertGrammarToEpsilonNFA() {
		final State globalStart = stateFactory.getNewState();

		for (final Production p : lexerProductions) {
			final String productionName = p.start().name();
			final State productionStart = stateFactory.getNewState();
			final State productionEnd = stateFactory.getNewAcceptingState(productionName);

			builder.addTransition(globalStart, NFA.EPSILON, productionStart);

			convertNode(p.result(), productionStart, productionEnd);
		}

		return builder.start(globalStart).priorities(priorities).build();
	}

	private void convertNode(final Node n, final State start, final State end) {
		switch (n) {
			case Terminal t -> convertTerminal(t, start, end);
			case NonTerminal nt -> {
				final Node referenced = lexerProductions.stream()
						.filter(p -> p.start().name().equals(nt.name()))
						.findFirst()
						.orElseThrow()
						.result();
				// for each non-terminal, we just copy the whole sub-automaton
				convertNode(referenced, start, end);
			}
			case ZeroOrOne zoo -> convertZeroOrOne(zoo, start, end);
			case ZeroOrMore zom -> convertZeroOrMore(zom, start, end);
			case OneOrMore oom -> convertOneOrMore(oom, start, end);
			case Or a -> convertAlternation(a, start, end);
			case Sequence s -> convertSequence(s, start, end);
			default -> throw new IllegalArgumentException(String.format("Unknown node '%s'.", n));
		}
	}

	private void convertOneOrMore(final OneOrMore oom, final State start, final State end) {
		final State a = stateFactory.getNewState();
		final State b = stateFactory.getNewState();

		builder.addTransition(start, NFA.EPSILON, a);
		convertNode(oom.inner(), a, b);
		builder.addTransition(b, NFA.EPSILON, a);
		builder.addTransition(b, NFA.EPSILON, end);
	}

	private void convertZeroOrMore(final ZeroOrMore zom, final State start, final State end) {
		final State a = stateFactory.getNewState();
		final State b = stateFactory.getNewState();

		builder.addTransition(start, NFA.EPSILON, end);
		builder.addTransition(start, NFA.EPSILON, a);
		convertNode(zom.inner(), a, b);
		builder.addTransition(b, NFA.EPSILON, a);
		builder.addTransition(b, NFA.EPSILON, end);
	}

	private void convertSequence(final Sequence s, final State start, final State end) {
		State prev = stateFactory.getNewState();
		builder.addTransition(start, NFA.EPSILON, prev);
		for (int i = 0; i < s.expressions().size(); i++) {
			final State tmp = stateFactory.getNewState();
			convertNode(s.expressions().get(i), prev, tmp);
			prev = tmp;
		}
		builder.addTransition(prev, NFA.EPSILON, end);
	}

	private void convertZeroOrOne(final ZeroOrOne zoo, final State start, final State end) {
		final State a = stateFactory.getNewState();
		final State b = stateFactory.getNewState();

		builder.addTransition(start, NFA.EPSILON, end);
		builder.addTransition(start, NFA.EPSILON, a);
		convertNode(zoo.inner(), a, b);
		builder.addTransition(b, NFA.EPSILON, end);
	}

	private void convertAlternation(final Or or, final State start, final State end) {
		for (final Node n : or.expressions()) {
			final State newStart = stateFactory.getNewState();
			final State newEnd = stateFactory.getNewState();
			builder.addTransition(start, NFA.EPSILON, newStart);
			builder.addTransition(newEnd, NFA.EPSILON, end);
			convertNode(n, newStart, newEnd);
		}
	}

	private void convertTerminal(final Terminal t, final State start, final State end) {
		State prev = stateFactory.getNewState();
		builder.addTransition(start, NFA.EPSILON, prev);
		for (int i = 0; i < t.literal().length(); i++) {
			final State s = stateFactory.getNewState();
			builder.addTransition(prev, t.literal().charAt(i), s);
			prev = s;
		}
		builder.addTransition(prev, NFA.EPSILON, end);
	}
}
