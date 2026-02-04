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

package com.epam.reportportal.base.core.bts.handler.impl;

import com.epam.reportportal.base.core.bts.handler.GetBugTrackingSystemHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetBugTrackingSystemHandlerImpl implements GetBugTrackingSystemHandler {

  private final IntegrationRepository integrationRepository;

  @Autowired
  public GetBugTrackingSystemHandlerImpl(IntegrationRepository integrationRepository) {
    this.integrationRepository = integrationRepository;
  }

  @Override
  public Optional<Integration> getEnabledProjectIntegration(
      MembershipDetails membershipDetails, String url,
      String btsProject) {

    Optional<Integration> integration = integrationRepository.findProjectBtsByUrlAndLinkedProject(
        url,
        btsProject,
        membershipDetails.getProjectId()
    );
    integration.ifPresent(this::validateBtsIntegration);
    return integration;
  }

  @Override
  public Optional<Integration> getEnabledProjectIntegration(
      MembershipDetails membershipDetails, Long integrationId) {

    Optional<Integration> integration = integrationRepository.findByIdAndProjectId(integrationId,
        membershipDetails.getProjectId());
    integration.ifPresent(this::validateBtsIntegration);
    return integration;
  }

  @Override
  public Optional<Integration> getEnabledGlobalIntegration(String url, String btsProject) {

    Optional<Integration> integration = integrationRepository.findGlobalBtsByUrlAndLinkedProject(
        url, btsProject);
    integration.ifPresent(this::validateBtsIntegration);
    return integration;
  }

  @Override
  public Optional<Integration> getEnabledGlobalIntegration(Long integrationId) {

    Optional<Integration> integration = integrationRepository.findGlobalById(integrationId);
    integration.ifPresent(this::validateBtsIntegration);
    return integration;
  }

  private void validateBtsIntegration(Integration integration) {

    BusinessRule.expect(integration,
            it -> IntegrationGroupEnum.BTS == it.getType().getIntegrationGroup())
        .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
            "Unable to test connection to the integration with type - '{}', Allowed type(es): '{}'",
            integration.getType().getIntegrationGroup(),
            IntegrationGroupEnum.BTS
        ));
  }
}
