package com.epam.ta.reportportal.core.integration.plugin.м2;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ExtensionClassesValidator implements PluginInfoValidator {

	private final PluginManager validationBox;

	@Autowired
	public ExtensionClassesValidator(PluginManager validationBox) {
		this.validationBox = validationBox;
	}

	@Override
	public void validate(PluginInfo pluginInfo) throws PluginValidationException {

		final String pluginId = ofNullable(validationBox.loadPlugin(pluginInfo.getOriginalFilePath())).orElseThrow(() -> {
			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Failed to load plugin into validation box from file = '{}'",
							pluginInfo.getOriginalFilePath().getFileName().toString()
					).get()
			);
		});

		validationBox.startPlugin(pluginId);
		final PluginWrapper plugin = validationBox.getPlugin(pluginId);

		final boolean validExtension = plugin.getPluginManager()
				.getExtensionClasses(plugin.getPluginId())
				.stream()
				.map(ExtensionPoint::findByExtension)
				.anyMatch(Optional::isPresent);

		validationBox.unloadPlugin(pluginId);

		if (!validExtension) {
			throw new PluginValidationException(Suppliers.formattedSupplier(
					"New plugin with id = '{}' doesn't have mandatory extension classes.",
					pluginId
			).get());
		}

	}

}
