package com.epam.ta.reportportal.core.widget.content.loader.materialized;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableContent;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import com.epam.ta.reportportal.entity.widget.content.healthcheck.LevelEntry;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.SortEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_PASSED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_TOTAL;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service(value = "healthCheckTableReadyContentLoader")
public class HealthCheckTableReadyContentLoader implements MaterializedWidgetContentLoader {

	public static final String SORT = "sort";
	public static final String CUSTOM_COLUMN = "customColumn";

	public static final String TOTAL = "total";
	public static final String STATISTICS = "statistics";
	public static final String PASSING_RATE = "passingRate";

	private final WidgetContentRepository widgetContentRepository;
	private final ObjectMapper objectMapper;

	@Autowired
	public HealthCheckTableReadyContentLoader(WidgetContentRepository widgetContentRepository, ObjectMapper objectMapper) {
		this.widgetContentRepository = widgetContentRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params) {
		HealthCheckTableGetParams getParams = getParams(widget.getWidgetOptions(),
				ofNullable(params.get(ATTRIBUTES)).map(attributes -> attributes.stream()
						.filter(StringUtils::isNotBlank)
						.collect(Collectors.toList())).orElseGet(Collections::emptyList)
		);
		List<HealthCheckTableContent> content = widgetContentRepository.componentHealthCheckTable(getParams);

		if (CollectionUtils.isEmpty(content)) {
			return emptyMap();
		}

		Map<String, Integer> totalStatistics = content.stream()
				.map(HealthCheckTableContent::getStatistics)
				.map(Map::entrySet)
				.flatMap(Collection::stream)
				.collect(groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));

		return ImmutableMap.<String, Object>builder().put(RESULT, content)
				.put(TOTAL,
						ImmutableMap.<String, Object>builder().put(STATISTICS, totalStatistics)
								.put(PASSING_RATE, calculatePassingRate(totalStatistics))
								.build()
				)
				.build();
	}

	private HealthCheckTableGetParams getParams(WidgetOptions widgetOptions, List<String> attributeValues) {
		List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widgetOptions);
		int currentLevel = attributeValues.size();
		BusinessRule.expect(attributeKeys, keys -> keys.size() > currentLevel)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Incorrect level definition");

		String viewName = ofNullable(WidgetOptionUtil.getValueByKey(VIEW_NAME, widgetOptions)).orElseThrow(() -> new ReportPortalException(
				ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
				"Widget view name not provided"
		));
		String currentLevelKey = attributeKeys.get(currentLevel);
		boolean includeCustomColumn = ofNullable(WidgetOptionUtil.getValueByKey(CUSTOM_COLUMN, widgetOptions)).isPresent();

		return HealthCheckTableGetParams.of(viewName,
				currentLevelKey,
				resolveSort(widgetOptions),
				includeCustomColumn,
				getLevelEntries(attributeKeys, attributeValues)
		);

	}

	private Sort resolveSort(WidgetOptions widgetOptions) {
		return ofNullable(widgetOptions).flatMap(wo -> ofNullable(wo.getOptions()).map(options -> options.get(SORT))).map(s -> {
			try {
				SortEntry sortEntry = objectMapper.readValue(objectMapper.writeValueAsString(s), SortEntry.class);
				return Sort.by(sortEntry.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC, sortEntry.getSortingColumn());
			} catch (JsonProcessingException e) {
				throw new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Sort format error: " + e.getMessage());
			}
		}).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Sort parameter not provided"));
	}

	private List<LevelEntry> getLevelEntries(List<String> attributeKeys, List<String> attributeValues) {
		return IntStream.range(0, attributeValues.size()).mapToObj(index -> {
			String attributeKey = attributeKeys.get(index);
			String attributeValue = attributeValues.get(index);
			return LevelEntry.of(attributeKey, attributeValue);
		}).collect(Collectors.toList());
	}

	private double calculatePassingRate(Map<String, Integer> totalStatistics) {
		double passingRate = 100.0 * totalStatistics.getOrDefault(EXECUTIONS_PASSED, 0) / totalStatistics.getOrDefault(EXECUTIONS_TOTAL, 1);
		return new BigDecimal(passingRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

}
