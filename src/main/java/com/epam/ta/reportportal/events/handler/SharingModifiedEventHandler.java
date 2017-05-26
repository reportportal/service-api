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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.events.SharingModifiedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Andrei Varabyeu
 */
@Component
public class SharingModifiedEventHandler {

	private final UserPreferenceRepository userPreferenceRepository;

	@Autowired
	public SharingModifiedEventHandler(UserPreferenceRepository userPreferenceRepository) {
		this.userPreferenceRepository = userPreferenceRepository;
	}

	@EventListener
	public void onFilterSharingModified(SharingModifiedEvent event) {
		//@formatter:off
		if (!event.isShare()) {
			event.getItems().stream()
					.filter(item -> UserFilter.class.equals(item.getClass()))
					.map(item -> (UserFilter) item)
					.forEach( userFilter -> userPreferenceRepository
							.deleteUnsharedFilters(event.getUser(), event.getProject(), userFilter.getId()));
		//@formatter:on
		}
	}
}
