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

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "issue", schema = "public", indexes = { @Index(name = "issue_pk", unique = true, columnList = "id ASC"),
		@Index(name = "issue_test_item_results_id_key", unique = true, columnList = "test_item_results_id ASC") })
public class Issue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, precision = 64)
	private Long id;

	@Column(name = "issue_type", precision = 32)
	private Integer issueType;

	@Column(name = "issue_description")
	private String issueDescription;

	@Column(name = "auto_analyzed")
	private Boolean autoAnalyzed;

	@Column(name = "ignore_analyzer")
	private Boolean ignoreAnalyzer;

	@Column(name = "test_item_results_id", unique = true, precision = 64)
	private Long testItemResultsId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getTestItemResultsId() {
		return testItemResultsId;
	}

	public void setTestItemResultsId(Long testItemResultsId) {
		this.testItemResultsId = testItemResultsId;
	}

	@Override
	public String toString() {
		return "Issue{" + "id=" + id + ", issueType=" + issueType + ", issueDescription='" + issueDescription + '\'' + ", autoAnalyzed="
				+ autoAnalyzed + ", ignoreAnalyzer=" + ignoreAnalyzer + ", testItemResultsId=" + testItemResultsId + '}';
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
		return Objects.equals(id, issue.id) && Objects.equals(issueType, issue.issueType) && Objects.equals(
				issueDescription, issue.issueDescription) && Objects.equals(autoAnalyzed, issue.autoAnalyzed) && Objects.equals(
				ignoreAnalyzer, issue.ignoreAnalyzer) && Objects.equals(testItemResultsId, issue.testItemResultsId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, issueType, issueDescription, autoAnalyzed, ignoreAnalyzer, testItemResultsId);
	}
}
