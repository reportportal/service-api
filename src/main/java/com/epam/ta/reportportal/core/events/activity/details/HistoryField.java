package com.epam.ta.reportportal.core.events.activity.details;

public class HistoryField<T> {
	private T oldValue;
	private T newValue;

	public HistoryField(T oldValue, T newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public T getOldValue() {
		return oldValue;
	}

	public void setOldValue(T oldValue) {
		this.oldValue = oldValue;
	}

	public T getNewValue() {
		return newValue;
	}

	public void setNewValue(T newValue) {
		this.newValue = newValue;
	}
}
