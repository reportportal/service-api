/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.filesystem;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dzianis_Shybeka
 */
@Component
public class LocalDataStore implements DataStore {

	private static final Logger logger = LoggerFactory.getLogger(LocalDataStore.class);

	private final String storageRootPath;

	public LocalDataStore(@Value("${datastore.default.path:/data/store}") String storageRootPath) {
		this.storageRootPath = storageRootPath;
	}

	@Override
	public String save(String filePath, InputStream inputStream) {

		try {

			Path targetPath = Paths.get(storageRootPath, filePath);
			Path targetDirectory = targetPath.getParent();

			if (!Files.isDirectory(targetDirectory)) {
				Files.createDirectories(targetDirectory);
			}

			logger.debug("Saving to: {} ", targetPath.toAbsolutePath());

			Files.copy(inputStream, targetPath);

			return targetPath.toString();
		} catch (IOException e) {

			logger.error("Unable to save log file ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save log file");
		}
	}

	@Override
	public InputStream load(String filePath) {

		try {

			return Files.newInputStream(Paths.get(filePath));
		} catch (IOException e) {

			logger.error("Unable to find file ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to find file");
		}
	}

	@Override
	public void delete(String filePath) {

		try {

			Files.delete(Paths.get(filePath));
		} catch (IOException e) {

			logger.error("Unable to delete file ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to delete file");
		}
	}
}
