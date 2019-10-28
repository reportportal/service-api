package com.epam.ta.reportportal.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpLoggingAspectTest {
	private static HelperController proxy;

	private static HttpLoggingAspect aspect;

	private static MockHttpServletRequest request;

	private static Map<Object, Object> payload;

	private static Logger logger;

	private static Appender<ILoggingEvent> appender;

	private static HttpLogging annotation;

	private static String requestLog;

	private static String responseLog;

	private static AtomicLong COUNT = new AtomicLong();

	@BeforeAll
	public static void beforeAll() {
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
	public void setup() {
		logger.detachAndStopAllAppenders();
		appender = mock(Appender.class);
		logger.addAppender(appender);
	}

	@Test
	public void testFull() throws Exception {

		annotation = mock(HttpLogging.class);
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
	public void testWithoutHeaders() throws Exception {

		annotation = mock(HttpLogging.class);
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
	public void testWithoutBody() throws Exception {

		annotation = mock(HttpLogging.class);
		when(annotation.logHeaders()).thenReturn(true);
		when(annotation.logRequestBody()).thenReturn(false);
		when(annotation.logResponseBody()).thenReturn(false);
		when(annotation.logExecutionTime()).thenReturn(false);

		long count = COUNT.incrementAndGet();
		ResponseEntity<Map<String, Object>> response = proxy.logWithoutBody(payload);
		formatRequestResponseAndPrint(count, "logWithoutBody", request, response);

		checkLoggingRecords(appender, 2, new Level[] { Level.DEBUG, Level.DEBUG }, requestLog, responseLog);
	}

	private void formatRequestResponseAndPrint(long count, String prefix, HttpServletRequest request, ResponseEntity response) throws Exception {
		requestLog = aspect.formatRequestRecord(count, prefix, request, payload, annotation);
		responseLog = aspect.formatResponseRecord(count, prefix, response, annotation, 0L);
		System.out.println(requestLog);
		System.out.println(responseLog);
	}

}