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
 * The registry could not be talked to at all — DNS, connect refused, connect or read timeout. The
 * host is carried because operators need to be told which one is down.
 */
public class RegistryUnreachableException extends MarketplaceException {

  private final String host;

  public RegistryUnreachableException(String host, Throwable cause) {
    super("Marketplace registry at '" + host + "' is unreachable: " + cause.getMessage(), cause);
    this.host = host;
  }

  public String getHost() {
    return host;
  }
}
