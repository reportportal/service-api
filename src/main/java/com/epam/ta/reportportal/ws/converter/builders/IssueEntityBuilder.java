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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;
import com.google.common.base.Preconditions;

import java.util.function.Supplier;

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
