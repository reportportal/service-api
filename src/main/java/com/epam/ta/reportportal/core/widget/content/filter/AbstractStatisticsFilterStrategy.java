package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * @author Ivan Budaev
 */
public abstract class AbstractStatisticsFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<String, ?> buildFilterAndLoadContent(LoadContentStrategy loadContentStrategy, ReportPortalUser.ProjectDetails projectDetails,
			Widget widget) {
		Map<Filter, Sort> filterSortMap = buildFilterSortMap(widget, projectDetails.getProjectId());
		return loadContentStrategy.loadContent(
				Lists.newArrayList(widget.getContentFields()),
				filterSortMap,
				widget.getWidgetOptions(),
				widget.getItemsCount()
		);
	}

	protected Map<Filter, Sort> buildFilterSortMap(Widget widget, Long projectId) {
		Map<Filter, Sort> filterSortMap = Maps.newLinkedHashMap();
		Set<UserFilter> filters = Optional.ofNullable(widget.getFilters()).orElse(Collections.emptySet());
		Filter defaultFilter = buildDefaultFilter(widget, projectId);
		Optional.ofNullable(defaultFilter).ifPresent(f -> filterSortMap.put(defaultFilter, Sort.unsorted()));

		filters.forEach(f -> {
			Filter filter = new Filter(f.getId(), f.getTargetClass(), Sets.newHashSet(f.getFilterCondition()));
			Optional<Set<FilterSort>> filterSorts = ofNullable(f.getFilterSorts());
			Sort sort = Sort.by(filterSorts.map(fs -> fs.stream()
					.map(s -> new Sort.Order(s.getDirection(), s.getField()))
					.collect(Collectors.toList())).orElseGet(Collections::emptyList));
			filterSortMap.put(filter, sort);
		});

		return filterSortMap;
	}

	protected abstract Filter buildDefaultFilter(Widget widget, Long projectId);
}
