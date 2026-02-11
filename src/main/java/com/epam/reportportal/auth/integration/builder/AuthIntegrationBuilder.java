/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.auth.integration.builder;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class AuthIntegrationBuilder {

  private final Integration integration;

  public AuthIntegrationBuilder() {
    integration = new Integration();
  }

  public AuthIntegrationBuilder(Integration integration) {
    this.integration = integration;
  }

  public AuthIntegrationBuilder addCreator(String username) {
    integration.setCreator(username);
    return this;
  }

  public AuthIntegrationBuilder addIntegrationType(IntegrationType type) {
    integration.setType(type);
    return this;
  }

  public AuthIntegrationBuilder addCreationDate(Instant creationDate) {
    integration.setCreationDate(creationDate);
    return this;
  }

  public @NotNull Integration build() {
    return integration;
  }
}
