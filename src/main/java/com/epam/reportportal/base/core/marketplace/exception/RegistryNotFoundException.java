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

package com.epam.reportportal.base.core.marketplace.exception;

import java.util.Locale;

/**
 * HTTP 404 — a healthy registry answering that it holds no such entry. Its own type because it is
 * the opposite diagnosis from an unreachable registry: nothing is wrong with the network, the
 * caller asked for something that does not exist.
 */
public class RegistryNotFoundException extends RegistryResponseException {

  private final String pluginId;
  private final String version;
  private final Subject subject;

  /**
   * What the registry said was missing.
   */
  public enum Subject {
    /** The plugin id is unknown to the registry. */
    PLUGIN,
    /** The plugin exists, this version of it does not. */
    VERSION,
    /** The registry did not say which of the two. */
    UNSPECIFIED
  }

  /**
   * Creates the exception from a registry 404.
   *
   * @param pluginId        registry plugin id that was asked for
   * @param version         version that was asked for, null on routes that name no version
   * @param registryCode    {@code code} field of the error body, may be null
   * @param registryMessage {@code message} field of the error body, may be null
   */
  public RegistryNotFoundException(String pluginId, String version, String registryCode,
      String registryMessage) {
    super("Marketplace registry has no '" + pluginId + (version == null ? "" : ":" + version) + "'"
        + (registryMessage == null ? "" : ": " + registryMessage), 404, registryCode,
        registryMessage);
    this.pluginId = pluginId;
    this.version = version;
    this.subject = subjectOf(registryMessage);
  }

  /**
   * The registry answers the same {@code NOT_FOUND} code for an unknown plugin, an unknown version
   * and a route that cannot tell the two apart, so only its message distinguishes them. Anything
   * unrecognised stays {@link Subject#UNSPECIFIED} — an operator is better served by "one of these
   * two" than by a confident wrong half.
   */
  private static Subject subjectOf(String registryMessage) {
    var message = registryMessage == null ? "" : registryMessage.toLowerCase(Locale.ROOT);
    var mentionsPlugin = message.contains("plugin");
    var mentionsVersion = message.contains("version");
    if (mentionsVersion && !mentionsPlugin) {
      return Subject.VERSION;
    }
    if (mentionsPlugin && !mentionsVersion) {
      return Subject.PLUGIN;
    }
    return Subject.UNSPECIFIED;
  }

  public String getPluginId() {
    return pluginId;
  }

  public String getVersion() {
    return version;
  }

  public Subject getSubject() {
    return subject;
  }
}
