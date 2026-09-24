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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A class to minimize a given DFA with the Myhill-Nerode algorithm. */
public class DFAMinimizer {

	private final StateFactory stateFactory;

	/**
	 * Constructs a DFAMinimizer with the specified StateFactory.
	 *
	 * @param factory the StateFactory to be used for creating states
	 */
	public DFAMinimizer(final StateFactory factory) {
		this.stateFactory = Objects.requireNonNull(factory);
	}

	/** Constructs a DFAMinimizer with a default StateFactory. */
	public DFAMinimizer() {
		this(new StateFactory());
	}

	/**
	 * Converts the given DFA into a minimized DFA.
	 *
	 * @param dfa The DFA to be minimized.
	 * @return A new minimized DFA.
	 */
	public DFA minimize(final DFA dfa) {
		// Myhill-Nerode theorem
		final DFABuilder builder = DFA.builder();
		final List<State> oldStates = dfa.states().stream().toList();
		final Map<State, Integer> oldStatesIndex =
				IntStream.range(0, oldStates.size()).boxed().collect(Collectors.toMap(oldStates::get, i -> i));
		final int n = oldStates.size();
		final boolean[][] isDistinguishable = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			final State a = oldStates.get(i);
			for (int j = i + 1; j < n; j++) {
				final State b = oldStates.get(j);
				// any accepting state is trivially distinguishable from any non-accepting one
				if (a.isAccepting() != b.isAccepting()) {
					isDistinguishable[i][j] = true;
					isDistinguishable[j][i] = true;
				} else
				// two accepting states are trivially distinguishable if they refer to different tokens
				if (a.isAccepting()
						&& b.isAccepting()
						&& !((AcceptingState) a).tokenName().equals(((AcceptingState) b).tokenName())) {
					isDistinguishable[i][j] = true;
					isDistinguishable[j][i] = true;
				}
			}
		}

		boolean atLeastOnePairDistinguished;
		do {
			atLeastOnePairDistinguished = false;
			for (int i = 0; i < n; i++) {
				final State p = oldStates.get(i);
				final Map<Character, State> px = dfa.neighbors(p);
				if (px == null) {
					continue;
				}

				for (int j = i + 1; j < n; j++) {
					if (isDistinguishable[i][j]) {
						continue;
					}

					final State q = oldStates.get(j);
					final Map<Character, State> qx = dfa.neighbors(q);
					if (qx == null) {
						continue;
					}

					if (!px.keySet().equals(qx.keySet())) {
						isDistinguishable[i][j] = true;
						isDistinguishable[j][i] = true;
						atLeastOnePairDistinguished = true;
						continue;
					}
					for (final char x : px.keySet()) {
						final int pxx = oldStatesIndex.get(px.get(x));
						final int qxx = oldStatesIndex.get(qx.get(x));
						if (isDistinguishable[pxx][qxx]) {
							isDistinguishable[i][j] = true;
							isDistinguishable[j][i] = true;
							atLeastOnePairDistinguished = true;
							break;
						}
					}
				}
			}
		} while (atLeastOnePairDistinguished);

		// Create group of equivalent states
		final Map<State, Set<State>> equivalentGroups = new HashMap<>();
		for (int i = 0; i < n; i++) {
			final State a = oldStates.get(i);
			equivalentGroups.putIfAbsent(a, new HashSet<>(Set.of(a)));
			for (int j = i + 1; j < n; j++) {
				final State b = oldStates.get(j);
				if (!isDistinguishable[i][j]) {
					equivalentGroups.get(a).add(b);
					equivalentGroups.put(b, equivalentGroups.get(a));
				}
			}
		}

		// Create a new state for each equivalent state
		final Map<State, State> oldToNew = new HashMap<>();
		for (final Set<State> eqClass : new HashSet<>(equivalentGroups.values())) {
			final boolean isAccept = eqClass.stream().anyMatch(State::isAccepting);
			final State rep = isAccept
					? stateFactory.getNewAcceptingState(((AcceptingState) eqClass.stream()
									.filter(State::isAccepting)
									.findFirst()
									.orElseThrow())
							.tokenName())
					: stateFactory.getNewState();
			for (final State s : eqClass) {
				oldToNew.put(s, rep);
			}
		}

		// Add transitions
		for (final State src : oldStates) {
			final Map<Character, State> neighbors = dfa.neighbors(src);
			if (neighbors == null) {
				continue;
			}
			for (final Map.Entry<Character, State> e : neighbors.entrySet()) {
				builder.addTransition(oldToNew.get(src), e.getKey(), oldToNew.get(e.getValue()));
			}
		}

		final State newStart = oldToNew.get(dfa.startingState());

		return builder.start(newStart).build();
	}
}
