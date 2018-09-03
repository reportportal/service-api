package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractStatisticsFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<String, ?> buildFilterAndLoadContent(LoadContentStrategy loadContentStrategy, ReportPortalUser.ProjectDetails projectDetails,
			Widget widget) {
		Set<UserFilter> filters = widget.getFilters();

		Sort sort = buildSort(filters);

		Set<Filter> queryFilters = buildQueryFilters(filters);

		queryFilters = updateWithDefaultConditions(queryFilters, projectDetails.getProjectId());

		return loadContentStrategy.loadContent(widget.getContentFields(),
				queryFilters,
				sort,
				widget.getWidgetOptions(),
				widget.getItemsCount()
		);
	}

	private Sort buildSort(Set<UserFilter> filters) {
		return Sort.by(filters.stream()
				.flatMap(fs -> fs.getFilterSorts().stream().map(s -> new Sort.Order(s.getDirection(), s.getField())))
				.collect(Collectors.toList()));
	}

	private Set<Filter> buildQueryFilters(Set<UserFilter> filters) {
		return filters.stream()
				.map(fs -> new Filter(fs.getTargetClass(), Sets.newHashSet(fs.getFilterCondition())))
				.collect(Collectors.toSet());
	}

	protected abstract Set<Filter> updateWithDefaultConditions(Set<Filter> filters, Long projectId);
}
