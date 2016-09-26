package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.auth.AuthConstants.ADMINISTRATOR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DemoDataControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private LaunchRepository launchRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private UserFilterRepository userFilterRepository;

	@Test
	public void generateDemoData() throws Exception {
		DemoDataRq rq = new DemoDataRq();
		rq.setCreateDashboard(true);
		rq.setLaunchesQuantity(1);
		mvcMock.perform(post("/demo/project1").content(objectMapper.writeValueAsBytes(rq)).contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));
		List<Launch> byName = launchRepository.findByName(rq.getLaunchName());
		Assert.assertFalse(byName.isEmpty());
		Assert.assertEquals(rq.getLaunchesQuantity(), byName.size());
		Dashboard dashboard = dashboardRepository.findOneByUserProject(AuthConstants.TEST_USER, "project1", rq.getDashboardName());
		Assert.assertNotNull(dashboard);
		Assert.assertEquals(9, dashboardRepository.findOne(dashboard.getId()).getWidgets().size());
		final UserFilter filter = userFilterRepository.findOneByName(AuthConstants.TEST_USER, rq.getFilterName(), "project1");
		Assert.assertNotNull(filter);

	}

	@Override
	protected Authentication authentication() {
		return ADMINISTRATOR;
	}
}