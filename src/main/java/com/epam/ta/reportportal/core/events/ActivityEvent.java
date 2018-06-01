package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.store.database.entity.Activity;

/**
 * @author Andrei Varabyeu
 */
public interface ActivityEvent {

	/**
	 * Converts Object to Activity to be persisted in DB
	 *
	 * @return Persistable representation of event
	 */
	Activity toActivity();
}
