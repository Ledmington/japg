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
package com.ledmington.ebnf;

import java.util.Objects;

/**
 * An element of the EBNF grammar which represents a symbol which cannot be expanded into other symbols.
 *
 * @param literal The content of the terminal symbol.
 * @param isSynthetic {@code true} if the literal of this terminal symbol does not actually appear in the grammar. Used
 *     for 'epsilon' and '$'.
 */
// TODO: remove isSynthetic and handle it with a new SyntheticTerminal class
public record Terminal(String literal, boolean isSynthetic) implements Expression {

	/** Creates a new Terminal symbol. */
	public Terminal {
		Objects.requireNonNull(literal);
		if (literal.isEmpty()) {
			throw new IllegalArgumentException("Empty terminal symbol.");
		}
	}

	/**
	 * Creates a new Terminal symbol. Equivalent to {@code new Terminal(literal, false)}.
	 *
	 * @param literal The string literal representing this terminal symbol.
	 */
	public Terminal(final String literal) {
		this(literal, false);
	}
}
