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

/** An element of the EBNF grammar which represents a symbol which cannot be expanded into other symbols. */
// TODO: remove isSynthetic and handle it with a new SyntheticTerminal class
public final class Terminal implements Expression {

	private final String literal;

	// true if the literal of this terminal symbol does not actually appear in the grammar. Used for 'epsilon' and '$'.
	private final boolean synthetic;

	// It is assumed that all terminal symbols appear on a single line. All positions are -1 if the terminal symbol does
	// not come from a source text.
	private final int line;
	private final int startColumn;
	private final int endColumn;

	/**
	 * Creates a new Terminal symbol.
	 *
	 * @param literal The content of the terminal symbol.
	 * @param isSynthetic {@code true} if the literal of this terminal symbol does not actually appear in the grammar.
	 *     Used for 'epsilon' and '$'.
	 * @param line The line where this terminal symbol appears, or -1 if it does not come from a source text.
	 * @param startColumn The column where this terminal symbol starts, or -1 if it does not come from a source text.
	 * @param endColumn The column where this terminal symbol ends, or -1 if it does not come from a source text.
	 */
	public Terminal(
			final String literal,
			final boolean isSynthetic,
			final int line,
			final int startColumn,
			final int endColumn) {
		Objects.requireNonNull(literal);
		if (literal.isEmpty()) {
			throw new IllegalArgumentException("Empty terminal symbol.");
		}
		this.literal = literal;
		this.synthetic = isSynthetic;
		this.line = line;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	/**
	 * Creates a new Terminal symbol without a position in the source text.
	 *
	 * @param literal The string literal representing this terminal symbol.
	 * @param isSynthetic {@code true} if the literal of this terminal symbol does not actually appear in the grammar.
	 */
	public Terminal(final String literal, final boolean isSynthetic) {
		this(literal, isSynthetic, -1, -1, -1);
	}

	/**
	 * Creates a new Terminal symbol. Equivalent to {@code new Terminal(literal, false)}.
	 *
	 * @param literal The string literal representing this terminal symbol.
	 */
	public Terminal(final String literal) {
		this(literal, false);
	}

	/**
	 * Returns the content of this terminal symbol.
	 *
	 * @return The content of this terminal symbol.
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * Checks whether the literal of this terminal symbol actually appears in the grammar.
	 *
	 * @return {@code true} if the literal of this terminal symbol does not actually appear in the grammar.
	 */
	public boolean isSynthetic() {
		return synthetic;
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

	@Override
	public String toString() {
		return "Terminal[literal=" + literal + ", isSynthetic=" + synthetic + ", line=" + line + ", startColumn="
				+ startColumn + ", endColumn=" + endColumn + "]";
	}

	// The position is not part of the identity of a terminal symbol
	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + literal.hashCode();
		h = 31 * h + (synthetic ? 1 : 0);
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
		if (!(other instanceof final Terminal t)) {
			return false;
		}
		return this.literal.equals(t.literal) && this.synthetic == t.synthetic;
	}
}
