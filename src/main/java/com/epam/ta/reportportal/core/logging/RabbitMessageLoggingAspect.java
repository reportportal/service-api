package com.epam.ta.reportportal.core.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Konstantin Antipin
 */

@Component
@Aspect
public class RabbitMessageLoggingAspect {

	private static final String NEWLINE = "\n";
	private static final String BODY_DENOMINATOR = "-- Body --";

	@Value("${rp.requestLogging:false}")
	private boolean requestLoggingEnabled;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MessageConverter messageConverter;

	@Around("execution(public * *(..)) && @annotation(annotation)")
	public Object log(ProceedingJoinPoint joinPoint, RabbitMessageLogging annotation) throws Throwable {

		if (!requestLoggingEnabled) {
			return joinPoint.proceed();
		}

		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

		Map<String, Object> headers = new HashMap<>();
		Object body = getHeadersAndBody(joinPoint, method, headers);

		String prefix = method.getName();

		if (logger.isDebugEnabled()) {
			logger.debug(formatMessageRecord(prefix, headers, body, annotation));
		}

		return joinPoint.proceed();
	}

	private Object getHeadersAndBody(ProceedingJoinPoint joinPoint, Method method, Map<String, Object> headers) {
		Object body = null;
		Object[] args = joinPoint.getArgs();
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Object arg = args[i];

			if (arg != null) {
				if (arg instanceof Message) {
					Message message = (Message) arg;
					body = messageConverter.fromMessage(message);
					headers.putAll(message.getMessageProperties().getHeaders());
					break;
				} else if (parameters[i].isAnnotationPresent(Payload.class)) {
					body = arg;
				} else if (parameters[i].isAnnotationPresent(Header.class)) {
					headers.put(parameters[i].getAnnotation(Header.class).name(), arg);
				}
			}
		}
		return body;
	}

	private String formatMessageRecord(String prefix, Map<String, Object> headers,
			Object body, RabbitMessageLogging annotation) throws Exception {
		StringBuilder record = new StringBuilder();

		record.append(prefix).append(" - Rabbit message");

		// headers
		if (annotation.logHeaders()) {
			for (Map.Entry<String, Object> entry : headers.entrySet()) {
				record.append(NEWLINE).append(' ').append(entry.getKey()).append(": ").append(entry.getValue());
			}
		}

		// body
		if (annotation.logBody() && body !=null) {
			record.append(NEWLINE).append(' ').append(BODY_DENOMINATOR)
					.append(NEWLINE).append(' ').append(objectMapper.writeValueAsString(body));
		}

		return record.toString();
	}
}
