package com.epam.ta.reportportal;

import com.epam.ta.reportportal.job.SaveBinaryDataJob;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
@EnableAutoConfiguration(exclude = QuartzAutoConfiguration.class)
@ComponentScan(value = "com.epam.ta.reportportal", excludeFilters = {
		//		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.core.analyzer.*"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.ws.rabbit.*"),
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.epam.ta.reportportal.job.*") }, includeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SaveBinaryDataJob.class) })
@PropertySource("classpath:test-application.properties")
public class TestConfig {
}