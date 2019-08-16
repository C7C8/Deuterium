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

import dev.crmyers.deuterium.data.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class BaseTestCase {
	/**
	 * Generate a test file for saving.
	 * @return Deuterium file object with some test data inside.
	 */
	protected DeuteriumFile generateTestFile() {
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
			final ArrayList<NodeHistory> nodeHistories = new ArrayList<>();
			for (Node node : nodes.values()) {
				for (Node neighbor : nodes.values()) {
					if (node.getId().equals(neighbor.getId()))
						continue;
					graph.getGraph().putEdge(node, neighbor);
				}

				nodeHistories.add(new NodeHistory(UUID.randomUUID(), new Date(), node.getId(), Action.ADD, "Add"));
			}
			graph.setHistory(nodeHistories);
			graphs.put(graph.getId(), graph);
		}

		return new DeuteriumFile("Test file", "Test file description", graphs);
	}
}
