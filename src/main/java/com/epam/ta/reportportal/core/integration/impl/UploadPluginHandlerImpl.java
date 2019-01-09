package com.epam.ta.reportportal.core.integration.impl;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.UploadPluginHandler;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.pf4j.util.FileUtils;
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
public class UploadPluginHandlerImpl implements UploadPluginHandler {

	public static final String PLUGIN_TEMP_DIRECTORY = "/temp/";

	@Value("${rp.plugins.path}")
	private String pluginsRootPath;

	private final PluginBox pluginBox;

	private final PluginDescriptorFinder pluginDescriptorFinder;

	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public UploadPluginHandlerImpl(PluginBox pluginBox, PluginDescriptorFinder pluginDescriptorFinder,
			IntegrationTypeRepository integrationTypeRepository) {
		this.pluginBox = pluginBox;
		this.pluginDescriptorFinder = pluginDescriptorFinder;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	public EntryCreatedRS uploadPlugin(MultipartFile pluginFile) {

		String pluginFileName = pluginFile.getOriginalFilename();

		BusinessRule.expect(pluginFileName, StringUtils::isNotBlank).verify(ErrorType.BAD_REQUEST_ERROR, "File name should be not empty.");

		String pluginsTempPath = pluginsRootPath + PLUGIN_TEMP_DIRECTORY;
		createTempPluginsFolderIfNotExists(pluginsTempPath);

		Path newPluginTempPath = Paths.get(pluginsTempPath, pluginFileName);
		uploadTempPlugin(pluginFile, newPluginTempPath);

		String pluginId = extractPluginId(newPluginTempPath);

		ReportPortalIntegrationEnum reportPortalIntegration = ReportPortalIntegrationEnum.findByName(pluginId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Unknown integration type - {} ", pluginId).get()
				));

		Optional<PluginWrapper> oldPlugin = pluginBox.getPluginById(pluginId);

		oldPlugin.ifPresent(p -> {

			if (!pluginBox.unloadPlugin(p.getPluginId())) {
				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Failed to stop old plugin with id = {}", p.getPluginId()).get()
				);
			}

			integrationTypeRepository.deleteByName(p.getPluginId());
		});

		String newPluginId = pluginBox.loadPlugin(newPluginTempPath);

		if (ofNullable(newPluginId).isPresent()) {

			if (!validateNewPlugin(newPluginId)) {

				pluginBox.unloadPlugin(newPluginId);
				oldPlugin.ifPresent(this::reloadOldPlugin);
				deleteTempPlugin(newPluginTempPath);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("New plugin with id = {} is not valid.")
				);

			}

			pluginBox.unloadPlugin(newPluginId);

			oldPlugin.ifPresent(p -> {
				try {
					Files.deleteIfExists(p.getPluginPath());
				} catch (IOException e) {
					reloadOldPlugin(p);

					throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
							Suppliers.formattedSupplier("Unable to delete the old plugin file with id = {}", p.getPluginId()).get()
					);
				}
			});

			try {
				org.apache.commons.io.FileUtils.copyFile(new File(pluginsTempPath, pluginFileName),
						new File(pluginsRootPath, pluginFileName)
				);
			} catch (IOException e) {

				oldPlugin.ifPresent(this::reloadOldPlugin);

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to copy the new plugin file with id = {} to the root directory", newPluginId)
								.get()
				);
			}

			deleteTempPlugin(newPluginTempPath);

			String newLoadedPluginId = pluginBox.loadPlugin(Paths.get(pluginsRootPath, pluginFileName));
			pluginBox.startUpPlugin(newLoadedPluginId);

			IntegrationType integrationType = new IntegrationType();
			integrationType.setName(newLoadedPluginId);
			integrationType.setIntegrationGroup(reportPortalIntegration.getIntegrationGroup());
			integrationType.setCreationDate(LocalDateTime.now());

			integrationTypeRepository.save(integrationType);

			return new EntryCreatedRS(integrationType.getId());

		} else {

			oldPlugin.ifPresent(this::reloadOldPlugin);

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load new plugin from file = {}", pluginFileName).get()
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

	private void uploadTempPlugin(MultipartFile pluginFile, Path pluginPath) {
		try {

			Files.copy(pluginFile.getInputStream(), pluginPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy the new plugin file with name = {} to the temp directory",
							pluginFile.getOriginalFilename()
					).get()
			);
		}

		if (!FileUtils.isJarFile(pluginPath) && !FileUtils.isZipFile(pluginPath)) {
			try {
				Files.deleteIfExists(pluginPath);
			} catch (IOException e) {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"An error occurred during removing - " + pluginFile.getOriginalFilename()
				);
			}
		}
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
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION)));
	}

	private boolean validateNewPlugin(String newPluginId) {

		PluginWrapper newPlugin = pluginBox.getPluginById(newPluginId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Plugin with id = {} has not been found.").get()
				));

		return newPlugin.getPluginManager()
				.getExtensionClasses(newPluginId)
				.stream()
				.map(ExtensionPoint::findByExtension)
				.anyMatch(Optional::isPresent);
	}

	private void deleteTempPlugin(Path tempPluginPath) {
		try {

			Files.deleteIfExists(tempPluginPath);

		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
					Suppliers.formattedSupplier("Unable to delete the new plugin file from the directory = {}",
							tempPluginPath.getParent().toString()
					)
			);
		}
	}
}
