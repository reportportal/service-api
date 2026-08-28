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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.IS_ADMIN;

import com.epam.reportportal.base.core.marketplace.handler.GetMarketplaceCatalogueHandler;
import com.epam.reportportal.base.core.marketplace.handler.InstallMarketplacePluginHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The marketplace catalogue, at {@code /v1/plugins} — plural, and deliberately not the
 * {@code /v1/plugin} of {@link PluginController}, which stays exactly as it is.
 *
 * <p>Reading the catalogue is for any authenticated user, the same guard
 * {@link PluginController#getPlugins} carries: neither method declares {@code @PreAuthorize}, so
 * both are covered by the filter chain's rule that every {@code /**} request holds ROLE_USER.
 * Admin rights gate installing a plugin, not looking at the list.
 *
 * <p>Deliberately not transactional. The one database read is short, and wrapping the registry
 * call in a transaction would hold a pooled connection for as long as the registry takes to
 * answer.
 */
@RestController
@RequestMapping(value = "/v1/plugins")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Marketplace plugin catalogue")
public class MarketplaceController {

  private final GetMarketplaceCatalogueHandler getMarketplaceCatalogueHandler;
  private final InstallMarketplacePluginHandler installMarketplacePluginHandler;

  /**
   * Everything the Plugins page renders, offline state included, in one response.
   *
   * @param q        free-text filter, applied to installed and available alike
   * @param category registry category filter, applied to installed and available alike
   * @param user     the caller
   * @return the merged catalogue
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get the merged marketplace catalogue")
  public MarketplaceCatalogueResource getCatalogue(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "category", required = false) String category,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getMarketplaceCatalogueHandler.getCatalogue(q, category);
  }

  /**
   * Makes one registry version the active one. Install, update and rollback are the same request —
   * only the version differs — and all three change what code runs on the instance, which is why
   * this one carries {@link com.epam.reportportal.base.auth.permissions.Permissions#IS_ADMIN} while
   * reading the catalogue does not.
   *
   * <p>Not transactional on purpose: the flow downloads an artifact over the network, and holding
   * a pooled connection for that long would be paid for by every other request.
   *
   * @param registryId registry plugin id
   * @param request    the version to activate
   * @param user       the admin performing it
   * @return what is now active
   */
  @PostMapping("/{registryId}/install")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Install, update or roll back a marketplace plugin")
  @PreAuthorize(IS_ADMIN)
  public MarketplaceInstallResource install(@PathVariable("registryId") String registryId,
      @RequestBody @Valid MarketplaceInstallRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return installMarketplacePluginHandler.install(registryId, request, user);
  }
}
