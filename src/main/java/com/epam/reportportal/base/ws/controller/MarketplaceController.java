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
import com.epam.reportportal.base.core.marketplace.handler.GetMarketplacePluginDetailHandler;
import com.epam.reportportal.base.core.marketplace.handler.InstallMarketplacePluginHandler;
import com.epam.reportportal.base.core.marketplace.handler.MarketplaceLicenceHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallResource;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceLicenceResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginDetailResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  private final GetMarketplacePluginDetailHandler getMarketplacePluginDetailHandler;
  private final InstallMarketplacePluginHandler installMarketplacePluginHandler;
  private final MarketplaceLicenceHandler marketplaceLicenceHandler;

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
   * One plugin's marketplace page: description, latest version, version history, changelog,
   * screenshots and the registry's advisory, block and removal state.
   *
   * <p>Guarded like {@link #getCatalogue} and not like {@link #install} — this is the same read of
   * the same catalogue, one plugin at a time, and nothing here changes what runs on the instance.
   *
   * <p>A removed plugin answers 200 with {@code removed} set, not 404. It is gone from the
   * marketplace and still running here, and the page has to be able to say both.
   *
   * <p>An unreachable registry answers 200 with {@code registry.status} OFFLINE and no
   * registry-derived content, exactly as the catalogue does. The UI has one rule for whether a
   * marketplace-sourced signal may be believed, and it reads that envelope on both screens.
   *
   * @param registryId registry plugin id
   * @param user       the caller
   * @return the registry's view of that plugin
   */
  @GetMapping("/{registryId}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Get one marketplace plugin")
  public MarketplacePluginDetailResource getPlugin(@PathVariable("registryId") String registryId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getMarketplacePluginDetailHandler.getPluginDetail(registryId);
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

  /**
   * Stores the credentials an operator got from the registry when their entitlement was created.
   * Idempotent, so re-submitting after a key rotation is the same request.
   *
   * @param request customer id and base64 Ed25519 private key
   * @param user    the admin doing it
   * @return what {@link #getLicence} now answers
   */
  @PutMapping("/licence")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Set this instance's marketplace licence credentials")
  @PreAuthorize(IS_ADMIN)
  public MarketplaceLicenceResource setLicence(@RequestBody @Valid MarketplaceLicenceRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return marketplaceLicenceHandler.setLicence(request, user);
  }

  /**
   * Whether this instance holds licence credentials, and who it signs as.
   *
   * <p>The private key is not in the answer and there is no endpoint that returns it. A key is
   * written once and only ever read by the signer; an operator who has lost theirs asks the
   * registry to rotate it.
   *
   * @param user the admin asking
   * @return configured state and customer id
   */
  @GetMapping("/licence")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Whether marketplace licence credentials are configured")
  @PreAuthorize(IS_ADMIN)
  public MarketplaceLicenceResource getLicence(@AuthenticationPrincipal ReportPortalUser user) {
    return marketplaceLicenceHandler.getLicence();
  }

  /**
   * Removes the credentials, so premium plugins are locked again and a premium install is refused
   * as not configured. This is what an operator who pasted the wrong key, or whose entitlement
   * ended, needs; it is idempotent, so deleting when there is nothing configured succeeds.
   *
   * @param user the admin doing it
   * @return what {@link #getLicence} now answers
   */
  @DeleteMapping("/licence")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Remove this instance's marketplace licence credentials")
  @PreAuthorize(IS_ADMIN)
  public MarketplaceLicenceResource deleteLicence(@AuthenticationPrincipal ReportPortalUser user) {
    return marketplaceLicenceHandler.deleteLicence(user);
  }
}
