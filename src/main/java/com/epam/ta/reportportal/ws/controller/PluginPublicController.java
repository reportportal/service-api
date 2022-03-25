package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/plugin/public")
public class PluginPublicController {

	private final PluginFilesProvider pluginPublicFilesProvider;

	public PluginPublicController(PluginFilesProvider pluginPublicFilesProvider) {
		this.pluginPublicFilesProvider = pluginPublicFilesProvider;
	}

	@GetMapping(value = "/{pluginName}/file/{name}")
	public void getPublicFile(@PathVariable(value = "pluginName") String pluginName, @PathVariable(value = "name") String fileName,
			HttpServletResponse response) {
		final BinaryData binaryData = pluginPublicFilesProvider.load(pluginName, fileName);
		toResponse(binaryData, response);
	}

	/**
	 * Copies data from provided {@link InputStream} to Response
	 *
	 * @param binaryData File data
	 * @param response   Response object
	 */
	private void toResponse(BinaryData binaryData, HttpServletResponse response) {
		try {
			response.setContentType(binaryData.getContentType());
			IOUtils.copy(binaryData.getInputStream(), response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
		}
	}
}
