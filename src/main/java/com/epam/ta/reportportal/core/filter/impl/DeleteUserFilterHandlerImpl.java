package com.epam.ta.reportportal.core.filter.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.FilterDeletedEvent;
import com.epam.ta.reportportal.core.filter.IDeleteUserFilterHandler;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserFilterHandlerImpl implements IDeleteUserFilterHandler {

	private final UserFilterRepository userFilterRepository;
	private final MessageBus messageBus;

	@Autowired
	public DeleteUserFilterHandlerImpl(UserFilterRepository userFilterRepository, MessageBus messageBus) {
		this.userFilterRepository = userFilterRepository;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS deleteFilter(Long id, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		UserFilter filter = userFilterRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_FILTER_NOT_FOUND, id));
		userFilterRepository.delete(filter);
		messageBus.publishActivity(new FilterDeletedEvent(filter, user.getUserId()));
		return new OperationCompletionRS("User filter with ID = '" + id + "' successfully deleted.");
	}
}
