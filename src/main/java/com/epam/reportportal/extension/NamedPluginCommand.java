package com.epam.reportportal.extension;

/**
 * Base interface for plugin commands that expose a name identifier.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface NamedPluginCommand {

  String getName();
}
