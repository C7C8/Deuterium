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

package dev.crmyers.deuterium;

import dev.crmyers.deuterium.model.*;

import java.util.*;

public class BaseTestCase {

	private static HashMap<String, Node> testNodes;

	/**
	 * Generate a test file for saving.
	 * @return Deuterium file object with some test data inside.
	 */
	protected static DeuteriumFile generateTestFile() {
		// Make 5 different graphs
		final HashMap<UUID, DeuteriumGraph> graphs = new HashMap<>();
		for (int i = 0; i < 5; i++) {

			final DeuteriumGraph graph = new DeuteriumGraph();
			graph.setId(UUID.randomUUID());
			graph.setName("Graph " + i);
			graph.setDescription("Graph description " + i);

			// Assemble a list of nodes with random IDs
			final HashMap<UUID, Node> nodes = new HashMap<>();
			for (int j = 1; j <= 10; j++) {
				final UUID nodeId = UUID.randomUUID();
				nodes.put(nodeId, new Node(nodeId, "Node " + j, "Details " + j));
			}
			graph.setNodes(nodes);

			// Link all nodes together and add some ADD history to them
//			final ArrayList<NodeHistory> nodeHistories = new ArrayList<>();
//			for (Node node : nodes.values()) {
//				for (Node neighbor : nodes.values()) {
//					if (node.getId().equals(neighbor.getId()))
//						continue;
//					graph.putEdge(node, neighbor);
//				}

//				nodeHistories.add(new NodeHistory(UUID.randomUUID(), new Date(), node.getId(), Action.ADD, "Add"));
//			}
//			graph.setHistory(nodeHistories);
			graphs.put(graph.getId(), graph);
		}

		return new DeuteriumFile("Test file", "Test file description", graphs);
	}

	/**
	 * Helper to get a generated test node, with name mappings from A-Z
	 * @param name Node name (A-Z, case sensitive)
	 * @return Test node
	 */
	protected static Node node(String name) {
		if (testNodes == null) {
			testNodes = new HashMap<>();
			for (char letter = 'A'; letter <= 'Z'; letter++)
				testNodes.put(String.valueOf(letter), new Node(String.valueOf(letter)));
		}
		return testNodes.get(name);
	}

	/**
	 * Helper to create a graph from a list of edges
	 * @param edges Letters that, when paired, form edges
	 * @return Graph constructed from input edges
	 */
	protected static DeuteriumGraph makeGraph(String... edges) {
		final DeuteriumGraph graph = new DeuteriumGraph();
		for (int i = 0; i < edges.length;) {
			final Node from = node(edges[i++]);
			final Node to = node(edges[i++]);
			graph.putEdge(from, to);
		}
		return graph;
	}
}
