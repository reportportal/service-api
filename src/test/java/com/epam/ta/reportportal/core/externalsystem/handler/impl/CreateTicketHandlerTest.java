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

package com.epam.ta.reportportal.core.externalsystem.handler.impl;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * @author Pavel Bortnik
 */
public class CreateTicketHandlerTest {

	@InjectMocks
	public CreateTicketHandler createTicketHandler;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void injectMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInvalidRq() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setIsIncludeLogs(true);
		postTicketRQ.setBackLinks(null);
		exception.expect(ReportPortalException.class);
		exception.expectMessage(
				"Impossible post ticket to external system. Test item id should be specified, when logs required in ticket description.");
		createTicketHandler.createIssue(postTicketRQ, "project", "id", "user");
	}

}