package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service("productStatusFilterStrategy")
public class ProductStatusFilterStrategy extends AbstractStatisticsFilterStrategy {

	@Override
	protected Map<Filter, Sort> buildFilterSortMap(Widget widget, Long projectId) {
		Map<Filter, Sort> filterSortMap = Maps.newLinkedHashMap();
		Optional.ofNullable(widget.getFilters()).orElse(Collections.emptySet()).forEach(f -> {
			Filter filter = GROUP_FILTERS.apply(Sets.newHashSet(
					new Filter(f.getId(), f.getTargetClass().getClassObject(), Sets.newLinkedHashSet(f.getFilterCondition())),
					buildDefaultFilter(widget, projectId)
			));
			Optional<Set<FilterSort>> filterSorts = ofNullable(f.getFilterSorts());

			Sort sort = Sort.by(filterSorts.map(fs -> fs.stream()
					.map(s -> new Sort.Order(s.getDirection(), s.getField()))
					.collect(Collectors.toList())).orElseGet(Collections::emptyList));

			filterSortMap.put(filter, sort);
		});
		return filterSortMap;
	}

	protected Filter buildDefaultFilter(Widget widget, Long projectId) {
		return new Filter(Launch.class,
				Sets.newHashSet(new FilterCondition(Condition.EQUALS, false, String.valueOf(projectId), CRITERIA_PROJECT_ID))
		);
	}
}
