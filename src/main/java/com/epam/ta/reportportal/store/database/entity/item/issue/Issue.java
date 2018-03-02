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

package com.epam.ta.reportportal.store.database.entity.item.issue;

import com.epam.ta.reportportal.store.database.entity.item.TestItemResults;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "issue", schema = "public", indexes = { @Index(name = "issue_pk", unique = true, columnList = "issue_id ASC") })
public class Issue {

	@Id
	@Column(name = "issue_id", unique = true, nullable = false, precision = 64)
	private Long issueId;

	@Column(name = "issue_type", precision = 32)
	private Integer issueType;

	@Column(name = "issue_description")
	private String issueDescription;

	@Column(name = "auto_analyzed")
	private Boolean autoAnalyzed;

	@Column(name = "ignore_analyzer")
	private Boolean ignoreAnalyzer;

	@OneToOne
	@MapsId
	@JoinColumn(name = "issue_id")
	private TestItemResults testItemResults;

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public Integer getIssueType() {
		return issueType;
	}

	public void setIssueType(Integer issueType) {
		this.issueType = issueType;
	}

	public String getIssueDescription() {
		return issueDescription;
	}

	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}

	public Boolean getAutoAnalyzed() {
		return autoAnalyzed;
	}

	public void setAutoAnalyzed(Boolean autoAnalyzed) {
		this.autoAnalyzed = autoAnalyzed;
	}

	public Boolean getIgnoreAnalyzer() {
		return ignoreAnalyzer;
	}

	public void setIgnoreAnalyzer(Boolean ignoreAnalyzer) {
		this.ignoreAnalyzer = ignoreAnalyzer;
	}

	public TestItemResults getTestItemResults() {
		return testItemResults;
	}

	public void setTestItemResults(TestItemResults testItemResults) {
		this.testItemResults = testItemResults;
	}

	@Override
	public String toString() {
		return "Issue{" + "issueId=" + issueId + ", issueType=" + issueType + ", issueDescription='" + issueDescription + '\''
				+ ", autoAnalyzed=" + autoAnalyzed + ", ignoreAnalyzer=" + ignoreAnalyzer + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Issue issue = (Issue) o;
		return Objects.equals(issueId, issue.issueId) && Objects.equals(issueType, issue.issueType) && Objects.equals(
				issueDescription, issue.issueDescription) && Objects.equals(autoAnalyzed, issue.autoAnalyzed) && Objects.equals(
				ignoreAnalyzer, issue.ignoreAnalyzer) && Objects.equals(testItemResults, issue.testItemResults);
	}

	@Override
	public int hashCode() {
		return Objects.hash(issueId, issueType, issueDescription, autoAnalyzed, ignoreAnalyzer, testItemResults);
	}
}
