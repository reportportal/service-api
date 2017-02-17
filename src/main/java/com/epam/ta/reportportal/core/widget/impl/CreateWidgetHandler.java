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

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.GadgetTypes.*;
import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.acl.SharingService;
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Lists;

import javax.inject.Provider;

/**
 * Default implementation of {@link ICreateWidgetHandler}
 *
 * @author Aliaksei_Makayed
 */
@Service
public class CreateWidgetHandler implements ICreateWidgetHandler {

	private WidgetRepository widgetRepository;

	private Provider<WidgetBuilder> widgetBuilder;

	private UserFilterRepository filterRepository;

	private CriteriaMapFactory criteriaMapFactory;

	private SharingService sharingService;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Autowired
	public void setWidgetBuilder(Provider<WidgetBuilder> widgetBuilder) {
		this.widgetBuilder = widgetBuilder;
	}

	@Autowired
	public void setUserFilterRepository(UserFilterRepository userFilterRepository) {
		this.filterRepository = userFilterRepository;
	}

	@Autowired
	public void setCriteriaMapFactory(CriteriaMapFactory criteriaMapFactory) {
		this.criteriaMapFactory = criteriaMapFactory;
	}

	@Autowired
	public void setSharingService(SharingService sharingService) {
		this.sharingService = sharingService;
	}

	@Override
	public EntryCreatedRS createWidget(WidgetRQ createWidgetRQ, String projectName, String userName) {
		List<Widget> widgetList = widgetRepository.findByProjectAndUser(projectName, userName);
		checkUniqueName(createWidgetRQ.getName(), widgetList);

		// load only type here it will be reused later for converting
		// content and metadata fields to db style
		UserFilter filter = filterRepository.findOneLoadACL(userName, createWidgetRQ.getApplyingFilter(), projectName);

		validateWidgetDataType(createWidgetRQ.getContentParameters().getType(), BAD_SAVE_WIDGET_REQUEST);
		validateGadgetType(createWidgetRQ.getContentParameters().getGadget(), BAD_SAVE_WIDGET_REQUEST);

		if ((findByName(createWidgetRQ.getContentParameters().getGadget()).get() != ACTIVITY)
				&& (findByName(createWidgetRQ.getContentParameters().getGadget()).get() != MOST_FAILED_TEST_CASES)) {
			checkApplyingFilter(filter, createWidgetRQ.getApplyingFilter(), userName);
		}

		if ((null != createWidgetRQ.getContentParameters().getMetadataFields())
				&& ((null == filter) || filter.getFilter().getTarget().equals(TestItem.class))) {
			if (createWidgetRQ.getContentParameters().getMetadataFields().contains(NUMBER))
				createWidgetRQ.getContentParameters().getMetadataFields().remove(NUMBER);
		}

		if ((null != createWidgetRQ.getContentParameters().getContentFields())
				&& ((null == filter) || filter.getFilter().getTarget().equals(TestItem.class))) {
			if (createWidgetRQ.getContentParameters().getContentFields().contains(NUMBER))
				createWidgetRQ.getContentParameters().getContentFields().remove(NUMBER);
			if (createWidgetRQ.getContentParameters().getContentFields().contains(USER))
				createWidgetRQ.getContentParameters().getContentFields().remove(USER);
		}

		Class<?> filterTarget;

		GadgetTypes gadget = findByName(createWidgetRQ.getContentParameters().getGadget()).get();
		if (gadget == UNIQUE_BUG_TABLE) {
			filterTarget = TestItem.class;
		} else if (gadget == ACTIVITY) {
			filterTarget = Activity.class;
		} else if (gadget == MOST_FAILED_TEST_CASES) {
			filterTarget = TestItem.class;
		} else {
			filterTarget = filter.getFilter().getTarget();
		}

		CriteriaMap<?> criteriaMap = criteriaMapFactory.getCriteriaMap(filterTarget);

		if (null != createWidgetRQ.getContentParameters().getContentFields()) {
			validateFields(createWidgetRQ.getContentParameters().getContentFields(), criteriaMap, BAD_SAVE_WIDGET_REQUEST);
		}
		if (null != createWidgetRQ.getContentParameters().getMetadataFields()) {
			validateFields(createWidgetRQ.getContentParameters().getMetadataFields(), criteriaMap, BAD_SAVE_WIDGET_REQUEST);
		}

		Widget widget = widgetBuilder.get().addWidgetRQ(createWidgetRQ).addFilter(createWidgetRQ.getApplyingFilter())
				.addProject(projectName)
				.addSharing(userName, projectName, createWidgetRQ.getDescription(), createWidgetRQ.getShare() == null ? false : createWidgetRQ.getShare()).build();

		// shareIfRequired(createWidgetRQ.getShare(), filter, userName,
		// projectName);
		shareIfRequired(createWidgetRQ.getShare(), widget, userName, projectName, filter);

		widgetRepository.save(widget);
		return new EntryCreatedRS(widget.getId());
	}

	private void shareIfRequired(Boolean isShare, Widget widget, String userName, String projectName, UserFilter filter) {
		if (isShare != null) {
			if (null != filter) {
				AclUtils.isPossibleToRead(filter.getAcl(), userName, projectName);
			}
			sharingService.modifySharing(Lists.newArrayList(widget), userName, projectName, isShare);
		}
	}

	/**
	 * Check is applying filter exists in database.
	 *
	 * @param filterID
	 */
	private void checkApplyingFilter(UserFilter filter, String filterID, String userName) {
		expect(filter, notNull()).verify(USER_FILTER_NOT_FOUND, filterID, userName);
		expect(filter.isLink(), equalTo(false)).verify(UNABLE_TO_CREATE_WIDGET, "Cannot create widget based on a link.");
	}
}