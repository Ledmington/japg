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

import static com.ledmington.generator.CorrectGrammars.g;
import static com.ledmington.generator.CorrectGrammars.nt;
import static com.ledmington.generator.CorrectGrammars.p;
import static com.ledmington.generator.CorrectGrammars.t;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.ledmington.ebnf.UnknownNonTerminalException;
import com.ledmington.ebnf.UnreachableStatesException;

public final class TestGrammarChecker {
	@Test
	void multipleStartSymbols() {
		assertThrows(UnreachableStatesException.class, () -> GrammarChecker.check(g(p("s", t("a")), p("t", t("b")))));
	}

	@Test
	void unusableNonTerminals() {
		assertThrows(UnknownNonTerminalException.class, () -> GrammarChecker.check(g(p("s", nt("t")))));
	}
}
