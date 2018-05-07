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
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

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

	@RabbitListener(queues = RabbitMqConfiguration.START_ITEM_QUEUE)
	public void startRootItem(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
			@Payload StartTestItemRQ rq) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		startTestItemHandler.startRootItem(user, projectName, rq);
	}

	@RabbitListener(queues = RabbitMqConfiguration.START_CHILD_QUEUE)
	public void startChildItem(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
			@Header(MessageHeaders.ITEM_ID) Long parentId, @Payload StartTestItemRQ rq) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		startTestItemHandler.startChildItem(user, projectName, rq, parentId);
	}

	@RabbitListener(queues = RabbitMqConfiguration.FINISH_ITEM_QUEUE)
	public void finishTestItem(@Header(MessageHeaders.USERNAME) String username, @Header(MessageHeaders.PROJECT_NAME) String projectName,
			@Header(MessageHeaders.ITEM_ID) Long itemId, @Payload FinishTestItemRQ rq) {
		ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
		finishTestItemHandler.finishTestItem(user, projectName, itemId, rq);
	}

}
