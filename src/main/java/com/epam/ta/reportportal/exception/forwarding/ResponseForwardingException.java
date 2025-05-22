/*
 * Copyright 2019 EPAM Systems
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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.exception.forwarding;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Exception to be forwarded from downstream to upstream microservice.
 *
 * @author Andrei Varabyeu
 */
@Getter
public class ResponseForwardingException extends RuntimeException {

  private final byte[] body;
  /**
   * HTTP headers from the original response that will be forwarded upstream
   */
  private final HttpHeaders headers;
  private final HttpStatus status;

  public ResponseForwardingException(ClientHttpResponse response) throws IOException {
    this.headers = response.getHeaders();
    this.status = HttpStatus.valueOf(response.getStatusCode().value());
    this.body = ByteStreams.toByteArray(response.getBody());
  }

}
