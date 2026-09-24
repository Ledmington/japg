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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Common interface for non-deterministic finite-state automata. */
public interface NFA extends Automaton {

	/** The symbol of an epsilon transition. */
	char EPSILON = (char) -1;

	/**
	 * Returns a new NFABuilder.
	 *
	 * @return A new NcFABuilder.
	 */
	static NFABuilder builder() {
		return new NFABuilder();
	}

	/**
	 * Returns the "set" of neighbors of the given state.
	 *
	 * @param s The state the transitions start from.
	 * @return The "set" of neighbors.
	 */
	Map<Character, Set<State>> neighbors(final State s);

	default String toGraphviz() {
		final Set<State> allStates = states();
		final StringBuilder sb = new StringBuilder();

		sb.append("digraph Automaton {\n");
		sb.append("    rankdir=LR;\n");
		sb.append("    node [shape = doublecircle];\n");

		for (final State s : allStates) {
			if (s.isAccepting()) {
				sb.append("    ").append(s.name()).append(";\n");
			}
		}

		sb.append("    node [shape = circle];\n");
		sb.append("    __start__ [shape = point];\n");
		sb.append("    __start__ -> ").append(startingState().name()).append(";\n");

		final Function<Character, String> escape = ch -> switch (ch) {
			case NFA.EPSILON -> "ε";
			case ' ' -> "\\\\s";
			case '\t' -> "\\\\t";
			case '\n' -> "\\\\n";
			case '"' -> "\\\"";
			case '\\' -> "\\\\";
			default -> "" + ch;
		};

		for (final State src : allStates) {
			final Map<Character, Set<State>> neighbors = neighbors(src);
			if (neighbors == null) {
				continue;
			}
			for (final Map.Entry<Character, Set<State>> e : neighbors.entrySet()) {
				final char symbol = e.getKey();
				for (final State dst : e.getValue()) {
					sb.append("    ")
							.append(src.name())
							.append(" -> ")
							.append(dst.name())
							.append(" [label=\"")
							.append(escape.apply(symbol))
							.append("\"];\n");
				}
			}
		}

		sb.append("}\n");
		return sb.toString();
	}

	/**
	 * Returns the map of token priorities of this automaton.
	 *
	 * @return The map of token priorities of this automaton.
	 */
	Map<String, Integer> priorities();
}
