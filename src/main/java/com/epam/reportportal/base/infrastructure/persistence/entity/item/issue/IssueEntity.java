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

package com.epam.reportportal.base.infrastructure.persistence.entity.item.issue;

import com.epam.reportportal.base.infrastructure.persistence.entity.bts.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.google.common.collect.Sets;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "issue", schema = "public", indexes = {
    @Index(name = "issue_pk", unique = true, columnList = "issue_id ASC")})
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
  private boolean autoAnalyzed;

  @Column(name = "ignore_analyzer")
  private boolean ignoreAnalyzer;

  @OneToOne
  @MapsId
  @JoinColumn(name = "issue_id")
  private TestItemResults testItemResults;

  @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST,
      CascadeType.REFRESH}, fetch = FetchType.EAGER)
  @JoinTable(name = "issue_ticket", joinColumns = @JoinColumn(name = "issue_id"), inverseJoinColumns = @JoinColumn(name = "ticket_id"))
  @Fetch(FetchMode.SUBSELECT)
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

  public Set<Ticket> getTickets() {
    return tickets;
  }

  public void setTickets(Set<Ticket> tickets) {
    this.tickets = tickets;
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

  public void removeTicket(Ticket ticket) {
    tickets.remove(ticket);
    ticket.getIssues().remove(this);
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
    return Objects.equals(issueId, that.issueId) && Objects.equals(issueType, that.issueType)
        && Objects.equals(issueDescription,
        that.issueDescription
    ) && Objects.equals(autoAnalyzed, that.autoAnalyzed) && Objects.equals(ignoreAnalyzer,
        that.ignoreAnalyzer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(issueId, issueType, issueDescription, autoAnalyzed, ignoreAnalyzer);
  }

  @Override
  public String toString() {
    return "IssueEntity{" + "issueId=" + issueId + ", issueType=" + issueType
        + ", issueDescription='" + issueDescription + '\''
        + ", autoAnalyzed=" + autoAnalyzed + ", ignoreAnalyzer=" + ignoreAnalyzer;
  }
}
