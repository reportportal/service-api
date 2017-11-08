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

package com.epam.ta.reportportal.database.search;

import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Positive tests for filter conditions and different data types
 *
 * @author Andrei Varabyeu
 */
@RunWith(Parameterized.class)
public class ConditionParameterizedTest {

	private static CriteriaMap<TestItem> criteriaMap;

	private static final String TEST_QUERY_FIELD = "test_field";

	private Condition condition;

	private String toFind;

	private String fieldName;

	/**
	 * Parameters list
	 *
	 * @param fieldName
	 * @param condition
	 * @param toFind
	 */
	public ConditionParameterizedTest(String fieldName, Condition condition, String toFind) {
		this.condition = condition;
		this.toFind = toFind;
		this.fieldName = fieldName;
	}

	@BeforeClass
	public static void prepareFilterCriteria() throws IOException, ClassNotFoundException {
		CriteriaMapFactory mp = new CriteriaMapFactory("com.epam.ta.reportportal.database.entity");
		criteriaMap = mp.getCriteriaMap(TestItem.class);
	}

	@Test
	public void checkEquals() {
		Filter filter = new Filter(TestItem.class, condition, false, toFind, criteriaMap.getCriteriaHolder(fieldName).getFilterCriteria());
		Criteria criteria = Criteria.where(TEST_QUERY_FIELD);
		condition.addCondition(criteria, filter.getFilterConditions().iterator().next(), criteriaMap.getCriteriaHolder(fieldName));
	}

	@Parameterized.Parameters(name = "{index}:{0},{1},{2}")
	public static List<Object[]> getParameters() {
		return Arrays.asList(new Object[][] {
		/*
		 * String
		 */
				{ "name", Condition.EQUALS, "test value" }, { "name", Condition.CONTAINS, "test value" },
				{ "name", Condition.EXISTS, "true" },

		/*
		 * Date
		 */
				{ "start_time", Condition.EXISTS, currentDate() }, { "start_time", Condition.EQUALS, currentDate() },
				{ "start_time", Condition.GREATER_THAN, currentDate() }, { "start_time", Condition.GREATER_THAN_OR_EQUALS, currentDate() },
				{ "start_time", Condition.BETWEEN, currentDate() + "," + currentDate() },
				/*
				 * Boolean
				 */
				{ "has_childs", Condition.EQUALS, "true" }, { "has_childs", Condition.EXISTS, "true" },

				/*
				 * Enumeration
				 */
				{ "status", Condition.EQUALS, Status.PASSED.toString() }, { "status", Condition.EXISTS, Status.PASSED.toString() },

				/*
				 * Numbers
				 */
				{ "statistics$executions$total", Condition.EQUALS, "1" }, { "statistics$executions$total", Condition.EXISTS, "1" },
				{ "statistics$executions$total", Condition.GREATER_THAN, "1" },
				{ "statistics$executions$total", Condition.GREATER_THAN_OR_EQUALS, "1" },
				{ "statistics$executions$total", Condition.LOWER_THAN, "1" },
				{ "statistics$executions$total", Condition.LOWER_THAN_OR_EQUALS, "1" },
				{ "statistics$executions$total", Condition.BETWEEN, "1,1" } });

	}

	private static String currentDate() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
}