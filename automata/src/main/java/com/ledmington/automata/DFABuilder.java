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
import java.util.Map;
import java.util.Objects;

/** A builder for easy creation of a DFA. */
public final class DFABuilder {

	private State startingState = null;
	private final Map<State, Map<Character, State>> transitions = new HashMap<>();

	/** Creates a new DFABuilder with no starting state and no transitions. */
	public DFABuilder() {}

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
		transitions.get(from).put(symbol, to);
	}

	/**
	 * Sets the given state as the starting state.
	 *
	 * @param startingState The new starting state.
	 * @return This instance of DFABuilder.
	 */
	public DFABuilder start(final State startingState) {
		if (this.startingState != null) {
			throw new IllegalArgumentException("Cannot set starting state twice.");
		}
		this.startingState = Objects.requireNonNull(startingState);
		return this;
	}

	/**
	 * Creates a new DFA with the data contained.
	 *
	 * @return A new DFA.
	 */
	public DFA build() {
		return new DFAImpl(startingState, transitions);
	}
}
