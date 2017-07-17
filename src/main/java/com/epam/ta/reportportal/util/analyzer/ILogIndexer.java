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

package com.epam.ta.reportportal.util.analyzer;

import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;

/**
 * Service for indexing log content in some external storage for further search/analysis.
 *
 * @author Ivan Sharamet
 *
 */
public interface ILogIndexer {

    /**
     * Index logs for all given test items within launch
     *
     * @param launchId
     *            - ID of the launch
     * @param testItems
     *            - list of test items, for which log indexing will be performed
     */
    void indexLogs(String launchId, List<TestItem> testItems);

    /**
     * Index all logs in repository
     */
    void indexAllLogs();
}
