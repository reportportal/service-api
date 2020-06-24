package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableContent;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.SortEntry;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_PASSED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_TOTAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HealthCheckTableReadyContentResolverTest {

	private final WidgetContentRepository widgetContentRepository = mock(WidgetContentRepository.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final HealthCheckTableReadyContentResolver contentResolver = new HealthCheckTableReadyContentResolver(widgetContentRepository,
			objectMapper
	);

	@Test
	void getContentTest() {

		WidgetRQ widgetRQ = new WidgetRQ();
		widgetRQ.setName("name");

		widgetRQ.setWidgetType("componentHealthCheckTable");
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(new ArrayList<>());
		contentParameters.setItemsCount(600);

		Map<String, Object> options = new HashMap<>();

		contentParameters.setWidgetOptions(options);
		widgetRQ.setContentParameters(contentParameters);
		widgetRQ.setFilterIds(Collections.singletonList(1L));
		widgetRQ.setDescription("descr");

		SortEntry sortEntry = new SortEntry();
		sortEntry.setSortingColumn("passingRate");
		Widget widget = new WidgetBuilder().addWidgetRq(widgetRQ).addOption("viewName", "name").addOption("sort", sortEntry).get();

		HealthCheckTableContent content = new HealthCheckTableContent();
		content.setAttributeValue("v2");
		content.setPassingRate(50.00);
		HashMap<String, Integer> statistics = new HashMap<>();
		statistics.put(EXECUTIONS_PASSED, 5);
		statistics.put(EXECUTIONS_TOTAL, 10);
		content.setStatistics(statistics);

		when(widgetContentRepository.componentHealthCheckTable(any())).thenReturn(Lists.newArrayList(content));

		Map<String, Object> result = contentResolver.getContent(widget, Lists.newArrayList("k1", "k2"), Lists.newArrayList("v1"));

		List<HealthCheckTableContent> resultList = (List<HealthCheckTableContent>) result.get("result");

		HealthCheckTableContent tableContent = resultList.get(0);

		assertEquals(content.getPassingRate(), tableContent.getPassingRate());
		assertEquals(content.getAttributeValue(), tableContent.getAttributeValue());
	}

}