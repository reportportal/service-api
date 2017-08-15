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

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.FilterCreatedEvent;
import com.epam.ta.reportportal.events.FilterDeletedEvent;
import com.epam.ta.reportportal.events.FilterUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.filter.CreateUserFilterRQ;
import com.epam.ta.reportportal.ws.model.filter.UpdateUserFilterRQ;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;
import static com.epam.ta.reportportal.events.handler.EventType.*;

/**
 * @author Pavel Bortnik
 */
@Component
public class UserFilterActivityHandler {

    private static final String USER_FILTER = "userFilter";

    private ActivityRepository activityRepository;

    @Autowired
    public UserFilterActivityHandler(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @EventListener
    public void onFilterCreate(FilterCreatedEvent event) {
        CreateUserFilterRQ userFilterRQ = event.getFilterRQ();
        Activity activityLog = new ActivityBuilder()
                .addActionType(CREATE_FILTER.getValue())
                .addObjectType(USER_FILTER)
                .addObjectName(userFilterRQ.getName())
                .addProjectRef(event.getProjectRef())
                .addUserRef(event.getCreatedBy())
                .addLoggedObjectRef(event.getFilterId())
                .addHistory(ImmutableMap.<String, Activity.FieldValues>builder()
                        .put(NAME, createHistoryField(EMPTY_FIELD, userFilterRQ.getName())).build())
                .build();
        activityRepository.save(activityLog);
    }

    @EventListener
    public void onFilterUpdate(FilterUpdatedEvent event) {
        UserFilter userFilter = event.getUserFilter();
        UpdateUserFilterRQ updateUserFilterRQ = event.getUpdateUserFilterRQ();
        HashMap<String, Activity.FieldValues> history = new HashMap<>();
        if (userFilter != null) {
            processShare(history, userFilter, updateUserFilterRQ.getShare());
            processName(history, userFilter.getName(), updateUserFilterRQ.getName());
            processDescription(history, userFilter.getDescription(), updateUserFilterRQ.getDescription());
            if (!history.isEmpty()) {
                Activity activityLog = new ActivityBuilder()
                        .addProjectRef(userFilter.getProjectName())
                        .addObjectType(USER_FILTER)
                        .addActionType(UPDATE_FILTER.getValue())
                        .addLoggedObjectRef(userFilter.getId())
                        .addUserRef(event.getUpdatedBy())
                        .addHistory(history)
                        .build();
                activityRepository.save(activityLog);
            }
        }
    }

    @EventListener
    public void onFilterDelete(FilterDeletedEvent event) {
        UserFilter before = event.getBefore();
        Activity activityLog = new ActivityBuilder()
                .addActionType(DELETE_FILTER.getValue())
                .addObjectType(USER_FILTER)
                .addObjectName(before.getName())
                .addProjectRef(before.getProjectName())
                .addUserRef(event.getRemovedBy())
                .addHistory(ImmutableMap.<String, Activity.FieldValues>builder()
                        .put(NAME, createHistoryField(before.getName(), EMPTY_FIELD)).build())
                .build();
        activityRepository.save(activityLog);
    }
}
