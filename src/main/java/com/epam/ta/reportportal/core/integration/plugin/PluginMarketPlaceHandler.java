package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.model.marketplace.MarketplaceResource;
import java.util.List;
import org.pf4j.update.PluginInfo;

public interface PluginMarketPlaceHandler {

  List<PluginInfo> getAvailablePlugins();

  void installPlugin(String pluginId, String version);

  List<MarketplaceResource> getAllMarketplaces();

  void addMarketPlace(MarketplaceResource marketplaceRs);

  void deleteMarketPlace(String marketplaceRs);

  void editMarketPlace(MarketplaceResource marketplaceRs);
}
