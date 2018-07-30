package com.epam.ta.reportportal.info;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
@Component
public class EnvironmentVariablesInfoContributor implements ExtensionContributor {

	private static final String RP_ENV_PREFIX = "RP_ENVIRONMENT_";

	@Override
	public Map<String, ?> contribute() {
		return System.getenv()
				.entrySet()
				.stream()
				.filter(it -> it.getKey().startsWith(RP_ENV_PREFIX))
				.collect(Collectors.toMap(e -> e.getKey().replaceFirst(RP_ENV_PREFIX, "").toLowerCase(), Map.Entry::getValue));
	}
}
