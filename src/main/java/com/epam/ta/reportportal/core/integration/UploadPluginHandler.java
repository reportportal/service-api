package com.epam.ta.reportportal.core.integration;

import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface UploadPluginHandler {

	EntryCreatedRS uploadPlugin(MultipartFile pluginFile);
}
