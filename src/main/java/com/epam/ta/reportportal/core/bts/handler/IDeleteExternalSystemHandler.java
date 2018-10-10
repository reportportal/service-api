/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.bts.handler;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.impl.DeleteExternalSystemHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Interface for {@link DeleteExternalSystemHandler} external system handler
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface IDeleteExternalSystemHandler {

	/**
	 * Delete method for external system entity
	 *
	 * @param projectName      Project Name
	 * @param externalSystemId External System to be deleted
	 * @param user             User
	 * @return Operation result
	 */
	OperationCompletionRS deleteExternalSystem(String projectName, Long externalSystemId, ReportPortalUser user);

	/**
	 * Delete all external system assigned to specified Report Portal project
	 *
	 * @param projectName Project Name
	 * @param user        User
	 * @return Operation Result
	 */
	OperationCompletionRS deleteAllExternalSystems(String projectName, ReportPortalUser user);

}