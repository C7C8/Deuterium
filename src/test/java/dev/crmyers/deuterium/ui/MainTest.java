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

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.crmyers.deuterium.DeuteriumTestModule;
import dev.crmyers.deuterium.Paths;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;

@Tag("ui")
@ExtendWith(ApplicationExtension.class)
public class MainTest {

	@Start
	public void start(Stage stage) throws IOException {
		// Set up Guice dependency injection.
		Injector injector = Guice.createInjector(new DeuteriumTestModule());
		FXMLLoader fxmlLoader = new FXMLLoader(Paths.fxml_main);
		fxmlLoader.setControllerFactory(injector::getInstance);

		stage.setScene(new Scene(fxmlLoader.load()));
		stage.show();
	}
}
