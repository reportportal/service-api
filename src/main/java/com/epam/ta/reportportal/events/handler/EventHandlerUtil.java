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

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.sharing.Shareable;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author pavel_bortnik
 */
public class EventHandlerUtil {

    public static final String SHARE = "share";
    public static final String UNSHARE = "unshare";
    static final String NAME = "name";
    static final String DESCRIPTION = "description";

    //for created or deleted widgets
    static final String EMPTY_FIELD = "";

    static Activity.FieldValues createHistoryField(String field, String oldValue, String newValue) {
        return Activity.FieldValues.newOne()
                .withField(field).withOldValue(oldValue)
                .withNewValue(newValue);
    }

    static void processShare(List<Activity.FieldValues> history, Shareable shareable, Boolean share) {
        if (null != share) {
            Boolean isShared = !shareable.getAcl().getEntries().isEmpty();
            if (!share.equals(isShared)) {
                history.add(createHistoryField(SHARE, isShared.toString(), share.toString()));
            }
        }
    }

    static void processName(List<Activity.FieldValues> history, String oldName, String newName) {
        if (!Strings.isNullOrEmpty(newName) && !oldName.equals(newName)) {
            history.add(createHistoryField(NAME, oldName, newName));
        }
    }

    static void processDescription(List<Activity.FieldValues> history, @Nullable String oldDescription,
                                   @Nullable String newDescription) {
        oldDescription = Strings.nullToEmpty(oldDescription);
        newDescription = Strings.nullToEmpty(newDescription);
        if (!newDescription.equals(oldDescription)) {
            history.add(createHistoryField(DESCRIPTION, oldDescription, newDescription));
        }
    }

}
