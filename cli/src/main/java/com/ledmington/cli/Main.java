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
package com.ledmington.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Parser;
import com.ledmington.ebnf.Utils;
import com.ledmington.generator.Generator;

public class Main {

	@SuppressWarnings("PMD.DoNotTerminateVM")
	private static void die(final String format, final Object... args) {
		System.err.printf(format, args);
		System.exit(-1);
	}

	public static void main(final String[] args) {
		String grammarFile = null;
		String outputFile = null;
		boolean verbose = false;
		boolean generateMainMethod = false;
		String packageName = "unknown";
		boolean overwriteOutputFile = false;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-h", "--help" -> {
					System.out.println(String.join(
							"\n",
							"",
							" japg - A zero-dependency parser generator",
							"",
							" -h, --help             Displays this message and exits.",
							" -v, --verbose          Displays useful information while parsing the grammar.",
							" -g, --grammar GRAMMAR  Reads the EBNF grammar from the given GRAMMAR file.",
							" -o, --output OUTPUT    Writes the resulting parser in the given OUTPUT file.",
							" -m, --main             Generates a main method to create a self-contained parser.",
							" -p, --package PACKAGE  Sets PACKAGE as the package name of the generated class.",
							" -w, --overwrite        If specified, overwrites the output file if it is already present.",
							""));
					System.exit(0);
					return;
				}
				case "-v", "--verbose" -> {
					if (verbose) {
						die("Cannot set verbosity twice.");
					}
					verbose = true;
				}
				case "-g", "--grammar" -> {
					i++;
					if (grammarFile != null) {
						die("Cannot set grammar file twice, was already '%s'.%n", grammarFile);
					}
					grammarFile = args[i];
				}
				case "-o", "--output" -> {
					i++;
					if (outputFile != null) {
						die("Cannot set output file twice, was already '%s'.%n", outputFile);
					}
					outputFile = args[i];
				}
				case "-p", "--package" -> {
					i++;
					if (!packageName.equals("unknown")) {
						die("Cannot set package name twice, was already '%s'.%n", packageName);
					}
					packageName = args[i];
				}
				case "-m", "--main" -> {
					if (generateMainMethod) {
						die("Cannot generate main method twice.");
					}
					generateMainMethod = true;
				}
				case "-w", "--overwrite" -> {
					if (overwriteOutputFile) {
						die("Cannot overwrite output file twice.");
					}
					overwriteOutputFile = true;
				}
				default -> die("Unknown command-line argument: '%s'.%n", args[i]);
			}
		}

		if (grammarFile == null) {
			die("No grammar file was set.%n");
		}
		if (outputFile == null) {
			die("No output file was set.%n");
		}

		final Grammar g;
		try {
			g = Parser.parse(Files.readString(Path.of(grammarFile)));
			if (verbose) {
				System.out.println(Utils.prettyPrint(g));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (outputFile.endsWith(".java")) {
			outputFile = outputFile.substring(0, outputFile.length() - 5);
		}
		final Path outputPath = Path.of(outputFile + ".java").normalize().toAbsolutePath();

		if (!overwriteOutputFile && outputPath.toFile().exists()) {
			System.out.printf("Output file '%s' already exists, skipping.%n", outputPath);
			System.exit(0);
		}

		final int idx = outputFile.lastIndexOf(File.separator);
		final String className = idx < 0 ? outputFile : outputFile.substring(idx + 1);
		final String indent = "\t";
		final String output = Generator.generate(g, className, packageName, indent, generateMainMethod);

		try (final BufferedWriter bw = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
			bw.write(output);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
