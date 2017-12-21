/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.resolver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;

/**
 * Module for custom serializer configuration for {@link JacksonViewAware}
 *
 * @author Andrei Varabyeu
 */
public class JacksonViewAwareModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	/**
	 * @param objectMapper - We need to provide ObjectMapper here since it's impossible
	 *                     to serialize using JSON Views without mapper. It's little bit
	 *                     unusual from Jackson point of view, but this is only one way
	 *                     to avoid 'instaceOf' and classcast on http message converters
	 *                     level
	 * @see <a
	 * href="http://wiki.fasterxml.com/JacksonHowToCustomSerializers">Jackson
	 * - HowTo - CustomSerializers</a>
	 */
	public JacksonViewAwareModule(ObjectMapper objectMapper) {
		addSerializer(JacksonViewAware.class, new JacksonViewAwareSerializer(objectMapper));
	}

	public static class JacksonViewAwareSerializer extends StdScalarSerializer<JacksonViewAware> {

		private ObjectMapper objectMapper;

		protected JacksonViewAwareSerializer(ObjectMapper objectMapper) {
			super(JacksonViewAware.class);
			this.objectMapper = objectMapper;
		}

		@Override
		public void serialize(JacksonViewAware value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			/*
			 * Writes bean with specified view
			 */
			objectMapper.writerWithView(value.getView()).writeValue(jgen, value.getPojo());
		}

	}

}