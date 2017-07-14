package com.epam.ta.reportportal.core.item;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.Parameters;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *
 * @author Pavel_Bortnik
 */
@SpringFixture("triggerTests")
public class UniqueIdGeneratorTest extends BaseTest {

    @Rule
    @Autowired
    public SpringFixtureRule dfRule;

    @Autowired
    private TestItemRepository testItemRepository;

    @Autowired
    private UniqueIdGenerator identifierGenerator;

    private static final String ITEM = "44524cc1553de753b3e5ab2f";
    private static final String PROJECT = "DEFAULT";

    @Test
    public void generateUniqueId() throws Exception {
        TestItem item = testItemRepository.findOne(ITEM);
        String s1 = identifierGenerator.generate(item, PROJECT);
        String s2 = identifierGenerator.generate(item, PROJECT);
        Assert.assertEquals(s1, s2);
        item.setParameters(getParameters());
        String s3 = identifierGenerator.generate(item, PROJECT);
        Assert.assertNotEquals(s1, s3);

        item.setName("Different");
        item.setParameters(null);
        String s4 = identifierGenerator.generate(item, PROJECT);
        Assert.assertNotEquals(s3, s4);
    }

    private List<Parameters> getParameters() {
        Parameters parameters = new Parameters();
        parameters.setKey("CardNumber");
        parameters.setValue("4444333322221111");
        Parameters parameters1 = new Parameters();
        parameters1.setKey("Stars");
        parameters1.setValue("2 stars");
        return ImmutableList.<Parameters>builder().add(parameters).add(parameters1).build();
    }
}