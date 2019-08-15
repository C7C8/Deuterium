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

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Interface that allows for saving of Deuterium data to file.
 * @apiNote This should be stateless!
 */
public interface FileSaver {

	/**
	 * Load a Deuterium save file and return it in the form of data.
	 * @param filename Relative filename.
	 * @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	 * @throws FileNotFoundException Thrown if the file cannot be loaded
	 * @throws IOException Generic IO exception (e.g. invalid file)
	 */
	DeuteriumFile loadFile(String filename) throws FileNotFoundException, IOException;

	/**
	 * Save a Deuterium data object to a new file.
	 * @param filename Relative filename.
	 * @param data Data to save.
	 * @throws FileNotFoundException Thrown if the file cannot be saved because the containing folder does not exist
	 * @throws IOException Generic IO exception (e.g. out of space)
	 */
	void saveNewFile(String filename, DeuteriumFile data) throws FileNotFoundException, IOException;

	/**
	 * Update an existing Deuterium file
	 * @param filename Relative filename.
	 * @param data Data to save.
	 * @throws FileNotFoundException Thrown if the file cannot be saved because the containing folder does not exist
	 * @throws IOException Generic IO exception (e.g. out of space).
	 */
	void updateFile(String filename, DeuteriumFile data) throws FileNotFoundException, IOException;
}
