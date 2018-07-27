package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MostFailedFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<String, ?> buildFilterAndLoadContent(LoadContentStrategy loadContentStrategy, ReportPortalUser.ProjectDetails projectDetails,
			Widget widget) {
		return loadContentStrategy.loadContent(widget.getContentFields(), null, widget.getWidgetOptions(), widget.getItemsCount());
	}
}
