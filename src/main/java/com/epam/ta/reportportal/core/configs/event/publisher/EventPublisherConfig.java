package com.epam.ta.reportportal.core.configs.event.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.ErrorHandler;

import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class EventPublisherConfig {

	private final ErrorHandler loggingEventErrorHandler;

	@Autowired
	public EventPublisherConfig(ErrorHandler loggingEventErrorHandler) {
		this.loggingEventErrorHandler = loggingEventErrorHandler;
	}

	@Bean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
	public ApplicationEventMulticaster applicationEventMulticaster() {
		final SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
		eventMulticaster.setErrorHandler(loggingEventErrorHandler);
		return eventMulticaster;
	}

}
