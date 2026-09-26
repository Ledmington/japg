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

import java.util.Arrays;

public final class TextIterator {

	private final char[] arr;
	private int pos = 0;

	public TextIterator(final String s) {
		this.arr = s.toCharArray();
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Arrays.hashCode(arr);
		h = 31 * h + pos;
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof TextIterator it)) {
			return false;
		}
		return this.pos == it.pos && Arrays.equals(this.arr, it.arr);
	}
}
