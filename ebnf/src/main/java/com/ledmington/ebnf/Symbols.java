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

/** A set of tokens represented by single characters. */
public enum Symbols implements Token {

	/** The semicolon symbol (U+003B). */
	SEMICOLON(';'),

	/** The vertical line (or pipe?) symbol (U+007C). */
	VERTICAL_LINE('|'),

	/** The left parenthesis symbol (U+0028). */
	LEFT_PARENTHESIS('('),

	/** The right parenthesis symbol (U+0029). */
	RIGHT_PARENTHESIS(')'),

	/** The equal sign symbol (U+003D). */
	EQUAL_SIGN('='),

	/** The plus sign symbol (U+002B). */
	PLUS('+'),

	/** The low line symbol (U+005F). */
	UNDERSCORE('_'),

	/** The question mark symbol (U+003F). */
	QUESTION_MARK('?'),

	/** The full stop symbol (U+002E). */
	DOT('.'),

	/** The asterisk symbol (U+002A). */
	ASTERISK('*'),

	/** The hyphens minus symbol (U+002D). */
	DASH('-'),

	/** The double quotes symbol (U+0022). */
	DOUBLE_QUOTES('\"'),

	/** The line feed symbol (U+000A). */
	NEWLINE('\n'),

	/** The character tabulation symbol (U+0009). */
	TAB('\t'),

	/** The whitespace symbol (U+0020). */
	WHITESPACE(' ');

	private final char character;

	Symbols(final char ch) {
		this.character = ch;
	}

	/**
	 * Returns the character representing this symbol.
	 *
	 * @return The character representing this symbol.
	 */
	public char getCharacter() {
		return character;
	}
}
