/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.events.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Abstract base for all domain events, supporting optional state snapshots and user context. Events
 * are serialized to JSON for API/message queue integration and can be published using Spring's
 * {@code ApplicationEventPublisher}.
 *
 * @param <T> Type of the state snapshot, use {@code Void} for events without state.
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
@NoArgsConstructor
public abstract class AbstractEvent<T> {

  protected Long userId;
  protected String userLogin;
  protected T before;
  protected T after;
  protected Long organizationId;
  protected Long projectId;
  protected Instant occurredAt = Instant.now();

  protected AbstractEvent(Long userId, String userLogin) {
    this.userId = userId;
    this.userLogin = userLogin;
    this.before = null;
    this.after = null;
  }

  protected AbstractEvent(Long userId, String userLogin, T before, T after) {
    this.userId = userId;
    this.userLogin = userLogin;
    this.before = before;
    this.after = after;
  }

  /**
   * Returns whether this is a system event (internal processing, no user context). System events
   * don't require activity tracking as they represent automated processes.
   *
   * @return true if userId is null (system event), false otherwise
   */
  public boolean isSystemEvent() {
    return userId == null;
  }

  /**
   * Sets the project ID for this event. Call this in constructor for events that have a project
   * context.
   *
   * @param projectId the project ID, or null if not applicable
   */
  protected void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  /**
   * Sets the organization ID for this event. Call this in constructor for events that have an
   * organization context.
   *
   * @param organizationId the organization ID, or null if not applicable
   */
  protected void setOrganizationId(Long organizationId) {
    this.organizationId = organizationId;
  }

}
