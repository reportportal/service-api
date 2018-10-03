package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.JsonbObject;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class ActivityDetails extends JsonbObject {
	private Map<String, HistoryField> history;

	public ActivityDetails() {
		history = new HashMap<>();
	}

	public ActivityDetails(Map<String, HistoryField> history) {
		this.history = new HashMap<>(history);
	}

	@JsonAnyGetter
	public Map<String, HistoryField> getHistory() {
		return ImmutableMap.copyOf(history);
	}

	@JsonAnySetter
	public void setHistory(Map<String, HistoryField> history) {
		this.history = new HashMap<>(history);
	}

	public void addHistoryField(String field, HistoryField historyField) {
		history.put(field, historyField);
	}

}
