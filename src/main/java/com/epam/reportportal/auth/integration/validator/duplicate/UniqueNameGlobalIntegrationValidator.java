/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.auth.integration.validator.duplicate;

import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UniqueNameGlobalIntegrationValidator implements IntegrationDuplicateValidator {

  private final IntegrationRepository integrationRepository;

  @Autowired
  public UniqueNameGlobalIntegrationValidator(IntegrationRepository integrationRepository) {
    this.integrationRepository = integrationRepository;
  }

  @Override
  public void validate(Integration integration) {
    integrationRepository.findByNameAndTypeIdAndProjectIdIsNull(integration.getName(),
            integration.getType().getId())
        .ifPresent(found -> BusinessRule.expect(found.getId(), id -> id.equals(integration.getId()))
            .verify(ErrorType.INTEGRATION_ALREADY_EXISTS, integration.getName()));
  }
}
