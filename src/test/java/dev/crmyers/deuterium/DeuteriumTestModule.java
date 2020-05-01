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

import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.crmyers.deuterium.ui.MenuHandler;

public class DeuteriumTestModule extends AbstractModule {
	public void configure() {}

	@Provides
	@Singleton
	public MenuHandler provideMenuHandler() {
		return mock(MenuHandler.class);
	}
}
