/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.store.database.entity.dashboard;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Pavel Bortnik
 */
@Embeddable
public class DashboardWidgetId implements Serializable {

	@Column(name = "dashboard_id")
	private Long dashboardId;

	@Column(name = "widget_id")
	private Long widgetId;

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	public Long getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Long widgetId) {
		this.widgetId = widgetId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DashboardWidgetId that = (DashboardWidgetId) o;

		if (dashboardId != null ? !dashboardId.equals(that.dashboardId) : that.dashboardId != null) {
			return false;
		}
		return widgetId != null ? widgetId.equals(that.widgetId) : that.widgetId == null;
	}

	@Override
	public int hashCode() {
		int result = dashboardId != null ? dashboardId.hashCode() : 0;
		result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
		return result;
	}
}
