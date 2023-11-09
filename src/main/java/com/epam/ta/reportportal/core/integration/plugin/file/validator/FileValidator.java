package com.epam.ta.reportportal.core.integration.plugin.file.validator;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface FileValidator {

	void validate(MultipartFile pluginFile);
}