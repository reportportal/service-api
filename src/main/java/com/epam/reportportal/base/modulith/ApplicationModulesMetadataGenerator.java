/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.modulith;

import com.epam.reportportal.ReportPortalApp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.util.ApplicationModulesExporter;

/**
 * Build-time-only entry point (invoked by the {@code generateModulithMetadata} Gradle task, never packaged as a
 * running part of the application). Runs the same {@link ApplicationModules#of(Class)} ArchUnit classpath scan that
 * Spring Modulith would otherwise perform on every application startup, and writes the result to
 * {@link ApplicationModulesExporter#DEFAULT_LOCATION} on the compiled resources path. When that resource is present
 * on the runtime classpath, Spring Modulith's {@code PrecomputedApplicationModuleInitializerInvoker} uses it
 * directly instead of re-running the scan at boot, removing a memory/CPU spike from application startup.
 */
public final class ApplicationModulesMetadataGenerator {

  private ApplicationModulesMetadataGenerator() {
  }

  public static void main(String[] args) throws IOException {
    Path output = Path.of(args[0]);
    Files.createDirectories(output.getParent());

    ApplicationModules modules = ApplicationModules.of(ReportPortalApp.class);
    Files.writeString(output, new ApplicationModulesExporter(modules).toFullJson());
  }
}
