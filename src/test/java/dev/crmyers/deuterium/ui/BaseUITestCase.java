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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.net.URL;

public abstract class BaseUITestCase extends ApplicationTest {

	protected Injector injector;
	protected URL fxml;

	@Override
	public void start(Stage stage) throws IOException {
		// Set up Guice dependency injection.
		injector = Guice.createInjector(new DeuteriumTestModule());
		FXMLLoader fxmlLoader = new FXMLLoader(fxml);
		fxmlLoader.setControllerFactory(injector::getInstance);

		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
}
