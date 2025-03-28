package com.epam.ta.reportportal.core.integration.plugin.strategy;

import com.epam.ta.reportportal.core.integration.plugin.PluginUploader;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginUploaderFactory {

  private final Map<String, PluginUploader> uploads = new HashMap<>();

  @Autowired
  public PluginUploaderFactory(JarPluginUploader jarPluginUploader,
      JsonPluginUploader jsonPluginUploader) {
    uploads.put("application/java-archive", jarPluginUploader);
    uploads.put("application/json", jsonPluginUploader);
  }

  public PluginUploader getUploader(String contentType) {
    return uploads.get(contentType);
  }
}