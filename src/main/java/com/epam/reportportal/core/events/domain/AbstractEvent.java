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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Abstract base for all domain events, supporting optional state snapshots and user context. Events
 * are serialized to JSON for API/message queue integration and can be published using Spring's
 * {@code ApplicationEventPublisher}.
 *
 * @param <T> Type of the state snapshot, use {@code Void} for events without state.
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
@JsonIgnoreProperties({"source"})
public abstract class AbstractEvent<T> extends ApplicationEvent {

  protected Long userId;
  protected String userLogin;
  protected T before;
  protected T after;
  protected Long organizationId;
  protected Long projectId;
  protected Instant occurredAt = Instant.now();
  protected String eventSource = "ReportPortal";

  /**
   * Default constructor for system events (no user context).
   */
  protected AbstractEvent() {
    super(new Object());
  }

  /**
   * Constructs an AbstractEvent with explicit event source.
   *
   * @param eventSource The name of the service/component that generated this event
   */
  protected AbstractEvent(String eventSource) {
    super(new Object());
    this.eventSource = eventSource;
  }

  /**
   * Constructs an AbstractEvent with user context but no state snapshots.
   *
   * @param userId    The ID of the user who triggered the event
   * @param userLogin The login of the user who triggered the event
   */
  protected AbstractEvent(Long userId, String userLogin) {
    super(new Object());
    this.userId = userId;
    this.userLogin = userLogin;
    this.before = null;
    this.after = null;
  }

  /**
   * Constructs an AbstractEvent with user context and state snapshots.
   *
   * @param userId    The ID of the user who triggered the event
   * @param userLogin The login of the user who triggered the event
   * @param before    The state before the event (can be null for CREATE events)
   * @param after     The state after the event (can be null for DELETE events)
   */
  protected AbstractEvent(Long userId, String userLogin, T before, T after) {
    super(new Object());
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
   * Returns whether this event should be published to RabbitMQ. Default implementation returns true
   * for backward compatibility. Override in subclasses to mark events as local-only.
   *
   * @return true if the event should be published to RabbitMQ
   */
  public boolean shouldPublishToRabbitMQ() {
    return true;
  }

}
