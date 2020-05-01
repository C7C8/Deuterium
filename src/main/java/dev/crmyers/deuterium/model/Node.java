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

import java.util.UUID;
import lombok.*;

/** Class to represent a single Deuterium node. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Node {
	private UUID id;
	private String name;
	private String details;

	/**
	* Construct a node with name only; details is left at nothing, ID is randomized
	*
	* @param name Node name
	*/
	public Node(String name) {
		this.name = name;
		id = UUID.randomUUID();
	}

	/**
	* Construct node from ID, name is left blank (literally).
	*
	* @param uuid UUID.
	*/
	public Node(@NonNull UUID uuid) {
		this.name = "Blank";
		this.id = uuid;
	}

	@Override
	public String toString() {
		//		return "Node(id=\"" + id + "\", name=\"" + name + "\", details=\"" + details + ")";
		return name;
	}
}
