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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledmington.utils.GraphUtils;

/** A class to convert an NFA into a DFA. */
public final class NFAToDFA {

	private final StateFactory stateFactory;

	/**
	 * Constructs an NFAToDFA with the specified StateFactory.
	 *
	 * @param factory the StateFactory to be used for creating states
	 */
	public NFAToDFA(final StateFactory factory) {
		this.stateFactory = Objects.requireNonNull(factory);
	}

	/** Constructs an NFAToDFA with a default StateFactory. */
	public NFAToDFA() {
		this(new StateFactory());
	}

	/**
	 * Converts the given NFA into a DFA.
	 *
	 * @param nfa The NFA to be converted.
	 * @return A new DFA.
	 */
	public DFA convert(final NFA nfa) {
		final DFABuilder builder = DFA.builder();
		final Map<String, Integer> priorities = nfa.priorities();

		final Set<Character> alphabet = nfa.states().stream()
				.flatMap(s -> {
					final Map<Character, Set<State>> m = nfa.neighbors(s);
					return m == null ? Stream.of() : m.keySet().stream();
				})
				.collect(Collectors.toUnmodifiableSet());

		final Set<State> startSet = Set.of(nfa.startingState());

		final State dfaStartState = createDFAState(startSet, priorities);

		final Map<Set<State>, State> stateMapping = new HashMap<>();
		stateMapping.put(startSet, dfaStartState);

		GraphUtils.bfs(
				startSet,

				// compute reachable DFA states
				currentSet -> {
					final Set<Set<State>> nextSets = new HashSet<>();

					for (final char symbol : alphabet) {
						final Set<State> moveSet = move(currentSet, symbol, nfa);
						if (!moveSet.isEmpty()) {
							nextSets.add(moveSet);
						}
					}

					return nextSets;
				},

				// build DFA transitions
				currentSet -> {
					final State fromDFAState = stateMapping.get(currentSet);

					for (final char symbol : alphabet) {
						final Set<State> moveSet = move(currentSet, symbol, nfa);
						if (moveSet.isEmpty()) {
							continue;
						}

						final State toDFAState =
								stateMapping.computeIfAbsent(moveSet, s -> createDFAState(s, priorities));

						builder.addTransition(fromDFAState, symbol, toDFAState);
					}
				});

		return builder.start(dfaStartState).build();
	}

	private State createDFAState(final Set<State> states, final Map<String, Integer> priorities) {
		final boolean accept = states.stream().anyMatch(State::isAccepting);

		if (!accept) {
			return stateFactory.getNewState();
		}

		final AcceptingState best = (AcceptingState) states.stream()
				.filter(State::isAccepting)
				.min(Comparator.comparing(
						s -> priorities.getOrDefault(((AcceptingState) s).tokenName(), Integer.MAX_VALUE)))
				.orElseThrow();

		return stateFactory.getNewAcceptingState(best.tokenName());
	}

	private Set<State> move(final Set<State> states, final char symbol, final NFA nfa) {
		final Set<State> result = new HashSet<>();
		for (final State s : states) {
			final Map<Character, Set<State>> neighbors = nfa.neighbors(s);
			if (neighbors == null) {
				continue;
			}
			if (neighbors.containsKey(symbol)) {
				result.addAll(neighbors.get(symbol));
			}
		}
		return result;
	}
}
