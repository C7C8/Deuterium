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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of file saving using Protobuf.
 */
public class ProtobufFileSaver implements FileSaver {

	/**
	 * Load a Deuterium save file and return it in the form of data.
	 *
	 * @param filename Relative filename.
	 * @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	 * @throws FileNotFoundException Thrown if the file cannot be loaded
	 * @throws IOException           Generic IO exception (e.g. invalid file)
	 */
	@Override
	public DeuteriumFile loadFile(String filename) throws FileNotFoundException, IOException {
		return null;
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
	public void saveNewFile(String filename, DeuteriumFile data) throws FileNotFoundException, IOException {
		// Step 1: Map a DeuteriumFile object to a Protobuffer-style DeuteriumFile
		final DeuteriumFormat.DeuteriumFile.Builder protoFile = DeuteriumFormat.DeuteriumFile.newBuilder()
				.setName(data.getName())
				.setDescription(data.getDescription());

		for (Graph inputGraph : data.getGraphs().values()) {
			// Basic graph metadata
			final DeuteriumFormat.Graph.Builder protoGraph = DeuteriumFormat.Graph.newBuilder()
					.setId(inputGraph.getId().toString())
					.setName(inputGraph.getName())
					.setDescription(inputGraph.getDescription());

			// Convert nodes
			for (Node inputNode : inputGraph.getNodes().values()) {
				// Basic node metadata
				final DeuteriumFormat.Node.Builder protoNode = DeuteriumFormat.Node.newBuilder()
						.setId(inputNode.getId().toString())
						.setName(inputNode.getName())
						.setDetails(inputNode.getDetails());

				// Copy over neighbors
				for (UUID id : inputNode.getNeighbors().keySet())
					protoNode.addNeighbors(id.toString());

				protoGraph.putNodes(inputNode.getId().toString(), protoNode.build());
			}

			// Convert histories
			for (NodeHistory inputHistory : inputGraph.getHistory()) {
				DeuteriumFormat.NodeHistory.Builder protoHistory = DeuteriumFormat.NodeHistory.newBuilder()
						.setId(inputHistory.getId().toString())
						.setDate(inputHistory.getDate().getTime())
						.setEditId(inputHistory.getEditId().toString())
						.setAction(DeuteriumFormat.NodeHistory.Action.valueOf(inputHistory.getAction().ordinal()))
						.setChange(inputHistory.getChange());
				protoGraph.addHistory(protoHistory.build());
			}

			protoFile.putGraphs(inputGraph.getId().toString(), protoGraph.build());
		}

		// Build the file, write it out in binary, and compress it
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		GZIPOutputStream compressor = new GZIPOutputStream(outputStream);
		protoFile.build().writeTo(compressor);
		compressor.close();

		// Write to file! Prepend with magic number ("DEUT")
		FileOutputStream outputFile = new FileOutputStream(filename);
		outputFile.write("DEUT".getBytes(StandardCharsets.UTF_8));
		outputFile.write(outputStream.toByteArray());
		outputFile.close();
	}

	/**
	 * Update an existing Deuterium file
	 *
	 * @param filename Relative filename.
	 * @param data     Data to save.
	 * @throws FileNotFoundException Thrown if the file cannot be saved because the containing folder does not exist
	 * @throws IOException           Generic IO exception (e.g. out of space).
	 */
	@Override
	public void updateFile(String filename, DeuteriumFile data) throws FileNotFoundException, IOException {

	}
}
