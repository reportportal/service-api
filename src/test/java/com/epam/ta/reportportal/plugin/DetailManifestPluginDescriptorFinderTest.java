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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.PluginRuntimeException;

/**
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public class DetailManifestPluginDescriptorFinderTest {

  private DetailManifestPluginDescriptorFinder finder;

  @TempDir
  Path tempDir;

  @BeforeEach
  public void setUp() {
    finder = new DetailManifestPluginDescriptorFinder();
  }

  @Test
  public void isApplicable() throws IOException {
    var jarPath = tempDir.resolve("plugin.jar");
    createJarFile(jarPath, createBasicManifest());

    assertTrue(finder.isApplicable(jarPath));
    assertFalse(finder.isApplicable(tempDir.resolve("non-existent-file")));
  }

  @Test
  public void findFromJar() throws IOException {
    var manifest = createFullManifest();
    var jarPath = tempDir.resolve("test-plugin.jar");
    createJarFile(jarPath, manifest);

    var descriptor = finder.find(jarPath);
    var detailDescriptor = (DetailPluginDescriptor) descriptor;

    assertInstanceOf(DetailPluginDescriptor.class, descriptor);
    assertEquals("test-plugin", detailDescriptor.getPluginId());
    assertEquals("Test Plugin", detailDescriptor.getPluginName());
    assertEquals("A test plugin", detailDescriptor.getPluginDescription());
    assertEquals("com.example.TestPlugin", detailDescriptor.getPluginClass());
    assertEquals("1.0.0", detailDescriptor.getVersion());
    assertEquals(">=1.8.0", detailDescriptor.getRequires());
    assertEquals("Example Provider", detailDescriptor.getProvider());
    assertEquals("Apache-2.0", detailDescriptor.getLicense());
    assertEquals("https://example.com/docs", detailDescriptor.getDocumentation());

    assertEquals(2, detailDescriptor.getDependencies().size());
    assertEquals("plugin1", detailDescriptor.getDependencies().get(0).getPluginId());
    assertEquals("plugin2", detailDescriptor.getDependencies().get(1).getPluginId());

    assertEquals("value1", detailDescriptor.getMetadata().get("stringValue"));
    assertEquals(42, detailDescriptor.getMetadata().get("intValue"));
    assertEquals(3.14, detailDescriptor.getMetadata().get("doubleValue"));
    assertEquals(true, detailDescriptor.getMetadata().get("boolValue"));

    assertEquals("prop1", detailDescriptor.getProperties().get("stringValue"));
    assertEquals(100, detailDescriptor.getProperties().get("intProp"));
  }

  @Test
  public void missingManifestInDirectory() throws IOException {
    var pluginDir = tempDir.resolve("empty-plugin");
    Files.createDirectories(pluginDir);

    assertThrows(PluginRuntimeException.class, () -> finder.find(pluginDir));
  }

  @Test
  public void valueParsing() throws IOException {
    var manifest = createFullManifest();
    var jarPath = tempDir.resolve("test-plugin.jar");
    createJarFile(jarPath, manifest);

    var descriptor = (DetailPluginDescriptor)  finder.find(jarPath);

    assertEquals(Integer.class, descriptor.getMetadata().get("intValue").getClass());
    assertEquals(Double.class, descriptor.getMetadata().get("doubleValue").getClass());
    assertEquals(Boolean.class, descriptor.getMetadata().get("boolValue").getClass());
    assertEquals(String.class, descriptor.getMetadata().get("stringValue").getClass());
  }

  private Manifest createBasicManifest() {
    var manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_ID, "test-plugin");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_NAME, "Test Plugin");
    return manifest;
  }

  private Manifest createFullManifest() {
    var manifest = new Manifest();
    var attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_ID, "test-plugin");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_NAME, "Test Plugin");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_DESCRIPTION, "A test plugin");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_CLASS, "com.example.TestPlugin");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_VERSION, "1.0.0");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_REQUIRES, ">=1.8.0");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_PROVIDER, "Example Provider");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_LICENSE, "Apache-2.0");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_DOCUMENTATION, "https://example.com/docs");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "plugin1,plugin2");

    // Add metadata
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_METADATA_PREFIX + "stringValue", "value1");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_METADATA_PREFIX + "intValue", "42");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_METADATA_PREFIX + "doubleValue", "3.14");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_METADATA_PREFIX + "boolValue", "true");

    // Add properties
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_PROPERTIES_PREFIX + "stringValue", "prop1");
    attributes.putValue(DetailManifestPluginDescriptorFinder.PLUGIN_PROPERTIES_PREFIX + "intProp", "100");

    return manifest;
  }

  private void createJarFile(Path jarPath, Manifest manifest) throws IOException {
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      var entry = new ZipEntry("dummy.txt");
      jos.putNextEntry(entry);
      jos.write("dummy content".getBytes());
      jos.closeEntry();
    }
  }
}
