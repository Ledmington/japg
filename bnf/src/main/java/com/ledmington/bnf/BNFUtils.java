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

import java.util.List;

/** A collection of various utilities. */
// TODO: move to an external module or merge with com.ledmington.ebnf.Utils
public final class BNFUtils {

	private static final char VERTICAL_LINE = '│';
	private static final char HORIZONTAL_LINE = '─';
	private static final char JOINT = '├';
	private static final char ANGLE = '└';

	private BNFUtils() {}

	/**
	 * Returns a String representing the given grammar with the given level of indentation.
	 *
	 * @param root The grammar to be serialized.
	 * @return An indented String representation of the grammar.
	 */
	public static String prettyPrint(final BNFGrammar root) {
		final StringBuilder sb = new StringBuilder();
		sb.append("grammar\n");
		final List<BNFProduction> productions = root.productions();
		if (productions.isEmpty()) {
			return sb.toString();
		}

		final int len = productions.size();
		for (int i = 0; i < len - 1; i++) {
			final BNFProduction p = productions.get(i);
			prettyPrintList(sb, "production", List.of(p.start(), p.result()), getJointIndent(""), getLineIndent(""));
		}
		final BNFProduction last = productions.getLast();
		prettyPrintList(sb, "production", List.of(last.start(), last.result()), getAngleIndent(""), getEmptyIndent(""));
		return sb.toString();
	}

	private static void prettyPrint(
			final StringBuilder sb, final BNFExpression n, final String indent, final String continuationIndent) {
		switch (n) {
			case BNFTerminal t -> {
				sb.append(indent);
				if (t == BNFTerminal.EPSILON) {
					sb.append("epsilon\n");
				} else {
					sb.append("terminal '")
							.append(BNFUtils.getEscapedString(t.literal()))
							.append("'\n");
				}
			}
			case BNFNonTerminal nt ->
				sb.append(indent).append("non_terminal '").append(nt.name()).append("'\n");
			case BNFAlternation or -> prettyPrintList(sb, "alternation", or.expressions(), indent, continuationIndent);
			case BNFSequence s -> prettyPrintList(sb, "sequence", s.expressions(), indent, continuationIndent);
			default -> throw new IllegalArgumentException(String.format("Unknown node: '%s'.", n));
		}
	}

	private static String getEmptyIndent(final String s) {
		return s + "   ";
	}

	private static String getLineIndent(final String s) {
		return s + ' ' + VERTICAL_LINE + ' ';
	}

	private static void prettyPrintContainer(
			final StringBuilder sb,
			final String nodeName,
			final BNFExpression inner,
			final String indentString,
			final String indent) {
		prettyPrintList(sb, nodeName, List.of(inner), indentString, indent);
	}

	private static void prettyPrintList(
			final StringBuilder sb,
			final String nodeName,
			final List<BNFExpression> nodes,
			final String indent,
			final String continuationIndent) {
		sb.append(indent).append(nodeName).append('\n');
		if (nodes.isEmpty()) {
			return;
		}
		for (int i = 0; i < nodes.size() - 1; i++) {
			prettyPrint(sb, nodes.get(i), getJointIndent(continuationIndent), getLineIndent(continuationIndent));
		}
		prettyPrint(sb, nodes.getLast(), getAngleIndent(continuationIndent), getEmptyIndent(continuationIndent));
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
	 * Returns the given BNF grammar as a String with one line per production.
	 *
	 * @param g The BNF grammar to be converted.
	 * @return The String representation of the Grammar.
	 */
	public static String printAsGrammar(final BNFGrammar g) {
		final StringBuilder sb = new StringBuilder();
		for (final BNFProduction p : g.productions()) {
			sb.append(p.start().name()).append(" -> ");
			printAsGrammar(sb, p.result());
			sb.append(" ;\n");
		}
		return sb.toString();
	}

	private static void printAsGrammar(final StringBuilder sb, final BNFExpression value) {}
}
