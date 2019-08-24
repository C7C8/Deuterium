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

import dev.crmyers.deuterium.Paths;
import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.IOException;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@Tag("ui")
@ExtendWith(ApplicationExtension.class)
public class MainTest extends BaseUITestCase {

	private MenuHandler menuHandler;

	@Override
	public void start(Stage stage) throws IOException {
		fxml = Paths.fxml_main;
		super.start(stage);
		menuHandler = injector.getInstance(MenuHandler.class);
		reset(menuHandler);
	}

	@Test
	void menuActionWiring(FxRobot robot) {
		robot.clickOn("#menuFile");
		robot.clickOn("#menuFileNew");
		verify(menuHandler).newFile();

		robot.clickOn("#menuFile");
		robot.clickOn("#menuFileOpen");
		verify(menuHandler).openFile();

		robot.clickOn("#menuFile");
		robot.clickOn("#menuFileSave");
		verify(menuHandler).saveFile();

		robot.clickOn("#menuFile");
		robot.clickOn("#menuFileSaveAs");
		verify(menuHandler).saveFileAs();

		robot.clickOn("#menuFile");
		robot.clickOn("#menuFileExit");
		verify(menuHandler).exit();

		robot.clickOn("#menuEdit");
		robot.clickOn("#menuEditUndo");
		verify(menuHandler).undo();

		robot.clickOn("#menuEdit");
		robot.clickOn("#menuEditRedo");
		verify(menuHandler).redo();

		robot.clickOn("#menuEdit");
		robot.clickOn("#menuEditCopy");
		verify(menuHandler).copy();

		robot.clickOn("#menuEdit");
		robot.clickOn("#menuEditCut");
		verify(menuHandler).cut();

		robot.clickOn("#menuEdit");
		robot.clickOn("#menuEditPaste");
		verify(menuHandler).paste();

		robot.clickOn("#menuEdit");
		robot.clickOn("#menuEditNewGraph");
		verify(menuHandler).newGraph();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeAddNode");
		verify(menuHandler).addNode();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeDeleteNode");
		verify(menuHandler).deleteNode();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeAddDependency");
		verify(menuHandler).addDependency();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeAddDependent");
		verify(menuHandler).addDependent();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeHighlightDependencies");
		verify(menuHandler).selectDependencies();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeSortDependencies");
		verify(menuHandler).sortDependencies();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeFindLoops");
		verify(menuHandler).findLoops();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeFindShortestPath");
		verify(menuHandler).findShortestPath();

		robot.clickOn("#menuNode");
		robot.clickOn("#menuNodeFindExclusive");
		verify(menuHandler).findExclusiveDependencies();

		robot.clickOn("#menuHistory");
		robot.clickOn("#menuHistoryGraphHistory");
		verify(menuHandler).graphHistory();

		robot.clickOn("#menuHistory");
		robot.clickOn("#menuHistoryNodeHistory");
		verify(menuHandler).nodeHistory();

		robot.clickOn("#menuHistory");
		robot.clickOn("#menuHistoryRevertTo");
		verify(menuHandler).revertTo();

		robot.clickOn("#menuHelp");
		robot.clickOn("#menuHelpUserGuide");
		verify(menuHandler).userGuide();

		robot.clickOn("#menuHelp");
		robot.clickOn("#menuHelpAbout");
		verify(menuHandler).about();
	}
}
