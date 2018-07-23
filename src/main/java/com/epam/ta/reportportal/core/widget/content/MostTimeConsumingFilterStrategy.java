package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.core.widget.content.history.LastLaunchFilterStrategy;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader.RESULT;

/**
 * @author Pavel Bortnik
 */
@Service
public class MostTimeConsumingFilterStrategy extends LastLaunchFilterStrategy {

	private final static String INCLUDE_METHODS = "include_methods";

	private TestItemRepository testItemRepository;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public Map<String, ?> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		Optional<Launch> lastLaunch = getLastLaunch(contentOptions, projectName);
		if (!lastLaunch.isPresent()) {
			return Collections.emptyMap();
		}
		Map<String, List<?>> res = new HashMap<>(RESULTED_MAP_SIZE);
		Launch last = lastLaunch.get();

		Filter filter = createFilter(last.getId(), contentOptions);
		res.put(RESULT, testItemRepository.findMostTimeConsumingTestItems(filter, contentOptions.getItemsCount()));

		ChartObject lastLaunchChartObject = new ChartObject();
		lastLaunchChartObject.setName(last.getName());
		lastLaunchChartObject.setNumber(last.getNumber().toString());
		lastLaunchChartObject.setId(last.getId());
		res.put(LAST_FOUND_LAUNCH, Collections.singletonList(lastLaunchChartObject));
		return res;
	}

	private Filter createFilter(String lastId, ContentOptions contentOptions) {
		Set<FilterCondition> filterConditions = new HashSet<>();
		filterConditions.add(new FilterCondition(Condition.EQUALS, false, lastId, TestItem.LAUNCH_CRITERIA));
		filterConditions.add(new FilterCondition(Condition.EQUALS, false, "false", "has_childs"));

		if (!contentOptions.getWidgetOptions().containsKey(INCLUDE_METHODS)) {
			filterConditions.add(new FilterCondition(Condition.EQUALS, false, "STEP", "type"));
		}

		return new Filter(TestItem.class, filterConditions);
	}
}
