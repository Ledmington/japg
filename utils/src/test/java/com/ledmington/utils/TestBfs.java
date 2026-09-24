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
package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class TestBfs {

	private TestBfs() {}

	private static final Map<Integer, Set<Integer>> graph = Map.ofEntries(
			Map.entry(0, Set.of(1)),
			Map.entry(1, Set.of(0, 3)),
			Map.entry(2, Set.of(1, 2, 4)),
			Map.entry(3, Set.of()),
			Map.entry(4, Set.of(3)));

	@Test
	void reachability() {
		assertEquals(Set.of(0, 1, 3), GraphUtils.bfs(0, graph::get));
		assertEquals(Set.of(0, 1, 3), GraphUtils.bfs(1, graph::get));
		assertEquals(Set.of(0, 1, 2, 3, 4), GraphUtils.bfs(2, graph::get));
		assertEquals(Set.of(3), GraphUtils.bfs(3, graph::get));
		assertEquals(Set.of(3, 4), GraphUtils.bfs(4, graph::get));
	}

	private static final class Visitor<X> implements Consumer<X> {

		private final List<X> visited = new ArrayList<>();

		public Visitor() {}

		public List<X> getVisited() {
			return this.visited;
		}

		public void reset() {
			this.visited.clear();
		}

		@Override
		public void accept(final X x) {
			this.visited.add(x);
		}
	}

	@Test
	void visitors() {
		final Visitor<Integer> visitor = new Visitor<>();

		GraphUtils.bfs(0, graph::get, visitor);
		assertEquals(List.of(0, 1, 3), visitor.getVisited());
		visitor.reset();

		GraphUtils.bfs(1, graph::get, visitor);
		assertTrue(Set.of(List.of(1, 3, 0), List.of(1, 0, 3)).contains(visitor.getVisited()));
		visitor.reset();

		GraphUtils.bfs(2, graph::get, visitor);
		assertTrue(
				Set.of(List.of(2, 1, 4, 3, 0), List.of(2, 4, 1, 3, 0), List.of(2, 1, 4, 0, 3), List.of(2, 4, 1, 0, 3))
						.contains(visitor.getVisited()));
		visitor.reset();

		GraphUtils.bfs(3, graph::get, visitor);
		assertEquals(List.of(3), visitor.getVisited());
		visitor.reset();

		GraphUtils.bfs(4, graph::get, visitor);
		assertEquals(List.of(4, 3), visitor.getVisited());
		visitor.reset();
	}

	private static Stream<Arguments> nodes() {
		return graph.keySet().stream().map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("nodes")
	void determinism(final int root) {
		final Set<Integer> result1 = GraphUtils.bfs(root, graph::get);
		final Set<Integer> result2 = GraphUtils.bfs(root, graph::get);
		assertEquals(result1, result2);
	}
}
