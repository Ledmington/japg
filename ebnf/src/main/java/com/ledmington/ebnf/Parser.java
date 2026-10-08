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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A parser of EBNF grammars. */
public final class Parser {

	private Parser() {}

	/**
	 * Parses the given String as an EBNF grammar.
	 *
	 * @param input A String which contains an EBNF grammar. May contain comments.
	 * @return A new Grammar object representing the parsed grammar.
	 */
	public static Grammar parse(final String input) {
		try {
			final TextIterator it = new StringTextIterator(input);
			final TextIterator stripped = skipComments(it);
			final List<Token> tokens = tokenize(stripped);
			return parse(tokens);
		} catch (final IndexOutOfBoundsException ioobe) {
			throw new ParsingException(ioobe);
		}
	}

	private static TextIterator skipComments(final TextIterator it) {
		return new CommentSkippingTextIterator(it);
	}

	private static List<Token> tokenize(final TextIterator it) {
		final List<Token> tokens = new ArrayList<>();
		while (it.hasNext()) {
			final char ch = it.current();
			if (ch == SymbolType.WHITESPACE.getCharacter()
					|| ch == SymbolType.TAB.getCharacter()
					|| ch == SymbolType.NEWLINE.getCharacter()) {
				skipWhitespaces(it);
			} else if (Character.isAlphabetic(ch) || ch == SymbolType.UNDERSCORE.getCharacter()) {
				tokens.add(readWord(it));
			} else if (ch == SymbolType.EQUAL_SIGN.getCharacter()) {
				tokens.add(new Symbol(SymbolType.EQUAL_SIGN, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.SEMICOLON.getCharacter()) {
				tokens.add(new Symbol(SymbolType.SEMICOLON, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.VERTICAL_LINE.getCharacter()) {
				tokens.add(new Symbol(SymbolType.VERTICAL_LINE, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.LEFT_PARENTHESIS.getCharacter()) {
				tokens.add(new Symbol(SymbolType.LEFT_PARENTHESIS, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.RIGHT_PARENTHESIS.getCharacter()) {
				tokens.add(new Symbol(SymbolType.RIGHT_PARENTHESIS, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.PLUS.getCharacter()) {
				tokens.add(new Symbol(SymbolType.PLUS, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.QUESTION_MARK.getCharacter()) {
				tokens.add(new Symbol(SymbolType.QUESTION_MARK, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.DOT.getCharacter()) {
				tokens.add(new Symbol(SymbolType.DOT, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.ASTERISK.getCharacter()) {
				tokens.add(new Symbol(SymbolType.ASTERISK, it.getLine(), it.getColumn()));
				it.move();
			} else if (ch == SymbolType.DOUBLE_QUOTES.getCharacter()) {
				tokens.add(readStringLiteral(it));
			} else {
				throw new ParsingException(String.format(
						"Unknown character '%c' (U+%04X) at %d:%d.", ch, (int) ch, it.getLine(), it.getColumn()));
			}
		}
		return tokens;
	}

	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	private static StringLiteral readStringLiteral(final TextIterator it) {
		if (it.current() != SymbolType.DOUBLE_QUOTES.getCharacter()) {
			throw new AssertionError(String.format(
					"Expected string literal to start with '%c' but it was '%c' at %d:%d.",
					SymbolType.DOUBLE_QUOTES.getCharacter(), it.current(), it.getLine(), it.getColumn()));
		}

		final int startLine = it.getLine();
		final int startColumn = it.getColumn();

		it.move();
		final StringBuilder sb = new StringBuilder();
		while (it.hasNext() && it.current() != SymbolType.DOUBLE_QUOTES.getCharacter()) {
			if (it.current() == SymbolType.NEWLINE.getCharacter()) {
				// string literals must be on the same line
				throw new ParsingException(String.format(
						"Unexpected newline while reading string literal at %d:%d.", it.getLine(), it.getColumn()));
			}
			if (it.current() == '\\') {
				final char next = it.peekNext();
				if (next == SymbolType.DOUBLE_QUOTES.getCharacter()) {
					sb.append(SymbolType.DOUBLE_QUOTES.getCharacter());
					it.move();
				} else if (next == 'n') {
					sb.append('\n');
					it.move();
				} else if (next == 't') {
					sb.append('\t');
					it.move();
				} else if (next == '\\') {
					sb.append('\\');
					it.move();
				}

				// Unknown escape sequences: the backslash is dropped and the next character is read normally

			} else {
				sb.append(it.current());
			}
			it.move();
		}
		if (!it.hasNext()) {
			throw new ParsingException(String.format(
					"Unclosed double quotes in string literal '%s' started at %d:%d.", sb, startLine, startColumn));
		}
		it.move();
		return new StringLiteral(sb.toString(), startLine, startColumn);
	}

	private static Word readWord(final TextIterator it) {
		final int startLine = it.getLine();
		final int startColumn = it.getColumn();
		final StringBuilder sb = new StringBuilder();
		do {
			sb.append(it.current());
			it.move();
		} while (it.hasNext()
				&& (Character.isAlphabetic(it.current()) || it.current() == SymbolType.UNDERSCORE.getCharacter()));
		return new Word(sb.toString(), startLine, startColumn);
	}

	private static void skipWhitespaces(final TextIterator it) {
		while (it.hasNext()
				&& (it.current() == SymbolType.WHITESPACE.getCharacter()
						|| it.current() == SymbolType.TAB.getCharacter()
						|| it.current() == SymbolType.NEWLINE.getCharacter())) {
			it.move();
		}
	}

	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	private static Grammar parse(final List<Token> tokens) {
		if (tokens.isEmpty()) {
			throw new ParsingException("No tokens.");
		}

		// ugly method: the alternative is to manually convert the EBNF grammar for EBNF
		// grammars to be left-recursive
		// and then implement it that way
		final List<Object> v = new ArrayList<>(tokens);

		// Hard-coded passes
		convertStringLiteralsToTerminals(v);
		convertWordsToNonTerminals(v);
		convertDotsToAlternations(v);

		while (true) {
			// Find closest pair of matching brackets which do not contain other brackets
			final Optional<Pair<Integer, Integer>> bracketPositions = findBrackets(v);
			if (bracketPositions.isEmpty()) {
				break;
			}
			final int left = bracketPositions.orElseThrow().first();
			final int right = bracketPositions.orElseThrow().second();
			final List<Object> sublist = new ArrayList<>(v.subList(left, right + 1));
			applyTransformations(sublist);
			if (sublist.size() != 1) {
				throw new AssertionError("Applied transformations did not reduce the expression to a single term.");
			}
			v.subList(left, right + 1).clear();
			v.add(left, sublist.getFirst());
		}

		// one last pass of transformations
		applyTransformations(v);

		if (v.size() != 1) {
			throw new AssertionError();
		}
		if (v.getFirst() instanceof final Production p) {
			return new Grammar(List.of(p));
		}
		if (!(v.getFirst() instanceof final Grammar g)) {
			throw new ParsingException(
					String.format("Expected root element to be a grammar but was '%s'.", v.getFirst()));
		}
		return g;
	}

	private static boolean isSymbol(final Object obj, final SymbolType type) {
		return obj instanceof Symbol(final SymbolType t, final int line, final int column) && t == type;
	}

	private static Optional<Pair<Integer, Integer>> findBrackets(final List<Object> v) {
		final int n = v.size();
		int leftBracketPosition = -1;
		for (int i = 0; i < n; i++) {
			if (isSymbol(v.get(i), SymbolType.LEFT_PARENTHESIS)) {
				leftBracketPosition = i;
				break;
			}
		}
		if (leftBracketPosition == -1) {
			return Optional.empty();
		}
		for (int i = leftBracketPosition + 1; i < n; i++) {
			if (isSymbol(v.get(i), SymbolType.LEFT_PARENTHESIS)) {
				leftBracketPosition = i;
			} else if (isSymbol(v.get(i), SymbolType.RIGHT_PARENTHESIS)) {
				return Optional.of(Pair.of(leftBracketPosition, i));
			}
		}
		throw new ParsingException("No matching pair of brackets was found.");
	}

	private static void applyTransformations(final List<Object> v) {
		final List<BiPredicate<List<Object>, Integer>> transformations = List.of(
				Parser::asterisk,
				Parser::plus,
				Parser::questionMark,
				Parser::parenthesis,
				Parser::mergeSequence,
				Parser::mergeOr,
				Parser::createProduction,
				Parser::mergeProductions);

		for (int pass = 1; v.size() > 1; pass++) {
			// do one pass
			final int initialSize = v.size();

			for (final BiPredicate<List<Object>, Integer> p : transformations) {
				boolean done = false;
				for (int i = 0; i < v.size(); ) {
					if (!p.test(v, i)) {
						i++;
					} else {
						done = true;
					}
				}
				if (done) {
					break;
				}
			}

			if (v.size() == initialSize) {
				throw new ParsingException(String.format(
						"Unknown parsing state after %d passes:%n%s",
						pass,
						IntStream.range(0, v.size())
								.mapToObj(i -> String.format(
										" %3d : %n%s",
										i,
										v.get(i) instanceof Token
												? v.get(i).toString()
												: Utils.prettyPrint((Node) v.get(i))))
								.collect(Collectors.joining("\n"))));
			}
		}
	}

	private static void convertStringLiteralsToTerminals(final List<Object> v) {
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof final StringLiteral s) {
				v.set(i, new Terminal(s.literal(), false, s.getLine(), s.getStartColumn(), s.getEndColumn()));
			}
		}
	}

	private static void convertWordsToNonTerminals(final List<Object> v) {
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof final Word w) {
				v.set(i, new NonTerminal(w.word(), w.getLine(), w.getStartColumn(), w.getEndColumn()));
			}
		}
	}

	private static void convertDotsToAlternations(final List<Object> v) {
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof final Symbol dot && dot.type() == SymbolType.DOT) {
				// The dot matches every printable ASCII character and the common whitespace ones. Every character
				// matched by the dot gets the position of the dot itself.
				v.set(
						i,
						new Or(IntStream.concat(IntStream.of('\t', '\n', '\r'), IntStream.range(32, 127))
								.mapToObj(x -> (Expression) new Terminal(
										"" + (char) x, false, dot.getLine(), dot.getStartColumn(), dot.getEndColumn()))
								.toList()));
			}
		}
	}

	private static boolean mergeProductions(final List<Object> v, final int i) {
		final List<Production> productions = new ArrayList<>();
		int count = 0;
		int j = i;
		for (; j < v.size(); j++) {
			if (v.get(j) instanceof final Grammar g) {
				productions.addAll(g.getProductions());
				count++;
			} else if (v.get(j) instanceof final Production p) {
				productions.add(p);
				count++;
			} else {
				break;
			}
		}
		final int minimumProductions = 1;
		if (count <= minimumProductions) {
			return false;
		} else {
			v.subList(i, j).clear();
			v.add(i, new Grammar(productions));
			return true;
		}
	}

	private static boolean parenthesis(final List<Object> v, final int i) {
		if (i + 2 >= v.size()) {
			return false;
		}
		if (isSymbol(v.get(i), SymbolType.LEFT_PARENTHESIS)
				&& v.get(i + 1) instanceof final Expression exp
				&& isSymbol(v.get(i + 2), SymbolType.RIGHT_PARENTHESIS)) {
			v.subList(i, i + 3).clear();
			v.add(i, exp);
			return true;
		}
		return false;
	}

	private static boolean createProduction(final List<Object> v, final int i) {
		if (i + 3 >= v.size()) {
			return false;
		}
		if (v.get(i) instanceof final NonTerminal start
				&& isSymbol(v.get(i + 1), SymbolType.EQUAL_SIGN)
				&& v.get(i + 2) instanceof final Expression exp
				&& isSymbol(v.get(i + 3), SymbolType.SEMICOLON)) {
			v.subList(i, i + 4).clear();
			v.add(i, new Production(start, exp));
			return true;
		}
		return false;
	}

	private static boolean mergeSequence(final List<Object> v, final int i) {
		final List<Expression> expressions = new ArrayList<>();
		int count = 0;
		int j = i;
		for (; j < v.size(); j++) {
			final Object obj = v.get(j);
			if (obj instanceof Sequence(final List<Expression> exp)) {
				expressions.addAll(exp);
				count++;
			} else if (obj instanceof final Expression exp /* && !(obj instanceof Or) */) {
				expressions.add(exp);
				count++;
			} else {
				break;
			}
		}
		final int minimumSequences = 1;
		if (count <= minimumSequences) {
			return false;
		} else {
			v.subList(i, j).clear();
			v.add(i, new Sequence(expressions));
			return true;
		}
	}

	private static boolean mergeOr(final List<Object> v, final int i) {
		final List<Expression> expressions = new ArrayList<>();
		if (!(v.get(i) instanceof Expression)) {
			return false;
		}
		expressions.add((Expression) v.get(i));
		int count = 0;
		int j = i + 1;
		for (; j < v.size() - 1; j++) {
			if (isSymbol(v.get(j), SymbolType.VERTICAL_LINE)
					&& v.get(j + 1) instanceof Or(final List<Expression> exp)) {
				expressions.addAll(exp);
				count++;
				j++;
			} else if (isSymbol(v.get(j), SymbolType.VERTICAL_LINE) && v.get(j + 1) instanceof final Expression exp) {
				expressions.add(exp);
				count++;
				j++;
			} else {
				break;
			}
		}
		if (count == 0) {
			return false;
		} else {
			v.subList(i, j).clear();
			v.add(i, new Or(expressions));
			return true;
		}
	}

	private static boolean asterisk(final List<Object> v, final int i) {
		if (i + 1 < v.size()
				&& v.get(i) instanceof final Expression exp
				&& isSymbol(v.get(i + 1), SymbolType.ASTERISK)) {
			v.subList(i, i + 2).clear();
			v.add(i, new ZeroOrMore(exp));
			return true;
		}
		return false;
	}

	private static boolean plus(final List<Object> v, final int i) {
		if (i + 1 >= v.size()) {
			return false;
		}
		if (v.get(i) instanceof final Expression exp && isSymbol(v.get(i + 1), SymbolType.PLUS)) {
			v.subList(i, i + 2).clear();
			v.add(i, new OneOrMore(exp));
			return true;
		}
		return false;
	}

	private static boolean questionMark(final List<Object> v, final int i) {
		if (i + 1 >= v.size()) {
			return false;
		}
		if (v.get(i) instanceof final Expression exp && isSymbol(v.get(i + 1), SymbolType.QUESTION_MARK)) {
			v.subList(i, i + 2).clear();
			v.add(i, new ZeroOrOne(exp));
			return true;
		}
		return false;
	}
}
