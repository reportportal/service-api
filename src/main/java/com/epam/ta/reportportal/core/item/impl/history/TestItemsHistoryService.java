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

package com.epam.ta.reportportal.core.item.impl.history;

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * History loading service for regular test items (is_root property was set to
 * false).
 *
 * @author Aliaksei_Makayed
 */
@Service
public class TestItemsHistoryService {

	//	@Autowired
	//	private LaunchRepository launchRepository;
	//
	private final ProjectRepository projectRepository;

	@Autowired
	public TestItemsHistoryService(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	//
	//	@Autowired
	//	private TestItemResourceAssembler itemResourceAssembler;

	public List<Launch> loadLaunches(int quantity, Long startingLaunchId, String projectName, boolean showBrokenLaunches) {
		//		Launch startingLaunch = launchRepository.findNameNumberAndModeById(startingLaunchId);
		//		if (startingLaunch == null) {
		//			return Collections.emptyList();
		//		}
		//		if (startingLaunch.getMode() == DEBUG) {
		//			return Collections.singletonList(startingLaunch);
		//		}
		//		Filter filter = HistoryUtils.getLaunchSelectionFilter(startingLaunch.getName(), projectName, startingLaunch.getNumber().toString(),
		//				showBrokenLaunches
		//		);
		//		return launchRepository.findIdsByFilter(filter, new Sort(DESC, "number"), quantity);
		throw new UnsupportedOperationException("No implementation");
	}

}
