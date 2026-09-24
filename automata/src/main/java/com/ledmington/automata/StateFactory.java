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

/**
 * A factory class for generating new states. This class provides methods to create regular states and accepting states,
 * each with a unique name.
 */
public final class StateFactory {

	private int stateID = 1;

	/** Constructs a new StateFactory. */
	public StateFactory() {}

	/**
	 * Creates a new regular state with a unique name.
	 *
	 * @return a new regular state with a unique name
	 */
	public State getNewState() {
		final String newStateName = "S" + stateID;
		stateID++;
		return new State(newStateName);
	}

	/**
	 * Creates a new accepting state with a unique name and the given production name.
	 *
	 * @param productionName the production name associated with the accepting state
	 * @return a new accepting state with a unique name and the provided production name
	 */
	public AcceptingState getNewAcceptingState(final String productionName) {
		final String newStateName = "S" + stateID;
		stateID++;
		return new AcceptingState(newStateName, productionName);
	}
}
