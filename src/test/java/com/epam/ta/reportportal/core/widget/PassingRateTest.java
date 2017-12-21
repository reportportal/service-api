package com.epam.ta.reportportal.core.widget;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.core.widget.content.GadgetTypes;
import com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader;
import com.epam.ta.reportportal.core.widget.content.WidgetDataTypes;
import com.epam.ta.reportportal.core.widget.impl.CreateWidgetHandler;
import com.epam.ta.reportportal.core.widget.impl.GetWidgetHandler;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader.*;

@SpringFixture("widgets")
public class PassingRateTest extends BaseTest {
	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private CreateWidgetHandler createWidgetHandler;

	@Autowired
	private GetWidgetHandler getWidgetHandler;

	private static final String FILTER_ID = "566e1f3818177ca334439d38";

	@Test
	public void testPassingRatePerLaunch() {
		EntryCreatedRS widget = createWidgetHandler.createWidget(widgetRQ(), "project2", "user1");
		WidgetResource widgetResource = getWidgetHandler.getWidget(widget.getId(), "user1", "project2");
		Map<String, List<ChartObject>> content = (Map<String, List<ChartObject>>) widgetResource.getContent();
		ChartObject chartObject = content.get(RESULT).get(0);
		Assert.assertEquals(GadgetTypes.PASSING_RATE_PER_LAUNCH.getType(), widgetResource.getContentParameters().getGadget());
		Assert.assertEquals(WidgetDataTypes.BAR_CHART.getType(), widgetResource.getContentParameters().getType());
		Assert.assertTrue(chartObject.getValues().size() == 2);
		Assert.assertEquals(chartObject.getValues().get(TOTAL_FIELD), String.valueOf(4));
		Assert.assertEquals(chartObject.getValues().get(PASSED_FIELD), String.valueOf(3));
	}

	@Test
	public void testPassingRateSummary() {
		EntryCreatedRS widget = createWidgetHandler.createWidget(widgetRQSummary(), "project2", "user1");
		WidgetResource widgetResource = getWidgetHandler.getWidget(widget.getId(), "user1", "project2");
		Map<String, List<ChartObject>> content = (Map<String, List<ChartObject>>) widgetResource.getContent();
		ChartObject chartObject = content.get(RESULT).get(0);
		StatisticBasedContentLoader loader = new StatisticBasedContentLoader();
		Assert.assertEquals(GadgetTypes.PASSING_RATE_SUMMARY.getType(), widgetResource.getContentParameters().getGadget());
		Assert.assertEquals(WidgetDataTypes.BAR_CHART.getType(), widgetResource.getContentParameters().getType());
		Assert.assertTrue(chartObject.getValues().size() == 2);
		Assert.assertEquals(chartObject.getValues().get("statistics$executions$total"), String.valueOf(8));
		Assert.assertEquals(chartObject.getValues().get("statistics$executions$passed"), String.valueOf(6));
	}

	private WidgetRQ widgetRQ() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.PASSING_RATE_PER_LAUNCH.getType());
		contentParameters.setType(WidgetDataTypes.BAR_CHART.getType());
		contentParameters.setWidgetOptions(
				ImmutableMap.<String, List<String>>builder().put("launchNameFilter", Collections.singletonList("Demo launch_launch1-stat"))
						.build());
		WidgetRQ rq = new WidgetRQ();
		rq.setFilterId("");
		rq.setName("widget");
		rq.setShare(false);
		rq.setContentParameters(contentParameters);
		return rq;
	}

	private WidgetRQ widgetRQSummary() {
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget(GadgetTypes.PASSING_RATE_SUMMARY.getType());
		contentParameters.setType(WidgetDataTypes.BAR_CHART.getType());
		contentParameters.setContentFields(
				ImmutableList.<String>builder().add("statistics$executions$total").add("statistics$executions$passed").build());
		contentParameters.setItemsCount(5);
		WidgetRQ rq = new WidgetRQ();
		rq.setFilterId(FILTER_ID);
		rq.setName("widget");
		rq.setShare(false);
		rq.setContentParameters(contentParameters);
		return rq;
	}
}
