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

package com.epam.ta.reportportal.core.item.impl.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.item.impl.provider.DataProviderHandler;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ControllerUtils;
import com.epam.reportportal.rules.exception.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.ta.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class MaterializedWidgetProviderHandlerImpl implements DataProviderHandler {

	private static final String WIDGET_ID_PARAM = "widgetId";

	@Autowired
	private Map<WidgetType, DataProviderHandler> testItemWidgetDataProviders;

	@Autowired
	private WidgetRepository widgetRepository;

	@Override
	public Page<TestItem> getTestItems(Queryable filter, Pageable pageable, MembershipDetails membershipDetails,
			ReportPortalUser user, Map<String, String> providerParams) {
		WidgetType widgetType = updateProviderParams(membershipDetails, providerParams);
		return testItemWidgetDataProviders.get(widgetType).getTestItems(filter, pageable, membershipDetails, user, providerParams);
	}

	@Override
	public Set<Statistics> accumulateStatistics(Queryable filter, MembershipDetails membershipDetails, ReportPortalUser user,
			Map<String, String> providerParams) {
		WidgetType widgetType = updateProviderParams(membershipDetails, providerParams);
		return testItemWidgetDataProviders.get(widgetType).accumulateStatistics(filter, membershipDetails, user, providerParams);
	}

	private WidgetType updateProviderParams(MembershipDetails membershipDetails, Map<String, String> providerParams) {
		Long widgetId = Optional.ofNullable(providerParams.get(WIDGET_ID_PARAM))
				.map(ControllerUtils::safeParseLong)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
						"Widget id must be provided for widget based items provider"
				));
		Widget widget = widgetRepository.findByIdAndProjectId(widgetId, membershipDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT,
						widgetId,
            membershipDetails.getProjectName()
				));
		validateState(widget.getWidgetOptions());
		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		providerParams.put(VIEW_NAME, widget.getWidgetOptions().getOptions().get(VIEW_NAME).toString());
		return widgetType;
	}

	private void validateState(WidgetOptions widgetOptions) {
		WidgetState widgetState = ofNullable(WidgetOptionUtil.getValueByKey(STATE, widgetOptions)).flatMap(WidgetState::findByName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_UPDATE_WIDGET_REQUEST, "Widget state not provided"));
		BusinessRule.expect(widgetState, it -> !WidgetState.RENDERING.equals(it))
				.verify(ErrorType.BAD_UPDATE_WIDGET_REQUEST, "Unable to remove widget in 'rendering' state");
	}
}
