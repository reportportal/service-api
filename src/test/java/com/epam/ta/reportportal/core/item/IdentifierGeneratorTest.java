package com.epam.ta.reportportal.core.item;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Pavel_Bortnik
 */
@SpringFixture("triggerTests")
public class IdentifierGeneratorTest extends BaseTest {

    @Rule
    @Autowired
    public SpringFixtureRule dfRule;

    @Autowired
    private TestItemRepository testItemRepository;

    @Autowired
    private TestItemIdentifierGenerator identifierGenerator;

    private static final String ITEM = "44524cc1553de753b3e5ab2f";

    @Test
    public void generateUniqueHash() throws Exception {
        TestItem item = testItemRepository.findOne(ITEM);
        String s1 = identifierGenerator.generate(item);
        String s2 = identifierGenerator.generate(item);
        Assert.assertEquals(s1, s2);

        item.setParameters(ImmutableList.<String>builder().add("CardNumber=4444333322221111")
                .add("Loyalty level=TrueBlue with Family Pooling").add("Stars=2 stars").build());

        String s3 = identifierGenerator.generate(item);
        Assert.assertNotEquals(s1, s3);

        item.setName("Different");
        item.setParameters(null);
        String s4 = identifierGenerator.generate(item);
        Assert.assertNotEquals(s3, s4);
    }
}