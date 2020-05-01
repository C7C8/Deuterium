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

package dev.crmyers.deuterium.command;

import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Edit node base command. */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class EditNodeCommand extends Command {
	protected UUID node;
	protected Date date;

	/**
	* Generate a command with the opposite effect of this one. E.g. add node -> delete node
	*
	* @return Reverse command.
	*/
	abstract EditNodeCommand generateReverse();
}
