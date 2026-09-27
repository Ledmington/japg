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

/** An iterator over the characters of a text which keeps track of the current line and column. */
public interface TextIterator {

	/**
	 * Checks whether the iterator still points to a valid character.
	 *
	 * @return True if there is a current character, false if the end of the text has been reached.
	 */
	default boolean hasNext() {
		return current() != CharacterIterator.DONE;
	}

	/**
	 * Returns the character the iterator currently points to.
	 *
	 * @return The current character, or {@link CharacterIterator#DONE} if the end of the text has been reached.
	 */
	char current();

	/**
	 * Returns the character after the current one, without moving the iterator.
	 *
	 * @return The next character, or {@link CharacterIterator#DONE} if there is none.
	 */
	char peekNext();

	/** Moves the iterator to the next character, updating the line and column accordingly. */
	void move();

	/**
	 * Returns the line of the current character.
	 *
	 * @return The line of the current character, starting from 1.
	 */
	int getLine();

	/**
	 * Returns the column of the current character.
	 *
	 * @return The column of the current character, starting from 1.
	 */
	int getColumn();
}
