package com.epam.ta.reportportal.core.configs.event.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LoggingEventErrorHandler implements ErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventErrorHandler.class);

	@Override
	public void handleError(Throwable throwable) {
		LOGGER.error("Error during event publishing", throwable);
	}
}
