package com.epam.ta.reportportal.store.filesystem;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.BinaryData;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LocalDataStore implements DataStore {

	private static final Logger logger = LoggerFactory.getLogger(LocalDataStore.class);

	private final LocalFilePathGenerator fileNameGenerator;
	private final String storageRootPath;

	public LocalDataStore(LocalFilePathGenerator fileNameGenerator,
			@Value("${datastore.default.path:/data/store}") String storageRootPath) {
		this.fileNameGenerator = fileNameGenerator;
		this.storageRootPath = storageRootPath;
	}

	@Override
	public String save(String fileName, BinaryData binaryData) {

		String result;

		String generatedFilePath = fileNameGenerator.generate();
		try {

			Path targetDirectory = Paths.get(storageRootPath, generatedFilePath);
			Path targetPath = Paths.get(storageRootPath, generatedFilePath, fileName);
			result = targetPath.toString();

			if (!Files.isDirectory(targetDirectory)) {
				Files.createDirectories(targetDirectory);
			}

			logger.debug("Saving to: {} ", targetPath.toAbsolutePath());

			Files.copy(binaryData.getInputStream(), targetPath);
		} catch (IOException e) {

			logger.error("Error ", e);

			throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save log file");
		}

		return result;
	}
}
