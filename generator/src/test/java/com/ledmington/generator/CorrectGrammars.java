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

import java.util.List;

import org.junit.jupiter.params.provider.Arguments;

import com.ledmington.ebnf.Expression;
import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.NonTerminal;
import com.ledmington.ebnf.OneOrMore;
import com.ledmington.ebnf.Or;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Sequence;
import com.ledmington.ebnf.Terminal;
import com.ledmington.ebnf.ZeroOrMore;
import com.ledmington.ebnf.ZeroOrOne;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class CorrectGrammars {

	private CorrectGrammars() {}

	public static final List<Arguments> TEST_CASES = List.of(
			Arguments.of(g(p("start", t("a"))), List.of("a"), List.of("", "b", "aa")),
			Arguments.of(g(p("start", t("abc"))), List.of("abc"), List.of("", "a", "b", "c", "ab", "bc", "cba")),
			Arguments.of(g(p("start", zero_or_one(t("a")))), List.of("", "a"), List.of("b", "aa")),
			Arguments.of(g(p("start", nt("t")), p("t", t("a"))), List.of("a"), List.of("", "b", "aa")),
			Arguments.of(g(p("start", nt("t")), p("t", zero_or_one(t("a")))), List.of("", "a"), List.of("b", "aa")),
			Arguments.of(g(p("start", seq(t("a"), t("b")))), List.of("ab"), List.of("", "a", "b", "aab", "abb", "c")),
			Arguments.of(
					g(p("start", seq(t("a"), zero_or_one(t("b")), t("c")))),
					List.of("ac", "abc"),
					List.of("", "a", "c", "ab", "bc")),
			Arguments.of(
					g(p("start", seq(zero_or_one(t("a")), zero_or_one(t("b")), zero_or_one(t("c"))))),
					List.of("", "a", "b", "c", "ab", "ac", "bc", "abc"),
					List.of("ba", "ca", "cb", "cba", "abb", "acb", "d")),
			Arguments.of(
					g(p("start", zero_or_one(seq(t("a"), t("b"))))),
					List.of("", "ab"),
					List.of("a", "b", "ba", "aa", "bb")),
			Arguments.of(
					g(
							p("start", seq(zero_or_one(seq(nt("t"), t("b"))), zero_or_one(seq(t("c"), nt("u"))))),
							p("t", t("a")),
							p("u", t("d"))),
					List.of("", "ab", "cd", "abcd"),
					List.of("a", "b", "c", "d", "bc", "ad", "abc", "bcd")),
			Arguments.of(
					g(
							p("start", seq(nt("t"), nt("u"), nt("v"))),
							p("t", zero_or_one(t("a"))),
							p("u", zero_or_one(t("b"))),
							p("v", zero_or_one(t("c")))),
					List.of("", "a", "b", "c", "ab", "ac", "bc", "abc"),
					List.of("cb", "ca", "ba", "cba", "aba", "bcb", "aabc", "abd")),
			Arguments.of(g(p("start", seq(nt("t"), t("a"))), p("t", t("a"))), List.of("aa"), List.of("", "a", "aaa")),
			Arguments.of(
					g(p("start", zero_or_more(t("a")))),
					List.of("", "a", "aa", "aaa", "aaaa", "aaaaa"),
					List.of("b", "ab", "aba", "bab")),
			Arguments.of(
					g(p("start", one_or_more(t("a")))),
					List.of("a", "aa", "aaa", "aaaa"),
					List.of("", "ba", "ab", "aba", "aab", "bab")),
			Arguments.of(
					g(p("start", seq(zero_or_more(seq(t("a"), t("b"))), t("c")))),
					List.of("c", "abc", "ababc", "abababc"),
					List.of("", "a", "b", "ab", "abcab", "aabc", "abbc")),
			Arguments.of(
					g(p("start", zero_or_more(seq(t("a"), zero_or_one(t("b")), t("c"))))),
					List.of("", "ac", "abc", "acac", "abcabc", "acabc", "abcac"),
					List.of("a", "b", "c", "ab", "bc", "aac", "acc", "abbc")),
			Arguments.of(g(p("start", or(t("a"), t("b")))), List.of("a", "b"), List.of("", "ab", "aa", "bb", "ba")),
			Arguments.of(
					g(p("start", zero_or_more(or(t("a"), t("b"))))),
					List.of("", "a", "b", "aa", "ab", "bb", "ba", "aba", "bab", "aaa"),
					List.of("c", "ac", "cb", "bc", "abc")),
			Arguments.of(
					g(
							p(
									"start",
									seq(
											zero_or_one(nt("sign")),
											or(
													nt("zero"),
													seq(nt("digit_excluding_zero"), zero_or_more(nt("digit")))))),
							p("sign", or(t("+"), t("-"))),
							p("zero", t("0")),
							p(
									"digit_excluding_zero",
									or(t("1"), t("2"), t("3"), t("4"), t("5"), t("6"), t("7"), t("8"), t("9"))),
							p("digit", or(nt("zero"), nt("digit_excluding_zero")))),
					List.of("0", "+0", "-0", "1", "+99", "-69", "123456789"),
					List.of("", "01", "001", "1a", "1.0")),
			Arguments.of(
					g(p("start", seq(t("'"), or(t("a"), t("b")), t("'")))),
					List.of("'a'", "'b'"),
					List.of("", "'a", "a'", "'''", "'a''", "'a'b'")),
			Arguments.of(
					g(p("start", seq(t("\""), or(t("a"), t("b")), t("\"")))),
					List.of("\"a\"", "\"b\""),
					List.of("", "\"a", "a\"", "\"\"\"", "\"a\"\"", "\"a\"b\"")),
			Arguments.of(
					g(p("start", seq(t("\\"), or(t("n"), t("t"))))),
					List.of("\\n", "\\t"),
					List.of("", "\\", "\n", "\t", "n", "t")));

	static Grammar g(final Production... productions) {
		return new Grammar(List.of(productions));
	}

	static Production p(final String nonTerminalName, final Expression exp) {
		return new Production(nt(nonTerminalName), exp);
	}

	static NonTerminal nt(final String name) {
		return new NonTerminal(name);
	}

	static Terminal t(final String literal) {
		return new Terminal(literal);
	}

	static Sequence seq(final Expression... expressions) {
		return new Sequence(expressions);
	}

	static ZeroOrOne zero_or_one(final Expression inner) {
		return new ZeroOrOne(inner);
	}

	static OneOrMore one_or_more(final Expression inner) {
		return new OneOrMore(inner);
	}

	static ZeroOrMore zero_or_more(final Expression exp) {
		return new ZeroOrMore(exp);
	}

	static Or or(final Expression... expressions) {
		return new Or(expressions);
	}
}
