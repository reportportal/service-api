package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.ta.reportportal.entity.integration.Integration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class BugTrackingSystemBuilderTest {

	@Test
	public void setParamsTest() {
		final String btsProject = "project";
		final String url = "http:/test.com";
		final String authKey = "authKey";
		final String authType = "authType";
		final String password = "password";
		final String userName = "userName";

		final Integration integration = new BugTrackingSystemBuilder().addBugTrackingProject(btsProject)
				.addUrl(url)
				.addAuthKey(authKey)
				.addAuthType(authType)
				.addPassword(password)
				.addUsername(userName)
				.get();

		assertEquals(btsProject, BtsConstants.PROJECT.getParam(integration.getParams(), String.class).get());
		assertEquals(url, BtsConstants.URL.getParam(integration.getParams(), String.class).get());
		assertEquals(authKey, BtsConstants.OAUTH_ACCESS_KEY.getParam(integration.getParams(), String.class).get());
		assertEquals(authType, BtsConstants.AUTH_TYPE.getParam(integration.getParams(), String.class).get());
		assertEquals(password, BtsConstants.PASSWORD.getParam(integration.getParams(), String.class).get());
		assertEquals(userName, BtsConstants.USER_NAME.getParam(integration.getParams(), String.class).get());
	}
}