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

package dev.crmyers.deuterium.ui;

import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

	@Inject
	MenuHandler menuHandler;

	/**
	 * Handle events from the menu bar.
	 */
	@FXML
	public void handleMenuEvent(ActionEvent event) {
		switch (((MenuItem) event.getTarget()).getText()) {
			case "New file":
				menuHandler.newFile();
				break;
			case "Open":
				menuHandler.openFile();
				break;
			case "Save":
				menuHandler.saveFile();
				break;
			case "Save as...":
				menuHandler.saveFileAs();
				break;
			case "Exit":
				menuHandler.exit();
				break;
			case "Undo":
				menuHandler.undo();
				break;
			case "Redo":
				menuHandler.redo();
				break;
			case "Copy":
				menuHandler.copy();
				break;
			case "Cut":
				menuHandler.cut();
				break;
			case "Paste":
				menuHandler.paste();
				break;
			case "New graph":
				menuHandler.newGraph();
				break;
			case "Add node":
				menuHandler.addNode();
				break;
			case "Delete node":
				menuHandler.deleteNode();
				break;
			case "Add dependency":
				menuHandler.addDependency();
				break;
			case "Add dependent":
				menuHandler.addDependent();
				break;
			case "Select dependencies":
				menuHandler.selectDependencies();
				break;
			case "Sort dependencies":
				menuHandler.sortDependencies();
				break;
			case "Find loops":
				menuHandler.findLoops();
				break;
			case "Find shortest path":
				menuHandler.findShortestPath();
				break;
			case "Find exclusive dependencies":
				menuHandler.findExclusiveDependencies();
				break;
			case "Graph history":
				menuHandler.graphHistory();
				break;
			case "Node history":
				menuHandler.nodeHistory();
				break;
			case "Revert to...":
				menuHandler.revertTo();
				break;
			case "User guide":
				menuHandler.userGuide();
				break;
			case "About":
				menuHandler.about();
				break;
		}
	}
}
