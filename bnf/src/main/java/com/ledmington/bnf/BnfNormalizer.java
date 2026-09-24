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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Normalizes a BNF grammar so that every production body is exactly one of: a {@link BNFTerminal}, a
 * {@link BNFNonTerminal}, a {@link BNFSequence} of only terminal/non-terminal symbols, or a {@link BNFAlternation}
 * whose branches are only terminal/non-terminal symbols or {@link BNFTerminal#EPSILON}. {@link Converter#convertToBnf}
 * can produce alternation branches which are themselves a (already flat) {@link BNFSequence} - e.g. from expanding a
 * {@code ?}/{@code *}/{@code +} operator - so this pass hoists any such branch into a fresh top-level production,
 * replacing it in-place with a reference to the new non-terminal.
 */
public final class BnfNormalizer {

	private BnfNormalizer() {}

	/**
	 * Normalizes the given BNF grammar.
	 *
	 * @param g The grammar to be normalized.
	 * @return A new, normalized BNF grammar.
	 */
	public static BNFGrammar normalize(final BNFGrammar g) {
		final Set<String> usedNames = new HashSet<>();
		for (final BNFProduction p : g.productions()) {
			usedNames.add(p.start().name());
		}

		final List<BNFProduction> result = new ArrayList<>();
		for (final BNFProduction p : g.productions()) {
			if (p.result() instanceof final BNFAlternation alt) {
				final List<BNFExpression> branches = new ArrayList<>();
				final List<BNFProduction> hoisted = new ArrayList<>();
				for (final BNFExpression branch : alt.expressions()) {
					if (branch instanceof final BNFSequence seq) {
						final BNFNonTerminal freshName = uniqueName(p.start().name(), usedNames);
						hoisted.add(new BNFProduction(freshName, seq));
						branches.add(freshName);
					} else {
						branches.add(branch);
					}
				}
				result.add(new BNFProduction(p.start(), new BNFAlternation(branches)));
				result.addAll(hoisted);
			} else {
				result.add(p);
			}
		}
		return new BNFGrammar(result);
	}

	private static BNFNonTerminal uniqueName(final String root, final Set<String> usedNames) {
		int index = 0;
		String candidate;
		do {
			candidate = root + "_branch" + index;
			index++;
		} while (usedNames.contains(candidate));
		usedNames.add(candidate);
		return new BNFNonTerminal(candidate);
	}
}
