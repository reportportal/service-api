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

package com.epam.ta.reportportal.core.item.merge.strategy;


import java.util.Arrays;
import java.util.Optional;

public enum MergeStrategyType {
    SUITE,
    TEST;

    public static MergeStrategyType fromValue(String value) {
        Optional<MergeStrategyType> optional = Arrays.stream(MergeStrategyType.values())
                .filter((t) -> value.equals(t.name()))
                .findFirst();
        return optional.isPresent() ? optional.get() : null;
    }
}
