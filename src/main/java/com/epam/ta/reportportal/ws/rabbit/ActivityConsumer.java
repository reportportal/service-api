package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.dao.ActivityRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Andrei Varabyeu
 */
@Component
@Transactional
public class ActivityConsumer {

	private final ActivityRepository activityRepository;

	@Autowired
	public ActivityConsumer(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@RabbitListener(queues = "#{ @activityQueue.name }")
	public void onEvent(@Payload ActivityEvent rq) {
		Optional.ofNullable(rq.toActivity()).ifPresent(activityRepository::save);
	}
}
