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

import java.util.Set;

/** Common interface for finite-state automata. */
public interface Automaton {

	/**
	 * Returns the starting state of the automaton.
	 *
	 * @return The starting state.
	 */
	State startingState();

	/**
	 * Returns the set of all states of this automaton.
	 *
	 * @return The set of all states.
	 */
	Set<State> states();

	/**
	 * Returns a string graphviz representation of this automaton. Useful for debugging when visualized through tools
	 * such as <a href="https://graph.flyte.org">this</a>.
	 *
	 * @return A string graphviz representation of this automaton.
	 */
	String toGraphviz();
}
