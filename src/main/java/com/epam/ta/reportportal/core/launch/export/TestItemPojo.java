/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.launch.export;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.ta.reportportal.core.jasper.util.ExportUtils.COMMENT_PREFIX;
import static com.epam.ta.reportportal.core.jasper.util.ExportUtils.DESCRIPTION_PREFIX;
import static com.epam.ta.reportportal.core.jasper.util.ExportUtils.adjustName;
import static com.epam.ta.reportportal.core.jasper.util.ExportUtils.getStatisticsCounter;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_AUTOMATION_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_NO_DEFECT_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_PRODUCT_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_SYSTEM_ISSUE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_TO_INVESTIGATE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_FAILED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_PASSED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_SKIPPED;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_TOTAL;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Data
public class TestItemPojo {

  private static final Double EMPTY_DURATION = 0D;
  private Long id;
  private String type;
  private String itemName;
  private String name;
  private String path;
  private String status;
  private Double duration;
  private Integer total;
  private Integer passed;
  private Integer failed;
  private Integer skipped;
  private Integer automationBug;
  private Integer productBug;
  private Integer systemIssue;
  private Integer noDefect;
  private Integer toInvestigate;
  private Instant startTime;
  private Set<AttachmentPojo> attachmentPojoList;

  public TestItemPojo(TestItem input) {
    this.id = input.getItemId();
    this.type = input.getType().name();
    this.itemName = input.getName();
    this.path = input.getPath();
    Optional<String> issueDescription = Optional.empty();
    if (input.getItemResults().getIssue() != null) {
      issueDescription = ofNullable(input.getItemResults().getIssue().getIssueDescription()).map(
          it -> COMMENT_PREFIX + it);
    }

    Optional<String> description = ofNullable(input.getDescription()).map(
        it -> DESCRIPTION_PREFIX + it);

    this.name = adjustName(input) + description.orElse(EMPTY_STRING) + issueDescription.orElse(
        EMPTY_STRING);
    this.status = input.getItemResults().getStatus().name();

    this.duration = Objects.nonNull(input.getItemResults().getEndTime()) ?
        Duration.between(input.getStartTime(), input.getItemResults().getEndTime()).toMillis()
            / (double) org.apache.commons.lang3.time.DateUtils.MILLIS_PER_SECOND :
        EMPTY_DURATION;

    Set<Statistics> statistics = input.getItemResults().getStatistics();

    this.total = getStatisticsCounter(statistics, EXECUTIONS_TOTAL);
    this.passed = getStatisticsCounter(statistics, EXECUTIONS_PASSED);
    this.failed = getStatisticsCounter(statistics, EXECUTIONS_FAILED);
    this.skipped = getStatisticsCounter(statistics, EXECUTIONS_SKIPPED);

    this.automationBug = getStatisticsCounter(statistics, DEFECTS_AUTOMATION_BUG_TOTAL);
    this.productBug = getStatisticsCounter(statistics, DEFECTS_PRODUCT_BUG_TOTAL);
    this.systemIssue = getStatisticsCounter(statistics, DEFECTS_SYSTEM_ISSUE_TOTAL);
    this.noDefect = getStatisticsCounter(statistics, DEFECTS_NO_DEFECT_TOTAL);
    this.toInvestigate = getStatisticsCounter(statistics, DEFECTS_TO_INVESTIGATE_TOTAL);

    if (!CollectionUtils.isEmpty(input.getAttachments())) {
      this.attachmentPojoList = input.getAttachments().stream().filter(Objects::nonNull)
          .peek(attachment -> this.type = this.type + "\n" + attachment.getFileName())
          .map(it -> AttachmentPojo.builder().fileId(it.getFileId()).fileName(it.getFileName()).build())
          .collect(Collectors.toSet());
    }
  }
}
