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

import com.ledmington.ebnf.Expression;
import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.NonTerminal;
import com.ledmington.ebnf.OneOrMore;
import com.ledmington.ebnf.Or;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Sequence;
import com.ledmington.ebnf.Terminal;
import com.ledmington.ebnf.ZeroOrMore;
import com.ledmington.ebnf.ZeroOrOne;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestingUtilities {

	private TestingUtilities() {}

	public static Grammar ebnf(final List<Production> productions) {
		return new Grammar(productions);
	}

	public static BNFGrammar bnf(final List<BNFProduction> productions) {
		return new BNFGrammar(productions);
	}

	public static NonTerminal nt(final String literal) {
		return new NonTerminal(literal);
	}

	public static Terminal t(final String literal) {
		return new Terminal(literal);
	}

	public static ZeroOrOne zero_or_one(final Expression inner) {
		return new ZeroOrOne(inner);
	}

	public static ZeroOrMore zero_or_more(final Expression inner) {
		return new ZeroOrMore(inner);
	}

	public static OneOrMore one_or_more(final Expression inner) {
		return new OneOrMore(inner);
	}

	public static Sequence seq(final Expression... expressions) {
		return new Sequence(expressions);
	}

	public static Or or(final Expression... expressions) {
		return new Or(expressions);
	}

	public static Production p(final String symbol, final Expression expression) {
		return new Production(new NonTerminal(symbol), expression);
	}

	public static BNFProduction p(final String symbol, final BNFExpression expression) {
		return new BNFProduction(new BNFNonTerminal(symbol), expression);
	}
}
