/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.widget.impl;

import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.acl.SharingService;
import com.epam.ta.reportportal.core.widget.IUpdateWidgetHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.events.WidgetUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.widget.content.GadgetTypes.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link IUpdateWidgetHandler}
 *
 * @author Aliaksei_Makayed
 *
 */
@Service
public class UpdateWidgetHandler implements IUpdateWidgetHandler {

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private UserFilterRepository filterRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private Provider<WidgetBuilder> widgetBuilder;

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

	@Autowired
	private SharingService sharingService;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public OperationCompletionRS updateWidget(String widgetId, WidgetRQ updateRQ, String userName,
											  String projectName, UserRole userRole) {
		Widget widget = widgetRepository.findOne(widgetId);
		Widget beforeUpdate = SerializationUtils.clone(widget);
		expect(widget, notNull()).verify(WIDGET_NOT_FOUND, widgetId);
		validateWidgetAccess(projectName, userName, userRole, widget, updateRQ);
		UserFilter newFilter = null;
		if (null != updateRQ.getApplyingFilter()) {
			String filterId = updateRQ.getApplyingFilter();
			newFilter = filterRepository.findOneLoadACL(userName, filterId, projectName);

			// skip filter validation for Activity and Most Failed Test Cases
			// widgets
			if (!(null != updateRQ.getContentParameters() && findByName(updateRQ.getContentParameters().getGadget()).isPresent()
					&& (findByName(updateRQ.getContentParameters().getGadget()).get() == ACTIVITY)
					&& (findByName(updateRQ.getContentParameters().getGadget()).get() == MOST_FAILED_TEST_CASES))) {
				expect(newFilter, notNull()).verify(USER_FILTER_NOT_FOUND, updateRQ.getApplyingFilter(), userName);
				expect(newFilter.isLink(), equalTo(false)).verify(UNABLE_TO_CREATE_WIDGET, "Widget cannot be based on a link");
			}
		}

		Widget newWidget = widgetBuilder.get().addWidgetRQ(updateRQ)
                .addDescription(updateRQ.getDescription())
                .build();

		validateWidgetFields(newWidget, newFilter, widget, userName, projectName);

		updateWidget(widget, newWidget, newFilter);

		shareIfRequired(updateRQ.getShare(), widget, userName, projectName, newFilter);

		widgetRepository.save(widget);

		eventPublisher.publishEvent(new WidgetUpdatedEvent(beforeUpdate, updateRQ, userName));
		return new OperationCompletionRS("Widget with ID = '" + widget.getId() + "' successfully updated.");
	}

    @Override
    public OperationCompletionRS updateEmpty(String projectName, String widgetId, WidgetRQ updateRQ, String userName, UserRole userRole) {
        Widget widget = widgetRepository.findOne(widgetId);
        Widget beforeUpdate = SerializationUtils.clone(widget);
        expect(widget, notNull()).verify(WIDGET_NOT_FOUND, widgetId);

        validateWidgetAccess(projectName, userName, userRole, widget, updateRQ);

        Widget newWidget = widgetBuilder.get().addWidgetRQ(updateRQ)
                .addDescription(updateRQ.getDescription())
                .build();

        updateWidget(widget, newWidget, null);
        shareIfRequired(updateRQ.getShare(), widget, userName, projectName, null);
        widgetRepository.save(widget);
        eventPublisher.publishEvent(new WidgetUpdatedEvent(beforeUpdate, updateRQ, userName));
        return new OperationCompletionRS("Widget with ID = '" + widget.getId() + "' successfully updated.");
    }

    private void validateWidgetAccess(String projectName, String userName, UserRole userRole, Widget widget, WidgetRQ updateRQ) {
        List<Widget> widgetList = widgetRepository.findByProjectAndUser(projectName, userName);
        if (null != updateRQ.getName() && !widget.getName().equals(updateRQ.getName())) {
            WidgetUtils.checkUniqueName(updateRQ.getName(), widgetList);
        }

        AclUtils.isAllowedToEdit(widget.getAcl(), userName, projectRepository.findProjectRoles(userName),
                widget.getName(), userRole);
        expect(widget.getProjectName(), equalTo(projectName)).verify(ACCESS_DENIED);
    }

    private void shareIfRequired(Boolean isShare, Widget widget, String userName, String projectName, UserFilter newFilter) {
		if (isShare != null) {
			if (null != newFilter) {
				AclUtils.isPossibleToRead(newFilter.getAcl(), userName, projectName);
			}
			sharingService.modifySharing(Lists.newArrayList(widget), userName, projectName, isShare);
		}
	}

	private void updateWidget(Widget oldWidget, Widget newValues, UserFilter filter) {
		if (newValues.getContentOptions() != null) {
			oldWidget.setContentOptions(newValues.getContentOptions());
		}
		if (newValues.getName() != null) {
			oldWidget.setName(newValues.getName());
		}
		if (filter != null) {
			oldWidget.setApplyingFilterId(filter.getId());
		}
		oldWidget.setDescription(newValues.getDescription());
	}

	/**
	 * Validate is content fields known to server and if them agreed with
	 * filter(new filter or current filter).
	 *
	 * @param newWidget
	 * @param newFilter
	 * @param widget
	 * @param userName
	 * @param projectName
	 */
	void validateWidgetFields(Widget newWidget, UserFilter newFilter, Widget widget, String userName, String projectName) {
		// if new filter, new content options are absent - validations is
		// redundant
		ContentOptions contentOptions = newWidget.getContentOptions();
		if (newFilter == null && null == contentOptions) {
			return;
		}
		Class<?> target = null;

		if ((null == contentOptions)
				|| (findByName(contentOptions.getGadgetType()).isPresent() && (findByName(contentOptions.getGadgetType()).get() != ACTIVITY)
				&& (findByName(contentOptions.getGadgetType()).get() != MOST_FAILED_TEST_CASES)
				&& (findByName(contentOptions.getGadgetType()).get() != PASSING_RATE_PER_LAUNCH))) {
			if (newFilter == null) {
				UserFilter currentFilter = filterRepository.findOneLoadACLAndType(userName, widget.getApplyingFilterId(), projectName);
				expect(currentFilter, notNull()).verify(BAD_UPDATE_WIDGET_REQUEST,
						formattedSupplier(
								"Unable update widget content parameters. Please specify new filter for widget. Current filter with id {} removed.",
								widget.getApplyingFilterId()));

				target = currentFilter.getFilter().getTarget();
			} else {
				target = newFilter.getFilter().getTarget();
			}
		}

		// check is new content fields agreed with new or current filter
		if (null != contentOptions) {
			if (TestItem.class.equals(target)) {
				removeLaunchSpecificFields(contentOptions);
			}

			WidgetUtils.validateWidgetDataType(contentOptions.getType(), BAD_UPDATE_WIDGET_REQUEST);
			WidgetUtils.validateGadgetType(contentOptions.getGadgetType(), BAD_UPDATE_WIDGET_REQUEST);

			if (findByName(contentOptions.getGadgetType()).get() == ACTIVITY) {
				target = Activity.class;
			}

			if (findByName(contentOptions.getGadgetType()).get() == MOST_FAILED_TEST_CASES) {
				target = TestItem.class;
			}

			if (findByName(contentOptions.getGadgetType()).get() == PASSING_RATE_PER_LAUNCH) {
				target = Launch.class;
			}

			CriteriaMap<?> criteriaMap = criteriaMapFactory.getCriteriaMap(target);
			if (null != contentOptions.getContentFields()) {
				WidgetUtils.validateFields(contentOptions.getContentFields(), criteriaMap, BAD_UPDATE_WIDGET_REQUEST);
			}
			if (null != contentOptions.getMetadataFields()) {
				WidgetUtils.validateFields(contentOptions.getMetadataFields(), criteriaMap, BAD_UPDATE_WIDGET_REQUEST);
			}
		}
	}

	void removeLaunchSpecificFields(ContentOptions contentOptions) {
		if (null != contentOptions.getMetadataFields() && contentOptions.getMetadataFields().contains(WidgetUtils.NUMBER))
			contentOptions.getMetadataFields().remove(WidgetUtils.NUMBER);
		if (null != contentOptions.getContentFields() && contentOptions.getContentFields().contains(WidgetUtils.NUMBER))
			contentOptions.getContentFields().remove(WidgetUtils.NUMBER);
		if (null != contentOptions.getContentFields() && contentOptions.getContentFields().contains(WidgetUtils.USER))
			contentOptions.getContentFields().remove(WidgetUtils.USER);
	}
}
