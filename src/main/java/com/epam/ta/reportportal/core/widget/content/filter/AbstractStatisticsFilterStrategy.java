package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.collect.Maps;
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

		Map<Filter, Sort> filterSortMap = buildFilterSortMap(filters, projectDetails.getProjectId());

		return loadContentStrategy.loadContent(widget.getContentFields(), filterSortMap, widget.getWidgetOptions(), widget.getItemsCount());
	}

	private Map<Filter, Sort> buildFilterSortMap(Set<UserFilter> filters, Long projectId) {
		Map<Filter, Sort> filterSortMap = Maps.newLinkedHashMap();
		//		Sort.by(filters.stream()
		//				.flatMap(fs -> fs.getFilterSorts().stream().map(s -> new Sort.Order(s.getDirection(), s.getField())))
		//				.collect(Collectors.toList()));
		filters.forEach(fs -> {
			Filter filter = updateWithDefaultConditions(new Filter(fs.getId(),
					fs.getTargetClass(),
					Sets.newLinkedHashSet(fs.getFilterCondition())
			), projectId);
			Sort sort = Sort.by(fs.getFilterSorts()
					.stream()
					.map(s -> new Sort.Order(s.getDirection(), s.getField()))
					.collect(Collectors.toList()));
			filterSortMap.put(filter, sort);
		});

		return filterSortMap;
	}

	protected abstract Filter updateWithDefaultConditions(Filter filter, Long projectId);
}
