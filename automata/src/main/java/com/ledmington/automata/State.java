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

import java.util.Objects;

/** A state of a finite-state automaton. */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public sealed class State permits AcceptingState {

	/** The name of this state. */
	protected final String name;

	/**
	 * Creates a new state with the given name.
	 *
	 * @param name The name of this state.
	 */
	public State(final String name) {
		Objects.requireNonNull(name);
		if (name.isBlank()) {
			throw new IllegalArgumentException("Empty name.");
		}
		this.name = name;
	}

	/**
	 * Returns the name of this state.
	 *
	 * @return The name of this state.
	 */
	public String name() {
		return name;
	}

	/**
	 * Tells whether this State is accepting or not.
	 *
	 * @return True if this state is accepting, false otherwise.
	 */
	public boolean isAccepting() {
		return false;
	}

	@Override
	public String toString() {
		return "State[name=" + name + "]";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
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
		if (!(other instanceof State s)) {
			return false;
		}
		return this.name.equals(s.name);
	}
}
