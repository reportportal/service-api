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
package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.project.email.EmailSenderCaseDto;
import com.epam.ta.reportportal.database.entity.project.email.ProjectEmailConfigDto;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class EmailConfigDtoBuilder extends Builder<ProjectEmailConfigDto> {

    public EmailConfigDtoBuilder addProjectEmailConfigRes(ProjectEmailConfig projectEmailConfig) {
        ProjectEmailConfigDto config = getObject();

        if (null != projectEmailConfig.getEmailCases()) {
            List<EmailSenderCaseDto> emailSenderCases = projectEmailConfig.getEmailCases()
                    .stream().map(emailSenderCase -> {
                        EmailSenderCaseDto senderCase = new EmailSenderCaseDto();
                        senderCase.setLaunchNames(emailSenderCase.getLaunchNames());
                        senderCase.setRecipients(emailSenderCase.getRecipients());
                        senderCase.setSendCase(emailSenderCase.getSendCase());
                        senderCase.setTags(emailSenderCase.getTags());
                        return senderCase;
                    }).collect(Collectors.toList());
            config.setEmailSenderCaseDtos(emailSenderCases);
        }
        config.setEmailEnabled(projectEmailConfig.getEmailEnabled());
        config.setFrom(projectEmailConfig.getFrom());
        return this;
    }

    @Override
    protected ProjectEmailConfigDto initObject() {
        return new ProjectEmailConfigDto();
    }
}
