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

import com.google.common.graph.EndpointPair;
import dev.crmyers.deuterium.BaseTestCase;
import dev.crmyers.deuterium.data.DeuteriumFile;
import dev.crmyers.deuterium.data.DeuteriumGraph;
import dev.crmyers.deuterium.data.exception.FileFormatException;
import dev.crmyers.deuterium.data.Node;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Protobuf file saver.
 */
@SuppressWarnings("UnstableApiUsage")
@Log4j2
class ProtobufFileSaverTest extends BaseTestCase {
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
	static void cleanup_global() throws IOException {
		File cwd = new File(System.getProperty("user.dir"));
		File[] d2oFiles = cwd.listFiles();
		if (d2oFiles == null)
			return;
		for (File file : d2oFiles) {
			if (file.getName().endsWith(".d2o")) {
				if (!file.delete())
					throw new IOException("Failed to delete test-generated file " + file.getName());
			}
		}
	}

	/**
	 * Test successful save & retrieve operation
	 */
	@Test
	void successfulSaveAndRetrieval() throws IOException {
		final DeuteriumFile inputFile = generateTestFile();
		fileSaver.saveFile("successfulSaveAndRetrieval.d2o", inputFile);
		final DeuteriumFile loadedFile = fileSaver.loadFile("successfulSaveAndRetrieval.d2o");

		assertThat(loadedFile, notNullValue());
		assertThat(loadedFile.getName(), is(equalTo(inputFile.getName())));
		assertThat(loadedFile.getDescription(), is(equalTo(inputFile.getDescription())));
		assertThat(loadedFile.getGraphs().values().size(), is(equalTo(inputFile.getGraphs().values().size())));

		// Order is irrelevant, the data stored is effectively unordered
		for (DeuteriumGraph loadedGraph : loadedFile.getGraphs().values()) {
			// Get the corresponding graph from the input file and check to make sure it's identical to the loaded one
			assertThat(loadedGraph.getId(), notNullValue());
			final DeuteriumGraph inputGraph = inputFile.getGraphs().get(loadedGraph.getId());
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
			}

			// Make sure the graph was copied over correctly
			for (EndpointPair<Node> inputEdge : inputGraph.edges()) {
				final Node from = loadedGraph.getNodes().get(inputEdge.nodeU().getId());
				final Node to = loadedGraph.getNodes().get(inputEdge.nodeV().getId());
				assertTrue(loadedGraph.hasEdgeConnecting(from, to));
			}

			// Make sure all history was copied over correctly. Order DOES matter here.
			for (int i = 0; i < loadedGraph.getHistory().size(); i++)
				assertThat(loadedGraph.getHistory().get(i), equalTo(inputGraph.getHistory().get(i)));
		}
	}

	/**
	 * Test loading a file that doesn't exist
	 */
	@Test
	void loadNonexistentFile() {
		assertThrows(FileNotFoundException.class, () -> fileSaver.loadFile("does_not_exist.d2o"));
	}

	@Test
	void loadCorruptFile() throws IOException {
		FileOutputStream outputStream = new FileOutputStream(new File("corrupted.d2o"));
		outputStream.write("This is bad data that should yield an error".getBytes(StandardCharsets.UTF_8));
		outputStream.close();
		assertThrows(FileFormatException.class, () -> fileSaver.loadFile("corrupted.d2o"));
	}
}
