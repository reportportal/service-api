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

package com.epam.reportportal.extension.bugtracking;

import com.epam.reportportal.infrastructure.model.externalsystem.PostFormField;
import com.epam.reportportal.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.infrastructure.persistence.entity.integration.Integration;
import java.util.List;
import java.util.Optional;
import org.pf4j.ExtensionPoint;

/**
 * Generic interface to access third-party bug tracking systems
 *
 * @author Andrei Varabyeu
 * @deprecated Use command based approach.
 */

@Deprecated
public interface BtsExtension extends ExtensionPoint {

  /**
   * Test connection to external system with provided details
   *
   * @param system - external system details
   * @return TRUE if connection is successful. Otherwise FALSE or throws an exception if no such external system is
   * present
   */
  boolean testConnection(Integration system);

  /**
   * Get ticket by ID
   *
   * @param id     ID of ticket
   * @param system ExternalSystem
   * @return Found Ticket
   */
  Optional<Ticket> getTicket(String id, Integration system);

  /**
   * Submit ticket into external system
   *
   * @param ticketRQ Create ticket DTO
   * @param system   External system
   * @return Created Ticket
   */
  Ticket submitTicket(PostTicketRQ ticketRQ, Integration system);

  /**
   * Get map of fields for ticket POST into external system
   *
   * @param issueType Type of issue
   * @param system    External system
   * @return List of form fields
   */
  List<PostFormField> getTicketFields(String issueType, Integration system);

  /**
   * Get list of allowable issue types for external system
   *
   * @param system External system
   * @return List of issue types
   */
  List<String> getIssueTypes(Integration system);
}
