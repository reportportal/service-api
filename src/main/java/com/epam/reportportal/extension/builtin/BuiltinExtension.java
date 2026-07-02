package com.epam.reportportal.extension.builtin;

import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.HashMap;
import java.util.Map;

public interface BuiltinExtension {

  default Map<String, ExtensionCommand<?>> getCommonExtensionCommands() {
    return new HashMap<>();
  }

  default Map<String, ExtensionCommand<?>> getIntegrationExtensionCommands() {
    return new HashMap<>();
  }

}
