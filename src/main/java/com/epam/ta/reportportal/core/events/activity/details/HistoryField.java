package com.epam.ta.reportportal.core.events.activity.details;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HistoryField {
	private String oldValue;
	private String newValue;

	public HistoryField(String oldValue, String newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public HistoryField() {
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@JsonIgnore
	public boolean isEmpty(){
		return null == oldValue || null == newValue;
	}
}
