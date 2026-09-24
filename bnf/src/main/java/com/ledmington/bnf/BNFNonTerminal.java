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

import java.util.Objects;

/**
 * An element of the BNF grammar which represents a symbol which can be expanded into other symbols.
 *
 * @param name The name used in the grammar to refer to this non-terminal symbol.
 */
public record BNFNonTerminal(String name) implements BNFExpression, Comparable<BNFNonTerminal> {

	/**
	 * Creates a new NonTerminal with the given name.
	 *
	 * @param name The name of the non-terminal symbol.
	 */
	public BNFNonTerminal {
		Objects.requireNonNull(name);
		if (name.isBlank()) {
			throw new IllegalArgumentException("Empty non-terminal name.");
		}
	}

	@Override
	public int compareTo(final BNFNonTerminal other) {
		return this.name.compareTo(other.name);
	}
}
