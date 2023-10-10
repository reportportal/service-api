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

package com.epam.ta.reportportal.core.events;

/**
 * MessageBus is an abstraction for dealing with events over external event-streaming system
 *
 * @author Andrei Varabyeu
 */
public interface MessageBus {

  /**
   * Sends event by the given route and exchange
   *
   * @param route Route
   * @param o     Payload
   */
  void publish(String exchange, String route, Object o);

  /**
   * Sends event by the given route
   *
   * @param route Route
   * @param o     Payload
   */
  void publish(String route, Object o);

  /**
   * Sends activity
   *
   * @param o Payload
   */
  void publishActivity(ActivityEvent o);

}
