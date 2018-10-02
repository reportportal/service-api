package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.events.handler.AddDemoProjectEventHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("demo")
public class FlushingDataJob implements Job {

	@Autowired
	private UserCreationBidRepository creationBidRepository;

	@Autowired
	private RestorePasswordBidRepository passwordBidRepository;

	@Autowired
	private ServerSettingsRepository serverSettingsRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserPreferenceRepository preferenceRepository;

	@Autowired
	private ILogIndexer logIndexer;

	@Autowired
	private AddDemoProjectEventHandler addDemoProjectEventHandler;

	@Autowired
	private DataStorage dataStorage;

	@Autowired
	private MongoOperations mongoOperations;

	private static final Logger LOGGER = LoggerFactory.getLogger(FlushingDataJob.class);

	@Override
	public void execute(JobExecutionContext context) {
		LOGGER.info("Start flushing all existing data!");

		mongoOperations.dropCollection("launchMetaInfo");
		mongoOperations.dropCollection("oauth_access_token");
		mongoOperations.dropCollection("oauth_refresh_token");
		mongoOperations.dropCollection("sessions");

		passwordBidRepository.deleteAll();
		creationBidRepository.deleteAll();

		List<String> allProjectNames = projectRepository.findAllProjectNames();
		projectRepository.delete(allProjectNames);
		allProjectNames.forEach(name -> logIndexer.deleteIndex(name));
		serverSettingsRepository.deleteAll();
		userRepository.deleteAll();
		preferenceRepository.deleteAll();
		dataStorage.deleteAll();
		addDemoProjectEventHandler.onApplicationEvent(null);
		LOGGER.info("Finish flushing all existing data!");
	}
}
