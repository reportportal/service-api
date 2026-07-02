package com.epam.reportportal.extension.builtin.email;

import com.epam.reportportal.extension.builtin.BuiltinExtension;
import com.epam.reportportal.extension.builtin.email.command.TestConnectionCommand;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("email")
public class EmailExtension implements BuiltinExtension {

  @Autowired
  TestConnectionCommand testConnectionCommand;

  @Override
  public Map<String, ExtensionCommand<?>> getIntegrationExtensionCommands() {
    var commands = new HashMap<String, ExtensionCommand<?>>();
    commands.put(testConnectionCommand.getName(), testConnectionCommand);
    return commands;
  }
}
