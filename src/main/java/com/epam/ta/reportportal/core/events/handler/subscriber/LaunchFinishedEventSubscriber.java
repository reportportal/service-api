package com.epam.ta.reportportal.core.events.handler.subscriber;

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import org.springframework.core.Ordered;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LaunchFinishedEventSubscriber extends Ordered {

	void handleEvent(LaunchFinishedEvent launchFinishedEvent, Project project, Launch launch);
}
