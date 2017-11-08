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

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.IOException;

/**
 * Unit tests for {@link Condition}
 *
 * @author Andrei Varabyeu
 */
public class ConditionTest {

	private static CriteriaMap<TestItem> criteriaMap;

	@BeforeClass
	public static void prepare() throws IOException, ClassNotFoundException {
		CriteriaMapFactory mp = new CriteriaMapFactory("com.epam.ta.reportportal.database.entity");
		criteriaMap = mp.getCriteriaMap(TestItem.class);
	}

	@Test(expected = ReportPortalException.class)
	public void checkNotEquals() {
		Filter filter = new Filter(TestItem.class, Condition.EQUALS, true, "toFind",
				criteriaMap.getCriteriaHolder("name").getFilterCriteria()
		);
		Criteria criteria = Criteria.where("name");
		Condition.EQUALS.addCondition(criteria, filter.getFilterConditions().iterator().next(), criteriaMap.getCriteriaHolder("name"));
	}

	@Test
	public void checkNotContains() {
		Filter filter = new Filter(TestItem.class, Condition.EQUALS, true, "toFind",
				criteriaMap.getCriteriaHolder("name").getFilterCriteria()
		);
		Criteria criteria = Criteria.where("name");
		Condition.CONTAINS.addCondition(criteria, filter.getFilterConditions().iterator().next(), criteriaMap.getCriteriaHolder("name"));
	}
}