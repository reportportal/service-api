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
package com.epam.ta.reportportal.events.handler;

/**
 * @author Pavel Bortnik
 */
public enum EventType {

    CREATE_DASHBOARD("create_dashboard"),
    UPDATE_DASHBOARD("update_dashboard"),
    DELETE_DASHBOARD("delete_dashboard"),
    CREATE_WIDGET("create_widget"),
    UPDATE_WIDGET("update_widget"),
    DELETE_WIDGET("delete_widget"),
    CREATE_FILTER("create_filter"),
    UPDATE_FILTER("update_filter"),
    DELETE_FILTER("delete_filter"),
    UPDATE_DEFECT("update_defect"),
    DELETE_DEFECT("delete_defect"),
    CREATE_BTS("create_bts"),
    UPDATE_BTS("update_bts"),
    DELETE_BTS("delete_bts"),
    START_LAUNCH("start_launch"),
    FINISH_LAUNCH("finish_launch"),
    DELETE_LAUNCH("delete_launch"),
    UPDATE_PROJECT("update_project"),
    POST_ISSUE("post_issue"),
    ATTACH_ISSUE("attach_issue"),
    UPDATE_ITEM("update_item"),
    CREATE_USER("create_user");

    private String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
