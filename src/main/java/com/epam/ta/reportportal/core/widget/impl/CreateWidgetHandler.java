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
import com.epam.ta.reportportal.core.widget.ICreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.events.WidgetCreatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Provider;
import java.util.List;

import static com.epam.ta.reportportal.core.widget.content.GadgetTypes.findByName;
import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_SAVE_WIDGET_REQUEST;

/**
 * Default implementation of {@link ICreateWidgetHandler}
 *
 * @author Aliaksei_Makayed
 */
@Service
@Resource
public class CreateWidgetHandler implements ICreateWidgetHandler {

	private WidgetRepository widgetRepository;

	private Provider<WidgetBuilder> widgetBuilder;

	private UserFilterRepository filterRepository;

	private CriteriaMapFactory criteriaMapFactory;

	private SharingService sharingService;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
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

		String widgetType = createWidgetRQ.getContentParameters().getType();
		String gadgetType = createWidgetRQ.getContentParameters().getGadget();

		validateWidgetDataType(widgetType, BAD_SAVE_WIDGET_REQUEST);
		validateGadgetType(gadgetType, BAD_SAVE_WIDGET_REQUEST);

		Widget widget = create(createWidgetRQ, projectName, userName);
		widgetRepository.save(widget);
		eventPublisher.publishEvent(new WidgetCreatedEvent(createWidgetRQ, userName, projectName, widget.getId()));
		return new EntryCreatedRS(widget.getId());
	}

	private Widget create(WidgetRQ createWidgetRQ, String projectName, String userName) {
		GadgetTypes gadget = findByName(createWidgetRQ.getContentParameters().getGadget()).get();

		UserFilter filter = null;
		if (!withoutFilter.containsKey(gadget)) {
			filter = filterRepository.findOneLoadACL(userName, createWidgetRQ.getFilterId(), projectName);
			checkApplyingFilter(filter, createWidgetRQ.getFilterId(), userName);
		}

		clearContentParameters(createWidgetRQ.getContentParameters(), filter);
		validateContentParameters(createWidgetRQ.getContentParameters(), filter, gadget);

		Widget widget = widgetBuilder.get()
				.addWidgetRQ(createWidgetRQ)
				.addFilter(createWidgetRQ.getFilterId())
				.addProject(projectName)
				.addSharing(userName, projectName, createWidgetRQ.getDescription(),
						createWidgetRQ.getShare() == null ? false : createWidgetRQ.getShare()
				)
				.build();

		shareIfRequired(createWidgetRQ.getShare(), widget, userName, projectName, filter);
		return widget;
	}

	private void shareIfRequired(Boolean isShare, Widget widget, String userName, String projectName, UserFilter filter) {
		if (isShare != null) {
			if (null != filter) {
				AclUtils.isPossibleToRead(filter.getAcl(), userName, projectName);
			}
			sharingService.modifySharing(Lists.newArrayList(widget), userName, projectName, isShare);
		}
	}

	private void clearContentParameters(ContentParameters contentParameters, UserFilter filter) {
		if ((null != contentParameters.getMetadataFields()) && ((null == filter) || filter.getFilter()
				.getTarget()
				.equals(TestItem.class))) {
			if (contentParameters.getMetadataFields().contains(NUMBER)) {
				contentParameters.getMetadataFields().remove(NUMBER);
			}
		}

		if ((null != contentParameters.getContentFields()) && ((null == filter) || filter.getFilter().getTarget().equals(TestItem.class))) {
			if (contentParameters.getContentFields().contains(NUMBER)) {
				contentParameters.getContentFields().remove(NUMBER);
			}
			if (contentParameters.getContentFields().contains(USER)) {
				contentParameters.getContentFields().remove(USER);
			}
		}
	}

	private void validateContentParameters(ContentParameters contentParameters, UserFilter filter, GadgetTypes gadget) {
		Class<?> filterTarget;
		if (WidgetUtils.withoutFilter.containsKey(gadget)) {
			filterTarget = withoutFilter.get(gadget);
		} else {
			filterTarget = filter.getFilter().getTarget();
		}
		CriteriaMap<?> criteriaMap = criteriaMapFactory.getCriteriaMap(filterTarget);
		if (null != contentParameters.getContentFields()) {
			validateFields(contentParameters.getContentFields(), criteriaMap, BAD_SAVE_WIDGET_REQUEST);
		}
		if (null != contentParameters.getMetadataFields()) {
			validateFields(contentParameters.getMetadataFields(), criteriaMap, BAD_SAVE_WIDGET_REQUEST);
		}
	}
}