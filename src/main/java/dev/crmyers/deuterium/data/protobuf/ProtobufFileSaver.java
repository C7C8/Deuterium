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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of file saving using Protobuf.
 */
@Log4j2
public class ProtobufFileSaver implements FileSaver {

	/**
	 * Load a Deuterium save file and return it in the form of data.
	 *
	 * @param filename Relative filename.
	 * @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	 * @throws FileFormatException When file format to load is invalid
	 * @throws FileNotFoundException Thrown if the file cannot be loaded
	 * @throws IOException           Generic IO exception (e.g. invalid file)
	 */
	@Override
	public DeuteriumFile loadFile(String filename) throws FileFormatException, FileNotFoundException, IOException {
		final long startTime = System.currentTimeMillis();
		FileInputStream input = new FileInputStream(filename);
		log.info("Loading objects from file {}", filename);

		// Sanity check: Does the file start with the right magic number?
		final byte[] magic = input.readNBytes(4);
		if (!(new String(magic)).equals("DEUT")) {
			// Convert magic string into hex printout for easier debugging
			StringBuilder builder = new StringBuilder("[ ");
			for (byte b : magic)
				builder.append(String.format("0x%02x ", b));
			builder.append("]");
			log.error("File {} not a Deuterium file, starts with {}", filename, builder.toString());
			throw new FileFormatException("Invalid file format");
		}

		// Unzip the input file (starting from the previous offset) and process basic file metadata
		DeuteriumFormat.DeuteriumFile protoFile = DeuteriumFormat.DeuteriumFile
				.parseDelimitedFrom(new GZIPInputStream(input));
		DeuteriumFile outputFile = new DeuteriumFile();
		outputFile.setName(protoFile.getName());
		outputFile.setDescription(protoFile.getDescription());

		int objectCount = 1;
		final HashMap<UUID, Graph> graphs = new HashMap<>();
		for (DeuteriumFormat.Graph protoGraph : protoFile.getGraphsMap().values()) {
			// Basic graph metadata
			objectCount++;
			final Graph graph = new Graph();
			graph.setId(UUID.fromString(protoGraph.getId()));
			graph.setName(protoGraph.getName());
			graph.setDescription(protoGraph.getDescription());
			log.debug("Unpacking graph {}: {}", graph.getId(), graph.getName());

			// Process nodes -- round 1, creation
			HashMap<UUID, Node> nodeMap = new HashMap<>();
			for (DeuteriumFormat.Node protoNode : protoGraph.getNodesMap().values()) {
				objectCount++;
				final Node node = new Node();
				node.setId(UUID.fromString(protoNode.getId()));
				node.setName(protoNode.getName());
				node.setDetails(protoNode.getDetails());
				nodeMap.put(node.getId(), node);
				log.debug("Unpacking node {}: {}", node.getId(), node.getName());
			}

			// Process nodes -- round 2, link nodes to each other
			for (DeuteriumFormat.Node protoNode : protoGraph.getNodesMap().values()) {
				final Node currentNode = nodeMap.get(UUID.fromString(protoNode.getId()));
				final Map<UUID, Node> current = currentNode.getDependencies();
				for (String neighborId : protoNode.getDependenciesList()) {
					final Node neighborNode = nodeMap.get(UUID.fromString(neighborId));
					current.put(neighborNode.getId(), neighborNode);
					neighborNode.getDependents().put(currentNode.getId(), currentNode);
				}
			}
			graph.setNodes(nodeMap);

			// Add node histories to the graph
			ArrayList<NodeHistory> nodeHistories = new ArrayList<>();
			for (DeuteriumFormat.NodeHistory protoHistory : protoGraph.getHistoryList()) {
				objectCount++;
				final NodeHistory history = new NodeHistory();
				history.setId(UUID.fromString(protoHistory.getId()));
				history.setDate(new Date(protoHistory.getDate()));
				history.setEditId(UUID.fromString(protoHistory.getEditId()));
				history.setAction(Action.values()[protoHistory.getAction().ordinal()]);
				history.setChange(protoHistory.getChange());
				log.debug("Unpacking history {} for node {} on {}", history.getId(), history.getEditId(), history.getDate());
				nodeHistories.add(history);
			}
			graph.setHistory(nodeHistories);
			graphs.put(graph.getId(), graph);
		}
		outputFile.setGraphs(graphs);
		log.info("Loaded {} objects from file in {} ms", objectCount, System.currentTimeMillis() - startTime);

		return outputFile;
	}

	/**
	 * Save a Deuterium data object to a new file.
	 *
	 * @param filename Relative filename.
	 * @param data     Data to save.
	 * @throws FileNotFoundException Thrown if the file cannot be saved because the containing folder does not exist
	 * @throws IOException           Generic IO exception (e.g. out of space)
	 */
	@Override
	public void saveFile(String filename, DeuteriumFile data) throws FileNotFoundException, IOException {
		// Map a DeuteriumFile object to a Protobuffer-style DeuteriumFile
		int objectCount = 1;
		final long startTime = System.currentTimeMillis();
		log.info("Beginning save of {} graphs to file {}", data.getGraphs().size(), filename);
		final DeuteriumFormat.DeuteriumFile.Builder protoFile = DeuteriumFormat.DeuteriumFile.newBuilder()
				.setName(data.getName())
				.setDescription(data.getDescription());

		for (Graph inputGraph : data.getGraphs().values()) {
			// Basic graph metadata
			objectCount++;
			log.debug("Packing graph {}: {}", inputGraph.getId(), inputGraph.getName());
			final DeuteriumFormat.Graph.Builder protoGraph = DeuteriumFormat.Graph.newBuilder()
					.setId(inputGraph.getId().toString())
					.setName(inputGraph.getName())
					.setDescription(inputGraph.getDescription());

			// Convert nodes
			for (Node inputNode : inputGraph.getNodes().values()) {
				// Basic node metadata
				objectCount++;
				log.debug("Packing node {}: {}", inputNode.getId(), inputNode.getName());
				final DeuteriumFormat.Node.Builder protoNode = DeuteriumFormat.Node.newBuilder()
						.setId(inputNode.getId().toString())
						.setName(inputNode.getName())
						.setDetails(inputNode.getDetails());

				// Copy over dependencies
				for (UUID id : inputNode.getDependencies().keySet())
					protoNode.addDependencies(id.toString());

				protoGraph.putNodes(inputNode.getId().toString(), protoNode.build());
			}

			// Convert histories
			for (NodeHistory inputHistory : inputGraph.getHistory()) {
				objectCount++;
				log.debug("Packing history {} for node {} on {}", inputHistory.getId(), inputHistory.getEditId(), inputHistory.getDate());
				final DeuteriumFormat.NodeHistory.Builder protoHistory = DeuteriumFormat.NodeHistory.newBuilder()
						.setId(inputHistory.getId().toString())
						.setDate(inputHistory.getDate().getTime())
						.setEditId(inputHistory.getEditId().toString())
						.setAction(DeuteriumFormat.NodeHistory.Action.forNumber(inputHistory.getAction().ordinal()))
						.setChange(inputHistory.getChange());
				protoGraph.addHistory(protoHistory.build());
			}

			protoFile.putGraphs(inputGraph.getId().toString(), protoGraph.build());
		}

		// Build the file, write it out in binary, and compress it
		final ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
		final GZIPOutputStream compressor = new GZIPOutputStream(compressedData);
		protoFile.build().writeDelimitedTo(compressor);
		compressor.close();
		log.info("Saved {} objects in {} bytes in {}ms", objectCount, compressedData.size(),
				System.currentTimeMillis() - startTime);

		// Write to file! Prepend with magic number ("DEUT")
		final FileOutputStream outputFile = new FileOutputStream(filename);
		outputFile.write("DEUT".getBytes(StandardCharsets.UTF_8));
		outputFile.write(compressedData.toByteArray());
		log.info("Wrote {} bytes to file {}", compressedData.size() + 4, filename);
		outputFile.close();
	}
}
