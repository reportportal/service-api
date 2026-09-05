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

/**
 * Plugin author, part of the registry manifest fields.
 */
public record MarketplaceAuthor(String name, String email, String url) {

  /**
   * The author's name, or null when the registry named nobody.
   *
   * <p>Only the name crosses to the UI. The email and URL are contact details the screens do not
   * use, and putting an address on the wire for no consumer is how addresses end up scraped.
   *
   * @param author the registry's author block, may be null
   * @return the name, or null
   */
  public static String nameOf(MarketplaceAuthor author) {
    return author == null ? null : author.name();
  }
}
