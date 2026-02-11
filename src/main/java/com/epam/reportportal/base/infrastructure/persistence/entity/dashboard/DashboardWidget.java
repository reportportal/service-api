/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.dashboard;

import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Pavel Bortnik
 */
@Setter
@Getter
@Entity
@Table(name = "dashboard_widget")
public class DashboardWidget implements Serializable {

  @EmbeddedId
  private DashboardWidgetId id;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("dashboardId")
  private Dashboard dashboard;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("widgetId")
  private Widget widget;

  @Column(name = "widget_name")
  private String widgetName;

  @Column(name = "widget_owner")
  private String widgetOwner;

  @Column(name = "widget_type")
  private String widgetType;

  @Column(name = "is_created_on")
  private boolean createdOn;

  @Column(name = "widget_width")
  private int width;

  @Column(name = "widget_height")
  private int height;

  @Column(name = "widget_position_x")
  private int positionX;

  @Column(name = "widget_position_y")
  private int positionY;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DashboardWidget that = (DashboardWidget) o;

    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
