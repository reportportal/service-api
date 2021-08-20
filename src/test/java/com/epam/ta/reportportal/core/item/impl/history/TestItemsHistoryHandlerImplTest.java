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

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.ReportPortalUserUtil;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		ReportPortalUser rpUser = ReportPortalUserUtil.getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		assertThrows(ReportPortalException.class,
				() -> handler.getItemsHistory(extractProjectDetails(rpUser, "test_project"),
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
		ReportPortalUser rpUser = ReportPortalUserUtil.getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		assertThrows(ReportPortalException.class,
				() -> handler.getItemsHistory(extractProjectDetails(rpUser, "test_project"),
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