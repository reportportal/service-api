/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.auth.integration.provider;

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import java.util.Map;
import java.util.Optional;

public class AuthIntegrationStrategyProvider {

  private final Map<AuthIntegrationType, AuthIntegrationStrategy> authIntegrationStrategyMapping;

  public AuthIntegrationStrategyProvider(
      Map<AuthIntegrationType, AuthIntegrationStrategy> authIntegrationStrategyMapping) {
    this.authIntegrationStrategyMapping = authIntegrationStrategyMapping;
  }

  public Optional<AuthIntegrationStrategy> provide(AuthIntegrationType type) {
    return Optional.ofNullable(authIntegrationStrategyMapping.get(type));
  }
}
