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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemIssueConverter;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toSet;

/**
 * @author Pavel Bortnik
 */
public class IssueEntityBuilder implements Supplier<IssueEntity> {

	private IssueEntity issueEntity;

	public IssueEntityBuilder() {
		this.issueEntity = new IssueEntity();
	}

	public IssueEntityBuilder(IssueEntity issueEntity) {
		this.issueEntity = issueEntity;
	}

	public IssueEntityBuilder addIssueType(IssueType issueType) {
		Preconditions.checkNotNull(issueType);
		issueEntity.setIssueType(issueType);
		return this;
	}

	public IssueEntityBuilder addDescription(String comment) {
		if (null != comment) {
			issueEntity.setIssueDescription(comment.trim());
		}
		return this;
	}

	public IssueEntityBuilder addIgnoreFlag(boolean ignoreAnalyzer) {
		issueEntity.setIgnoreAnalyzer(ignoreAnalyzer);
		return this;
	}

	public IssueEntityBuilder addAutoAnalyzedFlag(boolean autoAnalyzed) {
		issueEntity.setAutoAnalyzed(autoAnalyzed);
		return this;
	}

	@Override
	public IssueEntity get() {
		return this.issueEntity;
	}

}
