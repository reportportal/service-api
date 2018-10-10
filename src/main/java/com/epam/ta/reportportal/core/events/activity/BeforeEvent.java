package com.epam.ta.reportportal.core.events.activity;

import com.google.common.base.Preconditions;

/**
 * Contains data BEFORE some event has fired
 *
 * @author Andrei Varabyeu
 */
public class BeforeEvent<T> {

	private T before;

	BeforeEvent() {
	}

	BeforeEvent(T before) {
		this.before = Preconditions.checkNotNull(before);
	}

	public T getBefore() {
		return before;
	}

	public void setBefore(T before) {
		this.before = before;
	}
}
