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

import java.util.Objects;

/** A custom extension of the Java standard library's {@link StringBuilder} to easily handle indentation. */
public final class IndentedStringBuilder {

	private final String indentation;
	private int indentLevel = 0;
	private final StringBuilder sb = new StringBuilder();

	/**
	 * Creates a new IndentedStringBuilder with the given level of indentation.
	 *
	 * @param indent The level of indentation.
	 */
	public IndentedStringBuilder(final String indent) {
		this.indentation = Objects.requireNonNull(indent);
	}

	private void add(final String s) {
		if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
			sb.append(indentation.repeat(indentLevel));
		}

		if (s == null) {
			sb.append("null");
			return;
		}

		for (int i = 0; i < s.length(); i++) {
			final char ch = s.charAt(i);
			sb.append(ch);
			if (ch == '\n' && i < s.length() - 1) {
				sb.append(indentation.repeat(indentLevel));
			}
		}
	}

	/**
	 * Appends the given String and adds the proper indentation where needed.
	 *
	 * @param s The String to be appended.
	 * @return This instance of IndentedStringBuilder.
	 */
	public IndentedStringBuilder append(final String s) {
		add(s);
		return this;
	}

	/**
	 * Appends the given character and adds the proper indentation where needed.
	 *
	 * @param c The character to be appended.
	 * @return This instance of IndentedStringBuilder.
	 */
	public IndentedStringBuilder append(final char c) {
		add(String.valueOf(c));
		return this;
	}

	/**
	 * Adds a single level of indentation.
	 *
	 * @return This instance of IndentedStringBuilder.c
	 */
	public IndentedStringBuilder indent() {
		indentLevel++;
		return this;
	}

	/**
	 * Removes a single level of indentation.
	 *
	 * @return This instance of IndentedStringBuilder.
	 * @throws IllegalStateException When calling this method with an indentation level of 0.
	 */
	public IndentedStringBuilder deindent() {
		indentLevel--;
		if (indentLevel < 0) {
			throw new IllegalStateException("Negative indentation level.");
		}
		return this;
	}

	public String toString() {
		if (indentLevel != 0) {
			throw new IllegalStateException("Indentation level not zero.");
		}
		return sb.toString();
	}
}
