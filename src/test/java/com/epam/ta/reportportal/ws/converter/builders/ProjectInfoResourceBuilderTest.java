package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

/**
 * Created by Pavel_Bortnik on 3/14/2017.
 */
public class ProjectInfoResourceBuilderTest extends BaseTest{

    @Autowired
    private Provider<ProjectInfoResourceBuilder> provider;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testBeanScope(){
        Assert.assertTrue("Test builder should be prototype bean because it's not stateless",
                applicationContext.isPrototype(applicationContext.getBeanNamesForType(ProjectInfoResourceBuilder.class)[0]));
    }

    @Test
    public void testAddProject() throws Exception {
        ProjectInfoResource actual = provider.get().addProject(getProject()).build();
        ProjectInfoResource expected = getInfoResource();
        validateProjects(actual, expected);
    }

    @Test
    public void testLastRun(){
        ProjectInfoResource actual = provider.get().addProject(getProject())
                .addLastRun(BuilderTestsConstants.DATE_END).build();
        ProjectInfoResource expected = getInfoResource();
        expected.setLastRun(BuilderTestsConstants.DATE_END);
        validateProjects(expected,actual);
        Assert.assertEquals(actual.getLastRun(), expected.getLastRun());
    }

    @Test
    public void testLaunchesQuantity(){
        ProjectInfoResource actual = provider.get().addProject(getProject())
                .addLaunchesQuantity(BuilderTestsConstants.QUANTITY).build();
        ProjectInfoResource expected = getInfoResource();
        expected.setLaunchesQuantity(BuilderTestsConstants.QUANTITY);
        validateProjects(actual, expected);
        Assert.assertEquals(actual.getLaunchesQuantity(), expected.getLaunchesQuantity());
    }

    private ProjectInfoResource getInfoResource(){
        ProjectInfoResource resource = new ProjectInfoResource();
        resource.setUsersQuantity(BuilderTestsConstants.QUANTITY.intValue());
        resource.setProjectId(BuilderTestsConstants.ID);
        resource.setCreationDate(BuilderTestsConstants.DATE_START);
        resource.setEntryType(BuilderTestsConstants.ENTRY_TYPE);
        return resource;
    }

    private Project getProject(){
        Project project = new Project();
        project.setUsers(ImmutableMap.<String, Project.UserConfig>builder()
                .put(BuilderTestsConstants.NAME, new Project.UserConfig()).build());
        project.setName(BuilderTestsConstants.ID);
        project.setCreationDate(BuilderTestsConstants.DATE_START);
        Project.Configuration configuration = new Project.Configuration();
        configuration.setEntryType(EntryType.INTERNAL);
        project.setConfiguration(configuration);
        return project;
    }

    private void validateProjects(ProjectInfoResource expectedValue, ProjectInfoResource actualValue) {
        Assert.assertEquals(expectedValue.getUsersQuantity(), actualValue.getUsersQuantity());
        Assert.assertEquals(expectedValue.getProjectId(), actualValue.getProjectId());
        Assert.assertEquals(expectedValue.getCreationDate(), actualValue.getCreationDate());
        Assert.assertEquals(expectedValue.getEntryType(), actualValue.getEntryType());
    }

}