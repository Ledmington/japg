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

import java.util.List;
import java.util.function.Consumer;

/** A collection of various utilities. */
// TODO: move to an external module
public final class Utils {

	private static final char VERTICAL_LINE = '│';
	private static final char HORIZONTAL_LINE = '─';
	private static final char JOINT = '├';
	private static final char ANGLE = '└';

	private Utils() {}

	/**
	 * Returns a String representing the given tree with the given level of indentation.
	 *
	 * @param root The root of the tree to be serialized.
	 * @return An indented String representation of the tree.
	 */
	public static String prettyPrint(final Node root) {
		final StringBuilder sb = new StringBuilder();
		prettyPrint(sb, root, "", "");
		return sb.toString();
	}

	/**
	 * Returns a String representing the given grammar with the given level of indentation.
	 *
	 * @param root The grammar to be serialized.
	 * @return An indented String representation of the grammar.
	 */
	public static String prettyPrint(final Grammar root) {
		final StringBuilder sb = new StringBuilder();
		sb.append("grammar\n");
		if (root.getProductions().isEmpty()) {
			return sb.toString();
		}
		final List<Production> productions = root.getProductions();
		final int len = productions.size();
		for (int i = 0; i < len - 1; i++) {
			prettyPrint(sb, productions.get(i), getJointIndent(""), getLineIndent(""));
		}
		prettyPrint(sb, productions.getLast(), getAngleIndent(""), getEmptyIndent(""));
		return sb.toString();
	}

	private static void prettyPrint(
			final StringBuilder sb, final Node n, final String indent, final String continuationIndent) {
		switch (n) {
			case Production p ->
				prettyPrint(sb, "production", () -> List.of(p.start(), p.result()), indent, continuationIndent);
			case Terminal t ->
				sb.append(indent)
						.append("terminal '")
						.append(Utils.getEscapedString(t.literal()))
						.append("'\n");
			case NonTerminal nt ->
				sb.append(indent).append("non_terminal '").append(nt.name()).append("'\n");
			case Or or -> prettyPrint(sb, "or", or, indent, continuationIndent);
			case Sequence s -> prettyPrint(sb, "sequence", s, indent, continuationIndent);
			case ZeroOrOne zoo -> prettyPrint(sb, "zero_or_one", zoo, indent, continuationIndent);
			case ZeroOrMore zom -> prettyPrint(sb, "zero_or_more", zom, indent, continuationIndent);
			case OneOrMore oom -> prettyPrint(sb, "one_or_more", oom, indent, continuationIndent);
			default -> throw new IllegalArgumentException(String.format("Unknown node: '%s'.", n));
		}
	}

	private static String getEmptyIndent(final String s) {
		return s + "   ";
	}

	private static String getLineIndent(final String s) {
		return s + ' ' + VERTICAL_LINE + ' ';
	}

	private static void prettyPrint(
			final StringBuilder sb,
			final String nodeName,
			final Container c,
			final String indentString,
			final String indent) {
		prettyPrint(sb, nodeName, () -> List.of(c.inner()), indentString, indent);
	}

	private static void prettyPrint(
			final StringBuilder sb,
			final String nodeName,
			final Composite c,
			final String indent,
			final String continuationIndent) {
		sb.append(indent).append(nodeName).append('\n');
		if (c.expressions().isEmpty()) {
			return;
		}
		for (int i = 0; i < c.expressions().size() - 1; i++) {
			prettyPrint(
					sb, c.expressions().get(i), getJointIndent(continuationIndent), getLineIndent(continuationIndent));
		}
		prettyPrint(
				sb, c.expressions().getLast(), getAngleIndent(continuationIndent), getEmptyIndent(continuationIndent));
	}

	private static String getJointIndent(final String s) {
		return s + " " + JOINT + HORIZONTAL_LINE;
	}

	private static String getAngleIndent(final String s) {
		return s + " " + ANGLE + HORIZONTAL_LINE;
	}

	/**
	 * Escapes all the characters in the given string, if needed.
	 *
	 * @param s The string to be escaped
	 * @return The same input with all characters escaped, if needed.
	 */
	public static String getEscapedString(final String s) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			sb.append(getEscapedCharacter(s.charAt(i)));
		}
		return sb.toString();
	}

	/**
	 * Escapes a single character, if needed.
	 *
	 * @param ch The character to be escaped.
	 * @return The same input character escaped, if needed.
	 */
	public static String getEscapedCharacter(final char ch) {
		return switch (ch) {
			case '\'' -> "\\'";
			case '\"' -> "\\\"";
			case '\\' -> "\\\\";
			case '\t' -> "\\t";
			case '\n' -> "\\n";
			case '\r' -> "\\r";
			case '\0' -> "\\u0000";
			case '\b' -> "\\b";
			case '\f' -> "\\f";
			default -> String.valueOf(ch);
		};
	}

	/**
	 * Prints the given EBNF grammar as a String with one production per line.
	 *
	 * @param g The grammar to be converted.
	 * @return The EBNF grammar as a String.
	 */
	public static String printAsGrammar(final Grammar g) {
		final StringBuilder sb = new StringBuilder();
		g.getProductions().forEach(p -> {
			sb.append(p.start().name()).append(" -> ");
			printAsGrammar(sb, p.result());
			sb.append(" ;\n");
		});
		return sb.toString();
	}

	/**
	 * Prints the given EBNF expression as a String in one line.
	 *
	 * @param exp The EBNF expression to be converted.
	 * @return The EBNF expression as a String.
	 */
	public static String printAsGrammar(final Expression exp) {
		final StringBuilder sb = new StringBuilder();
		printAsGrammar(sb, exp);
		return sb.append("\n").toString();
	}

	private static void printAsGrammar(final StringBuilder sb, final Expression exp) {
		switch (exp) {
			case Terminal t -> sb.append("'").append(t.literal()).append("'");
			case NonTerminal nt -> sb.append(nt.name());
			case Sequence s -> printAsGrammar(sb, s, " ");
			case Or or -> printAsGrammar(sb, or, " | ");
			case ZeroOrOne zoo -> printAsGrammar(sb, zoo, '?');
			case ZeroOrMore zom -> printAsGrammar(sb, zom, '*');
			case OneOrMore oom -> printAsGrammar(sb, oom, '+');
			default -> throw new IllegalArgumentException(String.format("Unknown node: '%s'.", exp));
		}
	}

	private static void printAsGrammar(final StringBuilder sb, final Container c, final char symbol) {
		if (isSymbol(c.inner())) {
			printAsGrammar(sb, c.inner());
		} else {
			sb.append("( ");
			printAsGrammar(sb, c.inner());
			sb.append(") ");
		}
		sb.append(symbol);
	}

	private static void printAsGrammar(final StringBuilder sb, final Composite c, final String separator) {
		if (c.expressions().isEmpty()) {
			return;
		}

		final Consumer<Expression> parenthesis = e -> {
			if (isSymbol(e)) {
				printAsGrammar(sb, e);
			} else {
				sb.append("( ");
				printAsGrammar(sb, e);
				sb.append(" )");
			}
		};

		parenthesis.accept(c.expressions().getFirst());
		for (int i = 1; i < c.expressions().size(); i++) {
			sb.append(separator);
			parenthesis.accept(c.expressions().get(i));
		}
	}

	/**
	 * Checks whether the given BNF expression is either a terminal or a non-terminal symbol.
	 *
	 * @param e expression to check.
	 * @return {@code true} is the expression is a terminal or non-terminal symbol, {@code false} otherwise.
	 */
	public static boolean isSymbol(final Expression e) {
		return e instanceof Terminal || e instanceof NonTerminal;
	}
}
