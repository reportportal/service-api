/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static java.util.Collections.*;

/**
 * @author Pavel Bortnik
 */
@SpringFixture("widgets")
public class CumulativeContentLoaderTest extends BaseTest {

	@Autowired
	private CumulativeContentLoader contentLoader;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test(expected = ReportPortalException.class)
	public void testNullOrWithoutTagPrefix() {
		Filter filter = Filter.builder()
				.withTarget(Launch.class)
				.withCondition(new FilterCondition(Condition.CONTAINS, false, "name", "name"))
				.build();
		Map<String, List<ChartObject>> content = contentLoader.loadContent(
				"project", filter, null, 10, emptyList(), emptyList(), emptyMap());
		Assert.fail("Error in handled Request. Please, check specified parameters: 'widgetOptions'");
	}

	@Test(expected = ReportPortalException.class)
	public void testWithoutTagPrefix() {
		Filter filter = Filter.builder()
				.withTarget(Launch.class)
				.withCondition(new FilterCondition(Condition.CONTAINS, false, "name", "name"))
				.build();
		Map<String, List<ChartObject>> content = contentLoader.loadContent(
				"project", filter, null, 10, emptyList(), emptyList(), singletonMap("prefix", emptyList()));
		Assert.fail("Error in handled Request. Please, check specified parameters: 'prefix'");
	}

	@Test
	@Ignore(value = "Fongo doesn't support $addFields operation")
	public void testCumulative() {
		Filter filter = Filter.builder()
				.withTarget(Launch.class)
				.withCondition(new FilterCondition(Condition.CONTAINS, false, "l", "name"))
				.build();
		ImmutableList<String> contentField = ImmutableList.<String>builder().add("statistics.executionCounter.failed")
				.add("statistics.executionCounter.passed")
				.add("statistics.executionCounter.skipped")
				.add("statistics.issueCounter.productBug.total")
				.build();
		List<String> metadata = emptyList();
		ImmutableMap<String, List<String>> options = ImmutableMap.<String, List<String>>builder().put("prefix", singletonList("job"))
				.build();
		Map<String, List<ChartObject>> results = contentLoader.loadContent("project2", filter, null, 10, contentField, metadata, options);
		List<ChartObject> result = results.get("result");
		Assert.assertEquals("Got incorrect results", 2, result.size());

		ChartObject jobTwo = result.get(0);
		ChartObject jobOne = result.get(1);

		Assert.assertEquals("Sorting is not correct", "job:2", jobTwo.getId());
		Assert.assertEquals("1", jobTwo.getValues().get("statistics$executionCounter$failed"));
		Assert.assertEquals("3", jobTwo.getValues().get("statistics$executionCounter$passed"));
		Assert.assertEquals("0", jobTwo.getValues().get("statistics$executionCounter$skipped"));

		Assert.assertEquals("Sorting is not correct", "job:1", jobOne.getId());
		Assert.assertEquals("5", jobOne.getValues().get("statistics$executionCounter$failed"));
		Assert.assertEquals("3", jobOne.getValues().get("statistics$executionCounter$passed"));
		Assert.assertEquals("0", jobOne.getValues().get("statistics$executionCounter$skipped"));
		Assert.assertEquals("3", jobOne.getValues().get("statistics$issueCounter$productBug$total"));
	}

}