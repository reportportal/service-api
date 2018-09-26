package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserFilterHandlerImpl implements IUpdateUserFilterHandler {

	private final UserFilterRepository userFilterRepository;

	@Autowired
	public UpdateUserFilterHandlerImpl(UserFilterRepository userFilterRepository) {
		this.userFilterRepository = userFilterRepository;
	}

	@Override
	public OperationCompletionRS updateUserFilter(Long userFilterId, UpdateUserFilterRQ updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		return null;
	}
}
