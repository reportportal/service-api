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

import com.epam.ta.reportportal.store.database.entity.bts.Ticket;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "issue", schema = "public", indexes = { @Index(name = "issue_pk", unique = true, columnList = "issue_id ASC") })
public class IssueEntity implements Serializable {

	@Id
	@Column(name = "issue_id", unique = true, nullable = false, precision = 64)
	private Long issueId;

	@ManyToOne
	@JoinColumn(name = "issue_type")
	private IssueType issueType;

	@Column(name = "issue_description")
	private String issueDescription;

	@Column(name = "auto_analyzed")
	private Boolean autoAnalyzed;

	@Column(name = "ignore_analyzer")
	private Boolean ignoreAnalyzer;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "issue_ticket", joinColumns = @JoinColumn(name = "issue_id"), inverseJoinColumns = @JoinColumn(name = "ticket_id"))
	private Set<Ticket> tickets = Sets.newHashSet();

	public IssueEntity() {
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public IssueType getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueType issueType) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IssueEntity that = (IssueEntity) o;
		return Objects.equals(issueId, that.issueId) && Objects.equals(issueType, that.issueType) && Objects.equals(
				issueDescription, that.issueDescription) && Objects.equals(autoAnalyzed, that.autoAnalyzed) && Objects.equals(
				ignoreAnalyzer, that.ignoreAnalyzer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(issueId, issueType, issueDescription, autoAnalyzed, ignoreAnalyzer);
	}

	@Override
	public String toString() {
		return "IssueEntity{" + "issueId=" + issueId + ", issueType=" + issueType + ", issueDescription='" + issueDescription + '\''
				+ ", autoAnalyzed=" + autoAnalyzed + ", ignoreAnalyzer=" + ignoreAnalyzer;
	}
}
