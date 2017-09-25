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

package com.epam.ta.reportportal.util.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents single item in indexing operation response.
 *
 * @author Ivan Sharamet
 *
 */
public class IndexRsItem {

    public static final int STATUS_UPDATED = 200;
    public static final int STATUS_CREATED = 201;

    @JsonProperty("index")
    private IndexRsIndex index;
    @JsonProperty("created")
    private boolean created;
    @JsonProperty("status")
    private int status;

    public IndexRsItem() {}

    public IndexRsIndex getIndex() {
        return index;
    }

    public void setIndex(IndexRsIndex index) {
        this.index = index;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean failed() {
        return status != STATUS_CREATED && status != STATUS_UPDATED;
    }
}
