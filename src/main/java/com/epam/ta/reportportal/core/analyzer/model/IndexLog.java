/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents log container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 */
public class IndexLog {

	@JsonProperty("log_id")
	private String logId;

	@JsonProperty("logLevel")
	private int logLevel;

	@JsonProperty("message")
	private String message;

	public IndexLog() {
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexLog indexLog = (IndexLog) o;
		return logLevel == indexLog.logLevel && Objects.equals(message, indexLog.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(logLevel, message);
	}

	@Override
	public String toString() {
		return "IndexLog{" + "logLevel=" + logLevel + ", message='" + message + '\'' + '}';
	}
}
