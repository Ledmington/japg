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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.bnf.BNFAlternation;
import com.ledmington.bnf.BNFExpression;
import com.ledmington.bnf.BNFGrammar;
import com.ledmington.bnf.BNFNonTerminal;
import com.ledmington.bnf.BNFProduction;
import com.ledmington.bnf.BNFSequence;
import com.ledmington.bnf.BNFTerminal;

/** An helper class to generate java code for a given, already-normalized, set of BNF productions. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class BnfParserSerializer {

	private final IndentedStringBuilder sb;
	private final Set<String> tokenNames;

	/**
	 * Creates a new BnfParserSerializer.
	 *
	 * @param sb The StringBuilder to use.
	 * @param tokenNames The set of non-terminals corresponding to lexer productions.
	 */
	public BnfParserSerializer(final IndentedStringBuilder sb, final Set<String> tokenNames) {
		this.sb = Objects.requireNonNull(sb);
		this.tokenNames = Objects.requireNonNull(tokenNames);
	}

	private boolean isToken(final String tokenName) {
		return this.tokenNames.contains(tokenName);
	}

	/**
	 * Generates java code of a backtracking recursive-descent parser for the given, already-checked and normalized, BNF
	 * grammar.
	 *
	 * @param g The grammar to be used.
	 */
	public void generateParser(final BNFGrammar g) {
		generateTypes(g.productions());
		generateTerminalSymbolParsing();
		generateProductions(g.productions());
	}

	private void generateTypes(final List<BNFProduction> productions) {
		for (final BNFProduction p : productions) {
			final String newNodeName = p.start().name();
			switch (p.result()) {
				case BNFNonTerminal nt ->
					sb.append("public record ")
							.append(newNodeName)
							.append('(')
							.append(resolveElementTypeName(nt))
							.append(" inner) implements Alternation {\n")
							.indent()
							.append("@Override\n")
							.append("public String name() {\n")
							.indent()
							.append("return \"" + newNodeName + "\";\n")
							.deindent()
							.append("}\n")
							.append("@Override\n")
							.append("public Node match() {\n")
							.indent()
							.append("return inner;\n")
							.deindent()
							.append("}\n")
							.deindent()
							.append("}\n");
				case BNFSequence(final List<BNFExpression> expressions) ->
					sb.append("public record ")
							.append(newNodeName)
							.append('(')
							.append(IntStream.range(0, expressions.size())
									.mapToObj(i -> resolveElementTypeName(expressions.get(i)) + " n" + i)
									.collect(Collectors.joining(", ")))
							.append(") implements Sequence {\n")
							.indent()
							.append("@Override\n")
							.append("public String name() {\n")
							.indent()
							.append("return \"" + newNodeName + "\";\n")
							.deindent()
							.append("}\n")
							.append("@Override\n")
							.append("public List<Node> nodes() {\n")
							.indent()
							.append("return List.of(")
							.append(IntStream.range(0, expressions.size())
									.mapToObj(i -> "n" + i)
									.collect(Collectors.joining(", ")))
							.append(");\n")
							.deindent()
							.append("}\n")
							.deindent()
							.append("}\n");
				case BNFAlternation ignored ->
					sb.append("public record ")
							.append(newNodeName)
							.append("(Node match) implements Alternation {\n")
							.indent()
							.append("@Override\n")
							.append("public String name() {\n")
							.indent()
							.append("return \"" + newNodeName + "\";\n")
							.deindent()
							.append("}\n")
							.deindent()
							.append("}\n");
				default ->
					throw new IllegalStateException(String.format("Unexpected BNF production body: '%s'.", p.result()));
			}
		}
	}

	private void generateTerminalSymbolParsing() {
		sb.append("private Terminal parseTerminal(final TokenType expected) {\n")
				.indent()
				.append("if (pos < v.length && v[pos].type() == expected) {\n")
				.indent()
				.append("return new Terminal(v[pos++].content());\n")
				.deindent()
				.append("}\n")
				.append("return null;\n")
				.deindent()
				.append("}\n");
	}

	private String resolveElementTypeName(final BNFExpression exp) {
		if (exp instanceof final BNFNonTerminal nt) {
			return isToken(nt.name()) ? "Terminal" : nt.name();
		}
		throw new IllegalStateException(
				String.format("Unexpected BNF expression as an atomic sequence/alternation element: '%s'.", exp));
	}

	private void generateParseCall(final BNFExpression exp, final String variableName) {
		if (!(exp instanceof final BNFNonTerminal nt)) {
			throw new IllegalStateException(
					String.format("Unexpected BNF expression as an atomic sequence/alternation element: '%s'.", exp));
		}
		final boolean isToken = isToken(nt.name());
		final String typeName = isToken ? "Terminal" : nt.name();
		sb.append("final " + typeName + " " + variableName + " = ");
		if (isToken) {
			sb.append("parseTerminal(TokenType." + nt.name() + ")");
		} else {
			sb.append("parse_" + nt.name() + "()");
		}
		sb.append(";\n");
	}

	private void generateProductions(final List<BNFProduction> productions) {
		for (final BNFProduction p : productions) {
			final String productionName = p.start().name();
			switch (p.result()) {
				case BNFNonTerminal nt -> generateAlias(productionName, nt);
				case BNFSequence seq -> generateSequence(productionName, seq);
				case BNFAlternation alt -> generateAlternation(productionName, alt);
				default ->
					throw new IllegalStateException(String.format("Unexpected BNF production body: '%s'.", p.result()));
			}
		}
	}

	private void generateAlias(final String productionName, final BNFNonTerminal nt) {
		sb.append("private " + productionName + " parse_" + productionName + "() {\n")
				.indent();
		generateParseCall(nt, "inner");
		sb.append("if (inner == null) {\n")
				.indent()
				.append("return null;\n")
				.deindent()
				.append("}\n")
				.append("return new " + productionName + "(inner);\n")
				.deindent()
				.append("}\n");
	}

	private void generateSequence(final String productionName, final BNFSequence seq) {
		final List<BNFExpression> elements = seq.expressions();

		sb.append("private " + productionName + " parse_" + productionName + "() {\n")
				.indent()
				.append("stack.push(this.pos);\n");

		for (int i = 0; i < elements.size(); i++) {
			final String varName = "n" + i;
			generateParseCall(elements.get(i), varName);
			sb.append("if (" + varName + " == null) {\n")
					.indent()
					.append("this.pos = stack.pop();\n")
					.append("return null;\n")
					.deindent()
					.append("}\n");
		}

		sb.append("stack.pop();\n")
				.append("return new " + productionName + "("
						+ IntStream.range(0, elements.size())
								.mapToObj(i -> "n" + i)
								.collect(Collectors.joining(", "))
						+ ");\n")
				.deindent()
				.append("}\n");
	}

	private void generateAlternation(final String productionName, final BNFAlternation alt) {
		sb.append("private " + productionName + " parse_" + productionName + "() {\n")
				.indent();

		boolean hasEpsilon = false;
		int i = 0;
		for (final BNFExpression branch : alt.expressions()) {
			if (branch.equals(BNFTerminal.EPSILON)) {
				hasEpsilon = true;
				continue;
			}
			final String varName = "n" + i;
			generateParseCall(branch, varName);
			sb.append("if (" + varName + " != null) {\n")
					.indent()
					.append("return new " + productionName + "(" + varName + ");\n")
					.deindent()
					.append("}\n");
			i++;
		}

		if (hasEpsilon) {
			sb.append("return new " + productionName + "(null);\n");
		} else {
			sb.append("return null;\n");
		}
		sb.deindent().append("}\n");
	}
}
