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
package com.ledmington.generator;

import static com.ledmington.generator.CorrectGrammars.nt;
import static com.ledmington.generator.CorrectGrammars.one_or_more;
import static com.ledmington.generator.CorrectGrammars.or;
import static com.ledmington.generator.CorrectGrammars.p;
import static com.ledmington.generator.CorrectGrammars.seq;
import static com.ledmington.generator.CorrectGrammars.t;
import static com.ledmington.generator.CorrectGrammars.zero_or_more;
import static com.ledmington.generator.CorrectGrammars.zero_or_one;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Production;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TestSimpleGrammar {

	private static Stream<Arguments> correctCases() {
		return Stream.of(
				// just non-terminals
				Arguments.of(List.of(p("before", nt("after"))), List.of(p("before", nt("after")))),
				Arguments.of(List.of(p("start", seq(nt("a"), nt("b")))), List.of(p("start", seq(nt("a"), nt("b"))))),
				Arguments.of(List.of(p("start", or(nt("a"), nt("b")))), List.of(p("start", or(nt("a"), nt("b"))))),
				Arguments.of(List.of(p("start", zero_or_more(nt("a")))), List.of(p("start", zero_or_more(nt("a"))))),
				Arguments.of(List.of(p("start", one_or_more(nt("a")))), List.of(p("start", one_or_more(nt("a"))))),
				Arguments.of(List.of(p("start", zero_or_one(nt("a")))), List.of(p("start", zero_or_one(nt("a"))))),
				// also terminals
				Arguments.of(List.of(p("before", t("after"))), List.of(p("before", t("after")))),
				Arguments.of(List.of(p("start", seq(t("a"), t("b")))), List.of(p("start", seq(t("a"), t("b"))))),
				Arguments.of(List.of(p("start", or(t("a"), t("b")))), List.of(p("start", or(t("a"), t("b"))))),
				Arguments.of(List.of(p("start", zero_or_more(t("a")))), List.of(p("start", zero_or_more(t("a"))))),
				Arguments.of(List.of(p("start", one_or_more(t("a")))), List.of(p("start", one_or_more(t("a"))))),
				Arguments.of(List.of(p("start", zero_or_one(t("a")))), List.of(p("start", zero_or_one(t("a"))))),
				// seq + zero_or_one
				Arguments.of(
						List.of(p("start", zero_or_one(seq(nt("a"), nt("b"))))),
						List.of(p("start", zero_or_one(nt("sequence_0"))), p("sequence_0", seq(nt("a"), nt("b"))))),
				Arguments.of(
						List.of(p("start", seq(zero_or_one(nt("a")), zero_or_one(nt("b"))))),
						List.of(
								p("start", seq(nt("zero_or_one_0"), nt("zero_or_one_1"))),
								p("zero_or_one_0", zero_or_one(nt("a"))),
								p("zero_or_one_1", zero_or_one(nt("b"))))),
				// seq + or
				Arguments.of(
						List.of(p("start", or(seq(nt("a"), nt("b")), seq(nt("c"), nt("d"))))),
						List.of(
								p("start", or(nt("sequence_0"), nt("sequence_1"))),
								p("sequence_0", seq(nt("a"), nt("b"))),
								p("sequence_1", seq(nt("c"), nt("d"))))),
				Arguments.of(
						List.of(p("start", seq(or(nt("a"), nt("b")), or(nt("c"), nt("d"))))),
						List.of(
								p("start", seq(nt("or_0"), nt("or_1"))),
								p("or_0", or(nt("a"), nt("b"))),
								p("or_1", or(nt("c"), nt("d"))))),
				// seq + zero_or_more
				Arguments.of(
						List.of(p("start", seq(zero_or_more(nt("a")), zero_or_more(nt("b"))))),
						List.of(
								p("start", seq(nt("zero_or_more_0"), nt("zero_or_more_1"))),
								p("zero_or_more_0", zero_or_more(nt("a"))),
								p("zero_or_more_1", zero_or_more(nt("b"))))),
				Arguments.of(
						List.of(p("start", zero_or_more(seq(nt("a"), nt("b"))))),
						List.of(p("start", zero_or_more(nt("sequence_0"))), p("sequence_0", seq(nt("a"), nt("b"))))),
				// seq + one_or_more
				Arguments.of(
						List.of(p("start", seq(one_or_more(nt("a")), one_or_more(nt("b"))))),
						List.of(
								p("start", seq(nt("one_or_more_0"), nt("one_or_more_1"))),
								p("one_or_more_0", one_or_more(nt("a"))),
								p("one_or_more_1", one_or_more(nt("b"))))),
				Arguments.of(
						List.of(p("start", one_or_more(seq(nt("a"), nt("b"))))),
						List.of(p("start", one_or_more(nt("sequence_0"))), p("sequence_0", seq(nt("a"), nt("b"))))));
	}

	@ParameterizedTest
	@MethodSource("correctCases")
	void check(final List<Production> input, final List<Production> expected) {
		for (final Production p : expected) {
			assertTrue(
					Grammar.isSimpleProduction(p.result()),
					() -> String.format("Expected '%s' to be a simple production but it's not.", p.result()));
		}
		final List<Production> actual = Grammar.simplifyProductions(input);
		assertEquals(
				expected,
				actual,
				() -> String.format(
						" --- Expected grammar --- \n%s\n --- Actual grammar --- \n%s\n",
						expected.stream()
								.map(p -> p.start() + " -> " + p.result())
								.collect(Collectors.joining("\n")),
						actual.stream()
								.map(p -> p.start() + " -> " + p.result())
								.collect(Collectors.joining("\n"))));
	}
}
