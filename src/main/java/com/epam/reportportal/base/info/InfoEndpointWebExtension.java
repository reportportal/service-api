/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.info;

import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.info.InfoEndpoint;

/**
 * Serves {@code /info} over HTTP: the full response to an authenticated caller, the {@link AnonymousInfo} subset to
 * everyone else. One endpoint and one shape, so a client that does not send credentials gets fewer keys rather than
 * an error.
 */
@EndpointWebExtension(endpoint = InfoEndpoint.class)
public class InfoEndpointWebExtension {

  private final InfoEndpoint delegate;

  public InfoEndpointWebExtension(InfoEndpoint delegate) {
    this.delegate = delegate;
  }

  @ReadOperation
  public Map<String, Object> info() {
    Map<String, Object> info = delegate.info();
    return InfoAudience.isAuthenticated() ? info : AnonymousInfo.of(info);
  }
}
