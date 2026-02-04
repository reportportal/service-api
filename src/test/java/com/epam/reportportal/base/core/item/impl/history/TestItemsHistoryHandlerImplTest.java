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

package com.epam.reportportal.base.core.item.impl.history;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.ReportPortalUserUtil;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.core.item.impl.history.param.HistoryRequestParams;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class TestItemsHistoryHandlerImplTest {

  @Mock
  private TestItemRepository testItemRepository;

  @InjectMocks
  private TestItemsHistoryHandlerImpl handler;

  @Test
  void historyDepthLowerThanBoundTest() {
    ReportPortalUser rpUser = ReportPortalUserUtil.getRpUser("test", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);

    assertThrows(ReportPortalException.class,
        () -> handler.getItemsHistory(rpUserToMembership(rpUser),
            Filter.builder()
                .withTarget(TestItem.class)
                .withCondition(FilterCondition.builder().eq(CRITERIA_ID, "1").build())
                .build(),
            PageRequest.of(0, 10),
            HistoryRequestParams.of(0, 1L, 1L, 1L, null, 1L, 1, false),
            rpUser
        )
    );
  }

  @Test
  void historyDepthGreaterThanBoundTest() {
    ReportPortalUser rpUser = ReportPortalUserUtil.getRpUser("test", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);

    assertThrows(ReportPortalException.class,
        () -> handler.getItemsHistory(rpUserToMembership(rpUser),
            Filter.builder()
                .withTarget(TestItem.class)
                .withCondition(FilterCondition.builder().eq(CRITERIA_ID, "1").build())
                .build(),
            PageRequest.of(0, 10),
            HistoryRequestParams.of(31, 1L, 1L, 1L, "table", 1L, 1, false),
            rpUser
        )
    );
  }
}
