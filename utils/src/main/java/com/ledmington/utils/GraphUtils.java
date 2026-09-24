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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/** Collection of common utilities and algorithms on graphs. */
public final class GraphUtils {

	private GraphUtils() {}

	/**
	 * Breadth-First Search.
	 *
	 * @param startingNode The starting node.
	 * @param neighbors The function to use to obtain the set of neighbors of a given node.
	 * @return The set of visited nodes (it always includes the starting node).
	 * @param <X> The type of a node.
	 */
	public static <X> Set<X> bfs(final X startingNode, final Function<X, Set<X>> neighbors) {
		return bfs(startingNode, neighbors, _ -> {});
	}

	/**
	 * Breadth-First Search.
	 *
	 * @param startingNode The starting node.
	 * @param neighbors The function to use to obtain the set of neighbors of a given node.
	 * @param onVisit The function to be called on each visited node.
	 * @return The set of visited nodes (it always includes the starting node).
	 * @param <X> The type of a node.
	 */
	public static <X> Set<X> bfs(final X startingNode, final Function<X, Set<X>> neighbors, final Consumer<X> onVisit) {
		Objects.requireNonNull(startingNode, "Null starting nodes.");
		Objects.requireNonNull(neighbors, "Null neighbors function.");
		Objects.requireNonNull(onVisit, "Null onVisit consumer.");

		final Set<X> visited = new HashSet<>();
		final Queue<X> q = new ArrayDeque<>();
		q.add(startingNode);
		while (!q.isEmpty()) {
			final X current = q.remove();
			if (visited.contains(current)) {
				continue;
			}
			onVisit.accept(current);
			visited.add(current);
			q.addAll(neighbors.apply(current));
		}
		return visited;
	}
}
