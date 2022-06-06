package com.epam.ta.reportportal.core.project.settings.notification;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
public interface CreateProjectNotificationHandler {

	EntryCreatedRS createNotification(Project project, SenderCaseDTO createNotificationRQ, ReportPortalUser user);

}
