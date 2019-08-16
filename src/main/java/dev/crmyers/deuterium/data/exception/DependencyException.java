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

package dev.crmyers.deuterium.data.exception;

import dev.crmyers.deuterium.data.Node;

import java.util.Set;

/**
 * Thrown in case of a bad dependency calculation request
 */
public class DependencyException extends GraphException {
	public DependencyException(String message) {
		super(message);
	}

	public DependencyException(String message, Set<Node> nodes) {
		super(message, nodes);
	}
}
