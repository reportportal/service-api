/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.pf4j.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginLoaderTest {

	public static final String PLUGIN_ID = "pluginV1";
	public static final String PLUGIN_VERSION = "1.0.0";
	public static final String FILE_NAME = "file";

	private final String pluginRootPath = "plugins";

	private final Pf4jPluginBox pluginBox = mock(Pf4jPluginBox.class);

	private final PluginDescriptorFinder pluginDescriptorFinder = mock(PluginDescriptorFinder.class);

	private final PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);

	private final PluginWrapper pluginWrapper = mock(PluginWrapper.class);

	private final PluginManager pluginManager = mock(PluginManager.class);

	private final PluginLoader pluginLoader = new PluginLoaderImpl(pluginRootPath, pluginBox, pluginDescriptorFinder
	);

	@Test
	void shouldExtractPluginIdWhenExists() throws PluginException {

		Path path = Paths.get("dir", FILE_NAME);

		when(pluginDescriptorFinder.find(path)).thenReturn(pluginDescriptor);
		when(pluginDescriptor.getPluginId()).thenReturn(PLUGIN_ID);
		when(pluginDescriptor.getVersion()).thenReturn(PLUGIN_VERSION);

		PluginInfo pluginInfo = pluginLoader.extractPluginInfo(path);

		assertNotNull(pluginInfo);
		assertEquals(PLUGIN_ID, pluginInfo.getId());
		assertEquals(PLUGIN_VERSION, pluginInfo.getVersion());
	}

	@Test
	void shouldReloadPlugin() {

		Path path = Paths.get("dir", FILE_NAME);

		when(pluginWrapper.getPluginPath()).thenReturn(path);

		when(pluginBox.loadPlugin(pluginWrapper.getPluginPath())).thenReturn(PLUGIN_ID);

		when(pluginBox.startUpPlugin(PLUGIN_ID)).thenReturn(PluginState.STARTED);

		PluginState pluginState = pluginLoader.loadAndStartUpPlugin(pluginWrapper);

		assertNotNull(pluginState);
		assertEquals(PluginState.STARTED, pluginState);
	}

	@Test
	void shouldReturnTrueWhenPluginExtensionClassesValid() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.of(pluginWrapper));

		when(pluginWrapper.getPluginManager()).thenReturn(pluginManager);

		when(pluginWrapper.getPluginManager().getExtensionClasses(PLUGIN_ID)).thenReturn(Lists.newArrayList(BtsExtension.class));

		boolean isValid = pluginLoader.validatePluginExtensionClasses(PLUGIN_ID);

		assertTrue(isValid);
	}

	@Test
	void shouldReturnFalseWhenPluginExtensionClassesInvalid() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.of(pluginWrapper));

		when(pluginWrapper.getPluginManager()).thenReturn(pluginManager);

		when(pluginWrapper.getPluginManager().getExtensionClasses(PLUGIN_ID)).thenReturn(Lists.newArrayList(Collection.class));

		boolean isValid = pluginLoader.validatePluginExtensionClasses(PLUGIN_ID);

		assertFalse(isValid);
	}

	@Test
	void shouldRetrievePreviousPluginWhenExists() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.of(pluginWrapper));

		when(pluginWrapper.getPluginId()).thenReturn(PLUGIN_ID);

		when(pluginBox.unloadPlugin(PLUGIN_ID)).thenReturn(true);

		Optional<PluginWrapper> pluginWrapper = pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME);

		assertTrue(pluginWrapper.isPresent());
	}

	@Test
	void shouldNotRetrievePreviousPluginWhenNotExists() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.empty());

		Optional<PluginWrapper> pluginWrapper = pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME);

		assertFalse(pluginWrapper.isPresent());
	}

	@Test
	void shouldDeletePluginWhenPathsEqual() {

		when(pluginWrapper.getPluginPath()).thenReturn(Paths.get(pluginRootPath, FILE_NAME));

		pluginLoader.deletePreviousPlugin(pluginWrapper, FILE_NAME);
	}
}