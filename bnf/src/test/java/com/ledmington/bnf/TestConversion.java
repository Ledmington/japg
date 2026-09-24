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
package com.ledmington.bnf;

import static com.ledmington.bnf.TestingUtilities.bnf;
import static com.ledmington.bnf.TestingUtilities.ebnf;
import static com.ledmington.bnf.TestingUtilities.nt;
import static com.ledmington.bnf.TestingUtilities.one_or_more;
import static com.ledmington.bnf.TestingUtilities.or;
import static com.ledmington.bnf.TestingUtilities.p;
import static com.ledmington.bnf.TestingUtilities.seq;
import static com.ledmington.bnf.TestingUtilities.t;
import static com.ledmington.bnf.TestingUtilities.zero_or_more;
import static com.ledmington.bnf.TestingUtilities.zero_or_one;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Utils;

public final class TestConversion {

	@SuppressWarnings("PMD.AvoidDuplicateLiterals")
	private static Stream<Arguments> testCases() {
		return Stream.of(
				Arguments.of(ebnf(List.of(p("start", t("a")))), bnf(List.of(p("start", new BNFTerminal("a"))))),
				Arguments.of(
						ebnf(List.of(p("start", nt("A")), p("A", t("a")))),
						bnf(List.of(p("start", new BNFNonTerminal("A")), p("A", new BNFTerminal("a"))))),
				Arguments.of(
						ebnf(List.of(p("start", zero_or_one(t("a"))))),
						bnf(List.of(p("start", new BNFAlternation(new BNFTerminal("a"), BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(p("start", zero_or_more(t("a"))))),
						bnf(List.of(p(
								"start",
								new BNFAlternation(
										new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start")),
										BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(p("start", one_or_more(t("a"))))),
						bnf(List.of(
								p("start", new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start_tail"))),
								p(
										"start_tail",
										new BNFAlternation(
												new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start_tail")),
												BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(p("start", seq(t("a"), t("b"))))),
						bnf(List.of(p("start", new BNFSequence(new BNFTerminal("a"), new BNFTerminal("b")))))),
				Arguments.of(
						ebnf(List.of(p("start", or(t("a"), t("b"))))),
						bnf(List.of(p("start", new BNFAlternation(new BNFTerminal("a"), new BNFTerminal("b")))))),
				Arguments.of(
						ebnf(List.of(
								p("expr", seq(nt("term"), zero_or_more(seq(or(t("+"), t("-")), nt("term"))))),
								p("term", seq(nt("factor"), zero_or_more(seq(or(t("*"), t("/")), nt("factor"))))),
								p("factor", or(nt("number"), seq(t("("), nt("expr"), t(")")))),
								p("number", one_or_more(nt("digit"))),
								p(
										"digit",
										or(
												t("0"), t("1"), t("2"), t("3"), t("4"), t("5"), t("6"), t("7"), t("8"),
												t("9"))))),
						bnf(List.of(
								p("expr", new BNFSequence(new BNFNonTerminal("term"), new BNFNonTerminal("expr_tail"))),
								p(
										"expr_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal("+"),
														new BNFNonTerminal("term"),
														new BNFNonTerminal("expr_tail")),
												new BNFSequence(
														new BNFTerminal("-"),
														new BNFNonTerminal("term"),
														new BNFNonTerminal("expr_tail")),
												BNFTerminal.EPSILON)),
								p(
										"term",
										new BNFSequence(new BNFNonTerminal("factor"), new BNFNonTerminal("term_tail"))),
								p(
										"term_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal("*"),
														new BNFNonTerminal("factor"),
														new BNFNonTerminal("term_tail")),
												new BNFSequence(
														new BNFTerminal("/"),
														new BNFNonTerminal("factor"),
														new BNFNonTerminal("term_tail")),
												BNFTerminal.EPSILON)),
								p(
										"factor",
										new BNFAlternation(
												new BNFNonTerminal("number"), new BNFNonTerminal("non_terminal_0"))),
								p(
										"non_terminal_0",
										new BNFSequence(
												new BNFTerminal("("),
												new BNFNonTerminal("expr"),
												new BNFTerminal(")"))),
								p(
										"number",
										new BNFSequence(
												new BNFNonTerminal("digit"), new BNFNonTerminal("number_tail"))),
								p(
										"number_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFNonTerminal("digit"), new BNFNonTerminal("number_tail")),
												BNFTerminal.EPSILON)),
								p(
										"digit",
										new BNFAlternation(
												new BNFTerminal("0"),
												new BNFTerminal("1"),
												new BNFTerminal("2"),
												new BNFTerminal("3"),
												new BNFTerminal("4"),
												new BNFTerminal("5"),
												new BNFTerminal("6"),
												new BNFTerminal("7"),
												new BNFTerminal("8"),
												new BNFTerminal("9")))))),
				Arguments.of(
						ebnf(List.of(
								p(
										"if_stmt",
										seq(
												t("if"),
												nt("expr"),
												t("then"),
												nt("stmt"),
												zero_or_one(seq(t("else"), nt("stmt"))))),
								p("stmt", or(t("pass"), nt("if_stmt"))),
								p("expr", or(t("true"), t("false"))))),
						bnf(List.of(
								p(
										"if_stmt",
										new BNFSequence(
												new BNFTerminal("if"),
												new BNFNonTerminal("expr"),
												new BNFTerminal("then"),
												new BNFNonTerminal("stmt"),
												new BNFNonTerminal("non_terminal_0"))),
								p(
										"non_terminal_0",
										new BNFAlternation(
												new BNFSequence(new BNFTerminal("else"), new BNFNonTerminal("stmt")),
												BNFTerminal.EPSILON)),
								p("stmt", new BNFAlternation(new BNFTerminal("pass"), new BNFNonTerminal("if_stmt"))),
								p("expr", new BNFAlternation(new BNFTerminal("true"), new BNFTerminal("false")))))),
				Arguments.of(
						ebnf(List.of(
								p("list", seq(t("["), zero_or_one(nt("elements")), t("]"))),
								p("elements", seq(nt("element"), zero_or_more(seq(t(","), nt("element"))))),
								p("element", nt("identifier")),
								p("identifier", seq(t("a"), zero_or_more(or(t("a"), t("1"))))))),
						bnf(List.of(
								p(
										"list",
										new BNFSequence(
												new BNFTerminal("["),
												new BNFNonTerminal("opt_elements"),
												new BNFTerminal("]"))),
								p(
										"opt_elements",
										new BNFAlternation(new BNFNonTerminal("elements"), BNFTerminal.EPSILON)),
								p(
										"elements",
										new BNFSequence(
												new BNFNonTerminal("element"), new BNFNonTerminal("elements_tail"))),
								p(
										"elements_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal(","),
														new BNFNonTerminal("element"),
														new BNFNonTerminal("elements_tail")),
												BNFTerminal.EPSILON)),
								p("element", new BNFNonTerminal("identifier")),
								p(
										"identifier",
										new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("identifier_tail"))),
								p(
										"identifier_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal("a"), new BNFNonTerminal("identifier_tail")),
												new BNFSequence(
														new BNFTerminal("1"), new BNFNonTerminal("identifier_tail")),
												BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(
								p("pattern", zero_or_more(nt("group"))),
								p("group", seq(t("("), zero_or_more(or(t("a"), t("b"))), t(")"))))),
						bnf(List.of(
								p(
										"pattern",
										new BNFAlternation(
												new BNFSequence(
														new BNFNonTerminal("group"), new BNFNonTerminal("pattern")),
												BNFTerminal.EPSILON)),
								p(
										"group",
										new BNFSequence(
												new BNFTerminal("("),
												new BNFNonTerminal("group_tail"),
												new BNFTerminal(")"))),
								p(
										"group_tail",
										new BNFAlternation(
												new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("group_tail")),
												new BNFSequence(new BNFTerminal("b"), new BNFNonTerminal("group_tail")),
												BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(
								p("call", seq(t("f"), t("("), zero_or_one(nt("args")), t(")"))),
								p("args", seq(nt("expr"), zero_or_more(seq(t(","), nt("expr"))))),
								p("expr", or(t("a"), t("1"))))),
						bnf(List.of(
								p(
										"call",
										new BNFSequence(
												new BNFTerminal("f"),
												new BNFTerminal("("),
												new BNFNonTerminal("opt_args"),
												new BNFTerminal(")"))),
								p("opt_args", new BNFAlternation(new BNFNonTerminal("args"), BNFTerminal.EPSILON)),
								p("args", new BNFSequence(new BNFNonTerminal("expr"), new BNFNonTerminal("args_tail"))),
								p(
										"args_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal(","),
														new BNFNonTerminal("expr"),
														new BNFNonTerminal("args_tail")),
												BNFTerminal.EPSILON)),
								p("expr", new BNFAlternation(new BNFTerminal("a"), new BNFTerminal("1")))))),
				Arguments.of(
						ebnf(List.of(
								p("sentence", seq(nt("word"), zero_or_more(seq(t(" "), nt("word"))), t("."))),
								p("word", one_or_more(t("a"))))),
						bnf(List.of(
								p(
										"sentence",
										new BNFSequence(
												new BNFNonTerminal("word"),
												new BNFNonTerminal("sentence_tail"),
												new BNFTerminal("."))),
								p(
										"sentence_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal(" "),
														new BNFNonTerminal("word"),
														new BNFNonTerminal("sentence_tail")),
												BNFTerminal.EPSILON)),
								p("word", new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("word_tail"))),
								p(
										"word_tail",
										new BNFAlternation(
												new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("word_tail")),
												BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(p(
								"type",
								or(
										t("int"),
										t("float"),
										seq(
												t("array"),
												t("<"),
												nt("type"),
												zero_or_more(seq(t(","), nt("type"))),
												t(">")))))),
						bnf(List.of(
								p(
										"type",
										new BNFAlternation(
												new BNFTerminal("int"),
												new BNFTerminal("float"),
												new BNFNonTerminal("non_terminal_0"))),
								p(
										"non_terminal_0",
										new BNFSequence(
												new BNFTerminal("array"),
												new BNFTerminal("<"),
												new BNFNonTerminal("type"),
												new BNFNonTerminal("non_terminal_0_tail"),
												new BNFTerminal(">"))),
								p(
										"non_terminal_0_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal(","),
														new BNFNonTerminal("type"),
														new BNFNonTerminal("non_terminal_0_tail")),
												BNFTerminal.EPSILON))))),
				Arguments.of(
						ebnf(List.of(p(
								"decl",
								seq(
										t("var"),
										t("a"),
										zero_or_one(seq(t("="), t("expr"))),
										zero_or_more(seq(t(","), t("a"), zero_or_one(seq(t("="), t("expr"))))))))),
						bnf(List.of(
								p(
										"decl",
										new BNFSequence(
												new BNFTerminal("var"),
												new BNFTerminal("a"),
												new BNFNonTerminal("non_terminal_0"),
												new BNFNonTerminal("decl_tail"))),
								p(
										"non_terminal_0",
										new BNFAlternation(
												new BNFSequence(new BNFTerminal("="), new BNFTerminal("expr")),
												BNFTerminal.EPSILON)),
								p(
										"decl_tail",
										new BNFAlternation(
												new BNFSequence(
														new BNFTerminal(","),
														new BNFTerminal("a"),
														new BNFNonTerminal("non_terminal_1"),
														new BNFNonTerminal("decl_tail")),
												BNFTerminal.EPSILON)),
								p(
										"non_terminal_1",
										new BNFAlternation(
												new BNFSequence(new BNFTerminal("="), new BNFTerminal("expr")),
												BNFTerminal.EPSILON))))));
	}

	private static Stream<Arguments> onlyEBNF() {
		return testCases().map(x -> Arguments.of(x.get()[0]));
	}

	private static Stream<Arguments> onlyBNF() {
		return testCases().map(x -> Arguments.of(x.get()[1]));
	}

	@ParameterizedTest
	@MethodSource("testCases")
	void conversion(final Grammar input, final BNFGrammar expected) {
		final BNFGrammar actual = Converter.convertToBnf(input.getProductions());
		assertEquals(
				expected,
				actual,
				() -> String.format(
						" --- INPUT EBNF GRAMMAR --- %n%s%n --- EXPECTED BNF GRAMMAR --- %n%s%n --- ACTUAL BNF OUTPUT --- %n%s%n",
						Utils.prettyPrint(input), BNFUtils.prettyPrint(expected), BNFUtils.prettyPrint(actual)));
	}

	@ParameterizedTest
	@MethodSource("onlyEBNF")
	void determinism(final Grammar input) {
		final BNFGrammar actual1 = Converter.convertToBnf(input.getProductions());
		final BNFGrammar actual2 = Converter.convertToBnf(input.getProductions());
		assertEquals(actual1, actual2);
	}

	@ParameterizedTest
	@MethodSource("onlyBNF")
	void checkReference(final BNFGrammar input) {
		assertDoesNotThrow(() -> BNFGrammarChecker.check(input));
	}

	@ParameterizedTest
	@MethodSource("onlyEBNF")
	void checkOutput(final Grammar input) {
		final BNFGrammar g = Converter.convertToBnf(input.getProductions());
		assertDoesNotThrow(() -> BNFGrammarChecker.check(g));
	}
}
