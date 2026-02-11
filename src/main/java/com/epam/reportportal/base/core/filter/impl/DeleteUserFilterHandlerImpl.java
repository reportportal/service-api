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

package com.epam.reportportal.base.core.filter.impl;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.USER_FILTER_NOT_FOUND;
import static com.epam.reportportal.base.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.base.core.events.domain.FilterDeletedEvent;
import com.epam.reportportal.base.core.filter.DeleteUserFilterHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserFilterRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserFilterHandlerImpl implements DeleteUserFilterHandler {

  private final UserFilterRepository userFilterRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public OperationCompletionRS deleteFilter(Long id, MembershipDetails membershipDetails,
      ReportPortalUser user) {
    UserFilter userFilter = userFilterRepository.findByIdAndProjectId(id,
            membershipDetails.getProjectId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT,
            id,
            membershipDetails.getProjectName()
        ));
    expect(userFilter.getProject().getId(),
        Predicate.isEqual(membershipDetails.getProjectId())).verify(
        USER_FILTER_NOT_FOUND,
        id,
        membershipDetails.getProjectId(),
        user.getUserId()
    );
    userFilterRepository.delete(userFilter);
    eventPublisher.publishEvent(
        new FilterDeletedEvent(TO_ACTIVITY_RESOURCE.apply(userFilter), user.getUserId(),
            user.getUsername(),
            membershipDetails.getOrgId()));
    return new OperationCompletionRS("User filter with ID = '" + id + "' successfully deleted.");
  }
}
