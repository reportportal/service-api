package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.DeletePluginHandler;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DeletePluginHandlerImpl implements DeletePluginHandler {

	private final IntegrationTypeRepository integrationTypeRepository;
	private final Pf4jPluginBox pluginBox;

	@Autowired
	public DeletePluginHandlerImpl(IntegrationTypeRepository integrationTypeRepository, Pf4jPluginBox pluginBox) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginBox = pluginBox;
	}

	@Override
	public OperationCompletionRS deleteById(Long id) {

		IntegrationType integrationType = integrationTypeRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Plugin with id = '{}' not found", id).get()
				));

		PluginWrapper pluginWrapper = pluginBox.getPluginById(integrationType.getName())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Plugin with id = '{}' not found", id).get()
				));

		if (pluginBox.deletePlugin(pluginWrapper.getPluginId())) {
			integrationTypeRepository.deleteById(integrationType.getId());
			return new OperationCompletionRS(Suppliers.formattedSupplier("Plugin = '{}' has been successfully removed",
					integrationType.getName()
			).get());
		} else {
			throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR, "Unable to remove from plugin manager.");
		}

	}
}
