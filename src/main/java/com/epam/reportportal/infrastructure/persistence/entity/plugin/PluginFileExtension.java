/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.infrastructure.persistence.entity.plugin;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum PluginFileExtension {

  JAR(".jar"),
  ZIP(".zip");

  private String extension;

  PluginFileExtension(String extension) {
    this.extension = extension;
  }

  public static Optional<PluginFileExtension> findByExtension(String extension) {

    return Arrays.stream(values()).filter(e -> e.getExtension().equalsIgnoreCase(extension))
        .findFirst();
  }

  public String getExtension() {
    return extension;
  }
}
