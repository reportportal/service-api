package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class MostFailedFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<String, ?> buildFilterAndLoadContent(LoadContentStrategy loadContentStrategy, ReportPortalUser.ProjectDetails projectDetails,
			Widget widget) {
		UserFilter userFilter = widget.getFilter();
		Filter filter = new Filter(userFilter.getTargetClass(), Sets.newHashSet(userFilter.getFilterCondition()));
		filter = updateWithDefaultConditions(filter, projectDetails.getProjectId());
		return loadContentStrategy.loadContent(widget.getContentFields(), filter, widget.getWidgetOptions(), widget.getItemsCount());
	}

	private Filter updateWithDefaultConditions(Filter filter, Long projectId) {
		Set<FilterCondition> defaultConditions = Sets.newHashSet(
				new FilterCondition(Condition.EQUALS, false, String.valueOf(projectId), "project_id"),
				new FilterCondition(Condition.NOT_EQUALS, false, StatusEnum.IN_PROGRESS.name(), "status"),
				new FilterCondition(Condition.EQUALS, false, Mode.DEFAULT.toString(), "mode")
		);
		filter.withConditions(defaultConditions);
		return filter;
	}
}
