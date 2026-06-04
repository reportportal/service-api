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

package com.epam.reportportal.base.ws.converter.builders;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.PluginTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.google.common.collect.Maps;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Builds {@link com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType} instances for
 * API layers.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class IntegrationTypeBuilder implements Supplier<IntegrationType> {

  private final IntegrationType integrationType;

  public IntegrationTypeBuilder() {
    this.integrationType = new IntegrationType();
    this.integrationType.setCreationDate(Instant.now());
    integrationType.setDetails(createIntegrationTypeDetails());
  }

  public IntegrationTypeBuilder(final IntegrationType integrationType) {
    this.integrationType = integrationType;
    if (this.integrationType.getDetails() == null) {
      this.integrationType.setDetails(createIntegrationTypeDetails());
    }
  }

  public static IntegrationTypeDetails createIntegrationTypeDetails() {
    IntegrationTypeDetails integrationTypeDetails = new IntegrationTypeDetails();
    integrationTypeDetails.setDetails(Maps.newHashMap());
    return integrationTypeDetails;
  }

  public IntegrationTypeBuilder setName(String name) {
    integrationType.setName(name);
    return this;
  }

  public IntegrationTypeBuilder setPluginType(PluginTypeEnum pluginType) {
    integrationType.setPluginType(pluginType);
    return this;
  }

  public IntegrationTypeBuilder setIntegrationGroup(IntegrationGroupEnum integrationGroup) {
    integrationType.setIntegrationGroup(integrationGroup);
    return this;
  }

  public IntegrationTypeBuilder setDetails(IntegrationTypeDetails typeDetails) {
    integrationType.setDetails(typeDetails);
    return this;
  }

  public IntegrationTypeBuilder setEnabled(boolean enabled) {
    integrationType.setEnabled(enabled);
    return this;
  }

  public IntegrationTypeBuilder setAuthFlow(IntegrationAuthFlowEnum authFlow) {
    integrationType.setAuthFlow(authFlow);
    return this;
  }

  @NotNull
  @Override
  public IntegrationType get() {
    return integrationType;
  }
}
