package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.service.PipelineRelatedEntityResolver;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.PipelineStageTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Links a stage to the test cases it reports as related entities of type {@code TEST_CASE}, by display id.
 * <p>
 * Example: the Upload stage reports {@code [{entityType: "TEST_CASE", entityId: "TC-12"}]}; this resolver
 * finds TC-12 in the project's Test Case Library and links it to the stage, so the Review stage can later
 * count how many of the iteration's test cases are Ready. An id that does not exist (e.g. "TC-404") is
 * returned as not found.
 */
@Component
@RequiredArgsConstructor
public class TestCaseRelatedEntityResolver implements PipelineRelatedEntityResolver {

  public static final String TYPE = "TEST_CASE";

  private final TmsTestCaseService tmsTestCaseService;
  private final PipelineStageTestCaseRepository stageTestCaseRepository;

  @Override
  public String entityType() {
    return TYPE;
  }

  @Override
  public List<String> resolve(Long projectId, Pipeline pipeline, PipelineStage stage, List<String> displayIds) {
    stageTestCaseRepository.deleteByStage_Id(stage.getId());
    if (displayIds.isEmpty()) {
      return List.of();
    }

    var testCasesByDisplayId = tmsTestCaseService.getEntitiesByDisplayIds(projectId, displayIds).stream()
        .collect(Collectors.toMap(TmsTestCase::getDisplayId, Function.identity()));

    var links = new ArrayList<PipelineStageTestCase>();
    var notFound = new ArrayList<String>();
    for (var displayId : displayIds) {
      var testCase = testCasesByDisplayId.get(displayId);
      if (testCase == null) {
        notFound.add(displayId);
        continue;
      }
      var link = new PipelineStageTestCase();
      link.setStage(stage);
      link.setTestCase(testCase);
      links.add(link);
    }
    if (!links.isEmpty()) {
      stageTestCaseRepository.saveAll(links);
    }

    return notFound;
  }
}
