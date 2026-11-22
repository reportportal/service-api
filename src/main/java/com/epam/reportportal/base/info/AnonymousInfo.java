/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.info;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The part of {@code /info} that a caller who is not signed in may see: what the web UI needs to draw the login,
 * registration and password restore pages and to start analytics before sign-in. Everything else stays for
 * authenticated callers.
 *
 * <p>This is an allow-list. Keys and nesting are those of the full response, so a client written for the full
 * response reads the subset without changes; whatever is not listed here, including a key a future contributor or
 * server setting adds, is left out.
 */
public final class AnonymousInfo {

  static final String BUILD = "build";
  static final String AUTH_EXTENSIONS = "authExtensions";
  static final String EXTENSIONS = "extensions";
  static final String SERVER_SETTINGS = "result";
  static final String ENVIRONMENT = "environment";
  static final String JOBS = "jobs";
  static final String DEMO_DATA_TRIGGER = "flushingDataTrigger";

  /**
   * Shown in the login page footer; {@code name} is how the UI tells that the API is up.
   */
  static final Set<String> BUILD_KEYS = Set.of("name", "version");

  /**
   * Server settings a signed-out user needs.
   */
  static final Set<String> SERVER_SETTINGS_KEYS = Set.of(
      // analytics (and the consent it depends on) starts before sign-in
      "server.analytics.all",
      // instance id sent with every analytics event, the login page's included
      "server.details.instance",
      // registration and password restore forms validate the password length
      "server.password.min.length"
  );

  /**
   * {@code RP_ENVIRONMENT_VARIABLE_*} values a signed-out user needs.
   */
  static final Set<String> ENVIRONMENT_KEYS = Set.of(
      // analytics is initialised with this id when the application loads, before sign-in
      "ga_measurement_id",
      // the login and account-removed pages show the privacy policy link for hosted instances
      "instance_type"
  );

  private AnonymousInfo() {
    //static only
  }

  /**
   * Reduces a full {@code /info} map to what an anonymous caller may see.
   *
   * <p>On a demo instance the login form is pre-filled with the demo credentials; the UI recognises a demo
   * instance by the presence of the data flushing job, so only that the job exists is kept, not when it runs.
   *
   * @param info the full {@code /info} map
   * @return a new map holding the allow-listed part only
   */
  public static Map<String, Object> of(Map<String, Object> info) {
    Map<String, Object> result = new LinkedHashMap<>();
    putSubset(result, BUILD, info.get(BUILD), BUILD_KEYS);
    if (info.get(AUTH_EXTENSIONS) != null) {
      result.put(AUTH_EXTENSIONS, info.get(AUTH_EXTENSIONS));
    }
    if (info.get(EXTENSIONS) instanceof Map<?, ?> extensions) {
      Map<String, Object> anonymousExtensions = new LinkedHashMap<>();
      putSubset(anonymousExtensions, SERVER_SETTINGS, extensions.get(SERVER_SETTINGS), SERVER_SETTINGS_KEYS);
      result.put(EXTENSIONS, anonymousExtensions);
    }
    putSubset(result, ENVIRONMENT, info.get(ENVIRONMENT), ENVIRONMENT_KEYS);
    if (info.get(JOBS) instanceof Map<?, ?> jobs && jobs.containsKey(DEMO_DATA_TRIGGER)) {
      result.put(JOBS, Map.of(DEMO_DATA_TRIGGER, Map.of()));
    }
    return result;
  }

  private static void putSubset(Map<String, Object> target, String key, Object source, Set<String> allowedKeys) {
    if (!(source instanceof Map<?, ?> sourceMap)) {
      return;
    }
    Map<String, Object> subset = new LinkedHashMap<>();
    sourceMap.forEach((k, v) -> {
      if (k instanceof String name && allowedKeys.contains(name)) {
        subset.put(name, v);
      }
    });
    target.put(key, subset);
  }
}
