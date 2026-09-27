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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.CharacterIterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public final class TestCommentSkippingTextIterator {

	private static TextIterator iterator(final String s) {
		return new CommentSkippingTextIterator(new StringTextIterator(s));
	}

	private static String drain(final TextIterator it) {
		final StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			sb.append(it.current());
			it.move();
		}
		return sb.toString();
	}

	private static Stream<Arguments> provideSkipComments() {
		return Stream.of(
				Arguments.of("", ""),
				Arguments.of("abc", "abc"),
				Arguments.of("a//comment", "a"),
				Arguments.of("//comment", ""),
				Arguments.of("a/*comment*/b", "ab"),
				Arguments.of("/*a*//*b*/c", "c"),
				Arguments.of("/*/ */a", "a"),
				Arguments.of("a/b", "a/b"),
				Arguments.of("\"//not a comment\"", "\"//not a comment\""),
				Arguments.of("\"/*not a comment*/\"", "\"/*not a comment*/\""),
				Arguments.of("\"a\\\"//b\"", "\"a\\\"//b\""),
				Arguments.of("\"a\"//b", "\"a\""));
	}

	@ParameterizedTest
	@MethodSource("provideSkipComments")
	void skipsComments(final String input, final String expected) {
		assertEquals(expected, drain(iterator(input)));
	}

	@Test
	void lineCommentKeepsNewline() {
		assertEquals("a\nb", drain(iterator("a//comment\nb")));
	}

	@Test
	void peekNextSkipsComments() {
		final TextIterator it = iterator("a/*comment*/b");
		assertEquals('a', it.current());
		assertEquals('b', it.peekNext());
		it.move();
		assertEquals('b', it.current());
		assertEquals(CharacterIterator.DONE, it.peekNext());
	}

	@Test
	void positionsReferToOriginalText() {
		final TextIterator it = iterator("/*x\ny*/ab//c\nd");
		assertEquals('a', it.current());
		assertEquals(2, it.getLine());
		assertEquals(4, it.getColumn());
		it.move();
		it.move();
		assertEquals('\n', it.current());
		assertEquals(2, it.getLine());
		assertEquals(9, it.getColumn());
		it.move();
		assertEquals('d', it.current());
		assertEquals(3, it.getLine());
		assertEquals(1, it.getColumn());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/*", "a/*b", "/* a *", "\"", "a\"b", "\"a\\\""})
	void unterminated(final String input) {
		assertThrows(ParsingException.class, () -> drain(iterator(input)));
	}
}
