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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.core.log.impl.CreateLogHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pavel Bortnik
 */

@Component
@Transactional
public class LogReporterConsumer {

	private DatabaseUserDetailsService userDetailsService;

	private CreateLogHandler createLogHandler;

	public void onLogCreate(@Payload SaveLogRQ rq, @Header(MessageHeaders.USERNAME) String username,
			@Header(MessageHeaders.PROJECT_NAME) String projectName) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		createLogHandler.createLog(rq, null, ProjectUtils.extractProjectDetails(user, projectName));
	}

}
