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

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @author Pavel_Bortnik
 */
public class ProjectInfoConverterTest {

    @Test
    public void testConvert() {
        Project project = new Project();
        project.setUsers(ImmutableList.<Project.UserConfig>builder().add(Project.UserConfig.newOne().withLogin(""))
                .build());
        project.setCreationDate(new Date());
        ProjectInfoResource resource = ProjectInfoConverter.TO_RESOURCE.apply(project);
        Assert.assertTrue(resource.getUsersQuantity() == project.getUsers().size());
        Assert.assertEquals(resource.getCreationDate(), project.getCreationDate());
        Assert.assertEquals(resource.getEntryType(), EntryType.INTERNAL.name());
        Assert.assertNull(resource.getProjectId());
    }

}