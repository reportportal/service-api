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

package com.epam.reportportal.base.ws.converter.builders;

import static com.epam.reportportal.base.core.widget.content.loader.materialized.generator.AbstractViewGenerator.LAST_REFRESH;
import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.reportportal.base.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetOptions;
import com.epam.reportportal.base.model.widget.WidgetPreviewRQ;
import com.epam.reportportal.base.model.widget.WidgetRQ;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Creates dashboard widget entities from incoming widget definitions.
 *
 * @author Pavel Bortnik
 */
public class WidgetBuilder implements Supplier<Widget> {

  /**
   * Widget option keys that are managed by the server (materialized view bookkeeping). Values for
   * these keys coming from a client request are ignored; stored values are kept as is.
   */
  public static final Set<String> SERVER_MANAGED_OPTIONS = Set.of(VIEW_NAME, STATE, LAST_REFRESH);

  private Widget widget;

  public WidgetBuilder() {
    widget = new Widget();
  }

  public WidgetBuilder(Widget widget) {
    this.widget = widget;
  }

  public WidgetBuilder addWidgetRq(WidgetRQ widgetRQ) {
    ofNullable(widgetRQ.getName()).ifPresent(name -> widget.setName(name));
    widget.setDescription(widgetRQ.getDescription());

    ofNullable(widgetRQ.getContentParameters()).ifPresent(cp -> {
      ofNullable(cp.getWidgetOptions())
          .ifPresent(wo -> {
            WidgetOptions widgetOptions =
                ofNullable(widget.getWidgetOptions()).orElseGet(WidgetOptions::new);
            Map<String, Object> options =
                ofNullable(widgetOptions.getOptions()).orElseGet(LinkedHashMap::new);
            putClientOptions(options, wo);
            widgetOptions.setOptions(options);
            widget.setWidgetOptions(widgetOptions);
          });
      widget.setItemsCount(widgetRQ.getContentParameters().getItemsCount());
      widget.getContentFields().clear();
      widget.getContentFields().addAll(
          ofNullable(widgetRQ.getContentParameters().getContentFields()).orElse(
              Collections.emptyList()));
    });

    widget.setWidgetType(widgetRQ.getWidgetType());
    return this;
  }

  public WidgetBuilder addWidgetPreviewRq(WidgetPreviewRQ previewRQ) {
    WidgetOptions widgetOptions =
        ofNullable(widget.getWidgetOptions()).orElseGet(WidgetOptions::new);
    Map<String, Object> options =
        ofNullable(widgetOptions.getOptions()).orElseGet(LinkedHashMap::new);

    ofNullable(previewRQ.getContentParameters()).ifPresent(cp -> {
      ofNullable(cp.getWidgetOptions()).ifPresent(wo -> putClientOptions(options, wo));
      widget.setItemsCount(cp.getItemsCount());
      widget.getContentFields().clear();
      widget.getContentFields()
          .addAll(ofNullable(cp.getContentFields()).orElse(Collections.emptyList()));
    });
    widgetOptions.setOptions(options);
    widget.setWidgetOptions(widgetOptions);
    widget.setWidgetType(previewRQ.getWidgetType());
    return this;
  }

  public WidgetBuilder addProject(Long projectId) {
    Project project = new Project();
    project.setId(projectId);
    widget.setProject(project);
    return this;
  }

  public WidgetBuilder addFilters(Iterable<UserFilter> userFilters) {
    ofNullable(userFilters).ifPresent(it -> widget.setFilters(Sets.newLinkedHashSet(it)));
    return this;
  }

  public WidgetBuilder addOwner(String owner) {
    widget.setOwner(owner);
    return this;
  }

  public WidgetBuilder addOption(String key, Object value) {
    WidgetOptions widgetOptions = ofNullable(widget.getWidgetOptions()).orElseGet(() -> {
      WidgetOptions opts = new WidgetOptions();
      widget.setWidgetOptions(opts);
      return opts;
    });
    Map<String, Object> options = ofNullable(widgetOptions.getOptions()).orElseGet(() -> {
      LinkedHashMap<String, Object> opts = new LinkedHashMap<>();
      widgetOptions.setOptions(opts);
      return opts;
    });
    options.put(key, value);
    return this;
  }

  /**
   * Copies client-provided options into the target map, skipping
   * {@link #SERVER_MANAGED_OPTIONS server-managed keys} so a request can neither set nor change
   * them.
   */
  private static void putClientOptions(Map<String, Object> target,
      Map<String, Object> clientOptions) {
    clientOptions.forEach((key, value) -> {
      if (!SERVER_MANAGED_OPTIONS.contains(key)) {
        target.put(key, value);
      }
    });
  }

  @Override
  public Widget get() {
    return widget;
  }
}
