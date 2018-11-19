package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterDeletedEvent;
import com.epam.ta.reportportal.core.filter.GetUserFilterHandler;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.ws.converter.converters.UserFilterConverter.TO_ACTIVITY_RESOURCE;

@Service
public class DeleteUserFilterHandlerImpl implements IDeleteUserFilterHandler {

	private final UserFilterRepository userFilterRepository;
	private final MessageBus messageBus;
	private final GetUserFilterHandler getFilterHandler;

	@Autowired
	public DeleteUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus,
			GetUserFilterHandler getFilterHandler) {
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
		this.getFilterHandler = getFilterHandler;
	}

	@Override
	public OperationCompletionRS deleteFilter(Long id, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		UserFilter userFilter = getFilterHandler.getFilter(id, projectDetails, user);
		userFilterRepository.delete(userFilter);
		messageBus.publishActivity(new FilterDeletedEvent(TO_ACTIVITY_RESOURCE.apply(userFilter), user.getUserId()));
		return new OperationCompletionRS("User filter with ID = '" + id + "' successfully deleted.");
	}
}
