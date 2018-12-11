package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class DemoDataLaunchService {

	private final StartLaunchHandler startLaunchHandler;

	private final FinishLaunchHandler finishLaunchHandler;

	@Autowired
	public DemoDataLaunchService(StartLaunchHandler startLaunchHandler, FinishLaunchHandler finishLaunchHandler) {
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
	}

	@Transactional
	public Long startLaunch(String name, int i, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setMode(Mode.DEFAULT);
		rq.setDescription(ContentUtils.getLaunchDescription());
		rq.setName(name);
		rq.setStartTime(new Date());
		Set<ItemAttributeResource> tags = Sets.newHashSet(
				new ItemAttributeResource("platform", "desktop"),
				new ItemAttributeResource(null, "demo"),
				new ItemAttributeResource("build", "3.0.1." + i)
		);
		rq.setAttributes(tags);

		return startLaunchHandler.startLaunch(user, projectDetails, rq).getId();
	}

	@Transactional
	public void finishLaunch(Long launchId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishExecutionRQ rq = new FinishExecutionRQ();
		rq.setEndTime(new Date());

		finishLaunchHandler.finishLaunch(launchId, rq, projectDetails, user);
	}
}
