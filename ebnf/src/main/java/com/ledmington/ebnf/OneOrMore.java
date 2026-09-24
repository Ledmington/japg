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
 * An EBNF expression which maps to the same expression one or more times.
 *
 * @param inner The inner expression which can be repeated.
 */
public record OneOrMore(Expression inner) implements Container {

	/**
	 * Creates a new OneOrMore object with the given inner expression.
	 *
	 * @param inner The repeated expression.
	 */
	public OneOrMore {
		Objects.requireNonNull(inner);
	}
}
