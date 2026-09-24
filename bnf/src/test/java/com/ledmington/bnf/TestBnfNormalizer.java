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
import static com.ledmington.bnf.TestingUtilities.p;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TestBnfNormalizer {

	@Test
	void alreadyFlatGrammarIsUnchanged() {
		final BNFGrammar g = bnf(List.of(
				p("start", new BNFAlternation(new BNFNonTerminal("a"), BNFTerminal.EPSILON)),
				p("a", new BNFTerminal("a"))));
		assertEquals(g, BnfNormalizer.normalize(g));
	}

	@Test
	void hoistsSequenceBranchIntoFreshProduction() {
		// mirrors what Converter produces for 'start = "a"*'
		final BNFGrammar g = bnf(List.of(p(
				"start",
				new BNFAlternation(
						new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start")), BNFTerminal.EPSILON))));

		final BNFGrammar expected = bnf(List.of(
				p("start", new BNFAlternation(new BNFNonTerminal("start_branch0"), BNFTerminal.EPSILON)),
				p("start_branch0", new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start")))));

		assertEquals(expected, BnfNormalizer.normalize(g));
	}

	@Test
	void avoidsNameCollisionWithExistingProduction() {
		final BNFGrammar g = bnf(List.of(
				p(
						"start",
						new BNFAlternation(
								new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start")),
								BNFTerminal.EPSILON)),
				p("start_branch0", new BNFTerminal("z"))));

		final BNFGrammar normalized = BnfNormalizer.normalize(g);

		assertEquals(
				new BNFAlternation(new BNFNonTerminal("start_branch1"), BNFTerminal.EPSILON),
				normalized.productions().stream()
						.filter(p -> p.start().equals(new BNFNonTerminal("start")))
						.findFirst()
						.orElseThrow()
						.result());
		assertEquals(
				new BNFSequence(new BNFTerminal("a"), new BNFNonTerminal("start")),
				normalized.productions().stream()
						.filter(p -> p.start().equals(new BNFNonTerminal("start_branch1")))
						.findFirst()
						.orElseThrow()
						.result());
	}
}
