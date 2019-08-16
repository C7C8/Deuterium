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

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Class to represent a Deuterium graph.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeuteriumGraph {
	UUID id;
	String name;
	String description;
	Map<UUID, Node> nodes = new HashMap<>();
	List<NodeHistory> history = new ArrayList<>();

	@SuppressWarnings("UnstableApiUsage")
	MutableGraph<Node> graph = GraphBuilder.directed().build();
}
