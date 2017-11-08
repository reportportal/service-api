/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.database.dao;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.StatisticsDocumentHandler;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.CriteriaMap;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.database.search.Filter;
import com.google.common.collect.Lists;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Ignore
// TODO FIX REQUIRED!!!
public class LoadWithLineGraphCallbackTest extends BaseTest {

	@Autowired
	private LaunchRepository launchRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	private static CriteriaMap<?> criteriaMap;

	@BeforeClass
	public static void prepareFilterCriteria() throws IOException, ClassNotFoundException {
		CriteriaMapFactory mp = new CriteriaMapFactory("com.epam.ta.reportportal.database.entity");
		criteriaMap = mp.getCriteriaMap(Launch.class);
	}

	@Test
	public void testNull() {
		launchRepository.loadWithCallback(null, null, 0, null, null, null);
	}

	@Test
	public void testMismatchFieldsSet() {
		Filter filter = new Filter(Launch.class, Condition.CONTAINS, false, "launch", "name");
		Sort sort = new Sort(Direction.ASC, "name");

		String totalField = criteriaMap.getCriteriaHolder("statistics$executions$total").getQueryCriteria();

		StatisticsDocumentHandler callback = new StatisticsDocumentHandler(Lists.newArrayList(totalField), Lists.newArrayList(""));
		launchRepository.loadWithCallback(filter, sort, 5, new ArrayList<>(), callback, "launch");
		Assert.assertNotNull(callback.getResult());
		Assert.assertFalse(callback.getResult().isEmpty());
	}

	@Test
	public void testUnloadedFields() {
		Filter filter = new Filter(Launch.class, Condition.CONTAINS, false, "launch", "name");
		Sort sort = new Sort(Direction.ASC, "name");

		String totalField = criteriaMap.getCriteriaHolder("statistics$executions$total").getQueryCriteria();
		String failedField = criteriaMap.getCriteriaHolder("statistics$executions$failed").getQueryCriteria();

		StatisticsDocumentHandler callback = new StatisticsDocumentHandler(Lists.newArrayList(totalField), Lists.newArrayList(""));
		launchRepository.loadWithCallback(filter, sort, 5, Lists.newArrayList(failedField), callback, "launch");
		Assert.assertNotNull(callback.getResult());
		Assert.assertFalse(callback.getResult().isEmpty());
	}

	@Test
	public void testEmptyFieldsSet() {
		Filter filter = new Filter(Launch.class, Condition.CONTAINS, false, "launch", "name");
		Sort sort = new Sort(Direction.ASC, "name");

		StatisticsDocumentHandler callback = new StatisticsDocumentHandler(new ArrayList<>(), Lists.newArrayList(""));
		launchRepository.loadWithCallback(filter, sort, 5, new ArrayList<>(), callback, "launch");
		Assert.assertNotNull(callback.getResult());
		Assert.assertTrue(callback.getResult().isEmpty());
	}

	@Test
	@Ignore
	// TODO After complete widget refactoring!
	public void testloadWithCallback() {
		Filter filter = new Filter(Launch.class, Condition.CONTAINS, false, "launch", "name");
		Sort sort = new Sort(Direction.DESC, "name");

		String totalField = criteriaMap.getCriteriaHolder("statistics$executions$total").getQueryCriteria();
		String failedField = criteriaMap.getCriteriaHolder("statistics$executions$failed").getQueryCriteria();
		String bugsField = criteriaMap.getCriteriaHolder("statistics$defects$product_bugs").getQueryCriteria();

		String nameField = criteriaMap.getCriteriaHolder("name").getQueryCriteria();

		List<String> chartFields = Lists.newArrayList(totalField, failedField, bugsField);
		// statistics added because MongoDB can't load fields like
		// statistics.executions.failed
		List<String> allFields = Lists.newArrayList(totalField, failedField, nameField, "statistics");

		// StatisticsDocumentHandler callback = new
		// StatisticsDocumentHandler(chartFields,
		// Lists.newArrayList(nameField));
		// launchRepository.loadWithCallback(filter, sort, 5, allFields,
		// callback, "launch");
		// List<ChartObject> result = callback.getResult();
		//
		// Map<String, List<AxisObject>> total = result.get(totalField);
		// Map<String, List<AxisObject>> failed = result.get(failedField);
		// Map<String, List<AxisObject>> bugs = result.get(bugsField);
		// Assert.assertNotNull(bugs);
		// Assert.assertNotNull(total);
		// Assert.assertNotNull(failed);
		// List<AxisObject> bugsXaxis =
		// bugs.get(StatisticsDocumentHandler.X_AXIS_DOTS);
		// List<AxisObject> bugsYaxis =
		// bugs.get(StatisticsDocumentHandler.Y_AXIS_DOTS);
		//
		// List<AxisObject> totalXaxis =
		// total.get(StatisticsDocumentHandler.X_AXIS_DOTS);
		// List<AxisObject> totalYaxis =
		// total.get(StatisticsDocumentHandler.Y_AXIS_DOTS);
		//
		// List<AxisObject> failedXaxis =
		// failed.get(StatisticsDocumentHandler.X_AXIS_DOTS);
		// List<AxisObject> failedYaxis =
		// failed.get(StatisticsDocumentHandler.Y_AXIS_DOTS);
		//
		// Assert.assertNotNull(bugsXaxis);
		// Assert.assertNotNull(bugsYaxis);
		// Assert.assertNotNull(totalXaxis);
		// Assert.assertNotNull(totalYaxis);
		// Assert.assertNotNull(failedXaxis);
		// Assert.assertNotNull(failedYaxis);
		//
		// List<String> bugzz = Lists.newArrayList("0", "0", "0");
		// for (AxisObject ax : bugsYaxis) {
		// Assert.assertEquals(ax.getValue(), bugzz.get(bugsYaxis.indexOf(ax)));
		// }
		//
		// List<String> totalzz = Lists.newArrayList("4", "4", "0");
		// for (AxisObject ax2 : totalYaxis) {
		// Assert.assertEquals(ax2.getValue(),
		// totalzz.get(totalYaxis.indexOf(ax2)));
		// }
		//
		// List<String> failedzz = Lists.newArrayList("4", "1", "0");
		// for (AxisObject ax3 : failedYaxis) {
		// Assert.assertEquals(ax3.getValue(),
		// failedzz.get(failedYaxis.indexOf(ax3)));
		// }
		//
		// List<String> bugsXaxisNames =
		// Lists.newArrayList("launch for call back validation",
		// "Demo launch_launch1-stat",
		// "Demo launch name_sxbOa2");
		// for (AxisObject axXname : bugsXaxis) {
		// Assert.assertEquals(axXname.getName(),
		// bugsXaxisNames.get(bugsXaxis.indexOf(axXname)));
		// }
		//
		// Assert.assertEquals(bugsXaxis, totalXaxis);
		// Assert.assertEquals(bugsXaxis, failedXaxis);
		// Assert.assertEquals(totalXaxis, failedXaxis);
	}
}