package com.epam.ta.reportportal.events;

import com.google.common.base.Preconditions;

/**
 * @author Andrei Varabyeu
 */
public class AroundEvent<T> extends BeforeEvent<T> {

	private final T after;

	public AroundEvent(T before, T after) {
		super(before);
		this.after = Preconditions.checkNotNull(after);
	}

	public T getAfter() {
		return after;
	}

}
