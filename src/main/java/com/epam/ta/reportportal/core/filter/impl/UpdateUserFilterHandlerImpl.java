package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.UserFilterBuilder;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
		UserFilter userFilter = userFilterRepository.findById(userFilterId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, userFilterId));
		UserFilter updated = new UserFilterBuilder(userFilter).addUpdateFilterRQ(updateRQ).get();
		return new OperationCompletionRS("User filter with ID = '" + updated.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateUserFilter(CollectionsRQ<BulkUpdateFilterRQ> updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
