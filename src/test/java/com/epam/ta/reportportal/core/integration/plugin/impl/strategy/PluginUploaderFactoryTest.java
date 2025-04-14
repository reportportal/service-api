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

package com.epam.ta.reportportal.core.integration.plugin.impl.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.PluginUploader;
import com.epam.ta.reportportal.core.integration.plugin.strategy.JarPluginUploader;
import com.epam.ta.reportportal.core.integration.plugin.strategy.JsonPluginUploader;
import com.epam.ta.reportportal.core.integration.plugin.strategy.PluginUploaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public class PluginUploaderFactoryTest {

  private PluginUploaderFactory factory;
  private JarPluginUploader jarPluginUploader;
  private JsonPluginUploader jsonPluginUploader;

  @BeforeEach
  void setUp() {
    jarPluginUploader = mock(JarPluginUploader.class);
    jsonPluginUploader = mock(JsonPluginUploader.class);
    factory = new PluginUploaderFactory(jarPluginUploader, jsonPluginUploader);
  }

  @Test
  void shouldReturnJarUploaderForJarContentType() {
    PluginUploader result = factory.getUploader("application/java-archive");
    assertEquals(jarPluginUploader, result);
  }

  @Test
  void shouldReturnJsonUploaderForJsonContentType() {
    PluginUploader result = factory.getUploader("application/json");
    assertEquals(jsonPluginUploader, result);
  }

  @Test
  void shouldThrowExceptionForUnsupportedContentType() {
    ReportPortalException exception = assertThrows(
        ReportPortalException.class,
        () -> factory.getUploader("unsupported/content-type")
    );

    assertEquals(ErrorType.PLUGIN_UPLOAD_ERROR, exception.getErrorType());
  }
}
