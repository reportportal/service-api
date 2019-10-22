/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
