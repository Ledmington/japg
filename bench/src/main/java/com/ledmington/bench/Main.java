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
package com.ledmington.bench;

import com.ledmington.automata.AutomataUtils;
import com.ledmington.automata.DFA;
import com.ledmington.automata.DFAMinimizer;
import com.ledmington.automata.EpsilonNFAToNFA;
import com.ledmington.automata.GrammarToEpsilonNFA;
import com.ledmington.automata.NFA;
import com.ledmington.automata.NFAToDFA;
import com.ledmington.ebnf.Grammar;
import com.ledmington.ebnf.Parser;
import com.ledmington.generator.GrammarChecker;

public class Main {

	private interface TriConsumer<X, Y, Z> {
		void accept(X x, Y y, Z z);
	}

	private static long memory() {
		final Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	@SuppressWarnings("PMD.DoNotCallGarbageCollectionExplicitly")
	public static void main(final String[] args) {
		if (args.length > 0) {
			System.err.println("Arguments provided but not needed. Ignoring them.");
		}

		final String grammarText = """
				start = SIGN number ;
				number = ZERO | non_zero ;
				non_zero = DIGIT_EXCLUDING_ZERO DIGIT* ;
				ZERO = "0" ;
				SIGN = ( "+" | "-" )? ;
				DIGIT_EXCLUDING_ZERO = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
				DIGIT = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
				""";

		final GrammarToEpsilonNFA g2enfa = new GrammarToEpsilonNFA();
		final EpsilonNFAToNFA enfa2nfa = new EpsilonNFAToNFA();
		final NFAToDFA nfa2dfa = new NFAToDFA();
		final DFAMinimizer DFAmin = new DFAMinimizer();

		final int iterations = 10;
		long t;
		for (int i = 0; i < iterations; i++) {
			System.gc();
			final long initialUsedMemory = memory();

			t = System.nanoTime();
			final Grammar g = Parser.parse(grammarText);
			final long grammarParsingTime = System.nanoTime() - t;
			final long grammarParsingMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			GrammarChecker.check(g);
			final long grammarCheckingTime = System.nanoTime() - t;
			final long grammarCheckingMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			final NFA epsilonNFA = g2enfa.convert(g);
			final long grammarToEpsilonNFATime = System.nanoTime() - t;
			final long grammarToEpsilonNFAMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			AutomataUtils.assertEpsilonNFAValid(epsilonNFA);
			final long epsilonNFACheckingTime = System.nanoTime() - t;
			final long epsilonNFACheckingMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			final NFA nfa = enfa2nfa.convert(epsilonNFA);
			final long epsilonNFAToNFATime = System.nanoTime() - t;
			final long epsilonNFAToNFAMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			AutomataUtils.assertNFAValid(nfa);
			final long NFACheckingTime = System.nanoTime() - t;
			final long NFACheckingMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			final DFA dfa = nfa2dfa.convert(nfa);
			final long NFAToDFATime = System.nanoTime() - t;
			final long NFAToDFAMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			AutomataUtils.assertDFAValid(dfa);
			final long DFACheckingTime = System.nanoTime() - t;
			final long DFACheckingMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			final DFA minimizedDFA = DFAmin.minimize(dfa);
			final long minimizingDFATime = System.nanoTime() - t;
			final long minimizingDFAMemory = memory() - initialUsedMemory;
			System.gc();

			t = System.nanoTime();
			AutomataUtils.assertDFAValid(minimizedDFA);
			final long minimizedDFACheckingTime = System.nanoTime() - t;
			final long minimizedDFACheckingMemory = memory() - initialUsedMemory;
			System.gc();

			final long totalTime = grammarParsingTime
					+ grammarCheckingTime
					+ grammarToEpsilonNFATime
					+ epsilonNFACheckingTime
					+ epsilonNFAToNFATime
					+ NFACheckingTime
					+ NFAToDFATime
					+ DFACheckingTime
					+ minimizingDFATime
					+ minimizedDFACheckingTime;

			final long totalMemory = grammarParsingMemory
					+ grammarCheckingMemory
					+ grammarToEpsilonNFAMemory
					+ epsilonNFACheckingMemory
					+ epsilonNFAToNFAMemory
					+ NFACheckingMemory
					+ NFAToDFAMemory
					+ DFACheckingMemory
					+ minimizingDFAMemory
					+ minimizedDFACheckingMemory;

			final TriConsumer<String, Long, Long> printer = (name, time, memory) -> System.out.printf(
					" %-16s : %,15d ns | %6.2f %%  |  %,11d B | %6.2f %%%n",
					name,
					time,
					(double) time / (double) totalTime * 100.0,
					memory,
					(double) memory / (double) totalMemory * 100.0);

			printer.accept("Grammar parse", grammarParsingTime, grammarParsingMemory);
			printer.accept("Grammar check", grammarCheckingTime, grammarCheckingMemory);
			printer.accept("Grammar to ε-NFA", grammarToEpsilonNFATime, grammarToEpsilonNFAMemory);
			printer.accept("ε-NFA check", epsilonNFACheckingTime, epsilonNFACheckingMemory);
			printer.accept("ε-NFA to NFA", epsilonNFAToNFATime, epsilonNFAToNFAMemory);
			printer.accept("NFA check", NFACheckingTime, NFACheckingMemory);
			printer.accept("NFA to DFA", NFAToDFATime, NFAToDFAMemory);
			printer.accept("DFA check", DFACheckingTime, DFACheckingMemory);
			printer.accept("DFA minimization", minimizingDFATime, minimizingDFAMemory);
			printer.accept("Min-DFA check", minimizedDFACheckingTime, minimizedDFACheckingMemory);
			printer.accept("Total", totalTime, totalMemory);
			System.out.println();
		}
	}
}
