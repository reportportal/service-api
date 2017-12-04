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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Pavel Bortnik
 */
public class IssueConverterTest {

	@Test
	public void testConvertNull() {
		Issue apply = IssueConverter.TO_MODEL.apply(null);
		assertNull(apply);
	}

	@Test
	public void testConvert() {
		TestItemIssue testItemIssue = testItemIssue();
		Issue converted = IssueConverter.TO_MODEL.apply(testItemIssue);
		assertEquals(testItemIssue.getIssueType(), converted.getIssueType());
		assertEquals(testItemIssue.getIssueDescription(), converted.getComment());
		assertEquals(testItemIssue.isIgnoreAnalyzer(), converted.getIgnoreAnalyzer());
		assertEquals(testItemIssue.getExternalSystemIssues().size(), converted.getExternalSystemIssues().size());
		TestItemIssue.ExternalSystemIssue es = testItemIssue.getExternalSystemIssues().iterator().next();
		Issue.ExternalSystemIssue convertedEs = converted.getExternalSystemIssues().iterator().next();
		assertEquals(es.getTicketId(), convertedEs.getTicketId());
		assertEquals(es.getUrl(), convertedEs.getUrl());
		assertEquals(es.getExternalSystemId(), convertedEs.getExternalSystemId());
		assertEquals(es.getTicketId(), convertedEs.getTicketId());
		assertEquals(es.getTicketId(), convertedEs.getTicketId());


	}

	private TestItemIssue testItemIssue() {
		TestItemIssue testItemIssue = new TestItemIssue();
		testItemIssue.setIgnoreAnalyzer(true);
		testItemIssue.setAutoAnalyzed(true);
		testItemIssue.setIssueDescription("comment");
		testItemIssue.setIssueType("PB001");
		TestItemIssue.ExternalSystemIssue externalSystemIssue = new TestItemIssue.ExternalSystemIssue();
		externalSystemIssue.setExternalSystemId("id");
		externalSystemIssue.setSubmitDate(1510828124L);
		externalSystemIssue.setSubmitter("me");
		externalSystemIssue.setTicketId("ticket");
		externalSystemIssue.setUrl("url");
		testItemIssue.setExternalSystemIssues(ImmutableSet.<TestItemIssue.ExternalSystemIssue>builder().add(externalSystemIssue).build());
		return testItemIssue;
	}

}