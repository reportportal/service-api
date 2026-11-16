package com.epam.reportportal.extension.builtin;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resolves built-in extension commands by plugin name and command name.
 */
@Component
public class BuiltinCommandResolver {

  @Autowired
  private Map<String, BuiltinExtension> builtInExtensions;

  public ExtensionCommand<?> resolve(String pluginName, String commandName) {
    return Optional.ofNullable(builtInExtensions.get(pluginName))
        .map(extension -> extension.getIntegrationExtensionCommands().get(commandName))
        .orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
            formattedSupplier("Command '{}' is not found in plugin {}.", commandName, pluginName).get()
        ));
  }
}
