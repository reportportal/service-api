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

package com.epam.ta.reportportal.core.launch.rerun;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.item.ItemCreatedRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

import java.util.Optional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public interface RerunHandler {

	/**
	 * Updates launch state and return existed launch to rerun
	 *
	 * @param request   Request data
	 * @param projectId Project ID
	 * @param user      ReportPortal user
	 * @return {@link Launch}
	 */
	Launch handleLaunch(StartLaunchRQ request, Long projectId, ReportPortalUser user);

	/**
	 * Finds root {@link TestItem} to rerun and creates retries
	 *
	 * @param request Request data
	 * @param launch  {@link Launch}
	 * @return {@link ItemCreatedRS} if item is rerun, otherwise {@link Optional#empty()}
	 */
	Optional<ItemCreatedRS> handleRootItem(StartTestItemRQ request, Launch launch);

	/**
	 * Finds child {@link TestItem} to rerun and creates retries
	 *
	 * @param request Request data
	 * @param launch  {@link Launch}
	 * @return {@link ItemCreatedRS} if item is rerun, otherwise {@link Optional#empty()}
	 */
	Optional<ItemCreatedRS> handleChildItem(StartTestItemRQ request, Launch launch, TestItem parent);
}
