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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TestParser {

	private static final List<Arguments> CORRECT_TEST_CASES = List.of(
			Arguments.of("A=\"a\";", g(p("A", t("a")))),
			Arguments.of("a=B;B=\"a\";", g(p("a", nt("B")), p("B", t("a")))),
			Arguments.of("/**/A=\"a\";", g(p("A", t("a")))),
			Arguments.of("A/**/=\"a\";", g(p("A", t("a")))),
			Arguments.of("A=/**/\"a\";", g(p("A", t("a")))),
			Arguments.of("A=\"a\"/**/;", g(p("A", t("a")))),
			Arguments.of("A=\"a\";/**/", g(p("A", t("a")))),
			Arguments.of("A=//comment\n\"a\";", g(p("A", t("a")))),
			Arguments.of("A=\"/*a\";", g(p("A", t("/*a")))),
			Arguments.of("A=\"//a\";", g(p("A", t("//a")))),
			Arguments.of("A=/*\"a\"*/\"b\";", g(p("A", t("b")))),
			Arguments.of("A=\"a\";//", g(p("A", t("a")))),
			Arguments.of("MY_SYMBOL = \"a\";", g(p("MY_SYMBOL", t("a")))),
			Arguments.of("A = \"a\" \"b\";", g(p("A", seq(t("a"), t("b"))))),
			Arguments.of("A=\"a\" B;B=\"b\";", g(p("A", seq(t("a"), nt("B"))), p("B", t("b")))),
			Arguments.of("A=\"a\";b=A;", g(p("A", t("a")), p("b", nt("A")))),
			Arguments.of("a=b;b=c;c=\"a\";", g(p("a", nt("b")), p("b", nt("c")), p("c", t("a")))),
			Arguments.of("A=\"a\"|\"b\";", g(p("A", or(t("a"), t("b"))))),
			Arguments.of("A=\"a\"? \"b\";", g(p("A", seq(zero_or_one(t("a")), t("b"))))),
			Arguments.of("A=\"a\"* \"b\";", g(p("A", seq(zero_or_more(t("a")), t("b"))))),
			Arguments.of("A=\"a\"+ \"b\";", g(p("A", seq(one_or_more(t("a")), t("b"))))),
			Arguments.of("A=\"\\\"\";", g(p("A", t("\"")))),
			Arguments.of("A=\"\\n\";", g(p("A", t("\n")))),
			Arguments.of("a=\"\\t\";", g(p("a", t("\t")))),
			Arguments.of("a=\"a\"|\"b\"|\"c\";", g(p("a", or(t("a"), t("b"), t("c"))))),
			Arguments.of("S=\"a\"|(\"b\" \"c\");", g(p("S", or(t("a"), seq(t("b"), t("c")))))),
			Arguments.of("S=\"a\" \"b\" | \"c\" \"d\";", g(p("S", or(seq(t("a"), t("b")), seq(t("c"), t("d")))))),
			Arguments.of("S=\"a\" (\"b\" | \"c\") \"d\";", g(p("S", seq(t("a"), or(t("b"), t("c")), t("d"))))),
			Arguments.of(
					"S=(\"a\" | \"b\") (\"a\" | \"b\" | \"c\")*;",
					g(p("S", seq(or(t("a"), t("b")), zero_or_more(or(t("a"), t("b"), t("c"))))))),
			// Fix issue 30 (https://github.com/Ledmington/japg/issues/30)
			Arguments.of(
					String.join(
							"\n",
							"start = A | B | C ( D | E ) ;",
							"A = \"a\" ;",
							"B = \"b\" ;",
							"C = \"c\" ;",
							"D = \"d\" ;",
							"E = \"e\" ;"),
					g(
							p("start", or(nt("A"), nt("B"), seq(nt("C"), or(nt("D"), nt("E"))))),
							p("A", t("a")),
							p("B", t("b")),
							p("C", t("c")),
							p("D", t("d")),
							p("E", t("e")))),
			//
			Arguments.of(
					readFile("ebnf.g"),
					g(
							p("grammar", one_or_more(nt("production"))),
							p(
									"production",
									seq(
											zero_or_one(or(nt("parser_production"), nt("lexer_production"))),
											nt("SEMICOLON"))),
							p("parser_production", seq(nt("PARSER_SYMBOL"), nt("EQUALS"), nt("parser_expression"))),
							p("lexer_production", seq(nt("LEXER_SYMBOL"), nt("EQUALS"), nt("lexer_expression"))),
							p(
									"parser_expression",
									or(
											nt("PARSER_SYMBOL"),
											nt("LEXER_SYMBOL"),
											seq(nt("parser_expression"), nt("QUESTION_MARK")),
											seq(nt("parser_expression"), nt("PLUS")),
											seq(nt("parser_expression"), nt("ASTERISK")),
											seq(nt("parser_expression"), nt("VERTICAL_LINE"), nt("parser_expression")),
											seq(
													nt("LEFT_PARENTHESIS"),
													nt("parser_expression"),
													nt("RIGHT_PARENTHESIS")),
											seq(nt("parser_expression"), nt("parser_expression")))),
							p(
									"lexer_expression",
									or(
											seq(nt("lexer_expression"), nt("QUESTION_MARK")),
											seq(nt("lexer_expression"), nt("PLUS")),
											seq(nt("lexer_expression"), nt("ASTERISK")),
											seq(nt("lexer_expression"), nt("VERTICAL_LINE"), nt("lexer_expression")),
											seq(
													nt("LEFT_PARENTHESIS"),
													nt("lexer_expression"),
													nt("RIGHT_PARENTHESIS")),
											seq(
													nt("DOUBLE_QUOTES"),
													one_or_more(or(
															t(" "), t("!"), t("\""), t("#"), t("$"), t("%"), t("&"),
															t("'"), t("("), t(")"), t("*"), t("+"), t(","), t("-"),
															t("."), t("/"), t("0"), t("1"), t("2"), t("3"), t("4"),
															t("5"), t("6"), t("7"), t("8"), t("9"), t(":"), t(";"),
															t("<"), t("="), t(">"), t("?"), t("@"), t("A"), t("B"),
															t("C"), t("D"), t("E"), t("F"), t("G"), t("H"), t("I"),
															t("J"), t("K"), t("L"), t("M"), t("N"), t("O"), t("P"),
															t("Q"), t("R"), t("S"), t("T"), t("U"), t("V"), t("W"),
															t("X"), t("Y"), t("Z"), t("["), t("\\"), t("]"), t("^"),
															t("_"), t("`"), t("a"), t("b"), t("c"), t("d"), t("e"),
															t("f"), t("g"), t("h"), t("i"), t("j"), t("k"), t("l"),
															t("m"), t("n"), t("o"), t("p"), t("q"), t("r"), t("s"),
															t("t"), t("u"), t("v"), t("w"), t("x"), t("y"), t("z"),
															t("{"), t("|"), t("}"), t("~"))),
													nt("DOUBLE_QUOTES")))),
							p("SEMICOLON", t(";")),
							p("EQUALS", t("=")),
							p("QUESTION_MARK", t("?")),
							p("PLUS", t("+")),
							p("ASTERISK", t("*")),
							p("LEFT_PARENTHESIS", t("(")),
							p("RIGHT_PARENTHESIS", t(")")),
							p("DOUBLE_QUOTES", t("\"")),
							p("VERTICAL_LINE", t("|")),
							p(
									"LEXER_SYMBOL",
									one_or_more(or(
											t("A"), t("B"), t("C"), t("D"), t("E"), t("F"), t("G"), t("H"), t("I"),
											t("J"), t("K"), t("L"), t("M"), t("N"), t("O"), t("P"), t("Q"), t("R"),
											t("S"), t("T"), t("U"), t("V"), t("W"), t("X"), t("Y"), t("Z"), t("_")))),
							p(
									"PARSER_SYMBOL",
									one_or_more(or(
											t("a"), t("b"), t("c"), t("d"), t("e"), t("f"), t("g"), t("h"), t("i"),
											t("j"), t("k"), t("l"), t("m"), t("n"), t("o"), t("p"), t("q"), t("r"),
											t("s"), t("t"), t("u"), t("v"), t("w"), t("x"), t("y"), t("z"), t("_")))),
							p("_WHITESPACE", zero_or_more(or(t(" "), t("\t"), t("\n")))))),
			Arguments.of(
					readFile("number.g"),
					g(
							p("S", seq(nt("SIGN"), nt("number"))),
							p("number", or(nt("ZERO"), nt("non_zero"))),
							p("non_zero", seq(nt("DIGIT_EXCLUDING_ZERO"), zero_or_more(nt("DIGIT")))),
							p("ZERO", t("0")),
							p("SIGN", zero_or_one(or(t("+"), t("-")))),
							p(
									"DIGIT_EXCLUDING_ZERO",
									or(t("1"), t("2"), t("3"), t("4"), t("5"), t("6"), t("7"), t("8"), t("9"))),
							p(
									"DIGIT",
									or(
											t("0"), t("1"), t("2"), t("3"), t("4"), t("5"), t("6"), t("7"), t("8"),
											t("9"))))));

	private static String readFile(final String filename) {
		final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		try {
			return Files.readString(Path.of(Objects.requireNonNull(url).getPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Grammar g(final Production... productions) {
		return new Grammar(List.of(productions));
	}

	private static Production p(final String name, final Expression exp) {
		return new Production(nt(name), exp);
	}

	private static NonTerminal nt(final String name) {
		return new NonTerminal(name);
	}

	private static Terminal t(final String literal) {
		return new Terminal(literal);
	}

	private static Sequence seq(final Expression... expressions) {
		return new Sequence(expressions);
	}

	private static Or or(final Expression... expressions) {
		return new Or(expressions);
	}

	private static ZeroOrOne zero_or_one(final Expression inner) {
		return new ZeroOrOne(inner);
	}

	private static OneOrMore one_or_more(final Expression inner) {
		return new OneOrMore(inner);
	}

	@ParameterizedTest
	@ValueSource(
			strings = {
				"",
				"=",
				";",
				"a",
				"a=\"",
				"a=\";",
				"a=\"a\",;",
				"a=,\"a\";",
				"a=\"a\",,\"a\";",
				"a=\"a\nb\";",
				"1=\"a\";",
				"1a=\"a\";",
				"a=(\"a\";",
				"a=\"a\");",
				"a=(\"a\";)",
				"a=\"a\";/",
				"a=\"a\";/*",
				"a=\"a\";/**",
				"a=/*\"*/a\";",
				"a=\"a/*\"*/;"
			})
	void invalid(final String input) {
		assertThrows(ParsingException.class, () -> Parser.parse(input));
	}

	private static ZeroOrMore zero_or_more(final Expression exp) {
		return new ZeroOrMore(exp);
	}

	private static Stream<Arguments> correctTestCases() {
		return CORRECT_TEST_CASES.stream();
	}

	@ParameterizedTest
	@MethodSource("correctTestCases")
	void correct(final String input, final Grammar expected) {
		final Grammar actual = Parser.parse(input);
		assertEquals(
				expected,
				actual,
				() -> String.format(
						"Expected the first grammar but parsed the second one.%n%s%n%s%n",
						Utils.prettyPrint(expected), Utils.prettyPrint(actual)));
	}
}
