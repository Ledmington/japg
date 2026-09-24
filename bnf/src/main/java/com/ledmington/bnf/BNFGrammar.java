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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An object representing all the productions and symbols of an EBNF grammar.
 *
 * @param productions The productions of this grammar represented as an unordered map from a non-terminal symbol to an
 *     expression.
 */
public record BNFGrammar(List<BNFProduction> productions) {

	/**
	 * Creates a new BNF Grammar with the given Map of productions.
	 *
	 * @param productions The productions of the grammar.
	 */
	public BNFGrammar(final List<BNFProduction> productions) {
		Objects.requireNonNull(productions, "Null productions.");
		if (productions.isEmpty()) {
			throw new IllegalArgumentException("Empty productions.");
		}
		{
			final Set<String> nonTerminalNames = new HashSet<>();
			for (final BNFProduction p : productions) {
				if (nonTerminalNames.contains(p.start().name())) {
					throw new IllegalArgumentException(String.format(
							"Multiple productions for the same non-terminal symbol '%s'.",
							p.start().name()));
				}
				nonTerminalNames.add(p.start().name());
			}
		}
		this.productions = List.copyOf(productions);
	}
}
