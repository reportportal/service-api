package com.epam.ta.reportportal.core.integration.plugin.file;

import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginFileManager {

	Path uploadTemp(MultipartFile pluginFile, Path tempPath);

	PluginPathInfo upload(PluginInfo pluginInfo, Path pluginsPath, Path resourcesPath);

	void delete(Path path);

	void delete(String fileId);
}
