package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

@SpringFixture(value = "statistics")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StatisticsFacadeImplTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	@Qualifier(value = "statisticsFacadeImpl")
	private StatisticsFacade statisticsFacade;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Test
	public void deleteIssueStatistics() throws Exception {
		final String item = "44524cc1553de743b3e5aa28";
		TestItem testItem = testItemRepository.findOne(item);
		statisticsFacade.deleteIssueStatistics(testItem);
		Launch launch = launchRepository.findOne(testItem.getLaunchRef());
		testItem = testItemRepository.findOne(item);
		launch.getStatistics().getIssueCounter().getProductBug().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		launch.getStatistics().getIssueCounter().getAutomationBug().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		launch.getStatistics().getIssueCounter().getToInvestigate().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		launch.getStatistics().getIssueCounter().getSystemIssue().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		launch.getStatistics().getIssueCounter().getNoDefect().values().forEach(val -> Assert.assertEquals(0, val.intValue()));

		testItem.getStatistics().getIssueCounter().getProductBug().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		testItem.getStatistics().getIssueCounter().getAutomationBug().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		testItem.getStatistics().getIssueCounter().getToInvestigate().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		testItem.getStatistics().getIssueCounter().getSystemIssue().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
		testItem.getStatistics().getIssueCounter().getNoDefect().values().forEach(val -> Assert.assertEquals(0, val.intValue()));
	}

}