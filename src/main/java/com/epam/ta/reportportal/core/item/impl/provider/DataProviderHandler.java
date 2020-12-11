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

package com.epam.ta.reportportal.core.item.impl.provider;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface DataProviderHandler {

	Page<TestItem> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Map<String, String> params);

	Set<Statistics> accumulateStatistics(Queryable filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Map<String, String> params);

}
