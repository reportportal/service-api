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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCase;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts internal DB model from/to DTO
 *
 * @author Pavel Bortnik
 */
public final class EmailConfigConverters {

    private EmailConfigConverters() {
        //static only
    }

    public final static Function<ProjectEmailConfigDTO, ProjectEmailConfig> FROM_RESOURCE = resource -> {
        ProjectEmailConfig db = new ProjectEmailConfig();
        db.setEmailCases(
                Optional.ofNullable(resource.getEmailCases())
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(EmailConfigConverters.FROM_CASE_RESOURCE)
                        .collect(Collectors.toList())
        );
        db.setEmailEnabled(resource.getEmailEnabled());
        db.setFrom(resource.getFrom());
        return db;
    };

    public final static Function<ProjectEmailConfig, ProjectEmailConfigDTO> TO_RESOURCE = db -> {
        ProjectEmailConfigDTO resource = new ProjectEmailConfigDTO();
        resource.setEmailCases(
                Optional.ofNullable(db.getEmailCases())
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(EmailConfigConverters.TO_CASE_RESOURCE)
                        .collect(Collectors.toList())
        );
        resource.setEmailEnabled(db.getEmailEnabled());
        resource.setFrom(resource.getFrom());
        return resource;
    };

    public final static Function<EmailSenderCaseDTO, EmailSenderCase> FROM_CASE_RESOURCE = resource -> {
        EmailSenderCase db = new EmailSenderCase();
        db.setLaunchNames(resource.getLaunchNames());
        db.setRecipients(resource.getRecipients());
        db.setSendCase(resource.getSendCase());
        db.setTags(resource.getTags());
        return db;
    };

    public final static Function<EmailSenderCase, EmailSenderCaseDTO> TO_CASE_RESOURCE = db -> {
        EmailSenderCaseDTO resource = new EmailSenderCaseDTO();
        resource.setLaunchNames(db.getLaunchNames());
        resource.setTags(db.getTags());
        resource.setSendCase(db.getSendCase());
        resource.setRecipients(db.getRecipients());
        return resource;
    };
}
