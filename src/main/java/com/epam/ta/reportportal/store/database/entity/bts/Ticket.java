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

package com.epam.ta.reportportal.store.database.entity.bts;

import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Pavel Bortnik
 */

@Entity
@Table(name = "ticket")
public class Ticket implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "ticket_id")
	private String ticketId;

	@Column(name = "sumitter_id")
	private String submitterId;

	@Column(name = "submit_date")
	private LocalDateTime submitDate;

	@Column(name = "bts_id")
	private Long bugTrackingSystemId;

	@Column(name = "url")
	private String url;

	@ManyToMany(mappedBy = "tickets")
	private Set<IssueEntity> issues = Sets.newHashSet();

	public Ticket() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public String getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}

	public LocalDateTime getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(LocalDateTime submitDate) {
		this.submitDate = submitDate;
	}

	public Long getBugTrackingSystemId() {
		return bugTrackingSystemId;
	}

	public void setBugTrackingSystemId(Long bugTrackingSystemId) {
		this.bugTrackingSystemId = bugTrackingSystemId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Set<IssueEntity> getIssues() {
		return issues;
	}

	public void setIssues(Set<IssueEntity> issues) {
		this.issues = issues;
	}
}
