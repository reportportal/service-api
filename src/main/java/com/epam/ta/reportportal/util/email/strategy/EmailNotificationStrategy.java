/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.util.email.strategy;

import java.util.Map;

/**
 * Interface defining the email notification strategy.
 *
 * @author Andrei Piankouski
 */
public interface EmailNotificationStrategy {

  /**
   * Sends an email to the recipient with the given parameters.
   *
   * @param recipient the email recipient
   * @param params    the parameters for sending the email
   */
  void sendEmail(String recipient, Map<String, Object> params);

}
