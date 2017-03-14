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

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;
import java.util.List;

import static com.epam.ta.reportportal.ws.converter.builders.BuilderTestsConstants.*;

public class ProjectSettingsResourceBuilderTest extends BaseTest{

    @Autowired
    private Provider<ProjectSettingsResourceBuilder> provider;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testBeanScope() {
        Assert.assertTrue("Test builder should be prototype bean because it's not stateless",
                applicationContext.isPrototype(applicationContext.getBeanNamesForType(ProjectSettingsResourceBuilder.class)[0]));
    }

    @Test
    public void addProjectSettings() throws Exception {
        ProjectSettingsResource actual = provider.get().addProjectSettings(getProject()).build();
        ProjectSettingsResource expected = getProjectSettingsResource();
        validate(expected, actual);
    }

    private void validate(ProjectSettingsResource expected, ProjectSettingsResource actual) {
        Assert.assertEquals(expected.getProjectId(), actual.getProjectId());
        Assert.assertEquals(expected.getSubTypes().size(), actual.getSubTypes().size());
    }

    private Project getProject() {
        Project project = new Project();
        project.setName(PROJECT);
        Project.Configuration configuration = new Project.Configuration();
        configuration.setSubTypes(ImmutableMap.<TestItemIssueType, List<StatisticSubType>>builder()
                .put(TestItemIssueType.AUTOMATION_BUG, ImmutableList.<StatisticSubType>builder()
                        .add(new StatisticSubType(ID, TYPE_REF, NAME, NAME, COLOR))
                        .build()).build());
        project.setConfiguration(configuration);
        return project;
    }

    private ProjectSettingsResource getProjectSettingsResource() {
        ProjectSettingsResource resource = new ProjectSettingsResource();
        resource.setProjectId(PROJECT);
        resource.setSubTypes(ImmutableMap.<String, List<IssueSubTypeResource>>builder()
                .put(TestItemIssueType.AUTOMATION_BUG.name(), ImmutableList.<IssueSubTypeResource>builder()
                        .add(new IssueSubTypeResource(ID, TYPE_REF, NAME, NAME, COLOR))
                        .build()).build());
        return resource;
    }
}