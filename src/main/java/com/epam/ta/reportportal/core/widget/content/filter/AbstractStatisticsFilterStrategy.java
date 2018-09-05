package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static java.util.Optional.ofNullable;

public abstract class AbstractStatisticsFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<String, ?> buildFilterAndLoadContent(LoadContentStrategy loadContentStrategy, ReportPortalUser.ProjectDetails projectDetails,
			Widget widget) {
		Set<UserFilter> filters = widget.getFilters();

		validateFilters(filters, widget.getId());

		Map<Filter, Sort> filterSortMap = buildFilterSortMap(filters, projectDetails.getProjectId());

		return loadContentStrategy.loadContent(widget.getContentFields(), filterSortMap, widget.getWidgetOptions(), widget.getItemsCount());
	}

	private void validateFilters(Set<UserFilter> filters, Long widgetId) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(filters), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "No filters for widget with id = " + widgetId + " were found.");
	}

	private Map<Filter, Sort> buildFilterSortMap(Set<UserFilter> filters, Long projectId) {
		Map<Filter, Sort> filterSortMap = Maps.newLinkedHashMap();

		filters.forEach(f -> {
			Filter filter = updateWithDefaultConditions(new Filter(f.getId(),
					f.getTargetClass(),
					Sets.newLinkedHashSet(f.getFilterCondition())
			), projectId);

			Optional<Set<FilterSort>> filterSorts = ofNullable(f.getFilterSorts());

			Sort sort = Sort.by(filterSorts.map(fs -> fs.stream()
					.map(s -> new Sort.Order(s.getDirection(), s.getField()))
					.collect(Collectors.toList())).orElseGet(Collections::emptyList));

			filterSortMap.put(filter, sort);
		});

		return filterSortMap;
	}

	protected abstract Filter updateWithDefaultConditions(Filter filter, Long projectId);
}
