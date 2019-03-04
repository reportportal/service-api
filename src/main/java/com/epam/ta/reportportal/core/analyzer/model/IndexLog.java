/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	@JsonProperty("logId")
	private Long logId;

	@JsonProperty("logLevel")
	private int logLevel;

	@JsonProperty("message")
	private String message;

	public IndexLog() {
	}

	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
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
