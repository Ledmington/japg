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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Implementation of a deterministic finite-state automaton. */
public final class DFAImpl implements DFA {

	private final State start;
	private final Map<State, Map<Character, State>> transitions;

	/**
	 * Creates a new DFA with the given starting state and the given transitions.
	 *
	 * @param startingState The starting state.
	 * @param transitions The map of transitions of the automaton.
	 */
	public DFAImpl(final State startingState, final Map<State, Map<Character, State>> transitions) {
		this.start = Objects.requireNonNull(startingState);
		this.transitions = Objects.requireNonNull(transitions);
	}

	@Override
	public State startingState() {
		return start;
	}

	@Override
	public Set<State> states() {
		final Set<State> allStates = new HashSet<>();
		for (final Map.Entry<State, Map<Character, State>> e : transitions.entrySet()) {
			allStates.add(e.getKey());
			allStates.addAll(e.getValue().values());
		}
		return allStates;
	}

	@Override
	public Map<Character, State> neighbors(final State s) {
		return transitions.containsKey(s) ? transitions.get(s) : Map.of();
	}

	@Override
	public String toString() {
		return "DFA[start=" + start + ";transitions=" + transitions + "]";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + start.hashCode();
		h = 31 * h + transitions.hashCode();
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final DFAImpl dfa)) {
			return false;
		}
		return this.start.equals(dfa.start) && this.transitions.equals(dfa.transitions);
	}
}
