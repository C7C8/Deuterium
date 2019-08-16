/*
 * Copyright (c) 2019 Christopher Myers
 *
 * This file is part of Deuterium.
 *
 * Deuterium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Deuterium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Deuterium.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.crmyers.deuterium.data;

import dev.crmyers.deuterium.BaseTestCase;
import dev.crmyers.deuterium.data.exception.CycleException;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.jupiter.api.Assertions.*;

class DeuteriumGraphTest extends BaseTestCase {

	/**
	 * Make sure node dependency works
	 */
	@Test
	void dependentOn() {
		DeuteriumGraph graph = new DeuteriumGraph();
		graph.putEdge(node("A"), node("B"));
		assertTrue(graph.dependsOn(node("A"), node("B")));
	}

	/**
	 * Make sure nodes are properly reported as not dependent
	 */
	@Test
	void notDependentOn() {
		DeuteriumGraph graph = new DeuteriumGraph();
		graph.addNode(node("A"));
		graph.addNode(node("B"));
		assertFalse(graph.dependsOn(node("A"), node("B")));
	}

	/**
	 * Helper to generate a list of edges
	 * @param edges Array of letters that, when paired, form edges
	 * @return A list of node pairs (edges)
	 */
	private static List<Pair<Node, Node>> makeEdges(String... edges) {
		final ArrayList<Pair<Node, Node>> ret = new ArrayList<>();
		for (int i = 0; i < edges.length;)
			ret.add(new Pair<>(node(edges[i++]), node(edges[i++])));
		return ret;
	}

	/**
	 *  Stupid helper so I don't have to do new String[] {...} for every graph solution
	 * @param solutions Solutions list
	 * @return The exact same list. JUnit, you suck.
	 */
	private static String[] makeSolutions(String... solutions) {
		return solutions;
	}

	/**
	 * Provider for topological sort test cases
	 * @return Topological sort test cases
	 */
	static Stream<Arguments> topologicalSortGraphProvider() {
		/*
		Graph layout:
			 F -> A <- E
			\/		  \/
			 C -> D -> B
		 */
		List<Pair<Node, Node>> graph1 = makeEdges("F", "A",
												  "F", "C",
												  "E", "B",
												  "E", "A",
												  "C", "D",
												  "D", "B");
		return Stream.of(
				Arguments.of(graph1, "F", makeSolutions("B D C A F", "A B D C F")),
				Arguments.of(graph1, "C", makeSolutions("B D C")),
				Arguments.of(graph1, "E", makeSolutions("B A E", "A B E")),
				Arguments.of(graph1, "D", makeSolutions("B D")),
				Arguments.of(graph1, "B", makeSolutions("B")),
				Arguments.of(graph1, "A", makeSolutions("A"))
		);
	}

	/**
	 * Basic topological sort functionality test
	 */
	@ParameterizedTest
	@MethodSource("topologicalSortGraphProvider")
	void topologicalSort(List<Pair<Node, Node>> edges, String dependency, String... expected) {
		final DeuteriumGraph graph = new DeuteriumGraph();
		for (Pair<Node, Node> edge : edges)
			graph.putEdge(edge.getKey(), edge.getValue());
		final List<Node> sorted = graph.solveDependencies(node(dependency));

		// Concatenate node names into space-separated string
		StringBuilder result = new StringBuilder();
		for (Node node : sorted)
			result.append(node.getName()).append(" ");
		result.deleteCharAt(result.length() - 1); // remove trailing space

		// The exact order is non-deterministic; there may be many valid topological sorts of the graph, so expected
		// is just an array of possible ones.
		assertThat(result.toString(), isIn(expected));
	}

	/**
	 * Make sure that cycles are detected and exceptions are thrown appropriately
	 */
	@Test
	void topologicalSortDetectCycle() {
		final DeuteriumGraph graph = new DeuteriumGraph();
		// Four nodes in a cycle with one unrelated node hanging off to the side
		graph.putEdge(node("A"), node("B"));
		graph.putEdge(node("B"), node("C"));
		graph.putEdge(node("C"), node("D"));
		graph.putEdge(node("D"), node("A"));
		graph.putEdge(node("E"), node("A"));

		try {
			graph.solveDependencies(node("A"));
			fail("Graph did not detect cycle");
		} catch (CycleException ex) {
			assertThat(ex.getNodes(), hasItems(node("A"), node("B"), node("C"), node("D")));
			assertThat(ex.getNodes(), not(hasItem(node("E"))));
		}

		// Stress test on highly connected graphs
		final DeuteriumFile hellFile = generateTestFile();
		for (DeuteriumGraph hellGraph : hellFile.getGraphs().values()) {
			for (Node node : hellGraph.getNodes().values())
				assertThrows(CycleException.class, () -> hellGraph.solveDependencies(node));
		}
	}
}
