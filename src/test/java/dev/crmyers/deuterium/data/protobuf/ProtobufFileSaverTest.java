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

package dev.crmyers.deuterium.data.protobuf;

import dev.crmyers.deuterium.data.*;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Tests for Protobuf file saver.
 */
@Log4j2
public class ProtobufFileSaverTest {
	private static ProtobufFileSaver fileSaver;

	/**
	 * Global setup.
	 */
	@BeforeAll
	static void setup_global() {
		fileSaver = new ProtobufFileSaver();
	}

	/**
	 * Delete any .d2o files left over.
	 */
	@AfterAll
	static void cleanup_global() {

	}

	/**
	 * Generate a test file for saving.
	 * @return Deuterium file object with some test data inside.
	 */
	private DeuteriumFile getTestFile() {
		// Make 5 different graphs
		final HashMap<UUID, Graph> graphs = new HashMap<>();
		for (int i = 0; i < 5; i++) {
			// Assemble a list of nodes with random IDs
			final HashMap<UUID, Node> nodes = new HashMap<>();
			for (int j = 1; j <= 10; j++) {
				final UUID nodeId = UUID.randomUUID();
				nodes.put(nodeId, new Node(nodeId, "Node " + j, "Details " + j , new HashMap<>()));
			}

			// Link all nodes together and add some ADD history to them
			final ArrayList<NodeHistory> nodeHistories = new ArrayList<>();
			for (Node node : nodes.values()) {
				for (Node neighbor : nodes.values()) {
					if (neighbor == node)
						continue;
					node.getNeighbors().put(neighbor.getId(), neighbor);
				}

				nodeHistories.add(new NodeHistory(UUID.randomUUID(), new Date(), node.getId(), Action.ADD, "Add"));
			}

			final UUID graphId = UUID.randomUUID();
			graphs.put(graphId, new Graph(graphId, "Graph " + i, "Description " + i, nodes, nodeHistories));
		}

		return new DeuteriumFile("Test file", "Test file description", graphs);
	}

	/**
	 * Test successful save & retrieve operation
	 */
	@Test
	public void successfulSaveAndRetrievalTest() throws IOException {
		final DeuteriumFile inputFile = getTestFile();
		fileSaver.saveNewFile("successfulSaveAndRetrieval.d2o", inputFile);
		final DeuteriumFile loadedFile = fileSaver.loadFile("successfulSaveAndRetrieval.d2o");

		assertThat(loadedFile, notNullValue());
		assertThat(loadedFile.getName(), is(equalTo(inputFile.getName())));
		assertThat(loadedFile.getDescription(), is(equalTo(inputFile.getDescription())));
		assertThat(loadedFile.getGraphs().values().size(), is(equalTo(inputFile.getGraphs().values().size())));

		// Order is irrelevant, the data stored is effectively unordered
		for (Graph loadedGraph : loadedFile.getGraphs().values()) {
			// Get the corresponding graph from the input file and check to make sure it's identical to the loaded one
			assertThat(loadedGraph.getId(), notNullValue());
			final Graph inputGraph = inputFile.getGraphs().get(loadedGraph.getId());
			assertThat(inputGraph, notNullValue());

			assertThat(loadedGraph.getDescription(), equalTo(inputGraph.getDescription()));
			assertThat(loadedGraph.getName(), equalTo(inputGraph.getName()));
			assertThat(loadedGraph.getNodes().size(), equalTo(inputGraph.getNodes().size()));
			assertThat(loadedGraph.getHistory().size(), equalTo(loadedGraph.getHistory().size()));

			// Make sure all nodes were copied over correctly
			for (Node loadedNode : loadedGraph.getNodes().values()) {
				assertThat(loadedNode.getId(), notNullValue());
				final Node inputNode = inputGraph.getNodes().get(loadedNode.getId());
				assertThat(inputNode, notNullValue());

				assertThat(loadedNode.getName(), equalTo(inputNode.getName()));
				assertThat(loadedNode.getDetails(), equalTo(inputNode.getDetails()));

				// Ensure that the loaded node's neighbor set contains all elements in the original set of nodes,
				// *except* our current node's ID. Seriously, Java lambdas are UGLY.
				assertThat(loadedNode.getNeighbors().keySet(),
						containsInAnyOrder(loadedNode.getNeighbors().keySet().stream().
								filter(uuid -> !uuid.equals(loadedNode.getId()))
								.collect(Collectors.toSet())));
			}

			// Make sure all history was copied over correctly. Order DOES matter here.
			for (int i = 0; i < loadedGraph.getHistory().size(); i++)
				assertThat(loadedGraph.getHistory().get(i), equalTo(inputGraph.getHistory().get(i)));
		}
	}
}
