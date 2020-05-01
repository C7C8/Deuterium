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

package dev.crmyers.deuterium.model.protobuf;

import com.google.common.graph.EndpointPair;
import com.google.common.io.BaseEncoding;
import dev.crmyers.deuterium.command.*;
import dev.crmyers.deuterium.model.DeuteriumFile;
import dev.crmyers.deuterium.model.DeuteriumGraph;
import dev.crmyers.deuterium.model.FileSaver;
import dev.crmyers.deuterium.model.Node;
import dev.crmyers.deuterium.model.exception.FileFormatException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.extern.log4j.Log4j2;

/** Implementation of file saving using Protobuf. */
@SuppressWarnings("UnstableApiUsage")
@Log4j2
public class ProtobufFileSaver implements FileSaver {

	/**
	* Load a Deuterium save file and return it in the form of data.
	*
	* @param filename Relative filename.
	* @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	* @throws FileFormatException When file format to load is invalid
	* @throws FileNotFoundException Thrown if the file cannot be loaded
	* @throws IOException Generic IO exception (e.g. invalid file)
	*/
	@Override
	public DeuteriumFile loadFile(String filename) throws FileNotFoundException, IOException {
		return loadFile(new FileInputStream(filename));
	}

	/**
	* Load a Deuterium save file and return it in the form of data.
	*
	* @param file InputStream to read from
	* @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	* @throws FileFormatException When file format to load is invalid
	* @throws FileNotFoundException Thrown if the file cannot be loaded
	* @throws IOException Generic IO exception (e.g. invalid file)
	*/
	@Override
	public DeuteriumFile loadFile(InputStream file)
			throws FileFormatException, FileNotFoundException, IOException {
		final long startTime = System.currentTimeMillis();
		log.info("Loading objects from file {}", file);

		// Sanity check: Does the file start with the right magic number?
		final byte[] magic = file.readNBytes(4);
		if (!(new String(magic)).equals("DEUT")) {
			// Convert magic string into hex printout for easier debugging
			log.error(
					"File {} not a Deuterium file, starts with 0x{} (expected 0x{})",
					file,
					BaseEncoding.base16().lowerCase().encode(magic),
					BaseEncoding.base16().lowerCase().encode("DEUT".getBytes()));
			throw new FileFormatException("Invalid file format");
		}

		// Unzip the input file (starting from the previous offset) and process basic file metadata
		DeuteriumFormat.DeuteriumFile protoFile =
				DeuteriumFormat.DeuteriumFile.parseDelimitedFrom(new GZIPInputStream(file));
		DeuteriumFile outputFile = new DeuteriumFile();
		outputFile.setName(protoFile.getName());
		outputFile.setDescription(protoFile.getDescription());

		int objectCount = 1;
		final HashMap<UUID, DeuteriumGraph> graphs = new HashMap<>();
		for (DeuteriumFormat.Graph protoGraph : protoFile.getGraphsMap().values()) {
			// Basic graph metadata
			objectCount++;
			final DeuteriumGraph graph = new DeuteriumGraph();
			graph.setId(UUID.fromString(protoGraph.getId()));
			graph.setName(protoGraph.getName());
			graph.setDescription(protoGraph.getDescription());
			graphs.put(graph.getId(), graph);
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
			graph.setNodes(nodeMap);

			// Process nodes -- round 2, link nodes to each other
			for (DeuteriumFormat.Edge protoEdge : protoGraph.getEdgesList()) {
				log.debug("Unpacking edge {} -> {}", protoEdge.getFrom(), protoEdge.getTo());
				graph.putEdge(
						nodeMap.get(UUID.fromString(protoEdge.getFrom())),
						nodeMap.get(UUID.fromString(protoEdge.getTo())));
			}

			// Add node histories to the graph
			// TODO Replace with command-based history
			ArrayList<EditNodeCommand> nodeHistories = new ArrayList<>();
			for (DeuteriumFormat.NodeHistory protoHistory : protoGraph.getHistoryList()) {
				objectCount++;
				EditNodeCommand command;
				switch (protoHistory.getAction()) {
					case ADD:
						command = new AddNodeCommand();
						break;
					case DELETE:
						command = new DeleteNodeCommand();
						break;
					case EDIT_NAME:
						command = new EditNodeNameCommand();
						((EditNodeNameCommand) command).setChange(protoHistory.getChange());
						break;
					case EDIT_DETAILS:
						command = new EditNodeDetailsCommand();
						((EditNodeDetailsCommand) command).setChange(protoHistory.getChange());
						break;
					case ADD_NEIGHBOR:
						command = new AddDependencyCommand();
						((AddDependencyCommand) command)
								.setDependency(UUID.fromString(protoHistory.getChange()));
						break;
					case DEL_NEIGHBOR:
						command = new DeleteDependencyCommand();
						((DeleteDependencyCommand) command)
								.setDependency(UUID.fromString(protoHistory.getChange()));
						break;
					default:
						log.error(
								"Read unidentifiable node history type for history {}",
								protoHistory);
						throw new FileFormatException("Read unidentifiable node history type");
				}
				command.setNode(UUID.fromString(protoHistory.getEditId()));
				command.setDate(new Date(protoHistory.getDate()));

				log.debug(
						"Unpacking history for node {} on {}",
						command.getNode(),
						command.getDate());
				nodeHistories.add(command);
			}
			graph.setHistory(nodeHistories);
		}
		outputFile.setGraphs(graphs);
		log.info(
				"Loaded {} objects from file in {} ms",
				objectCount,
				System.currentTimeMillis() - startTime);

		return outputFile;
	}

	/**
	* Save a Deuterium data object to a new file.
	*
	* @param filename Relative filename.
	* @param data Data to save.
	* @throws FileNotFoundException Thrown if the file cannot be saved because the containing
	*     folder does not exist
	* @throws IOException Generic IO exception (e.g. out of space)
	*/
	@Override
	public void saveFile(String filename, DeuteriumFile data)
			throws FileNotFoundException, IOException {
		saveFile(new FileOutputStream(filename), data);
	}

	/**
	* Save a Deuterium data object to a new file.
	*
	* @param file OutputStream to write to
	* @param data Data to save.
	* @throws FileNotFoundException Thrown if the file cannot be saved because the containing
	*     folder does not exist
	* @throws IOException Generic IO exception (e.g. out of space)
	*/
	@Override
	public void saveFile(OutputStream file, DeuteriumFile data)
			throws FileNotFoundException, IOException {
		// Map a DeuteriumFile object to a Protobuffer-style DeuteriumFile
		int objectCount = 1;
		final long startTime = System.currentTimeMillis();
		log.info("Beginning save of {} graphs", data.getGraphs().size());
		final DeuteriumFormat.DeuteriumFile.Builder protoFile =
				DeuteriumFormat.DeuteriumFile.newBuilder()
						.setName(data.getName())
						.setDescription(data.getDescription());

		for (DeuteriumGraph inputGraph : data.getGraphs().values()) {
			// Basic graph metadata
			objectCount++;
			log.debug("Packing graph {}: {}", inputGraph.getId(), inputGraph.getName());
			final DeuteriumFormat.Graph.Builder protoGraph =
					DeuteriumFormat.Graph.newBuilder()
							.setId(inputGraph.getId().toString())
							.setName(inputGraph.getName())
							.setDescription(inputGraph.getDescription());

			// Convert nodes
			for (Node inputNode : inputGraph.getNodes().values()) {
				// Basic node metadata
				objectCount++;
				log.debug("Packing node {}: {}", inputNode.getId(), inputNode.getName());
				final DeuteriumFormat.Node.Builder protoNode =
						DeuteriumFormat.Node.newBuilder()
								.setId(inputNode.getId().toString())
								.setName(inputNode.getName())
								.setDetails(inputNode.getDetails());
				protoGraph.putNodes(inputNode.getId().toString(), protoNode.build());
			}

			// Convert histories
			// TODO Replace with command-based history
			//			for (NodeHistory inputHistory : inputGraph.getHistory()) {
			//				objectCount++;
			//				log.debug("Packing history {} for node {} on {}", inputHistory.getId(),
			// inputHistory.getEditId(), inputHistory.getDate());
			//				final DeuteriumFormat.NodeHistory.Builder protoHistory =
			// DeuteriumFormat.NodeHistory.newBuilder()
			//						.setId(inputHistory.getId().toString())
			//						.setDate(inputHistory.getDate().getTime())
			//						.setEditId(inputHistory.getEditId().toString())
			//
			//	.setAction(DeuteriumFormat.NodeHistory.Action.forNumber(inputHistory.getAction().ordinal()))
			//						.setChange(inputHistory.getChange());
			//				protoGraph.addHistory(protoHistory.build());
			//			}

			// Convert edges
			for (EndpointPair<Node> inputEdge : inputGraph.edges()) {
				log.debug(
						"Packing edge {} -> {}",
						inputEdge.nodeU().getId(),
						inputEdge.nodeV().getId());
				protoGraph.addEdges(
						DeuteriumFormat.Edge.newBuilder()
								.setFrom(inputEdge.nodeU().getId().toString())
								.setTo(inputEdge.nodeV().getId().toString())
								.build());
			}

			protoFile.putGraphs(inputGraph.getId().toString(), protoGraph.build());
		}

		// Build the file, write it out in binary, and compress it
		final ByteArrayOutputStream compressedData = new ByteArrayOutputStream();
		final GZIPOutputStream compressor = new GZIPOutputStream(compressedData);
		protoFile.build().writeDelimitedTo(compressor);
		compressor.close();
		log.info(
				"Saved {} objects in {} bytes in {}ms",
				objectCount,
				compressedData.size(),
				System.currentTimeMillis() - startTime);

		// Write to file! Prepend with magic number ("DEUT")
		file.write("DEUT".getBytes(StandardCharsets.UTF_8));
		file.write(compressedData.toByteArray());
		log.info("Wrote {} bytes to file", compressedData.size() + 4);
		file.close();
	}
}
