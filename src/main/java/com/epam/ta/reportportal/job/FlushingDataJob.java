package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

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
	private ApplicationContext applicationContext;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private DataStorage dataStorage;

	@Autowired
	private MongoOperations mongoOperations;

	private static final Logger LOGGER = LoggerFactory.getLogger(FlushingDataJob.class);

	@Override
	public void execute(JobExecutionContext context) {
		LOGGER.info("Start flushing all existing data!");
		mongoOperations.dropCollection("launchMetaInfo");
		passwordBidRepository.deleteAll();
		creationBidRepository.deleteAll();
		serverSettingsRepository.deleteAll();
		projectRepository.delete(projectRepository.findAllProjectNames());
		userRepository.deleteAll();
		dataStorage.deleteAll();
		eventPublisher.publishEvent(new ContextRefreshedEvent(applicationContext));
		LOGGER.info("Finish flushing all existing data!");
	}
}
