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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.CharacterIterator;

import org.junit.jupiter.api.Test;

// FIXME: this warning suppression should not be needed
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TestTextIterator {

	@Test
	void emptyHasNoNext() {
		assertFalse(new TextIterator("").hasNext());
	}

	@Test
	void iterateAllCharacters() {
		final TextIterator it = new TextIterator("abc");
		final StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			sb.append(it.current());
			it.move();
		}
		assertEquals("abc", sb.toString());
	}

	@Test
	void peekNext() {
		final TextIterator it = new TextIterator("ab");
		assertEquals('a', it.current());
		assertEquals('b', it.peekNext());
		it.move();
		assertEquals('b', it.current());
		assertEquals(CharacterIterator.DONE, it.peekNext());
	}

	@Test
	void lineAndColumn() {
		final TextIterator it = new TextIterator("ab\nc");
		assertEquals(1, it.getLine());
		assertEquals(1, it.getColumn());
		it.move();
		assertEquals(1, it.getLine());
		assertEquals(2, it.getColumn());
		it.move(); // on '\n'
		assertEquals(1, it.getLine());
		assertEquals(3, it.getColumn());
		it.move(); // on 'c'
		assertEquals(2, it.getLine());
		assertEquals(1, it.getColumn());
		assertTrue(it.hasNext());
	}

	@Test
	void equality() {
		final TextIterator a = new TextIterator("abc");
		final TextIterator b = new TextIterator("abc");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		a.move();
		assertNotEquals(a, b);
	}
}
