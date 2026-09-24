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

/**
 * Utility class to group together two objects of different types.
 *
 * @param <X> The type of the first element.
 * @param <Y> The type of the second element.
 * @param first The first element of the pair.
 * @param second The second element of the pair.
 */
public record Pair<X, Y>(X first, Y second) {

	/**
	 * Utility method to create a Pair.
	 *
	 * @param first The first element.
	 * @param second The second element.
	 * @return A new Pair with the given elements.
	 * @param <A> The type of the first element.
	 * @param <B> The type of the second element.
	 */
	public static <A, B> Pair<A, B> of(final A first, final B second) {
		return new Pair<>(first, second);
	}
}
