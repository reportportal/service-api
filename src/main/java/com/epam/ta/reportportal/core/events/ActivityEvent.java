package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.entity.Activity;

import java.io.Serializable;

/**
 * @author Andrei Varabyeu
 */
public interface ActivityEvent extends Serializable {

	/**
	 * Converts Object to Activity to be persisted in DB
	 *
	 * @return Persistable representation of event
	 */
	Activity toActivity();
}
