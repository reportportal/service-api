package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.core.configs.SchedulerConfiguration;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CleanLaunchesJobTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private LogCleanerService logCleanerService;

	@Mock
	private LaunchCleanerService launchCleanerService;

	@Mock
	private SchedulerConfiguration.CleanLaunchesJobProperties cleanLaunchesJobProperties;

	@InjectMocks
	private CleanLaunchesJob cleanLaunchesJob;

	@Test
	void executeTest() throws JobExecutionException {
		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.keepLaunches");
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("1 month");
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));

		project.setName(name);

		when(projectRepository.findAllIdsAndProjectAttributes(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));
		when(cleanLaunchesJobProperties.getTimeout()).thenReturn(100);

		cleanLaunchesJob.execute(null);

		verify(logCleanerService, times(1)).removeProjectAttachments(any(), any(), any(), any());
		verify(launchCleanerService, times(1)).cleanOutdatedLaunches(any(), any(), any());
	}

	@Test
	void wrongAttributeValue() throws JobExecutionException {
		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.keepLaunches");
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("wrong");
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));

		project.setName(name);

		when(projectRepository.findAllIdsAndProjectAttributes(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));
		when(cleanLaunchesJobProperties.getTimeout()).thenReturn(100);

		cleanLaunchesJob.execute(null);
	}
}