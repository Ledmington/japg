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
 * An element of the EBNF grammar which represents a symbol which can be expanded into other symbols.
 *
 * @param name The name used in the grammar to refer to this non-terminal symbol.
 * @param line The line where this non-terminal symbol appears, or -1 if it does not come from a source text.
 * @param startColumn The column where this non-terminal symbol starts, or -1 if it does not come from a source text.
 * @param endColumn The column where this non-terminal symbol ends, or -1 if it does not come from a source text.
 */
public record NonTerminal(String name, int line, int startColumn, int endColumn) implements Expression {

	/** Creates a new NonTerminal. */
	public NonTerminal {
		Objects.requireNonNull(name);
		if (name.isBlank()) {
			throw new IllegalArgumentException("Empty non-terminal name.");
		}
	}

	/**
	 * Creates a new NonTerminal with the given name, without a position in the source text.
	 *
	 * @param name The name of the non-terminal symbol.
	 */
	public NonTerminal(final String name) {
		this(name, -1, -1, -1);
	}

	@Override
	public int getStartLine() {
		return line;
	}

	@Override
	public int getEndLine() {
		return line;
	}

	@Override
	public int getStartColumn() {
		return startColumn;
	}

	@Override
	public int getEndColumn() {
		return endColumn;
	}

	// The position is not part of the identity of a non-terminal symbol
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final NonTerminal nt)) {
			return false;
		}
		return this.name.equals(nt.name);
	}
}
