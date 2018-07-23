package com.epam.ta.reportportal.core.widget.content.history;

import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public abstract class LastLaunchFilterStrategy implements BuildFilterStrategy {

	private LaunchRepository launchRepository;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	public Optional<Launch> getLastLaunch(ContentOptions contentOptions, String projectName) {
		/*
		 * Return false response for absent filtering launch name parameter
		 */
		if (contentOptions.getWidgetOptions() == null || contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD) == null) {
			return Optional.empty();
		}
		return launchRepository.findLastLaunch(projectName,
				contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD).get(0),
				Mode.DEFAULT.name()
		);
	}

}
