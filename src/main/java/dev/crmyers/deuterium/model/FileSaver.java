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

package dev.crmyers.deuterium.model;

import dev.crmyers.deuterium.model.exception.FileFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
* Interface that allows for saving of Deuterium data to file.
*
* @apiNote This should be stateless!
*/
public interface FileSaver {

	/**
	* Load a Deuterium save file and return it in the form of data.
	*
	* @param filename Relative filename.
	* @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	* @throws FileFormatException When file format to load is invalid
	* @throws FileNotFoundException Thrown if the file cannot be loaded
	* @throws IOException Generic IO exception (e.g. invalid file)
	*/
	DeuteriumFile loadFile(String filename) throws FileNotFoundException, IOException;

	/**
	* Load a Deuterium save file and return it in the form of data.
	*
	* @param file Input stream to read from
	* @return Loaded data, already linked up and ready to go (i.e. graphs should be traversable)
	* @throws FileFormatException When file format to load is invalid
	* @throws FileNotFoundException Thrown if the file cannot be loaded
	* @throws IOException Generic IO exception (e.g. invalid file)
	*/
	DeuteriumFile loadFile(InputStream file) throws FileNotFoundException, IOException;

	/**
	* Save a Deuterium data object to a new file.
	*
	* @param filename Relative filename.
	* @param data Data to save.
	* @throws FileNotFoundException Thrown if the file cannot be saved because the containing
	*     folder does not exist
	* @throws IOException Generic IO exception (e.g. out of space)
	*/
	void saveFile(String filename, DeuteriumFile data) throws FileNotFoundException, IOException;

	/**
	* Save a Deuterium data object to a new file.
	*
	* @param file Output stream to write to.
	* @param data Data to save.
	* @throws FileNotFoundException Thrown if the file cannot be saved because the containing
	*     folder does not exist
	* @throws IOException Generic IO exception (e.g. out of space)
	*/
	void saveFile(OutputStream file, DeuteriumFile data) throws FileNotFoundException, IOException;
}
