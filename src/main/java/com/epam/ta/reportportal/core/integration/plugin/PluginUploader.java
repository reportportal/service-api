package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.io.IOException;
import java.io.InputStream;

public interface PluginUploader {

  IntegrationType uploadPlugin(String fileName, InputStream inputStream) throws IOException;
}
