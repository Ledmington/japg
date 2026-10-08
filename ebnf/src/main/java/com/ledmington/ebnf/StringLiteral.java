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

/**
 * A sequence of any characters delimited by double quotes.
 *
 * @param literal The content of this literal.
 * @param line The line where this token appears.
 * @param startColumn The column where this token starts (i.e. where the first double quote is placed).
 */
public record StringLiteral(String literal, int line, int startColumn) implements Token {
	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getStartColumn() {
		return startColumn;
	}

	@Override
	public int getEndColumn() {
		return startColumn + literal.length() + 1; // +1 for the closing quote
	}
}
