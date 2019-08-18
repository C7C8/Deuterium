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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
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
		DeuteriumGraph graph1 = makeGraph("F", "A",
				"F", "C",
				"E", "B",
				"E", "A",
				"C", "D",
				"D", "B");
		return Stream.of(
				Arguments.of(graph1, "F", makeSolutions("BDCAF", "ABDCF")),
				Arguments.of(graph1, "C", makeSolutions("BDC")),
				Arguments.of(graph1, "E", makeSolutions("BAE", "ABE")),
				Arguments.of(graph1, "D", makeSolutions("BD")),
				Arguments.of(graph1, "B", makeSolutions("B")),
				Arguments.of(graph1, "A", makeSolutions("A"))
		);
	}

	/**
	 * Basic topological sort functionality test
	 */
	@ParameterizedTest
	@MethodSource("topologicalSortGraphProvider")
	void topologicalSort(DeuteriumGraph graph, String dependency, String... expected) {
		final List<Node> sorted = graph.solveDependencies(node(dependency));

		// The exact order is non-deterministic; there may be many valid topological sorts of the graph, so expected
		// is just an array of possible ones.
		assertThat(sorted.stream().map(Node::toString).reduce("", (a, b) -> a + b), isIn(expected));
	}

	/**
	 * Make sure that cycles are detected and exceptions are thrown appropriately
	 */
	@Test
	void topologicalSortDetectCycle() {
		// Test 1: Four nodes in a cycle with one unrelated node hanging off to the side
		DeuteriumGraph graph = makeGraph("A", "B",
				"B", "C",
				"C", "D",
				"D", "A",
				"E", "A");
		try {
			graph.solveDependencies(node("A"));
			fail("Graph did not detect cycle 1");
		} catch (CycleException ex) {
			assertThat(ex.getNodes(), hasItems(node("A"), node("B"), node("C"), node("D")));
			assertThat(ex.getNodes(), not(hasItem(node("E"))));
		}

		/* Test 2: Graph 1 configuration from provider tests, but with a node hanging off of E that's in a cycle with
		another node.
		Graph layout:
			 F -> A <- E -> G <-> H
			\/		  \/
			 C -> D -> B
		 */
		graph = makeGraph("F", "A",
				"F", "C",
				"E", "B",
				"E", "A",
				"C", "D",
				"D", "B",
				"E", "G",
				"G", "H",
				"H", "G");

		// Solving for F should work since F won't hit G
		graph.solveDependencies(node("F"));

		// Solving for E should throw an exception that contains only G and H, as those are the nodes involved in the cycle
		try {
			graph.solveDependencies(node("E"));
			fail("Graph did not detect cycle 2");
		} catch (CycleException ex) {
			assertThat(ex.getNodes().size(), equalTo(2));
			assertThat(ex.getNodes(), hasItems(node("G"), node("H")));
		}
	}

	static Stream<Arguments> findAllExclusivelyDependentOnProvider() {
		/* Graph 1:
			A -> B -> C, D
		    |-> F

		*/
		final DeuteriumGraph graph1 = makeGraph("A", "B",
				"A", "F",
				"B", "C",
				"B", "D");

		/*
		Graph 2:
			 F -> A <- E
			\/		  \/
			 C -> D -> B -> G -> H, I

		 */
		final DeuteriumGraph graph2 = makeGraph("F", "A",
				"F", "C",
				"E", "B",
				"E", "A",
				"C", "D",
				"D", "B",
				"B", "G",
				"G", "H",
				"G", "I");
		return Stream.of(

				// Graph 1 -- simple tree
				Arguments.of(graph1, "B", "CD"),
				Arguments.of(graph1, "A", "BFCD"),
				Arguments.of(graph1, "C", ""),
				Arguments.of(graph1, "D", ""),
				Arguments.of(graph1, "F", ""),

				// Graph 2 -- more complications
				Arguments.of(graph2, "F", "CD"),
				Arguments.of(graph2, "A", ""),
				Arguments.of(graph2, "E", ""),
				Arguments.of(graph2, "C", "D"),
				Arguments.of(graph2, "B", "GHI"),
				Arguments.of(graph2, "G", "HI"),
				Arguments.of(graph2, "H", ""),
				Arguments.of(graph2, "I", "")
		);
	}
	/**
	 * Functionality of findAllExclusivelyDependentOn
	 */
	@ParameterizedTest
	@MethodSource("findAllExclusivelyDependentOnProvider")
	void findAllExclusivelyDependentOn(DeuteriumGraph graph, String node, String expected) {
		final Set<Node> results = graph.findAllExclusivelyDependentOn(node(node));
		assertThat(results, equalTo(expected.chars().mapToObj(n -> node(String.valueOf((char) n))).collect(Collectors.toSet())));
	}
}
