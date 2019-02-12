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
import com.epam.ta.reportportal.core.integration.plugin.PluginUploadingCache;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import org.junit.Assert;
import org.junit.Test;
import org.pf4j.*;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

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

	private final PluginBox pluginBox = mock(PluginBox.class);

	private final PluginDescriptorFinder pluginDescriptorFinder = mock(PluginDescriptorFinder.class);

	private final PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);

	private final PluginWrapper pluginWrapper = mock(PluginWrapper.class);

	private final PluginManager pluginManager = mock(PluginManager.class);

	private final PluginUploadingCache pluginUploadingCache = mock(PluginUploadingCache.class);

	private final PluginLoader pluginLoader = new PluginLoaderImpl(pluginRootPath, pluginBox, pluginDescriptorFinder, pluginUploadingCache);

	@Test
	public void shouldExtractPluginIdWhenExists() throws PluginException {

		Path path = Paths.get("dir", FILE_NAME);

		when(pluginDescriptorFinder.find(path)).thenReturn(pluginDescriptor);
		when(pluginDescriptor.getPluginId()).thenReturn(PLUGIN_ID);
		when(pluginDescriptor.getVersion()).thenReturn(PLUGIN_VERSION);

		PluginInfo pluginInfo = pluginLoader.extractPluginInfo(path);

		Assert.assertNotNull(pluginInfo);
		Assert.assertEquals(PLUGIN_ID, pluginInfo.getId());
		Assert.assertEquals(PLUGIN_VERSION, pluginInfo.getVersion());
	}

	@Test
	public void shouldReloadPlugin() {

		Path path = Paths.get("dir", FILE_NAME);

		when(pluginWrapper.getPluginPath()).thenReturn(path);

		when(pluginBox.loadPlugin(pluginWrapper.getPluginPath())).thenReturn(PLUGIN_ID);

		when(pluginBox.startUpPlugin(PLUGIN_ID)).thenReturn(PluginState.STARTED);

		PluginState pluginState = pluginLoader.loadAndStartUpPlugin(pluginWrapper);

		Assert.assertNotNull(pluginState);
		Assert.assertEquals(PluginState.STARTED, pluginState);
	}

	@Test
	public void shouldReturnTrueWhenPluginExtensionClassesValid() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.of(pluginWrapper));

		when(pluginWrapper.getPluginManager()).thenReturn(pluginManager);

		when(pluginWrapper.getPluginManager().getExtensionClasses(PLUGIN_ID)).thenReturn(Lists.newArrayList(BtsExtension.class));

		boolean isValid = pluginLoader.validatePluginExtensionClasses(PLUGIN_ID);

		Assert.assertTrue(isValid);
	}

	@Test
	public void shouldReturnFalseWhenPluginExtensionClassesInvalid() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.of(pluginWrapper));

		when(pluginWrapper.getPluginManager()).thenReturn(pluginManager);

		when(pluginWrapper.getPluginManager().getExtensionClasses(PLUGIN_ID)).thenReturn(Lists.newArrayList(Collection.class));

		boolean isValid = pluginLoader.validatePluginExtensionClasses(PLUGIN_ID);

		Assert.assertFalse(isValid);
	}

	@Test
	public void shouldRetrievePreviousPluginWhenExists() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.of(pluginWrapper));

		when(pluginWrapper.getPluginId()).thenReturn(PLUGIN_ID);

		when(pluginBox.unloadPlugin(PLUGIN_ID)).thenReturn(true);

		Optional<PluginWrapper> pluginWrapper = pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME);

		Assert.assertTrue(pluginWrapper.isPresent());
	}

	@Test
	public void shouldNotRetrievePreviousPluginWhenNotExists() {

		when(pluginBox.getPluginById(PLUGIN_ID)).thenReturn(Optional.empty());

		Optional<PluginWrapper> pluginWrapper = pluginLoader.retrievePreviousPlugin(PLUGIN_ID, FILE_NAME);

		Assert.assertFalse(pluginWrapper.isPresent());
	}

	@Test
	public void shouldDeletePluginWhenPathsEqual() {

		when(pluginWrapper.getPluginPath()).thenReturn(Paths.get(pluginRootPath, FILE_NAME));

		pluginLoader.deletePreviousPlugin(pluginWrapper, FILE_NAME);
	}
}