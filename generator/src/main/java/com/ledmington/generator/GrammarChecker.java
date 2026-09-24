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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ledmington.ebnf.Expression;
import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.NonTerminal;
import com.ledmington.ebnf.OneOrMore;
import com.ledmington.ebnf.Or;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Sequence;
import com.ledmington.ebnf.Terminal;
import com.ledmington.ebnf.UnknownNonTerminalException;
import com.ledmington.ebnf.UnreachableStatesException;
import com.ledmington.ebnf.ZeroOrMore;
import com.ledmington.ebnf.ZeroOrOne;
import com.ledmington.utils.GraphUtils;

/** A class to check an EBNF grammar for correctness. */
public final class GrammarChecker {

	private GrammarChecker() {}

	/**
	 * Checks the given EBNF grammar for correctness. Throws a RuntimeException in case it's not.
	 *
	 * @param g The grammar to be checked.
	 */
	public static void check(final Grammar g) {
		Objects.requireNonNull(g);

		// Build reachability graph
		final Map<NonTerminal, Set<NonTerminal>> graph = new HashMap<>();
		final Set<NonTerminal> allNonTerminals = new HashSet<>();
		for (final Production p : g.getProductions()) {
			final Set<NonTerminal> neighbors = findAllNonTerminals(p.result());
			graph.put(p.start(), neighbors);

			allNonTerminals.add(p.start());
			allNonTerminals.addAll(neighbors);
		}

		for (final NonTerminal nt : allNonTerminals) {
			if (g.getProductions().stream().noneMatch(p -> p.start().equals(nt))) {
				throw new UnknownNonTerminalException(nt);
			}
		}

		// remove all skippable lexer symbols
		g.getProductions().stream().filter(Production::isSkippable).forEach(p -> allNonTerminals.remove(p.start()));

		// Check that the start symbol can reach all non-terminal symbols
		final NonTerminal startSymbol = g.getProductions().getFirst().start();
		final Set<NonTerminal> reachableSymbols = GraphUtils.bfs(startSymbol, graph::get);

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

	private static Set<NonTerminal> findAllNonTerminals(final Expression exp) {
		return switch (exp) {
			case Terminal _ -> Set.of();
			case NonTerminal nt -> Set.of(nt);
			case Or or ->
				or.expressions().stream()
						.flatMap(x -> findAllNonTerminals(x).stream())
						.collect(Collectors.toSet());
			case Sequence s ->
				s.expressions().stream()
						.flatMap(x -> findAllNonTerminals(x).stream())
						.collect(Collectors.toSet());
			case ZeroOrMore zom -> findAllNonTerminals(zom.inner());
			case ZeroOrOne zoo -> findAllNonTerminals(zoo.inner());
			case OneOrMore oom -> findAllNonTerminals(oom.inner());
			default -> throw new IllegalArgumentException(String.format("Unknown EBNF expression '%s'.", exp));
		};
	}
}
