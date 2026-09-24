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

import java.util.List;

/**
 * An element of an EBNF grammar which represents different possibilities of expressions.
 *
 * @param expressions The alternated expressions.
 */
public record Or(List<Expression> expressions) implements Composite {

	/**
	 * Creates a new Alternation Node with the given expressions.
	 *
	 * @param expressions The expressions to be alternated.
	 */
	public Or(final Expression... expressions) {
		this(List.of(expressions));
	}
}
