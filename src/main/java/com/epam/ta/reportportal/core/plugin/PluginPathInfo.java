package com.epam.ta.reportportal.core.plugin;

import java.nio.file.Path;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginPathInfo {

  private Path pluginPath;
  private Path resourcesPath;
  private String fileName;
  private String fileId;

  public PluginPathInfo() {
  }

  public PluginPathInfo(Path pluginPath, Path resourcesPath, String fileName, String fileId) {
    this.pluginPath = pluginPath;
    this.resourcesPath = resourcesPath;
    this.fileName = fileName;
    this.fileId = fileId;
  }

  public Path getPluginPath() {
    return pluginPath;
  }

  public void setPluginPath(Path pluginPath) {
    this.pluginPath = pluginPath;
  }

  public Path getResourcesPath() {
    return resourcesPath;
  }

  public void setResourcesPath(Path resourcesPath) {
    this.resourcesPath = resourcesPath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
}