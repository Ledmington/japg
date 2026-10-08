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

import java.text.CharacterIterator;
import java.util.Arrays;

/** A {@link TextIterator} over the characters of a String. */
public final class StringTextIterator implements TextIterator {

	private final char[] arr;

	// Position of the current character from the beginning of the file
	private int characterPosition = 0;

	private int line = 1; // The first line is number 1 by convention
	private int column = 1; // The first column is number 1 by convention

	/**
	 * Creates a new StringTextIterator pointing to the first character of the given String.
	 *
	 * @param s The text to iterate over.
	 */
	public StringTextIterator(final String s) {
		this.arr = s.toCharArray();
	}

	@Override
	public boolean hasNext() {
		return characterPosition < arr.length;
	}

	@Override
	public char current() {
		return hasNext() ? arr[characterPosition] : CharacterIterator.DONE;
	}

	@Override
	public char peekNext() {
		return characterPosition + 1 < arr.length ? arr[characterPosition + 1] : CharacterIterator.DONE;
	}

	@Override
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	public void move() {
		if (arr[characterPosition] == '\n') {
			line++;
			column = 1;
		} else {
			column++;
		}
		characterPosition++;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getColumn() {
		return column;
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Arrays.hashCode(arr);
		h = 31 * h + characterPosition;
		h = 31 * h + line;
		h = 31 * h + column;
		return h;
	}

	@Override
	public String toString() {
		return "StringTextIterator(content='" + Arrays.toString(arr) + "'; pos=" + characterPosition + "; line=" + line
				+ "; column=" + column + ")";
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof StringTextIterator it)) {
			return false;
		}
		return this.characterPosition == it.characterPosition
				&& Arrays.equals(this.arr, it.arr)
				&& this.line == it.line
				&& this.column == it.column;
	}
}
