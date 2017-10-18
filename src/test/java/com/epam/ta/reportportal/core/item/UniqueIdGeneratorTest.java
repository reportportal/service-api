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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.Parameter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Pavel_Bortnik
 */
@SpringFixture("triggerTests")
public class UniqueIdGeneratorTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private UniqueIdGenerator identifierGenerator;

	private static final String ITEM = "44524cc1553de753b3e5ab2f";

	@Test
	public void generateUniqueId() {
		TestItem item = testItemRepository.findOne(ITEM);
		String s1 = identifierGenerator.generate(item);
		String s2 = identifierGenerator.generate(item);
		Assert.assertEquals(s1, s2);
		item.setParameters(getParameters());
		String s3 = identifierGenerator.generate(item);
		Assert.assertNotEquals(s1, s3);

		item.setName("Different");
		item.setParameters(null);
		String s4 = identifierGenerator.generate(item);
		Assert.assertNotEquals(s3, s4);
	}

	@Test
	public void validate() {
		TestItem item = testItemRepository.findOne(ITEM);
		String s1 = identifierGenerator.generate(item);
		Assert.assertTrue(identifierGenerator.validate(s1));
	}

	private List<Parameter> getParameters() {
		Parameter parameters = new Parameter();
		parameters.setKey("CardNumber");
		parameters.setValue("4444333322221111");
		Parameter parameters1 = new Parameter();
		parameters1.setKey("Stars");
		parameters1.setValue("2 stars");
		return ImmutableList.<Parameter>builder().add(parameters).add(parameters1).build();
	}
}