package com.epam.ta.reportportal.core.file;

import com.epam.ta.reportportal.auth.ReportPortalUser;

import java.io.InputStream;

/**
 * @author Ivan Budaev
 */
public interface GetFileHandler {

	InputStream getUserPhoto(ReportPortalUser loggedInUser);

	InputStream getUserPhoto(String username, ReportPortalUser loggedInUser);
}
