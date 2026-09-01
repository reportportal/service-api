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

package com.epam.reportportal.base.model.marketplace;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.epam.reportportal.base.core.configs.JacksonConfiguration;
import com.epam.reportportal.base.model.marketplace.catalogue.AvailablePluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.InstalledPluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.InstanceCapabilitiesResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceAdvisoryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceBlockedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceEntryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceRemovedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatusResource;
import com.epam.reportportal.base.model.marketplace.catalogue.UpdateAvailableResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceChangelogResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginDetailResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceVersionResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * The names service-api actually puts on the wire for the plugins page, pinned — and published, so
 * the consumer can eat them.
 *
 * <p>The UI destructures these responses by field name; nothing else does. Renaming or dropping
 * one compiles, passes every other test, and breaks a badge or a block silently. Pinning the names
 * here was never enough on its own: an expectation written beside the record only ever proves the
 * record equals itself, and while the UI fed its own tests hand-made objects the two services
 * could be, and were, wrong together.
 *
 * <p>So this test also writes the serialised responses out, into service-ui's
 * {@code controllers/plugins/__fixtures__}, and the UI's tests read those files instead of
 * inventing their own. The producer publishes the truth; a shape change here fails the consumer's
 * tests on the next run rather than in production.
 *
 * <p>Requests are published the same way, and for the same reason: the install and licence bodies
 * were two separate ideas of one shape, literals in the UI's saga tests on one side and records
 * here on the other, with nothing making them agree. The names go out with the constraints on them
 * — a consumer that learns {@code version} but not that it may not be blank still earns a 400.
 *
 * <p>And because a published copy is only a copy, a marker goes out with it: a hash over the
 * field-path lists, which the consumer asserts against a hash pinned in its own source. A stale
 * fixture directory is then a failing test on that side rather than silent drift.
 *
 * <p>When this fails: if the UI changed with you, update the list in the same commit; if it did
 * not, the change is a break and the field goes back.
 */
class MarketplaceWireContractTest {

  private final ObjectMapper objectMapper = new JacksonConfiguration().objectMapper();

  /** The bean validation the controller's {@code @Valid} runs, so the facts are the real ones. */
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /**
   * The consumer's directory. Overridable so a checkout laid out differently, or a CI job that
   * wants the fixtures somewhere else, does not have to move a repository to run this.
   */
  private static final Path CONSUMER = Path.of(System.getProperty("marketplace.fixtures.dir",
      "../service-ui/app/src/controllers/plugins")).toAbsolutePath().normalize();

  private static final Path FIXTURES = CONSUMER.resolve("__fixtures__");

  private static final String README = """
      # Marketplace wire fixtures

      Generated by `MarketplaceWireContractTest` in service-api
      (`src/test/java/com/epam/reportportal/base/model/marketplace/MarketplaceWireContractTest.java`).

      **Do not hand-edit.** Every file here is a real response body, produced by serialising real
      resource records with service-api's own ObjectMapper. Editing one by hand would put this
      directory back where it started: a set of invented fixtures that the UI's tests pass against
      and the running service does not match.

      To change a fixture, change the response in service-api and re-run:

          ./gradlew test --tests '*MarketplaceWireContractTest'

      Absent fields are absent on purpose: the service serialises with `NON_NULL`, so a `null` is
      never sent and the UI must treat "missing" and "null" as the same thing.

      | File | Route | State |
      | --- | --- | --- |
      | `catalogue.json` | `GET /v1/plugins` | registry online: installed matched, installed unmatched, installed removed, available, premium locked, advisory, blocked, update available |
      | `catalogue-offline.json` | `GET /v1/plugins` | registry unreachable: local rows only, no marketplace block, nothing available |
      | `plugin-detail.json` | `GET /v1/plugins/{registryId}` | registry online: manifest, version history, changelog, screenshots, advisory, blocked, premium locked |
      | `plugin-detail-removed.json` | `GET /v1/plugins/{registryId}` | registry online: tombstone only — removed from the marketplace, still running here |
      | `plugin-detail-offline.json` | `GET /v1/plugins/{registryId}` | registry unreachable: the envelope and nothing registry-derived |

      ## Request fixtures

      These are bodies the UI **sends**, not answers it receives. They are produced from the same
      records the controller deserialises into, and the test proves each file reads back into its
      record, so a body built from one of these is a body this service accepts.

      | File | Route | Body |
      | --- | --- | --- |
      | `install-request.json` | `POST /v1/plugins/{registryId}/install` | install, update and rollback are the same request — only `version` differs |
      | `licence-request.json` | `PUT /v1/plugins/licence` | the credentials an operator got from the registry |

      The `privateKey` in `licence-request.json` is 64 zero bytes in base64. It is a shape, not a
      credential, and the running service rejects it as not an Ed25519 key.

      ### The constraints go with the names

      `request-constraints.json` carries what the service enforces on those bodies, generated by
      running service-api's own bean validator over each field. A consumer that gets every field
      name right and sends `{"version": ""}` still gets a 400, so:

      * `mandatory` — the field is rejected when absent or `null`.
      * `refusesEmpty` / `refusesBlank` — `""` and `"   "` are rejected too, not stored as blanks.
      * `maxLength` / `refusesLonger` — the declared bound, and that exceeding it is rejected.
      * `onViolation` — what the caller gets: a 400 raised before the handler runs, so nothing is
        installed and nothing is stored.

      ## Is this copy stale?

      `contract-marker.json` holds `contractHash`: a SHA-256 over the field-path lists of every
      route above. Not a timestamp — a timestamp changes on every run and tells you nothing. This
      changes exactly when the wire shape changes.

      Assert it from a UI test against a hash pinned in the UI's own source:

      ```js
      import marker from 'controllers/plugins/__fixtures__/contract-marker.json';

      // Bump this together with the fixtures, in the same commit that regenerates them.
      const EXPECTED = '<the contractHash in contract-marker.json>';

      it('the checked-in fixtures are the shapes service-api publishes today', () => {
        expect(marker.contractHash).toBe(EXPECTED);
      });
      ```

      **A mismatch means one of two things**, and both are real:

      1. Someone regenerated the fixtures from a service-api whose wire shape changed. Read
         `routes` in the marker, fix the UI for the new shape, and update the pinned hash.
      2. This directory was never regenerated after service-api changed, so these files describe a
         service that no longer exists. Regenerate them.

      To regenerate, from a service-api checkout with service-ui beside it:

          ./gradlew test --tests '*MarketplaceWireContractTest'

      or, if the checkouts are laid out differently:

          ./gradlew test --tests '*MarketplaceWireContractTest' \\
            -Dmarketplace.fixtures.dir=/path/to/service-ui/app/src/controllers/plugins

      Nothing runs that command for you across the two repositories. Until a CI job does, the
      marker is what makes a stale copy visible.
      """;

  private static final String CATALOGUE_ROUTE = "GET /v1/plugins";
  private static final String PLUGIN_DETAIL_ROUTE = "GET /v1/plugins/{registryId}";
  private static final String INSTALL_ROUTE = "POST /v1/plugins/{registryId}/install";
  private static final String LICENCE_ROUTE = "PUT /v1/plugins/licence";

  /**
   * GET /v1/plugins. Nested objects are listed by path, because the UI reads them by path too.
   */
  private static final List<String> CATALOGUE_FIELDS = List.of(
      "available[].access",
      "instance.uploadAllowed",
      "available[].author",
      "available[].contactUrl",
      "available[].description",
      "available[].groupType",
      "available[].id",
      "available[].latestVersion",
      "available[].locked",
      "available[].name",
      "available[].tier",
      "installed[].enabled",
      "installed[].groupType",
      "installed[].integrationTypeId",
      "installed[].marketplace.access",
      "installed[].marketplace.author",
      "installed[].marketplace.advisory.attachedAt",
      "installed[].marketplace.advisory.severity",
      "installed[].marketplace.advisory.text",
      "installed[].marketplace.blocked.blockedAt",
      "installed[].marketplace.blocked.reason",
      "installed[].marketplace.blocked.version",
      "installed[].marketplace.description",
      "installed[].marketplace.latestVersion",
      "installed[].marketplace.locked",
      "installed[].marketplace.name",
      "installed[].marketplace.pluginId",
      "installed[].marketplace.removed.removalReason",
      "installed[].marketplace.removed.removed",
      "installed[].marketplace.removed.removedBy",
      "installed[].marketplace.tier",
      "installed[].marketplace.updateAvailable.version",
      "installed[].name",
      "installed[].version",
      "registry.host",
      "registry.status"
  );

  /**
   * GET /v1/plugins/{registryId}, across the published and the removed answer. It carries the same
   * {@code registry} envelope the catalogue does: the UI has one rule for whether a
   * marketplace-sourced signal may be believed, and that rule reads the envelope.
   */
  private static final List<String> PLUGIN_DETAIL_FIELDS = List.of(
      "advisory.attachedAt",
      "advisory.severity",
      "advisory.text",
      "blocked.blockedAt",
      "blocked.reason",
      "blocked.version",
      "changelog.lines[]",
      "changelog.version",
      "locked",
      "plugin.access",
      "plugin.author",
      "plugin.description",
      "plugin.id",
      "plugin.latestVersion",
      "plugin.name",
      "plugin.tier",
      "registry.host",
      "registry.status",
      "removed.removalReason",
      "removed.removed",
      "removed.removedBy",
      "screenshots[]",
      "versions[].blocked",
      "versions[].publishedAt",
      "versions[].version"
  );

  /** POST /v1/plugins/{registryId}/install — what the UI sends, not what it gets back. */
  private static final List<String> INSTALL_REQUEST_FIELDS = List.of("version");

  /** PUT /v1/plugins/licence. */
  private static final List<String> LICENCE_REQUEST_FIELDS = List.of("customerId", "privateKey");

  /**
   * The files the consumer imports by name. Checked in beside the fixtures themselves, because a
   * fixture that quietly stops being published leaves the UI importing a file that is merely stale
   * — or, once someone deletes it, one that is not there at all.
   */
  private static final List<String> PUBLISHED = List.of(
      "README.md",
      "catalogue-offline.json",
      "catalogue.json",
      "contract-marker.json",
      "install-request.json",
      "licence-request.json",
      "plugin-detail-offline.json",
      "plugin-detail-removed.json",
      "plugin-detail.json",
      "request-constraints.json"
  );

  private static final String HOST = "marketplace.reportportal.io";
  private static final Instant WHEN = Instant.parse("2026-03-12T10:15:30Z");
  private static final Instant EARLIER = Instant.parse("2026-01-05T12:00:00Z");

  private static RegistryStatusResource online() {
    return new RegistryStatusResource(RegistryStatus.ONLINE, HOST);
  }

  private static RegistryStatusResource offline() {
    return new RegistryStatusResource(RegistryStatus.OFFLINE, HOST);
  }

  private static MarketplaceAdvisoryResource advisory() {
    return new MarketplaceAdvisoryResource("high", "Leaks the API key into the log", WHEN);
  }

  private static MarketplaceRemovedResource removed() {
    return new MarketplaceRemovedResource(EARLIER, "Vendor withdrew it", "operator@rp.io");
  }

  /** The plugin whose installed version has both a badge and an update waiting. */
  private static InstalledPluginResource installedMatched() {
    return new InstalledPluginResource(7L, "jira", "1.5.2", true, "BTS",
        new MarketplaceEntryResource("plugin-bts-jira", "Jira Cloud",
            "Post and link Jira Cloud issues from a failed test item.", "Atlassian", "premium",
            "official",
            "1.6.0", new UpdateAvailableResource("1.6.0"), advisory(),
            new MarketplaceBlockedResource("1.5.2", WHEN, "Signed with a revoked key"), null,
            true));
  }

  /** Matched, current, nothing wrong with it — the row every badge rule must leave alone. */
  private static InstalledPluginResource installedClean() {
    return new InstalledPluginResource(8L, "rally", "5.0.0", true, "BTS",
        new MarketplaceEntryResource("plugin-bts-rally", "Rally", "Rally work-item integration.",
            "Broadcom", "public", "official", "5.0.0", null, null, null, null, false));
  }

  /**
   * Removed from the marketplace, still running here: a tombstone on an otherwise bare block.
   * Name and description are absent along with everything else — a removed plugin has no
   * catalogue entry left to read them from, so the row falls back to the local name.
   */
  private static InstalledPluginResource installedRemoved() {
    return new InstalledPluginResource(9L, "gitlab", "2.1.0", false, "BTS",
        new MarketplaceEntryResource("plugin-bts-gitlab", null, null, null, null, null, null, null,
            null, null, removed(), false));
  }

  /**
   * Installed here and not matched to any registry entry. The block is absent, and with it the
   * registry id — there is no known registry id for a plugin the registry was never able to
   * confirm, so there is nowhere else on the row for one to sit.
   */
  private static InstalledPluginResource installedUnmatched() {
    return new InstalledPluginResource(10L, "custom-scanner", "0.9.0", true, "OTHER", null);
  }

  private static InstanceCapabilitiesResource uploadAllowed() {
    return new InstanceCapabilitiesResource(true);
  }

  private static MarketplaceCatalogueResource catalogue() {
    return new MarketplaceCatalogueResource(online(), uploadAllowed(),
        List.of(installedMatched(), installedClean(), installedRemoved(), installedUnmatched()),
        List.of(
            new AvailablePluginResource("plugin-notify-slack", "Slack", "2.0.0",
                "Posts a message when a launch finishes", "ReportPortal", null, "NOTIFICATION",
                "public", "official", false),
            new AvailablePluginResource("plugin-bts-azure", "Azure DevOps", "1.2.0",
                "Tracks issues in Azure Boards", "Microsoft", "https://reportportal.io/contact",
                "BTS", "premium", "verified", true)));
  }

  /**
   * The registry could not be reached. The local rows survive, stripped of every marketplace-
   * sourced claim, and nothing is offered for install.
   */
  private static MarketplaceCatalogueResource offlineCatalogue() {
    // Upload stays allowed with the registry down: it is the escape valve for exactly this.
    return new MarketplaceCatalogueResource(offline(), uploadAllowed(),
        List.of(new InstalledPluginResource(7L, "jira", "1.5.2", true, "BTS", null),
            installedUnmatched()),
        List.of());
  }

  private static MarketplacePluginDetailResource pluginDetail() {
    return new MarketplacePluginDetailResource(online(),
        new MarketplacePluginResource("plugin-bts-jira", "Jira", "Tracks issues in Jira",
            "Atlassian", "1.6.0", "premium", "official"),
        List.of(new MarketplaceVersionResource("1.6.0", WHEN, false),
            new MarketplaceVersionResource("1.5.2", EARLIER, true)),
        new MarketplaceChangelogResource("1.6.0",
            List.of("Fixed a crash on an empty summary", "Dropped the legacy field")),
        List.of("https://cdn.rp.io/jira/1.png", "https://cdn.rp.io/jira/2.png"),
        advisory(),
        new MarketplaceBlockedResource("1.6.0", WHEN, "Signed with a revoked key"),
        null,
        true);
  }

  /** The registry answered with a tombstone: the id survives, the manifest does not. */
  private static MarketplacePluginDetailResource removedPluginDetail() {
    return new MarketplacePluginDetailResource(online(),
        new MarketplacePluginResource("plugin-bts-gitlab", null, null, null, null, null, null),
        List.of(), null, List.of(), null, null, removed(), false);
  }

  private static MarketplacePluginDetailResource offlinePluginDetail() {
    return new MarketplacePluginDetailResource(offline(), null, List.of(), null, List.of(), null,
        null, null, false);
  }

  /** Every fixture, by the file name the consumer imports it under. */
  private static Map<String, Object> fixtures() {
    var fixtures = new LinkedHashMap<String, Object>();
    fixtures.put("catalogue.json", catalogue());
    fixtures.put("catalogue-offline.json", offlineCatalogue());
    fixtures.put("plugin-detail.json", pluginDetail());
    fixtures.put("plugin-detail-removed.json", removedPluginDetail());
    fixtures.put("plugin-detail-offline.json", offlinePluginDetail());
    return fixtures;
  }

  // --- requests ---------------------------------------------------------------------------------
  // The response fixtures cured one half of the disease. The other half is that the UI's outgoing
  // bodies were pinned by literals in its saga tests while this service pinned its own records, and
  // nothing made the two agree. So the request shapes are published too, from the same records the
  // controller deserialises into — together with the constraints on them, because a consumer that
  // learns the field name but not that `version` may not be blank still earns a 400 in production.

  /** Obviously not a real key: 64 zero bytes, base64. It is a shape, not a credential. */
  private static final String PLACEHOLDER_KEY = Base64.getEncoder().encodeToString(new byte[64]);

  private static MarketplaceInstallRQ installRequest() {
    return new MarketplaceInstallRQ("1.6.0");
  }

  private static MarketplaceLicenceRQ licenceRequest() {
    return new MarketplaceLicenceRQ("acme-gmbh", PLACEHOLDER_KEY);
  }

  /** One request field, and how to build the request carrying a given value in that field. */
  private record RequestField(String name, Function<String, Object> carrying) {

  }

  /** A request body this service accepts: what it is called, where it goes, what it must obey. */
  private record RequestShape(String fixture, String route, Class<?> type, Object example,
                              List<RequestField> fields) {

  }

  private static List<RequestShape> requests() {
    return List.of(
        new RequestShape("install-request.json", INSTALL_ROUTE, MarketplaceInstallRQ.class,
            installRequest(),
            List.of(new RequestField("version", MarketplaceInstallRQ::new))),
        new RequestShape("licence-request.json", LICENCE_ROUTE, MarketplaceLicenceRQ.class,
            licenceRequest(),
            List.of(new RequestField("customerId", v -> new MarketplaceLicenceRQ(v,
                PLACEHOLDER_KEY)),
                new RequestField("privateKey", v -> new MarketplaceLicenceRQ("acme-gmbh", v)))));
  }

  /** What the caller gets when a constraint below is broken. Published, because it is the point. */
  private static final String ON_VIOLATION =
      "400 Bad Request. The body is refused by @Valid before the handler runs, so nothing is"
          + " installed and nothing is stored.";

  /**
   * The constraints, run rather than described: each fact below is produced by handing the real
   * validator a body with that value in that field and seeing whether it comes back rejected.
   * Drop a {@code @NotBlank} and the published file says so.
   */
  private Map<String, Object> requestConstraints() {
    var published = new LinkedHashMap<String, Object>();
    for (var request : requests()) {
      var fields = new LinkedHashMap<String, Object>();
      for (var field : request.fields()) {
        var facts = new LinkedHashMap<String, Object>();
        facts.put("mandatory", refuses(field, null));
        facts.put("refusesEmpty", refuses(field, ""));
        facts.put("refusesBlank", refuses(field, "   "));
        maxLength(request.type(), field.name()).ifPresent(max -> {
          facts.put("maxLength", max);
          facts.put("refusesLonger", refuses(field, "x".repeat(max + 1)));
        });
        fields.put(field.name(), facts);
      }
      var shape = new LinkedHashMap<String, Object>();
      shape.put("route", request.route());
      shape.put("onViolation", ON_VIOLATION);
      shape.put("fields", fields);
      published.put(request.fixture(), shape);
    }
    return published;
  }

  /** Whether a body carrying {@code value} in this field is refused because of that field. */
  private boolean refuses(RequestField field, String value) {
    return validator.validate(field.carrying().apply(value)).stream()
        .anyMatch(violation -> violation.getPropertyPath().toString().equals(field.name()));
  }

  /**
   * The declared bound, read off the record's backing field so the published number cannot drift
   * from the annotation. Not off the record component: {@code @Size} does not target
   * RECORD_COMPONENT, so it is propagated to the field and not kept on the component.
   */
  private static Optional<Integer> maxLength(Class<?> type, String field) {
    try {
      return Optional.ofNullable(type.getDeclaredField(field).getAnnotation(Size.class))
          .map(Size::max);
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(type.getSimpleName() + " has no field '" + field + "'", e);
    }
  }

  /**
   * What the published constraints must say. Pinned here as well as generated, so that loosening a
   * rule on a record is a reviewed two-file diff and not a fixture that quietly changes under the
   * consumer.
   */
  private static final String REQUEST_CONSTRAINTS = """
      {
        "install-request.json": {
          "route": "POST /v1/plugins/{registryId}/install",
          "onViolation": "%s",
          "fields": {
            "version": { "mandatory": true, "refusesEmpty": true, "refusesBlank": true }
          }
        },
        "licence-request.json": {
          "route": "PUT /v1/plugins/licence",
          "onViolation": "%s",
          "fields": {
            "customerId": { "mandatory": true, "refusesEmpty": true, "refusesBlank": true,
                            "maxLength": 255, "refusesLonger": true },
            "privateKey": { "mandatory": true, "refusesEmpty": true, "refusesBlank": true,
                            "maxLength": 512, "refusesLonger": true }
          }
        }
      }""".formatted(ON_VIOLATION, ON_VIOLATION);

  // --- staleness --------------------------------------------------------------------------------

  /**
   * A fingerprint of every shape published here, so a consumer holding an old copy can say so.
   * Over the field-path lists and not over a timestamp: a timestamp changes on every run and
   * proves only that someone ran the test, while this changes exactly when the wire changes.
   */
  private static final String CONTRACT_HASH =
      "74cb316e28f6344062ac99cd40d40de8dd141e4ce26c044b786dc11654bd43a8";

  private static final String HASH_ALGORITHM =
      "SHA-256, hex, over the routes below in the order given: the route on a line of its own,"
          + " then one line per field path.";

  /** Every route's field paths, taken from the records rather than from the list beside them. */
  private SortedMap<String, TreeSet<String>> shapes() {
    var shapes = new TreeMap<String, TreeSet<String>>();
    shapes.put(CATALOGUE_ROUTE, paths(catalogue()));
    shapes.put(PLUGIN_DETAIL_ROUTE, paths(pluginDetail(), removedPluginDetail()));
    requests().forEach(request -> shapes.put(request.route(), paths(request.example())));
    return shapes;
  }

  private String contractHash() {
    var canonical = new StringBuilder();
    shapes().forEach((route, paths) -> {
      canonical.append(route).append('\n');
      paths.forEach(path -> canonical.append(path).append('\n'));
    });
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest(canonical.toString().getBytes(UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }

  /** The marker file itself: the hash, how it was made, and the lists it was made from. */
  private Map<String, Object> marker() {
    var marker = new LinkedHashMap<String, Object>();
    marker.put("contractHash", contractHash());
    marker.put("algorithm", HASH_ALGORITHM);
    marker.put("routes", shapes());
    return marker;
  }

  /** Everything written into the consumer's directory, by the file name it is imported under. */
  private Map<String, Object> published() {
    var published = new LinkedHashMap<String, Object>(fixtures());
    requests().forEach(request -> published.put(request.fixture(), request.example()));
    published.put("request-constraints.json", requestConstraints());
    published.put("contract-marker.json", marker());
    return published;
  }

  @Test
  void theCatalogueEmitsExactlyTheFieldNamesTheUiReads() {
    assertFields(CATALOGUE_FIELDS, CATALOGUE_ROUTE);
  }

  @Test
  void thePluginDetailEmitsExactlyTheFieldNamesTheUiReads() {
    assertFields(PLUGIN_DETAIL_FIELDS, PLUGIN_DETAIL_ROUTE);
  }

  @Test
  void theInstallRequestTakesExactlyTheFieldNamesTheUiSends() {
    assertFields(INSTALL_REQUEST_FIELDS, INSTALL_ROUTE);
  }

  @Test
  void theLicenceRequestTakesExactlyTheFieldNamesTheUiSends() {
    assertFields(LICENCE_REQUEST_FIELDS, LICENCE_ROUTE);
  }

  /**
   * A request fixture is only worth publishing if this service can read it back: the UI copies it
   * onto the wire, and the controller has to turn those bytes into the record.
   */
  @Test
  void theRequestFixturesAreBodiesThisServiceCanRead() throws IOException {
    for (var request : requests()) {
      var body = objectMapper.writeValueAsString(request.example());
      assertEquals(request.example(), objectMapper.readValue(body, request.type()),
          request.fixture() + " does not read back into " + request.type().getSimpleName());
      assertTrue(validator.validate(request.example()).isEmpty(),
          request.fixture() + " is published as a body a consumer may send, so it must be valid");
    }
  }

  /**
   * Field names alone are half a contract. A consumer that sends {@code {"version": ""}} has every
   * name right and still gets a 400, so what may not be blank, and how long a value may be, is
   * published beside the names.
   */
  @Test
  void theRequestConstraintsAConsumerMustRespectArePublished() throws IOException {
    assertEquals(objectMapper.readTree(REQUEST_CONSTRAINTS),
        objectMapper.valueToTree(requestConstraints()),
        "the validation rules on the request records changed."
            + " Update service-ui in the same commit, or put the constraint back.");
  }

  /**
   * The consumer's copy is a copy: nothing on the UI side can otherwise tell whether it still
   * matches the records here. The marker gives it something to assert against, so a stale fixture
   * directory is a failing test rather than silent drift.
   */
  @Test
  void theContractMarkerFingerprintsEveryShapeThePageDependsOn() {
    assertEquals(
        new TreeSet<>(List.of(CATALOGUE_ROUTE, INSTALL_ROUTE, LICENCE_ROUTE, PLUGIN_DETAIL_ROUTE)),
        shapes().keySet(), "the marker must cover every route the plugins page uses");
    assertEquals(CONTRACT_HASH, contractHash(),
        "the published wire shapes changed, so every checked-in fixture in service-ui is stale."
            + " Re-run this test to republish, then update this constant and the one service-ui"
            + " pins, in the same commit.");
  }

  /**
   * The registry id is a marketplace-sourced fact, so it lives inside the marketplace block and
   * nowhere else. With no block there is no known registry id, and the row says so by carrying
   * none — an invariant of the shape rather than something two places have to keep in step.
   */
  @Test
  void anInstalledRowWithNoMarketplaceBlockCarriesNoRegistryIdAnywhere() {
    var unmatched = objectMapper.valueToTree(installedUnmatched());

    assertTrue(unmatched.path("marketplace").isMissingNode(),
        "an unmatched row must carry no marketplace block");
    assertEquals(List.of(), pathsEndingIn(unmatched, "pluginId"),
        "with no marketplace block there is no known registry id, so none may be on the row");

    // And when there is a block, the id is in it — the one place the UI is told to read.
    assertEquals(List.of("marketplace.pluginId"),
        pathsEndingIn(objectMapper.valueToTree(installedMatched()), "pluginId"));
  }

  /**
   * Offline is a 200 that says the registry could not be asked, not a page with blanks in it.
   * Every registry-derived field is absent, so nothing on either screen can be believed by
   * accident.
   */
  @Test
  void theOfflineEnvelopesCarryTheStatusAndTheHostAndNothingRegistryDerived() {
    var page = objectMapper.valueToTree(offlinePluginDetail());
    assertEquals("OFFLINE", page.path("registry").path("status").asText());
    assertEquals(HOST, page.path("registry").path("host").asText());
    assertEquals(new TreeSet<>(List.of("locked", "registry", "screenshots", "versions")),
        new TreeSet<>(names(page)));
    assertTrue(page.path("versions").isEmpty());
    assertTrue(page.path("screenshots").isEmpty());

    var list = objectMapper.valueToTree(offlineCatalogue());
    assertEquals("OFFLINE", list.path("registry").path("status").asText());
    assertEquals(HOST, list.path("registry").path("host").asText());
    assertTrue(list.path("available").isEmpty());
    assertEquals(List.of(), pathsEndingIn(list, "marketplace"),
        "an offline catalogue can vouch for nothing, so no row carries a marketplace block");
  }

  /**
   * Publishes the fixtures the UI's tests read. They are written from the same records the
   * assertions above run over, through the service's own mapper, so what the consumer eats is
   * what this service emits.
   */
  @Test
  void theSerialisedResponsesArePublishedForTheUiToConsume() throws IOException {
    assumeTrue(Files.isDirectory(CONSUMER),
        "service-ui is not checked out at " + CONSUMER + "; nothing to publish to");
    Files.createDirectories(FIXTURES);
    Files.writeString(FIXTURES.resolve("README.md"), README);

    var writer = objectMapper.writerWithDefaultPrettyPrinter();
    for (var fixture : published().entrySet()) {
      Files.writeString(FIXTURES.resolve(fixture.getKey()),
          writer.writeValueAsString(fixture.getValue()) + "\n");
    }

    // The directory holds exactly what the consumer imports: no fixture silently dropped, and
    // none left behind by a rename.
    try (var listing = Files.list(FIXTURES)) {
      assertEquals(new TreeSet<>(PUBLISHED),
          listing.map(file -> file.getFileName().toString()).collect(toCollection(TreeSet::new)),
          FIXTURES + " does not hold exactly the files the UI imports");
    }

    // Read them back rather than trust the write: a fixture the consumer cannot parse, or one
    // that lost a field on the way out, is worse than none.
    for (var fixture : published().entrySet()) {
      var path = FIXTURES.resolve(fixture.getKey());
      assertTrue(Files.exists(path), path + " was not published");
      assertEquals(objectMapper.writeValueAsString(fixture.getValue()),
          objectMapper.writeValueAsString(objectMapper.readTree(Files.readString(path))),
          fixture.getKey() + " does not round-trip to the body it was made from");
    }

    // A request fixture has one more thing to prove than a response one: the file the UI copies
    // onto the wire has to deserialise into the record this service validates and acts on.
    for (var request : requests()) {
      assertEquals(request.example(),
          objectMapper.readValue(Files.readString(FIXTURES.resolve(request.fixture())),
              request.type()),
          request.fixture() + " does not deserialise into " + request.type().getSimpleName());
    }
  }

  /**
   * Compares the paths a route's records actually emitted with the checked-in list, naming what
   * appeared and what went missing.
   */
  private void assertFields(List<String> expected, String route) {
    var actual = shapes().get(route);
    var missing = new ArrayList<>(expected);
    missing.removeAll(actual);
    var unexpected = new ArrayList<>(actual);
    unexpected.removeAll(expected);
    assertEquals(new TreeSet<>(expected), actual,
        route + " no longer emits the fields the UI reads."
            + " Dropped or renamed: " + missing + ". Not in the contract: " + unexpected
            + ". Update service-ui in the same commit, or put the field back.");
  }

  /** Every leaf path the given resources emit between them. */
  private TreeSet<String> paths(Object... resources) {
    var paths = new TreeSet<String>();
    for (var resource : resources) {
      collect(objectMapper.valueToTree(resource), "", paths);
    }
    return paths;
  }

  /**
   * Every leaf path of the serialised tree. An array contributes {@code []} once rather than one
   * path per element: the contract is about names, not about how many rows a fixture happens to
   * carry. An empty array contributes nothing — it names no leaves, and every array here is
   * populated in at least one of the answers a route can give.
   */
  private static void collect(JsonNode node, String path, TreeSet<String> into) {
    if (node.isObject()) {
      node.properties().forEach(field ->
          collect(field.getValue(), path.isEmpty() ? field.getKey() : path + "." + field.getKey(),
              into));
    } else if (node.isArray()) {
      node.forEach(element -> collect(element, path + "[]", into));
    } else {
      into.add(path);
    }
  }

  /** Every path in the tree whose last segment is {@code name}, wherever it sits. */
  private static List<String> pathsEndingIn(JsonNode node, String name) {
    var found = new ArrayList<String>();
    walk(node, "", name, found);
    return found;
  }

  private static void walk(JsonNode node, String path, String name, List<String> found) {
    if (node.isObject()) {
      node.properties().forEach(field -> {
        var child = path.isEmpty() ? field.getKey() : path + "." + field.getKey();
        if (field.getKey().equals(name)) {
          found.add(child);
        }
        walk(field.getValue(), child, name, found);
      });
    } else if (node.isArray()) {
      node.forEach(element -> walk(element, path + "[]", name, found));
    }
  }

  private static List<String> names(JsonNode node) {
    var names = new ArrayList<String>();
    node.fieldNames().forEachRemaining(names::add);
    return names;
  }
}
