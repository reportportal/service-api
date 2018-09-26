package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserFilterHandlerImpl implements IDeleteUserFilterHandler {

	private final UserFilterRepository userFilterRepository;

	public DeleteUserFilterHandlerImpl(UserFilterRepository userFilterRepository) {
		this.userFilterRepository = userFilterRepository;
	}

	@Override
	public OperationCompletionRS deleteFilter(Long id, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		return null;
	}
}
