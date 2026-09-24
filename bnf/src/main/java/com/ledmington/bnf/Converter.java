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
import java.util.Optional;
import java.util.Set;

import com.ledmington.ebnf.Expression;
import com.ledmington.ebnf.NonTerminal;
import com.ledmington.ebnf.OneOrMore;
import com.ledmington.ebnf.Or;
import com.ledmington.ebnf.Production;
import com.ledmington.ebnf.Sequence;
import com.ledmington.ebnf.Terminal;
import com.ledmington.ebnf.ZeroOrMore;
import com.ledmington.ebnf.ZeroOrOne;

/** A class to convert and EBNF grammar into a BNF grammar. */
public final class Converter {

	private static int nonTerminalCounter = 0;
	private static final Set<String> USED_NAMES = new HashSet<>();

	private Converter() {}

	/**
	 * Converts the given EBNF parser productions into a BNF grammar.
	 *
	 * @param ebnfProductions The EBNF parser productions to be converted.
	 * @return The converted BNF grammar.
	 */
	public static BNFGrammar convertToBnf(final List<Production> ebnfProductions) {
		nonTerminalCounter = 0;
		USED_NAMES.clear();
		for (final Production p : ebnfProductions) {
			USED_NAMES.add(p.start().name());
		}

		List<BNFProduction> productions = new ArrayList<>();
		for (final Production p : ebnfProductions) {
			final BNFNonTerminal start = new BNFNonTerminal(p.start().name());
			final List<BNFProduction> prods = convertToBnfProductions(start, p.result());
			productions = mergeProductions(productions, prods);
		}
		return new BNFGrammar(productions);
	}

	/** NOTE: creates a new List. */
	private static List<BNFProduction> mergeProductions(
			final List<BNFProduction> productions, final List<BNFProduction> newProductions) {
		final List<BNFProduction> result = new ArrayList<>(productions);
		for (final BNFProduction p : newProductions) {
			final Optional<BNFProduction> tmp = productions.stream()
					.filter(x -> x.start().equals(p.start()))
					.findAny();
			if (tmp.isPresent()) {
				// Another production for the same NonTerminal was already present, we merge them inside an Or
				final BNFProduction old = tmp.orElseThrow();
				result.add(new BNFProduction(p.start(), new BNFAlternation(old.result(), p.result())));
			} else {
				result.add(p);
			}
		}
		return result;
	}

	private static List<BNFProduction> convertToBnfProductions(final BNFNonTerminal root, final Expression exp) {
		final List<BNFProduction> productions = new ArrayList<>();
		switch (exp) {
			case Terminal t -> productions.add(new BNFProduction(root, new BNFTerminal(t.literal())));
			case NonTerminal nt -> productions.add(new BNFProduction(root, new BNFNonTerminal(nt.name())));
			case Or or -> {
				final List<BNFExpression> alternatives = new ArrayList<>();
				for (final Expression e : or.expressions()) {
					alternatives.add(embedOrSynthesize(e, productions));
				}
				productions.addFirst(new BNFProduction(root, new BNFAlternation(alternatives)));
			}
			case Sequence s -> {
				final List<BNFExpression> elements = new ArrayList<>();
				for (final Expression e : s.expressions()) {
					elements.add(convertSequenceElement(root, e, productions));
				}
				productions.addFirst(new BNFProduction(root, new BNFSequence(elements)));
			}
			case ZeroOrOne zoo -> {
				final BNFExpression inner = embedInlineOrSynthesize(zoo.inner(), productions);
				productions.addFirst(new BNFProduction(root, new BNFAlternation(inner, BNFTerminal.EPSILON)));
			}
			case ZeroOrMore zom -> {
				final List<List<BNFExpression>> branches = flattenRepetitionBranches(zom.inner(), productions);
				final List<BNFExpression> alternatives = new ArrayList<>();
				for (final List<BNFExpression> branch : branches) {
					final List<BNFExpression> full = new ArrayList<>(branch);
					full.add(root);
					alternatives.add(new BNFSequence(full));
				}
				alternatives.add(BNFTerminal.EPSILON);
				productions.addFirst(new BNFProduction(root, new BNFAlternation(alternatives)));
			}
			case OneOrMore oom -> {
				final BNFNonTerminal tail = uniqueName(root.name() + "_tail");
				final List<List<BNFExpression>> branches = flattenRepetitionBranches(oom.inner(), productions);

				final List<BNFExpression> firstOccurrence = new ArrayList<>();
				for (final List<BNFExpression> branch : branches) {
					final List<BNFExpression> full = new ArrayList<>(branch);
					full.add(tail);
					firstOccurrence.add(new BNFSequence(full));
				}
				final BNFExpression rootBody =
						firstOccurrence.size() == 1 ? firstOccurrence.getFirst() : new BNFAlternation(firstOccurrence);

				final List<BNFExpression> repeated = new ArrayList<>();
				for (final List<BNFExpression> branch : branches) {
					final List<BNFExpression> full = new ArrayList<>(branch);
					full.add(tail);
					repeated.add(new BNFSequence(full));
				}
				repeated.add(BNFTerminal.EPSILON);

				productions.addFirst(new BNFProduction(tail, new BNFAlternation(repeated)));
				productions.addFirst(new BNFProduction(root, rootBody));
			}
			default -> throw new IllegalArgumentException(String.format("Unknown EBNF node '%s'.", exp));
		}
		return productions;
	}

	/**
	 * Converts a single element of an EBNF Sequence into the BNF expression which is going to be used inside the
	 * corresponding BNFSequence, creating and accumulating (into {@code productions}) any auxiliary production needed
	 * to represent it.
	 */
	private static BNFExpression convertSequenceElement(
			final BNFNonTerminal root, final Expression e, final List<BNFProduction> productions) {
		if (e instanceof NonTerminal(final String name)) {
			return new BNFNonTerminal(name);
		}
		if (e instanceof final Terminal t) {
			return new BNFTerminal(t.literal());
		}
		if (e instanceof final ZeroOrOne zoo && zoo.inner() instanceof NonTerminal(final String name)) {
			// x = ... y? ... ; -> a synthetic 'opt_y' non-terminal, since its meaning is unambiguous.
			final BNFNonTerminal opt = uniqueName("opt_" + name);
			productions.addAll(convertToBnfProductions(opt, e));
			return opt;
		}
		if (e instanceof ZeroOrMore || e instanceof OneOrMore) {
			// A repetition appearing inside a sequence is always the "tail" of the production it belongs to.
			final BNFNonTerminal tail = uniqueName(root.name() + "_tail");
			productions.addAll(convertToBnfProductions(tail, e));
			return tail;
		}
		final BNFNonTerminal tmp = getNewNonTerminal();
		productions.addAll(convertToBnfProductions(tmp, e));
		return tmp;
	}

	/**
	 * Converts the given expression into a single BNFExpression, embedding it directly whenever it is already an atomic
	 * terminal/non-terminal symbol or a plain sequence of such symbols (both of which BNFAlternation can hold directly
	 * as one of its branches), and otherwise creating (and accumulating into {@code productions}) a new synthetic
	 * production for it.
	 */
	private static BNFExpression embedInlineOrSynthesize(final Expression e, final List<BNFProduction> productions) {
		if (e instanceof NonTerminal(final String name)) {
			return new BNFNonTerminal(name);
		}
		if (e instanceof final Terminal t) {
			return new BNFTerminal(t.literal());
		}
		if (e instanceof final Sequence seq) {
			final List<BNFExpression> elements = new ArrayList<>();
			for (final Expression sub : seq.expressions()) {
				elements.add(embedOrSynthesize(sub, productions));
			}
			return new BNFSequence(elements);
		}
		return embedOrSynthesize(e, productions);
	}

	/**
	 * Converts the given expression into a single atomic BNFExpression: a terminal/non-terminal symbol is embedded
	 * directly, anything else is extracted into a new synthetic production (accumulated into {@code productions}).
	 */
	private static BNFExpression embedOrSynthesize(final Expression e, final List<BNFProduction> productions) {
		if (e instanceof NonTerminal(final String name)) {
			return new BNFNonTerminal(name);
		}
		if (e instanceof final Terminal t) {
			return new BNFTerminal(t.literal());
		}
		final BNFNonTerminal tmp = getNewNonTerminal();
		productions.addAll(convertToBnfProductions(tmp, e));
		return tmp;
	}

	/**
	 * Flattens the body of a repetition (the inner expression of a ZeroOrMore/OneOrMore) into the list of "branches"
	 * (each branch being the ordered list of symbols making up one repeated occurrence). A plain sequence is flattened
	 * one level so its own elements end up directly in the tail production instead of behind an extra synthetic
	 * non-terminal, and any Or found while flattening is distributed across multiple branches instead of being
	 * extracted into its own production.
	 */
	private static List<List<BNFExpression>> flattenRepetitionBranches(
			final Expression inner, final List<BNFProduction> productions) {
		final List<Expression> subExpressions =
				(inner instanceof final Sequence seq) ? seq.expressions() : List.of(inner);

		List<List<BNFExpression>> branches = new ArrayList<>();
		branches.add(new ArrayList<>());
		for (final Expression e : subExpressions) {
			if (e instanceof final Or or) {
				final List<List<BNFExpression>> newBranches = new ArrayList<>();
				for (final List<BNFExpression> existing : branches) {
					for (final Expression choice : or.expressions()) {
						final List<BNFExpression> extended = new ArrayList<>(existing);
						extended.add(embedOrSynthesize(choice, productions));
						newBranches.add(extended);
					}
				}
				branches = newBranches;
			} else {
				final BNFExpression embedded = embedOrSynthesize(e, productions);
				for (final List<BNFExpression> existing : branches) {
					existing.add(embedded);
				}
			}
		}
		return branches;
	}

	private static BNFNonTerminal getNewNonTerminal() {
		String candidate;
		do {
			candidate = "non_terminal_" + nonTerminalCounter;
			nonTerminalCounter++;
		} while (USED_NAMES.contains(candidate));
		USED_NAMES.add(candidate);
		return new BNFNonTerminal(candidate);
	}

	private static BNFNonTerminal uniqueName(final String base) {
		if (USED_NAMES.add(base)) {
			return new BNFNonTerminal(base);
		}
		int suffix = 2;
		while (!USED_NAMES.add(base + "_" + suffix)) {
			suffix++;
		}
		return new BNFNonTerminal(base + "_" + suffix);
	}
}
