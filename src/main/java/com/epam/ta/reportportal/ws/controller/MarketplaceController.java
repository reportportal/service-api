/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.ta.reportportal.core.integration.plugin.PluginMarketPlaceHandler;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.marketplace.InstallPluginRQ;
import com.epam.ta.reportportal.model.marketplace.MarketplaceResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.update.PluginInfo;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing plugin marketplaces in ReportPortal. Provides endpoints for creating, updating, retrieving, and
 * deleting marketplaces.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
@RestController
@RequestMapping("/marketplaces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Marketplace", description = "Manages plugin marketplace operations")
public class MarketplaceController {

  private final PluginMarketPlaceHandler pluginMarketPlaceHandler;

  @GetMapping
  @Operation(summary = "Get all marketplace entries")
  @PreAuthorize(IS_ADMIN)
  public List<MarketplaceResource> getAllMarketplaces() {
    return pluginMarketPlaceHandler.getAllMarketplaces();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Add a new marketplace entry")
  @PreAuthorize(IS_ADMIN)
  public void addMarketplace(@RequestBody @Valid MarketplaceResource marketplaceResource) {
    pluginMarketPlaceHandler.addMarketPlace(marketplaceResource);
  }

  @PutMapping
  @Operation(summary = "Update an existing marketplace entry")
  @PreAuthorize(IS_ADMIN)
  public void updateMarketplace(@RequestBody @Valid MarketplaceResource marketplaceResource) {
    pluginMarketPlaceHandler.editMarketPlace(marketplaceResource);
  }

  @DeleteMapping("/{name}")
  @Operation(summary = "Delete a marketplace entry")
  @PreAuthorize(IS_ADMIN)
  public void deleteMarketplace(@PathVariable String name) {
    pluginMarketPlaceHandler.deleteMarketPlace(name);
  }

  @GetMapping("/plugins")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get List of Available plugins from marketplace")
  @PreAuthorize(IS_ADMIN)
  public List<PluginInfo> getAvailablePlugins() {
    return pluginMarketPlaceHandler.getAvailablePlugins();
  }

  @Transactional
  @PostMapping("/plugins")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Install plugin from marketplace")
  @PreAuthorize(IS_ADMIN)
  public EntryCreatedRS installFromMarketPlace(@RequestBody InstallPluginRQ installPluginRQ) {
    pluginMarketPlaceHandler.installPlugin(installPluginRQ.getPluginId(), installPluginRQ.getVersion());
    return new EntryCreatedRS(1L);
  }
}
