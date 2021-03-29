package com.epam.ta.reportportal.core.integration.plugin.м2;

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.core.plugin.PluginMetadata;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class IntegrationTypeHandlerImpl implements IntegrationTypeHandler {

	private final IntegrationTypeRepository integrationTypeRepository;

	public IntegrationTypeHandlerImpl(IntegrationTypeRepository integrationTypeRepository) {
		this.integrationTypeRepository = integrationTypeRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<IntegrationType> getByName(String name) {
		return Optional.empty();
	}

	@Override
	@Transactional
	public IntegrationType create(PluginMetadata pluginMetadata) {
		final IntegrationTypeBuilder builder = new IntegrationTypeBuilder();
		builder.setName(pluginMetadata.getPluginInfo().getId())
				.setIntegrationGroup(IntegrationGroupEnum.OTHER)
				.setDetails(IntegrationTypeBuilder.createIntegrationTypeDetails());
		builder.setEnabled(true);
		fillBuilder(builder, pluginMetadata);
		return integrationTypeRepository.save(builder.get());
	}

	@Override
	@Transactional
	public IntegrationType update(IntegrationType integrationType, PluginMetadata pluginMetadata) {
		final IntegrationTypeBuilder builder = new IntegrationTypeBuilder(integrationType);
		builder.setEnabled(true);
		fillBuilder(builder, pluginMetadata);
		return integrationTypeRepository.save(builder.get());
	}

	private void fillBuilder(IntegrationTypeBuilder builder, PluginMetadata pluginMetadata) {
		builder.putDetails(IntegrationTypeProperties.VERSION.getAttribute(), pluginMetadata.getPluginInfo().getVersion());
		ofNullable(pluginMetadata.getPluginParams()).ifPresent(builder::putDetails);
		ofNullable(pluginMetadata.getIntegrationGroup()).ifPresent(builder::setIntegrationGroup);
		fillBuilder(builder, pluginMetadata.getPluginPathInfo());
	}

	private void fillBuilder(IntegrationTypeBuilder builder, PluginPathInfo pluginPathInfo) {
		builder.putDetails(IntegrationTypeProperties.FILE_ID.getAttribute(), pluginPathInfo.getFileId());
		builder.putDetails(IntegrationTypeProperties.FILE_NAME.getAttribute(), pluginPathInfo.getFileName());
		builder.putDetails(IntegrationTypeProperties.RESOURCES_DIRECTORY.getAttribute(), pluginPathInfo.getResourcesPath().toString());
	}

}
