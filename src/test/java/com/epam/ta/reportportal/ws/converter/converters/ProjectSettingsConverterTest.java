package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel_Bortnik
 */
public class ProjectSettingsConverterTest {

    @Test
    public void testConvert() {
        Project project = new Project();
        Project.Configuration configuration = new Project.Configuration();
        StatisticSubType subType = new StatisticSubType();
        subType.setHexColor("#ec3900");
        subType.setLocator("PB001");
        subType.setLongName("Product bug");
        subType.setShortName("PB");
        subType.setTypeRef("PRODUCT_BUG");
        configuration.setSubTypes(ImmutableMap.<TestItemIssueType, List<StatisticSubType>>builder()
                .put(TestItemIssueType.PRODUCT_BUG, ImmutableList.<StatisticSubType>builder().add(subType).build()).build());
        configuration.setStatisticsCalculationStrategy(StatisticsCalculationStrategy.TEST_BASED);
        project.setConfiguration(configuration);

        Map<TestItemIssueType, List<StatisticSubType>> subTypes = configuration.getSubTypes();

        ProjectSettingsResource resource = ProjectSettingsConverter.TO_RESOURCE.apply(project);
        Assert.assertNull(resource.getProjectId());
        Assert.assertEquals(configuration.getStatisticsCalculationStrategy().name(), resource.getStatisticsStrategy());
        Assert.assertTrue(subTypes.size() == resource.getSubTypes().size());

        Assert.assertTrue(resource.getSubTypes().containsKey(TestItemIssueType.PRODUCT_BUG.name()));
        IssueSubTypeResource subTypeResource = resource.getSubTypes().get(TestItemIssueType.PRODUCT_BUG.name()).get(0);
        Assert.assertEquals(subType.getHexColor(), subTypeResource.getColor());
        Assert.assertEquals(subType.getLocator(), subTypeResource.getLocator());
        Assert.assertEquals(subType.getLongName(), subTypeResource.getLongName());
        Assert.assertEquals(subType.getShortName(), subTypeResource.getShortName());
        Assert.assertEquals(subType.getTypeRef(), subTypeResource.getTypeRef());

    }

}