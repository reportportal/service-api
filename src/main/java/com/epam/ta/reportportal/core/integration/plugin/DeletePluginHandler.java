package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface DeletePluginHandler {

	/**
	 * Delete plugin representation from the database and from the {@link com.epam.ta.reportportal.core.plugin.Pf4jPluginBox} instance
	 *
	 * @param id {@link com.epam.ta.reportportal.entity.integration.IntegrationType#id}
	 * @return {@link OperationCompletionRS} with result message
	 */
	OperationCompletionRS deleteById(Long id);
}
