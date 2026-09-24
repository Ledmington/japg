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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
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
			final String stripped = removeComments(input);
			final List<Token> tokens = tokenize(stripped);
			return parse(tokens);
		} catch (final IndexOutOfBoundsException ioobe) {
			throw new ParsingException(ioobe);
		}
	}

	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	private static String removeComments(final String input) {
		final StringBuilder sb = new StringBuilder(input.length());
		final int n = input.length();

		boolean inBlockComment = false;
		boolean inLineComment = false;
		boolean inString = false;

		for (int i = 0; i < n; i++) {
			final char c = input.charAt(i);

			if (inBlockComment) {
				// End of block comment
				if (i + 1 < n && c == '*' && input.charAt(i + 1) == '/') {
					inBlockComment = false;
					i++;
				}
			} else if (inLineComment) {
				// End of line comment
				if (c == '\n') {
					inLineComment = false;
					sb.append(c);
				}
			} else if (inString) {
				sb.append(c);
				if (c == '\\' && i + 1 < n) {
					i++;
					sb.append(input.charAt(i));
				} else if (c == '"') {
					inString = false;
				}
			} else {
				// Not inside a string or comment
				if (i + 1 < n && c == '/' && input.charAt(i + 1) == '*') {
					inBlockComment = true;
					i++;
				} else if (i + 1 < n && c == '/' && input.charAt(i + 1) == '/') {
					inLineComment = true;
					i++;
				} else {
					sb.append(c);
					if (c == '"') {
						inString = true;
					}
				}
			}
		}

		if (inBlockComment) {
			throw new ParsingException("Unterminated block comment.");
		}
		if (inString) {
			throw new ParsingException("Unterminated string literal.");
		}

		return sb.toString();
	}

	private static List<Token> tokenize(final String input) {
		final List<Token> tokens = new ArrayList<>();
		final StringCharacterIterator it = new StringCharacterIterator(input);
		while (it.current() != CharacterIterator.DONE) {
			final char ch = it.current();
			if (ch == Symbols.WHITESPACE.getCharacter()
					|| ch == Symbols.TAB.getCharacter()
					|| ch == Symbols.NEWLINE.getCharacter()) {
				skipWhitespaces(it);
			} else if (Character.isAlphabetic(ch) || ch == Symbols.UNDERSCORE.getCharacter()) {
				tokens.add(readWord(it));
			} else if (ch == Symbols.EQUAL_SIGN.getCharacter()) {
				tokens.add(Symbols.EQUAL_SIGN);
				it.next();
			} else if (ch == Symbols.SEMICOLON.getCharacter()) {
				tokens.add(Symbols.SEMICOLON);
				it.next();
			} else if (ch == Symbols.VERTICAL_LINE.getCharacter()) {
				tokens.add(Symbols.VERTICAL_LINE);
				it.next();
			} else if (ch == Symbols.LEFT_PARENTHESIS.getCharacter()) {
				tokens.add(Symbols.LEFT_PARENTHESIS);
				it.next();
			} else if (ch == Symbols.RIGHT_PARENTHESIS.getCharacter()) {
				tokens.add(Symbols.RIGHT_PARENTHESIS);
				it.next();
			} else if (ch == Symbols.PLUS.getCharacter()) {
				tokens.add(Symbols.PLUS);
				it.next();
			} else if (ch == Symbols.QUESTION_MARK.getCharacter()) {
				tokens.add(Symbols.QUESTION_MARK);
				it.next();
			} else if (ch == Symbols.DOT.getCharacter()) {
				tokens.add(Symbols.DOT);
				it.next();
			} else if (ch == Symbols.ASTERISK.getCharacter()) {
				tokens.add(Symbols.ASTERISK);
				it.next();
			} else if (ch == Symbols.DASH.getCharacter()) {
				tokens.add(Symbols.DASH);
				it.next();
			} else if (ch == Symbols.DOUBLE_QUOTES.getCharacter()) {
				tokens.add(readStringLiteral(it));
			} else {
				throw new ParsingException(String.format("Unknown character: '%c' (U+%04X).", ch, (int) ch));
			}
		}
		return tokens;
	}

	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	private static StringLiteral readStringLiteral(final StringCharacterIterator it) {
		if (it.current() != Symbols.DOUBLE_QUOTES.getCharacter()) {
			throw new AssertionError("Expected string literal to start with '\"'.");
		}
		it.next();
		final StringBuilder sb = new StringBuilder();
		while (it.current() != CharacterIterator.DONE && it.current() != Symbols.DOUBLE_QUOTES.getCharacter()) {
			if (it.current() == Symbols.NEWLINE.getCharacter()) {
				// string literals must be on the same line
				throw new ParsingException("Unexpected newline while reading string literal.");
			}
			final int idx = it.getIndex();
			if (it.current() == '\\') {
				it.next();
				if (it.current() == Symbols.DOUBLE_QUOTES.getCharacter()) {
					sb.append(Symbols.DOUBLE_QUOTES.getCharacter());
				} else if (it.current() == 'n') {
					sb.append('\n');
				} else if (it.current() == 't') {
					sb.append('\t');
				} else if (it.current() == '\\') {
					sb.append('\\');
				} else {
					it.setIndex(idx);
				}
			} else {
				sb.append(it.current());
			}
			it.next();
		}
		if (it.current() == CharacterIterator.DONE) {
			throw new ParsingException(String.format("Unclosed double quotes in string literal '%s'.", sb));
		}
		it.next();
		return new StringLiteral(sb.toString());
	}

	private static Word readWord(final StringCharacterIterator it) {
		final StringBuilder sb = new StringBuilder();
		do {
			sb.append(it.current());
			it.next();
		} while (it.current() != CharacterIterator.DONE
				&& (Character.isAlphabetic(it.current()) || it.current() == Symbols.UNDERSCORE.getCharacter()));
		return new Word(sb.toString());
	}

	private static void skipWhitespaces(final StringCharacterIterator it) {
		while (it.current() != CharacterIterator.DONE
				&& (it.current() == Symbols.WHITESPACE.getCharacter()
						|| it.current() == Symbols.TAB.getCharacter()
						|| it.current() == Symbols.NEWLINE.getCharacter())) {
			it.next();
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

	private static Optional<Pair<Integer, Integer>> findBrackets(final List<Object> v) {
		final int n = v.size();
		int leftBracketPosition = -1;
		for (int i = 0; i < n; i++) {
			if (v.get(i).equals(Symbols.LEFT_PARENTHESIS)) {
				leftBracketPosition = i;
				break;
			}
		}
		if (leftBracketPosition == -1) {
			return Optional.empty();
		}
		for (int i = leftBracketPosition + 1; i < n; i++) {
			if (v.get(i).equals(Symbols.LEFT_PARENTHESIS)) {
				leftBracketPosition = i;
			} else if (v.get(i).equals(Symbols.RIGHT_PARENTHESIS)) {
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
						"Unknown parsing state:%n%s",
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
			if (v.get(i) instanceof StringLiteral(final String literal)) {
				v.set(i, new Terminal(literal));
			}
		}
	}

	private static void convertWordsToNonTerminals(final List<Object> v) {
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof Word(final String content)) {
				v.set(i, new NonTerminal(content));
			}
		}
	}

	private static void convertDotsToAlternations(final List<Object> v) {
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i).equals(Symbols.DOT)) {
				v.set(
						i,
						new Or(IntStream.range(32, 127)
								.mapToObj(x -> (Expression) new Terminal("" + (char) x))
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
		if (v.get(i).equals(Symbols.LEFT_PARENTHESIS)
				&& v.get(i + 1) instanceof final Expression exp
				&& v.get(i + 2).equals(Symbols.RIGHT_PARENTHESIS)) {
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
				&& v.get(i + 1).equals(Symbols.EQUAL_SIGN)
				&& v.get(i + 2) instanceof final Expression exp
				&& v.get(i + 3).equals(Symbols.SEMICOLON)) {
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
			if (v.get(j).equals(Symbols.VERTICAL_LINE) && v.get(j + 1) instanceof Or(final List<Expression> exp)) {
				expressions.addAll(exp);
				count++;
				j++;
			} else if (v.get(j).equals(Symbols.VERTICAL_LINE) && v.get(j + 1) instanceof final Expression exp) {
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
				&& v.get(i + 1).equals(Symbols.ASTERISK)) {
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
		if (v.get(i) instanceof final Expression exp && v.get(i + 1).equals(Symbols.PLUS)) {
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
		if (v.get(i) instanceof final Expression exp && v.get(i + 1).equals(Symbols.QUESTION_MARK)) {
			v.subList(i, i + 2).clear();
			v.add(i, new ZeroOrOne(exp));
			return true;
		}
		return false;
	}
}
