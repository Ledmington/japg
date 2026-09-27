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
import java.util.Objects;

/**
 * A {@link TextIterator} which lazily skips line comments and block comments of the underlying iterator. Comments
 * inside string literals are left untouched. The line and column reported are the ones of the underlying iterator, so
 * they refer to the original text.
 */
@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
final class CommentSkippingTextIterator implements TextIterator {

	private final TextIterator it;

	// Whether the first character has been read from the underlying iterator
	private boolean started = false;

	// Whether the comments after the current character have already been skipped in the underlying iterator
	private boolean skipped = false;

	// The current character, read from the underlying iterator which is always one character ahead
	private boolean hasCurrent = false;
	private char currentChar = CharacterIterator.DONE;
	private int line = 1;
	private int column = 1;

	// The state after the current character
	private boolean inString = false;
	private boolean escaped = false;
	private int stringLine = -1;
	private int stringColumn = -1;

	/**
	 * Creates a new CommentSkippingTextIterator over the given TextIterator.
	 *
	 * @param it The TextIterator whose comments need to be skipped.
	 */
	CommentSkippingTextIterator(final TextIterator it) {
		this.it = Objects.requireNonNull(it);
	}

	@Override
	public boolean hasNext() {
		start();
		return hasCurrent;
	}

	@Override
	public char current() {
		start();
		return hasCurrent ? currentChar : CharacterIterator.DONE;
	}

	@Override
	public char peekNext() {
		start();
		if (!hasCurrent) {
			return CharacterIterator.DONE;
		}
		skipComments();
		return it.hasNext() ? it.current() : CharacterIterator.DONE;
	}

	@Override
	public void move() {
		start();
		advance();
	}

	@Override
	public int getLine() {
		start();
		return line;
	}

	@Override
	public int getColumn() {
		start();
		return column;
	}

	private void start() {
		if (!started) {
			started = true;
			advance();
		}
	}

	// Reads the next non-comment character from the underlying iterator and makes it the current one.
	private void advance() {
		skipComments();

		line = it.getLine();
		column = it.getColumn();
		if (!it.hasNext()) {
			hasCurrent = false;
			currentChar = CharacterIterator.DONE;
			if (inString) {
				throw new ParsingException(
						String.format("Unterminated string literal started at %d:%d.", stringLine, stringColumn));
			}
			return;
		}

		hasCurrent = true;
		currentChar = it.current();
		it.move();
		skipped = false;

		if (inString) {
			if (escaped) {
				escaped = false;
			} else if (currentChar == '\\') {
				escaped = true;
			} else if (currentChar == '"') {
				inString = false;
			}
		} else if (currentChar == '"') {
			inString = true;
			stringLine = line;
			stringColumn = column;
		}
	}

	// Moves the underlying iterator past any comment, unless inside a string literal.
	private void skipComments() {
		if (skipped) {
			return;
		}
		skipped = true;
		if (inString) {
			return;
		}

		while (it.hasNext() && it.current() == '/') {
			if (it.peekNext() == '/') {
				// Line comments end right before the newline, which is kept
				while (it.hasNext() && it.current() != '\n') {
					it.move();
				}
			} else if (it.peekNext() == '*') {
				final int startLine = it.getLine();
				final int startColumn = it.getColumn();
				it.move();
				it.move();
				while (it.hasNext() && !(it.current() == '*' && it.peekNext() == '/')) {
					it.move();
				}
				if (!it.hasNext()) {
					throw new ParsingException(
							String.format("Unterminated block comment started at %d:%d.", startLine, startColumn));
				}
				it.move();
				it.move();
			} else {
				break;
			}
		}
	}
}
