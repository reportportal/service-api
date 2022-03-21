package com.epam.ta.reportportal.core.integration.plugin.binary;

import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginFilesProvider {

	private final String baseDirectory;
	private final String folderQualifier;
	private final IntegrationTypeRepository integrationTypeRepository;

	public PluginFilesProvider(String baseDirectory, String folderQualifier, IntegrationTypeRepository integrationTypeRepository) {
		this.baseDirectory = baseDirectory;
		this.folderQualifier = folderQualifier;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	public InputStream load(String pluginName, String fileName) {
		final IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

		final Path filePath = Paths.get(baseDirectory, integrationType.getName(), folderQualifier, fileName);

		if (Files.notExists(filePath)) {
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, fileName);
		}

		return getFileStream(filePath);

	}

	private InputStream getFileStream(Path filePath) {
		try {
			return Files.newInputStream(filePath);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
		}
	}
}
