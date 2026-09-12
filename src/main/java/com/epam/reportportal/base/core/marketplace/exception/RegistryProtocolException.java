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

package com.epam.reportportal.base.core.marketplace.exception;

/**
 * The registry answered with something the client cannot make sense of — unreadable body, or a
 * redirect without a Location. Reaching this is a contract breach, not an operational state.
 */
public class RegistryProtocolException extends MarketplaceException {

  public RegistryProtocolException(String message) {
    super(message);
  }

  public RegistryProtocolException(String message, Throwable cause) {
    super(message, cause);
  }
}
