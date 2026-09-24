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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** A builder for easy creation of an NFA. */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class NFABuilder {

	private State startingState = null;
	private final Map<State, Map<Character, Set<State>>> transitions = new HashMap<>();
	private Map<String, Integer> priorities = null;

	/** Creates a new NFABuilder with no starting state and no transitions. */
	public NFABuilder() {}

	/**
	 * Adds a new transition from the state "from" to the state "to" with the symbol "symbol".
	 *
	 * @param from The source state of the new transition.
	 * @param symbol THe character of the new transition.
	 * @param to The destination state of the new transition.
	 */
	public void addTransition(final State from, final char symbol, final State to) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		if (!transitions.containsKey(from)) {
			transitions.put(from, new HashMap<>());
		}
		final Map<Character, Set<State>> m = transitions.get(from);
		if (!m.containsKey(symbol)) {
			m.put(symbol, new HashSet<>());
		}
		m.get(symbol).add(to);
	}

	/**
	 * Returns the "set" of neighbors of the given state.
	 *
	 * @param s The state the transitions start from.
	 * @return The "set" of neighbors.
	 */
	public Map<Character, Set<State>> neighbors(final State s) {
		return transitions.get(s);
	}

	/**
	 * Sets the given state as the starting state.
	 *
	 * @param startingState The new starting state.
	 * @return This instance of NFABuilder.
	 */
	public NFABuilder start(final State startingState) {
		if (this.startingState != null) {
			throw new IllegalArgumentException("Cannot set starting state twice.");
		}
		this.startingState = Objects.requireNonNull(startingState);
		return this;
	}

	/**
	 * Allows to set the map of token priorities of this automaton.
	 *
	 * @param priorities The map of priorities.
	 * @return This instance of NFABuilder.
	 */
	public NFABuilder priorities(final Map<String, Integer> priorities) {
		if (this.priorities != null) {
			throw new IllegalArgumentException("Cannot set priorities twice.");
		}
		this.priorities = priorities;
		return this;
	}

	/**
	 * Creates a new NFA with the data contained.
	 *
	 * @return A new NFA.
	 */
	public NFA build() {
		return new NFAImpl(startingState, transitions, priorities == null ? Map.of() : priorities);
	}

	/**
	 * Removes all the given states and any transition related to them.
	 *
	 * @param unreachableStates The set of states to be removed.
	 */
	public void removeStates(final Set<State> unreachableStates) {
		for (final State s : unreachableStates) {
			transitions.remove(s);
		}
		for (final State s : transitions.keySet()) {
			for (final char symbol : transitions.get(s).keySet()) {
				transitions.get(s).get(symbol).removeAll(unreachableStates);
			}
		}
	}
}
