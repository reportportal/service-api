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
import java.util.HashMap;
import java.util.Map;

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

    static Activity.FieldValues createHistoryField(String oldValue, String newValue) {
        return Activity.FieldValues.newOne().withOldValue(oldValue).withNewValue(newValue);
    }

    static void processShare(Map<String, Activity.FieldValues> history, Shareable shareable, Boolean share) {
        if (null != share) {
            Boolean isShared = !shareable.getAcl().getEntries().isEmpty();
            if (!share.equals(isShared)) {
                history.put(SHARE, createHistoryField(isShared.toString(), share.toString()));
            }
        }
    }

    static void processName(Map<String, Activity.FieldValues> history, String oldName, String newName) {
        if (!Strings.isNullOrEmpty(newName) && !oldName.equals(newName)) {
            history.put(NAME, createHistoryField(oldName, newName));
        }
    }

    static void processDescription(HashMap<String, Activity.FieldValues> history, @Nullable String oldDescription,
                                   String newDescription) {
        if (null != newDescription && !newDescription.equals(oldDescription)) {
            history.put(DESCRIPTION, createHistoryField(oldDescription, newDescription));
        }
    }

}
