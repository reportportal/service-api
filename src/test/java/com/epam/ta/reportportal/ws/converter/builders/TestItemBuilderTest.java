package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.Parameter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemBuilderTest {

	@Test
	void testItemBuilder() {
		final Launch launch = new Launch();
		launch.setId(1L);
		launch.setName("name");
		final ParameterResource parameterResource = new ParameterResource();
		parameterResource.setKey("key");
		parameterResource.setValue("value");
		final String description = "description";
		final String typeValue = "step";
		final TestItem parent = new TestItem();
		final TestItem testItem = new TestItemBuilder().addDescription(description)
				.addType(typeValue)
				.addLaunch(launch)
				.addParameters(Collections.singletonList(parameterResource))
				.addAttributes(Sets.newHashSet(new ItemAttributesRQ("key", "value")))
				.addParent(parent)
				.get();

		assertThat(testItem.getLaunch()).isEqualToComparingFieldByField(launch);
		assertEquals(description, testItem.getDescription());
		assertEquals(TestItemTypeEnum.STEP, testItem.getType());
		final Parameter param = new Parameter();
		param.setKey("key");
		param.setValue("value");
		assertTrue(testItem.getParameters().contains(param));
		assertThat(testItem.getAttributes()).containsExactly(new ItemAttribute("key", "value", false));
		assertNotNull(testItem.getParent());
	}

	@Test
	void addStartRqTest() {
		final StartTestItemRQ rq = new StartTestItemRQ();
		rq.setType("step");
		final ParameterResource parameterResource = new ParameterResource();
		parameterResource.setKey("key");
		parameterResource.setValue("value");
		rq.setParameters(Collections.singletonList(parameterResource));
		final String uuid = "uuid";
		rq.setUniqueId(uuid);
		final String description = "description";
		rq.setDescription(description);
		final LocalDateTime now = LocalDateTime.now();
		rq.setStartTime(Date.from(now.atZone(ZoneId.of("UTC")).toInstant()));
		final String name = "name";
		rq.setName(name);

		final TestItem testItem = new TestItemBuilder().addStartItemRequest(rq).get();

		assertEquals(TestItemTypeEnum.STEP, testItem.getType());
		final Parameter param = new Parameter();
		param.setKey("key");
		param.setValue("value");
		assertTrue(testItem.getParameters().contains(param));
		assertEquals(uuid, testItem.getUniqueId());
		assertEquals(description, testItem.getDescription());
		assertEquals(now, testItem.getStartTime());
		assertEquals(name, testItem.getName());
	}

	@Test
	void addResultsTest() {
		TestItem item = new TestItem();
		final LocalDateTime now = LocalDateTime.now();
		item.setStartTime(now);
		final TestItemResults itemResults = new TestItemResults();
		itemResults.setEndTime(now.plusSeconds(120));
		item.setItemResults(itemResults);
		final ItemAttribute systemAttribute = new ItemAttribute("key", "val", true);
		item.setAttributes(Sets.newHashSet(new ItemAttribute("key", "val", false), systemAttribute));

		final TestItem resultItem = new TestItemBuilder(item).addTestItemResults(itemResults)
				.addStatus(StatusEnum.PASSED)
				.overwriteAttributes(Sets.newHashSet(new ItemAttributeResource("k", "v")))
				.get();

		assertEquals(120, resultItem.getItemResults().getDuration(), 0.1);
		assertEquals(StatusEnum.PASSED, resultItem.getItemResults().getStatus());
		assertThat(resultItem.getAttributes()).containsExactlyInAnyOrder(systemAttribute, new ItemAttribute("k", "v", false));
	}
}