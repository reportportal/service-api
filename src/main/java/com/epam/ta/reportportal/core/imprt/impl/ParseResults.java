/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
