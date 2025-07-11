/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginRuntimeException;
import org.pf4j.util.FileUtils;
import org.pf4j.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin descriptor finder that reads the plugin descriptor from the manifest file. This class
 * implements the {@link PluginDescriptorFinder} interface and provides methods to check if a plugin
 * path is applicable, and to find the plugin descriptor from the given path.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public class DetailManifestPluginDescriptorFinder implements PluginDescriptorFinder {

  private static final Logger log = LoggerFactory.getLogger(
      DetailManifestPluginDescriptorFinder.class);

  public static final String PLUGIN_ID = "Plugin-Id";
  public static final String PLUGIN_NAME = "Plugin-Name";
  public static final String PLUGIN_DESCRIPTION = "Plugin-Description";
  public static final String PLUGIN_CLASS = "Plugin-Class";
  public static final String PLUGIN_VERSION = "Plugin-Version";
  public static final String PLUGIN_PROVIDER = "Plugin-Provider";
  public static final String PLUGIN_DEPENDENCIES = "Plugin-Dependencies";
  public static final String PLUGIN_REQUIRES = "Plugin-Requires";
  public static final String PLUGIN_LICENSE = "Plugin-License";
  public static final String PLUGIN_DOCUMENTATION = "Plugin-Documentation";
  public static final String PLUGIN_METADATA_PREFIX = "Plugin-Metadata-";
  public static final String PLUGIN_PROPERTIES_PREFIX = "Plugin-Property-";
  public static final String PLUGIN_BINARY_DATA_PREFIX = "Plugin-Binary-Data-";

  @Override
  public boolean isApplicable(Path pluginPath) {
    return Files.exists(pluginPath) && (Files.isDirectory(pluginPath) || FileUtils.isZipOrJarFile(
        pluginPath));
  }

  @Override
  public PluginDescriptor find(Path pluginPath) {
    var manifest = readManifest(pluginPath);

    return createPluginDescriptor(manifest);
  }

  private PluginDescriptor createPluginDescriptor(Manifest manifest) {
    var attributes = manifest.getMainAttributes();

    return DetailPluginDescriptor.builder()
        .pluginId(attributes.getValue(PLUGIN_ID))
        .pluginName(attributes.getValue(PLUGIN_NAME))
        .pluginDescription(attributes.getValue(PLUGIN_DESCRIPTION))
        .pluginClass(attributes.getValue(PLUGIN_CLASS))
        .version(attributes.getValue(PLUGIN_VERSION))
        .requires(attributes.getValue(PLUGIN_REQUIRES))
        .provider(attributes.getValue(PLUGIN_PROVIDER))
        .license(attributes.getValue(PLUGIN_LICENSE))
        .documentation(attributes.getValue(PLUGIN_DOCUMENTATION))
        .dependencies(attributes.getValue(PLUGIN_DEPENDENCIES))
        .metadata(processAttributesByPrefix(attributes, PLUGIN_METADATA_PREFIX))
        .properties(processAttributesByPrefix(attributes, PLUGIN_PROPERTIES_PREFIX))
        .binaryData(processAttributesByPrefix(attributes, PLUGIN_BINARY_DATA_PREFIX))
        .build();
  }

  private Map<String, Object> processAttributesByPrefix(Attributes attributes, String prefix) {
    var map = new HashMap<String, Object>();

    attributes.entrySet().stream()
        .filter(entry -> entry.getKey()
            .toString()
            .startsWith(prefix))
        .forEach(entry -> {
          String key = entry.getKey().toString().substring(prefix.length());
          String value = entry.getValue().toString();
          if (StringUtils.isNotNullOrEmpty(key)) {
            map.put(key, parseValue(value));
          }
        });

    return map;
  }

  private Object parseValue(String value) {
    if (Pattern.matches("^\\d+$", value)) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException ignored) {
        return value;
      }
    }

    if (Pattern.matches("^\\d+\\.\\d+$", value)) {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException ignored) {
        return value;
      }
    }

    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
      return Boolean.parseBoolean(value);
    }

    return value;
  }

  private Manifest readManifest(Path pluginPath) {
    if (FileUtils.isJarFile(pluginPath)) {
      return readManifestFromJar(pluginPath);
    }

    if (FileUtils.isZipFile(pluginPath)) {
      return readManifestFromZip(pluginPath);
    }

    return readManifestFromDirectory(pluginPath);
  }

  private Manifest readManifestFromJar(Path jarPath) {
    try (JarFile jar = new JarFile(jarPath.toFile())) {
      return jar.getManifest();
    } catch (IOException e) {
      throw new PluginRuntimeException(e, "Cannot read manifest from {}", jarPath);
    }
  }

  private Manifest readManifestFromZip(Path zipPath) {
    try (ZipFile zip = new ZipFile(zipPath.toFile())) {
      ZipEntry manifestEntry = zip.getEntry("classes/META-INF/MANIFEST.MF");
      try (InputStream manifestInput = zip.getInputStream(manifestEntry)) {
        return new Manifest(manifestInput);
      }
    } catch (IOException e) {
      throw new PluginRuntimeException(e, "Cannot read manifest from {}", zipPath);
    }
  }

  private Manifest readManifestFromDirectory(Path pluginPath) {
    // legacy (the path is something like "classes/META-INF/MANIFEST.MF")
    Path manifestPath = FileUtils.findFile(pluginPath, "MANIFEST.MF");
    if (manifestPath == null) {
      throw new PluginRuntimeException("Cannot find the manifest path");
    }

    log.debug("Lookup plugin descriptor in '{}'", manifestPath);
    if (Files.notExists(manifestPath)) {
      throw new PluginRuntimeException("Cannot find '{}' path", manifestPath);
    }

    try (InputStream input = Files.newInputStream(manifestPath)) {
      return new Manifest(input);
    } catch (IOException e) {
      throw new PluginRuntimeException(e, "Cannot read manifest from {}", pluginPath);
    }
  }
}
