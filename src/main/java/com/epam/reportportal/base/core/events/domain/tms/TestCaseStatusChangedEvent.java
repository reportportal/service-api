package com.epam.reportportal.base.core.events.domain.tms;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A test case changed its lifecycle status (Draft / Ready): the project activity records who did it and why.
 * <p>
 * Example: a user approves a Draft case: {@code Draft -> Ready, reason "Approved"}, subject the user. Auto-Ready
 * promotes another: {@code Draft -> Ready, reason "Auto-Ready (93 >= 90)"}, subject the rule "Auto-Ready". The agent
 * fixes a Ready case: {@code Ready -> Draft, reason "Agent fix"}, subject the application "Agent".
 */
@Getter
@NoArgsConstructor
public class TestCaseStatusChangedEvent extends AbstractEvent<TestCaseActivityResource> {

  private String oldStatus;
  private String newStatus;
  private String reason;
  private EventSubject subjectType;
  private String subjectName;
  private Long subjectId;

  /**
   * Creates the event.
   *
   * @param resource       the test case (id, name and project are enough)
   * @param subjectType    {@code USER}, {@code RULE} for Auto-Ready or {@code APPLICATION} for the agent and the pipeline
   * @param subjectId      the user id for a user, otherwise null
   * @param subjectName    the login of the user, or the name of the rule / application
   * @param organizationId the organization of the project
   */
  public TestCaseStatusChangedEvent(TestCaseActivityResource resource, String oldStatus, String newStatus,
      String reason, EventSubject subjectType, Long subjectId, String subjectName, Long organizationId) {
    super(subjectId, subjectName, resource, resource);
    this.organizationId = organizationId;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.reason = reason;
    this.subjectType = subjectType;
    this.subjectId = subjectId;
    this.subjectName = subjectName;
  }
}
