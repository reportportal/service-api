package com.epam.reportportal.extension.util;

import java.util.Optional;
import java.util.jar.Manifest;

public final class PluginManifestUtils {

  public static String PLUGIN_ID_PROPERTY = "Plugin-Id";

  private PluginManifestUtils() {
  }

  public static String readPluginIdFromManifest(Class<?> clazz, String defaultValue) {
    try {
      return Optional.ofNullable(new Manifest(clazz.getResourceAsStream("/META-INF/MANIFEST.MF"))
              .getMainAttributes()
              .getValue(PLUGIN_ID_PROPERTY))
          .orElse(defaultValue);
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
