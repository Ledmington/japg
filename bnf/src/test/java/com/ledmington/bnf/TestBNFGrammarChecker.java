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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public final class TestBNFGrammarChecker {

	@Test
	void happyPathDoesNotThrow() {
		final BNFGrammar g = bnf(List.of(
				p("start", new BNFNonTerminal("a")),
				p("a", new BNFAlternation(new BNFTerminal("x"), new BNFTerminal("y")))));
		assertDoesNotThrow(() -> BNFGrammarChecker.check(g));
	}

	@Test
	void unknownNonTerminalThrows() {
		final BNFGrammar g = bnf(List.of(p("start", new BNFNonTerminal("undefined"))));
		assertThrows(UnknownNonTerminalException.class, () -> BNFGrammarChecker.check(g));
	}

	@Test
	void unreachableStateThrows() {
		final BNFGrammar g = bnf(List.of(p("start", new BNFTerminal("a")), p("unreachable", new BNFTerminal("b"))));
		assertThrows(UnreachableStatesException.class, () -> BNFGrammarChecker.check(g));
	}
}
