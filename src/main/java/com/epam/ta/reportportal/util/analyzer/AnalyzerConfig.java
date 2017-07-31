/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.util.analyzer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for auto analyzer.
 */

@Component
@ConfigurationProperties("rp.issue.analyzer")
public class AnalyzerConfig {

    private int rate;

    private int depth;

    private int logsLimit;

    private int itemsLimit;

    private int logSizeLimit;

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getLogsLimit() {
        return logsLimit;
    }

    public void setLogsLimit(int logsLimit) {
        this.logsLimit = logsLimit;
    }

    public int getItemsLimit() {
        return itemsLimit;
    }

    public void setItemsLimit(int itemsLimit) {
        this.itemsLimit = itemsLimit;
    }

    public int getLogSizeLimit() {
        return logSizeLimit;
    }

    public void setLogSizeLimit(int logSizeLimit) {
        this.logSizeLimit = logSizeLimit;
    }
}
