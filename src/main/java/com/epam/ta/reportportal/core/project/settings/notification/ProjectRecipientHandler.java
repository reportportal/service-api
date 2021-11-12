package com.epam.ta.reportportal.core.project.settings.notification;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.entity.user.User;

/**
 * Interface to work with project {@link SenderCase#getRecipients()}
 *
 * @author Ivan Budaev
 */
public interface ProjectRecipientHandler {

	void handle(Iterable<User> users, Project project);
}
