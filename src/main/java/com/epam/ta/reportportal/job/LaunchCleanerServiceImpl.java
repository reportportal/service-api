package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchCleanerServiceImpl implements LaunchCleanerService {

	private final LaunchRepository launchRepository;

	private final ActivityRepository activityRepository;

	@Autowired
	public LaunchCleanerServiceImpl(LaunchRepository launchRepository, ActivityRepository activityRepository) {
		this.launchRepository = launchRepository;
		this.activityRepository = activityRepository;
	}

	@Override
	@Async
	@Transactional
	public void cleanOutdatedLaunches(Project project, Duration period, AtomicLong launchesRemoved) {
		activityRepository.deleteModifiedLaterAgo(project.getId(), period);
		launchesRemoved.addAndGet(launchRepository.deleteLaunchesByProjectIdModifiedBefore(project.getId(),
				TO_LOCAL_DATE_TIME.apply(Date.from(Instant.now().minusSeconds(period.getSeconds())))
		));
	}
}
