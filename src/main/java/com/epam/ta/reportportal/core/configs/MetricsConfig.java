package com.epam.ta.reportportal.core.configs;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Class-based metrics configuration. Place here common tags, filters, naming conventions, etc.
 */
@Configuration
public class MetricsConfig {

	@Autowired(required = false)
	private BuildProperties buildProperties;

	@Bean
	public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config()
				.commonTags(Collections.singleton(Tag.of("version", buildProperties != null ? buildProperties.getVersion() : "")));
	}
}
