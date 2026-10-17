package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.aifactory.dto.PipelineDefinitionRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IngestFingerprintTest {

  private PipelineIterationRQ rq(Map<String, Object> document, String status) {
    return PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.named("p"))
        .stages(List.of(PipelineStageRQ.builder().stageKey("s").sequence(1).status(status).document(document).build()))
        .build();
  }

  @Test
  void of_ShouldNotDependOnMapEntryOrder() {
    var a = new LinkedHashMap<String, Object>();
    a.put("x", 1);
    a.put("y", 2);
    var b = new LinkedHashMap<String, Object>();
    b.put("y", 2);
    b.put("x", 1);

    assertEquals(IngestFingerprint.of(rq(a, "Passed")), IngestFingerprint.of(rq(b, "Passed")));
  }

  @Test
  void of_ShouldDifferWhenBodyDiffers() {
    assertNotEquals(IngestFingerprint.of(rq(null, "Passed")), IngestFingerprint.of(rq(null, "Failed")));
  }

  @Test
  void sizeInBytes_ShouldGrowWithThePayload() {
    var small = IngestFingerprint.sizeInBytes(rq(null, "Passed"));
    var large = IngestFingerprint.sizeInBytes(rq(Map.of("blob", "x".repeat(10_000)), "Passed"));

    assertTrue(large > small + 10_000 - 1);
  }
}
