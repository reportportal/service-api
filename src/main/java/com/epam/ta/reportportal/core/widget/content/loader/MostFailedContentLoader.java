package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.MostFailedObject;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.jooq.Tables.*;

@Service
public class MostFailedContentLoader implements LoadContentStrategy {

	private static final String LAUNCH_NAME_FIELD = "launch_name_filter";
	private static final String EXECUTION_CRITERIA = "execution_criteria";
	private static final String ISSUE_CRITERIA = "issue_criteria";

	@Autowired
	private LaunchRepository launchRepository;

	private TestItemRepository testItemRepository;

	@Autowired
	private DSLContext dslContext;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Set<WidgetOption> widgetOptions) {
		Map<String, Set<String>> options = validateWidgetOptions(widgetOptions);
		Launch latestByName = launchRepository.findLatestByName(options.get(LAUNCH_NAME_FIELD).iterator().next());

		List<MostFailedObject> content;

		if (options.containsKey(EXECUTION_CRITERIA)) {
			content = loadByExecutionCriteria(options.get(LAUNCH_NAME_FIELD).iterator().next(),
					options.get(EXECUTION_CRITERIA).iterator().next()
			);
		} else {
			content = loadByIssueCriteria(options.get(LAUNCH_NAME_FIELD).iterator().next(), options.get(ISSUE_CRITERIA).iterator().next());
		}
		Map<String, Object> res = new HashMap<>(2);
		res.put("latestLaunch", LaunchConverter.TO_RESOURCE.apply(latestByName));
		res.put(RESULT, content);
		return res;
	}

	private List<MostFailedObject> loadByIssueCriteria(String launchName, String criteria) {
		return dslContext.with("history")
				.as(dslContext.select(TEST_ITEM.UNIQUE_ID,
						TEST_ITEM.NAME,
						DSL.arrayAgg(DSL.when(ISSUE_GROUP.ISSUE_GROUP_.eq(DSL.cast(criteria.toUpperCase(), ISSUE_GROUP.ISSUE_GROUP_)),
								"true"
						)
								.otherwise("false"))
								.orderBy(LAUNCH.NAME.asc())
								.as("status_history"),
						DSL.sum(DSL.when(ISSUE_GROUP.ISSUE_GROUP_.eq(DSL.cast(criteria.toUpperCase(), ISSUE_GROUP.ISSUE_GROUP_)), 1)
								.otherwise(0))
								.as("criteria"),
						DSL.count(TEST_ITEM_RESULTS.RESULT_ID).as("total")
				)
						.from(LAUNCH)
						.join(TEST_ITEM_STRUCTURE)
						.on(LAUNCH.ID.eq(TEST_ITEM_STRUCTURE.LAUNCH_ID))
						.join(TEST_ITEM_RESULTS)
						.on(TEST_ITEM_STRUCTURE.STRUCTURE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
						.join(TEST_ITEM)
						.on(TEST_ITEM_STRUCTURE.STRUCTURE_ID.eq(TEST_ITEM.ITEM_ID))
						.leftJoin(ISSUE)
						.on(TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID))
						.leftJoin(ISSUE_TYPE)
						.on(ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
						.leftJoin(ISSUE_GROUP)
						.on(ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID))
						.where(TEST_ITEM.TYPE.eq(JTestItemTypeEnum.STEP))
						.and(LAUNCH.NAME.eq(launchName))
						.groupBy(TEST_ITEM.UNIQUE_ID, TEST_ITEM.NAME))
				.select()
				.from(DSL.table(DSL.name("history")))
				.orderBy(DSL.field(DSL.name("criteria")).desc(), DSL.field(DSL.name("total")).asc())
				.fetchInto(MostFailedObject.class);
	}

	private List<MostFailedObject> loadByExecutionCriteria(String launchName, String criteria) {

		return dslContext.with("history")
				.as(dslContext.select(TEST_ITEM.UNIQUE_ID,
						TEST_ITEM.NAME,
						DSL.arrayAgg(DSL.when(TEST_ITEM_RESULTS.STATUS.eq(DSL.cast(criteria.toUpperCase(), TEST_ITEM_RESULTS.STATUS)),
								"true"
						)
								.otherwise("false"))
								.orderBy(LAUNCH.NAME.asc())
								.as("status_history"),
						DSL.sum(DSL.when(TEST_ITEM_RESULTS.STATUS.eq(DSL.cast(criteria.toUpperCase(), TEST_ITEM_RESULTS.STATUS)), 1)
								.otherwise(0))
								.as("criteria"),
						DSL.count(TEST_ITEM_RESULTS.STATUS).as("total")
				)
						.from(LAUNCH)
						.join(TEST_ITEM_STRUCTURE)
						.on(LAUNCH.ID.eq(TEST_ITEM_STRUCTURE.LAUNCH_ID))
						.join(TEST_ITEM_RESULTS)
						.on(TEST_ITEM_STRUCTURE.STRUCTURE_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
						.join(TEST_ITEM)
						.on(TEST_ITEM_STRUCTURE.STRUCTURE_ID.eq(TEST_ITEM.ITEM_ID))
						.where(TEST_ITEM.TYPE.eq(JTestItemTypeEnum.STEP))
						.and(LAUNCH.NAME.eq(launchName))
						.groupBy(TEST_ITEM.UNIQUE_ID, TEST_ITEM.NAME))
				.select()
				.from(DSL.table(DSL.name("history")))
				.orderBy(DSL.field(DSL.name("criteria")).desc(), DSL.field(DSL.name("total")).asc())
				.fetchInto(MostFailedObject.class);
	}

	private Map<String, Set<String>> validateWidgetOptions(Set<WidgetOption> widgetOptions) {
		Map<String, Set<String>> res = Optional.ofNullable(widgetOptions)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT))
				.stream()
				.collect(Collectors.toMap(WidgetOption::getWidgetOption, WidgetOption::getValues));

		BusinessRule.expect(res.containsKey(LAUNCH_NAME_FIELD), Predicate.isEqual(true))
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");

		BusinessRule.expect(res.containsKey(EXECUTION_CRITERIA) ^ res.containsKey(ISSUE_CRITERIA), Predicate.isEqual(true))
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
						"One of widget options " + EXECUTION_CRITERIA + ", " + ISSUE_CRITERIA + " should be specified for widget."
				);
		return res;
	}
}
