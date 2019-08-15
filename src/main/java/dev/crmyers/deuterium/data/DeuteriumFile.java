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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Class to represent a Deuterium save file (.d2o)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeuteriumFile {
	String name;
	String description;
	Map<UUID, Graph> graphs;
}
