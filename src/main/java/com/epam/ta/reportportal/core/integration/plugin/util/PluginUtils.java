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

package com.epam.ta.reportportal.core.integration.plugin.util;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class PluginUtils {

	private PluginUtils() {

		//static only
	}

	public static void createTempPluginsFolderIfNotExists(String path) {
		if (!Files.isDirectory(Paths.get(path))) {
			try {
				Files.createDirectories(Paths.get(path));
			} catch (IOException e) {

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to create directory = {}", path).get()
				);
			}
		}
	}

	/**
	 * Resolve file type and upload it to the temporary plugins directory.
	 * If successful returns file extension
	 *
	 * @param pluginFile Plugin file to upload
	 * @return {@link PluginFileExtension#extension}
	 */
	public static String resolveExtensionAndUploadTempPlugin(MultipartFile pluginFile, String pluginsTempPath) {

		String resolvedExtension = FilenameUtils.getExtension(pluginFile.getOriginalFilename());

		PluginFileExtension pluginFileExtension = PluginFileExtension.findByExtension("." + resolvedExtension)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unsupported plugin file extension = {}", resolvedExtension).get()
				));

		Path pluginPath = Paths.get(pluginsTempPath, pluginFile.getOriginalFilename());

		try {

			Files.copy(pluginFile.getInputStream(), pluginPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy the new plugin file with name = {} to the temp directory",
							pluginFile.getOriginalFilename()
					).get()
			);
		}

		return pluginFileExtension.getExtension();

	}

	public static void deleteTempPlugin(Path tempPluginPath) {
		try {

			Files.deleteIfExists(tempPluginPath);

		} catch (IOException e) {
			//error during temp plugin is not crucial, temp files cleaning will be delegated to //TODO impl Quartz job to clean temp files
		}
	}
}
