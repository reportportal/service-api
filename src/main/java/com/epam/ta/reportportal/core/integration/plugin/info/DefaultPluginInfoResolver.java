package com.epam.ta.reportportal.core.integration.plugin.info;

import com.epam.ta.reportportal.core.integration.plugin.file.PluginFileManager;
import com.epam.ta.reportportal.core.integration.plugin.validator.PluginInfoValidator;
import com.epam.ta.reportportal.core.integration.plugin.validator.exception.PluginValidationException;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DefaultPluginInfoResolver implements PluginInfoResolver {

  private static final Logger logger = LoggerFactory.getLogger(DefaultPluginInfoResolver.class);


  private final PluginDescriptorFinder pluginDescriptorFinder;
  private final List<PluginInfoValidator> pluginInfoValidators;
  private final PluginFileManager pluginFileManager;

  @Autowired
  public DefaultPluginInfoResolver(PluginDescriptorFinder pluginDescriptorFinder,
      List<PluginInfoValidator> pluginInfoValidators,
      PluginFileManager pluginFileManager) {
    this.pluginDescriptorFinder = pluginDescriptorFinder;
    this.pluginInfoValidators = pluginInfoValidators;
    this.pluginFileManager = pluginFileManager;
  }

  @Override
  public PluginInfo resolveInfo(Path pluginPath) {
    try {
      PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);
      final PluginInfo pluginInfo = new PluginInfo(pluginDescriptor.getPluginId(),
          pluginDescriptor.getVersion(), pluginPath);
      validateInfo(pluginInfo);
      return pluginInfo;
    } catch (PluginException e) {
      logger.error(e.getMessage(), e);
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
    }
  }


  private void validateInfo(PluginInfo pluginInfo) {
    pluginInfoValidators.forEach(v -> {
      try {
        v.validate(pluginInfo);
      } catch (PluginValidationException e) {
        pluginFileManager.delete(pluginInfo.getOriginalFilePath());
        throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
      }
    });
  }

}