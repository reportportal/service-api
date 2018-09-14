package com.epam.ta.reportportal.core.imprt.impl;

import com.epam.ta.reportportal.commons.EntityUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class ParseResults {

	private LocalDateTime startTime;

	private long duration;

	ParseResults() {
		startTime = LocalDateTime.now();
	}

	public ParseResults(LocalDateTime startTime, long duration) {
		this.startTime = startTime;
		this.duration = duration;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public long getDuration() {
		return duration;
	}

	void checkAndSetStartLaunchTime(LocalDateTime startSuiteTime) {
		if (this.startTime.isAfter(startSuiteTime)) {
			this.startTime = startSuiteTime;
		}
	}

	void increaseDuration(long duration) {
		this.duration += duration;
	}

	public Date getEndTime() {
		return EntityUtils.TO_DATE.apply(startTime.plus(duration, ChronoUnit.MILLIS));
	}
}
