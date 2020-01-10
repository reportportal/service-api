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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.core.logging.HelperUtil.checkLoggingRecords;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpLoggingAspectTest {
	private static HelperController proxy;

	private static HttpLoggingAspect aspect;

	private static MockHttpServletRequest request;

	private static Map<Object, Object> payload;

	private static Logger logger;

	@Mock
	private static Appender<ILoggingEvent> appender;

	@Mock
	private static HttpLogging annotation;

	private static String requestLog;

	private static String responseLog;

	private static AtomicLong COUNT = new AtomicLong();

	@BeforeAll
	static void beforeAll() {
		aspect = new HttpLoggingAspect();
		ReflectionTestUtils.setField(aspect, "objectMapper", new ObjectMapper());

		HelperController controller = new HelperController();
		AspectJProxyFactory factory = new AspectJProxyFactory(controller);
		factory.addAspect(aspect);
		proxy = factory.getProxy();

		request = new MockHttpServletRequest("GET", "/request/path/is/here");
		request.setQueryString("ddd=qwerty");
		request.addHeader("Content-Type", "application/json");
		request.addHeader("Host", "localhost");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		payload = new HashMap<>();
		payload.put("key1", "one");
		payload.put("key2", "two");
		payload.put("key3", "three");

		logger = (Logger) LoggerFactory.getLogger(HelperController.class);
		logger.setLevel(Level.DEBUG);
		logger.setAdditive(false);
	}

	@BeforeEach
	void setup() {
		logger.detachAndStopAllAppenders();
		logger.addAppender(appender);
	}

	@Test
	void testFull() throws Exception {

		when(annotation.logHeaders()).thenReturn(true);
		when(annotation.logRequestBody()).thenReturn(true);
		when(annotation.logResponseBody()).thenReturn(true);
		when(annotation.logExecutionTime()).thenReturn(false);

		long count = COUNT.incrementAndGet();
		ResponseEntity<Map<String, Object>> response = proxy.logFull(payload);
		formatRequestResponseAndPrint(count, "logFull", request, response);

		checkLoggingRecords(appender, 2, new Level[] { Level.DEBUG, Level.DEBUG }, requestLog, responseLog);
	}

	@Test
	void testWithoutHeaders() throws Exception {

		when(annotation.logHeaders()).thenReturn(false);
		when(annotation.logRequestBody()).thenReturn(true);
		when(annotation.logResponseBody()).thenReturn(true);
		when(annotation.logExecutionTime()).thenReturn(false);

		long count = COUNT.incrementAndGet();
		ResponseEntity<Map<String, Object>> response = proxy.logWithoutHeaders(payload);
		formatRequestResponseAndPrint(count, "logWithoutHeaders", request, response);

		checkLoggingRecords(appender, 2, new Level[] { Level.DEBUG, Level.DEBUG }, requestLog, responseLog);
	}

	@Test
	void testWithoutBody() throws Exception {

		when(annotation.logHeaders()).thenReturn(true);
		when(annotation.logRequestBody()).thenReturn(false);
		when(annotation.logResponseBody()).thenReturn(false);
		when(annotation.logExecutionTime()).thenReturn(false);

		long count = COUNT.incrementAndGet();
		ResponseEntity<Map<String, Object>> response = proxy.logWithoutBody(payload);
		formatRequestResponseAndPrint(count, "logWithoutBody", request, response);

		checkLoggingRecords(appender, 2, new Level[] { Level.DEBUG, Level.DEBUG }, requestLog, responseLog);
	}

	private void formatRequestResponseAndPrint(long count, String prefix, HttpServletRequest request, ResponseEntity response)
			throws Exception {
		requestLog = aspect.formatRequestRecord(count, prefix, request, payload, annotation);
		responseLog = aspect.formatResponseRecord(count, prefix, response, annotation, 0L);
		System.out.println(requestLog);
		System.out.println(responseLog);
	}

}