package com.epam.ta.reportportal.core.integration.plugin.binary;

import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FileUtils;

import javax.activation.FileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginFilesProvider {

	private final String baseDirectory;
	private final String folderQualifier;

	private final FileTypeMap fileTypeResolver;

	private final IntegrationTypeRepository integrationTypeRepository;

	public PluginFilesProvider(String baseDirectory, String folderQualifier, FileTypeMap fileTypeResolver,
			IntegrationTypeRepository integrationTypeRepository) {
		this.baseDirectory = baseDirectory;
		this.folderQualifier = folderQualifier;
		this.fileTypeResolver = fileTypeResolver;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	public BinaryData load(String pluginName, String fileName) {
		final IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

		final File file = Paths.get(baseDirectory, integrationType.getName(), folderQualifier, fileName).toFile();

		if (!file.exists() || file.isDirectory()) {
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, fileName);
		}

		return getBinaryData(file);

	}

	private BinaryData getBinaryData(File file) {
		try {
			final InputStream fileStream = FileUtils.openInputStream(file);
			final String contentType = fileTypeResolver.getContentType(file.getName());
			return new BinaryData(contentType, (long) fileStream.available(), fileStream);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
		}
	}
}
