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

package com.epam.ta.reportportal.core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

public class HelperController {
	private static final Logger logger = LoggerFactory.getLogger(HelperController.class);

	private static final HttpHeaders responseHeaders;

	static {
		responseHeaders = new HttpHeaders();
		responseHeaders.set("Cotent-Type", "text/plain");
		responseHeaders.set("Server", "Apache");
	}

	@HttpLogging(logExecutionTime = false)
	public ResponseEntity<Map<String, Object>> logFull(@RequestBody Object payload) {
		return new ResponseEntity(payload, responseHeaders, HttpStatus.OK);
	}

	@HttpLogging(logExecutionTime = false, logHeaders = false)
	public ResponseEntity<Map<String, Object>> logWithoutHeaders(@RequestBody Object payload) {
		return new ResponseEntity(payload, responseHeaders, HttpStatus.OK);
	}

	@HttpLogging(logExecutionTime = false, logResponseBody = false, logRequestBody = false)
	public ResponseEntity<Map<String, Object>> logWithoutBody(@RequestBody Object payload) {
		return new ResponseEntity(payload, responseHeaders, HttpStatus.OK);
	}
}
