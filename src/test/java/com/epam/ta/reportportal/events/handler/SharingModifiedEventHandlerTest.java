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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.events.SharingModifiedEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Andrei Varabyeu
 */
public class SharingModifiedEventHandlerTest {

	private SharingModifiedEventHandler sharingModifiedEventHandler;
	private UserPreferenceRepository userPreferenceRepository;

	@Before
	public void before() {
		userPreferenceRepository = mock(UserPreferenceRepository.class);
		sharingModifiedEventHandler = new SharingModifiedEventHandler(userPreferenceRepository);
	}

	@Test
	public void checkFiltersDelete() {

		UserFilter filter = new UserFilter();
		filter.setId("filterId");

		SharingModifiedEvent event = new SharingModifiedEvent(Collections.singletonList(filter), "user", "project", false);
		sharingModifiedEventHandler.onFilterSharingModified(event);
		verify(userPreferenceRepository, times(1)).deleteUnsharedFilters(eq("user"), eq("project"), eq("filterId"));
	}

	@Test
	public void checkNoDeleteOnShare() {
		SharingModifiedEvent event = new SharingModifiedEvent(Collections.singletonList(new UserFilter()), "user", "project", true);
		sharingModifiedEventHandler.onFilterSharingModified(event);
		verify(userPreferenceRepository, times(0)).deleteUnsharedFilters(anyString(), anyString(), any());
	}

}