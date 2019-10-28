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
