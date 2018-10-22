package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterUpdatedEvent;
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
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpdateUserFilterHandlerImpl implements IUpdateUserFilterHandler {

	private final UserFilterRepository userFilterRepository;

	private final MessageBus messageBus;

	@Autowired
	public UpdateUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus) {
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateUserFilter(Long userFilterId, UpdateUserFilterRQ updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		UserFilter userFilter = userFilterRepository.findById(userFilterId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, userFilterId));
		UserFilter before = SerializationUtils.clone(userFilter);
		UserFilter updated = new UserFilterBuilder(userFilter).addUpdateFilterRQ(updateRQ).get();
		messageBus.publishActivity(new FilterUpdatedEvent(before, updated, user.getUserId()));
		return new OperationCompletionRS("User filter with ID = '" + updated.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateUserFilter(CollectionsRQ<BulkUpdateFilterRQ> updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
