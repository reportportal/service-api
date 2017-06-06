package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.google.common.collect.ImmutableMap;
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
        project.setUsers(ImmutableMap.<String, Project.UserConfig>builder().put("", new Project.UserConfig()).build());
        project.setCreationDate(new Date());
        ProjectInfoResource resource = ProjectInfoConverter.TO_RESOURCE.apply(project);
        Assert.assertTrue(resource.getUsersQuantity() == project.getUsers().size());
        Assert.assertEquals(resource.getCreationDate(), project.getCreationDate());
        Assert.assertEquals(resource.getEntryType(), EntryType.INTERNAL.name());
        Assert.assertNull(resource.getProjectId());
    }

}