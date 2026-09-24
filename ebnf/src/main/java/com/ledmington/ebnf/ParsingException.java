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

import java.io.Serial;

/** A common type for all exception which may occur during the parsing of a grammar. */
public final class ParsingException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -5819885757375952885L;

	/**
	 * Creates a new ParsingException with the given message.
	 *
	 * @param message The message of the exception.
	 */
	public ParsingException(final String message) {
		super(message);
	}

	/**
	 * Creates a new ParsingException with the given RuntimeException as the cause.
	 *
	 * @param cause The exception which caused this ParsingException to be thrown.
	 */
	public ParsingException(final RuntimeException cause) {
		super(cause);
	}
}
