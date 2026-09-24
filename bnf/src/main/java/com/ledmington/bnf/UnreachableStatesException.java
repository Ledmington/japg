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
package com.ledmington.bnf;

import java.io.Serial;

/**
 * The proper RuntimeException for a case in which the start symbol of a given grammar can not produce ('reach') all
 * other non-terminal symbols.
 */
public final class UnreachableStatesException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 8143650245287905337L;

	/**
	 * Creates a new UnreachableStatesException with a pre-defined message.
	 *
	 * @param startSymbol The start symbol of the grammar.
	 */
	public UnreachableStatesException(final BNFNonTerminal startSymbol) {
		super(String.format("The start symbol '%s' can not reach all other symbols.", startSymbol.name()));
	}
}
