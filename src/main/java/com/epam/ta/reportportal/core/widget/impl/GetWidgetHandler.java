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

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.widget.IGetWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategyLatest;
import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.WidgetResourceAssembler;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.validateGadgetType;
import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.validateWidgetDataType;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link IGetWidgetHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetWidgetHandler implements IGetWidgetHandler {

	private WidgetRepository widgetRepository;

	private WidgetResourceAssembler resourceAssembler;

	private UserFilterRepository userFilterRepository;

	private Map<GadgetTypes, BuildFilterStrategy> buildFilterStrategy;

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Autowired
	public void setWidgetResourceAssembler(WidgetResourceAssembler widgetResourceAssembler) {
		this.resourceAssembler = widgetResourceAssembler;
	}

	@Autowired
	public void setUserFilterRepository(UserFilterRepository userFilterRepository) {
		this.userFilterRepository = userFilterRepository;
	}

	@Autowired
	@Qualifier("buildFilterStrategy")
	public void setBuildFilterStrategy(Map<GadgetTypes, BuildFilterStrategy> buildFilterStrategy) {
		this.buildFilterStrategy = buildFilterStrategy;
	}

	@Override
	public WidgetResource getWidget(String widgetId, String userName, String project) {
		Widget widget = widgetRepository.findOne(widgetId);
		expect(widget, notNull()).verify(WIDGET_NOT_FOUND, widgetId);
		expect(widget.getProjectName(), equalTo(project)).verify(ACCESS_DENIED);

		/*
		 * If resource was un-shared by owner then widget name & owner-name
		 * return only. Exceptions not proceed on UI so we could skip it here a
		 * while. In case of business rule: remove IF statement.
		 */
		WidgetResource widgetResource;
		if (!AclUtils.isPossibleToReadResource(widget.getAcl(), userName, project)) {
			Widget emptyModel = new Widget();
			emptyModel.setName(widget.getName());
			emptyModel.setOwner(widget.getAcl().getOwnerUserId());
			widgetResource = resourceAssembler.toResource(emptyModel);
		} else {
			widgetResource = resourceAssembler.toResource(widget);

			Optional<UserFilter> userFilter = findUserFilter(widget.getApplyingFilterId());
			if (isFilterUnShared(userName, project, userFilter)) {
				widgetResource.setContent(new HashMap<>());
			} else {
				widgetResource.setContent(loadContentByFilterType(userFilter, project, widget.getContentOptions()));
			}
		}
		return widgetResource;
	}

	@Override
	public Map<String, SharedEntity> getSharedWidgetNames(String userName, String projectName) {
		List<Widget> widgets = widgetRepository.findSharedEntities(userName, projectName,
				Arrays.asList(Widget.ID, Widget.NAME, Widget.OWNER), Shareable.NAME_OWNER_SORT
		);
		return toMap(widgets);
	}

	@Override
	public List<WidgetResource> getSharedWidgetsList(String userName, String projectName) {
		List<Widget> widgets = widgetRepository.findSharedEntities(userName, projectName,
				Arrays.asList(Widget.ID, Widget.NAME, "description", Widget.OWNER, Widget.GADGET_TYPE, Widget.CONTENT_FIELDS,
						Widget.ENTRIES
				), Shareable.NAME_OWNER_SORT
		);
		return resourceAssembler.toResources(widgets);
	}

	@Override
	public List<String> getWidgetNames(String projectName, String userName) {
		return widgetRepository.findByProjectAndUser(projectName, userName).stream().map(Widget::getName).collect(Collectors.toList());
	}

	@Override
	public Map<String, ?> getWidgetPreview(String projectName, String userName, WidgetPreviewRQ previewRQ) {
		validateWidgetDataType(previewRQ.getContentParameters().getType(), BAD_REQUEST_ERROR);
		validateGadgetType(previewRQ.getContentParameters().getGadget(), BAD_REQUEST_ERROR);

		Optional<UserFilter> userFilter = findUserFilter(previewRQ.getFilterId());
		if (isFilterUnShared(userName, projectName, userFilter)) {
			return Collections.emptyMap();
		} else {
			ContentOptions contentOptions = new WidgetBuilder().addContentParameters(previewRQ.getContentParameters())
					.build()
					.getContentOptions();
			return loadContentByFilterType(userFilter, projectName, contentOptions);
		}
	}

	/**
	 * Transform list of widgets to map of Shared entities result map:<br>
	 * <li>key - widget id
	 * <li>value - {@link SharedEntity}
	 *
	 * @param widgets
	 * @return
	 */
	private Map<String, SharedEntity> toMap(List<Widget> widgets) {
		Map<String, SharedEntity> result = new LinkedHashMap<>();
		for (Widget widget : widgets) {
			SharedEntity sharedEntity = new SharedEntity();
			sharedEntity.setName(widget.getName());
			if (null != widget.getAcl()) {
				sharedEntity.setOwner(widget.getAcl().getOwnerUserId());
			}
			result.put(widget.getId(), sharedEntity);
		}
		return result;
	}

	private boolean isRequireUserFilter(GadgetTypes gadgetType, Optional<UserFilter> userFilter) {
		return !(!userFilter.isPresent() && (gadgetType != GadgetTypes.ACTIVITY) && (gadgetType != GadgetTypes.MOST_FAILED_TEST_CASES) && (
				gadgetType != GadgetTypes.PASSING_RATE_PER_LAUNCH));
	}

	private boolean isFilterUnShared(String userName, String project, Optional<UserFilter> userFilter) {
		return userFilter.isPresent() && !AclUtils.isPossibleToReadResource(userFilter.get().getAcl(), userName, project);
	}

	/**
	 * Load widget content according filter type.
	 *
	 * @param userFilter
	 * @param projectName
	 * @param contentOptions
	 * @return
	 */
	Map<String, ?> loadContentByFilterType(Optional<UserFilter> userFilter, String projectName, ContentOptions contentOptions) {
		// Log doesn't have any statistics, so currently unable to create any
		// widget with valid content for log
		Map<String, ?> content;
		if (userFilter.isPresent() && Log.class.equals(userFilter.get().getFilter().getTarget())) {
			content = new HashMap<>();
		} else {
			BuildFilterStrategy filterStrategy = buildFilterStrategy.get(GadgetTypes.findByName(contentOptions.getGadgetType()).get());
			expect(filterStrategy, notNull()).verify(UNABLE_LOAD_WIDGET_CONTENT,
					Suppliers.formattedSupplier("Unknown gadget type: '{}'.", contentOptions.getGadgetType())
			);

			if (contentOptions.getWidgetOptions() != null && contentOptions.getWidgetOptions().containsKey("latest")) {
				content = ((BuildFilterStrategyLatest) filterStrategy).loadContentOfLatest(
						userFilter.orElse(null), contentOptions, projectName);
			} else {
				content = filterStrategy.buildFilterAndLoadContent(userFilter.orElse(null), contentOptions, projectName);
			}
		}
		return content;
	}

	/**
	 * Get userFilter by id, id can be null.
	 *
	 * @param filterId
	 * @return
	 */
	private Optional<UserFilter> findUserFilter(String filterId) {
		return Optional.ofNullable(filterId == null ? null : userFilterRepository.findOne(filterId));
	}
}
