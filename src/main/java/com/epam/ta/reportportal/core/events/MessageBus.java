package com.epam.ta.reportportal.core.events;

/**
 * MessageBus is an abstraction for dealing with events over external event-streaming system
 */
public interface MessageBus {

	/**
	 * Sends event by the given route
	 *
	 * @param route Route
	 * @param o     Payload
	 */
	void send(String route, Object o);

	/**
	 * Sends event to special broadcasting exchange
	 *
	 * @param o Payload
	 */
	void broadcast(Object o);

}
