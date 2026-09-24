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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledmington.utils.GraphUtils;

/** A class to check a BNF grammar for correctness. */
public final class BNFGrammarChecker {

	private BNFGrammarChecker() {}

	/**
	 * Checks the given BNF grammar for correctness. Throws a RuntimeException in case it's not.
	 *
	 * @param g The grammar to be checked.
	 */
	public static void check(final BNFGrammar g) {
		check(g, Set.of());
	}

	/**
	 * Checks the given BNF grammar for correctness, treating references to any of the given names as valid leaf symbols
	 * even though they have no production of their own in {@code g} (e.g. lexer/token names, which this BNF grammar
	 * does not define but is still allowed to reference). Throws a RuntimeException in case it's not.
	 *
	 * @param g The grammar to be checked.
	 * @param externalNames The names which are allowed to be referenced without a corresponding production.
	 */
	public static void check(final BNFGrammar g, final Set<String> externalNames) {
		final Map<BNFNonTerminal, Set<BNFNonTerminal>> neighbors = new HashMap<>();
		final Set<BNFNonTerminal> allNonTerminals = new HashSet<>();
		for (final BNFProduction p : g.productions()) {
			final Set<BNFNonTerminal> referenced = findAllNonTerminals(p.result());
			neighbors.put(p.start(), referenced);

			allNonTerminals.add(p.start());
			allNonTerminals.addAll(referenced);
		}

		// Ensure every referenced non-terminal has a corresponding production (or is a known external name),
		// before building the reachability graph: otherwise a dangling reference would only surface as a
		// NullPointerException deep inside the BFS.
		for (final BNFNonTerminal nt : allNonTerminals) {
			if (externalNames.contains(nt.name())) {
				// an external (e.g. lexer/token) leaf symbol: reachable, but has no production/outgoing edges
				neighbors.putIfAbsent(nt, Set.of());
				continue;
			}
			if (g.productions().stream().noneMatch(p -> p.start().equals(nt))) {
				throw new UnknownNonTerminalException(nt);
			}
		}

		// Ensure all symbols are reachable from the starting symbol
		final BNFNonTerminal startSymbol = g.productions().getFirst().start();
		final Set<BNFNonTerminal> reachableSymbols = GraphUtils.bfs(startSymbol, neighbors::get);

		final boolean allReachable = reachableSymbols.equals(allNonTerminals);
		final boolean allReachableExceptItself =
				without(reachableSymbols, startSymbol).equals(without(allNonTerminals, startSymbol));

		if (!allReachable && !allReachableExceptItself) {
			throw new UnreachableStatesException(startSymbol);
		}
	}

	private static <X> Set<X> without(final Set<X> s, final X toBeRemoved) {
		final Set<X> c = new HashSet<>(s);
		c.remove(toBeRemoved);
		return c;
	}

	private static Set<BNFNonTerminal> findAllNonTerminals(final BNFExpression exp) {
		return switch (exp) {
			case BNFTerminal _ -> Set.of();
			case BNFNonTerminal nt -> Set.of(nt);
			case BNFSequence s ->
				s.expressions().stream()
						.map(BNFGrammarChecker::findAllNonTerminals)
						.reduce(
								Set.of(),
								(a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet()));
			case BNFAlternation or ->
				or.expressions().stream()
						.map(BNFGrammarChecker::findAllNonTerminals)
						.reduce(
								Set.of(),
								(a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet()));
			default -> throw new IllegalArgumentException(String.format("Unknown BNF expression: '%s'.", exp));
		};
	}
}
