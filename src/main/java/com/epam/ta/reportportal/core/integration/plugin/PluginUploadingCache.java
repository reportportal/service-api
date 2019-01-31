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

package com.epam.ta.reportportal.core.integration.plugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class PluginUploadingCache {

	private static final long MAXIMUM_UPLOADED_PLUGINS = 50;
	private static final long PLUGIN_LIVE_TIME = 2;

	private Cache<String, Path> uploadingPlugins;

	public PluginUploadingCache() {

		uploadingPlugins = CacheBuilder.newBuilder()
				.maximumSize(MAXIMUM_UPLOADED_PLUGINS)
				.expireAfterWrite(PLUGIN_LIVE_TIME, TimeUnit.MINUTES)
				.build();
	}

	public Cache<String, Path> getUploadingPlugins() {
		return uploadingPlugins;
	}

	public void startPluginUploading(String fileName, Path path) {

		uploadingPlugins.put(fileName, path);
	}

	public void finishPluginUploading(String fileName) {

		uploadingPlugins.invalidate(fileName);
	}

	public boolean isPluginStillBeingUploaded(String fileName) {

		return uploadingPlugins.asMap().containsKey(fileName);
	}
}
