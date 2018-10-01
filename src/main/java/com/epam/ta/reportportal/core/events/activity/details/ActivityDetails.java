package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityDetails extends JsonbObject {
	private Map<String, HistoryField> history;

	public ActivityDetails() {
		history = new HashMap<>();
	}

	public ActivityDetails(Map<String, HistoryField> history) {
		this.history = history;
	}

	public void addHistoryField(String field, HistoryField historyField) {
		history.put(field, historyField);
	}

	public boolean isEmpty() {
		return history.isEmpty();
	}
}
