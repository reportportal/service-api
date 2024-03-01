package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.widget.content.updater.validator.ActivityContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.BugTrendChartContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.CasesTrendContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.ChartInvestigatedContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.ComponentHealthCheckContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.CumulativeTrendChartValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.FlakyCasesTableContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.LaunchExecutionAndIssueStatisticsContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.LaunchesComparisonContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.LaunchesDurationContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.LaunchesTableContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.LineChartContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.MostTimeConsumingContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.MultilevelValidatorStrategy;
import com.epam.ta.reportportal.core.widget.content.updater.validator.NotPassedTestsContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.OverallStatisticsContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.PassingRatePerLaunchContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.PassingRateSummaryContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.ProductStatusContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.TopPatternContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.TopTestCasesContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.UniqueBugContentValidator;
import com.epam.ta.reportportal.core.widget.content.updater.validator.WidgetValidatorStrategy;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WidgetValidatorConfig {

  private ApplicationContext applicationContext;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Bean("widgetValidatorLoader")
  public Map<WidgetType, WidgetValidatorStrategy> widgetValidatorLoader() {
    return ImmutableMap.<WidgetType, WidgetValidatorStrategy>builder()
        .put(WidgetType.FLAKY_TEST_CASES,
            applicationContext.getBean(FlakyCasesTableContentValidator.class)
        )
        .put(WidgetType.OVERALL_STATISTICS,
            applicationContext.getBean(OverallStatisticsContentValidator.class))
        .put(WidgetType.PASSING_RATE_SUMMARY,
            applicationContext.getBean(PassingRateSummaryContentValidator.class))
        .put(WidgetType.OLD_LINE_CHART, applicationContext.getBean(LineChartContentValidator.class))
        .put(WidgetType.INVESTIGATED_TREND,
            applicationContext.getBean(ChartInvestigatedContentValidator.class))
        .put(WidgetType.STATISTIC_TREND,
            applicationContext.getBean(LineChartContentValidator.class))
        .put(WidgetType.LAUNCH_STATISTICS,
            applicationContext.getBean(LaunchExecutionAndIssueStatisticsContentValidator.class))
        .put(WidgetType.CASES_TREND, applicationContext.getBean(CasesTrendContentValidator.class))
        .put(WidgetType.NOT_PASSED,
            applicationContext.getBean(NotPassedTestsContentValidator.class))
        .put(WidgetType.UNIQUE_BUG_TABLE,
            applicationContext.getBean(UniqueBugContentValidator.class))
        .put(WidgetType.BUG_TREND, applicationContext.getBean(BugTrendChartContentValidator.class))
        .put(WidgetType.ACTIVITY, applicationContext.getBean(ActivityContentValidator.class))
        .put(WidgetType.LAUNCHES_COMPARISON_CHART,
            applicationContext.getBean(LaunchesComparisonContentValidator.class))
        .put(WidgetType.LAUNCHES_DURATION_CHART,
            applicationContext.getBean(LaunchesDurationContentValidator.class))
        .put(WidgetType.LAUNCHES_TABLE,
            applicationContext.getBean(LaunchesTableContentValidator.class))
        .put(WidgetType.TOP_TEST_CASES,
            applicationContext.getBean(TopTestCasesContentValidator.class))
        .put(WidgetType.PASSING_RATE_PER_LAUNCH,
            applicationContext.getBean(PassingRatePerLaunchContentValidator.class))
        .put(WidgetType.MOST_TIME_CONSUMING,
            applicationContext.getBean(MostTimeConsumingContentValidator.class))
        .put(WidgetType.PRODUCT_STATUS,
            applicationContext.getBean(ProductStatusContentValidator.class))
        .build();
  }

  @Bean("multilevelValidatorLoader")
  public Map<WidgetType, MultilevelValidatorStrategy> multilevelValidatorLoader() {
    return ImmutableMap.<WidgetType, MultilevelValidatorStrategy>builder()
        .put(WidgetType.CUMULATIVE,
            applicationContext.getBean(CumulativeTrendChartValidator.class)
        )
        .put(WidgetType.TOP_PATTERN_TEMPLATES,
            applicationContext.getBean(TopPatternContentValidator.class))
        .put(WidgetType.COMPONENT_HEALTH_CHECK,
            applicationContext.getBean(ComponentHealthCheckContentValidator.class))
        .put(WidgetType.COMPONENT_HEALTH_CHECK_TABLE,
            applicationContext.getBean(ComponentHealthCheckContentValidator.class))
        .build();
  }
}
