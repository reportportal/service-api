package com.epam.ta.reportportal.events;

import com.google.common.base.Preconditions;

/**
 * Contains data BEFORE some event has fired
 *
 * @author Andrei Varabyeu
 */
public class BeforeEvent<T> {

	private final T before;

	public BeforeEvent(T before) {
		this.before = Preconditions.checkNotNull(before);
	}

	public T getBefore() {
		return before;
	}

}
