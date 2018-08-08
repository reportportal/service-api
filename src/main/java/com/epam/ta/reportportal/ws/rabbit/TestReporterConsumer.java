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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.core.configs.RabbitMqConfiguration;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

/**
 * @author Pavel Bortnik
 */
@Component
public class TestReporterConsumer {

	@Autowired
	private DatabaseUserDetailsService userDetailsService;

	@Autowired
	private StartTestItemHandler startTestItemHandler;

	@Autowired
	private FinishTestItemHandler finishTestItemHandler;

	@RabbitListener(queues = RabbitMqConfiguration.QUEUE_START_ITEM)
	public void startItem(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
			@Header(name = MessageHeaders.PARENT_ID, required = false) Long parentId, @Payload StartTestItemRQ rq) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, normalizeId(projectName));
		if (null != parentId && parentId > 0) {
			startTestItemHandler.startChildItem(user, projectDetails, rq, parentId);
		} else {
			startTestItemHandler.startRootItem(user, projectDetails, rq);
		}
	}

	@RabbitListener(queues = RabbitMqConfiguration.QUEUE_FINISH_ITEM)
	public void finishTestItem(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
			@Header(MessageHeaders.ITEM_ID) Long itemId, @Payload FinishTestItemRQ rq) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishTestItemHandler.finishTestItem(user, ProjectUtils.extractProjectDetails(user, normalizeId(projectName)), itemId, rq);
	}

}
