package com.epam.ta.reportportal.core.integration.plugin.м2;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class EmptyNameValidator implements FileValidator, Ordered {

	@Override
	public void validate(MultipartFile pluginFile) {
		BusinessRule.expect(pluginFile.getName(), StringUtils::isNotBlank).verify(ErrorType.BAD_REQUEST_ERROR, "File name should be not empty.");
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
