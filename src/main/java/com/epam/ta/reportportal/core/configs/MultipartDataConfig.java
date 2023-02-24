package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.util.BinaryDataResponseWriter;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.activation.MimetypesFileTypeMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class MultipartDataConfig {

	@Bean
	public MimetypesFileTypeMap mimetypesFileTypeMap() {
		return new MimetypesFileTypeMap();
	}

	@Bean
	public AutoDetectParser autoDetectParser() {
		return new AutoDetectParser();
	}

	@Bean
	public BinaryDataResponseWriter binaryDataResponseWriter() {
		return new BinaryDataResponseWriter();
	}
}
