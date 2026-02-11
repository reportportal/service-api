package com.epam.reportportal.base.infrastructure.persistence.dao.widget.healthcheck.content;

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PASSING_RATE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.epam.reportportal.base.infrastructure.persistence.dao.widget.WidgetProviderChain;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableContent;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableStatisticsContent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class HealthCheckTableChain implements
    WidgetProviderChain<HealthCheckTableGetParams, List<HealthCheckTableContent>> {

  private static final Set<String> ALLOWED_SORTING = Sets.newHashSet(PASSING_RATE);

  private final WidgetProviderChain<HealthCheckTableGetParams, Map<String, HealthCheckTableStatisticsContent>> healthCheckTableStatisticsChain;
  private final WidgetProviderChain<HealthCheckTableGetParams, Map<String, List<String>>> healthCheckTableColumnChain;

  @Autowired
  public HealthCheckTableChain(
      WidgetProviderChain<HealthCheckTableGetParams, Map<String, HealthCheckTableStatisticsContent>> healthCheckTableStatisticsChain,
      WidgetProviderChain<HealthCheckTableGetParams, Map<String, List<String>>> healthCheckTableColumnChain) {
    this.healthCheckTableStatisticsChain = healthCheckTableStatisticsChain;
    this.healthCheckTableColumnChain = healthCheckTableColumnChain;
  }

  @Override
  public List<HealthCheckTableContent> apply(HealthCheckTableGetParams params) {

    boolean contentFirst =
        healthCheckTableStatisticsChain.resolvePriority(params)
            - healthCheckTableColumnChain.resolvePriority(params) >= 0;
    List<HealthCheckTableContent> result = concat(contentFirst,
        healthCheckTableStatisticsChain.apply(params),
        healthCheckTableColumnChain.apply(params)
    );

    return params.getSort().get().filter(order -> ALLOWED_SORTING.contains(order.getProperty()))
        .findFirst().map(it -> {
          Comparator<HealthCheckTableContent> comparator = Comparator.comparingDouble(
              HealthCheckTableContent::getPassingRate);
          return result.stream().sorted(it.isAscending() ? comparator : comparator.reversed())
              .collect(toList());
        }).orElse(result);
  }

  private List<HealthCheckTableContent> concat(boolean contentFirst,
      Map<String, HealthCheckTableStatisticsContent> content,
      Map<String, List<String>> columnMapping) {

    List<HealthCheckTableContent> result = Lists.newArrayListWithExpectedSize(content.size());

    if (contentFirst) {
      content.forEach((key, statistics) -> {
        HealthCheckTableContent resultEntry = entryFromStatistics(key, statistics);
        ofNullable(columnMapping.get(key)).ifPresent(resultEntry::setCustomValues);
        result.add(resultEntry);
      });
    } else {
      columnMapping.forEach((key, attributes) ->
          ofNullable(content.remove(key))
              .map(statisticsContent -> entryFromStatistics(key, statisticsContent))
              .ifPresent(resultEntry -> {
                resultEntry.setCustomValues(attributes);
                result.add(resultEntry);
              }));

      content.forEach((key, statistics) -> result.add(entryFromStatistics(key, statistics)));
    }
    return result;
  }

  private HealthCheckTableContent entryFromStatistics(String key,
      HealthCheckTableStatisticsContent statisticsContent) {
    HealthCheckTableContent resultEntry = new HealthCheckTableContent();
    resultEntry.setAttributeValue(key);
    resultEntry.setPassingRate(statisticsContent.getPassingRate());
    resultEntry.setStatistics(statisticsContent.getStatistics());
    return resultEntry;
  }

}
