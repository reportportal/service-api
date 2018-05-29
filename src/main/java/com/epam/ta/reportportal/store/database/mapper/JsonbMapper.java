package com.epam.ta.reportportal.store.database.mapper;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.entity.JsonbObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.lang.CharEncoding.UTF_8;

public class JsonbMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonbMapper.class);
	private static ObjectMapper objectMapper;

	static {

		objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping();
	}

	private JsonbMapper() {
		//static only
	}

	public static <T extends JsonbObject> T getJsonb(Object object) {

		return (T) Optional.ofNullable(object)
				.filter(v -> v instanceof PGobject)
				.map(v -> ((PGobject) object).getValue())
				.flatMap(JsonbMapper::read)
				.orElseThrow(() -> new ReportPortalException("Failed to convert String to JsonObject"));
	}

	private static Optional<JsonbObject> read (String value) {

		try {

			return Optional.ofNullable(objectMapper.readValue(value.getBytes(UTF_8), JsonbObject.class));
		} catch (IOException e) {

			LOGGER.error("Failed to convert String to JsonObject: ", e);

			return Optional.empty();
		}
	}
}
