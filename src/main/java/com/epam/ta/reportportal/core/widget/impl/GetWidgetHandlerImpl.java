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

package com.epam.ta.reportportal.core.widget.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.MaterializedLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_OWNER;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTES;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetWidgetHandlerImpl implements GetWidgetHandler {

	private Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping;

	private Map<WidgetType, LoadContentStrategy> loadContentStrategy;

	private Map<WidgetType, MultilevelLoadContentStrategy> multilevelLoadContentStrategy;

	private MaterializedLoadContentStrategy materializedLoadContentStrategy;

	private Set<WidgetType> unfilteredWidgetTypes;

	@Autowired
	private GetShareableEntityHandler<Widget> getShareableEntityHandler;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private GetUserFilterHandler getUserFilterHandler;

	@Autowired
	@Qualifier("buildFilterStrategy")
	public void setBuildFilterStrategy(Map<WidgetType, BuildFilterStrategy> buildFilterStrategyMapping) {
		this.buildFilterStrategyMapping = buildFilterStrategyMapping;
	}

	@Autowired
	@Qualifier("contentLoader")
	public void setLoadContentStrategy(Map<WidgetType, LoadContentStrategy> loadContentStrategy) {
		this.loadContentStrategy = loadContentStrategy;
	}

	@Autowired
	@Qualifier("multilevelContentLoader")
	public void setMultilevelLoadContentStrategy(Map<WidgetType, MultilevelLoadContentStrategy> multilevelLoadContentStrategy) {
		this.multilevelLoadContentStrategy = multilevelLoadContentStrategy;
	}

	@Autowired
	public void setMaterializedLoadContentStrategy(MaterializedLoadContentStrategy materializedLoadContentStrategy) {
		this.materializedLoadContentStrategy = materializedLoadContentStrategy;
	}

	@Autowired
	@Qualifier("unfilteredWidgetTypes")
	public void setUnfilteredWidgetTypes(Set<WidgetType> unfilteredWidgetTypes) {
		this.unfilteredWidgetTypes = unfilteredWidgetTypes;
	}

	@Override
	public WidgetResource getWidget(Long widgetId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Widget widget = getShareableEntityHandler.getPermitted(widgetId, projectDetails);

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
				));

		expect(widgetType.isSupportMultilevelStructure(), Predicate.isEqual(false)).verify(ErrorType.INCORRECT_REQUEST,
				formattedSupplier("Unsupported widget type '{}'", widgetType)
		);

		Map<String, ?> content;

		if (unfilteredWidgetTypes.contains(widgetType) || isFilteredContentLoadAllowed(widget.getFilters(), projectDetails, user)) {
			content = loadContentStrategy.get(widgetType)
					.loadContent(Lists.newArrayList(widget.getContentFields()),
							buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
							widget.getWidgetOptions(),
							widget.getItemsCount()
					);
		} else {
			content = Collections.emptyMap();
		}

		WidgetResource resource = WidgetConverter.TO_WIDGET_RESOURCE.apply(widget);
		resource.setContent(content);
		return resource;
	}

	@Override
	public WidgetResource getWidget(Long widgetId, String[] attributes, MultiValueMap<String, String> params,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Widget widget = getShareableEntityHandler.getPermitted(widgetId, projectDetails);

		WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
				));

		expect(widgetType.isSupportMultilevelStructure(), Predicate.isEqual(true)).verify(ErrorType.INCORRECT_REQUEST,
				formattedSupplier("Widget type '{}' does not support multilevel structure.", widgetType)
		);
		Map<String, ?> content;

		if (unfilteredWidgetTypes.contains(widgetType) || isFilteredContentLoadAllowed(widget.getFilters(), projectDetails, user)) {
			params.put(ATTRIBUTES, Lists.newArrayList(attributes));
			content = ofNullable(multilevelLoadContentStrategy.get(widgetType)).map(strategy -> strategy.loadContent(Lists.newArrayList(
					widget.getContentFields()),
					buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
					widget.getWidgetOptions(),
					attributes,
					params,
					widget.getItemsCount()
			)).orElseGet(() -> materializedLoadContentStrategy.loadContent(widget, params));

		} else {
			content = Collections.emptyMap();
		}

		WidgetResource resource = WidgetConverter.TO_WIDGET_RESOURCE.apply(widget);
		resource.setContent(content);
		return resource;
	}

	private Boolean isFilteredContentLoadAllowed(Collection<UserFilter> userFilters, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		if (CollectionUtils.isEmpty(userFilters)) {
			return false;
		}

		Long[] ids = userFilters.stream().map(UserFilter::getId).toArray(Long[]::new);
		List<UserFilter> permittedFilters = getPermittedFilters(ids, projectDetails, user);
		return userFilters.size() == permittedFilters.size();

	}

	@Override
	public Map<String, ?> getWidgetPreview(WidgetPreviewRQ previewRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {

		WidgetType widgetType = WidgetType.findByName(previewRQ.getWidgetType())
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_REQUEST,
						formattedSupplier("Unsupported widget type '{}'", previewRQ.getWidgetType())
				));

		List<UserFilter> userFilter = null;
		if (CollectionUtils.isNotEmpty(previewRQ.getFilterIds())) {
			userFilter = getPermittedFilters(previewRQ.getFilterIds().toArray(Long[]::new), projectDetails, user);
		}

		if (!unfilteredWidgetTypes.contains(widgetType) && CollectionUtils.isEmpty(userFilter)) {
			return Collections.emptyMap();
		}

		Widget widget = new WidgetBuilder().addWidgetPreviewRq(previewRQ)
				.addProject(projectDetails.getProjectId())
				.addFilters(userFilter)
				.get();

		if (widgetType.isSupportMultilevelStructure()) {
			return multilevelLoadContentStrategy.get(widgetType)
					.loadContent(Lists.newArrayList(widget.getContentFields()),
							buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
							widget.getWidgetOptions(),
							null,
							null,
							widget.getItemsCount()
					);
		} else {
			return loadContentStrategy.get(widgetType)
					.loadContent(Lists.newArrayList(widget.getContentFields()),
							buildFilterStrategyMapping.get(widgetType).buildFilter(widget),
							widget.getWidgetOptions(),
							widget.getItemsCount()
					);
		}
	}

	List<UserFilter> getPermittedFilters(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		return getUserFilterHandler.getFiltersById(ids, projectDetails, user);
	}

	@Override
	public Iterable<Object> getOwnNames(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user) {
		Page<Widget> own = widgetRepository.getOwn(ProjectFilter.of(filter, projectDetails.getProjectId()), pageable, user.getUsername());
		return PagedResourcesAssembler.pageConverter().apply(own.map(Widget::getName));
	}

	@Override
	public Iterable<WidgetResource> getShared(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user) {
		Page<Widget> shared = widgetRepository.getShared(ProjectFilter.of(filter, projectDetails.getProjectId()),
				pageable,
				user.getUsername()
		);
		return PagedResourcesAssembler.pageConverter(WidgetConverter.TO_WIDGET_RESOURCE).apply(shared);
	}

	@Override
	public Iterable<WidgetResource> searchShared(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user, String term) {
		Filter termFilter = Filter.builder()
				.withTarget(Widget.class)
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_NAME))
				.withCondition(new FilterCondition(Operator.OR, Condition.CONTAINS, false, term, CRITERIA_OWNER))
				.build();
		Page<Widget> shared = widgetRepository.getShared(ProjectFilter.of(new CompositeFilter(Operator.AND, filter, termFilter),
				projectDetails.getProjectId()
		), pageable, user.getUsername());
		return PagedResourcesAssembler.pageConverter(WidgetConverter.TO_WIDGET_RESOURCE).apply(shared);
	}
}
