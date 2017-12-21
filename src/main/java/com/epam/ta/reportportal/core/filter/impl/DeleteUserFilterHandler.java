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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.acl.AclUtils;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.events.FilterDeletedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IDeleteUserFilterHandler}
 *
 * @author Aliaksei_Makayed
 */

@Service
public class DeleteUserFilterHandler implements IDeleteUserFilterHandler {

	private final UserFilterRepository filterRepository;

	private final ProjectRepository projectRepository;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public DeleteUserFilterHandler(UserFilterRepository filterRepository, ProjectRepository projectRepository,
			ApplicationEventPublisher eventPublisher) {
		this.filterRepository = filterRepository;
		this.projectRepository = projectRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public OperationCompletionRS deleteFilter(String filterId, String userName, String projectName, UserRole userRole) {

		UserFilter userFilter = filterRepository.findOne(filterId);
		BusinessRule.expect(userFilter, Predicates.notNull()).verify(ErrorType.USER_FILTER_NOT_FOUND, filterId, userName);
		AclUtils.isAllowedToEdit(userFilter.getAcl(), userName, projectRepository.findProjectRoles(userName), userFilter.getName(),
				userRole
		);
		BusinessRule.expect(userFilter.getProjectName(), Predicates.equalTo(projectName)).verify(ErrorType.ACCESS_DENIED);

		try {
			filterRepository.delete(filterId);
			eventPublisher.publishEvent(new FilterDeletedEvent(userFilter, userName));

		} catch (Exception e) {
			throw new ReportPortalException("Error during deleting complex filter item", e);
		}
		OperationCompletionRS response = new OperationCompletionRS();
		StringBuilder msg = new StringBuilder("User filter with ID = '");
		msg.append(filterId);
		msg.append("' successfully deleted.");
		response.setResultMessage(msg.toString());

		return response;
	}
}
