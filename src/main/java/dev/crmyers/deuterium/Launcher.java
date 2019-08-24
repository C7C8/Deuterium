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

package dev.crmyers.deuterium;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class Launcher extends Application {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		logger.info("Application start");
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// Set up Guice dependency injection.
		Injector injector = Guice.createInjector(new DeuteriumModule());
		FXMLLoader fxmlLoader = new FXMLLoader(Paths.fxml_main);
		fxmlLoader.setControllerFactory(injector::getInstance);

		Scene scene = new Scene(fxmlLoader.load());
		primaryStage.setScene(scene);
	}
}
