/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.impl;

import static com.epam.ta.reportportal.auth.permissions.Permissions.CAN_READ_OBJECT;

import com.epam.ta.reportportal.auth.acl.ReportPortalAclService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Ivan Nikitsenka
 */
@Service
public class ShareWidgetHandler implements com.epam.ta.reportportal.core.widget.ShareWidgetHandler {

    @Autowired
    private WidgetRepository widgetRepository;

    @Autowired
    private ReportPortalAclService aclService;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public void shareWidget(String projectName, Long widgetId) {
        Widget widget = findById(widgetId);
        Project project = projectRepository.findByName(projectName)
            .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND));
        project.getUsers()
            .forEach(user -> aclService.addReadPermissions(widget, user.getUser().getLogin()));
    }

    @Override
    @PostAuthorize(CAN_READ_OBJECT)
    public Widget findById(Long widgetId){
        return widgetRepository.findById(widgetId)
            .orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND, widgetId));
    }

}
