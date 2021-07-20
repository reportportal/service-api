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

package com.epam.ta.reportportal.demodata.model;

import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DemoItemMetadata {

	private String name;

	private String parentId;

	private boolean retry;

	private boolean nested;

	private TestItemTypeEnum type;

	private StatusEnum status;

	private String issue;

	private int logCount;

	public DemoItemMetadata withName(String name) {
		this.name = name;
		return this;
	}

	public DemoItemMetadata withParentId(String parentId) {
		this.parentId = parentId;
		return this;
	}

	public DemoItemMetadata withRetry(boolean retry) {
		this.retry = retry;
		return this;
	}

	public DemoItemMetadata withNested(boolean nested) {
		this.nested = nested;
		return this;
	}

	public DemoItemMetadata withType(TestItemTypeEnum type) {
		this.type = type;
		return this;
	}

	public DemoItemMetadata withStatus(StatusEnum status) {
		this.status = status;
		return this;
	}

	public DemoItemMetadata withIssue(String issue) {
		this.issue = issue;
		return this;
	}

	public DemoItemMetadata withLogCount(int logCount) {
		this.logCount = logCount;
		return this;
	}

	public String getName() {
		return name;
	}

	public String getParentId() {
		return parentId;
	}

	public boolean isRetry() {
		return retry;
	}

	public boolean isNested() {
		return nested;
	}

	public TestItemTypeEnum getType() {
		return type;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public String getIssue() {
		return issue;
	}

	public int getLogCount() {
		return logCount;
	}
}
