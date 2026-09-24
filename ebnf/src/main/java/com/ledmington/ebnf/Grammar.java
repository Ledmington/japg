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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/** An object representing all the productions and symbols of an EBNF grammar. */
public final class Grammar {

	private final String startSymbol;
	private final List<Production> productions;
	private final List<Production> splitParserProductions; // pre-computed
	private final List<Production> parserProductions; // pre-computed
	private final List<Production> lexerProductions; // pre-computed

	/**
	 * Creates a new EBNF Grammar with the given List of productions.
	 *
	 * @param productions The productions ordered by priority.
	 */
	public Grammar(final List<Production> productions) {
		Objects.requireNonNull(productions);
		if (productions.isEmpty()) {
			throw new IllegalArgumentException("Empty productions.");
		}
		{
			final Set<String> nonTerminalNames = new HashSet<>();
			for (final Production p : productions) {
				if (nonTerminalNames.contains(p.start().name())) {
					throw new IllegalArgumentException(String.format(
							"Multiple productions for the same non-terminal symbol '%s'.",
							p.start().name()));
				}
				nonTerminalNames.add(p.start().name());
			}
		}
		this.productions = List.copyOf(productions);
		this.startSymbol = productions.getFirst().start().name();

		// temporary list to hold parser productions
		final List<Production> tmp = new ArrayList<>();
		this.lexerProductions = new ArrayList<>();
		splitProductions(productions, lexerProductions, tmp);
		this.splitParserProductions = List.copyOf(tmp);
		this.parserProductions = simplifyProductions(tmp);
	}

	/**
	 * Splits the given set of productions into the ones going into a lexer and the ones going into a parser.
	 *
	 * @param productions The set of grammar productions.
	 * @param lexerProductions The productions belonging to a lexer.
	 * @param parserProductions The productions belonging to a parser.
	 */
	public static void splitProductions(
			final List<Production> productions,
			final List<Production> lexerProductions,
			final List<Production> parserProductions) {
		// Divide all trivial lexer productions from the rest
		productions.stream()
				.filter(p -> Production.isLexerProduction(p.start().name()))
				.forEach(lexerProductions::add);
		productions.stream()
				.filter(p -> !Production.isLexerProduction(p.start().name()))
				.forEach(parserProductions::add);

		// Convert all terminal symbols still in the parser into "anonymous" non-terminal ones
		final Supplier<String> nameSupplier = new Supplier<>() {
			private int id = 0;

			@Override
			public String get() {
				final String newName = "terminal_" + id;
				id++;
				return newName;
			}
		};
		for (int i = 0; i < parserProductions.size(); i++) {
			final Production p = parserProductions.get(i);
			if (containsAtLeastOneTerminal(p.result())) {
				parserProductions.set(
						i, new Production(p.start(), convertExpression(nameSupplier, lexerProductions, p.result())));
			}
		}

		// Final sort by name the productions
		parserProductions.sort(Comparator.comparing(a -> a.start().name()));
	}

	/**
	 * Converts the terminal symbols in the given expression into corresponding fake non-terminal symbols and adding
	 * them into the lexer productions.
	 */
	private static Expression convertExpression(
			final Supplier<String> name, final List<Production> lexerProductions, final Expression e) {
		return switch (e) {
			case Terminal t -> {
				final Optional<Production> replacement = lexerProductions.stream()
						.filter(p -> p.result() instanceof Terminal(final String literal, final boolean ignored)
								&& t.literal().equals(literal))
						.findFirst();
				// Avoid generating fake non-terminal expressions for terminals already present in other productions
				if (replacement.isPresent()) {
					yield replacement.orElseThrow().start();
				} else {
					final String newName = name.get();
					final NonTerminal nt = new NonTerminal(newName);
					lexerProductions.add(new Production(nt, t));
					yield nt;
				}
			}
			case NonTerminal nt -> nt;
			case ZeroOrOne zoo -> new ZeroOrOne(convertExpression(name, lexerProductions, zoo.inner()));
			case OneOrMore oom -> new OneOrMore(convertExpression(name, lexerProductions, oom.inner()));
			case ZeroOrMore zom -> new ZeroOrMore(convertExpression(name, lexerProductions, zom.inner()));
			case Sequence s ->
				new Sequence(s.expressions().stream()
						.map(n -> convertExpression(name, lexerProductions, n))
						.toList());
			case Or a ->
				new Or(a.expressions().stream()
						.map(n -> convertExpression(name, lexerProductions, n))
						.toList());
			default -> throw new IllegalArgumentException(String.format("Unknown node '%s'.", e));
		};
	}

	private static boolean containsAtLeastOneTerminal(final Node n) {
		return switch (n) {
			case Terminal ignored -> true;
			case NonTerminal ignored -> false;
			case ZeroOrOne zoo -> containsAtLeastOneTerminal(zoo.inner());
			case ZeroOrMore zom -> containsAtLeastOneTerminal(zom.inner());
			case OneOrMore oom -> containsAtLeastOneTerminal(oom.inner());
			case Sequence s -> s.expressions().stream().anyMatch(Grammar::containsAtLeastOneTerminal);
			case Or or -> or.expressions().stream().anyMatch(Grammar::containsAtLeastOneTerminal);
			default -> throw new IllegalArgumentException(String.format("Unknown node '%s'.", n));
		};
	}

	/**
	 * Simplifies the given list of parser productions by returning a new list of productions. A production is defined
	 * to be simple when its corresponding expression is either a terminal symbol, a non-terminal symbol or any
	 * composite node containing only terminal or non-terminal symbols. In other words, we can say that a simple
	 * production has its corresponding expression tree with maximum depth 2.
	 *
	 * @param complexParserProductions The productions to be simplified.
	 * @return A new list of simplified productions.
	 */
	public static List<Production> simplifyProductions(final List<Production> complexParserProductions) {
		final Map<NonTerminal, Expression> productions = new LinkedHashMap<>();

		final Function<Node, NonTerminal> freshNT = new Function<>() {

			private int sequenceCounter = 0;
			private int orCounter = 0;
			private int zeroOrOneCounter = 0;
			private int zeroOrMoreCounter = 0;
			private int oneOrMoreCounter = 0;

			@Override
			public NonTerminal apply(final Node n) {
				return new NonTerminal(
						switch (n) {
							case Sequence _ -> {
								final String newName = "sequence_" + sequenceCounter;
								sequenceCounter++;
								yield newName;
							}
							case Or _ -> {
								final String newName = "or_" + orCounter;
								orCounter++;
								yield newName;
							}
							case ZeroOrOne _ -> {
								final String newName = "zero_or_one_" + zeroOrOneCounter;
								zeroOrOneCounter++;
								yield newName;
							}
							case ZeroOrMore _ -> {
								final String newName = "zero_or_more_" + zeroOrMoreCounter;
								zeroOrMoreCounter++;
								yield newName;
							}
							case OneOrMore _ -> {
								final String newName = "one_or_more_" + oneOrMoreCounter;
								oneOrMoreCounter++;
								yield newName;
							}
							default -> throw new IllegalStateException(String.format("Unknown node: '%s'.", n));
						});
			}
		};

		for (final Production prod : complexParserProductions) {
			if (isSimpleProduction(prod.result())) {
				productions.put(prod.start(), prod.result());
				continue;
			}

			final Queue<Pair<NonTerminal, Expression>> q = new ArrayDeque<>();
			q.add(Pair.of(prod.start(), prod.result()));

			while (!q.isEmpty()) {
				final Pair<NonTerminal, Expression> p = q.remove();
				final NonTerminal start = p.first();
				final Expression result = p.second();

				switch (result) {
					case NonTerminal nt -> productions.put(start, nt);
					case Sequence seq -> {
						final List<Expression> newElems = new ArrayList<>();
						for (final Expression e : seq.expressions()) {
							if (e instanceof NonTerminal) {
								newElems.add(e);
							} else {
								final NonTerminal tmp = freshNT.apply(e);
								newElems.add(tmp);
								q.add(Pair.of(tmp, e));
							}
						}
						productions.put(start, new Sequence(newElems));
					}
					case Or or -> {
						final List<Expression> newOpts = new ArrayList<>();
						for (final Expression e : or.expressions()) {
							if (e instanceof NonTerminal) {
								newOpts.add(e);
							} else {
								final NonTerminal tmp = freshNT.apply(e);
								newOpts.add(tmp);
								q.add(Pair.of(tmp, e));
							}
						}
						productions.put(start, new Or(newOpts));
					}
					case ZeroOrOne zoo -> {
						if (zoo.inner() instanceof NonTerminal) {
							productions.put(start, zoo);
						} else {
							final NonTerminal tmp = freshNT.apply(zoo.inner());
							productions.put(start, new ZeroOrOne(tmp));
							q.add(Pair.of(tmp, zoo.inner()));
						}
					}
					case ZeroOrMore zom -> {
						if (zom.inner() instanceof NonTerminal) {
							productions.put(start, zom);
						} else {
							final NonTerminal tmp = freshNT.apply(zom.inner());
							productions.put(start, new ZeroOrMore(tmp));
							q.add(Pair.of(tmp, zom.inner()));
						}
					}
					case OneOrMore oom -> {
						if (oom.inner() instanceof NonTerminal) {
							productions.put(start, oom);
						} else {
							final NonTerminal tmp = freshNT.apply(oom.inner());
							productions.put(start, new OneOrMore(tmp));
							q.add(Pair.of(tmp, oom.inner()));
						}
					}
					default -> throw new IllegalArgumentException(String.format("Unknown node: '%s'.", result));
				}
			}
		}

		return productions.entrySet().stream()
				.map(e -> new Production(e.getKey(), e.getValue()))
				.toList();
	}

	/**
	 * Checks whether the given expression is 'simple'. An expression is considered simple if it is a terminal symbol, a
	 * non-terminal symbol or any other composite expression made only of terminal and non-terminal symbols.
	 *
	 * @param result The expression to be checked.
	 * @return {@code true} if it is a 'simple' expression, {@code false} otherwise.
	 */
	public static boolean isSimpleProduction(final Expression result) {
		return switch (result) {
			case Terminal ignored -> true;
			case NonTerminal ignored -> true;
			case ZeroOrOne(final Expression inner) -> inner instanceof NonTerminal || inner instanceof Terminal;
			case ZeroOrMore(final Expression inner) -> inner instanceof NonTerminal || inner instanceof Terminal;
			case OneOrMore(final Expression inner) -> inner instanceof NonTerminal || inner instanceof Terminal;
			case Sequence(final List<Expression> expressions) ->
				expressions.stream().allMatch(n -> n instanceof NonTerminal || n instanceof Terminal);
			case Or(final List<Expression> expressions) ->
				expressions.stream().allMatch(n -> n instanceof NonTerminal || n instanceof Terminal);
			default -> false;
		};
	}

	/**
	 * Returns the productions, ordered by their priority.
	 *
	 * @return The productions.
	 */
	public List<Production> getProductions() {
		return productions;
	}

	/**
	 * Returns the start symbol.
	 *
	 * @return The start symbol.
	 */
	public String getStartSymbol() {
		return startSymbol;
	}

	/**
	 * Returns the sorted list of productions belonging to the parser.
	 *
	 * @return The sorted list of parser productions.
	 */
	public List<Production> getParserProductions() {
		return parserProductions;
	}

	/**
	 * Returns the sorted list of parser productions, already split from the lexer ones and with every terminal literal
	 * hoisted into a synthetic lexer production, but not yet flattened to depth &lt;= 2 (unlike
	 * {@link #getParserProductions()}).
	 *
	 * @return The sorted list of split, terminal-hoisted, un-simplified parser productions.
	 */
	public List<Production> getSplitParserProductions() {
		return splitParserProductions;
	}

	/**
	 * Returns the sorted list of productions belonging to the lexer.
	 *
	 * @return The sorted list of lexer productions.
	 */
	public List<Production> getLexerProductions() {
		return lexerProductions;
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + productions.hashCode();
		h = 31 * h + startSymbol.hashCode();
		return h;
	}

	@Override
	public String toString() {
		return "Grammar(productions=" + productions + ";startSymbol=" + startSymbol + ")";
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final Grammar g)) {
			return false;
		}
		return this.productions.equals(g.productions) && this.startSymbol.equals(g.startSymbol);
	}
}
