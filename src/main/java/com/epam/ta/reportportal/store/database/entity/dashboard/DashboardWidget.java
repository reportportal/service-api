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

import com.epam.ta.reportportal.store.database.entity.widget.Widget;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Pavel Bortnik
 */
@Entity
@Table(name = "dashboard_widget")
public class DashboardWidget implements Serializable {

	@EmbeddedId
	private DashboardWidgetId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("dashboard_id")
	private Dashboard dashboard;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("widget_id")
	private Widget widget;

	@Column(name = "widget_width")
	private int width;

	@Column(name = "widget_heigth")
	private int heigth;

	@Column(name = "widget_position_x")
	private int positionX;

	@Column(name = "widget_position_y")
	private int positionY;

	public DashboardWidgetId getId() {
		return id;
	}

	public void setId(DashboardWidgetId id) {
		this.id = id;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public Widget getWidget() {
		return widget;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeigth() {
		return heigth;
	}

	public void setHeigth(int heigth) {
		this.heigth = heigth;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}
}
