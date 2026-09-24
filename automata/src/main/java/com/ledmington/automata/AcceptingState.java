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

/** A finite-automaton which is accepting. */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class AcceptingState extends State {

	private final String tokenName;

	/**
	 * Creates a new AcceptingState with the given name and the token it represents.
	 *
	 * @param stateName The name of this state.
	 * @param tokenName The name of the represented token.
	 */
	public AcceptingState(final String stateName, final String tokenName) {
		super(stateName);
		Objects.requireNonNull(tokenName);
		if (tokenName.isBlank()) {
			throw new IllegalArgumentException("Empty token stateName.");
		}
		this.tokenName = tokenName;
	}

	@Override
	public boolean isAccepting() {
		return true;
	}

	/**
	 * Retusn the name of the token this accepting state refers to.
	 *
	 * @return The name of the token this accepting state refers to.
	 */
	public String tokenName() {
		return tokenName;
	}

	@Override
	public String toString() {
		return "AcceptingState[name=" + name + ";tokenName=" + tokenName + "]";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + super.hashCode();
		h = 31 * h + tokenName.hashCode();
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
		if (!(other instanceof AcceptingState as)) {
			return false;
		}
		return this.name.equals(as.name) && this.tokenName.equals(as.tokenName);
	}
}
