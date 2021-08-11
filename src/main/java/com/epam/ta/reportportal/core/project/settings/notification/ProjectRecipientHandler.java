package com.epam.ta.reportportal.core.project.settings.notification;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;

public interface ProjectRecipientHandler {

	void excludeProjectRecipients(Iterable<User> users, Project project);
}
