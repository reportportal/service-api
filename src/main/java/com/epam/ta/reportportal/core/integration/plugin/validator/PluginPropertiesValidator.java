package com.epam.ta.reportportal.core.integration.plugin.validator;

import com.epam.ta.reportportal.core.integration.plugin.validator.exception.PluginValidationException;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginPropertiesValidator implements PluginInfoValidator {
	@Override
	public void validate(PluginInfo pluginInfo) throws PluginValidationException {
		final boolean versionSpecified = ofNullable(pluginInfo.getVersion()).filter(StringUtils::isNotBlank).isPresent();
		if (!versionSpecified) {
			throw new PluginValidationException("Plugin version should be specified.");
		}
	}
}
