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
import com.epam.ta.reportportal.exception.ReportPortalException;
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
 * Negative tests for filter conditions and different data types
 *
 * @author Andrei Varabyeu
 */
@RunWith(Parameterized.class)
public class ConditionNegativeTest {
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
	public ConditionNegativeTest(String fieldName, Condition condition, String toFind) {
		this.condition = condition;
		this.toFind = toFind;
		this.fieldName = fieldName;
	}

	@BeforeClass
	public static void prepareFilterCriteria() throws IOException, ClassNotFoundException {
		CriteriaMapFactory mp = new CriteriaMapFactory("com.epam.ta.reportportal.database.entity");
		criteriaMap = mp.getCriteriaMap(TestItem.class);
	}

	@Test(expected = ReportPortalException.class)
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
				{ "name", Condition.GREATER_THAN, "test value" }, { "name", Condition.GREATER_THAN_OR_EQUALS, "test value" },
				{ "name", Condition.LOWER_THAN, "test value" }, { "name", Condition.LOWER_THAN_OR_EQUALS, "test value" },
				{ "name", Condition.BETWEEN, "test value" }, { "name", Condition.BETWEEN, "test,value" },

				/*
				 * Date
				 */
				{ "start_time", Condition.CONTAINS, currentDate() }, { "start_time", Condition.EQUALS, "not date" },

				/*
				 * Boolean
				 */
				{ "has_childs", Condition.GREATER_THAN, "true" }, { "has_childs", Condition.LOWER_THAN, "true" },

				/*
				 * Enumeration
				 */
				{ "status", Condition.CONTAINS, Status.PASSED.toString() }, { "status", Condition.LOWER_THAN, Status.PASSED.toString() },
				{ "status", Condition.LOWER_THAN_OR_EQUALS, Status.PASSED.toString() },
				{ "status", Condition.GREATER_THAN, Status.PASSED.toString() },
				{ "status", Condition.GREATER_THAN_OR_EQUALS, Status.PASSED.toString() },

				/*
				 * Numbers
				 */
				{ "statistics$executions$total", Condition.CONTAINS, "1" },
				{ "statistics$executions$total", Condition.EQUALS, "notnumber" },
				
				/*
				 * Collections
				 */
				{ "name", Condition.SIZE, "not_collection" }, { "name", Condition.HAS, "not_collection" } });

	}

	private static String currentDate() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}
}