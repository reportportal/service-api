package com.epam.ta.reportportal.core.events;

import java.util.concurrent.ExecutionException;

/**
 * MessageBus is an abstraction for dealing with events over external event-streaming system
 *
 * @author Andrei Varabyeu
 */
public interface MessageBus {

	void publish(String exchange, String route, Object o) throws ExecutionException, InterruptedException;

	/**
	 * Sends event by the given route
	 *
	 * @param route Route
	 * @param o     Payload
	 */
	void publish(String route, Object o);

	/**
	 * Sends event to special broadcasting exchange
	 *
	 * @param o Payload
	 */
	void broadcastEvent(Object o);

	/**
	 * Sends activity
	 *
	 * @param o Payload
	 */
	void publishActivity(ActivityEvent o);

}
