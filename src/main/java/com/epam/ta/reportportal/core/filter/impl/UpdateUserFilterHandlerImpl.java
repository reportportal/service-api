package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterUpdatedEvent;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IUpdateUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.converter.builders.UserFilterBuilder;
import com.epam.ta.reportportal.ws.model.CollectionsRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.UserFilterActivityResource;
import com.epam.ta.reportportal.ws.model.filter.BulkUpdateFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;

@Service
public class UpdateUserFilterHandlerImpl implements IUpdateUserFilterHandler {

	private final UserFilterRepository userFilterRepository;
	private final GetUserFilterHandler getFilterHandler;

	private final MessageBus messageBus;

	@Autowired
	public UpdateUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus,
			GetUserFilterHandler getFilterHandler) {
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
		this.getFilterHandler = getFilterHandler;
	}

	@Override
	public OperationCompletionRS updateUserFilter(Long userFilterId, UpdateUserFilterRQ updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		UserFilter userFilter = getFilterHandler.getFilter(userFilterId, projectDetails, user);
		UserFilterActivityResource before = TO_ACTIVITY_RESOURCE.apply(userFilter);
		UserFilter updated = new UserFilterBuilder(userFilter).addUpdateFilterRQ(updateRQ).get();
		messageBus.publishActivity(new FilterUpdatedEvent(before, TO_ACTIVITY_RESOURCE.apply(updated), user.getUserId()));
		return new OperationCompletionRS("User filter with ID = '" + updated.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateUserFilter(CollectionsRQ<BulkUpdateFilterRQ> updateRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
