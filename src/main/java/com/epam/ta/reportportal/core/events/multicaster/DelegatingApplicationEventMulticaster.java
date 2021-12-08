/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.events.multicaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;

/**
 * Extension for {@link SimpleApplicationEventMulticaster} to allow error handling only for provided set of events
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class DelegatingApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

	private final Set<Class<?>> errorHandlingEventTypes;

	public DelegatingApplicationEventMulticaster(Set<Class<?>> errorHandlingEventTypes) {
		this.errorHandlingEventTypes = errorHandlingEventTypes;
	}

	private static final AtomicLong COUNTER = new AtomicLong(0L);

	@Override
	protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
		ofNullable(getErrorHandler()).filter(h -> errorHandlingEventTypes.contains(event.getClass())).ifPresentOrElse(h -> {
			try {
				doInvokeListener(listener, event);
			} catch (Throwable err) {
				h.handleError(err);
			}
		}, () -> doInvokeListener(listener, event));
	}

	/**
	 * @see SimpleApplicationEventMulticaster
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
		try {
			listener.onApplicationEvent(event);
		} catch (ClassCastException ex) {
			String msg = ex.getMessage();
			if (msg == null || matchesClassCastMessage(msg, event.getClass())) {
				// Possibly a lambda-defined listener which we could not resolve the generic event type for
				// -> let's suppress the exception and just log a debug message.
				Log logger = LogFactory.getLog(getClass());
				if (logger.isTraceEnabled()) {
					logger.trace("Non-matching event type for listener: " + listener, ex);
				}
			} else {
				throw ex;
			}
		}
	}

	/**
	 * @see SimpleApplicationEventMulticaster
	 */
	private boolean matchesClassCastMessage(String classCastMessage, Class<?> eventClass) {
		// On Java 8, the message starts with the class name: "java.lang.String cannot be cast..."
		if (classCastMessage.startsWith(eventClass.getName())) {
			return true;
		}
		// On Java 11, the message starts with "class ..." a.k.a. Class.toString()
		if (classCastMessage.startsWith(eventClass.toString())) {
			return true;
		}
		// On Java 9, the message used to contain the module name: "java.base/java.lang.String cannot be cast..."
		int moduleSeparatorIndex = classCastMessage.indexOf('/');
		if (moduleSeparatorIndex != -1 && classCastMessage.startsWith(eventClass.getName(), moduleSeparatorIndex + 1)) {
			return true;
		}
		// Assuming an unrelated class cast failure...
		return false;
	}
}
