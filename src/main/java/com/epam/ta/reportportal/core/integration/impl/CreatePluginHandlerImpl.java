package com.epam.ta.reportportal.core.integration.impl;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatePluginHandlerImpl implements CreatePluginHandler {

	public static final String PLUGIN_TEMP_DIRECTORY = "/temp/";

	@Value("${rp.plugins.path}")
	private String pluginsRootPath;

	private final PluginBox pluginBox;

	private final PluginDescriptorFinder pluginDescriptorFinder;

	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public CreatePluginHandlerImpl(PluginBox pluginBox, PluginDescriptorFinder pluginDescriptorFinder,
			IntegrationTypeRepository integrationTypeRepository) {
		this.pluginBox = pluginBox;
		this.pluginDescriptorFinder = pluginDescriptorFinder;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	public EntryCreatedRS uploadPlugin(MultipartFile pluginFile) {

		String newPluginFileName = pluginFile.getOriginalFilename();
		BusinessRule.expect(newPluginFileName, StringUtils::isNotBlank)
				.verify(ErrorType.BAD_REQUEST_ERROR, "File name should be not empty.");

		final String pluginsTempPath = pluginsRootPath + PLUGIN_TEMP_DIRECTORY;

		createTempPluginsFolderIfNotExists(pluginsTempPath);
		resolveExtensionAndUploadTempPlugin(pluginFile, pluginsTempPath);

		Path newPluginTempPath = Paths.get(pluginsTempPath, newPluginFileName);
		String pluginId = extractPluginId(newPluginTempPath);

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(pluginId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Unknown integration type - {} ", pluginId).get()
				));

		Optional<PluginWrapper> oldPlugin = retrieveOldPlugin(pluginId, newPluginFileName);

		String newPluginId = pluginBox.loadPlugin(newPluginTempPath);

		pluginBox.startUpPlugin(newPluginId);

		if (ofNullable(newPluginId).isPresent()) {

			if (!validateNewPluginExtensionClasses(newPluginId)) {

				pluginBox.unloadPlugin(newPluginId);
				oldPlugin.ifPresent(this::reloadOldPlugin);
				deleteTempPlugin(newPluginTempPath);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("New plugin with id = {} doesn't have mandatory extension classes.")
				);

			}

			pluginBox.unloadPlugin(newPluginId);

			try {

				org.apache.commons.io.FileUtils.copyFile(new File(pluginsTempPath, newPluginFileName),
						new File(pluginsRootPath, newPluginFileName)
				);

			} catch (IOException e) {

				oldPlugin.ifPresent(this::reloadOldPlugin);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to copy the new plugin file with id = {} to the root directory", newPluginId)
								.get()
				);
			}

			oldPlugin.ifPresent(p -> deleteOldPlugin(p, newPluginFileName));

			String newLoadedPluginId = pluginBox.loadPlugin(Paths.get(pluginsRootPath, newPluginFileName));
			pluginBox.startUpPlugin(newLoadedPluginId);

			IntegrationType integrationType = new IntegrationType();
			integrationType.setName(newLoadedPluginId);
			integrationType.setIntegrationGroup(reportPortalIntegration.getIntegrationGroup());
			integrationType.setCreationDate(LocalDateTime.now());

			integrationTypeRepository.save(integrationType);

			deleteTempPlugin(newPluginTempPath);

			return new EntryCreatedRS(integrationType.getId());

		} else {

			oldPlugin.ifPresent(this::reloadOldPlugin);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load new plugin from file = {}", newPluginFileName).get()
			);
		}

	}

	private void createTempPluginsFolderIfNotExists(String path) {
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
	private String resolveExtensionAndUploadTempPlugin(MultipartFile pluginFile, String pluginsTempPath) {

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

	private String extractPluginId(Path pluginPath) {

		try {

			PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);
			return pluginDescriptor.getPluginId();

		} catch (PluginException e) {

			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, e);
		}
	}

	private void reloadOldPlugin(PluginWrapper oldPlugin) {
		pluginBox.startUpPlugin(ofNullable(pluginBox.loadPlugin(oldPlugin.getPluginPath())).orElseThrow(() -> new ReportPortalException(
				ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Unable to load old plugin with id = '{}", oldPlugin.getPluginId()).get()
		)));
	}

	private boolean validateNewPluginExtensionClasses(String newPluginId) {

		PluginWrapper newPlugin = pluginBox.getPluginById(newPluginId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Plugin with id = {} has not been found.", newPluginId).get()
				));

		return newPlugin.getPluginManager()
				.getExtensionClasses(newPluginId)
				.stream()
				.map(ExtensionPoint::findByExtension)
				.anyMatch(Optional::isPresent);
	}

	private Optional<PluginWrapper> retrieveOldPlugin(String newPluginId, String newPluginName) {

		Optional<PluginWrapper> oldPlugin = pluginBox.getPluginById(newPluginId);

		oldPlugin.ifPresent(p -> {

			if (!pluginBox.unloadPlugin(p.getPluginId())) {
				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Failed to stop old plugin with id = {}", p.getPluginId()).get()
				);
			}

			integrationTypeRepository.deleteByName(p.getPluginId());
		});

		validateNewPluginFile(oldPlugin, newPluginName);

		return oldPlugin;
	}

	private void validateNewPluginFile(Optional<PluginWrapper> oldPlugin, String newPluginName) {
		if (new File(pluginsRootPath, newPluginName).exists()) {

			if (!oldPlugin.isPresent() || !Paths.get(pluginsRootPath, newPluginName).equals(oldPlugin.get().getPluginPath())) {
				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to rewrite plugin file = '{}' with the different plugin type", newPluginName)
								.get()
				);
			}
		}
	}

	private void deleteOldPlugin(PluginWrapper pluginWrapper, String newPluginFileName) {
		if (!pluginWrapper.getPluginPath().equals(Paths.get(pluginsRootPath, newPluginFileName))) {
			try {
				Files.deleteIfExists(pluginWrapper.getPluginPath());
			} catch (IOException e) {

				reloadOldPlugin(pluginWrapper);

				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Unable to delete the old plugin file with id = {}", pluginWrapper.getPluginId()).get()
				);
			}
		}
	}

	private void deleteTempPlugin(Path tempPluginPath) {
		try {

			Files.deleteIfExists(tempPluginPath);

		} catch (IOException e) {
			//error during temp plugin is not crucial, temp files cleaning will be delegated to //TODO impl Quartz job to clean temp files
		}
	}
}
