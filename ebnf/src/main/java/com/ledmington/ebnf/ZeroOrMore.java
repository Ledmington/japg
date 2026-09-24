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

import java.util.Objects;

/**
 * An element of an EBNF grammar which represents the ability to repeat a given expression as many times as needed (even
 * zero).
 *
 * @param inner The repeated Expression.
 */
public record ZeroOrMore(Expression inner) implements Container {

	/**
	 * Creates a new ZeroOrMore object with the given inner expression.
	 *
	 * @param inner The repeated expression.
	 */
	public ZeroOrMore {
		Objects.requireNonNull(inner);
	}
}
